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
import org.neo4j.driver.types.Path;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import static org.neo4j.driver.Values.parameters;

public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "pandora-agent-twist-ariel-critic-7077"));
    }

    public List<Map<String, GraphItem>> getConnectionsToKevinBacon(String actorName) {
        Session session = driver.session();
        List<Map<String, GraphItem>> result = session.writeTransaction(new TransactionWork<List<Map<String, GraphItem>>>() {
            @Override
            public List<Map<String, GraphItem>> execute(Transaction tx) {
                Result result = tx.run("MATCH p=shortestPath(" +
                                "(bacon:Actor {name:'Bacon, Kevin (I)'})-[*]-" +
                                "(other:Actor {name:$actorName})" +
                                ")" +
                                "RETURN p",
                        parameters("actorName", actorName));
                Path path = result.single().get("p").asPath();
                List<Map<String, GraphItem>> list = new ArrayList<Map<String, GraphItem>>();
                for (Node node : path.nodes()) {
                    Map<String, GraphItem> map = new HashMap<String, GraphItem>();
                    map.put(String.valueOf(node.id()), mapNodeToGraphNode(node));
                    list.add(map);
                }
                for (Relationship relation : path.relationships()) {
                    Map<String, GraphItem> map = new HashMap<String, GraphItem>();
                    map.put(String.valueOf(relation.id()), mapRelationShipToGraphEdge(relation));
                    list.add(map);
                }
                return list;
            }
        });
        return result;
    }

    private GraphEdge mapRelationShipToGraphEdge(Relationship relationship) {
        return new GraphEdge(relationship.id(), relationship.startNodeId(), relationship.endNodeId(), relationship.type());
    }

    private GraphNode mapNodeToGraphNode(Node node) {
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
            return String.format("{ \"id\": \"%s\", \"value\": \"%s\", \"type\": \"%s\" }", String.valueOf(this.id), this.value, this.type);
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
            return String.format("{ \"id\": \"%s\", \"source\": \"%s\", \"target\": \"%s\", \"value\": \"%s\" }", String.valueOf(this.id), this.source, this.target, this.value);
        }
    }
}
