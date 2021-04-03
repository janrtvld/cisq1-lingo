package nl.hu.cisq1.lingo.trainer.domain;

public enum GameStatus {
    WAITING_FOR_ROUND("WAITING_FOR_ROUND"),
    PLAYING("PLAYING"),
    ELIMINATED("ELIMINATED");

    private final String status;

    private GameStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
