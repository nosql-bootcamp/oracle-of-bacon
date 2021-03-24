package com.serli.oracle.of.bacon.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.abego.treelayout.internal.util.java.util.IteratorUtil;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.Value;

public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "neo"));
    }

    public List<Map<String, GraphItem>> getConnectionsToKevinBacon(String actorName) {
        Session session = driver.session();

        // MATCH (KevinB:Actor {name: 'Bacon, Kevin (I)'} ),
        // (Al:Actor {name: 'Nisbet, Stuart (I)'}),
        // p = shortestPath((KevinB)-[:PLAYED_IN*]-(Al))
        // RETURN p

        Value query = session.writeTransaction(new TransactionWork<Value>() {
            public Value execute(Transaction tx) {
                Result result = tx.run(
                        "MATCH (KevinB:Actor {name: 'Bacon, Kevin (I)'}), " + "(to:Actor {name: $actorName}), "
                                + "p = shortestPath((KevinB)-[:PLAYED_IN*]-(to)) " + "RETURN p",
                        parameters("actorName", actorName));

                return result.single().get("p");
            }
        });

        Path result = query.asPath();
        System.out.println("nodes : " + result.nodes());
        System.out.println("relations : " + result.relationships());


        List<Map<String, GraphItem>> finalResult = new ArrayList<Map<String, GraphItem>>();


        for (Node node : result.nodes()) {
            Map nodeMap = new HashMap();
            nodeMap.put("data", mapNodeToGrapNode(node));
            finalResult.add(nodeMap);
        }
        for (Relationship relationship : result.relationships()) {
            Map edgeMap = new HashMap();
            edgeMap.put("data", mapRelationShipToNodeEdge(relationship));
            finalResult.add(edgeMap);
        }

        return finalResult;
        
    }

    private GraphEdge mapRelationShipToNodeEdge(Relationship relationship) {
        return new GraphEdge(relationship.id(), relationship.startNodeId(), relationship.endNodeId(),
                relationship.type());
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
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

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
