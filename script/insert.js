const csv = require("csv-parser");
const fs = require("fs");
const { Client } = require("@elastic/elasticsearch");

const client = new Client({ node: "http://localhost:9200" });

async function insert() {
  client.indices.create({ index: 'imdb' }, (err, resp) => {
    if (err) console.trace(err.message);
  });

  let actors = [];
  let first = true;
  fs.createReadStream("./imdb-data/actors.csv")
    .pipe(csv())
    // Pour chaque ligne on créé un document JSON pour l'acteur correspondant
    .on("data", async ({ name }) => {
      actors.push(name);
    })
    // A la fin on créé l'ensemble des acteurs dans ElasticSearch
    .on("end", () => {
      client.bulk(createBulkInsertQuery(actors), (err, resp) => {
        if (err) console.trace(err.message);
        else console.log(`Inserted ${resp.body.items.length} actors`);
        client.close();
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
  const body = names.flatMap((name) => [{ index: { _index: INDEX_NAME, _type: '_doc' } }, name]);
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
