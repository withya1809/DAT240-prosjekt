package no.uis.party;

import no.uis.imagegame.GameLogic;
import no.uis.players.Player;
import no.uis.players.ScoreData;
import no.uis.repositories.ScoreBoardRepository;
import no.uis.websocket.SocketMessage;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import java.util.HashMap;
import java.util.Random;


/**
 * Holds the game state data of the guesser and proposer.
 *
 * @author Alan Rostem
 */
public class Party {
    private String id = "" + Math.abs(new Random().nextLong());
    private Player guesser;
    private Player proposer;
    private PartyStatus currentStatus;
    private GameLogic game = new GameLogic();

    /**
     * Sequential update method that updates the game logic. Sends data to the players
     * and saves to the scoreboard when a game has finished. This is executed via a TickExecution
     *
     * @param smos Messaging operation object to send data via websocket
     * @param scoreBoardRepository The repository to store the score data
     * @see no.uis.tools.TickExecution
     * @author Alan Rostem
     */
    public void update(SimpMessageSendingOperations smos, ScoreBoardRepository scoreBoardRepository) {
        game.update();
        if (game.isFinished()) {
            SocketMessage finishedMsg = new SocketMessage();
            finishedMsg.setSender("server");
            HashMap<String, Object> content = new HashMap<>();
            finishedMsg.setContent(content);
            content.put("score", game.getScore());
            content.put("time", game.getTime());
            content.put("gameState", game.getCurrentState().toString());
            content.put("state", "FINISHED");

            getGuesser().sendData(finishedMsg, smos);
            getProposer().sendData(finishedMsg, smos);
            setStatus(PartyStatus.FINISHED_GAME);

            scoreBoardRepository.save(new ScoreData(
                    getId(),
                    getProposer().getUsername(),
                    getGuesser().getUsername(),
                    getGame().getScore(),
                    getGame().getImageName()
            ));
        }
    }

    /**
     * Retrieve the randomly generated party ID
     *
     * @author Alan Rostem
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieve the game logic object
     *
     * @author Alan Rostem
     * @see GameLogic
     */
    public GameLogic getGame() {
        return game;
    }

    /**
     * Party status enumerate class
     *
     * @author Alan Rostem
     */
    public enum PartyStatus {
        WAITING_FOR_PLAYERS,
        READY_TO_PLAY,
        PLAYING_GAME,
        FINISHED_GAME,
    }

    /**
     * Initializes a party with status WAITING_FOR_PLAYERS.
     *
     * @author Alan Rostem
     */
    public Party() {
        this.currentStatus = PartyStatus.WAITING_FOR_PLAYERS;
    }

    /**
     * Method called when the party state has been changed to READY_TO_PLAY.
     *
     * @author Alan Rostem
     */
    public void onReady() {
        game.addPlayers(guesser, proposer);
    }

    /**
     * @param message Message containing data sent by a player
     * @param smos    Messaging operation object to send data via websocket
     */
    public void receiveUpdateFromFront(SocketMessage message, SimpMessageSendingOperations smos) {
        SocketMessage sockMess = new SocketMessage();

        game.receiveUpdatesFromFront(this, message);

        HashMap<String, Object> content = new HashMap<>();
        sockMess.setContent(content);
        sockMess.setType(message.getType());

        content.put("score", game.getScore());
        content.put("time", game.getTime());
        content.put("gameState", game.getCurrentState().toString());

        switch (message.getType()) {
            case "REQUEST_SEGMENT":
                content.put("requestSegment", "true");
                content.put("state", getProposer().getPlayerStatus().toString());
                sockMess.setSender(getGuesser().getId());
                getProposer().sendData(sockMess, smos);
                break;
            case "SEND_SEGMENT":
                content.put("segment", message.contentToMap().get("segment"));
                content.put("state", getGuesser().getPlayerStatus().toString());
                sockMess.setSender(getProposer().getId());
                getGuesser().sendData(sockMess, smos);
                break;
            case "SEND_GUESS":
                content.put("guess", message.contentToMap().get("guess"));
                content.put("state", getProposer().getPlayerStatus().toString());
                sockMess.setSender(getGuesser().getId());
                getProposer().sendData(sockMess, smos);
                break;
            case "QUIT":
            	setStatus(PartyStatus.FINISHED_GAME);
            	content.put("state", "DECONNECTION");
            	getGuesser().sendData(sockMess, smos);
            	getProposer().sendData(sockMess, smos);
            default:
                System.out.println("Invalid message type received on party: " + getId());
        }
    }

    /**
     * Sets a proposer to the party.
     *
     * @param proposer Player
     * @author Alan Rostem
     */
    public void setProposer(Player proposer) {
        this.proposer = proposer;
    }

    /**
     * Sets a guesser to the party.
     *
     * @param guesser Player
     * @author Alan Rostem
     */
    public void setGuesser(Player guesser) {
        this.guesser = guesser;
    }

    /**
     * Sets a proposer to the party.
     *
     * @author Alan Rostem
     */
    public Player getProposer() {
        return this.proposer;
    }

    /**
     * Sets a guesser to the party.
     *
     * @author Alan Rostem
     */
    public Player getGuesser() {
        return (this.guesser);
    }

    /**
     * Retrieve the current state of the party.
     *
     * @return Party.PartyStatus
     * @author Alan Rostem
     */
    public PartyStatus getStatus() {
        return currentStatus;
    }

    /**
     * Set the current state of the party. Respective event methods are called upon setting.
     *
     * @param status Party.PartyStatus
     * @author Alan Rostem
     */
    public void setStatus(PartyStatus status) {
        currentStatus = status;
        if (status == PartyStatus.READY_TO_PLAY) {
            onReady();
        }
    }
}
