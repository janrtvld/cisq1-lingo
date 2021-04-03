package nl.hu.cisq1.lingo.trainer.domain;

import nl.hu.cisq1.lingo.trainer.domain.exception.GameStateException;
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

    private int score = 0;

    public Game() {
        // Hibernate needs no-arg constructor;
    }

    public void startNewRound(String wordToGuess) {
        if (gameStatus != WAITING_FOR_ROUND) {
            throw new GameStateException(gameStatus);
        }

        Round round = new Round(wordToGuess);
        rounds.add(round);

        gameStatus = PLAYING;
    }

    public void guess(String attempt) {
        if (gameStatus != PLAYING) {
            throw new GameStateException(gameStatus);
        }

        getLatestRound().guess(attempt);

        checkPlayerEliminated();
        checkPlayerVictory();
    }

    public void checkPlayerEliminated() {
        if (getLatestRound().attemptLimitReached() && !getLatestRound().getLastFeedback().isWordGuessed()) {
            gameStatus = ELIMINATED;
        }
    }

    public void checkPlayerVictory() {
        if (getLatestRound().getLastFeedback().isWordGuessed()) {
            score += (5 * (5-getLatestRound().getAttempts()) + 5);
            gameStatus = WAITING_FOR_ROUND;
        }
    }

    public Round getLatestRound() {
        if(rounds.isEmpty()) {
            throw new GameStateException(gameStatus);
        }
        return rounds.get(rounds.size() - 1);
    }

    public Integer provideNextWordLength() {
        int maxWordLength = 7;
        if (rounds.isEmpty() || getLatestRound().getCurrentWordLength() == maxWordLength) {
            return 5;
        }
        return getLatestRound().getCurrentWordLength()+1;
    }

    public boolean isPlaying() {
        return gameStatus == PLAYING;
    }

    public int getScore() {
        return score;
    }

    public Long getId() {
        return id;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

}
