package no.uis.players;

import no.uis.websocket.SocketMessage;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

/**
 * Player object created when a user has logged in and queued for a game.
 *
 * @author Alan Rostem & Markus
 */
public class Player {
    private String id;

    private String username;

    private PlayerType type;
    private PlayerStatus status = PlayerStatus.WAITING;
    private GameStatus gameStatus = GameStatus.INACTIVE;

    /**
     * Enumerator to indicate the role of the player
     *
     * @author Alan Rostem
     */
    public enum PlayerType {
        GUESSER, PROPOSER
    }

    /**
     * Enumerator to indicate whose turn it is in a game
     *
     * @author Alan Rostem
     */
    public enum PlayerStatus {
        WAITING, PLAYING, FINISHED
    }

    /**
     * Enumerator to indicate the players status.
     *
     * @author Alan Rostem
     */
    public enum GameStatus {
        QUEUEING, PLAYING, INACTIVE
    }

    /**
     * Constructor for a player with identifiers
     *
     * @param id       ID of the user
     * @param username Name of the user
     * @author Alan Rostem
     * @see User
     */
    public Player(String id, String username) {
        this.username = username;
        this.id = id;
    }

    /**
     * Unit test constructor method
     *
     * @param username Name of the user
     * @param pType    Player role
     * @author Alan Rostem
     * @see no.uis.imagegame.PartyManagerTest
     */
    public Player(String username, PlayerType pType) {
        this.username = username;
        this.type = pType;
    }

    /**
     * Retrieve the ID of the user generated player object
     *
     * @author Alan Rostem
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieve the name of the user generated player object
     *
     * @author Alan Rostem
     */
    public String getUsername() {
        return username;
    }


    /**
     * Retrieve the role of the player
     *
     * @author Alan Rostem
     */
    public PlayerType getPlayerType() {
        return type;
    }

    /**
     * Set the role of the player
     *
     * @param playerType Player role
     * @author Alan Rostem
     */
    public void setPlayerType(PlayerType playerType) {
        this.type = playerType;
    }

    /**
     * Retrieve the player's status.
     * @author Alan Rostem
     */
    public PlayerStatus getPlayerStatus() {
        return status;
    }

    /**
     * Set the status of the player
     * @author Alan Rostem
     */
    public void setPlayerStatus(PlayerStatus status) {
        this.status = status;
    }

    /**
     * Retrieve the player's game activity status.
     * @author Alan Rostem
     */
    public GameStatus getGameStatus() {
        return gameStatus;
    }

    /**
     * Set the player's game activity status.
     * @author Alan Rostem
     */
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    /**
     * Send a socket message to the player on the front end
     * @param message Custom message
     * @param messageSendingOperations Autowired message operation object
     */
    public void sendData(SocketMessage message, SimpMessageSendingOperations messageSendingOperations) {
        messageSendingOperations.convertAndSend("/channel/update/" + getId(),
                message);
    }

    @Override
    public String toString() {
        return "Player [name=" + username + ", id=" + id + "]";
    }
}
