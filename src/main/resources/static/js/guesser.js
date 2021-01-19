/**
 * proposer.js
 * file managing the event on the proposer.html
 * @namespace guesser
 */

const client = new SocketConnector();

/*
 * Party state label
 * @memberOf guesser
 */
const msg = {
    msgMyTurn: "Make a guess!",
    msgNotMyTurn: "Wait for a new segment...",
    msgPartyFinished: "congrats! You win!",
    msgPartyLoose: "you lost, try again!",
    msgPartyDeco: "Deconnection! You can come back to the home page"
};

/**
 * Chance count label
 * @type {{"0": string, "1": string, "2": string, "3": string}}
 * @memberOf guesser
 */
const nbChance = {
    0: "",
    1: "Wrong again! Last chance!",
    2: "Wrong! 2 chances remaining",
    3: "You have 3 chances"
};

/**
 * Image segment ID received from the back used to show which segment
 * @type {number}
 * @memberOf guesser
 */
var imageId = 0;

/**
 * Index used to show which guess label for what amount of guesses left
 * @type {number}
 * @memberOf guesser
 */
var guessChance = 0;

client.onConnect = function () {
    route = `/app/party/${partyId}/addUser`;
    client.send({sender: "", type: 'JOIN'}, route);
};

/**
 * Player game state received from the back
 * @memberOf guesser
 */
var state = PLAYER_STATES.WAITING;

/**
 * Score display variable
 * @type {string}
 * @memberOf guesser
 */
var score = "0";

updateState();

/**
 * Send the guess to the back via the websocket channel
 * @param guess {string} - Given guess by the guesser
 * @memberOf guesser
 * @author Grégoire Guillien
 */
function sendGuess(guess) {
    console.log("send the guess via websocket----------");
    const message = {
        content: {
            guess: guess,
            role: PLAYER_ROLES.GUESSER,
            sender: userId
        },
        type: MSG_TYPES.SEND_GUESS
    };
    client.send(message, `/app/party/${partyId}/update`);
}

/**
 * Subscribe to the websocket
 * @param user_id - Id of the user
 * @param party_id - Id of the party
 * @memberOf guesser
 * @author Guillien Grégoire
 */
function subscribe(user_id, party_id) {
    userId = user_id;
    partyId = party_id;
    console.log(user_id);
    client.addStompListener(`/channel/update/${user_id}`, update);
    console.log("Subscribed ro the web-socket--------------------");
}


/**
 * Callback method for the /channel/update/{userId} destination subscription
 * @param msg - object or string
 * @author Guillien Grégoire
 * @memberOf guesser
 */
function update(msg) {
    console.log("Received a msg via web-socket---------------");
    if (msg.content.segment != null) {
        imageId = msg.content.segment;
    }
    state = msg.content.state;
    score = msg.content.score;
    time = Number(msg.content.time);
    if (msg.content.segments != null) {
        console.log("update segments");
        segments = msg.content.segments;
        updateSegments();
    }
    updateState();
    updateFeatures();
}

/**
 * Update all the selected segments received from the back
 * @memberOf guesser
 */
function updateSegments() {
    segments.forEach(function (element) {
        console.log(element);
        document.getElementById(element).style.visibility = "visible";
    });
}

/**
 * Update the page based on the state received from the back.
 * @author Guillien Grégoire
 * @memberOf guesser
 */
