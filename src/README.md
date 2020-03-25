最近把SSM框架的基础知识撸了一遍，跟着[github上的这个项目](https://github.com/codingXiaxw/seckill) ，实现了一下秒杀系统，并对这个项目中存在的问题进行了 一些小小的改进，记录一下。整个项目用到了Spring+SpringMVC+Mybatis+Redis框架，如果是刚学SSM框架希望找个小项目练手的，可以跟着这个项目来练练手，感觉涉及的知识还是很全面的。项目源码已上传到GitHub：[https://github.com/MeteorCh/SecKill](https://github.com/MeteorCh/SecKill)，需要的同学自取。
# 一、项目功能及涉及知识点
项目的整体业务流程如下（感觉自己画的可能不是很标准，表达意思即可）
![抢购流程](https://img-blog.csdnimg.cn/20200325111907951.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMxNzA5MjQ5,size_16,color_FFFFFF,t_70)
## 1.数据库
项目涉及三个数据表
### (1)用户表user
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200325114514245.png)
### (2)商品表seckill
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200325114543975.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMxNzA5MjQ5,size_16,color_FFFFFF,t_70)
### (3)秒杀成功记录表success_seckilled
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200325114706724.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMxNzA5MjQ5,size_16,color_FFFFFF,t_70)

## 2.登录模块
### 2.1 功能描述
在这里使用了Spring的拦截器对访问的网址进行拦截，如果没登录，就跳转到登录页面登录。如果输入的用户名和密码都正确，则将用户名和用户名用MD5加密的token写入Cookie，下次登录时，首先判断Cookie的登录信息正不正确，正确的话自动登录。此外，当用户访问登录页面时，如果存在cookie，则直接跳转到列表页，这些逻辑都写在**LoginInterceptor**类中，具体内容下载源码查看。登录页面如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200325112707366.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMxNzA5MjQ5,size_16,color_FFFFFF,t_70)
### 2.2 涉及知识点
登录模块涉及的知识点主要有：==Cookie、Session、SpringMVC拦截器、MD5验证==
## 3.秒杀商品列表模块
### 3.1功能描述
展示所有的秒杀商品，需要注意的是，从数据库中查找商品时，应把库存数量为0的过滤掉。秒杀商品列表页面如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200325113022222.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMxNzA5MjQ5,size_16,color_FFFFFF,t_70)
### 3.2 涉及知识
这个主要就涉及一些简单的JSP和MyBatis的操作知识。没有什么复杂的。
## 4.商品详情页
### 4.1功能描述
点击秒杀商品列表页中的详情页，显示秒杀商品的详情并提供秒杀通道。这里，是整个项目的核心，也是高并发的地方之一。所以，这里使用了Redis作为缓存。查询商品详情时，先去Redis中查找，如果没有，则去数据库中查并将结果写入到Redis，以便下次查找的时候直接从Redis中查找。界面如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200325113556446.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMxNzA5MjQ5,size_16,color_FFFFFF,t_70)
### 4.2涉及知识
这里涉及的知识有：==Redis缓存、缓存穿透的处理==
## 5.秒杀
### 5.1功能描述
用户点击秒杀的时候，会首先写入一条记录到success_seckilled表中，如果写入失败，则说明是重复秒杀。如果写入成功，再去seckill表中，将对应商品的数量-1，此处为了防止超卖，需要限制-1时，剩余商品的数量要大于0。
此处，为了防止数据被篡改，同样也需要同时携带商品ID用MD5加密的密文，并在后端判断数据是否被篡改。最后，需要将秒杀的结果以Json的形式返回给浏览器，并在客户端进行显示。秒杀结果界面如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200325114254950.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMxNzA5MjQ5,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200325114329100.png)
### 5.2涉及知识
这里面也是涉及到==Redis==的 一些知识。具体在第二节讨论。
# 二、问题及解决
原项目中存在这以下问题，我解决了一下，具体如下：
## 1.自动登录
可以利用Cookie和Session来实现自动登录。我这里保存的Cookie有两个：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200325121628865.png)
其中UserKey为用户名，SsID为UserKey通过MD5加密的结果。这样，在后端，就可以通过判断UserKey通过MD5加密的结果是否和SsID相等，来判断是否能自动登录。
## 2.缓存穿透的问题
缓存穿透是指，请求一条不可能存在的数据，请求时先去缓存中找，不存在，再去数据库中找，数据库中也不存在。这样的话，缓存就没有意义了。我这里的解决方案是，一个商品详情的请求，如果从数据库中找不存在，首先去数据库中找，如果结果为空，仍然把这个数据存储到Redis中，下次请求的时候，就直接从Redis缓存中找了。但是，用这种方法，**这种数据的缓存有效期要尽量短一些，防止增加了数据一直查不到的情况。**(我设置的是，有效商品的缓存有效期为1小时，无效请求的有效期为3分钟)。
## 3.库存为0仍然可以显示秒杀页
我感觉这个是原项目特别需要完善的一个地方。因为Redis缓存存储的商品信息，自从写入就一直没有变化。当商品抢购完了以后，用户点击详情页，仍然可以进入到秒杀页面，体验不是很好。所以，我这里的处理方法是，当商品库存为0后，去主动更新一下Redis缓存中对象的库存值。在请求时，判断从Redis中取到的商品库存是否为0，如果为0，则跳转到另一个页面。
## 4.错误页面的处理
有时候，我们再请求的过程中会出现异常，然后会在浏览器显示异常的信息，看起来不是很好。我们可以通过简单的配置web.xml文件，让出现错误时，跳转到我们自定义的错误页面，配置信息如下：

```xml
<error-page>
    <error-code>404</error-code>
    <location>/error</location>
  </error-page>
  <error-page>
    <error-code>500</error-code>
    <location>/error</location>
  </error-page>
  <error-page>
    <error-code>400</error-code>
    <location>/error</location>
  </error-page>
```
# 三、并发测试
既然是秒杀系统，那我们需要做一个高并发的测试，看系统的性能到底怎么样。我这里用多线程去模拟请求，测试高并发。代码如下（我这里是写在测试类中）：

```java
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
    @Test
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
```
使用时，首先通过**insertUser**插入2000个用户，再调用**simulateConcurrency**函数，开多线程去模拟高并发，计算请求的时间。我这里的测试是，500的并发量，耗时是10秒左右。