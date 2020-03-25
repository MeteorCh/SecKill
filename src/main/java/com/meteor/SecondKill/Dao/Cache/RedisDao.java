package com.meteor.SecondKill.Dao.Cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.meteor.SecondKill.Pojo.SecKill;
import com.meteor.SecondKill.Utility.JedisUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.function.Function;

public class RedisDao {
    private final JedisPool jedisPool;
    private final int SEC_KILL_TIMEOUT=60*60;//请求商品存在时的存储时间
    private final int NULL_KILL_TIMEOUT=3*60;//请求商品不存在时，存储时间
    Logger logger= LoggerFactory.getLogger(RedisDao.class);
    public RedisDao(String ip, int port){
        this.jedisPool = new JedisPool(ip,port);
    }
    private RuntimeSchema<SecKill> schema = RuntimeSchema.createFrom(SecKill.class);

    public SecKill getSecKill(long secKillId) {
        return getSecKill(secKillId, null);
    }

    /**
     * 从redis获取信息
     *
     * @param seckillId id
     * @return 如果不存在，则返回null
     */
    public SecKill getSecKill(long seckillId, Jedis jedis) {
        boolean hasJedis = jedis != null;
        //redis操作逻辑
        try {
            if (!hasJedis) {
                jedis = jedisPool.getResource();
            }
            try {
                String key = getSeckillRedisKey(seckillId);
                byte[] bytes = jedis.get(key.getBytes());
                //缓存重获取到
                if (bytes != null) {
                    SecKill SecKill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, SecKill, schema);
                    return SecKill;
                }
            } finally {
                if (!hasJedis) {
                    jedis.close();
                }
            }
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 从缓存获取，如果没有，则从数据库获取
     * 会用到分布式锁
     *
     * @param seckillId     id
     * @param getDataFromDb 从数据库获取的方法
     * @return 返回商品信息
     */
    public SecKill getOrPutSeckill(long seckillId, Function<Long, SecKill> getDataFromDb) {
        String lockKey = "secKill:locks:getSeckill:" + seckillId;
        String lockRequestId = UUID.randomUUID().toString();
        Jedis jedis = jedisPool.getResource();
        try {
            // 循环直到获取到数据
            while (true) {
                SecKill SecKill = getSecKill(seckillId, jedis);
                if (SecKill != null) {
                    logger.debug("从Redis中获取到了数据");
                    return SecKill;
                }
                // 尝试获取锁。
                // 锁过期时间是防止程序突然崩溃来不及解锁，而造成其他线程不能获取锁的问题。过期时间是业务容忍最长时间。
                boolean getLock = JedisUtility.tryGetDistributedLock(jedis, lockKey, lockRequestId, 1000);
                if (getLock) {
                    // 获取到锁，从数据库拿数据, 然后存redis
                    logger.debug("Redis中没有数据，去数据库中获取");
                    SecKill = getDataFromDb.apply(seckillId);
                    //此处做一个判断，防止缓存穿透
                    if (SecKill==null)
                        putSecKill(seckillId);
                    else
                        putSecKill(SecKill);
                    return SecKill;
                }
                // 获取不到锁，睡一下，等会再出发。sleep的时间需要斟酌，主要看业务处理速度
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            // 无论如何，最后要去解锁
            JedisUtility.releaseDistributedLock(jedis, lockKey, lockRequestId);
            jedis.close();
        }
        return null;
    }

    /**
     * 根据id获取redis的key
     *
     * @param seckillId 商品id
     * @return redis的key
     */
    private String getSeckillRedisKey(long seckillId) {
        return "secKill:" + seckillId;
    }

    public String putSecKill(SecKill SecKill) {
        return putSecKill(SecKill,null, null,SEC_KILL_TIMEOUT);
    }

    public String putSecKill(long secKillID){
        return putSecKill(null,secKillID,null,NULL_KILL_TIMEOUT);
    }


    public String putSecKill(SecKill secKill,Long secKillID, Jedis jedis,int timeout) {
        boolean hasJedis = jedis != null;
        try {
            if (!hasJedis) {
                jedis = jedisPool.getResource();
            }
            try {
                String key=null;
                byte[] bytes=null;
                if (secKill==null&&secKillID!=null){
                    key=getSeckillRedisKey(secKillID);
                    bytes=new byte[1];
                }else if (secKill!=null){
                    key= getSeckillRedisKey(secKill.getSeckillId());
                    bytes= ProtostuffIOUtil.toByteArray(secKill, schema,
                            LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                }
                if (key!=null&&bytes!=null)
                    return jedis.setex(key.getBytes(), timeout, bytes);
            } finally {
                if (!hasJedis) {
                    jedis.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
