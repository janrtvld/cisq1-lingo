package nl.hu.cisq1.lingo.trainer.domain;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String attempt;

    @ElementCollection
    private List<Mark> marks;

    public Feedback(){}
    public Feedback(String attempt, List<Mark> marks) {
        this.attempt = attempt;
        this.marks = marks;
    }

    public String giveHint(String previousHint) {
        StringBuilder newHint = new StringBuilder();

        for(int i = 0; i < previousHint.length(); i++) {
            if (marks.get(i) == Mark.CORRECT) {
                newHint.append(attempt.charAt(i));
            } else if (previousHint.charAt(i) != '.') {
                newHint.append(previousHint.charAt(i));
            } else {
                newHint.append(".");
            }
        }
        return newHint.toString();
    }

    public boolean isWordGuessed() {
        return marks.stream().allMatch(mark -> mark == Mark.CORRECT);
    }

    public boolean isAttemptValid() {
        return marks.stream().noneMatch(mark -> mark == Mark.INVALID);
    }

    public String getAttempt() {
        return attempt;
    }

    public List<Mark> getMarks() {
        return marks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feedback feedback = (Feedback) o;
        return Objects.equals(id, feedback.id) && Objects.equals(attempt, feedback.attempt) && Objects.equals(marks, feedback.marks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attempt, marks);
    }
}
