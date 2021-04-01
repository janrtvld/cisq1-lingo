package nl.hu.cisq1.lingo.trainer.presentation;

import com.jayway.jsonpath.JsonPath;
import nl.hu.cisq1.lingo.CiTestConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This test is a controller integration test as it
 * integrates between all layers and the framework.
 * In a dev environment, we test against the actual database.
 *
 * In continuous integration pipelines, we should not
 * use the actual database as we don't have one.
 * We want to replace it with an in-memory database.
 *
 * Set the profile to CI, so that application-ci.properties is loaded
 * and an import script is run.
 */
@SpringBootTest
@Import(CiTestConfiguration.class)
@AutoConfigureMockMvc
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("start a new game")
    void startNewGame() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/start");

        mockMvc.perform(request)
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
        RequestBuilder newGameRequest = MockMvcRequestBuilders
                .post("/lingo/start");

        MockHttpServletResponse response = mockMvc.perform(newGameRequest).andReturn().getResponse();
        Integer gameId = JsonPath.read(response.getContentAsString(), "$.id");

        String attempt = "LOSER";

        RequestBuilder guessRequest = MockMvcRequestBuilders
                .post("/lingo/" + gameId + "/guess")
                .param("attempt", attempt);

        mockMvc.perform(guessRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.feedbackHistory", hasSize(1)));

    }

}
