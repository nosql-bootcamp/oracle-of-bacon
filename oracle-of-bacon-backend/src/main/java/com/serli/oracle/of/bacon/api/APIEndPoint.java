package com.serli.oracle.of.bacon.api;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import com.serli.oracle.of.bacon.repository.Neo4JRepository;
import com.serli.oracle.of.bacon.repository.Neo4JRepository.GraphItem;
import com.serli.oracle.of.bacon.repository.RedisRepository;
import net.codestory.http.annotations.Get;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        redisRepository.addSearch(actorName);
        List<Map<String, GraphItem>> graph = neo4JRepository.getConnectionsToKevinBacon(actorName);
        return Arrays.toString(
            graph.stream()
                .map(map -> String.format("{ \"data\": %s }", map.entrySet().iterator().next()
                                                                 .getValue().toString()))
                .toArray());
    }

    @Get("suggest?q=:searchQuery")
    public List<String> getActorSuggestion(String searchQuery) throws IOException {
        return elasticSearchRepository.getActorsSuggests(searchQuery);
    }

    @Get("last-searches")
    public List<String> last10Searches() {
        return redisRepository.getLastXSearches(10);
    }
}
