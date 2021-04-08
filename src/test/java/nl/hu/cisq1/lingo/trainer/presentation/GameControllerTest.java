package nl.hu.cisq1.lingo.trainer.presentation;

import com.jayway.jsonpath.JsonPath;
import nl.hu.cisq1.lingo.CiTestConfiguration;
import nl.hu.cisq1.lingo.trainer.domain.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasLength;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(CiTestConfiguration.class)
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private RequestBuilder startGameRequest;

    @BeforeEach
    void beforeEachTest() {
        this.startGameRequest = MockMvcRequestBuilders
                .post("/lingo/start");
    }

    @Test
    @DisplayName("start a new game")
    void startNewGame() throws Exception {
        mockMvc.perform(startGameRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.score", is(0)))
                .andExpect(jsonPath("$.gameStatus", is("PLAYING")))
                .andExpect(jsonPath("$.feedbackHistory", hasSize(0)))
                .andExpect(jsonPath("$.currentHint", hasLength(5)));
    }

    @Test
    @DisplayName("guess a word after starting a game")
    void guessWord() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(startGameRequest).andReturn().getResponse();
        Integer gameId = JsonPath.read(response.getContentAsString(), "$.id");

        String attempt = "X";

        RequestBuilder guessRequest = MockMvcRequestBuilders
                .post("/lingo/" + gameId + "/guess")
                .param("attempt", attempt);

        mockMvc.perform(guessRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.feedbackHistory", hasSize(1)));
    }

    @Test
    @DisplayName("get the progress of a playing game")
    void getGameProgress() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(startGameRequest).andReturn().getResponse();
        Integer gameId = JsonPath.read(response.getContentAsString(), "$.id");

        RequestBuilder progressRequest = MockMvcRequestBuilders
                .get("/lingo/" + gameId);

        mockMvc.perform(progressRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.score", is(0)))
                .andExpect(jsonPath("$.gameStatus", is("PLAYING")))
                .andExpect(jsonPath("$.feedbackHistory", hasSize(0)))
                .andExpect(jsonPath("$.currentHint", hasLength(5)));
    }

    @Test
    @DisplayName("getting a list of all games")
    void getListGames() throws Exception {
        mockMvc.perform(startGameRequest);

        RequestBuilder progressRequest = MockMvcRequestBuilders
                .get("/lingo/games");

        mockMvc.perform(progressRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

}
