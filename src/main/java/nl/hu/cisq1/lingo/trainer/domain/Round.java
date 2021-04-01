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
        List<Mark> marks = new ArrayList<Mark>();

        for (int i = 0; i < wordToGuess.length(); i++) {
            if (attemptLengthInvalid(attempt)) {
                marks.add(Mark.INVALID);
                continue;
            }
            if (wordToGuess.indexOf(attempt.charAt(i)) != -1) {
                if (attempt.charAt(i) == wordToGuess.charAt(i)) {
                    marks.add(Mark.CORRECT);
                } else if (attempt.charAt(wordToGuess.indexOf(attempt.charAt(i))) == wordToGuess.charAt(wordToGuess.indexOf(attempt.charAt(i)))) {
                    marks.add(Mark.ABSENT);
                } else {
                    marks.add(Mark.PRESENT);
                }
            } else {
                marks.add(Mark.ABSENT);
            }
        }
        attempts++;
        feedbackHistory.add(new Feedback(attempt,marks));
    }

    private boolean attemptLengthInvalid(String attempt) {
        return attempt.length() != wordToGuess.length();
    }

    private String getBaseHint() {
        StringBuilder baseHint = new StringBuilder();
        baseHint.append(wordToGuess.charAt(0));
        baseHint.append(".".repeat(wordToGuess.length() - 1));

        return baseHint.toString();
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
        return Objects.equals(id, round.id) && Objects.equals(wordToGuess, round.wordToGuess) && Objects.equals(attempts, round.attempts) && Objects.equals(feedbackHistory, round.feedbackHistory) && Objects.equals(lastHint, round.lastHint);
    }

}
