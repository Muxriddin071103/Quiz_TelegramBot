package uz.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String state;
    private List<Test> currentTest;
    private int currentQuestionIndex;
    private int correctAnswersCount;
    private String currentTestQuestion;
    private List<Answer> currentTestAnswers;
    private String addTestStep;
    private int editingTestIndex;
    private int selectedMessageIndex;

    public void resetAddTestProcess() {
        this.addTestStep = null;
        this.currentTestQuestion = null;
        this.currentTestAnswers = null;
        this.editingTestIndex = -1;
    }

}
