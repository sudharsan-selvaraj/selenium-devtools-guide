let express = require("express");
let path = require("path");

let app = express();

function renderHtml(fileName) {
    return function (req, res, next) {
        res.sendFile(path.resolve("public/pages/" + fileName + ".html"))
    }
}

app.use(express.static("public"));
app.get("/addScriptToEvaluateOnNewDocument", renderHtml("addScriptToEvaluateOnNewDocument"))
app.get("/", renderHtml("index"));

app.listen(3000);