var stompClient = null;
connect();

function connect() {
	console.log("connection websocket");
    var socket = new SockJS('/gs-guide-websocket-game');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/game/notif', function (notif) {
        	console.log("callback called");
        });
    });
}

function disconnect() {
	console.log("disconnect");
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

function sendNotif(message){
	console.log("game / notif appelé");
	stompClient.send("/app/notif", {}, JSON.stringify({'name': $("#name").val()}));
	console.log(message);
}

$(function () {
    $("form").on('submit', function (e) {
    	console.log(e);
   //     e.preventDefault();
    });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#endPlayRound" ).click(function() { sendNotif(); });
});