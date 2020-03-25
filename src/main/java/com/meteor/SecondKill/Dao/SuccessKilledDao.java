package com.meteor.SecondKill.Dao;

import com.meteor.SecondKill.Pojo.SuccessKilled;
import org.apache.ibatis.annotations.Param;

public interface SuccessKilledDao {
    int insertSuccessKilled(@Param("secKillId") Long secKillID,@Param("userKey") String userKey);
    SuccessKilled queryByIdWithSecKill(@Param("secKillId") Long secKillID,@Param("userKey")String userKey);
}
