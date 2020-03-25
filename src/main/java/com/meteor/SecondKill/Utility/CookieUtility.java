package com.meteor.SecondKill.Utility;

import org.springframework.util.DigestUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CookieUtility {
    //加入一个混淆字符串(秒杀接口)的salt，为了我避免用户猜出我们的md5值，值任意给，越复杂越好
    private static final String salt = "shsdssljdd'l.";

    /**
     * 获取MD5
     * @param info
     * @return
     */
    public static String getMd5(Object info){
        return DigestUtils.md5DigestAsHex((info.toString()+salt).getBytes());
    }

    /**
     * 创建cookie
     */
    public static void createCookie(String username,HttpServletResponse resp, int sec) {
        //写入userKey和ssid信息，方便下次登陆查询
        Cookie userCookie = new Cookie(ConstValue.USER_KEY, username);
        Cookie ssidCookie = new Cookie(ConstValue.SS_ID, getMd5(username));
        userCookie.setMaxAge(sec);
        ssidCookie.setMaxAge(sec);
        resp.addCookie(userCookie);
        resp.addCookie(ssidCookie);
    }
}
