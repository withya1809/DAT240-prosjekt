const client = new SocketConnector();

/**
 * @namespace welcomePage
 */

/**
 * Add listeners for what role the user has selected and redirect to the respective page
 * @memberOf welcomePage
 */
client.onConnect = () => {
    client.send(makeMessage(userId, "JOIN"), `/app/update/${userId}/registerUserUpdates`);
    client.subscribe(`/channel/update/${userId}`, data => {
        if (data.type === "JOIN_PARTY") {
            let role = data.content.role.toLowerCase();
            if (role === "proposer") {
                role = "proposerImageSelection";
            }

            let url = "http://localhost:8080/" + role +
                "?username=" + username + "&partyId=" + data.content.partyId;
            if (role === "guesser") {
                let form = document.getElementById("guesserForm");
                let partyIdInput = document.getElementById("partyIdInput");
                partyIdInput.value = data.content.partyId;
                form.submit();
            }

            window.location.replace(url);
        }
    });
};

/**
 * Create a message with the correct format
 * @param content
 * @param type
 * @returns {{sender: *, type: *, content: *}}
 * @author Alan Rostem
 * @memberOf welcomePage
 */
function makeMessage(content, type = "MSG") {
    return {
        content: content,
        sender: username,
        type: type
    };
}


/**
 * Send data to the back about the selected role
 * @author Grégoire Guillien
 * @memberOf welcomePage
 */
function sendPartyParameters() {
    console.log("sendPartyParameters used");
    client.send(makeMessage($("#dropOperator").val()), "/app/party/queueUp");
}

/**
 * Display the searching loader wheel
 * @author Grégoire Guillien
 * @memberOf welcomePage
 */
function researchParty() {
    $("#loader")[0].style = "visibility:;";
    $("#search")[0].style = "visibility: hidden";
    $("#searching-text")[0].style = "visibility:; text-align: center;";
}

/**
 * Control variable given by the back to check if the player is already active
 * @memberOf welcomePage
 * @type {boolean}
 * @author Alan Rostem
 */
let searching = false;

/**
 * Click event management
 * @author Grégoire Guillien
 * @memberOf welcomePage
 */
$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#search").click(function () {
        if (!searching) {
            sendPartyParameters();
            researchParty();
            searching = true;
        }
    });
    $("#dropOperator").val("GUESSER");
});