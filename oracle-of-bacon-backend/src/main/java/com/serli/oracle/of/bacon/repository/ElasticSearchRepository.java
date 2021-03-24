package com.serli.oracle.of.bacon.repository;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
// import org.elasticsearch.index.query.QueryStringQueryBuilder;
// import org.elasticsearch.search.suggest.SuggestBuilder;
// import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.elasticsearch.search.SearchHit;

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
        MatchQueryBuilder queryBuilder = new MatchQueryBuilder("suggest", searchQuery);
        // Not working as intended
        // QueryStringQueryBuilder queryBuilder = new QueryStringQueryBuilder(String.format("*%s*", searchQuery));
        // queryBuilder = queryBuilder.defaultField("suggest");
        /*
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        TermSuggestionBuilder suggestionBuilder = new TermSuggestionBuilder("suggest");
        suggestionBuilder = suggestionBuilder.text(searchQuery);
        suggestBuilder = suggestBuilder.addSuggestion("suggestion", suggestionBuilder);
        */
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // sourceBuilder = sourceBuilder.query(queryBuilder).size(10).suggest(suggestBuilder);
        sourceBuilder = sourceBuilder.query(queryBuilder).size(10);
        String[] indices = { "actor" };
        SearchRequest request = new SearchRequest(indices, sourceBuilder);
        SearchResponse response = this.client.search(request, RequestOptions.DEFAULT);
        List<String> suggests = new ArrayList<String>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            suggests.add(map.get("name").toString());
        }
        return suggests;
    }
}
