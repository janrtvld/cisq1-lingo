package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.AttemptLimitReachedException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoFeedbackFoundException;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany
    @JoinColumn
    @Cascade(CascadeType.ALL)
    private final List<Feedback> feedbackHistory = new ArrayList<Feedback>();

    private String wordToGuess;
    private Integer attempts = 0;
    private String lastHint;

    public Round() {}
    public Round(String wordToGuess) {
        this.wordToGuess = wordToGuess;
        this.lastHint = getBaseHint();
    }

    public void guess(String attempt) {
        if(attemptLimitReached()) {
            throw new AttemptLimitReachedException(attempts);
        }
        createFeedback(attempt);
        attempts++;
    }

    private void createFeedback(String attempt) {
        List<Mark> marks = new ArrayList<>();

        for (int i = 0; i < wordToGuess.length(); i++) {
            Character character = attempt.charAt(i);
            if (attemptInvalid(attempt)) {
                marks.add(Mark.INVALID);
                continue;
            }
            if (characterInWordToGuess(character)) {
                if (characterCorrect(character, i)) {
                    marks.add(Mark.CORRECT);
                } else if (characterAlreadyMarked(attempt, i)) {
                    marks.add(Mark.ABSENT);
                } else {
                    marks.add(Mark.PRESENT);
                }
            } else {
                marks.add(Mark.ABSENT);
            }
        }
        feedbackHistory.add(new Feedback(attempt,marks));
    }

    private boolean characterInWordToGuess(Character character) {
        return wordToGuess.indexOf(character) != -1;
    }

    private boolean characterCorrect(Character character, Integer index) {
        return wordToGuess.charAt(index) == character;
    }

    private boolean characterAlreadyMarked(String attempt, Integer index) {
        return attempt.charAt(wordToGuess.indexOf(attempt.charAt(index))) == wordToGuess.charAt(wordToGuess.indexOf(attempt.charAt(index)));
    }

    private boolean attemptInvalid(String attempt) {
        return attempt.length() != wordToGuess.length();
    }

    private String getBaseHint() {
        return wordToGuess.charAt(0) +
                ".".repeat(wordToGuess.length() - 1);
    }

    public String giveHint() {
        if (!feedbackHistory.isEmpty()) {
            this.lastHint = feedbackHistory.get(feedbackHistory.size() - 1).giveHint(lastHint);
        }
        return lastHint;
    }

    public Feedback getLastFeedback() {
        if(feedbackHistory.isEmpty()) {
            throw new NoFeedbackFoundException();
        }
        return feedbackHistory.get(feedbackHistory.size() - 1);
    }

    public boolean attemptLimitReached() {
        int attemptLimit = 5;
        return attempts >= attemptLimit;
    }

    public Integer getCurrentWordLength() {
        return wordToGuess.length();
    }

    public List<Feedback> getFeedbackHistory() {
        return feedbackHistory;
    }

    public Integer getAttempts() {
        return attempts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Round round = (Round) o;
        return Objects.equals(id, round.id) && Objects.equals(feedbackHistory, round.feedbackHistory) && Objects.equals(wordToGuess, round.wordToGuess) && Objects.equals(attempts, round.attempts) && Objects.equals(lastHint, round.lastHint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, feedbackHistory, wordToGuess, attempts, lastHint);
    }
}
