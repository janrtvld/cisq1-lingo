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


class RoundTest {

    private Round round;

    @BeforeEach
    @DisplayName("initiate round for tests")
    void init() {
        round = new Round("BAARD");
    }

    @Test
    @DisplayName("Every guess reduces amount of attempts left")
    void guessReducesAttempts() {
        round.guess("BAKEN");

        assertEquals(1,round.getAttempts());
    }

    @Test
    @DisplayName("guess can not be processed if attempt limit is reached")
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
    @DisplayName("attempt limit is reached when 5 or more attempts have been done")
    void attemptLimitReached() {
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");
        round.guess("BAKEN");

        assertTrue(round.attemptLimitReached());
    }

    @Test
    @DisplayName("base hint only provides first letter")
    void baseHintProvidesFirstLetter() {
        String hint = round.giveHint();

        assertEquals("B....", hint);
    }

    @Test
    @DisplayName("provide hint based on previous feedback")
    void generateHintBasedOnFeedback() {
        round.guess("BAKEN");
        String hint = round.giveHint();

        assertEquals("BA...", hint);
    }


    @Test
    @DisplayName("feedback is added to the feedback history")
    void feedbackSaved() {
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
        // Input
        Round round1 = new Round("BAARD");
        Round round2 = new Round("ABBBBB");
        Round round3 = new Round("WOLOLOO");

        List<Mark> marks1 = List.of(INVALID, INVALID, INVALID, INVALID, INVALID);
        List<Mark> marks2 = List.of(CORRECT, ABSENT, ABSENT, ABSENT, ABSENT);
        List<Mark> marks3 = List.of(CORRECT, CORRECT, PRESENT, ABSENT, ABSENT);
        List<Mark> marks4 = List.of(ABSENT, PRESENT, CORRECT, PRESENT, CORRECT);
        List<Mark> marks5 = List.of(CORRECT, CORRECT, CORRECT, CORRECT, CORRECT);
        List<Mark> marks6 = List.of(PRESENT, PRESENT, ABSENT, ABSENT, ABSENT,ABSENT);
        List<Mark> marks7 = List.of(PRESENT, ABSENT, PRESENT, ABSENT, PRESENT,ABSENT,CORRECT);

        // Output
        Feedback feedback1 = new Feedback("BERGEN", marks1);
        Feedback feedback2 = new Feedback("BONJE", marks2);
        Feedback feedback3 = new Feedback("BARST", marks3);
        Feedback feedback4 = new Feedback("DRAAD", marks4);
        Feedback feedback5 = new Feedback("BAARD", marks5);
        Feedback feedback6 = new Feedback("BAAAAA", marks6);
        Feedback feedback7 = new Feedback("ONONOMO", marks7);

        return Stream.of(
                Arguments.of(round1,"BERGEN", feedback1),
                Arguments.of(round1,"BONJE", feedback2),
                Arguments.of(round1,"BARST", feedback3),
                Arguments.of(round1,"DRAAD", feedback4),
                Arguments.of(round1,"BAARD", feedback5),
                Arguments.of(round2,"BAAAAA", feedback6),
                Arguments.of(round3,"ONONOMO", feedback7)
        );
    }

}