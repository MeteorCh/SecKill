package com.meteor.SecondKill.Interceptor;

import com.meteor.SecondKill.Utility.ConstValue;
import com.meteor.SecondKill.Utility.CookieUtility;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        //拦截器，判断是否登录
        String userName=(String) httpServletRequest.getSession().getAttribute(ConstValue.USER_KEY);
        if (userName==null){
            if (httpServletRequest.getRequestURI().endsWith("login"))
                return true;
            //首先用cookies尝试登录
            Cookie[] cookies = httpServletRequest.getCookies();
            String userKey=null,ssID=null;
            if (cookies!=null){
                for (Cookie cookie:cookies){
                    if (cookie.getName().equals(ConstValue.USER_KEY)){
                        userKey=cookie.getValue();
                    }else if (cookie.getName().equals(ConstValue.SS_ID)){
                        ssID=cookie.getValue();
                    }
                }
            }
            if (userKey!=null&&ssID!=null){
                String decrypt= CookieUtility.getMd5(userKey);
                if (ssID.equals(decrypt)) {
                    httpServletRequest.getSession().setAttribute(ConstValue.USER_KEY,userKey);
                    return true;
                }
            }
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/login");
        }else if (userName!=null&&httpServletRequest.getRequestURI().endsWith("login")){
            //重定向到列表界面
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/meteor/list");
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
