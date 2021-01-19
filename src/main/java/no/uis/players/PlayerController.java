package no.uis.players;

import no.uis.repositories.PlayerRepository;
import no.uis.repositories.ScoreBoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import no.uis.players.Player.PlayerType;

import java.util.*;

/**
 * Main view controller. Handles login and registering.
 */
@Controller
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * Main page.
     *
     * @author Alan Rostem
     */
    @RequestMapping("/")
    public String home() {
        return "loginPage";
    }

    /**
     * Redirection when registering. Validates the user input and acts upon it.
     *
     * @param model Model with attributes used to send data to the view and parse with Thymeleaf
     * @param reg_username    Username input
     * @param reg_password    Password input
     * @param reg_confirmpass Password confirmation input
     * @author Alan Rostem
     */
    @RequestMapping("reg-post")
    public String register(Model model,
                           @RequestParam(value = "reg_username") String reg_username,
                           @RequestParam(value = "reg_password") String reg_password,
                           @RequestParam(value = "reg_confirmpass") String reg_confirmpass
    ) {
        if (!userExists(reg_username)) {
            if (reg_confirmpass.equals(reg_password)) {
                User player = createUser(reg_username, reg_password);
                return "redirect:welcomePage?username=" + reg_username + "&id=" + player.getId();
            }
            model.addAttribute("invalid_message", "Please confirm your password.");
            return "loginPage";
        }
        model.addAttribute("invalid_message", "This username already exists!");
        return "loginPage";
    }

    /**
     * Redirection when logging in. Validates user input and acts upon it.
     *
     * @param model Model with attributes used to send data to the view and parse with Thymeleaf
     * @param login_username Username input
     * @param login_password Password input
     */
    @RequestMapping("login-post")
    public String login(Model model,
                        @RequestParam(value = "login_username") String login_username,
                        @RequestParam(value = "login_password") String login_password) {
        if (userExists(login_username)) {
            User player = playerRepository.findByUsername(login_username);
            if (player.getPassword().equals(login_password)) {
                model.addAttribute("username", player.getUsername());
                return "redirect:welcomePage?username=" + login_username + "&id=" + player.getId();
            }
        }
        model.addAttribute("invalid_message", "Username or password is incorrect!");
        return "loginPage";
    }

    /**
     * Add a new user to the repository (database) and return it.
     * @param username Username input
     * @param password Password input
     * @author Alan Rostem
     */
    private User createUser(String username, String password) {
        User user = new User(username, password);
        System.out.println(user);
        playerRepository.save(user);
        System.out.println("Created a new user: " + user);
        return user;
    }

    /**
     * Boolean check if a user exists. Used upon registration input validation
     * @param username Username input
     */
    private boolean userExists(String username) {
        return playerRepository.findByUsername(username) != null;
    }
}
