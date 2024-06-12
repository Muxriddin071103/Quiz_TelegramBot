package uz.app.db;

import uz.app.entity.Answer;
import uz.app.entity.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class Db {
   public ArrayList<Test> tests = new ArrayList<>();

   public Db() {
      tests.add(new Test("What is the size of int in Java?",
              Arrays.asList(
                      new Answer("4 bytes", true),
                      new Answer("2 bytes", false),
                      new Answer("8 bytes", false),
                      new Answer("16 bytes", false)
              )));

      tests.add(new Test("Which of the following is not a Java keyword?",
              Arrays.asList(
                      new Answer("static", false),
                      new Answer("Boolean", true),
                      new Answer("void", false),
                      new Answer("class", false)
              )));

      tests.add(new Test("Which company used and updated Java?",
              Arrays.asList(
                      new Answer("Sun Microsystems", false),
                      new Answer("Microsoft", false),
                      new Answer("Google", false),
                      new Answer("Oracle", true)
              )));

      tests.add(new Test("Which method is used to start a thread in Java?",
              Arrays.asList(
                      new Answer("start()", true),
                      new Answer("run()", false),
                      new Answer("init()", false),
                      new Answer("begin()", false)
              )));

      tests.add(new Test("What is the default value of a boolean variable in Java?",
              Arrays.asList(
                      new Answer("false", true),
                      new Answer("true", false),
                      new Answer("0", false),
                      new Answer("null", false)
              )));

      tests.add(new Test("Which of these is a checked exception in Java?",
              Arrays.asList(
                      new Answer("IOException", true),
                      new Answer("ArrayIndexOutOfBoundsException", false),
                      new Answer("NullPointerException", false),
                      new Answer("ClassCastException", false)
              )));

      tests.add(new Test("Which of the following is used to find and fix bugs in the Java programs?",
              Arrays.asList(
                      new Answer("JDB", true),
                      new Answer("JVM", false),
                      new Answer("JRE", false),
                      new Answer("JDK", false)
              )));

      tests.add(new Test("Which package contains the Random class?",
              Arrays.asList(
                      new Answer("java.util", true),
                      new Answer("java.lang", false),
                      new Answer("java.io", false),
                      new Answer("java.net", false)
              )));

      tests.add(new Test("Which of these cannot be used for a variable name in Java?",
              Arrays.asList(
                      new Answer("keyword", true),
                      new Answer("identifier", false),
                      new Answer("variable", false),
                      new Answer("function", false)
              )));

      tests.add(new Test("Which of these classes are part of Java Collection Framework?",
              Arrays.asList(
                      new Answer("HashSet", true),
                      new Answer("Array", false),
                      new Answer("Object", false),
                      new Answer("String", false)
              )));
   }
}
