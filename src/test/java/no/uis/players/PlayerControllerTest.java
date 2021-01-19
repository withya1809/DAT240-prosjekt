
package no.uis.players;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import no.uis.imagegame.ImagegameApplication;
import no.uis.party.Party;
import no.uis.party.PartyManager;
import no.uis.party.QueueController;
import no.uis.players.Player;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = ImagegameApplication.class)
public class PlayerControllerTest {

	@Autowired
	private MockMvc mvc;
	
	
	/**
	 * testing GET index
	 * @throws Exception
	 * @author Eirik
	 */
	@Test
	public void getIndex() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()); //.andDo(print());
	}
	
	/**
	 * loginpage for user "test", user_id = 802543238333125092
	 * @throws exception
	 * @author Eirik
	 */
	@Test
	public void getLoginPage() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/welcomePage?username=test&id=802543238333125092")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()); //.andDo(print());
	}
	
	/**
	 * check scoreboard init at welcomePage with test user
	 * @throws Exception
	 * @author Eirik
	 * need to add elements with text for verifying  in welcomePage.html (?)
	 */
	@Test
	public void checkScoreBoard() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/welcomePage?username=test&id=802543238333125092").accept(MediaType.APPLICATION_JSON_UTF8))
		.andExpect(status().isOk()).andExpect(content().contentType("text/html;charset=UTF-8"))
		.andExpect(jsonPath("$.scoreBoard", hasSize(5))) // html name here
		.andDo(print());
	}
	
	/**
	 * check welcomePage without user
	 * @throws Exception
	 */
	@Test
	public void welcomePageWithoutUser() throws Exception {
		mvc.perform(get("/welcomepage")).andExpect(status().isNotFound());
	}		
}