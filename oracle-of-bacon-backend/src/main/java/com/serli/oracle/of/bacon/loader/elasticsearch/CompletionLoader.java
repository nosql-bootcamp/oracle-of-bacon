package com.serli.oracle.of.bacon.loader.elasticsearch;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CompletionLoader {
    private static AtomicInteger COUNT = new AtomicInteger(0);

    public static void main(String[] args) throws IOException, InterruptedException {
        RestHighLevelClient client = ElasticSearchRepository.createClient();

        if (args.length != 1) {
            System.err.println("Expecting 1 arguments, actual : " + args.length);
            System.err.println("Usage : completion-loader <actors file path>");
            System.exit(-1);
        }

        createOrRecreateImdbIndex(client);

        AtomicReference<BulkRequest> bulk = new AtomicReference<>(new BulkRequest());

        String inputFilePath = args[0];
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFilePath))) {
            bufferedReader
                    .lines()
                    .skip(1)
                    .forEach(line -> {
                                String actorName = line.substring(1, line.length() - 1);

                                List<String> suggestionTerms = new ArrayList<>(Arrays.asList(actorName
                                        .replaceAll("\\(.+\\)", "")
                                        .split(", ")));

                                Collections.reverse(suggestionTerms);
                                String joinedReversedTerm = String.join(" ", suggestionTerms);

                                suggestionTerms.add(actorName);
                                suggestionTerms.add(joinedReversedTerm);

                                bulk.get().add(new IndexRequest("actor").source(XContentType.JSON, "name", actorName, "suggest", suggestionTerms));

                                if (COUNT.incrementAndGet() % 10000 == 0) {
                                    try {
                                        client.bulk(bulk.get(), RequestOptions.DEFAULT);
                                        System.out.println("Inserted " + COUNT.get() + " actors");
                                        bulk.set(new BulkRequest());
                                    } catch (IOException e) {
                                        throw new IllegalArgumentException(e);
                                    }
                                }
                    });
        }

        System.out.println("Inserted total of " + COUNT.get() + " actors");

        client.close();
    }

    private static void createOrRecreateImdbIndex(RestHighLevelClient client) throws IOException {
        final boolean indexExists = client.indices().exists(new GetIndexRequest("actor"), RequestOptions.DEFAULT);

        if(indexExists) {
            System.out.println("Deleting indice");
            client.indices().delete(new DeleteIndexRequest("actor"), RequestOptions.DEFAULT);
            System.out.println("Done");
        }


        System.out.println("Creating indice");
        CreateIndexRequest request = new CreateIndexRequest("actor");

        request.mapping("{" +
                "  \"properties\": {\n" +
                "    \"suggest\": {\n" +
                "      \"type\": \"completion\"\n" +
                "    },\n" +
                "    \"name\": {\n" +
                "      \"type\": \"text\"\n" +
                "    }\n" +
                "  }" +
                "}", XContentType.JSON);
        client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println("Done");
    }
}
