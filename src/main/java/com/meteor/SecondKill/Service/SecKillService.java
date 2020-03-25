package com.meteor.SecondKill.Service;

import com.meteor.SecondKill.DTO.Exposer;
import com.meteor.SecondKill.DTO.SecKillExecution;
import com.meteor.SecondKill.Pojo.SecKill;

import java.util.List;

public interface SecKillService {
    List<SecKill> queryAll();
    SecKill querySecKillByID(Long ID);
    Exposer exportSecKillUrl(Long secKillID);
    SecKillExecution executeSecKill(Long secKillID, String userKey, String md5);
}
