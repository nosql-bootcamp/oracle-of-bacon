package com.serli.oracle.of.bacon.repository;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import static org.neo4j.driver.v1.Values.parameters;

import java.util.LinkedList;

public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "motdepasse"));
    }

	private String getQuery() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("MATCH p = shortestPath((Kevin:Actor {name: \"Bacon, Kevin (I)\"})-[:PLAYED_IN*]-(Other:Actor {name: {nomAutreActeur}})) ")
				  .append("RETURN p")
		;
		return strBuilder.toString();
	}

	public List<GraphItem> getConnectionsToKevinBacon(String actorName) {
        Session session = driver.session();
          // Implémentation de l'oracle de Bacon
          List<GraphItem> graphe = new LinkedList<>();
          StatementResult resultats = null;
          try (Transaction transaction = session.beginTransaction())
          {
              resultats = transaction.run(getQuery(), parameters("nomAutreActeur", actorName));
              transaction.success();
          }
  
          while (resultats.hasNext()) {
              Record entree = resultats.next();
              List<Value> valeurs = entree.values();
  
              
              for (Value valeur : valeurs) {
                  Path chemin = valeur.asPath();
  
                  chemin.nodes().forEach(node -> {
                      String type = node.labels().iterator().next();
  
                      String clePropriete = "Actor".equals(type) ? "name" : "title";
                      graphe.add(new GraphNode(node.id(), node.get(clePropriete).asString(), type));
                  });
  
                  chemin.relationships().forEach(relationship -> {
                      graphe.add(new GraphEdge(relationship.id(), relationship.startNodeId(), relationship.endNodeId(), relationship.type()));
                  });
              }
          }
  
          return graphe;
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
        @Override
        public String toString() {
        	StringBuilder strBuilder = new StringBuilder();
        	strBuilder.append("{ \"data\" : { ")
        			  .append("	    \"id\": "     ).append(this.id    ).append(", "  )
        			  .append("     \"source\": " ).append(this.source).append(", "  )
        			  .append("     \"target\": " ).append(this.target).append(", "  )
        			  .append("     \"value\": \"").append(this.value ).append("\"")
        			  .append("}}")
        	;
        	return strBuilder.toString();
        }
    }
}
