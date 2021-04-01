package nl.hu.cisq1.lingo.trainer.presentation.dto;

import nl.hu.cisq1.lingo.trainer.domain.Feedback;

import java.util.List;
import java.util.Objects;

public class ProgressPresentationDTO {
    public Long id;
    public String gameStatus;
    public Integer score;
    public List<Feedback> feedbackHistory;
    public String currentHint;

    private ProgressPresentationDTO() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressPresentationDTO that = (ProgressPresentationDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(gameStatus, that.gameStatus) && Objects.equals(score, that.score) && Objects.equals(feedbackHistory, that.feedbackHistory) && Objects.equals(currentHint, that.currentHint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gameStatus, score, feedbackHistory, currentHint);
    }

    public static class Builder {
        public final Long id;
        public String gameStatus;
        public Integer score;
        public List<Feedback> feedbackHistory;
        public String currentHint;

        public Builder(Long id) {
            this.id = id;
        }

        public Builder gameStatus(String gameStatus) {
            this.gameStatus = gameStatus;

            return this;
        }

        public Builder score(Integer score) {
            this.score = score;

            return this;
        }


        public Builder feedbackHistory(List<Feedback> feedbackHistory) {
            this.feedbackHistory = feedbackHistory;

            return this;
        }

        public Builder currentHint(String currentHint) {
            this.currentHint = currentHint;

            return this;
        }

        public ProgressPresentationDTO build() {
            ProgressPresentationDTO progress = new ProgressPresentationDTO();
            progress.id = this.id;
            progress.gameStatus = this.gameStatus;
            progress.score = this.score;
            progress.feedbackHistory = this.feedbackHistory;
            progress.currentHint = this.currentHint;

            return progress;
        }
    }
}

