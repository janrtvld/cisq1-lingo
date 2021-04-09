package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.GameStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Game")
class GameTest {

    private Game game;

    @BeforeEach
    @DisplayName("initiates new game before each test")
    void beforeEachTest() {
        game = new Game();
    }

    @Test
    @DisplayName("cannot start new round when there already is an open round")
    void cannotStartRoundWhenCurrentRoundIsOpen() {
        game.startNewRound("BAARD");

        assertThrows(GameStateException.class, () ->
            game.startNewRound("BAARD")
        );
    }

    @Test
    @DisplayName("cannot start new round when the game is eliminated")
    void cannotStartRoundWhenGameEliminated() {
        game.startNewRound("BAARD");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        assertThrows(GameStateException.class, () ->
            game.startNewRound("BLOEM")
        );
    }

    @Test
    @DisplayName("changes game status by starting a new round")
    void newRoundChangesGameStatus() {
        game.startNewRound("BAARD");

        assertEquals(GameStatus.PLAYING,game.getGameStatus());
    }

    @Test
    @DisplayName("returns the latest round")
    void returnsLatestRound() {
        game.startNewRound("BAARD");

        Round round = new Round("BAARD");
        assertEquals(round,game.getLatestRound());
    }

    @Test
    @DisplayName("returns the latest round when multiple rounds are played")
    void multipleRoundsReturnsLatest() {
        game.startNewRound("BAARD");
        game.guess("BAARD");
        game.startNewRound("BOORD");

        Round round = new Round("BOORD");
        assertEquals(round,game.getLatestRound());
    }

    @Test
    @DisplayName("throws exception if there is no last round")
    void noActiveRoundsException() {
        assertThrows(GameStateException.class, () ->
            game.getLatestRound()
        );
    }

    @Test
    @DisplayName("throws exception when trying to guess without a open round")
    void cannotGuessWhenNoRound() {
        assertThrows(GameStateException.class, () ->
            game.guess("BAREN")
        );
    }

    @Test
    @DisplayName("throws exception when trying to guess when game is eliminated")
    void cannotGuessWhenGameEliminated() {
        game.startNewRound("BAARD");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        assertThrows(GameStateException.class, () ->
            game.guess("BAREN")
        );
    }

    @Test
    @DisplayName("is eliminated when word is not guessed within attempt limit")
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
    @DisplayName("is not eliminated when word is guessed within attempt limit")
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
    @DisplayName("is still eliminated when word is correctly guessed after attempt limit has been reached")
    void playerEliminatedWordGuessedAfterLimitReached() {
        game.startNewRound("BAARD");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        assertThrows(GameStateException.class, () ->
                game.guess("BAARD")
        );
    }

    @Test
    @DisplayName("changes game status when word is guessed")
    void correctGuessChangesStatus() {
        game.startNewRound("BAARD");
        game.guess("BAARD");

        assertEquals(GameStatus.WAITING_FOR_ROUND, game.getGameStatus());
    }

    @Test
    @DisplayName("adds score when word is guessed")
    void addScoreWhenWordIsGuessed() {
        game.startNewRound("BAARD");
        game.guess("BAARD");

        assertEquals(25,game.getScore());
    }

    @ParameterizedTest
    @DisplayName("correctly adds score when winning multiple rounds")
    @MethodSource("provideGameExamples")
    void nextScoreCorrectlyCounted(Game game, Integer expectedScore) {
        assertEquals(game.getScore(), expectedScore);
    }

    static Stream<Arguments> provideGameExamples() {
        Game testGame1 = new Game();
        testGame1.startNewRound("BAARD");
        testGame1.guess("BAARD");

        Game testGame2 = new Game();
        testGame2.startNewRound("BAARD");
        testGame2.guess("BAARD");
        testGame2.startNewRound("DAAGDE");
        testGame2.guess("BAARD");
        testGame2.guess("DAAGDE");

        Game testGame3 = new Game();
        testGame3.startNewRound("BAARD");
        testGame3.guess("BAARD");
        testGame3.startNewRound("DAAGDE");
        testGame3.guess("DAAGDE");
        testGame3.startNewRound("APEKOOL");
        testGame3.guess("APOKOOL");
        testGame3.guess("APOKOOL");
        testGame3.guess("APEKOOL");

        return Stream.of(
                Arguments.of(testGame1,  25),
                Arguments.of(testGame2,  45),
                Arguments.of(testGame3,  65)
        );
    }

    @Test
    @DisplayName("is still playing when word is not guessed")
    void playerIsPlaying() {
        game.startNewRound("BAARD");
        game.guess("BAREN");

        assertTrue(game.isPlaying());
    }

    @Test
    @DisplayName("is not playing when word is guessed")
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