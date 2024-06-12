package uz.app.util;

public interface Utils {

    String START_TEST = "start test";
    String INFO = "info";
    String CONTACT_US = "contact us";
    String[][] userMenu = {
            {INFO, START_TEST},
            {CONTACT_US}
    };

    String ADD_TEST = "add test";
    String EDIT_TEST = "edit test";
    String DELETE_TEST = "delete test";
    String SHOW_TESTS = "show tests";
    String[][] adminMenu = {
            {ADD_TEST, EDIT_TEST},
            {DELETE_TEST, SHOW_TESTS}
    };


}
