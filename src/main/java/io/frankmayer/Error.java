package io.frankmayer;

public class Error {

  public static void panic(final String message) {
    System.err.println(Error.prettyPrint(message));
    System.exit(message.hashCode());
  }

  public static void panic(final String message, final Throwable cause) {
    System.err.println(Error.prettyPrint(message, cause.getMessage()));
    System.exit(message.hashCode());
  }

  public static void panic(final Throwable cause) {
    System.err.println(Error.prettyPrint(cause.getMessage()));
    System.exit(cause.hashCode());
  }

  /** Format the given messages into a single string with a border around it. */
  private static String prettyPrint(final String... messages) {
    final StringBuilder sb = new StringBuilder();
    var longestLine = 4;
    for (final String message : messages) {
      for (final String line : message.split("\n")) {
        if (line.length() > longestLine) {
          longestLine = line.length();
        }
      }
    }
    sb.append('╔');
    for (int i = -1; i <= longestLine; ++i) {
      sb.append('═');
    }
    sb.append('╗');
    sb.append('\n');
    for (final String message : messages) {
      for (final String line : message.split("\n")) {
        sb.append("║ ");
        sb.append(line);
        for (int i = line.length(); i < longestLine; ++i) {
          sb.append(' ');
        }
        sb.append(" ║\n");
      }
    }
    sb.append('╚');
    for (int i = -1; i <= longestLine; ++i) {
      sb.append('═');
    }
    sb.append('╝');

    return sb.toString();
  }

  private Error() {}
}