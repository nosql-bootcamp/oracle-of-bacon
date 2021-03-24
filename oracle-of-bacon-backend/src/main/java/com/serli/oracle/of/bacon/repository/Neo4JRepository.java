package com.serli.oracle.of.bacon.repository;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;

import static org.neo4j.driver.Values.parameters;

public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
    }

    public List<Map<String, GraphItem>> getConnectionsToKevinBacon(String actorName) {
        Session session = driver.session();

        List<Map<String, GraphItem>> graph = new ArrayList<Map<String, GraphItem>>();
        try (Transaction tx = session.beginTransaction()) {
            Result result = tx.run("MATCH (start:Actors {name: 'Bacon, Kevin (I)'}), (end:Actors {name: $actorName})" +
                                    "MATCH path = shortestPath((start)-[actors:PLAYED_IN *]-(end))" +
                                    "RETURN path", parameters("actorName", actorName));


            for (Record value: result.list()) {
                Path path = value.get("path").asPath();
                for (Node node: path.nodes()) {
                    List<Value> _name = new ArrayList<Value>();
                    node.values().forEach(_name::add);
                    String name = _name.get(0).asString();

                    Map<String, GraphItem> elem = new HashMap<String, GraphItem>();
                    elem.put(name, mapNodeToGrapNode(node));
                    graph.add(elem);
                }
                for (Relationship relationship: path.relationships()) {
                    Map<String, GraphItem> elem = new HashMap<String, GraphItem>();
                    elem.put(relationship.type(), mapRelationShipToNodeEdge(relationship));
                    graph.add(elem);
                }
            }
        }

        return graph;
    }

    private GraphEdge mapRelationShipToNodeEdge(Relationship relationship) {
        return new GraphEdge(relationship.id(), relationship.startNodeId(), relationship.endNodeId(), relationship.type());
    }

    private GraphNode mapNodeToGrapNode(Node node) {
        String type = node.labels().iterator().next();
        String value = null;
        if (!node.get("name").isNull()) {
            value = node.get("name").asString();
        } else if (!node.get("title").isNull()) {
            value = node.get("title").asString();
        }
        return new GraphNode(node.id(), value, type);
    }


    public static abstract class GraphItem {
        public final long id;

        private GraphItem(long id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphItem graphItem = (GraphItem) o;

            return id == graphItem.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

    private static class GraphNode extends GraphItem {
        public final String type;
        public final String value;

        public GraphNode(long id, String value, String type) {
            super(id);
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return "\"id\": \"" + this.id + "\"" +
            ",\"value\": \"" + this.value + "\"" +
            // .substring(0, this.type.length()-1) to remove the trailing -s so
            // that the types are valid for the frontend
            ",\"type\": \"" + this.type.substring(0, this.type.length()-1) + "\"";
        }
    }

    private static class GraphEdge extends GraphItem {
        public final long source;
        public final long target;
        public final String value;

        public GraphEdge(long id, long source, long target, String value) {
            super(id);
            this.source = source;
            this.target = target;
            this.value = value;
        }

        @Override
        public String toString() {
            return "\"id\": \"" + this.id + "\"" +
            ",\"source\": \"" + this.source + "\"" +
            ",\"target\": \"" + this.target + "\"" +
            ",\"value\": \"" + this.value + "\"";
        }
    }
}
