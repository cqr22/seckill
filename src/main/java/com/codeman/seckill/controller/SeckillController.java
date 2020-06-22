package com.codeman.seckill.controller;

import com.codeman.seckill.dto.SeckillInfo;
import com.codeman.seckill.entity.OrderInfo;
import com.codeman.seckill.exception.GlobalException;
import com.codeman.seckill.rabbitMq.RabbitMqSender;
import com.codeman.seckill.rabbitMq.SeckillMessage;
import com.codeman.seckill.result.CommonEnum;
import com.codeman.seckill.result.ResultBody;
import com.codeman.seckill.service.SeckillService;
import com.codeman.seckill.utils.Constant;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Created by Kuzma on 2020/6/13.
 */
@RequestMapping("/goods")
@RestController
public class SeckillController implements InitializingBean{

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private ExecutorService executorService;

    /**
     * 基于令牌桶算法实现流量限制 适用于突发的高并发
     */
    private RateLimiter rateLimiter;

    @PostConstruct
    public void init(){
        executorService = Executors.newFixedThreadPool(20);
        // 创建一个限流器 每秒生成的300ge个令牌数
        rateLimiter = RateLimiter.create(300);
    }

    @PostMapping("publishSeckillPromo")
    public ResultBody publishSeckillPromo(HttpServletRequest request,@RequestBody SeckillInfo seckillInfo, String userId){
        seckillService.publishSeckillPromo(seckillInfo,userId);
        return ResultBody.success();
    }

    @GetMapping("all_seckill")
    public ResultBody listAllSeckill(){
        return ResultBody.success(seckillService.listAllSeckill());
    }

    /**
     * 获取秒杀token
     * @param request
     * @param seckillId
     * @param goodsId
     * @param userId
     * @return
     */
    @PostMapping("getSeckillToken")
    public ResultBody getSeckillToken(HttpServletRequest request, Long seckillId, Long goodsId, Long userId){

        // 获取session 并校验 TODO 测试环境暂时不开启

        // 获取秒杀访问令牌
        String seckillToken = seckillService.getSeckillToken(seckillId,goodsId,userId);

        return ResultBody.success(seckillToken);
    }

    /**
     * 秒杀接口
     * @param request
     * @param orderInfo
     * @return
     */
    @PostMapping("seckill")
    public ResultBody seckill(HttpServletRequest request, @RequestBody OrderInfo orderInfo, String seckillToken, String userId){

        // 如果获取不到限流令牌
        if (!rateLimiter.tryAcquire()){
            return ResultBody.success(CommonEnum.RATELIMIT.getResultCode(),CommonEnum.RATELIMIT.getResultMsg(),"");
        }

        // 校验秒杀令牌
        String validTokenInRedis = (String) redisTemplate.opsForValue().get("seckill_token_"+orderInfo.getSeckillId()+"_"+userId);
        if (Objects.isNull(seckillToken) || validTokenInRedis == null || !Objects.equals(seckillToken, validTokenInRedis)){
            throw new GlobalException(CommonEnum.PARAMETER_VALIDATION_ERROR);
        }

        // 获取session 并校验 TODO 测试环境暂时不开启


        // 预减库存 库存不足则返回
        long stock = redisTemplate.opsForValue().increment(String.valueOf(orderInfo.getGoodsId()),-1) ;
        if (stock < 0){
            return ResultBody.success(CommonEnum.STOCK_NOT_ENOUGH.getResultCode(),CommonEnum.STOCK_NOT_ENOUGH.getResultMsg(),"");
        }

        // 判断是否已经抢到了 抢到了就不给他继续抢了
        if (redisTemplate.hasKey(orderInfo.getUserId() + Constant.LINKSTRING + orderInfo.getSeckillId())){
            return ResultBody.success(CommonEnum.REPEATE_SECKILL.getResultCode(),CommonEnum.REPEATE_SECKILL.getResultMsg(),"");
        }

        // 同步调用线程池的submit方法 拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(() -> {
            // 进入队列 异步秒杀
            rabbitMqSender.sendSeckillMessage(new SeckillMessage(orderInfo));
            return null;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new GlobalException(CommonEnum.UNKNOWN_ERROR);
        }

        // 返回秒杀ing
        return ResultBody.success(CommonEnum.SECKILLING.getResultCode(),CommonEnum.SECKILLING.getResultMsg(),"");
    }

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

    // TODO 定期脚本清除过期秒杀活动redis遗留的数据 过期一天以上就删了吧 或者来一个redis的LRU策略
    // 凌晨四点载入当天的秒杀数据（秒杀商品的相关信息以及秒杀大闸的限制数）


}
