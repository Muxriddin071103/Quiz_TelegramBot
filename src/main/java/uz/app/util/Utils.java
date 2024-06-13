package uz.app.util;

public interface Utils {

    String START_TEST = "start test";
    String INFO = "info";
    String CONTACT_US = "contact us (COMING SOON...)";
    String HELP = "/help";
    String QUIT = "/stop";
    String START = "/start";
    String[][] userMenu = {
            {INFO, START_TEST},
            {CONTACT_US},
            {HELP,QUIT}
    };

    String ADD_TEST = "add test";
    String EDIT_TEST = "edit test";
    String DELETE_TEST = "delete test";
    String SHOW_TESTS = "show tests";
    String VIEW_USER_MESSAGES = "view user messages";
    String[][] adminMenu = {
            {ADD_TEST, EDIT_TEST},
            {DELETE_TEST, SHOW_TESTS},
            {HELP,QUIT}
    };


}
