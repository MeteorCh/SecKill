package com.meteor.SecondKill.Pojo;

import java.util.Date;

public class SuccessKilled {
    private long secKillId;
    private String userName;
    private short state;
    private Date createTime;

    //多对一,因为一件商品在库存中有很多数量，对应的购买明细也有很多。
    private SecKill secKill;

    public long getSecKillId() {
        return secKillId;
    }

    public void setSecKillId(long secKillId) {
        this.secKillId = secKillId;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public SecKill getSecKill() {
        return secKill;
    }

    public void setSecKill(SecKill seckill) {
        this.secKill = seckill;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "SuccessKilled{" +
                "seckillId=" + secKillId +
                ", userName=" + userName +
                ", state=" + state +
                ", createTime=" + createTime +
                '}';
    }
}
