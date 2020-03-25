package com.meteor.SecondKill.Dao;

import com.meteor.SecondKill.Pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface UserDao {
    User queryUser(@Param("username") String username, @Param("password")String password);
    int insertUser(@Param("user") User user);
    void insertUsers(@Param("users") List<User> users);
}
