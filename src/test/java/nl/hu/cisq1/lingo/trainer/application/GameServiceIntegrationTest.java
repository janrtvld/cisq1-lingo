package nl.hu.cisq1.lingo.trainer.application;

import nl.hu.cisq1.lingo.CiTestConfiguration;
import nl.hu.cisq1.lingo.trainer.data.SpringGameRepository;
import nl.hu.cisq1.lingo.trainer.domain.Game;
import nl.hu.cisq1.lingo.trainer.domain.GameStatus;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameStateException;
import nl.hu.cisq1.lingo.trainer.application.dto.ProgressDTO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


/**
 * This integration test integrates between the service layer,
 * the data layer and the framework.
 * In a dev environment, we test against the actual database.
 *
 * In continuous integration pipelines, we should not
 * use the actual database as we don't have one.
 * We want to replace it with an in-memory database.
 *
 * Set the profile to CI, so that application-ci.properties is loaded
 * and an import script is run.
 **/
@SpringBootTest
@Import(CiTestConfiguration.class)
class GameServiceIntegrationTest {

    @Autowired
    private GameService service;

    @Autowired
    private SpringGameRepository repository;

    private Game game;

    @BeforeEach
    @DisplayName("initiate game for test")
    void beforeEachTest() {
        this.repository.deleteAll();

        this.game = new Game();
        game.startNewRound("BAARD");

        this.repository.save(game);
    }

    @AfterEach
    @DisplayName("clean up after test")
    void afterEachTest() {
        this.repository.deleteAll();
    }

    @Test
    @DisplayName("starting a game starts a new round")
    void startGameCreatesNewGame() {
        ProgressDTO progress = this.service.startGame();

        assertEquals( GameStatus.PLAYING.getStatus(), progress.getGameStatus());
        assertEquals( 0, progress.getScore());
        assertEquals( 5, progress.getCurrentHint().length());
        assertEquals( 0, progress.getFeedbackHistory().size());
    }

    @Test
    @DisplayName("cannot start a new round when still playing")
    void cannotStartNewRoundWhenPlaying() {
        Long id = game.getId();
        assertThrows(GameStateException.class, () -> this.service.startNewRound(id));
    }

    @Test
    @DisplayName("cannot start new round when player is eliminated")
    void cannotStartNewRoundWhenPlayerEliminated() {
        Long id = game.getId();
        this.service.guess(id,"B");
        this.service.guess(id,"B");
        this.service.guess(id,"B");
        this.service.guess(id,"B");
        this.service.guess(id,"B");

        assertThrows(GameStateException.class, () -> this.service.startNewRound(id));
    }

    @Test
    @DisplayName("playing an attempt returns newly created feedback in progress")
    void guessIsPlayed() {
        ProgressDTO actual = this.service.guess(game.getId(),"BAARS");

        String expectedHint = "BAAR.";

        assertEquals( 1, actual.getFeedbackHistory().size());
        assertEquals( expectedHint, actual.getCurrentHint());
    }

    @Test
    @DisplayName("cannot play guess if player has been eliminated")
    void cannotGuessIfPlayerIsEliminated() {
        Long id = game.getId();
        this.service.guess(id,"L");
        this.service.guess(id,"L");
        this.service.guess(id,"L");
        this.service.guess(id,"L");
        this.service.guess(id,"L");

        assertThrows(GameStateException.class, () -> this.service.guess(id,"BAARD"));
    }

    @ParameterizedTest
    @DisplayName("getting progress of game returns the current state of the game")
    @MethodSource("randomGameExamples")
    void getProgressReturnsCurrentGameState(Game game, ProgressDTO progress) {
        this.repository.deleteAll();
        this.repository.save(game);

        ProgressDTO actual = this.service.getProgress(game.getId());

        assertEquals(progress.getGameStatus(),actual.getGameStatus());
    }

    static Stream<Arguments> randomGameExamples() {
        Game gameWithPlayingRound = new Game();
        gameWithPlayingRound.startNewRound("tower");
        ProgressDTO gameWithPlayingRoundProgress = convertGameToProgressDTO(gameWithPlayingRound);

        Game gameWithWordGuessed = new Game();
        gameWithWordGuessed.startNewRound("tower");
        gameWithWordGuessed.guess("tower");
        ProgressDTO gameWithWordGuessedProgress = convertGameToProgressDTO(gameWithWordGuessed);

        Game gameWithPlayerEliminated = new Game();
        gameWithPlayerEliminated.startNewRound("tower");
        gameWithPlayerEliminated.guess("stupid");
        gameWithPlayerEliminated.guess("stupid");
        gameWithPlayerEliminated.guess("stupid");
        gameWithPlayerEliminated.guess("stupid");
        gameWithPlayerEliminated.guess("stupid");
        ProgressDTO gameWithPlayerEliminatedProgress = convertGameToProgressDTO(gameWithPlayerEliminated);

        return Stream.of(
                Arguments.of(gameWithPlayingRound, gameWithPlayingRoundProgress),
                Arguments.of(gameWithWordGuessed,gameWithWordGuessedProgress),
                Arguments.of(gameWithPlayerEliminated,gameWithPlayerEliminatedProgress)
        );
    }

    @ParameterizedTest
    @DisplayName("next word length is based on previous round")
    @MethodSource("wordLengthExamples")
    void nextWordLength(Game game, int nextLength) {
        this.repository.save(game);

        ProgressDTO progress = this.service.startNewRound(game.getId());

        assertEquals(nextLength, progress.getCurrentHint().length());
    }

    static Stream<Arguments> wordLengthExamples() {
        Game gameWithFiveLetterWord = new Game();
        gameWithFiveLetterWord.startNewRound("BAARD");
        gameWithFiveLetterWord.guess("BAARD");

        Game gameWithSixLetterWord = new Game();
        gameWithSixLetterWord.startNewRound("BERGEN");
        gameWithSixLetterWord.guess("BERGEN");

        Game gameWithSevenLetterWord = new Game();
        gameWithSevenLetterWord.startNewRound("BAARDEN");
        gameWithSevenLetterWord.guess("BAARDEN");

        return Stream.of(
                Arguments.of(gameWithFiveLetterWord, 6),
                Arguments.of(gameWithSixLetterWord, 7),
                Arguments.of(gameWithSevenLetterWord, 5)
        );
    }

    private static ProgressDTO convertGameToProgressDTO(Game game) {
        return new ProgressDTO.Builder(game.getId())
                .gameStatus(game.getGameStatus().getStatus())
                .score(game.getScore())
                .currentHint(game.getLatestRound().giveHint())
                .feedbackHistory(game.getLatestRound().getFeedbackHistory())
                .build();
    }
}








