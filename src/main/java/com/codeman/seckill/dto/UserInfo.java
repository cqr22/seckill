package com.codeman.seckill.dto;

/**
 * 返回给前端的用户信息
 * Created by Kuzma on 2020/6/13.
 */
public class UserInfo {
    private Long userId;

    private String userName;

    private Double wallet;

    private String faceImg;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Double getWallet() {
        return wallet;
    }

    public void setWallet(Double wallet) {
        this.wallet = wallet;
    }

    public String getFaceImg() {
        return faceImg;
    }

    public void setFaceImg(String faceImg) {
        this.faceImg = faceImg;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", wallet=" + wallet +
                ", faceImg='" + faceImg + '\'' +
                '}';
    }
}
