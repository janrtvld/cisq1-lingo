package nl.hu.cisq1.lingo.trainer.domain.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(Long id ) {
        super("Game with id: " + id + " can not be found.");
    }
}