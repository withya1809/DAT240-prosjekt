package no.uis.repositories;

import no.uis.players.ScoreData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository storing all the score data such as player names and score count using ScoreData objects.
 * @see ScoreData
 * @author Alan Rostem
 */
@Repository
public interface ScoreBoardRepository extends CrudRepository<ScoreData, Long> {
    List<ScoreData> findAllByScoreAfter(Integer integer);
}