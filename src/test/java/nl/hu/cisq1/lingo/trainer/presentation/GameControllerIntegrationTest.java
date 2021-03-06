package nl.hu.cisq1.lingo.trainer.presentation;

import nl.hu.cisq1.lingo.CiTestConfiguration;
import nl.hu.cisq1.lingo.trainer.data.SpringGameRepository;
import nl.hu.cisq1.lingo.trainer.domain.Game;
import nl.hu.cisq1.lingo.words.data.SpringWordRepository;
import nl.hu.cisq1.lingo.words.domain.Word;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
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

    @MockBean
    private SpringWordRepository wordRepository;

    @Autowired
    private SpringGameRepository gameRepository;

    @Autowired
    private MockMvc mockMvc;

    private Game game;

    @BeforeEach
    @DisplayName("initiate game for test")
    void beforeEachTest() {
        this.gameRepository.deleteAll();

        this.game = new Game();
        game.startNewRound("BLOEM");

        this.gameRepository.save(game);
    }

    @AfterEach
    @DisplayName("clean up after test")
    void afterEachTest() {
        this.gameRepository.deleteAll();
    }

    @Test
    @DisplayName("start a new game")
    void startNewGame() throws Exception {
        when(wordRepository.findRandomWordByLength(5))
                .thenReturn(Optional.of(new Word("BAARD")));

        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/start");

        String expectedHint = "B....";

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(0)))
                .andExpect(jsonPath("$.gameStatus", is("PLAYING")))
                .andExpect(jsonPath("$.feedbackHistory", hasSize(0)))
                .andExpect(jsonPath("$.currentHint", hasLength(5)))
                .andExpect(jsonPath("$.currentHint", is(expectedHint)));
    }

    @Test
    @DisplayName("start a new round")
    void startNewRound() throws Exception {
        Long id = game.getId();
        game.guess("BLOEM");
        this.gameRepository.save(game);

        when(wordRepository.findRandomWordByLength(6))
                .thenReturn(Optional.of(new Word("HOEDEN")));

        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/"+ id + "/newRound");

        String expectedHint = "H.....";

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(25)))
                .andExpect(jsonPath("$.gameStatus", is("PLAYING")))
                .andExpect(jsonPath("$.feedbackHistory", hasSize(0)))
                .andExpect(jsonPath("$.currentHint", hasLength(6)))
                .andExpect(jsonPath("$.currentHint", is(expectedHint)));
    }

    @Test
    @DisplayName("cannot start new round if game not found")
    void cannotStartRound() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/1/newRound");

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("cannot start new round if still playing")
    void cannotStartRoundWhenPlaying() throws Exception {
        Long id = game.getId();

        when(wordRepository.findRandomWordByLength(6))
                .thenReturn(Optional.of(new Word("HOEDEN")));

        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/" + id + "/newRound");

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cannot start new round if player is eliminated")
    void cannotStartRoundWhenEliminated() throws Exception {
        Long id = game.getId();
        game.guess("boert");
        game.guess("boert");
        game.guess("boert");
        game.guess("boert");
        game.guess("boert");

        when(wordRepository.findRandomWordByLength(6))
                .thenReturn(Optional.of(new Word("HOEDEN")));

        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/" + id + "/newRound");

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("get progress of playing game")
    void getProgressOfGame() throws Exception {
        Long id = game.getId();
        game.guess("BLOEI");
        this.gameRepository.save(game);

        RequestBuilder request = MockMvcRequestBuilders
                .get("/lingo/" + id);

        String expectedHint = "BLOE.";

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(0)))
                .andExpect(jsonPath("$.gameStatus", is("PLAYING")))
                .andExpect(jsonPath("$.feedbackHistory", hasSize(1)))
                .andExpect(jsonPath("$.currentHint", hasLength(5)))
                .andExpect(jsonPath("$.currentHint", is(expectedHint)));
    }

    @Test
    @DisplayName("cannot get progress if game not found")
    void cannotGetProgress() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders
                .get("/lingo/1");

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("cannot guess if player is eliminated")
    void cannotGuessWhenEliminated() throws Exception {
        Long id = game.getId();
        game.guess("boert");
        game.guess("boert");
        game.guess("boert");
        game.guess("boert");
        game.guess("boert");
        this.gameRepository.save(game);

        String attempt = "LOSER";

        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/"+ id + "/guess")
                .param("attempt", attempt);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cannot guess if round ended")
    void cannotGuessWhenNoRound() throws Exception {
        Long id = game.getId();
        game.guess("BLOEM");
        this.gameRepository.save(game);

        String attempt = "LOSER";

        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/"+ id + "/guess")
                .param("attempt", attempt);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cannot guess if game is not found")
    void cannotGuessWhenNoGame() throws Exception {
        String attempt = "LOSER";

        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/1/guess")
                .param("attempt", attempt);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("guess provides new hint")
    void guessProvidesNewHint() throws Exception {
        Long id = game.getId();
        String attempt = "BLOEI";

        RequestBuilder request = MockMvcRequestBuilders
                .post("/lingo/"+ id + "/guess")
                .param("attempt", attempt);

        String expectedHint = "BLOE.";

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentHint", is(expectedHint)));
    }

    @Test
    @DisplayName("cannot get games if there are none")
    void cannotGetGamesIfNoGames() throws Exception {
        this.gameRepository.deleteAll();

        RequestBuilder request = MockMvcRequestBuilders
                .get("/lingo/games");

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("all games are provided")
    void allGamesAreProvided() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders
                .get("/lingo/games");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

}
