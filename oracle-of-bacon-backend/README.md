# Oracle of Bacon
This application is an Oracle of Bacon implementation based on NoSQL data stores :
* ElasticSearch (http) - localhost:9200
* Redis - localhost:6379
* Mongo - localhost:27017
* Neo4J (bolt) - locahost:7687

To build :
```
./gradlew build
```

To Run, execute class *com.serli.oracle.of.bacon.Application*.

neo4j-admin import --database=neo4j --nodes=..\..\Data\movies.csv --nodes=..\..\Data\actors.csv --relationships=..\..\Data\roles.csv

On a eu beaucoup de problèmes sur l'import de la partie neo4j qui nous a empecher de continuer.
