/**
 * @namespace game
 */

/**
 * Message type enum config
 * @type {Enum}
 * @memberOf game
 */
const MSG_TYPES = new Enum(
    "SEND_GUESS",
    "REQUEST_SEGMENT",
    "SEND_SEGMENT",
    "QUIT"
);

/**
 * Player game state enum config
 * @type {Enum}
 * @memberOf game
 */
const PLAYER_STATES = new Enum(
    "PLAYING",
    "WAITING",
    "FINISHED",
    "DECONNECTION"
);

/**
 * Player role enum config
 * @type {Enum}
 * @memberOf game
 */
const PLAYER_ROLES = new Enum(
    "PROPOSER",
    "GUESSER",
);

/**
 * Time variable that gets incremented on the front and updated every time we receive messages from the back
 * @type {number}
 * @memberOf game
 */
let time = 0;

/**
 * Control variable to stop the front end timer when the guesser is not playing
 * @type {boolean}
 * @memberOf game
 */
let stopTimer = false;