package no.uis.party;

import no.uis.players.Player;
import no.uis.repositories.ScoreBoardRepository;
import no.uis.websocket.SocketMessage;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import static no.uis.players.Player.PlayerType.*;
import static no.uis.party.Party.PartyStatus.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that manages what party a player goes into depending on the role they select
 *
 * @author Alan Rostem
 */
public class PartyManager {
    static private ArrayDeque<Player> proposerQueue = new ArrayDeque<>();
    static private ArrayDeque<Player> guesserQueue = new ArrayDeque<>();
    static private HashMap<String, Party> parties = new HashMap<>();

    static private HashMap<String, Player> activePlayers = new HashMap<>();

    static private Party currentOpenParty;
    static private Player currentlyWaitingProposer;
    static private Player currentlyWaitingGuesser;

    static public boolean isPlayerActive(String username) {
        return activePlayers.containsKey(username);
    }

    /**
     * Creates a new party and pushes them into the array list where we update them in the future.
     *
     * @author Alan Rostem
     */
    static private void openParty() {
        currentOpenParty = new Party();
    }

    /**
     * Retrieve a party by its ID
     *
     * @param id Party ID
     * @author Alan Rostem
     */
    static public Party getParty(String id) {
        return parties.get(id);
    }

    /**
     * Returns true if an open party exists
     *
     * @return boolean
     * @author Alan Rostem
     */
    static public boolean isThereAnOpenParty() {
        return currentOpenParty != null;
    }

    /**
     * Method used to update the game sequentially. Party management core logic occurs here.
     * Execution is performed sequentially by a TickExecution object.
     *
     * @author Alan Rostem
     * @see no.uis.tools.TickExecution
     */
    static public void update(SimpMessageSendingOperations messagingTemplate, ScoreBoardRepository scoreBoardRepository) {
        if (!isThereAnOpenParty()) {
            openParty();
            System.out.println("Party opened: " + currentOpenParty.getId() + ". Parties created: " + parties.size());
        }
        // If both types of player are waiting, add them to the newly created party
        if (currentlyWaitingGuesser != null && currentlyWaitingProposer != null) {
            currentOpenParty.setGuesser(currentlyWaitingGuesser);
            currentOpenParty.setProposer(currentlyWaitingProposer);

            SocketMessage msg = new SocketMessage();
            msg.setSender("server");

            HashMap<String, Object> proposerContent = new HashMap<>();
            proposerContent.put("role", "PROPOSER");
            proposerContent.put("partyId", "" + currentOpenParty.getId());
            proposerContent.put("otherPlayerName", currentlyWaitingGuesser.getUsername());
            msg.setContent(proposerContent);

            msg.setType("JOIN_PARTY");
            currentlyWaitingProposer.sendData(msg, messagingTemplate);

            currentlyWaitingGuesser.setGameStatus(Player.GameStatus.PLAYING);
            currentlyWaitingProposer.setGameStatus(Player.GameStatus.PLAYING);

            currentlyWaitingGuesser.setPlayerStatus(Player.PlayerStatus.WAITING);
            currentlyWaitingProposer.setPlayerStatus(Player.PlayerStatus.PLAYING);

            currentlyWaitingGuesser = null; // Guesser no longer waiting
            currentlyWaitingProposer = null; // Proposer no longer waiting

            currentOpenParty.setStatus(READY_TO_PLAY);
            parties.put(currentOpenParty.getId(), currentOpenParty);
            currentOpenParty = null; // Party is now closed
            System.out.println("Both users put into party. Next!");
        } else {
            // If one or the other waiting player is non-existent, take them
            // out of the queue and set them if the queues are not empty.
            if (isQueueNotEmpty(GUESSER)) {
                currentlyWaitingGuesser = guesserQueue.pop();
                System.out.println("The guesser " + currentlyWaitingGuesser.getUsername() + " has waited long enough!");
            }

            if (isQueueNotEmpty(PROPOSER)) {
                currentlyWaitingProposer = proposerQueue.pop();
                System.out.println("The proposer " + currentlyWaitingProposer.getUsername() + " has waited long enough!");
            }
        }
        // Update all parties and remove those that are finished
        for (Map.Entry<String, Party> pair : parties.entrySet()) {
            Party party = pair.getValue();
            party.update(messagingTemplate, scoreBoardRepository);
            if (party.getStatus() == FINISHED_GAME) {
                activePlayers.remove(party.getProposer().getUsername());
                activePlayers.remove(party.getGuesser().getUsername());
                System.out.println("Removed!" +
                        isPlayerActive(party.getProposer().getUsername()) + " " +
                        isPlayerActive(party.getGuesser().getUsername())
                );
                parties.remove(party.getId());
            }
        }
    }

    /**
     * Get the guesser waiting for a game
     *
     * @return Player
     * @author Alan Rostem
     */
    static public Player getCurrentlyWaitingGuesser() {
        return currentlyWaitingGuesser;
    }

    /**
     * Get the proposer waiting for a game
     *
     * @return Player
     * @author Alan Rostem
     */
    static public Player getCurrentlyWaitingProposer() {
        return currentlyWaitingProposer;
    }

    /**
     * Get the number of open/playing parties
     *
     * @return int
     */
    static public int getPartyCount() {
        return parties.size();
    }

    /**
     * Check if a queue of a player type is not empty
     *
     * @param type Player type
     * @return True if not empty
     * @author Alan Rostem
     * @see Player.PlayerType
     */
    static public boolean isQueueNotEmpty(Player.PlayerType type) {
        switch (type) {
            case PROPOSER:
                return proposerQueue.size() > 0;
            case GUESSER:
                return guesserQueue.size() > 0;
            default:
                return false;
        }
    }

    /**
     * Adds a player to the guesser queue if given PlayerType is GUESSER
     *
     * @param guesser Player with PlayerType GUESSER
     * @author Alan Rostem
     */
    static private void queueUpGuesser(Player guesser) {
        guesserQueue.add(guesser);
        System.out.println("We queued a guesser named " + guesser.getUsername() + "! Queue count: " + guesserQueue.size());
    }

    /**
     * Adds a player to the proposer queue if given PlayerType is PROPOSER
     *
     * @param proposer Player with PlayerType GUESSER
     * @author Alan Rostem
     */
    static private void queueUpProposer(Player proposer) {
        proposerQueue.add(proposer);
        System.out.println("We queued a proposer named " + proposer.getUsername() + "! Queue count: " + proposerQueue.size());
    }

    /**
     * Queues up a player for a party. The player is added into a queue respective to its PlayerType
     *
     * @param player Either a proposer or guesser depending on the PlayerType
     * @author Alan Rostem
     * @see no.uis.players.Player.PlayerType
     */
    static public void queueUpPlayer(Player player) {
        switch (player.getPlayerType()) {
            case PROPOSER:
                queueUpProposer(player);
                player.setGameStatus(Player.GameStatus.QUEUEING);
                activePlayers.put(player.getUsername(), player);
                break;
            case GUESSER:
                queueUpGuesser(player);
                player.setGameStatus(Player.GameStatus.QUEUEING);
                activePlayers.put(player.getUsername(), player);
                break;
        }
    }

    /**
     * Retrieve an active player (player that is queueing or playing) by its username
     *
     * @param username Name of the player
     * @author Alan Rostem
     */
    static public Player getActivePlayer(String username) {
        return activePlayers.get(username);
    }
}
