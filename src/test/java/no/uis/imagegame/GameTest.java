package no.uis.imagegame;

import static org.junit.Assert.*;

import org.junit.Test;

import no.uis.players.Player;


public class GameTest {
	
	/**
	 * Testing init of new game
	 * @author Eirik & Markus
	 */
	@Test
	public void newGame_instantiate() {
		Player testPlayer1 = new Player("Josephine", "PROPOSER");
		Player testPlayer2 = new Player("Bernard", "GUESSER");
		GameLogic testGame = new GameLogic();
		assertEquals(true, testPlayer1 instanceof Player);
		assertEquals(true, testPlayer2 instanceof Player);
		assertEquals(true, testGame instanceof GameLogic);
	}
}




