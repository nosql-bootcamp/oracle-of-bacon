package com.serli.oracle.of.bacon.repository;

import java.util.List;
import java.util.Collections;

import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;
    
    private final String search_key = "oracle_of_bacon:searches";

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }
    
    public void addSearch(String search) {
        this.jedis.rpush(this.search_key, search);
    }

    public List<String> getLastXSearches(int x) {
        List<String> lastX = this.jedis.lrange(this.search_key, -x, -1);
        // We invert the list's order to make the first element of the list correspond to the last search
        Collections.reverse(lastX);
        return lastX;
    }
}
