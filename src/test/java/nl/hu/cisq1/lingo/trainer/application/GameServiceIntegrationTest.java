package nl.hu.cisq1.lingo.trainer.application;

import nl.hu.cisq1.lingo.CiTestConfiguration;
import nl.hu.cisq1.lingo.trainer.data.SpringGameRepository;
import nl.hu.cisq1.lingo.trainer.domain.Game;
import nl.hu.cisq1.lingo.trainer.domain.GameStatus;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameStateException;
import nl.hu.cisq1.lingo.trainer.presentation.dto.ProgressDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        ProgressDTO progress = this.service.startGame();
        Long id = progress.getId();

        assertThrows(GameStateException.class, () -> this.service.startNewRound(id));
    }

    @Test
    @DisplayName("cannot start new round when player is eliminated")
    void cannotStartNewRoundWhenPlayerEliminated() {
        ProgressDTO progress = this.service.startGame();
        Long id = progress.getId();

        this.service.guess(id,"L");
        this.service.guess(id,"L");
        this.service.guess(id,"L");
        this.service.guess(id,"L");
        this.service.guess(id,"L");

        assertThrows(GameStateException.class, () -> this.service.startNewRound(id));
    }

    @Test
    @DisplayName("playing an attempt returns newly created feedback in progress")
    void guessIsPlayed() {
        ProgressDTO progress = this.service.startGame();
        Long id = progress.getId();

        ProgressDTO actual = this.service.guess(id,"PIZZA");

        assertEquals( 1, actual.getFeedbackHistory().size());
    }

    @Test
    @DisplayName("cannot play guess if player has been eliminated")
    void cannotGuessIfPlayerIsEliminated() {
        ProgressDTO progress = this.service.startGame();
        Long id = progress.getId();

        this.service.guess(id,"L");
        this.service.guess(id,"L");
        this.service.guess(id,"L");
        this.service.guess(id,"L");
        this.service.guess(id,"L");

        assertThrows(GameStateException.class, () -> this.service.guess(id,"L"));
    }

    @ParameterizedTest
    @DisplayName("getting progress of game returns the current state of the game")
    @MethodSource("randomGameExamples")
    void getProgressReturnsCurrentGameState(Game game, ProgressDTO progress) {
        assertEquals(progress.getFeedbackHistory(), game.getLatestRound().getFeedbackHistory());
        assertEquals(progress.getCurrentHint(), game.getLatestRound().giveHint());
        assertEquals(progress.getScore(), game.getScore());
        assertEquals(progress.getId(), game.getId());
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
                Arguments.of(gameWithWordGuessed, gameWithWordGuessedProgress),
                Arguments.of(gameWithPlayerEliminated, gameWithPlayerEliminatedProgress)
        );
    }

    private static ProgressDTO convertGameToProgressDTO(Game game) {
        return new ProgressDTO.Builder(game.getId())
                .score(game.getScore())
                .currentHint(game.getLatestRound().giveHint())
                .feedbackHistory(game.getLatestRound().getFeedbackHistory())
                .build();
    }

    @ParameterizedTest
    @DisplayName("next word length is based on current word to guess")
    @MethodSource("wordLengthExamples")
    void nextWordLength(String wordToGuess, int nextLength) {
        Game game = new Game();
        game.startNewRound(wordToGuess);
        assertEquals(nextLength, game.provideNextWordLength());
    }

    static Stream<Arguments> wordLengthExamples() {
        return Stream.of(
                Arguments.of("baard", 6),
                Arguments.of("bergen", 7),
                Arguments.of("baarden", 5),
                Arguments.of("bord", 5)
        );
    }

}








