# seckill
秒杀项目 springboot+mybatis+nginx+spring-session+rabbitMq+Redis
## 启动服务时预热加载秒杀数据到redis
更全的方案时，在每天的凌晨四点加载当天的秒杀数据到redis
首先controller类要先实现InitializingBean接口
public class SeckillController implements InitializingBean
    /**
     * 预热加载秒杀数据 这里方便测试 直接把所有数据都加载
     * 真实的场景 应该是做一个定时器脚本 每天凌晨4点扫描明天要秒杀的活动 存放到redis
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<SeckillInfo> seckillGoodsList = seckillService.listAllSeckill();
        // 存到redis 并设置存活时间
        for (SeckillInfo seckillInfo: seckillGoodsList
             ) {
            Long mills = Duration.between(LocalDateTime.now(),seckillInfo.getEndDate()).toMillis();
            if (mills < 0){
                continue;
            }
            // TODO 分开存储 存库存 还有商品的基本信息
            redisTemplate.opsForValue().set(String.valueOf(seckillInfo.getSeckillGoodsId()),seckillInfo.getStock(),
                    mills, TimeUnit.MILLISECONDS);
            redisTemplate.opsForValue().set(seckillInfo.getSeckillGoodsId()+"_info",seckillInfo);
        }
    }

## 秒杀前获取秒杀令牌
只有令牌处于合法之后，才能进入对应的秒杀下单的逻辑
事先安排好令牌数量，防止令牌无限生成，影响系统性能
   @Override
    public String getSeckillToken(Long seckillId, Long goodsId, Long userId) {

        // 判断对应的秒杀商品是否已卖完
        if ((int)redisTemplate.opsForValue().get(goodsId) < 0){
            throw new GlobalException(CommonEnum.STOCK_NOT_ENOUGH);
        }

        SeckillInfo seckillInfo = (SeckillInfo) redisTemplate.opsForValue().get(goodsId+"_info");
        if (seckillInfo == null){
            throw new GlobalException(CommonEnum.PARAMETER_VALIDATION_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        // 判断秒杀时间是否合理
        if (seckillInfo.getStartDate().isAfter(now) || seckillInfo.getEndDate().isBefore(now)){
            throw new GlobalException(CommonEnum.NOT_IN_SECKILLING);
        }

        // 获取该商品秒杀大闸的数量
        long num = redisTemplate.opsForValue().increment("seckill_door_"+seckillId,-1);
        if (num < 0){
            // 实际上已经拿不到令牌了 但是友好提示一下 不要让用户知道他被淘汰了
            throw  new GlobalException(CommonEnum.SECKILLING);
        }

        // 生成秒杀Token，设置有效时间5分钟
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set("seckill_token_"+seckillId+"_"+userId,token,5, TimeUnit.MINUTES);

        return token;
    }
## 解决买超
使用redis预减库存
rabbitmq队列异步化下单
数据库加唯一索引防止重复购买
sql判断防止为负数
客户端定时轮询是否秒杀成功
### controller层

        // 预减库存 库存不足则返回
        long stock = redisTemplate.opsForValue().increment(String.valueOf(orderInfo.getGoodsId()),-1) ;
        if (stock < 0){
            return ResultBody.success(CommonEnum.STOCK_NOT_ENOUGH.getResultCode(),CommonEnum.STOCK_NOT_ENOUGH.getResultMsg(),"");
        }

        // 判断是否已经抢到了 抢到了就不给他继续抢了
        if (redisTemplate.hasKey(orderInfo.getUserId() + Constant.LINKSTRING + orderInfo.getSeckillId())){
            return ResultBody.success(CommonEnum.REPEATE_SECKILL.getResultCode(),CommonEnum.REPEATE_SECKILL.getResultMsg(),"");
        }
        
        // 进入队列 异步秒杀
        rabbitMqSender.sendSeckillMessage(new SeckillMessage(orderInfo));
        
### 消费队列
            @RabbitListener(queues = RabbitMqConfig.SECKILL_QUEQUE)
    public void receive(SeckillMessage message){
        Long userId = message.getUserId();
        Long goodsId = message.getGoodsId();

        // 再去判断一次是否抢到了 抢到了就不给他继续抢了
        if (redisTemplate.hasKey(userId + Constant.LINKSTRING + goodsId)){
            throw new GlobalException(CommonEnum.REPEATE_SECKILL);
        }

        // 去数据库判断库存
        Stock stock = stockMapper.selectByPrimaryKey(message.getGoodsId());
        if (stock.getStock() <= 0){
            throw new GlobalException(CommonEnum.STOCK_NOT_ENOUGH);
        }

        // 事务执行 减库存 下订单
        seckillService.seckillGoods(message);
    }
    
### 下单操作事务处理，一旦出错回滚加库存
      @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void seckillGoods(SeckillMessage seckillMessage) {
        try {
            // 减库存成功 去下订单
            if(stockService.reduceStock(seckillMessage.getGoodsId())){
                orderService.createOrder(seckillMessage);
            }
        }catch (Exception e){
            // 手动回滚异常 加回库存
            redisTemplate.opsForValue().increment(String.valueOf(seckillMessage.getGoodsId()),1);
        }
    }
    
        @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public boolean reduceStock(Long goodsId) {
        return stockMapper.reduceStock(goodsId, LocalDateTime.now()) == 1;
    }
    
        @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void createOrder(SeckillMessage seckillMessage) {
        OrderInfo orderInfo = new OrderInfo();
        // 使用雪花算法生成订单id
        orderInfo.setOrderId(SnowflakeIdUtil.getSnowflakeId());
        // 商品id
        orderInfo.setGoodsId(seckillMessage.getGoodsId());
        // 购买者id
        orderInfo.setUserId(seckillMessage.getUserId());
        // 购买数量
        orderInfo.setOrderQuantity(seckillMessage.getOrderQuantity());
        // 单价
        orderInfo.setUnitPrice(seckillMessage.getUnitPrice());
        // 订单状态 为已下单未支付  TODO：用户15分钟内未支付取消 思路：在redis里面用map存放为支付的订单 同时启动定时任务
        orderInfo.setOrderStatus(Constant.ORDER_UNPAID);
        // 收货地址id
        orderInfo.setReceiveId(seckillMessage.getReceiveId());
        // 发货地址id
        orderInfo.setShipId(seckillMessage.getShipId());
        // 每一次对订单的操作都要记录
        orderInfo.setUpdateTime(LocalDateTime.now());
        orderInfo.setCreateTime(LocalDateTime.now());
        orderInfoMapper.insert(orderInfo);
        // 标记已经秒杀
        redisTemplate.opsForValue().set(orderInfo.getUserId() + Constant.LINKSTRING + seckillMessage.getSeckillId(),true);
    }
## 防刷限流
Google开源工具包Guava提供了限流工具类RateLimiter基于令牌桶算法实现流量限制，十分高效。令牌式是专门应对突发的并发的，RateLimiter通过限制后面请求的等待时间，来支持一定程度的突发请求(预消费)
令牌桶算法的原理是系统会以一个恒定的速度往桶里放入令牌，而如果请求需要被处理，则需要先从桶里获取一个令牌，当桶里没有令牌可取时，则拒绝服务。

    /**
     * 基于令牌桶算法实现流量限制 适用于突发的高并发
     */
    private RateLimiter rateLimiter;
        
        
    @PostConstruct
    public void init(){
        // 创建一个限流器 每秒生成的300个令牌数
        rateLimiter = RateLimiter.create(300);
    }
     // 如果获取不到限流令牌
     if (!rateLimiter.tryAcquire()){
          return ResultBody.success(CommonEnum.RATELIMIT.getResultCode(),CommonEnum.RATELIMIT.getResultMsg(),"");
     }
   
    

    
## 队列泄洪
防止浪涌流量涌入后台，用排队的策略限制并发流量，依靠排队和下游阻塞窗口程度调整队列释放流量大小。
以支付宝银行网关队列为例，支付宝需要对接许多银行网关，当你的支付宝绑定多张银行卡，那么支付宝对于这些银行都有不同的支付渠道。在大促活动时，支付宝的网关会有上亿级别的流量，银行的网关扛不住，支付宝就会将支付请求队列放到自己的消息队列中，依靠银行网关承诺可以处理的TPS流量去泄洪；
消息队列就像“水库”一样，拦蓄上游的洪水，削减进入下游河道的洪峰流量，从而达到减免洪水灾害的目的；
    @PostConstruct
    public void init(){
        // 定义一个只有20可工作的线程池
        executorService = Executors.newFixedThreadPool(20);
    }
    
            // 同步调用线程池的submit方法 拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(() -> {
            // 进入队列 异步秒杀
            rabbitMqSender.sendSeckillMessage(new SeckillMessage(orderInfo));
            return null;
        });
## 前端部分未完成（前端部分验证码技术防刷）
## redis哨兵机制、redis持久化（未完成）
## 消息队列持久化
