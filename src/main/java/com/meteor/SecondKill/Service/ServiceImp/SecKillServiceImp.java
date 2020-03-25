package com.meteor.SecondKill.Service.ServiceImp;

import com.meteor.SecondKill.DTO.Exposer;
import com.meteor.SecondKill.DTO.SecKillExecution;
import com.meteor.SecondKill.Dao.Cache.RedisDao;
import com.meteor.SecondKill.Dao.SecKillDao;
import com.meteor.SecondKill.Dao.SuccessKilledDao;
import com.meteor.SecondKill.Enum.SecKillStatEnum;
import com.meteor.SecondKill.Exception.RepeatKillException;
import com.meteor.SecondKill.Exception.SeckillCloseException;
import com.meteor.SecondKill.Exception.SeckillException;
import com.meteor.SecondKill.Pojo.SecKill;
import com.meteor.SecondKill.Pojo.SuccessKilled;
import com.meteor.SecondKill.Service.SecKillService;
import com.meteor.SecondKill.Utility.CookieUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SecKillServiceImp implements SecKillService {
    @Autowired
    SecKillDao secKillDao;
    @Autowired
    SuccessKilledDao successKilledDao;
    @Autowired
    RedisDao redisDao;
    Logger logger= LoggerFactory.getLogger(SecKillService.class);
    @Override
    public List<SecKill> queryAll() {
        return secKillDao.listSecKill();
    }

    @Override
    public SecKill querySecKillByID(Long ID) {
        //首先通过redis获取，如果没有再去数据库中获取
        SecKill  seckill=redisDao.getOrPutSeckill(ID, id -> secKillDao.getSecKillByID(id));
        logger.debug(seckill.toString());
        return seckill;
    }

    @Override
    public Exposer exportSecKillUrl(Long secKillID) {
        SecKill seckill = querySecKillByID(secKillID);
        //若是秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        if (startTime.getTime() > nowTime.getTime() || endTime.getTime() < nowTime.getTime()) {
            return new Exposer(false, secKillID, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        //秒杀开启，返回秒杀商品的id、用给接口加密的md5
        String md5 = CookieUtility.getMd5(secKillID);
        return new Exposer(true, md5, secKillID);
    }

    @Override
    @Transactional
    public SecKillExecution executeSecKill(Long secKillID, String userKey, String md5) {
        if (md5 == null || !md5.equals(CookieUtility.getMd5(secKillID))) {
            //秒杀数据被重写了
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime = new Date();
        try {
            //否则更新了库存，秒杀成功,增加明细
            int insertCount = successKilledDao.insertSuccessKilled(secKillID, userKey);
            //看是否该明细被重复插入，即用户是否重复秒杀
            if (insertCount <= 0) {
                throw new RepeatKillException("seckill repeated");
            } else {
                //减库存,热点商品竞争
                int updateCount = secKillDao.reduceNumber(secKillID, nowTime);
                if (updateCount <= 0) {
                    //没有更新库存记录，说明秒杀结束 rollback
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息 commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSecKill(secKillID, userKey);
                    return new SecKillExecution(secKillID, SecKillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            //秒杀结束后，需要设置redis中的数据，用户再次点击详情页时直接显示秒杀结束
            SecKill kill=querySecKillByID(secKillID);
            kill.setNumber(0);
            redisDao.putSecKill(kill);
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所以编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error :" + e.getMessage());
        }
    }
}
