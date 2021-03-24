const csv = require("csv-parser");
const fs = require("fs");
const { Client } = require("@elastic/elasticsearch");

// /!\ il faut mettre le dossier imbd-data dans le dossier racine du projet

const client = new Client({ node: "http://localhost:9200" });
const INDEX_NAME = 'actor';

async function insert() {

    // TODO créer l'index (et plus pour la ré-exécution ?)
    // Drop index if exists
    await client.indices.delete({
        index: INDEX_NAME,
        ignore_unavailable: true
    });

    // Création de l'indice
    client.indices.create({ index: INDEX_NAME }, (err, resp) => {
        if (err) console.trace(err.message);
    });

    let actors = [];
    let first = true;
    fs.createReadStream("../imdb-data/actors.csv")
        .pipe(csv())
        // Pour chaque ligne on créé un document JSON pour l'acteur correspondant
        .on("data", name => {
            ///console.log("name:", name)
            actors.push(name['name:ID'])
        })
        // A la fin on créé l'ensemble des acteurs dans ElasticSearch
        .on("end", () => {
            // TODO insérer dans elastic (les fonctions ci-dessous peuvent vous aider)
            body = createBulkInsertQueries(actors, 10000)
                // client.bulk(createBulkInsertQuery(actors), (err, resp) => {
                //     if (err) console.trace(err.message);
                //     else console.log(`Inserted ${resp.body.items.length} actors`);
                //     client.close();
                // });
            recBulk(client, body)
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
    body = createBulkInsertQuery(names).body
    console.log(body[0])
    size = Math.floor(body.length / length)
    bodies = []
    for (i = 0; i < length; i++) {
        bodies.push({ body: body.slice(i * size, (i + 1) * size) })
    }
    bodies.push({ body: body.slice((length + 1) * size, body.length - 1) })
    return bodies
}

// Fonction utilitaire permettant de formatter les données pour l'insertion "bulk" dans elastic
function createBulkInsertQuery(names) {
    const body = names.reduce((acc, name) => {
        const suggest = [name];
        const parts = name.split(",");
        parts.forEach((part) => suggest.push(part.trim()));
        parts.reverse().forEach((part) => suggest.push(part.trim()));

        acc.push({ index: { _index: INDEX_NAME, _type: "_doc" } });
        acc.push({ name, suggest });
        return acc;
    }, []);

    return { body };
}

insert().catch(console.error);