package com.serli.oracle.of.bacon.repository;

import java.util.List;

import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public List<String> getLastTenSearches() {
        return this.jedis.lrange("LastTenSearches", 0, 2);
    }

    public void addLastSearch(String search){
        
        if (!this.jedis.lrange("LastTenSearches", 0, 2).contains(search)){
            this.jedis.lpush("LastTenSearches", search);
        }
        
    }
}
