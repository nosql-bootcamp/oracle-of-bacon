const csv = require("csv-parser");
const fs = require("fs");
const { Client } = require("@elastic/elasticsearch");

const client = new Client({ node: "http://localhost:9200" });

async function insert() {
  client.indices.create({ index: 'name' }, (err, resp) => {
    if (err) console.trace(err.message);
  });

  let actors = [];
  let first = true;
  fs.createReadStream("../imdb-data/actors.csv")
    .pipe(csv())
    // Pour chaque ligne on créé un document JSON pour l'acteur correspondant
    .on("data", async (data) => {
        actors.push(data["name:ID"]);
    })
    // A la fin on créé l'ensemble des acteurs dans ElasticSearch
    .on("end", () => {
        recBulk(client, createBulkInsertQueries(actors, actors.length / 10000));
    });
}

function recBulk(client, bulks) {
  console.log("remaining bulks " + bulks.length);
  if (bulks.length <= 0) {
    client.close();
    return Promise.resolve();
  }

  const first = bulks.pop();
  return client.bulk(first).then((r) => {
    console.log("Done inserting " + first.body.length / 2);
    return recBulk(client, bulks);
  });
}

function createBulkInsertQueries(names, length) {
  const nb_elements = names.length / length;
  let i = 0;
  // Etape 1 : On sépare la liste des acteurs en sous-listes de longueur à peu près égale
  const chunks = names.reduce((acc, name) => {
    if (i < nb_elements) {
        acc[acc.length-1].push(name);
        i++;
    }
    else {
        acc.push([name]);
        i = 0;
    }
    return acc;
  }, [[]]);
  // Etape 2 : On transforme ces sous-listes en requêtes
  const queries = chunks.map(chunk => createBulkInsertQuery(chunk));
  return queries;
}

// Fonction utilitaire permettant de formatter les données pour l'insertion "bulk" dans elastic
function createBulkInsertQuery(names) {
  const body = names.reduce((acc, name) => {
    const suggest = [name];
    const parts = name.split(",");
    parts.forEach((part) => suggest.push(part.trim()));
    parts.reverse().forEach((part) => suggest.push(part.trim()));

    acc.push({ index: { _index: "actor", _type: "_doc" } });
    acc.push({ name, suggest });
    return acc;
  }, []);

  return { body };
}

insert().catch(console.error);
