package com.serli.oracle.of.bacon.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.serli.oracle.of.bacon.utils.Utils;
import redis.clients.jedis.Jedis;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public void insertSearch(String search){
        // insert in a sorted set (timestamp: search)
        Timestamp ts = new Timestamp(new Date().getTime());
        Utils.print("search-text", search + " at "+ ts.getTime());
        jedis.zadd("search", -ts.getTime(), search);
    }

    public List<String> getLastNSearches( int n ) {
        List<String> res = new ArrayList<>();
        for(String s: jedis.zrange("search",0, -1) ){
            res.add(s);
            if(res.size()==n) return res;
            Utils.print("last-searches", s);
        }
        return res;
    }

    public static void main(String args[]){
        RedisRepository  r = new RedisRepository();
        r.insertSearch("dark-vader");
        r.insertSearch("anakin-vader");
        r.insertSearch("luke-vader");
        r.insertSearch("ramona-vader");
        r.insertSearch("daniel-vader");
        r.getLastNSearches(10);
    }
}
