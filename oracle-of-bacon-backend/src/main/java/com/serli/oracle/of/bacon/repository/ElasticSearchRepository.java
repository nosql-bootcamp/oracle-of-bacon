package com.serli.oracle.of.bacon.repository;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticSearchRepository {

    private final RestHighLevelClient client;

    public ElasticSearchRepository() {
        client = createClient();

    }

    public static RestHighLevelClient createClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")
                )
        );
    }

    public List<String> getActorsSuggests(String searchQuery) throws IOException {
        // TODO
        // Ne marche pas
        SearchRequest searchRequest = null;
        RequestOptions options = null;
        this.client.search(searchRequest, options);
        return null;
    }
}
