package io.frankmayer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Error {

  private Error() {}

  public static final void panic(final String message) {
    System.err.println(Error.prettyPrint(message));
    System.exit(message.hashCode() | 0b1);
  }

  public static final void panic(final String message, final Throwable cause) {
    final var sw = new StringWriter();
    final var pw = new PrintWriter(sw);
    cause.printStackTrace(pw);
    System.err.println(Error.prettyPrint(message, cause.getMessage(), sw.toString()));
    System.exit(message.hashCode() | cause.hashCode() | 0b1);
  }

  public static final void panic(final Throwable cause) {
    final var sw = new StringWriter();
    final var pw = new PrintWriter(sw);
    cause.printStackTrace(pw);
    System.err.println(Error.prettyPrint(cause.getMessage(), sw.toString()));
    System.exit(cause.hashCode() | 0b1);
  }

  /** Format the given messages into a single string with a border around it. */
  private static final String prettyPrint(final String... messages) {
    return Arrays.stream(messages).collect(Collectors.joining("\n"));
  }
}