function updateState() {

    switch (state) {
        case PLAYER_STATES.PLAYING:
            stopTimer = false;
            $("#proposerPopUp").text(msg.msgMyTurn);
            $("#submitGuess")[0].disabled = false;
            $("#submitNewSegment")[0].disabled = false;
            document.getElementById("submitGuess").disabled = false;
            document.getElementById("submitNewSegment").disabled = false;
            guessChance = 3;
            $("#guessRemaning")[0].innerText = nbChance[guessChance];
            printImageSegment();
            break;

        case PLAYER_STATES.WAITING:
            stopTimer = true;
            $("#proposerPopUp").text(msg.msgNotMyTurn);
            $("#submitGuess").className = "myButton";
            $("#submitGuess")[0].disabled = true;
            $("#submitNewSegment")[0].disabled = true;
            $("#guessRemaning")[0].innerText = nbChance[guessChance];
            break;

        case PLAYER_STATES.FINISHED:
            stopTimer = true;
            $("#proposerPopUp").text(msg.msgPartyFinished);
            document.getElementById("submitGuess").disabled = true;
            document.getElementById("submitNewSegment").disabled = true;
            client.disconnect();
            $("#score").text(score);
            $("#guessRemaning")[0].innerText = nbChance[0];
            break;

        case "DECONNECTION":
            console.log("disconected");
            stopTimer = true;
            $("#proposerPopUp").text(msg.msgPartyDeco);
            document.getElementById("submitGuess").disabled = true;
            document.getElementById("submitNewSegment").disabled = true;
            client.disconnect();
            $("#score").text(score);
            $("#guessRemaning")[0].innerText = nbChance[0];
            break;
        default:
            break

    }
}

/**
 * Update score and timer on the page
 * @author Grégoire Guillien
 * @memberOf guesser
 */
function updateFeatures() {
    $("#time").text(time);
    $("#score").text(score);

}

/**
 * Set the image segment to visible
 * @author Grégoire Guillien
 * @memberOf guesser
 */
function printImageSegment() {
    document.getElementById(imageId).style.visibility = "visible";
}

/**
 * Manage the 'click' events
 * @author Grégoire Guillien
 * @memberOf guesser
 */
$(function () {
    $("#submitGuess").click(function () {
        submitGuess();
    });
    $("#submitNewSegment").click(function () {
        submitNewSegment();
    });
    $("#home").click(function () {
        home();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
});

/**
 * Send the guess from the input field to the back
 * @author Grégoire Guillien
 * @memberOf guesser
 */
function submitGuess() {
    var guess = $("#guess").val();
    imageId = guess;
    const message = {
        content: ({
            guess: guess,
            role: PLAYER_ROLES.GUESSER
        }),
        type: MSG_TYPES.SEND_GUESS
    };
    client.send(message, `/app/party/${partyId}/update`);
    guessChance--;
    console.log("send a msg via web-socket---------");
    $("#guessRemaning")[0].innerText = nbChance[guessChance];
    if (guessChance === 0) {
        state = PLAYER_STATES.WAITING;
        updateState();
    }
}

/**
 * Send a message to the back asking for a new segment
 * @author Grégoire Guillien
 * @memberOf guesser
 */
function submitNewSegment() {
    console.log("send a msg via web-socket---------");
    const message = {
        content: ({
            requestSegment: true,
            role: PLAYER_ROLES.GUESSER,
        }),
        type: MSG_TYPES.REQUEST_SEGMENT,
        sender: userId
    };
    client.send(message, `/app/party/${partyId}/update`);
    state = PLAYER_STATES.WAITING;
    guessChance = 0;
    updateState();
}

/**
 *send a surrend message to the back and return to the home page.
 * @author Guillien Grégoire
 * @memberOf guesser
 */
function home(){
	const message = {
	        content: {
	        	partyId: partyId,
	            role: PLAYER_ROLES.GUESSER,
	            sender: userId
	        },
	        type: MSG_TYPES.QUIT
	    };
	client.send(message, `/app/party/${partyId}/update`);
	let url = "http://localhost:8080/welcomePage" + "?username=" + username + "&id=" + userId;
	window.location.replace(url);
}

/**
 *send a surrend message to the back and return to the login page.
 * @author Guillien Grégoire
 * @memberOf guesser
 */
function disconnect(){
	const message = {
	        content: {
	        	partyId: partyId,
	            role: PLAYER_ROLES.GUESSER,
	            sender: userId
	        },
	        type: MSG_TYPES.QUIT
	    };
	client.send(message, `/app/party/${partyId}/update`);
	let url = "http://localhost:8080/";
	window.location.replace(url);

}