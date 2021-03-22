package nl.hu.cisq1.lingo.trainer.domain;

import javassist.NotFoundException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoActiveRoundsException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoFeedbackFoundException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Progress implements Serializable {

    private Integer score;
    private int roundNumber;
    private List<Feedback> currentFeedbackHistory;
    private String lastHint;

    public Progress() {
        this.score = 0;
        this.roundNumber = 0;
        this.currentFeedbackHistory = new ArrayList<>();
        this.lastHint = "";
    }

    public void progressRound() {
        this.roundNumber++;
        this.currentFeedbackHistory = new ArrayList<>();
        this.lastHint = "";
    }

    public void saveNewProgress(String hint, List<Feedback> feedbackHistory) {
        this.lastHint = hint;
        this.currentFeedbackHistory = feedbackHistory;
    }

    public Feedback getLastFeedback() {
        if(currentFeedbackHistory.isEmpty()) {
            throw new NoFeedbackFoundException();
        }
        return currentFeedbackHistory.get(currentFeedbackHistory.size() - 1);
    }

    public Integer getScore() {
        return score;
    }

    public void addScore(Integer score) {
        this.score += score;
    }

    public int getRoundNumber() {
        return roundNumber;
    }
}
