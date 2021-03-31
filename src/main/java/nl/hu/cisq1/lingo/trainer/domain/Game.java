package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.NoActiveRoundsException;
import nl.hu.cisq1.lingo.words.domain.Word;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static nl.hu.cisq1.lingo.trainer.domain.GameStatus.*;

@Entity
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus = WAITING_FOR_ROUND;

    @OneToMany
    @JoinColumn
    @Cascade(CascadeType.ALL)
    private final List<Round> rounds = new ArrayList<>();

    private Progress progress = new Progress();

    public Game() {
    }

    public void startNewRound(Word wordToGuess) {
        if (gameStatus != WAITING_FOR_ROUND) {
            throw new IllegalStateException("Current game status doesn't allow this action. Status: " + gameStatus + ".");
        }

        Round round = new Round(wordToGuess);

        rounds.add(round);

        gameStatus = PLAYING;

        progress.progressRound();

        progress.saveNewProgress(round.giveHint(),round.getFeedbackHistory());
    }

    public void guess(String attempt) {
        if (gameStatus != PLAYING) {
            throw new IllegalStateException("Current game status doesn't allow this action. Status: " + gameStatus + ".");
        }

        getLatestRound().guess(attempt);

        // Change the progress of the game based on the new round information
        progress.saveNewProgress(getLatestRound().giveHint(), getLatestRound().getFeedbackHistory());

        isPlayerEliminated();
        isPlaying();

    }

    public Integer provideNextWordLength() {
        if (rounds.isEmpty()) {
            return 5;
        }
        if (getLatestRound().getCurrentWordLength()+1 > 7) {
            return 5;
        }
        return getLatestRound().getCurrentWordLength()+1;
    }

    public boolean isPlayerEliminated() {
        if (getLatestRound().attemptLimitReached() && !progress.getLastFeedback().isWordGuessed()) {
            gameStatus = ELIMINATED;
        }
        return gameStatus == ELIMINATED;
    }

    public boolean isPlaying() {
        if (progress.getLastFeedback().isWordGuessed()) {
            progress.addScore(5 * (5-getLatestRound().getAttempts()) + 5);
            gameStatus = WAITING_FOR_ROUND;
        }
        return gameStatus == PLAYING;
    }

    public Round getLatestRound() {
        if(rounds.isEmpty()) {
            throw new NoActiveRoundsException();
        }
        return rounds.get(rounds.size() - 1);
    }

    public Progress getProgress() {
        return progress;
    }

    public Long getId() {
        return id;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public List<Round> getRounds() {
        return rounds;
    }
}
