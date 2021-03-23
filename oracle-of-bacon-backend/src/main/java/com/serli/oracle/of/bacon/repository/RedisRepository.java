package com.serli.oracle.of.bacon.repository;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public List<String> getLastTenSearches() {
        List<String> top10 = jedis.lrange("last10", 0, 9);
        return top10;
    }

    public void saveSearch(String actorName){
        if(!getLastTenSearches().contains(actorName)) {
            jedis.lpush("last10", actorName);
            jedis.ltrim("last10", 0, 9);
        }
    }
}
