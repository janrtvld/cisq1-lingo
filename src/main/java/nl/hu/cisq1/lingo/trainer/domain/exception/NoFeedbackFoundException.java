package nl.hu.cisq1.lingo.trainer.domain.exception;

public class NoFeedbackFoundException extends RuntimeException {
    public NoFeedbackFoundException() {
        super("Please play a guess to generate feedback.");
    }
}