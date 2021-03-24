package com.serli.oracle.of.bacon.repository;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;


public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
    }

    public List<Map<String, GraphItem>> getConnectionsToKevinBacon(String actorName) {
        Session session = driver.session();

        Transaction transaction = session.beginTransaction();
        String baconName = "Bacon, Kevin (I)";

        StatementResult result = transaction.run(
                "MATCH " +
                        "(bc:Actors {name: {baconName}}), (ran:Actors {name: {relatedActorName}})," +
                        "p = shortestPath((bc)-[:PLAYED_IN*]-(ran)) " +
                        "WITH p WHERE length(p) > 1 " +
                        "RETURN p",
                parameters("baconName", baconName, "relatedActorName", actorName)
        );

        List<Path> paths = result
                .list()
                .stream()
                .flatMap(records -> records.values().stream().map(Value::asPath))
                .collect(Collectors.toList());

        List<GraphNode> nodes = paths
                .stream()
                .map(path -> iteratorToList(path.nodes().iterator()))
                .flatMap(ns -> ns.stream().map(this::toGraphNode))
                .collect(Collectors.toList());

        List<GraphEdge> edges = paths
                .stream()
                .map(path -> iteratorToList(path.relationships().iterator()))
                .flatMap(es -> es.stream().map(this::toGraphEdge))
                .collect(Collectors.toList());

        List<GraphItem> items = new ArrayList<>(nodes);
        items.addAll(edges);

        return items;
    }
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
