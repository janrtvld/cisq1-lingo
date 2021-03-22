package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.AttemptLimitReachedException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoActiveRoundsException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoFeedbackFoundException;
import nl.hu.cisq1.lingo.words.domain.Word;
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
        Word wordToGuess = new Word("BAARD");

        // Act
        game.startNewRound(wordToGuess);

        // Assert
        Round round = new Round(wordToGuess);
        assertEquals(round,game.getRounds().get(0));
    }

    @Test
    @DisplayName("new round can not be started when there is already an open round")
    void roundMadeWithOpenRounds() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);

        // Act / Assert
        assertThrows(IllegalStateException.class, () -> {
            game.startNewRound(wordToGuess);
        });

    }

    @Test
    @DisplayName("new round can not be started when the game is over")
    void newRoundWhenGameIsOver() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        // Act / Assert
        Word wordToGuess2 = new Word("BLOEM");
        assertThrows(IllegalStateException.class, () -> {
            game.startNewRound(wordToGuess2);
        });

    }

    @Test
    @DisplayName("Current round throws error if game has no rounds")
    void noActiveRoundsException() {
        // Act / Assert
        assertThrows(NoActiveRoundsException.class, () -> {
            game.getLatestRound();
        });
    }

    @Test
    @DisplayName("guess can not be made if the player is eliminated")
    void guessWhenGameOver() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");

        // Act / Assert
        assertThrows(IllegalStateException.class, () -> {
            game.guess("BAREN");
        });
    }

    @Test
    @DisplayName("guess can not be made if the game has no open round")
    void guessWhenNoRound() {
        // Arrange

        // Act / Assert
        assertThrows(IllegalStateException.class, () -> {
            game.guess("BAREN");
        });

    }

    @Test
    @DisplayName("score is added when the word is guessed")
    void addScoreWhenWordIsGuessed() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);

        // Act
        game.guess("BAARD");

        // Assert
        assertEquals(25,game.getProgress().getScore());

    }

    @Test
    @DisplayName("last feedback throws exception when there have been no guesses")
    void progressIsCleared() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);

        // Act
        game.guess("BAARD");

        Word wordToGuess2 = new Word("PLAAG");
        game.startNewRound(wordToGuess2);

        // Assert
        assertThrows(NoFeedbackFoundException.class, () -> {
            game.getProgress().getLastFeedback();
        });

    }

    @Test
    @DisplayName("progress is not cleared when the round is still going")
    void progressIsNotCleared() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);

        // Act
        game.guess("BAREN");

        // Assert
        assertEquals("BAREN", game.getProgress().getLastFeedback().getAttempt());
    }

    @Test
    @DisplayName("player is eliminated when word is not guessed within attempt limit")
    void playerEliminatedWordNotGuessed() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);

        // Act
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");


        // Assert
        assertTrue(game.isPlayerEliminated());

    }

    @Test
    @DisplayName("player is not eliminated when word is guessed within attempt limit")
    void playerNotEliminatedWhenWordIsGuessed() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);

        // Act
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAREN");
        game.guess("BAARD");

        // Assert
        assertFalse(game.isPlayerEliminated());

    }


    @Test
    @DisplayName("player is still playing when word is not guessed")
    void playerIsPlaying() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);

        // Act
        game.guess("BAREN");


        // Assert
        assertTrue(game.isPlaying());

    }

    @Test
    @DisplayName("player is not playing when word is guessed")
    void playerIsNotPlaying() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);

        // Act
        game.guess("BAARD");

        // Assert
        assertFalse(game.isPlaying());

    }

    @Test
    @DisplayName("next word length is based on previous round")
    void nextWordLengthBasedOnRound() {
        // Arrange
        Word wordToGuess = new Word("BAARD");
        game.startNewRound(wordToGuess);

        // Act
        game.guess("BAARD");

        // Assert
        assertEquals(6, game.provideNextWordLength());

    }

    @Test
    @DisplayName("next word length is reset after 7 letter word")
    void nextWordBetweenValues() {
        // Arrange
        Word fiveLetterWord = new Word("BAARD");
        Word sixLetterWord = new Word("DAAGDE");
        Word sevenLetterWord = new Word("APEKOOL");

        game.startNewRound(fiveLetterWord);
        game.guess("BAARD");

        game.startNewRound(sixLetterWord);
        game.guess("DAAGDE");

        // Act
        game.startNewRound(sevenLetterWord);
        game.guess("APEKOOL");

        // Assert
        assertEquals(5, game.provideNextWordLength());

    }


}