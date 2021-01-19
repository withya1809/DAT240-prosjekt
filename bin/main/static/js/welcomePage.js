var stompClient = null;

function setConnected(connected) {
	console.log("setConnected")
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    $("#greetings").html("");
}

function connect() {
	console.log("connect")
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/game/lunch', function (lunch) {
            console.log("----------------------------------J'ai recu le call");
            console.log(lunch);
            console.log(lunch.toString());
            var link = lunch.body;
            console.log(link);
            $("#navigateur")[0].action = link;
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}



function sendPartyParameters(){
	console.log("sendPartyParameters used")
	stompClient.send("/app/welcomePage", {}, JSON.stringify({'selectedPlayModelabel': $("#dropOperator").val(), 'selectedPlayerModelabel': $("#dropOperator2").val()}));
}


$(function () {
    $("form").on('submit', function (e) {
//        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#search" ).click(function() { sendPartyParameters(); });
});