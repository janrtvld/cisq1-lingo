package nl.hu.cisq1.lingo.trainer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static nl.hu.cisq1.lingo.trainer.domain.Mark.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedbackTest {

    @Test
    @DisplayName("word is guessed when all marks are correct")
    void isWordGuessed() {
        List<Mark> marks = List.of(CORRECT, CORRECT, CORRECT, CORRECT, CORRECT);
        String attempt = "PAARD";
        Feedback feedback = new Feedback(attempt, marks);

        assertTrue(feedback.isWordGuessed());
    }

    @Test
    @DisplayName("word is not guessed when all marks are not correct")
    void isWordNotGuessed() {
        List<Mark> marks = List.of(CORRECT, CORRECT, CORRECT, CORRECT, ABSENT);
        String attempt = "PAARS";
        Feedback feedback = new Feedback(attempt, marks);

        assertFalse(feedback.isWordGuessed());

    }

    @Test
    @DisplayName("attempt is valid when none of the marks are invalid")
    void isAttemptValid() {
        List<Mark> marks = List.of(CORRECT, CORRECT, CORRECT, CORRECT, ABSENT);
        String attempt = "PAARS";
        Feedback feedback = new Feedback(attempt, marks);

        assertTrue(feedback.isAttemptValid());
    }

    @Test
    @DisplayName("attempt is invalid when one/all marks are invalid")
    void isAttemptInvalid() {
        List<Mark> marks = List.of(INVALID, INVALID, INVALID, INVALID, INVALID, INVALID);
        String attempt = "PAARDEN";
        Feedback feedback = new Feedback(attempt, marks);

        assertFalse(feedback.isAttemptValid());
    }

    @ParameterizedTest
    @MethodSource("provideHintExamples")
    @DisplayName("provide a correct hint")
    void correctHint(String previousHint, Feedback feedback, String nextHint) {
        assertEquals(nextHint, feedback.giveHint(previousHint));
    }

    static Stream<Arguments> provideHintExamples() {
        String invalidAttempt = "BERGEN";
        String presentAttempt = "DRAAD";
        String correctAttempt = "BAARD";
        String longAttempt = "BAARDVOGEL";

        Feedback fb1 = new Feedback(invalidAttempt, List.of(INVALID, INVALID, INVALID, INVALID, INVALID, INVALID));
        Feedback fb2 = new Feedback(longAttempt, List.of(INVALID, INVALID, INVALID, INVALID, INVALID, INVALID));
        Feedback fb3 = new Feedback(presentAttempt, List.of(ABSENT, PRESENT, CORRECT, PRESENT, CORRECT));
        Feedback fb4 = new Feedback(correctAttempt, List.of(CORRECT, CORRECT, CORRECT, CORRECT, CORRECT));

        return Stream.of(
                Arguments.of("B....", fb1, "B...."),
                Arguments.of("B....", fb2, "B...."),
                Arguments.of("B....", fb3, "B.A.D"),
                Arguments.of("B.A.D", fb4,  "BAARD")
        );
    }

}