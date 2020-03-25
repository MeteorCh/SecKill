<%--
  Created by IntelliJ IDEA.
  User: hezezhong
  Date: 2020-3-23
  Time: 21:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page isELIgnored="false" %>
<html>
<head>
    <title>登录</title>
    <%@include file="common/head.jsp"%>
</head>
<body style="background: url(http://global.bing.com/az/hprichbg/rb/RavenWolf_EN-US4433795745_1920x1080.jpg) no-repeat center center fixed; background-size: 100%;">
<div>
    <%--登录弹出层 输入电话--%>
        <div class="modal-dialog" style="margin-top: 10%;">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title text-center" id="myModalLabel">登录</h4>
                </div>
                <div class="modal-body" id = "model-body">
                    <div class="form-group">

                        <input type="text" class="form-control"placeholder="用户名" autocomplete="off" id="userNameKey">
                    </div>
                    <div class="form-group">

                        <input type="password" class="form-control" placeholder="密码" autocomplete="off" id="passwordKey">
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <button type="button" class="btn btn-primary form-control" id="killPhoneBtn">登录</button>
                    </div>
                </div>
            </div><!-- /.modal-content -->
        </div>    <!-- /.modal -->
</div>
</body>
<%--jQery文件,务必在bootstrap.min.js之前引入--%>
<script src="http://apps.bdimg.com/libs/jquery/2.0.0/jquery.min.js"></script>
<script src="http://apps.bdimg.com/libs/bootstrap/3.3.0/js/bootstrap.min.js"></script>
<%--使用CDN 获取公共js http://www.bootcdn.cn/--%>
<%--jQuery Cookie操作插件--%>
<script src="http://cdn.bootcss.com/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>

<script type="text/javascript">
    $(function () {
        $('#killPhoneModal').modal('show');
    });
    $('#killPhoneBtn').click(function () {
        var userName=$('#userNameKey').val();
        var password=$('#passwordKey').val();
        console.log("userName: " + userName+"password:"+password);
        $.ajax({
            url:'${pageContext.request.contextPath}/meteor/login',//地址
            type: "post",
            data:{"userName":userName,"password":password},
            dataType: 'json',
            async: true,
            timeout:2000,//超时
            xhrFields: {
                withCredentials: true // 这里设置了withCredentials
            },
            //请求成功
            success:function(data){
                if (data['success']==1){//登录成功
                    console.log("登录成功")
                    window.location.href='${pageContext.request.contextPath}/meteor/list'
                }else {
                    alert("用户名或密码错误")
                }
            }
        });
    });
</script>
</html>
