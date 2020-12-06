package com.vidor;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class JedisSingleTest {

    public static void main(String[] args) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);

        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "192.168.1.101", 6379, 3000, null);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set("hello_singleRedis", "singleRedis");
            jedis.set("guoqi_mutex", "value", "nx", "ex", 180);
            System.out.println(jedis.get("hello_singleRedis"));


            System.out.println("---------------Jedis Pipeline--------------------------");

            Pipeline pipeline = jedis.pipelined();
            jedis.del("pipelineKey");
            for (int i = 0; i < 10; i++) {
                pipeline.incr("pipelineKey"); //没有这个表示，管道结果输出就没有ID
                pipeline.set("pipeline" + i, "v" + i);
            }
            List<Object> returnAll = pipeline.syncAndReturnAll();
            System.out.println(returnAll);// [1, OK, 2, OK, 3, OK, 4, OK, 5, OK, 6, OK, 7, OK, 8, OK, 9, OK, 10, OK]

            System.out.println("---------------Jedis Pipeline Error--------------------------");

            jedis.del("pipeline_error");
            jedis.del("pipeline_errorK");
            for (int i = 0; i < 2; i++) {
                pipeline.incr("pipeline_error");
                if (i == 0) {
                    pipeline.setbit("pipeline_errorK", -1, true);
                } else {
                    pipeline.setbit("pipeline_errorK", i, true);
                }
            }
            List<Object> returnAll1 = pipeline.syncAndReturnAll();
            System.out.println(returnAll1);//[1, redis.clients.jedis.exceptions.JedisDataException: ERR bit offset is not an integer or out of range, 2, false]

            System.out.println("---------------Jedis Lua --------------------------");
            jedis.set("product_001", "300");
            String script = "local count = redis.call('get', KEYS[1]) " +
                    "local a = tonumber(count) " +
                    "local b = tonumber(ARGV[1]) " +
                    "if a >= b then " +
                    "redis.call('set', KEYS[1], a - b) " +
                    "return 1 " +
                    "end " +
                    "return 0 ";
            Scanner scanner = new Scanner(System.in);
            int bugCount = scanner.nextInt();

            Long o = (Long)jedis.eval(script, Arrays.asList("product_001"), Arrays.asList(String.valueOf(bugCount)));
            if (o.intValue() == 1){
                System.out.println("可以售卖,请继续下单");
            } else {
                System.out.println("超售，请重新下单");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != jedis) {
                jedis.close();//注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池。
            }
        }
    }
}
