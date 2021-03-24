const csv = require("csv-parser");
const fs = require("fs");
const { Client } = require("@elastic/elasticsearch");


async function insert() {

  
const client = new Client({ node: "http://localhost:9200" });

  await client.indices.delete({
    index: 'bacon',
    ignore_unavailable: true
  });

  await client.indices.create({ index: 'bacon' });

  let actors = [];
  let first = true;
  fs.createReadStream("./imdb-data/actors.csv")
    .pipe(csv())
    // Pour chaque ligne on créé un document JSON pour l'acteur correspondant
    .on("data", async (data) => {
      //console.log(data['name:ID'])
      actors.push({
        name: data['name:ID']
      });
    })
    // A la fin on créé l'ensemble des acteurs dans ElasticSearch
    .on("end", () => {
      client.bulk(createBulkInsertQueries(actors.map(actor => actor.name), 10000), (err, resp) => {
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
  return client.bulk(first).then(() => {
    console.log("Done inserting " + first.body.length / 2);
    return recBulk(client, bulks);
  });
}

function createBulkInsertQueries(names, length) {
  const arrays = [];
  while (names.length > 0) {
    arrays.push(names.splice(0, length));
  }

  return arrays.map((arr) => createBulkInsertQuery(arr));
}

// Fonction utilitaire permettant de formatter les données pour l'insertion "bulk" dans elastic
function createBulkInsertQuery(names) {
  //console.log(names);
  const body = names.reduce((acc, name) => {
    // const suggest = [name];
    // const parts = name.split(",");
    // parts.forEach((part) => suggest.push(part.trim()));
    // parts.reverse().forEach((part) => suggest.push(part.trim()));

    acc.push({ index: { _index: "bacon" } });
    acc.push({ name });
    return acc;
  }, []);
  //console.log(body);
  return { body };
}

insert().catch(console.error);
