package com.atguigu.sk;

import com.atguigu.sk.utils.JedisPoolUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

@RestController
public class SKController {

    static String secKillScript = "local userid=KEYS[1];\r\n"
            + "local prodid=KEYS[2];\r\n"
            + "local qtkey='sk:'..prodid..\":qt\";\r\n"
            + "local usersKey='sk:'..prodid..\":usr\";\r\n"
            + "local userExists=redis.call(\"sismember\",usersKey,userid);\r\n"
            + "if tonumber(userExists)==1 then \r\n"
            + "   return 2;\r\n"
            + "end\r\n"
            + "local num= redis.call(\"get\" ,qtkey);\r\n"
            + "if tonumber(num)<=0 then \r\n"
            + "   return 0;\r\n"
            + "else \r\n"
            + "   redis.call(\"decr\",qtkey);\r\n"
            + "   redis.call(\"sadd\",usersKey,userid);\r\n"
            + "end\r\n"
            + "return 1";

    @PostMapping(value = "/sk/doSecondKill", produces = "text/html;charset=UTF-8")
    public String doSkByLUA(Integer id){
        //随机生成用户id
        Integer usrid = (int)(10000*Math.random());
        //Jedis jedis = new Jedis("192.168.1.130", 6379);
        //从jedis连接池中获取一个连接,连接对象是被JedisPool代理过的连接对象
        Jedis jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
        //加载LUA脚本
        String sha1 = jedis.scriptLoad(secKillScript);
        //将LUA脚本和LUA脚本需要的参数传给redis执行：keyCount：lua脚本需要的参数数量，params：参数列表
        Object obj = jedis.evalsha(sha1, 2, usrid + "", id + "");
        // Long 强转为Integer会报错  ，Lange和Integer没有父类和子类的关系
        //被代理的jedis对象调用关闭方法，相当将jedis连接还给连接池
        jedis.close();
        int result = (int)((long)obj);
        if(result==1){
            System.out.println("秒杀成功");
            return "ok";
        }else if(result==2){
            System.out.println("重复秒杀");
            return "重复秒杀";
        }else{
            System.out.println("库存不足");
            return "库存不足";
        }

    }


















    public String doSecondKill(Integer id) {
        //模拟用户id
        Integer userId = (int) (Math.random() * 10000);
        //商品的id
        Integer pid = id;
        //秒杀业务
        //拼接商品库存的key和用户列表集合的key
        String qtKey = "sk:" + pid + ":qt";
        String usersKey = "sk:" + pid + ":usr";
        //连接虚拟机的redis库
        Jedis jedis = new Jedis("192.168.89.128", 6379);

       /* System.out.println(jedis.ping());*/

        //判断用户是否已参与秒杀
        if (jedis.sismember(usersKey, userId + "")) {
            System.err.println("重复秒杀：" + userId);
            return "该用户已经秒杀过，请勿重复秒杀";
        }
        //添加watch
        jedis.watch(qtKey);

        //判断库存数量
        //先判断redis库中是否存在这个商品key，不存在则说明秒杀未开始
        String qtStr = jedis.get(qtKey);
        if (StringUtils.isEmpty(qtStr)) {
            System.err.println("秒杀未开始");
            return "秒杀未开始";
        }
        //如果字符串不为空，查看库存是否小于0，小于则提示用户笑啥已结束，库存不足
        int qtNum = Integer.parseInt(qtStr);
        if (qtNum <= 0) {
            System.err.println("库存不足");
            return "库存不足";
        }
        //万事俱备，秒杀成功减库存
        Transaction multi = jedis.multi();//开启redis的组队
        multi.decr(qtKey);
        //将用户加入到秒杀成功的列表中
        multi.sadd(usersKey , userId+"");
        multi.exec();
        System.out.println("秒杀成功："+ userId);
        return "ok";
    }
}
