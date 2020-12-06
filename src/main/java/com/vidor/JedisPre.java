package com.vidor;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

public class JedisPre {
    public static void main(String[] args) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);

        int minIdle = jedisPoolConfig.getMinIdle();
        List<Jedis> minIdleList = new ArrayList<Jedis>(minIdle);

        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "192.168.1.101", 6379, 3000, null);

        for (int i = 0; i < minIdle; i++) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                minIdleList.add(jedis);
                jedis.ping();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                //注意，这里不能马上close将连接还回连接池，否则最后连接池里只会建立1个连接。
                // jedis.close();
            }
        }

        System.out.println("--------------size: " + minIdleList.size());

        //统一将预热的连接还回连接池
        for (int i = 0; i < minIdle; i++) {
            try {
                minIdleList.get(i).close(); //规范连接池
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
            }
        }

    }
}
