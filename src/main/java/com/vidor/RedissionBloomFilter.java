package com.vidor;

import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissionBloomFilter {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.1.101:6379");
        RedissonClient redissonClient = Redisson.create(config);

        RBloomFilter<String> cityListBloom = redissonClient.getBloomFilter("cityList");
        // 初始化布隆过滤器大小 : 预计元素大小， 误差率
        // Bloom filter size can't be greater than 4294967294
        cityListBloom.tryInit(100000000L, 0.03);

        cityListBloom.add("ChangSha");
        cityListBloom.add("ShangHai");
        cityListBloom.add("ShenZhen");

        System.out.println(cityListBloom.contains("Beijing"));
        System.out.println(cityListBloom.contains("GuangZhou"));
        System.out.println(cityListBloom.contains("ShenZhen"));

        redissonClient.shutdown();
    }
}
