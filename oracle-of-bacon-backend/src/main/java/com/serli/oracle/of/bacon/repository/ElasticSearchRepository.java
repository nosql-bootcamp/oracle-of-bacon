package com.serli.oracle.of.bacon.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;

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
        // TODO
        /*
            GET actors/_search
            {
              "suggest": {
                "mySuggest": {
                  "text": "niro",
                  "completion": {
                    "field": "suggest",
                    "skip_duplicates": true
                  }
                }
              }
            }
        */

        final List<String> suggestions = new ArrayList<String>();

        SearchRequest searchRequest = new SearchRequest(ElasticSearchRepository.INDEX);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        SearchSourceBuilder searchBuilder = new SearchSourceBuilder();


        SuggestionBuilder suggestionBuilder = SuggestBuilders.completionSuggestion("suggest")
                .size(5).prefix(searchQuery).skipDuplicates(true);
        suggestBuilder.addSuggestion("suggestion", suggestionBuilder);
        searchBuilder.suggest(suggestBuilder);

        searchRequest.source(searchBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        CompletionSuggestion completionSuggestion = searchResponse.getSuggest().getSuggestion("suggestion");
        List<CompletionSuggestion.Entry> entries = completionSuggestion.getEntries();

        entries.forEach(entry -> {
            entry.getOptions().forEach(option -> {
                suggestions.add(option.getHit().getSourceAsMap().get("name").toString());
            });
        });

        return suggestions;

    }
}
