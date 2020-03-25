import com.meteor.SecondKill.Pojo.User;
import com.meteor.SecondKill.Service.SecKillService;
import com.meteor.SecondKill.Service.UserService;
import com.meteor.SecondKill.Utility.ConstValue;
import com.meteor.SecondKill.Utility.CookieUtility;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring-dao.xml",
        "classpath:spring-service.xml"})
public class Tester {
    @Autowired
    SecKillService secKillService;
    @Autowired
    UserService userService;

    /**
     * 插入用户
     * @throws InterruptedException
     */
    //@Test
    public void insertUser() throws InterruptedException {
        List<User> users=new ArrayList<>(2000);
        for (int i=500;i<2000;++i){
            User user=new User("user"+i,"12345");
            users.add(user);
        }
        userService.insertUsers(users);
    }

    /**
     *测试并发的入口
     */
    //@Test
    public void simulateConcurrency(){
        try {
            calculateTime(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }


    /**
     * 模拟请求
     * @param userNum 并发数
     * @throws InterruptedException
     */
    public void calculateTime(int userNum) throws InterruptedException {
        long startTime=System.currentTimeMillis();
        ExecutorService service= Executors.newFixedThreadPool(userNum);
        for (int i=0;i<userNum;++i){
            final int num=i;
            service.execute(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        request(num);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.HOURS);
        long endTime=System.currentTimeMillis();
        System.out.println("耗费时间："+(endTime-startTime)/1000);
    }

    /**
     * 请求
     * @param i
     * @throws IOException
     */
    public void  request(int i) throws IOException {
        /**
         * 耗时统计：500并发10秒
         */
        //高并发请求测试
        int secID=1001;
        String urlPath = "http://localhost:8080/SecondKill/meteor/"+secID+"/"
                +CookieUtility.getMd5(secID)+"/execution";
        String userKey = "user"+i;
        String result = "";
        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
        RequestConfig requestConfig =  RequestConfig.custom().setSocketTimeout(1000000).setConnectTimeout(1000000).build();
        try {
            HttpPost post = new HttpPost(urlPath);//这里发送post请求
            post.setConfig(requestConfig);
            List<BasicClientCookie> cookies=createCookie(userKey);
            for (BasicClientCookie cookie:cookies)
                cookieStore.addCookie(cookie);
            // 通过请求对象获取响应对象
            HttpResponse response = httpClient.execute(post);
            // 判断网络连接状态码是否正常(0--200都数正常)
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    List<BasicClientCookie> createCookie(String userName){
        List<BasicClientCookie> cookies=new ArrayList<>();
        //用户名
        BasicClientCookie cookie = new BasicClientCookie(ConstValue.USER_KEY, userName);
        cookie.setDomain("localhost");
        cookie.setPath("/SecondKill/");
        cookies.add(cookie);
        //ssid
        BasicClientCookie ssID = new BasicClientCookie(ConstValue.SS_ID,CookieUtility.getMd5(userName));
        ssID.setDomain("localhost");
        ssID.setPath("/SecondKill/");
        cookies.add(cookie);
        return cookies;
    }
}
