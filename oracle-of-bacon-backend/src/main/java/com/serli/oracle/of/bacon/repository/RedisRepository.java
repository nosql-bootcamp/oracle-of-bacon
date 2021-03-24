package com.serli.oracle.of.bacon.repository;

import java.util.List;

import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public List<String> getLastTenSearches() {
        return jedis.lrange(searchesKey, 0, -1);
    }

    public void addSearch(String search) {
        jedis.lpush(searchesKey, search);
        jedis.ltrim(searchesKey, 0, 9);
    }
}
