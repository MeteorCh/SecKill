package com.meteor.SecondKill.Service;

import com.meteor.SecondKill.Pojo.User;

import java.util.List;

public interface UserService {
    User queryUser(String userName,String password);
    int addUser(User user);
    void insertUsers(List<User> users);
}
