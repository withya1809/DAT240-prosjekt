package no.uis.repositories;

import no.uis.players.User;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository storing all the user data such as username and password using User objects.
 * @see User
 * @author Alan Rostem
 */
public interface PlayerRepository extends CrudRepository<User, String> {
    User findByUsername(String username);
    User findById(Long id);
}