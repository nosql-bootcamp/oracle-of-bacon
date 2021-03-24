package com.serli.oracle.of.bacon.repository;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;


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

    public boolean indexExist(String index) throws Exception {
        GetIndexRequest request = new GetIndexRequest();
        request.indices(index);
        return this.client.indices().exists(request, RequestOptions.DEFAULT);
    }


    private SearchRequest createSearchRequest(String searchQuery, String index) throws Exception {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(QueryBuilders.multiMatchQuery(searchQuery, "name", "title")); //looking in authors and movies
        if (!this.indexExist(index) ){
            throw new Exception("E01: Index not found" + index);
        }
        return new SearchRequest(index).source(searchSourceBuilder);
    }

    public List<String> getActorsSuggests(String searchQuery) throws Exception {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(QueryBuilders.multiMatchQuery(searchQuery, "name", "title"));
        SearchResponse response = client.search(createSearchRequest(searchQuery, "suggestions"), RequestOptions.DEFAULT);
        ArrayList<String> ans = new ArrayList<>();
        if (response.getHits().getTotalHits().value == 0){
            return ans;
        }
        for (SearchHit hit : response.getHits().getHits()) {
            ans.add(hit.getSourceAsString());
        }
        return ans;
    }
}
