package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.GameStateException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoFeedbackFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;


class GameTest {

    private Game game;

    @BeforeEach
    @DisplayName("init")
    void init() {
        game = new Game();
    }

    @Test
    @DisplayName("new round can only be started if there is no open round")
    void roundMadeWithNoOpenRounds() {
        // Arrange

        // Act
        game.startNewRound("BAARD");

        // Assert
        Round round = new Round("BAARD");
        assertEquals(round,game.getLatestRound());
    }

    @Test
    @DisplayName("new round can not be started when there is already an open round")
    void roundMadeWithOpenRounds() {
        // Arrange
        game.startNewRound("BAARD");

        // Act / Assert
        assertThrows(GameStateException.class, () -> {
            game.startNewRound("BAARD");
        });

    }

    @Test
    @DisplayName("new round can not be started when the game is over")
    void newRoundWhenGameIsOver() {
        // Arrange
        game.startNewRound("BAARD");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        // Act / Assert
        assertThrows(GameStateException.class, () -> {
            game.startNewRound("BLOEM");
        });

    }

    @Test
    @DisplayName("Current round throws error if game has no rounds")
    void noActiveRoundsException() {
        // Act / Assert
        assertThrows(GameStateException.class, () -> {
            game.getLatestRound();
        });
    }

    @Test
    @DisplayName("guess can not be made if the player is eliminated")
    void guessWhenGameOver() {
        // Arrange
        game.startNewRound("BAARD");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        // Act / Assert
        assertThrows(GameStateException.class, () -> {
            game.guess("BAREN");
        });
    }

    @Test
    @DisplayName("guess can not be made if the game has no open round")
    void guessWhenNoRound() {
        // Arrange

        // Act / Assert
        assertThrows(GameStateException.class, () -> {
            game.guess("BAREN");
        });

    }

    @Test
    @DisplayName("score is added when the word is guessed")
    void addScoreWhenWordIsGuessed() {
        // Arrange
        game.startNewRound("BAARD");

        // Act
        game.guess("BAARD");

        // Assert
        assertEquals(25,game.getScore());

    }

    @Test
    @DisplayName("last feedback throws exception when there have been no guesses")
    void progressIsCleared() {
        // Arrange
        game.startNewRound("BAARD");

        // Act
        game.guess("BAARD");

        game.startNewRound("PLAAG");

        // Assert
        assertThrows(NoFeedbackFoundException.class, () -> {
            game.getLatestRound().getLastFeedback();
        });

    }

    @Test
    @DisplayName("player is eliminated when word is not guessed within attempt limit")
    void playerEliminatedWordNotGuessed() {
        // Arrange
        game.startNewRound("BAARD");

        // Act
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");


        // Assert
        assertEquals(GameStatus.ELIMINATED, game.getGameStatus());
    }

    @Test
    @DisplayName("player is not eliminated when word is guessed within attempt limit")
    void playerNotEliminatedWhenWordIsGuessed() {
        // Arrange
        game.startNewRound("BAARD");

        // Act
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAARD");

        // Assert
        assertNotSame(GameStatus.ELIMINATED, game.getGameStatus());
    }


    @Test
    @DisplayName("player is still playing when word is not guessed")
    void playerIsPlaying() {
        // Arrange
        game.startNewRound("BAARD");

        // Act
        game.guess("BAREN");

        // Assert
        assertTrue(game.isPlaying());
    }

    @Test
    @DisplayName("player is not playing when word is guessed")
    void playerIsNotPlaying() {
        // Arrange
        game.startNewRound("BAARD");

        // Act
        game.guess("BAARD");

        // Assert
        assertFalse(game.isPlaying());

    }

    @Test
    @DisplayName("next word length is based on previous round")
    void nextWordLengthBasedOnRound() {
        // Arrange
        game.startNewRound("BAARD");

        // Act
        game.guess("BAARD");

        // Assert
        assertEquals(6, game.provideNextWordLength());

    }

    @Test
    @DisplayName("next word length is reset after 7 letter word")
    void nextWordBetweenValues() {
        // Arrange
        game.startNewRound("BAARD");
        game.guess("BAARD");

        game.startNewRound("DAAGDE");
        game.guess("DAAGDE");

        // Act
        game.startNewRound("APEKOOL");
        game.guess("APEKOOL");

        // Assert
        assertEquals(5, game.provideNextWordLength());

    }


}