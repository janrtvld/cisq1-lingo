package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.AttemptLimitReachedException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoFeedbackFoundException;
import nl.hu.cisq1.lingo.words.domain.Word;
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
            if (attempt.length() != wordToGuess.length()) {
                marks.add(Mark.INVALID);
                continue;
            }
            if (attempt.charAt(i) == wordToGuess.charAt(i)) {
                marks.add(Mark.CORRECT);
                continue;
            }
            if (wordToGuess.indexOf(attempt.charAt(i)) != -1) {
                if (attempt.charAt(wordToGuess.indexOf(attempt.charAt(i))) == wordToGuess.charAt(wordToGuess.indexOf(attempt.charAt(i)))) {
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

    private String getBaseHint() {
        String baseHint = "";
        baseHint += wordToGuess.charAt(0);
        for(int i = 0; i < wordToGuess.length()-1; i++) {
            baseHint += ".";
        }
        return baseHint;
    }

    public String giveHint() {
        if (!feedbackHistory.isEmpty()) {
            this.lastHint = feedbackHistory.get(feedbackHistory.size() - 1).giveHint(lastHint);
        }
        return lastHint;
    }

    public boolean attemptLimitReached() {
        return attempts >= 5;
    }

    public Integer getCurrentWordLength() {
        return wordToGuess.length();
    }

    public List<Feedback> getFeedbackHistory() {
        return feedbackHistory;
    }

    public Feedback getLastFeedback() {
        if(feedbackHistory.isEmpty()) {
            throw new NoFeedbackFoundException();
        }
        return feedbackHistory.get(feedbackHistory.size() - 1);
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

    @Override
    public int hashCode() {
        return Objects.hash(id, wordToGuess, attempts, feedbackHistory, lastHint);
    }
}
