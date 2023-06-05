package io.frankmayer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Error {

  private Error() {}

  public static void panic(final String message) {
    System.err.println(Error.prettyPrint(message));
    System.exit(message.hashCode() | 0b1);
  }

  public static void panic(final String message, final Throwable cause) {
    final var sw = new StringWriter();
    final var pw = new PrintWriter(sw);
    cause.printStackTrace(pw);
    System.err.println(Error.prettyPrint(message, cause.getMessage(), sw.toString()));
    System.exit(message.hashCode() | cause.hashCode() | 0b1);
  }

  public static void panic(final Throwable cause) {
    final var sw = new StringWriter();
    final var pw = new PrintWriter(sw);
    cause.printStackTrace(pw);
    System.err.println(Error.prettyPrint(cause.getMessage(), sw.toString()));
    System.exit(cause.hashCode() | 0b1);
  }

  /** Format the given messages into a single string with a border around it. */
  private static String prettyPrint(final String... messages) {
    return Arrays.stream(messages)
        .map(message -> RED + "■ " + message)
        .map(
            message -> Arrays.stream(message.split("\n")).collect(Collectors.joining("\n  " + RED)))
        .collect(Collectors.joining("\n"));
  }

  /** Color code for system terminal (Windows or Unix). */
  private static String RED =
      System.getProperty("os.name").startsWith("Windows")
          ? System.getenv("ESC") + "[31m"
          : "\033[31m";

  /** Color code for system terminal (Windows or Unix). */
  private static String RESET =
      System.getProperty("os.name").startsWith("Windows")
          ? System.getenv("ESC") + "[0m"
          : "\033[0m";
}
