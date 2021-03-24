package com.serli.oracle.of.bacon.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;

import static org.neo4j.driver.Values.parameters;

public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
    }

    public List<GraphItem> getConnections(String actorName) {
        List<GraphItem> res = new ArrayList<>();

        try (Session session = driver.session()) {
            String query = "match p=(n:Actor)-[*]-(o:Movie) where  n.name=$actorName return p";
            Result result = session.run(query, parameters("actorName", actorName));
            while (result.hasNext()) {
                Record x = result.next();
                Path p = x.get(0).asPath();
                for (Node n : p.nodes()) {
                    GraphNode darkVaderNode = mapNodeToGrapNode(n);
                    res.add(darkVaderNode);
                }
                for (Relationship r : p.relationships()) {
                    GraphEdge darkVaderRel = mapRelationShipToNodeEdge(r);
                    res.add(darkVaderRel);
                }
            }
        }
        return res;
    }

    public List<GraphItem> getSomeNodes() {
        List<GraphItem> res = new ArrayList<>();

        try (Session session = driver.session()) {
            String query = "match p=(n:Actor)-[*]-(o:Movie)  return p limit 5"; //query testing
            Result result = session.run(query, parameters("actorName", ""));
            while (result.hasNext()) {
                Record x = result.next();
                Path p = x.get(0).asPath();
                for (Node n : p.nodes()) {
                    GraphNode darkVaderNode = mapNodeToGrapNode(n);
                    res.add(darkVaderNode);
                }
                for (Relationship r : p.relationships()) {
                    GraphEdge darkVaderRel = mapRelationShipToNodeEdge(r);
                    res.add(darkVaderRel);
                }
            }
        }
        return res;
    }

    public String connectionsToJson(List<GraphItem> res) {
        String json = "[";
        for (int i = 0; i < res.size(); i++) {
            if (i != 0) json += ",";
            json += res.get(i).toString();
        }
        json += "]";
        Utils.print("json-neo4j", json);
        return json;
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
            return "{\n" +
                    "\"data\": {\n" +
                    "\"id\":" + id + ",\n" +
                    "\"type\": \"" + type + "\",\n" +
                    "\"value\": \"" + value + "\"\n" +
                    "}\n" +
                    "}\n";
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
            return "{\n" +
                    "\"data\": {\n" +
                    "\"id\": " + id + ",\n" +
                    "\"source\": " + source + ",\n" +
                    "\"target\": " + target + ",\n" +
                    "\"value\": \"" + value + "\"\n" +
                    "}\n" +
                    "}\n";
        }
    }
}
