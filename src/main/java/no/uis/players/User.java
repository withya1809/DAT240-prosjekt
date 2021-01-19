package no.uis.players;

import javax.persistence.*;
import java.util.Random;

/**
 * Data structure for user data storage in the repository (which is then redirected to the database)
 *
 * @author Alan Rostem & Markus
 */
@Entity
@Table(name = "Players")
public class User {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id = "" + Math.abs(new Random().nextLong());

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    /**
     * Construct the user data structure
     *
     * @param username Username
     * @param password Password
     * @author Alan Rostem
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User() {
    }

    /**
     * @return Username string
     * @author Alan Rostem
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return User ID string
     */
    public String getId() {
        return id;
    }

    /**
     * @return User password string
     */
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User [name=" + username + ", id=" + id + "]";
    }
}
