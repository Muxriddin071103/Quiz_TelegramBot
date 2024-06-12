package uz.app.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.app.db.Db;
import uz.app.entity.Answer;
import uz.app.entity.Test;
import uz.app.entity.User;
import uz.app.payload.InlineString;
import java.util.*;

import static uz.app.util.Utils.*;

public class BotLogicService {
    private SendMessage sendMessage = new SendMessage();
    Db db = new Db();
    BotService botService = BotService.getInstance();
    private final ReplyMarkupService replyService = new ReplyMarkupService();
    private final InlineMarkupService inlineService = new InlineMarkupService();

    private Set<User> users = new HashSet<>();
    private final String adminId = "1555507909";

    public void messageHandler(Update update) {
        Long id = update.getMessage().getChat().getId();
        User currentUser = getUserById(id);
        sendMessage.setReplyMarkup(null);
        sendMessage.setChatId(id);

        String text = update.getMessage().getText();

        if (currentUser == null) {
            users.add(new User(id.toString(), "main", new ArrayList<>(), 0, 0,null,null,null,0));
            currentUser = getUserById(id);
        }

        if (currentUser.getId().equals(adminId)) {
            if (currentUser.getAddTestStep() != null) {
                handleAddTestCallback(currentUser, text);
                return;
            }

            switch (text) {
                // Admin Menu
                case "/start" -> {
                    sendMessage.setText("Welcome Admin!");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                    botService.executeMessages(sendMessage);
                }
                case ADD_TEST -> {
                    sendMessage.setText("Please enter the new test question.");
                    currentUser.setAddTestStep("QUESTION");
                    botService.executeMessages(sendMessage);
                }
                case EDIT_TEST -> {
                    sendMessage.setText("Please select a test to edit:");
                    List<String> testOptions = new ArrayList<>();
                    for (int i = 0; i < db.tests.size(); i++) {
                        testOptions.add("Question " + (i + 1));
                    }
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(new String[][]{testOptions.toArray(new String[0])}));
                    currentUser.setAddTestStep("EDIT_TEST");
                    botService.executeMessages(sendMessage);
                }
                case DELETE_TEST -> {
                    sendMessage.setText("Please select a test to delete:");
                    List<String> deleteOptions = new ArrayList<>();
                    for (int i = 0; i < db.tests.size(); i++) {
                        deleteOptions.add("Question " + (i + 1));
                    }
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(new String[][]{deleteOptions.toArray(new String[0])}));
                    currentUser.setAddTestStep("DELETE_TEST");
                    botService.executeMessages(sendMessage);
                }

                case SHOW_TESTS -> {
                    StringBuilder testsList = new StringBuilder("Showing all tests:\n");
                    int questionNumber = 1;
                    for (Test test : db.tests) {
                        testsList.append("Question ").append(questionNumber++).append(": ").append(test.getQuestion()).append("\n");
                        List<Answer> answers = test.getAnswers();
                        for (int i = 0; i < answers.size(); i++) {
                            testsList.append("Answer ").append(i + 1).append(": ").append(answers.get(i).getVariant()).append("\n");
                        }
                        testsList.append("\n");
                    }
                    sendMessage.setText(testsList.toString());
                    botService.executeMessages(sendMessage);
                }
                default -> {
                    sendMessage.setText("Unknown command. Please use the provided options.");
                    botService.executeMessages(sendMessage);
                }
            }
        } else {
            // User Menu
            switch (text) {
                case "/start" -> {
                    sendMessage.setText("Welcome User!");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                    botService.executeMessages(sendMessage);
                }
                case INFO -> {
                    sendMessage.setText("""
                    This is an online test to check your knowledge about Java!
                    Creators are:
                    Aziza     ->  not username! 
                    Muxriddin ->  https://t.me/M07112003
                    Laziz     ->  https://t.me/lazizsamandarovv
                    Behruz    ->  https://t.me/bekhruz_a7s
                    Alisher   ->  https://t.me/alishernarzullayev01
                    Behruz    ->  https://t.me/bexhruz01
                    """);
                    botService.executeMessages(sendMessage);
                }
                case START_TEST -> {
                    sendMessage.setText("Are you ready?");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(new String[][]{{"Ready", "Back"}}));
                    botService.executeMessages(sendMessage);
                }
                case "Ready" -> {
                    ArrayList<Test> tests = new ArrayList<>();
                    Set<Integer> taskcount = new HashSet<>();
                    Random random = new Random();
                    while (taskcount.size() < 10) {
                        taskcount.add(random.nextInt(db.tests.size()));
                    }
                    for (Integer i : taskcount) {
                        tests.add(db.tests.get(i));
                    }
                    currentUser.setCurrentTest(tests);
                    currentUser.setCurrentQuestionIndex(0);
                    currentUser.setCorrectAnswersCount(0);
                    sendNextQuestion(currentUser);
                }
                case CONTACT_US -> {
                    sendMessage.setText("Please ask your question.");
                    botService.executeMessages(sendMessage);
                }
                default -> {
                    sendMessage.setText("Unknown command. Please use the provided options.");
                    botService.executeMessages(sendMessage);
                }
            }
        }
    }

    private void handleAddTestCallback(User user, String response) {
        switch (user.getAddTestStep()) {
            case "QUESTION":
                user.setCurrentTestQuestion(response);
                user.setAddTestStep("ANSWER1");
                sendMessage.setChatId(user.getId());
                sendMessage.setText("Please enter the first answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "ANSWER1":
                user.getCurrentTestAnswers().add(new Answer(response, false));
                user.setAddTestStep("ANSWER2");
                sendMessage.setChatId(user.getId());
                sendMessage.setText("Please enter the second answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "ANSWER2":
                user.getCurrentTestAnswers().add(new Answer(response, false));
                user.setAddTestStep("ANSWER3");
                sendMessage.setChatId(user.getId());
                sendMessage.setText("Please enter the third answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "ANSWER3":
                user.getCurrentTestAnswers().add(new Answer(response, false));
                user.setAddTestStep("ANSWER4");
                sendMessage.setChatId(user.getId());
                sendMessage.setText("Please enter the fourth answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "ANSWER4":
                user.getCurrentTestAnswers().add(new Answer(response, false));
                user.setAddTestStep("CORRECT");
                sendMessage.setChatId(user.getId());
                sendMessage.setText("Please specify the correct answer (1-4).");
                botService.executeMessages(sendMessage);
                break;
            case "CORRECT":
                int correctIndex;
                try {
                    correctIndex = Integer.parseInt(response) - 1;
                    if (correctIndex < 0 || correctIndex > 3) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    sendMessage.setChatId(user.getId());
                    sendMessage.setText("Invalid input. Please enter a number between 1 and 4.");
                    botService.executeMessages(sendMessage);
                    return;
                }
                List<Answer> answers = user.getCurrentTestAnswers();
                answers.get(correctIndex).setHasCorrect(true);
                Test newTest = new Test(user.getCurrentTestQuestion(), answers);
                db.tests.add(newTest);
                sendMessage.setChatId(user.getId());
                sendMessage.setText("Test added successfully!");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                botService.executeMessages(sendMessage);
                user.resetAddTestProcess();
                break;
            case "EDIT_TEST":
                if (user.getEditingTestIndex() == -1) {
                    // User is selecting a test to edit
                    try {
                        int testIndex = Integer.parseInt(response.substring("Question ".length())) - 1;
                        if (testIndex >= 0 && testIndex < db.tests.size()) {
                            // Valid test selection
                            Test testToEdit = db.tests.get(testIndex);
                            user.setEditingTestIndex(testIndex);
                            user.setAddTestStep("EDIT_QUESTION");

                            // Prepare a message to edit the question and answers
                            StringBuilder editMessage = new StringBuilder();
                            editMessage.append("Current question: ").append(testToEdit.getQuestion()).append("\n");
                            editMessage.append("Current answers:\n");
                            for (int i = 0; i < testToEdit.getAnswers().size(); i++) {
                                editMessage.append(i + 1).append(". ").append(testToEdit.getAnswers().get(i).getVariant()).append("\n");
                            }
                            editMessage.append("\nPlease enter the updated question:");

                            sendMessage.setChatId(user.getId());
                            sendMessage.setText(editMessage.toString());
                            botService.executeMessages(sendMessage);
                        } else {
                            // Invalid test selection
                            sendMessage.setText("Invalid test selection.");
                            botService.executeMessages(sendMessage);
                        }
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        // Invalid input format
                        sendMessage.setText("Invalid test selection.");
                        botService.executeMessages(sendMessage);
                    }
                } else if (user.getAddTestStep().equals("EDIT_QUESTION")) {
                    // User is editing the question
                    user.setCurrentTestQuestion(response);
                    user.setAddTestStep("EDIT_ANSWER1");

                    sendMessage.setChatId(user.getId());
                    sendMessage.setText("Please enter the first answer option.");
                    botService.executeMessages(sendMessage);
                } else if (user.getAddTestStep().startsWith("EDIT_ANSWER")) {
                    // User is editing the answers
                    try {
                        int answerIndex = Integer.parseInt(user.getAddTestStep().substring("EDIT_ANSWER".length())) - 1;
                        List<Answer> currentAnswers = user.getCurrentTestAnswers();

                        // Update current answer
                        currentAnswers.get(answerIndex).setVariant(response);

                        // Move to next answer or finish editing
                        int nextAnswerIndex = answerIndex + 1;
                        if (nextAnswerIndex < 4) {
                            user.setAddTestStep("EDIT_ANSWER" + (nextAnswerIndex + 1));
                            sendMessage.setChatId(user.getId());
                            sendMessage.setText("Please enter answer option " + (nextAnswerIndex + 1) + ".");
                            botService.executeMessages(sendMessage);
                        } else {
                            user.setAddTestStep("EDIT_CORRECT");
                            sendMessage.setChatId(user.getId());
                            sendMessage.setText("Please specify the correct answer (1-4).");
                            botService.executeMessages(sendMessage);
                        }
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        // Invalid input format
                        sendMessage.setText("Invalid input for answer option number. Please enter a valid number.");
                        botService.executeMessages(sendMessage);
                    }
                } else if (user.getAddTestStep().equals("EDIT_CORRECT")) {
                    // User is specifying the correct answer
                    try {
                        int correctIndex1 = Integer.parseInt(response) - 1;
                        if (correctIndex1 < 0 || correctIndex1 > 3) {
                            throw new NumberFormatException();
                        }

                        List<Answer> currentAnswers = user.getCurrentTestAnswers();
                        for (int i = 0; i < currentAnswers.size(); i++) {
                            currentAnswers.get(i).setHasCorrect(i == correctIndex1);
                        }

                        // Update test in database
                        int editingIndex = user.getEditingTestIndex();
                        Test editedTest = db.tests.get(editingIndex);
                        editedTest.setQuestion(user.getCurrentTestQuestion());
                        editedTest.setAnswers(currentAnswers);

                        sendMessage.setChatId(user.getId());
                        sendMessage.setText("Test updated successfully!");
                        sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                        botService.executeMessages(sendMessage);

                        // Reset edit state
                        user.resetAddTestProcess();
                        user.setEditingTestIndex(-1);
                    } catch (NumberFormatException e) {
                        // Invalid input for correct answer
                        sendMessage.setText("Invalid input. Please enter a number between 1 and 4.");
                        botService.executeMessages(sendMessage);
                    }
                }
                break;
            case "DELETE_TEST":
                try {
                    int testIndex = Integer.parseInt(response.substring("Question ".length())) - 1;
                    if (testIndex >= 0 && testIndex < db.tests.size()) {
                        Test deletedTest = db.tests.remove(testIndex);
                        sendMessage.setChatId(user.getId());
                        sendMessage.setText("Test \"" + deletedTest.getQuestion() + "\" deleted successfully!");
                        sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                        botService.executeMessages(sendMessage);
                    } else {
                        sendMessage.setText("Invalid test selection.");
                        botService.executeMessages(sendMessage);
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    sendMessage.setText("Invalid test selection.");
                    botService.executeMessages(sendMessage);
                }
                break;
            default:
                sendMessage.setChatId(user.getId());
                sendMessage.setText("Unknown command. Please use the provided options.");
                botService.executeMessages(sendMessage);
                break;
        }
    }

    private User getUserById(Long id) {
        for (User user : users) {
            if (user.getId().equals(id.toString())) {
                return user;
            }
        }
        return null;
    }

    public void callbackHandler(Update update) {
        Long id = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        User currentUser = getUserById(id);

        if (currentUser != null) {
            handleUserResponse(currentUser, callbackData);
        }
    }

    private void sendNextQuestion(User user) {
        int currentQuestionIndex = user.getCurrentQuestionIndex();
        if (currentQuestionIndex < user.getCurrentTest().size()) {
            Test currentQuestion = user.getCurrentTest().get(currentQuestionIndex);
            sendMessage.setChatId(user.getId());
            sendMessage.setText(currentQuestion.getQuestion());
            List<Answer> answers = currentQuestion.getAnswers();
            InlineString[][] options = new InlineString[answers.size()][1];
            for (int i = 0; i < answers.size(); i++) {
                options[i][0] = new InlineString(answers.get(i).getVariant(), answers.get(i).getVariant());
            }
            sendMessage.setReplyMarkup(inlineService.inlineMarkup(options));
            botService.executeMessages(sendMessage);
        } else {
            sendMessage.setChatId(user.getId());
            sendMessage.setText("You have completed the test! You answered " + user.getCorrectAnswersCount() + " out of " + user.getCurrentTest().size() + " questions correctly. Thank you for participating.");
            sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
            botService.executeMessages(sendMessage);
        }
    }

    private void handleUserResponse(User user, String response) {
        int currentQuestionIndex = user.getCurrentQuestionIndex();
        Test currentQuestion = user.getCurrentTest().get(currentQuestionIndex);
        List<Answer> answers = currentQuestion.getAnswers();

        boolean isCorrect = answers.stream()
                .anyMatch(answer -> answer.getVariant().equals(response) && answer.getHasCorrect());

        if (isCorrect) {
            user.setCorrectAnswersCount(user.getCorrectAnswersCount() + 1);
            sendMessage.setChatId(user.getId());
            sendMessage.setText("Correct answer!");
            sendMessage.setReplyMarkup(null);
            botService.executeMessages(sendMessage);
        } else {
            sendMessage.setChatId(user.getId());
            sendMessage.setText("Incorrect answer. The correct answer was: " +
                    answers.stream().filter(Answer::getHasCorrect).findFirst().get().getVariant());
            sendMessage.setReplyMarkup(null);
            botService.executeMessages(sendMessage);
        }

        user.setCurrentQuestionIndex(currentQuestionIndex + 1);
        sendNextQuestion(user);
    }

    private static BotLogicService botLogicService;
    public static BotLogicService getInstance() {
        if (botLogicService == null) {
            botLogicService = new BotLogicService();
        }
        return botLogicService;
    }
}
