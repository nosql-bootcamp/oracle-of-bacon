package com.serli.oracle.of.bacon.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public void insertSearch(String search){
        // insert in a sorted set (timestamp: search)
        Timestamp ts = new Timestamp(new Date().getTime());
        jedis.zadd("search", ts.getTime(), search);
    }

    public List<String> getLastNSearches( int n ) {
        return (List<String>) jedis.zrange("search",0, n);
    }
}
