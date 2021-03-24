This is the instruction to replicate the results over neo4j bacon

1 - create a connection dbms to neo4j
  User: oracle-bacon
  Password: password
2 - import the data
  In the folder of imports copy the three files 
  ***actors.csv, movies.csv, roles.csv***
3 - in your terminal execute bin/cyphershell
  User: neo4j
  password: password # this is the password of your dbms
4 - Run the following commands in cyphershell
4.1 - First check that the data is right executing the following commands
These commands will return the number of lines, (you should see something like this)


```
neo4j@neo4j> LOAD CSV FROM 'file:///actors.csv' AS row RETURN count(row);
+------------+
| count(row) |
+------------+
| 1854871    |
+------------+

neo4j@neo4j> LOAD CSV FROM 'file:///movies.csv' AS row RETURN count(row);
+------------+
| count(row) |
+------------+
| 759282     |
+------------+

neo4j@neo4j> LOAD CSV FROM 'file:///roles.csv' AS row RETURN count(row);
+------------+
| count(row) |
+------------+
| 13103223   |
+------------+


```


5- Now let's load the data, the following lines ensure that even if you 
execute the same script, will not repeat information :v


Actors
```
:auto USING PERIODIC COMMIT 
LOAD CSV WITH HEADERS FROM 'file:///actors.csv' AS line  
WITH line.`name:ID` as name 
MERGE(o:Actor{name:name}) set o.name=name  
RETURN count(o);
```

Movies
```
:auto USING PERIODIC COMMIT 
LOAD CSV WITH HEADERS FROM 'file:///movies.csv' AS line  
WITH line.`title:ID` as title 
MERGE(o:Movie{title:title}) set o.title=title 
RETURN count(o);
```

Relations
```
:auto USING PERIODIC COMMIT  500
LOAD CSV WITH HEADERS FROM 'file:///roles.csv'
AS line WITH line.`:END_ID` as title, line.`:TYPE` 
as relation, line.`:START_ID` as actor 
MATCH (a:Actor{name:actor})
MATCH (m:Movie{title:title})
MERGE (a)-[rel:PLAYED_IN]->(m)
RETURN count(rel);
```


Queries 

Get some actors and movies where they played in
```
MATCH (o:Actor)-[rel:PLAYED_IN]->(p:Movie) RETURN p,rel,o LIMIT 50;
```

Delete all connection type ```relation```
```
match(n)-[r:relation]-(o) delete r;
```

Get movies of a given actor
```
match p=(n:Actor)-[r:PLAYED_IN]-(o:Movie) where  n.name="Bacon, Kevin (I)" return p;
match p=(n:Actor)-[r:PLAYED_IN]-(o:Movie) where  n.name="A., Randy" return p;

```

Get subgraphs
```
MATCH (o:Actor)-[rel:PLAYED_IN]->(p:Movie)
RETURN p,rel,o LIMIT 100;
```


Get all nodes related to a given node(in this case nodes related to Abadie, William

```
match p=(n:Actor)-[*]-(o:Movie) where  n.name="Abadie, William" return p;
```


With the last query now it rests to implement it in java, here we have the link

(Neo4j Data)[https://neo4j.com/developer/java/)

For further information please visit [Import Data](https://neo4j.com/developer/desktop-csv-import/)
