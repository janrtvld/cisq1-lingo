package nl.hu.cisq1.lingo.trainer.domain.exception;

public class NoActiveRoundsException extends RuntimeException {
    public NoActiveRoundsException() {
        super("There are no rounds yet. Please start a round");
    }
}