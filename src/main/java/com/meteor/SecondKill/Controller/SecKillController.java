package com.meteor.SecondKill.Controller;

import com.meteor.SecondKill.DTO.Exposer;
import com.meteor.SecondKill.DTO.SecKillResult;
import com.meteor.SecondKill.DTO.SecKillExecution;
import com.meteor.SecondKill.Enum.SecKillStatEnum;
import com.meteor.SecondKill.Exception.RepeatKillException;
import com.meteor.SecondKill.Exception.SeckillCloseException;
import com.meteor.SecondKill.Pojo.SecKill;
import com.meteor.SecondKill.Pojo.User;
import com.meteor.SecondKill.Service.SecKillService;
import com.meteor.SecondKill.Service.UserService;
import com.meteor.SecondKill.Utility.ConstValue;
import com.meteor.SecondKill.Utility.CookieUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/meteor")
public class SecKillController {
    Logger logger= LoggerFactory.getLogger(SecKillController.class);
    @Autowired
    SecKillService secKillService;
    @Autowired
    UserService userService;
    @RequestMapping("list")
    String listSecKill(Model model){
        List<SecKill> secKills=secKillService.queryAll();
        model.addAttribute("secKills",secKills);
        logger.debug("所有秒杀选项"+secKills.toString());
        return "list";
    }

    @RequestMapping(value = "/{secKillId}/detail",method = RequestMethod.GET)
    String detail(@PathVariable Long secKillId,Model model){
        SecKill kill=secKillService.querySecKillByID(secKillId);
        if (kill.getNumber()<=0)
            return "error";//此处只是显示错误界面，应当显示秒杀已结束页面
        model.addAttribute("secKill",kill);
        return "detail";
    }

   @RequestMapping(value = "login",method = RequestMethod.POST)
   @ResponseBody
   SecKillResult login(HttpServletRequest request, HttpServletResponse response,String userName,String password){
       SecKillResult<String>  result;
       User user=userService.queryUser(userName,password);
       if (user!=null){//首次登录且登录成功
           //进行Cookie的保存
           logger.debug("查询到的用户"+user.toString());
           CookieUtility.createCookie(userName,response,ConstValue.SAVE_TIME);
           request.getSession().setAttribute(ConstValue.USER_KEY,userName);
           result=new SecKillResult<>(true,"登录成功");
       }else
           result=new SecKillResult<>(false,"用户名或密码错误！");
       return result;
   }
   @RequestMapping("time/now")
   @ResponseBody
   SecKillResult getSysTime(){
       Date now=new Date();
       return new SecKillResult(true,now.getTime());
   }

   @RequestMapping(value = "/{secKillID}/exposer",
           method = RequestMethod.GET,
           produces = {"application/json;charset=UTF-8"})
   @ResponseBody
   SecKillResult exposer(@PathVariable Long secKillID){
       SecKillResult<Exposer> result;
       try{
           Exposer exposer=secKillService.exportSecKillUrl(secKillID);
           result=new SecKillResult<>(true,exposer);
       }catch (Exception e)
       {
           e.printStackTrace();
           result=new SecKillResult<>(false,e.getMessage());
       }
       return result;
   }

    /**
     * 执行秒杀
     * @param secKillID
     * @param md5
     * @return
     */
   @RequestMapping(value = "/{secKillID}/{md5}/execution",
                    method = RequestMethod.POST,
           produces = {"application/json;charset=UTF-8"})
   @ResponseBody
   SecKillResult execution(@PathVariable Long secKillID, @PathVariable String md5,
                            @CookieValue(value = ConstValue.USER_KEY,required = false) String userKey){
       if (userKey==null)
       {
           return new SecKillResult(false,"未注册");
       }
       try {
           SecKillExecution execution = secKillService.executeSecKill(secKillID, userKey, md5);
           return new SecKillResult<SecKillExecution>(true, execution);
       }catch (RepeatKillException e1)
       {
           SecKillExecution execution=new SecKillExecution(secKillID, SecKillStatEnum.REPEAT_KILL);
           return new SecKillResult(true,execution);
       }catch (SeckillCloseException e2)
       {
           SecKillExecution execution=new SecKillExecution(secKillID, SecKillStatEnum.END);
           return new SecKillResult<SecKillExecution>(true,execution);
       }
       catch (Exception e)
       {
           SecKillExecution execution=new SecKillExecution(secKillID, SecKillStatEnum.INNER_ERROR);
           return new SecKillResult<SecKillExecution>(true,execution);
       }
   }
}
