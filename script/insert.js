const csv = require("csv-parser");
const fs = require("fs");
const { Client } = require("@elastic/elasticsearch");

const client = new Client({ node: "http://localhost:9200" });
const INDEX_NAME = 'actors';

async function insert() {
  // créer l'index (et delete pour la ré-exécution)
  await client.indices.delete({
    index: INDEX_NAME,
    ignore_unavailable: true
  });

  await client.indices.create({
    index: INDEX_NAME,
    body : {
      "mappings": {
        "properties": {
          "name": { "type": "text" },
          "suggest": { "type": "completion" },
        }
      }
    }
  });

  let actors = [];
  let first = true;
  fs.createReadStream("/home/agustin/Desktop/IMT/big_data/actors.csv")
    .pipe(csv())
    // Pour chaque ligne on créé un document JSON pour l'acteur correspondant
    .on("data", async ({ name }) => { // on a change la premiere ligne du csv : name:ID -> name
      // ajouter les acteurs dans la liste
      actors.push(name);
    })
    // A la fin on créé l'ensemble des acteurs dans ElasticSearch
    .on("end", async () => {
      // insérer dans elastic
      recBulk(client, createBulkInsertQueries(actors, 100000)).then(res => {
        console.log(res);
      });
    });
}

function recBulk(client, bulks) {
  console.log("remaining bulks " + bulks.length);
  if (bulks.length <= 0) {
    return Promise.resolve();
  }

  const first = bulks.pop();
  return client.bulk(first).then((r) => {
    console.log("Done inserting " + first.body.length / 2);
    return recBulk(client, bulks);
  });
}

function createBulkInsertQueries(names, length) {
  let i;
  let slices = []
  let bulks = []
  for(i = 0; i <= names.length / length; i++) {
    slices.push(names.slice(i, i + length));
  }
  slices.forEach(slice => {
    bulks.push(createBulkInsertQuery(slice))
  });
  return bulks;
}

// Fonction utilitaire permettant de formatter les données pour l'insertion "bulk" dans elastic
function createBulkInsertQuery(names) {
  const body = names.reduce((acc, name) => {

    const suggest = [name];
    const parts = name.split(",");
    parts.forEach((part) => suggest.push(part.trim()));
    parts.reverse().forEach((part) => suggest.push(part.trim()));

    acc.push({ index: { _index: "actors", _type: "_doc" } });
    acc.push({ name, suggest });
    return acc;
  }, []);

  return { body };
}

insert().catch(console.error);
