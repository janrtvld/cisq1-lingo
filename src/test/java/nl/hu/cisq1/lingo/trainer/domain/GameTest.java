package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.GameStateException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoFeedbackFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;


class GameTest {

    private Game game;

    @BeforeEach
    @DisplayName("initiate game for tests")
    void init() {
        game = new Game();
    }

    @Test
    @DisplayName("new round can not be started when current round is still open")
    void cannotStartRoundWhenCurrentRoundIsOpen() {
        game.startNewRound("BAARD");

        // Act / Assert
        assertThrows(GameStateException.class, () -> {
            game.startNewRound("BAARD");
        });
    }

    @Test
    @DisplayName("new round can not be started when the game is eliminated")
    void cannotStartRoundWhenGameEliminated() {
        game.startNewRound("BAARD");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        assertThrows(GameStateException.class, () -> {
            game.startNewRound("BLOEM");
        });
    }

    @Test
    @DisplayName("new round changes status of the game to Playing")
    void newRoundChangesGameStatus() {
        game.startNewRound("BAARD");

        assertEquals(GameStatus.PLAYING,game.getGameStatus());
    }

    @Test
    @DisplayName("round is added to the game")
    void roundAddedToGame() {
        game.startNewRound("BAARD");

        Round round = new Round("BAARD");
        assertEquals(round,game.getLatestRound());
    }

    @Test
    @DisplayName("Latest round throws error if game has no rounds")
    void noActiveRoundsException() {
        assertThrows(GameStateException.class, () -> {
            game.getLatestRound();
        });
    }

    @Test
    @DisplayName("guess can not be made if the game has no open round")
    void cannotGuessWhenNoRound() {
        assertThrows(GameStateException.class, () -> {
            game.guess("BAREN");
        });
    }

    @Test
    @DisplayName("guess can not be made if the game is eliminated")
    void cannotGuessWhenGameEliminated() {
        game.startNewRound("BAARD");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        assertThrows(GameStateException.class, () -> {
            game.guess("BAREN");
        });
    }

    @Test
    @DisplayName("player is eliminated when word is not guessed within attempt limit")
    void playerEliminatedWordNotGuessed() {
        game.startNewRound("BAARD");

        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        assertEquals(GameStatus.ELIMINATED, game.getGameStatus());
    }

    @Test
    @DisplayName("player is not eliminated when word is guessed within attempt limit")
    void playerNotEliminatedWhenWordIsGuessed() {
        game.startNewRound("BAARD");

        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAARD");

        assertNotSame(GameStatus.ELIMINATED, game.getGameStatus());
    }

    @Test
    @DisplayName("game status is changed when word is guessed")
    void correctGuessChangesStatus() {
        game.startNewRound("BAARD");
        game.guess("BAARD");

        assertEquals(GameStatus.WAITING_FOR_ROUND, game.getGameStatus());
    }

    @Test
    @DisplayName("score is added when the word is guessed")
    void addScoreWhenWordIsGuessed() {
        game.startNewRound("BAARD");
        game.guess("BAARD");

        assertEquals(25,game.getScore());
    }

    @Test
    @DisplayName("player is still playing when word is not guessed")
    void playerIsPlaying() {
        game.startNewRound("BAARD");
        game.guess("BAREN");

        assertTrue(game.isPlaying());
    }

    @Test
    @DisplayName("player is not playing when word is guessed")
    void playerIsNotPlaying() {
        game.startNewRound("BAARD");
        game.guess("BAARD");

        assertFalse(game.isPlaying());
    }

    @Test
    @DisplayName("next word length is based on previous round")
    void nextWordLengthBasedOnRound() {
        game.startNewRound("BAARD");
        game.guess("BAARD");

        assertEquals(6, game.provideNextWordLength());
    }

    @ParameterizedTest
    @DisplayName("next word length is reset after 7 letter word")
    @MethodSource("provideNextWordExamples")
    void nextWordBetweenValues(String wordToGuess, Integer expectedWordLength) {
        game.startNewRound(wordToGuess);
        game.guess(wordToGuess);

        assertEquals(expectedWordLength, game.provideNextWordLength());
    }

    static Stream<Arguments> provideNextWordExamples() {
        String fiveLetterWord = "BAARD";
        String sixLetterWord = "DAAGDE";
        String sevenLetterWord = "APEKOOL";

        return Stream.of(
                Arguments.of(fiveLetterWord,  6),
                Arguments.of(sixLetterWord,  7),
                Arguments.of(sevenLetterWord,  5)
        );
    }

}