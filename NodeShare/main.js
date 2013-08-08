var WebSocketServer = require('ws').Server;
var Connect = require('connect');

var wss = new WebSocketServer({
    port: 8080
});

Connect.createServer(
    Connect.static(__dirname + "/public")
).listen(80);

wss.on('connection', function(ws) 
{
    console.log("Connected");

    if(typeof(ws.upgradeReq.headers["publisher"]) !== "undefined")
    {
        var message = {"publisher_key": ws.upgradeReq.headers["sec-websocket-key"]};
        ws.send(JSON.stringify(message));
    }

    ws.on('message', function(message) 
    {
        message = JSON.parse(message);

        console.log(message);

    	if(typeof(message["publisher_handshake"]) !== "undefined")
        {
            //TODO; check authentication
            ws.publisher = ws.upgradeReq.headers["publisher"];
            ws.send(JSON.stringify({
                publishing: true,
            }));
        }
        else
        {
            for(var i = 0; i < wss.clients.length; i++)
            {
                wss.clients[i].send(JSON.stringify(message));
            }
        }
    });
});