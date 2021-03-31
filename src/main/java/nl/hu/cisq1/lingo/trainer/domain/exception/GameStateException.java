package nl.hu.cisq1.lingo.trainer.domain.exception;

import nl.hu.cisq1.lingo.trainer.domain.GameStatus;

public class GameStateException extends RuntimeException {
    public GameStateException(GameStatus state ) {
        super("Current gamestate doesn't allow this action: " +  state.getStatus() + ".");
    }
}
