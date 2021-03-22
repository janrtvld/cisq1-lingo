package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.NoActiveRoundsException;
import nl.hu.cisq1.lingo.words.domain.Word;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game")
public class Game implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Lob
    private GameStatus gameStatus;

    @Lob
    private Progress progress;

    @Lob
    private ArrayList<Round> rounds;

    public Game() {
        this.gameStatus = GameStatus.WAITING_FOR_ROUND;
        this.progress = new Progress();
        this.rounds = new ArrayList<>();
    }

    public void startNewRound(Word wordToGuess) {
        if (gameStatus != GameStatus.WAITING_FOR_ROUND) {
            throw new IllegalStateException("Current game status doesn't allow this action. Status: " + gameStatus + ".");
        }

        Round round = new Round(wordToGuess);

        // Add to array of rounds
        rounds.add(round);

        // Change the GameStatus to Playing
        gameStatus = GameStatus.PLAYING;

        // Progress to the next round
        progress.progressRound();

        // Set new progress data
        progress.saveNewProgress(round.giveHint(),round.getFeedbackHistory());
    }

    public void guess(String attempt) {
        if (gameStatus != GameStatus.PLAYING) {
            throw new IllegalStateException("Current game status doesn't allow this action. Status: " + gameStatus + ".");
        }

        getLatestRound().guess(attempt);

        // Change the progress of the game based on the new round information
        progress.saveNewProgress(getLatestRound().giveHint(), getLatestRound().getFeedbackHistory());

        isPlayerEliminated();
        isPlaying();

    }

    public Integer provideNextWordLength() {
        if (getLatestRound().getCurrentWordLength()+1 > 7) {
            return 5;
        }
        return getLatestRound().getCurrentWordLength()+1;
    }

    public boolean isPlayerEliminated() {
        if (getLatestRound().attemptLimitReached() && !progress.getLastFeedback().isWordGuessed()) {
            gameStatus = GameStatus.ELIMINATED;
        }
        return gameStatus == GameStatus.ELIMINATED;
    }

    public boolean isPlaying() {
        if (progress.getLastFeedback().isWordGuessed()) {
            progress.addScore(5 * (5-getLatestRound().getAttempts()) + 5);
            gameStatus = GameStatus.WAITING_FOR_ROUND;
        }
        return gameStatus == GameStatus.PLAYING;
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
