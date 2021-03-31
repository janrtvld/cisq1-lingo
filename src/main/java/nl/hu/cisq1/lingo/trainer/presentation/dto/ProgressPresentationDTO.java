package nl.hu.cisq1.lingo.trainer.presentation.dto;

import nl.hu.cisq1.lingo.trainer.domain.Feedback;

import java.util.List;
import java.util.Objects;

public class ProgressPresentationDTO {
    public Long id;
    public Integer score;
    public List<Feedback> feedbackHistory;
    public String newHint;

    private ProgressPresentationDTO() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressPresentationDTO that = (ProgressPresentationDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(score, that.score) && Objects.equals(feedbackHistory, that.feedbackHistory) && Objects.equals(newHint, that.newHint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, score, feedbackHistory, newHint);
    }

    public static class Builder {
        public final Long id;
        public Integer score;
        public List<Feedback> feedbackHistory;
        public String newHint;

        public Builder(Long id) {
            this.id = id;
        }

        public Builder score(Integer score) {
            this.score = score;

            return this;
        }

        public Builder feedbackHistory(List<Feedback> feedbackHistory) {
            this.feedbackHistory = feedbackHistory;

            return this;
        }

        public Builder newHint(String newHint) {
            this.newHint = newHint;

            return this;
        }

        public ProgressPresentationDTO build() {
            ProgressPresentationDTO progress = new ProgressPresentationDTO();
            progress.id = this.id;
            progress.score = this.score;
            progress.feedbackHistory = this.feedbackHistory;
            progress.newHint = this.newHint;

            return progress;
        }
    }
}

