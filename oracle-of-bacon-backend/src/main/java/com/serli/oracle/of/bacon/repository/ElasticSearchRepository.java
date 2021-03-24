package com.serli.oracle.of.bacon.repository;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;

public class ElasticSearchRepository {
    public static final String INDEX = "actors";

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
        // Create the output list
        List<String> suggestions = new ArrayList<String>();

        // Init a search request
        SearchRequest searchRequest = new SearchRequest(ElasticSearchRepository.INDEX);
        
        // Use a builder to build our search request
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SuggestionBuilder termSuggestionBuilder = SuggestBuilders.completionSuggestion("suggest").size(10).prefix(searchQuery);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("actor-suggest", termSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);

        // Execute the search request
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // Extract the hits from the response
        CompletionSuggestion compSuggestion = searchResponse.getSuggest().getSuggestion("actor-suggest");
        List<CompletionSuggestion.Entry> entries = compSuggestion.getEntries();

        if(entries != null) {    
            for(CompletionSuggestion.Entry entry : entries) {               
                for(CompletionSuggestion.Entry.Option opt : entry.getOptions()) {
                    String suggestText = opt.getText().string();
                    String hit = (String) opt.getHit().getSourceAsMap().get("name");
                    suggestions.add(hit);
                }
            }
        }

        return suggestions;
    }
}
