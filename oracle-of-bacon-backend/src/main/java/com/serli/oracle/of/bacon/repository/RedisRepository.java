package com.serli.oracle.of.bacon.repository;

import java.util.List;

import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public void addSearch(String actorName) {
        List<String> lastTenSearches = this.getLastTenSearches();
        if(!lastTenSearches.contains(actorName)){
            this.jedis.lpush("lastTenSearches", actorName);
            this.jedis.ltrim("lastTenSearches", 0, 9);
        }
    }

    public List<String> getLastTenSearches() {
        return this.jedis.lrange("lastTenSearches", 0, -1);
    }
}
