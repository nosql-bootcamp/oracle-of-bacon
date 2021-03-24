package com.serli.oracle.of.bacon.api;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import com.serli.oracle.of.bacon.repository.Neo4JRepository;
import com.serli.oracle.of.bacon.repository.RedisRepository;
import net.codestory.http.annotations.Get;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    //  change return type
    public String getConnectionsToKevinBacon(String actorName) {
        System.out.println("-----\nappel getConnectionsToKevinBacon...");
    	List<Neo4JRepository.GraphItem> elementsGraphe = neo4JRepository.getConnectionsToKevinBacon(actorName);
    	String elements = elementsGraphe.stream().map(element -> element.toString()).collect(Collectors.joining(", "));

    	System.out.println("Retourne le resultat :");
    	System.out.println(elements);

    	System.out.println("-----");
    	return "[" + elements + "]";

  
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
        return redisRepository.getLastTenSearches();
    }
}
