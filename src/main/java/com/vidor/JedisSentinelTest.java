package com.vidor;

import redis.clients.jedis.*;

import java.util.*;

public class JedisSentinelTest {

    public static void main(String[] args) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);

        String masterName = "mymaster";
        Set<String> sentinels = new HashSet<String>();
        sentinels.add("192.168.1.101:26380");
        sentinels.add(new HostAndPort("192.168.1.101", 26381).toString());
        sentinels.add("192.168.1.101:26382");

        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(masterName, sentinels, jedisPoolConfig, 3000, "redis");
        Jedis jedis = null;
        try {
            jedis = jedisSentinelPool.getResource();

            jedis.set("sentinel-key", "sentinel-value");
            System.out.println(jedis.get("sentinel-key"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != jedis) {
                jedis.close();//注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池。
            }
        }
    }
}
