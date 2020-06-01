<%--
  Created by IntelliJ IDEA.
  User: Armer
  Date: 2020/5/27
  Time: 22:03
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>Title</title>
        <script src="jquery/jquery-2.1.1.min.js" type="text/javascript"></script>
    </head>
    <body>
        <form action="sk/doSecondKill" method="post">
            <input type="hidden" name="id" value="10001">
            <a  href="#">点击参与1元秒杀Iphone11</a>
        </form>

        <script type="text/javascript">
            $("a").click(function () {
                $.ajax({
                    type:"post",
                    data:$("form").serialize(),
                    url:$("form").prop("action"),
                    success:function (result) {
                        if(result=="ok"){
                            alert("秒杀成功");
                        }else{
                            alert(result);
                            $("a").prop("disabled" , true);
                        }
                    }
                });
                return false;
            });
        </script>
    </body>
</html>
