package com.serli.oracle.of.bacon.repository;

import java.util.List;

import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public List<String> getLastTenSearches() {
        // TODO implement last 10 searchs
        return null;
    }
}
