package com.serli.oracle.of.bacon.repository;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Transaction;

public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
    }

    public List<Map<String, GraphItem>> getConnectionsToKevinBacon(String actorName) {
        Session session = driver.session();
        List<Map<String, GraphItem>> connections = new ArrayList<Map<String, GraphItem>>();
        // MATCH ({name:actorName})-[:PLAYED_IN*1]->(m) WHERE ({name:"Bacon, Kevin (I)"})-[:PLAYED_IN*1]->(m) RETURN m
        String request = "MATCH p=shortestPath(({ name: \"Bacon, Kevin (I)\" })-[:PLAYED_IN*]-({ name:" + actorName +" }))\n RETURN p";
        Result result = session.run(request);
        while (result.hasNext()) {
            Record rec = result.next();

            Map<String, GraphItem> item = new HashMap<String, GraphItem>();
            //item.put(result.get(0).asString() , (GraphItem) (result.get(0)));
            //result.next();
            System.out.println(rec.toString());
            //connections.add(item);
        }
        return connections;
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
    }
}
