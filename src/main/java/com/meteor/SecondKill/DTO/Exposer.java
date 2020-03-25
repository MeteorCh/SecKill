package com.meteor.SecondKill.DTO;

public class Exposer {
    //是否开启秒杀
    private boolean exposed;

    //加密措施
    private String md5;

    private long secKillId;

    //系统当前时间(毫秒)
    private long now;

    //秒杀的开启时间
    private long start;

    //秒杀的结束时间
    private long end;

    public Exposer(boolean exposed, String md5, long secKillId) {
        this.exposed = exposed;
        this.md5 = md5;
        this.secKillId = secKillId;
    }

    public Exposer(boolean exposed, long secKillId, long now, long start, long end) {
        this.exposed = exposed;
        this.secKillId = secKillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public Exposer(boolean exposed, long secKillId) {
        this.exposed = exposed;
        this.secKillId = secKillId;
    }

    public boolean isExposed() {
        return exposed;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSecKillId() {
        return secKillId;
    }

    public void setSecKillId(long secKillId) {
        this.secKillId = secKillId;
    }

    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "Exposer{" +
                "exposed=" + exposed +
                ", md5='" + md5 + '\'' +
                ", seckillId=" + secKillId +
                ", now=" + now +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
