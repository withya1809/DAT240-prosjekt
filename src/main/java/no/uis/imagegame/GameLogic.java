package no.uis.imagegame;

import no.uis.party.Party;
import no.uis.players.Player;
import no.uis.websocket.SocketMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Defining the basic game logic
 *
 * @author Eirik & Markus
 */
public class GameLogic {
    private static final int MAX_GUESSES = 3;
    private static final int LOSS_INTERVAL = 10;
    private static final int POINT_PENALTY = 10;

    private String image;
    private ArrayList<String> proposerSegments=new ArrayList<>();
    private ArrayList<String> guesserSegments=new ArrayList<>();

    private Player proposer;
    private Player guesser;
    private GameState currentState = GameState.WAITING_TO_START;
    private boolean finished = false;
    private int time = 0;
    private int currentGuesses = 0;
    private int remainingPoints = 1000;

    /**
     * Retrieve the image label selected by the proposer at the start of the game.
     *
     * @return String - Image label name
     */
    public String getImageName() {
        return image;
    }

    /**
     * Enumerator indicating the current status of the game.
     */
    public enum GameState {
        WAITING_TO_START,
        PLAYING,
        WIN,
        LOST
    }

    private HashMap<String, GameResponseFunction> responseMapping = new HashMap<>();

    @FunctionalInterface
    public interface GameResponseFunction {
        void apply(Party party, SocketMessage message);
    }

    public GameLogic() {
        responseMapping.put("SEND_SEGMENT", (party, message) -> {
            guesser.setPlayerStatus(Player.PlayerStatus.PLAYING);
            proposer.setPlayerStatus(Player.PlayerStatus.WAITING);
            chooseSegment((String) message.contentToMap().get("segment"));
            time = 0;
        });

        responseMapping.put("SEND_GUESS", (party, message) -> {
            if (checkAnswer((String) message.contentToMap().get("guess"))) {
                guesser.setPlayerStatus(Player.PlayerStatus.FINISHED);
                proposer.setPlayerStatus(Player.PlayerStatus.FINISHED);
                currentState = GameState.WIN;
                finished = true;
            }
        });

        responseMapping.put("REQUEST_SEGMENT", (party, message) -> giveUp());
    
        
    }

    /**
     * Called when the party is ready to enter into Party.PartyStatus.READY_TO_PLAY
     *
     * @param guesser  Player
     * @param proposer Player
     * @see Party.PartyStatus
     */
    public void addPlayers(Player guesser, Player proposer) {
        this.guesser = guesser;
        this.proposer = proposer;
    }

    /**
     * Sets the current image label name to store the right answer and maximum image segments.
     *
     * @param image      String - Image label
     * @param imageCount Integer - Image segment count
     */
    public void setImage(String image, int imageCount) {
        this.image = image;
        guesserSegments = new ArrayList<>();
        proposerSegments = new ArrayList<>();
        for (int i = 0; i < imageCount; ++i) {
            proposerSegments.add(Integer.toString(i));
        }
        System.out.println("Segment count for " + image + ": " + proposerSegments.size());
        currentState = GameState.PLAYING;
    }

    /**
     * Sets each player's status to indicate that a new round started and resets current guesses for this round to zero.
     *
     * @author Eirik & Markus
     */
    public void nextRound() {
        currentGuesses = 0;
        guesser.setPlayerStatus(Player.PlayerStatus.WAITING);
        proposer.setPlayerStatus(Player.PlayerStatus.PLAYING);
    }

    /**
     * Checks if the answer is correct. If the guesser guessed more than three times
     * the round ends. The game ends if the answer is correct.
     *
     * @author Eirik & Markus
     */
    public boolean checkAnswer(String guess) {
        currentGuesses++;
        if (currentGuesses == MAX_GUESSES) {
            nextRound();
        }
        return guess.equals(this.image);
    }

    /**
     * Starts the next round with a point penalty of five.
     *
     * @author Eirik & Markus
     */
    public void giveUp() {
        nextRound();
        remainingPoints -= POINT_PENALTY;
    }

    /**
     * Chooses new segment for new round
     *
     * @param segmentID String - ID of the new segment chosen by the proposer
     * @author Eirik & Markus
     */
    public void chooseSegment(String segmentID) {
        if (!guesserSegments.contains(segmentID)) {
            guesserSegments.add(segmentID);
        }
    }

    /**
     * Performs game logic when a player in the party has performed a game action.
     *
     * @param party   Party - The party of players this game belongs to
     * @param message SocketMessage - The input sent by each user
     */
    public void receiveUpdatesFromFront(Party party, SocketMessage message) {
        if (currentState == GameState.PLAYING) {
        	if (responseMapping.containsKey(message.getType())) {
        		responseMapping.get(message.getType()).apply(party, message);        		
        	}
        }
    }

    /**
     * Sequential update that changes the state of the game based on the game status and increments the game time.
     * Execution is performed sequentially by a TickExecution object.
     * @see no.uis.tools.TickExecution
     */
    public void update() {
        if (currentState == GameState.PLAYING) {
            if (proposer.getPlayerStatus() != Player.PlayerStatus.PLAYING) {
                time++;
            }

            if (time % LOSS_INTERVAL == 0) {
                remainingPoints--;
            }

            if (guesserSegments.size() == proposerSegments.size() || remainingPoints == 0) {
                currentState = GameState.LOST;
                guesser.setPlayerStatus(Player.PlayerStatus.FINISHED);
                proposer.setPlayerStatus(Player.PlayerStatus.FINISHED);
                finished = true;
            }
        }
    }

    /**
     * Calculates the score based on time, chosen segments and segment request penalties and returns it
     *
     * @return Integer - The calculated score
     * @author Eirik & Markus
     */
    public int getScore() {
        return (int) ((1f - (float) guesserSegments.size() / (float) proposerSegments.size()) * remainingPoints);
    }

    public int getTime() {
        return time;
    }

    /**
     * Retrieve a list of all the segments visible to the guesser.
     *
     * @return Array list of the segments
     */
    public ArrayList<String> getGuesserSegments() {
        return guesserSegments;
    }

    
    public ArrayList<String> getProposerSegments() {
        return proposerSegments;
    }
    
    /**
     * Retrieve the current state of the game.
     *
     * @see GameLogic.GameState
     */
    public GameState getCurrentState() {
        return currentState;
    }

    /**
     * Return a boolean indicating if the game is finished
     */
    public boolean isFinished() {
        return finished;
    }
}
