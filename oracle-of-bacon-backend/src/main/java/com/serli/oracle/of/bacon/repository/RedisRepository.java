package com.serli.oracle.of.bacon.repository;

import java.util.List;
import java.util.Collections;

import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public List<String> getLastXSearches(int x) {
        List<String> lastX = this.jedis.lrange("oracle_of_bacon:searches", -x, -1);
        // We invert the list's order to make the first element of the list correspond to the last search
        Collections.reverse(lastX);
        return lastX;
    }
}
