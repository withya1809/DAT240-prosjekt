package no.uis.players;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Data structure for score data storage in the repository (which is then redirected to the database)
 *
 * @author Alan Rostem
 */
@Entity
@Table(name = "ScoreBoard")
public class ScoreData {
    @Id
    @Column(name = "id")
    public String id;

    @Column(name = "proposername")
    public String proposerName;

    @Column(name = "guessername")
    public String guesserName;

    @Column(name = "score")
    public Integer score;

    @Column(name = "imagename")
    public String imagename;

    public ScoreData() {

    }

    /**
     * Construct the score data structure
     *
     * @param partyId      Party ID
     * @param proposerName Name of the proposer in the given game/party
     * @param guesserName  Name of the guesser in the given game/party
     * @param score        Score count in the given game/party
     * @param imagename    Name of the image label used in the given game/party
     * @author Alan Rostem
     */
    public ScoreData(String partyId, String proposerName, String guesserName, Integer score, String imagename) {
        this.id = partyId;
        this.proposerName = proposerName;
        this.guesserName = guesserName;
        this.score = score;
        this.imagename = imagename;
    }

    @Override
    public String toString() {
        return proposerName + " & " + guesserName + " cored " + score + " on image " + imagename + ".";
    }
}
