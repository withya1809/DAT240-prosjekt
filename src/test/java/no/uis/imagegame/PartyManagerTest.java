package no.uis.imagegame;

import no.uis.party.PartyManager;
import no.uis.players.Player;
import org.junit.Test;
import static org.junit.Assert.*;

public class PartyManagerTest {

    @Test
    public void createPartyTest() {
        PartyManager partyManager = new PartyManager();
        Player guesser = new Player("Test", Player.PlayerType.GUESSER);

        assertFalse(partyManager.isThereAnOpenParty());

        partyManager.queueUpPlayer(guesser);
        partyManager.update(null);

        assertTrue(partyManager.isThereAnOpenParty());

    }

    @Test
    public void queuePlayersTest() {
        PartyManager partyManager = new PartyManager();
        Player guesser = new Player("Test", Player.PlayerType.GUESSER);

        Player proposer = new Player("Test", Player.PlayerType.PROPOSER);


        partyManager.queueUpPlayer(guesser);
        assertTrue(partyManager.isQueueNotEmpty(Player.PlayerType.GUESSER));
        partyManager.update(null);

        partyManager.queueUpPlayer(proposer);
        assertTrue(partyManager.isQueueNotEmpty(Player.PlayerType.PROPOSER));
        partyManager.update(null);

        assertEquals(1, partyManager.getPartyCount());

    }
}
