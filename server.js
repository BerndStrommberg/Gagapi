// server.js
// where your node app starts

// init project
const express = require('express')
const app = express()
const url = require("url")

// we've started you off with Express, 
// but feel free to use whatever libs or frameworks you'd like through `package.json`.

// http://expressjs.com/en/starter/static-files.html
app.use(express.static('public'))

// http://expressjs.com/en/starter/basic-routing.html
app.get("/", (request, response) => {
  response.sendFile(__dirname + '/views/index.html')
})


app.get("/docs",(requers, response)=> {
  response.sendFile(__dirname + '/views/docs.html')
})

// Simple in-memory store
//Solange wir keine Datenbank anbindung haben
const gags = [
  {
    quelle: "Neo Magazin Royale",
    datum: "15.03.2018",
    text: "Gestern sprachen die neuen Ministerinnen und Minister bei Bundespräsident... Ähm wie heißt er noch? Frank? Frank? Frank? Frank Walter Steinmeier! Bundespräsident Frank Walter Steinmeier. In seinem Amtssitz die feierliche Vereidigungsformel 'Vou Lez Vouz Bellevue avec mois? Mocca Chocolata ja ja Kriölä mamelä'"
  },
  {
    quelle: "Neo Magazin Roylae",
    datum: "15.03.2018",
    text: "Die GroKo, die Kolaition der Träume hat gestern ihre Arbeit aufgenommen mit vielen neuen Ministern. Das heißt Bastelstress für Pegida. Bis zur nächsten Montagsdemo müssen neue Puppen für die Galgen her. Gibt's bestimmt auch schöne Galgenpuppenvorlagen bei Dawanda oder sowas. Da kann man ja mal gucken."
  },
  {
    quelle: "Neo Magazin Roylae",
    datum: "15.03.2018",
    text: "Unser Mann in der Bundesregierung Neo Magazin Royale-Minister hat auch schon eingecheckt auf der GroKo-Überholspur. Er hat gesagt: 'Hartz-!V reicht doch völlig aus zum Leben.' Ja klar! Zur not sollen die Leute halt den Koalitionsvertrag essen. Hm Schwarz weiß, nach was schmeckt der? Stracciatella?Reicht Hartz IV wirklich aus? Vielleicht sollte Jens Spahn mal bei Martin Schulz anrufen und Fragen, wie es ihm so geht."  
  }

]


app.get("/api", (request, response) => {
  let urlSended = url.parse(request.url, true)
  let query = urlSended.query
  
  if(query.searchType == "random") {
    console.log(Math.floor(Math.random()*(gags.length-1)))
      response.send(gags[Math.floor(Math.random()*(gags.length-1))])
  } else {
    response.send("Sorry so weit sind wir leider noch nicht")
  }
  
                
  
  
                  
})

// could also use the POST body instead of query string: http://expressjs.com/en/api.html#req.body


// listen for requests :)
const listener = app.listen(process.env.PORT, () => {
  console.log(`Your app is listening on port ${listener.address().port}`)
})
