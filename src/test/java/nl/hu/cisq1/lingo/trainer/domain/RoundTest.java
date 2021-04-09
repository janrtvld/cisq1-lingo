package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.AttemptLimitReachedException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoFeedbackFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static nl.hu.cisq1.lingo.trainer.domain.Mark.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Round")
class RoundTest {

    private Round round;

    @BeforeEach
    @DisplayName("initiates new round before each test")
    void beforeEachTest() {
        round = new Round("BAARD");
    }

    @Test
    @DisplayName("counts attempts after every guess")
    void guessCountsAttempts() {
        round.guess("BAKEN");

        assertEquals(1,round.getAttempts());
    }

    @Test
    @DisplayName("attempt limit is not reached when there are less then 5 attempts")
    void attemptLimitNotReached() {
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");

        assertFalse(round.attemptLimitReached());
    }

    @Test
    @DisplayName("attempt limit is reached when 5 attempts have been done")
    void attemptLimitReached() {
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");

        assertTrue(round.attemptLimitReached());
    }

    @Test
    @DisplayName("throws exception when trying to guess after attempt limit is reached")
    void guessLimitThrowsError() {
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");

        assertThrows(AttemptLimitReachedException.class, () ->
                round.guess("BAKEN")
        );
    }

    @ParameterizedTest
    @MethodSource("provideBaseHintExamples")
    @DisplayName("provides a base hint with the first character")
    void baseHintProvidesFirstCharacter(Round round, String expectedHint) {
        assertEquals(expectedHint, round.giveHint());
    }

    static Stream<Arguments> provideBaseHintExamples() {
        Round roundWithFiveLetterWord = new Round("BLOEM");
        Round roundWithSixLetterWord = new Round("DAAGDE");
        Round roundWithSevenLetterWord = new Round("APEKOOL");

        return Stream.of(
                Arguments.of(roundWithFiveLetterWord,  "B...."),
                Arguments.of(roundWithSixLetterWord, "D....."),
                Arguments.of(roundWithSevenLetterWord,  "A......")
        );
    }

    @Test
    @DisplayName("provides hint based on previous guesses")
    void generateHintBasedOnFeedback() {
        round.guess("BAKEN");
        String hint = round.giveHint();

        assertEquals("BA...", hint);
    }

    @Test
    @DisplayName("keeps track of feedback history")
    void feedbackIsSaved() {
        round.guess("BAKEN");
        round.guess("BAKEN");

        assertEquals(2,round.getFeedbackHistory().size());
    }

    @Test
    @DisplayName("throws exception if there is no feedback")
    void throwExceptionByNoFeedback() {
        assertThrows(NoFeedbackFoundException.class, () ->
            round.getLastFeedback()
        );
    }

    @ParameterizedTest
    @MethodSource("provideWordLengthExamples")
    @DisplayName("provide current word length")
    void CurrentWordLength(Round round, Integer expectedWordLength) {
        assertEquals(expectedWordLength, round.getCurrentWordLength());
    }

    static Stream<Arguments> provideWordLengthExamples() {
        String fiveLetterWord = "BAARD";
        Round roundWithFiveLetters = new Round(fiveLetterWord);

        String sixLetterWord = "BOEREN";
        Round roundWithSixLetters = new Round(sixLetterWord);

        String sevenLetterWord = "APEKOOL";
        Round roundWithSevenLetters = new Round(sevenLetterWord);

        return Stream.of(
                Arguments.of(roundWithFiveLetters, 5),
                Arguments.of(roundWithSixLetters, 6),
                Arguments.of(roundWithSevenLetters, 7)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFeedbackExamples")
    @DisplayName("provide the correct Feedback")
    void generateFeedback(Round round, String attempt, Feedback feedback) {
        round.guess(attempt);

        assertEquals(feedback,round.getLastFeedback());
    }

    static Stream<Arguments> provideFeedbackExamples() {
        Round round1 = new Round("BAARD");
        Round round2 = new Round("ABBBBB");
        Round round3 = new Round("WOLOLOO");

        Feedback fb1 = new Feedback("BERGEN", List.of(INVALID, INVALID, INVALID, INVALID, INVALID));
        Feedback fb2 = new Feedback("BONJE", List.of(CORRECT, ABSENT, ABSENT, ABSENT, ABSENT));
        Feedback fb3 = new Feedback("BARST", List.of(CORRECT, CORRECT, PRESENT, ABSENT, ABSENT));
        Feedback fb4 = new Feedback("DRAAD", List.of(ABSENT, PRESENT, CORRECT, PRESENT, CORRECT));
        Feedback fb5 = new Feedback("BAARD", List.of(CORRECT, CORRECT, CORRECT, CORRECT, CORRECT));
        Feedback fb6 = new Feedback("BAAAAA", List.of(PRESENT, PRESENT, ABSENT, ABSENT, ABSENT,ABSENT));
        Feedback fb7 = new Feedback("ONONOMO", List.of(PRESENT, ABSENT, PRESENT, ABSENT, PRESENT,ABSENT,CORRECT));
        Feedback fb8 = new Feedback("O", List.of(INVALID, INVALID, INVALID, INVALID, INVALID,INVALID,INVALID));
        Feedback fb9 = new Feedback("", List.of(INVALID, INVALID, INVALID, INVALID, INVALID,INVALID,INVALID));
        Feedback fb10 = new Feedback("OOOOOOOOOOOOOOOOOO", List.of(INVALID, INVALID, INVALID, INVALID, INVALID,INVALID,INVALID));

        return Stream.of(
                Arguments.of(round1,"BERGEN", fb1),
                Arguments.of(round1,"BONJE", fb2),
                Arguments.of(round1,"BARST", fb3),
                Arguments.of(round1,"DRAAD", fb4),
                Arguments.of(round1,"BAARD", fb5),
                Arguments.of(round2,"BAAAAA", fb6),
                Arguments.of(round3,"ONONOMO", fb7),
                Arguments.of(round3,"O", fb8),
                Arguments.of(round3,"", fb9),
                Arguments.of(round3,"OOOOOOOOOOOOOOOOOO", fb10)
        );
    }

}