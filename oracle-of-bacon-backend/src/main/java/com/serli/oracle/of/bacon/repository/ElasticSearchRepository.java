package com.serli.oracle.of.bacon.repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;

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
        String suggestionName = "actor-suggestion";

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        CompletionSuggestionBuilder termSuggestionBuilder =
                SuggestBuilders.completionSuggestion("suggest").prefix(searchQuery);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(suggestionName, termSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);

        final SearchResponse response = client.search(new SearchRequest("actor").source(searchSourceBuilder), RequestOptions.DEFAULT);

        return response.getSuggest().getSuggestion(suggestionName).getEntries().stream()
                .flatMap(entry -> StreamSupport.stream(entry.spliterator(), false))
                .map(option -> ((CompletionSuggestion.Entry.Option) option).getHit().getSourceAsMap().get("name").toString())
                .collect(Collectors.toList());
    }
}
