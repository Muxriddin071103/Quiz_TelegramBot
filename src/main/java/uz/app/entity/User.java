package uz.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String state;
    private List<Test> currentTest;
    private int currentQuestionIndex;
    private int correctAnswersCount;

    private String addTestStep;
    private String currentTestQuestion;
    private List<Answer> currentTestAnswers;
    private int editingTestIndex;

    public List<Answer> getCurrentTestAnswers() {
        if (currentTestAnswers == null) {
            currentTestAnswers = new ArrayList<>();
        }
        return currentTestAnswers;
    }

    public void resetAddTestProcess() {
        this.addTestStep = null;
        this.currentTestQuestion = null;
        this.currentTestAnswers = new ArrayList<>();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        User user = (User) object;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void setEditingTestIndex(int editingTestIndex) {
        this.editingTestIndex = editingTestIndex;
    }
}
