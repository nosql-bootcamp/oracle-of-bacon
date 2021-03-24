package com.serli.oracle.of.bacon.repository;

import java.util.List;

import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public List<String> getLastTenSearches() {
        return this.jedis.lrange("lastTenSearches", 0, -1);
    }

    public void addLastSearch(String search) {
        this.jedis.lrem("lastTenSearches", 1, search);
        this.jedis.lpush("lastTenSearches", search);
        this.jedis.ltrim("lastTenSearches", 0, 9);
    }
}
