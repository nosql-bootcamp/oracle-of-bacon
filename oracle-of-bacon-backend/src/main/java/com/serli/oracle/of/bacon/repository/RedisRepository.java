package com.serli.oracle.of.bacon.repository;

import java.util.List;
import java.util.ArrayList;

import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;
    private List<String> cacheSearches;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
        this.cacheSearches = new ArrayList<String>();
    }

    public void saveSearch(String search) {
        if (cacheSearches.size() == 0 || !cacheSearches.get(cacheSearches.size() - 1).equals(search))
            cacheSearches.add(search);
    }

    public List<String> getLastTenSearches() {
        int from = Math.max(0, this.cacheSearches.size() - 10);
        return this.cacheSearches.subList(from, this.cacheSearches.size());
    }
}
