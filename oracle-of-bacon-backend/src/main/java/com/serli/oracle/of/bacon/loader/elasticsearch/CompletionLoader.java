package com.serli.oracle.of.bacon.loader.elasticsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.action.support.master.AcknowledgedResponse;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;

public class CompletionLoader {
    private static AtomicInteger COUNT = new AtomicInteger(0);
    private static final int BULK_SIZE = 200000;
    private static final boolean DELETE_INDEX = true;

    // Pre-compile regex
    private static final Pattern actorPattern = Pattern.compile("(.*), (.*)");

    
    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        if (args.length != 1) {
            System.err.println("Expecting 1 arguments, actual : " + args.length);
            System.err.println("Usage : completion-loader <actors file path>");
            System.exit(-1);
        }
        String inputFilePath = args[0];
        */

        String inputFilePath = "./build/resources/main/imdb-data/actors.csv";
        
        RestHighLevelClient client = ElasticSearchRepository.createClient();
    
        // Re-create the index
        if (DELETE_INDEX) deleteIndex(client); // would be cleaner to find a way to pass ignore_unavailable=false
        createIndex(client);
        
        // Configure mapping
        configureMapping(client);

        // Split requests so we don't get [HTTP/1.1 413 Request Entity Too Large]
        List<BulkRequest> requests = new ArrayList<BulkRequest>();        

        System.out.println("Preparing requests...");
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFilePath))) {
            bufferedReader
                .lines()
                .forEach(line -> {
                    int i = CompletionLoader.COUNT.getAndIncrement();
                    if ((i - 1) % BULK_SIZE == 0) requests.add(new BulkRequest());

                    // Skip the header line
                    if (i == 0) return;

                    // Format our object
                    Map<String, Object> jsonMap = prepareJSON(line);

                    // Create the index request
                    IndexRequest indexRequest = new IndexRequest(ElasticSearchRepository.INDEX).id(i + "").source(jsonMap);
                    
                    // Add it to the last bulk request
                    requests.get(requests.size() - 1).add(indexRequest);
                });
        }

        // Execute all requests in sequence, synchronously
        // could probably be improved by running the requests asynchronously, but this does the job
        for (int i = 0; i < requests.size(); i++) {
            BulkRequest request = requests.get(i);
            System.out.println("Sending bulk request " + (i+1) + "/" + requests.size() + "...");
            BulkResponse resp = client.bulk(request, RequestOptions.DEFAULT);

            if (resp.hasFailures()) {
                System.out.println("Failure(s) occured during bulk request.");
            } else {
                double took = resp.getTook().getSecondsFrac();
                System.out.println("Successfully inserted " + 	request.requests().size() + " actors (took " + took + "s)");
            }
        }

        client.close();
    }


    private static void deleteIndex(RestHighLevelClient client) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(ElasticSearchRepository.INDEX); 

        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            System.out.println("Index deletion was acknowledged");
        } else {
            System.out.println("Failed to delete index");
        }
    }

    private static void createIndex(RestHighLevelClient client) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(ElasticSearchRepository.INDEX); 

        AcknowledgedResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            System.out.println("Index creation was acknowledged");
        } else {
            System.out.println("Failed to create index");
        }
    }

    private static void configureMapping(RestHighLevelClient client) throws IOException {
        System.out.println("Configuring mapping...");
        PutMappingRequest request = new PutMappingRequest(ElasticSearchRepository.INDEX); 
        request.source(
            "{\n" +
            "  \"properties\": {\n" +
            "    \"suggest\": {\n" +
            "      \"type\": \"completion\"\n" +
            "    },\n" +
            "    \"name\": {\n" +
            "      \"type\": \"keyword\"\n" +
            "    }\n" +
            "  }\n" +
            "}", 
            XContentType.JSON
        );

        AcknowledgedResponse response = client.indices().putMapping(request, RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            System.out.println("Success");
        } else {
            System.out.println("Failure");
        }
    }
    
    private static Map<String, Object> prepareJSON(String line) {
        String actorName = line.replaceAll("^\"|\"$", "");
        String actorNameFormatted = actorName;
        List<String> suggest = new ArrayList<String>();
        suggest.add(actorName);
        
        // Extract the first and second name for the 'suggest' property
        Matcher m = actorPattern.matcher(actorNameFormatted);
        if (m.matches()) {
            String s1 = m.group(1).trim();
            String s2 = m.group(2).trim();
            if (s1.length() > 0) suggest.add(s1);
            if (s2.length() > 0) suggest.add(s2);
            // Add the complete name too
            actorNameFormatted = (s2 + " " + s1).trim();
            suggest.add(actorNameFormatted);
        }
        
         // Create the JSON object
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", actorName);
        jsonMap.put("name_formatted", actorNameFormatted);
        jsonMap.put("suggest", suggest);

        return jsonMap;
    
    }
}
