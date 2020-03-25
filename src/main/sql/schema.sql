-- 数据库初始化脚本

-- 创建数据库
CREATE DATABASE seckill;
-- 使用数据库
use seckill;
CREATE TABLE seckill(
  `seckill_id` BIGINT NOT NUll AUTO_INCREMENT COMMENT '商品库存ID',
  `name` VARCHAR(120) NOT NULL COMMENT '商品名称',
  `number` int NOT NULL COMMENT '库存数量',
  `start_time` TIMESTAMP  NOT NULL COMMENT '秒杀开始时间',
  `end_time`   TIMESTAMP   NOT NULL COMMENT '秒杀结束时间',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (seckill_id),
  key idx_start_time(start_time),
  key idx_end_time(end_time),
  key idx_create_time(create_time)
)ENGINE=INNODB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';

-- 初始化数据
INSERT into seckill(name,number,start_time,end_time)
VALUES
  ('1000元秒杀iphone6',100,'2020-03-25 15:00:00','2021-01-02 00:00:00'),
  ('800元秒杀ipad',200,'2020-03-25 15:00:00','2021-01-02 00:00:00'),
  ('6600元秒杀mac book pro',300,'2020-03-25 15:00:00','2021-01-02 00:00:00'),
  ('7000元秒杀iMac',400,'2020-03-25 15:00:00','2021-01-02 00:00:00');
--用户表
CREATE TABLE user(
    `userName` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码',
    PRIMARY KEY(userName)
)ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='用户表';
--初始化用户信息
INSERT INTO user values('tom',12345)
-- 秒杀成功明细表
CREATE TABLE success_killed(
  `seckill_id` BIGINT NOT NULL COMMENT '秒杀商品ID',
  `user_name` varchar(50) NOT NULL COMMENT '用户名',
  `state` TINYINT NOT NULL DEFAULT -1 COMMENT '状态标识:-1:无效 0:成功 1:已付款 2:已发货',
  `create_time` TIMESTAMP NOT NULL COMMENT '创建时间',
  PRIMARY KEY(seckill_id,user_name),/*联合主键*/
  FOREIGN KEY(seckill_id) REFERENCES seckill(seckill_id),
  FOREIGN KEY(user_name) REFERENCES user(userName),
  KEY idx_create_time(create_time)
)ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';

