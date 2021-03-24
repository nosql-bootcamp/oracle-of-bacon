package com.serli.oracle.of.bacon.api;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import com.serli.oracle.of.bacon.repository.Neo4JRepository;
import com.serli.oracle.of.bacon.repository.RedisRepository;
import net.codestory.http.annotations.Get;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

public class APIEndPoint {
    private final Neo4JRepository neo4JRepository;
    private final ElasticSearchRepository elasticSearchRepository;
    private final RedisRepository redisRepository;

    public APIEndPoint() {
        neo4JRepository = new Neo4JRepository();
        elasticSearchRepository = new ElasticSearchRepository();
        redisRepository = new RedisRepository();
    }

    @Get("bacon-to?actor=:actorName")
    // TODO change return type
    public String getConnectionsToKevinBacon(String actorName) {
        List<AbstractMap.SimpleEntry<String, Neo4JRepository.GraphItem>> result = this
                .neo4JRepository
                .getConnectionsToKevinBacon(actorName)
                .stream().map(item -> new AbstractMap.SimpleEntry<>("data", item))
                .collect(Collectors.toList());

        return TypeConvert.toJson(result);
    }

    @Get("suggest?q=:searchQuery")
    public List<String> getActorSuggestion(String searchQuery) throws IOException {
        return Arrays.asList("Niro, Chel",
                "Senanayake, Niro",
                "Niro, Juan Carlos",
                "de la Rua, Niro",
                "Niro, Simão");
    }

    @Get("last-searches")
    public List<String> last10Searches() {
        return redisRepository.getLast10Searches();
    }
}
