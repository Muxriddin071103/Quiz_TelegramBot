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
            users.add(new User(id.toString(), "main", new ArrayList<>(), 0, 0, null, new ArrayList<>(),null,-1,0));
            currentUser = getUserById(id);
        }

        if (currentUser.getId().equals(adminId)) {
            if (currentUser.getAddTestStep() != null) {
                handleTestCallback(currentUser, text);
                return;
            }

            switch (text) {
                // Admin Menu
                case QUIT -> {
                    sendMessage.setText("Stopping bot...\n Bot stopped! Goodbye!!!");
                    botService.executeMessages(sendMessage);
                    System.exit(0);
                }
                case HELP -> {
                    sendMessage.setText("Admin Help:\n" +
                            "/start      -> Start the bot\n" +
                            "ADD TEST    -> Add a new test\n" +
                            "EDIT TEST   -> Edit an existing test\n" +
                            "DELETE TEST -> Delete an existing test\n" +
                            "SHOW TESTS  -> Show all tests\n" +
                            "/stop       -> Stop the bot\n" +
                            "/help       -> Display this help message");
                    botService.executeMessages(sendMessage);
                }
                case START -> {
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
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(createTwoColumnKeyboard(testOptions)));
                    currentUser.setAddTestStep("SELECT_EDIT_TEST");
                    botService.executeMessages(sendMessage);
                }
                case DELETE_TEST -> {
                    sendMessage.setText("Please select a test to delete:");
                    List<String> deleteOptions = new ArrayList<>();
                    for (int i = 0; i < db.tests.size(); i++) {
                        deleteOptions.add("Question " + (i + 1));
                    }
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(createTwoColumnKeyboard(deleteOptions)));
                    currentUser.setAddTestStep("SELECT_DELETE_TEST");
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
                case QUIT -> {
                    sendMessage.setText("Stopping bot...\n Bot stopped! Goodbye!!!");
                    botService.executeMessages(sendMessage);
                    System.exit(0);
                }
                case HELP -> {
                    sendMessage.setText("User Help:\n" +
                            "/start     -> Start the bot\n" +
                            "INFO       -> Information about the bot\n" +
                            "START TEST -> Start a new test\n" +
                            "CONTACT US -> Contact the admin\n" +
                            "/stop      -> Stop user commands\n" +
                            "/help      -> Display this help message");
                    botService.executeMessages(sendMessage);
                }
                case START -> {
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
                    sendMessage.setText("Please send your message to the admin.");
                    // Need logic to send message to admin
                    botService.executeMessages(sendMessage);
                }
                default -> {
                    sendMessage.setText("Unknown command. Please use the provided options.");
                    botService.executeMessages(sendMessage);
                }
            }
        }
    }

    private String[][] createTwoColumnKeyboard(List<String> options) {
        int rows = (int) Math.ceil(options.size() / 2.0);
        String[][] keyboard = new String[rows][2];

        for (int i = 0; i < options.size(); i++) {
            keyboard[i / 2][i % 2] = options.get(i);
        }

        return keyboard;
    }

    private void handleTestCallback(User currentUser, String response) {
        SendMessage sendMessage = new SendMessage();
        switch (currentUser.getAddTestStep()) {
            case "QUESTION":
                currentUser.setCurrentTestQuestion(response);
                currentUser.setAddTestStep("ANSWER1");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please enter the first answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "ANSWER1":
                currentUser.getCurrentTestAnswers().add(new Answer(response, false));
                currentUser.setAddTestStep("ANSWER2");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please enter the second answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "ANSWER2":
                currentUser.getCurrentTestAnswers().add(new Answer(response, false));
                currentUser.setAddTestStep("ANSWER3");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please enter the third answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "ANSWER3":
                currentUser.getCurrentTestAnswers().add(new Answer(response, false));
                currentUser.setAddTestStep("ANSWER4");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please enter the fourth answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "ANSWER4":
                currentUser.getCurrentTestAnswers().add(new Answer(response, false));
                currentUser.setAddTestStep("CORRECT");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please specify the correct answer (1-4).");
                botService.executeMessages(sendMessage);
                break;
            case "CORRECT":
                try {
                    int correctIndex = Integer.parseInt(response) - 1;
                    if (correctIndex < 0 || correctIndex > 3) {
                        throw new NumberFormatException();
                    }
                    currentUser.getCurrentTestAnswers().get(correctIndex).setHasCorrect(true);
                    Test newTest = new Test(currentUser.getCurrentTestQuestion(), currentUser.getCurrentTestAnswers());
                    db.tests.add(newTest);
                    sendMessage.setChatId(currentUser.getId());
                    sendMessage.setText("Test added successfully!");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                    botService.executeMessages(sendMessage);
                    currentUser.resetAddTestProcess();
                } catch (NumberFormatException e) {
                    sendMessage.setChatId(currentUser.getId());
                    sendMessage.setText("Invalid input. Please enter a number between 1 and 4.");
                    botService.executeMessages(sendMessage);
                }
                break;
            case "SELECT_EDIT_TEST":
                try {
                    int selectedTestIndex = Integer.parseInt(response.split(" ")[1]) - 1;
                    if (selectedTestIndex >= 0 && selectedTestIndex < db.tests.size()) {
                        currentUser.setCurrentTestQuestion(db.tests.get(selectedTestIndex).getQuestion());
                        currentUser.setCurrentTestAnswers(new ArrayList<>(db.tests.get(selectedTestIndex).getAnswers()));
                        currentUser.setAddTestStep("EDIT_QUESTION");
                        currentUser.setEditingTestIndex(selectedTestIndex);
                        sendMessage.setChatId(currentUser.getId());
                        sendMessage.setText("Editing Test - Please enter the new question:");
                        botService.executeMessages(sendMessage);
                    } else {
                        throw new IndexOutOfBoundsException();
                    }
                } catch (Exception e) {
                    sendMessage.setChatId(currentUser.getId());
                    sendMessage.setText("Invalid selection. Please select a valid test.");
                    botService.executeMessages(sendMessage);
                }
                break;
            case "EDIT_QUESTION":
                currentUser.setCurrentTestQuestion(response);
                currentUser.setAddTestStep("EDIT_ANSWER1");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please enter the new first answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "EDIT_ANSWER1":
                currentUser.getCurrentTestAnswers().set(0, new Answer(response, currentUser.getCurrentTestAnswers().get(0).getHasCorrect()));
                currentUser.setAddTestStep("EDIT_ANSWER2");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please enter the new second answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "EDIT_ANSWER2":
                currentUser.getCurrentTestAnswers().set(1, new Answer(response, currentUser.getCurrentTestAnswers().get(1).getHasCorrect()));
                currentUser.setAddTestStep("EDIT_ANSWER3");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please enter the new third answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "EDIT_ANSWER3":
                currentUser.getCurrentTestAnswers().set(2, new Answer(response, currentUser.getCurrentTestAnswers().get(2).getHasCorrect()));
                currentUser.setAddTestStep("EDIT_ANSWER4");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please enter the new fourth answer option.");
                botService.executeMessages(sendMessage);
                break;
            case "EDIT_ANSWER4":
                currentUser.getCurrentTestAnswers().set(3, new Answer(response, currentUser.getCurrentTestAnswers().get(3).getHasCorrect()));
                currentUser.setAddTestStep("EDIT_CORRECT");
                sendMessage.setChatId(currentUser.getId());
                sendMessage.setText("Please specify the correct answer (1-4).");
                botService.executeMessages(sendMessage);
                break;
            case "EDIT_CORRECT":
                try {
                    int correctAnswerIndex = Integer.parseInt(response) - 1;
                    if (correctAnswerIndex < 0 || correctAnswerIndex > 3) {
                        throw new NumberFormatException();
                    }
                    for (Answer answer : currentUser.getCurrentTestAnswers()) {
                        answer.setHasCorrect(false);
                    }
                    currentUser.getCurrentTestAnswers().get(correctAnswerIndex).setHasCorrect(true);
                    int editingTestIndex = currentUser.getEditingTestIndex();
                    Test editingTest = db.tests.get(editingTestIndex);
                    editingTest.setQuestion(currentUser.getCurrentTestQuestion());
                    editingTest.setAnswers(new ArrayList<>(currentUser.getCurrentTestAnswers()));
                    sendMessage.setChatId(currentUser.getId());
                    sendMessage.setText("Test updated successfully!");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                    botService.executeMessages(sendMessage);
                    currentUser.resetAddTestProcess();
                } catch (NumberFormatException e) {
                    sendMessage.setChatId(currentUser.getId());
                    sendMessage.setText("Invalid input. Please enter a number between 1 and 4.");
                    botService.executeMessages(sendMessage);
                }
                break;
            case "SELECT_DELETE_TEST":
                try {
                    int selectedTestIndex = Integer.parseInt(response.split(" ")[1]) - 1;
                    if (selectedTestIndex >= 0 && selectedTestIndex < db.tests.size()) {
                        db.tests.remove(selectedTestIndex);
                        sendMessage.setChatId(currentUser.getId());
                        sendMessage.setText("Test deleted successfully!");
                        sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                        botService.executeMessages(sendMessage);
                        currentUser.resetAddTestProcess();
                    } else {
                        throw new IndexOutOfBoundsException();
                    }
                } catch (Exception e) {
                    sendMessage.setChatId(currentUser.getId());
                    sendMessage.setText("Invalid selection. Please select a valid test.");
                    botService.executeMessages(sendMessage);
                }
                break;
            default:
                sendMessage.setChatId(currentUser.getId());
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
