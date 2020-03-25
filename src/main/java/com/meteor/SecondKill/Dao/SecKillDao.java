package com.meteor.SecondKill.Dao;

import com.meteor.SecondKill.Pojo.SecKill;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SecKillDao {
    List<SecKill> listSecKill();
    SecKill getSecKillByID(Long secID);
    int reduceNumber(@Param("secKillID") Long secID,@Param("date")Date date);
}
