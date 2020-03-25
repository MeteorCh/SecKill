package com.meteor.SecondKill.Service.ServiceImp;

import com.meteor.SecondKill.Dao.UserDao;
import com.meteor.SecondKill.Pojo.User;
import com.meteor.SecondKill.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    UserDao userDao;
    @Override
    public User queryUser(String userName, String password) {
        return userDao.queryUser(userName,password);
    }

    @Override
    public int addUser(User user) {
        return userDao.insertUser(user);
    }

    @Override
    public void insertUsers(List<User> users) {
        userDao.insertUsers(users);
    }
}
