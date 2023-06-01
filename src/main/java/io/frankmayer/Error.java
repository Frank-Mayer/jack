package io.frankmayer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Error {

  private Error() {}

  public static void panic(final String message) {
    System.err.println(Error.prettyPrint(message));
    System.exit(message.hashCode());
  }

  public static void panic(final String message, final Throwable cause) {
    final var sw = new StringWriter();
    final var pw = new PrintWriter(sw);
    cause.printStackTrace(pw);
    System.err.println(Error.prettyPrint(message, cause.getMessage(), sw.toString()));
    System.exit(message.hashCode());
  }

  public static void panic(final Throwable cause) {
    final var sw = new StringWriter();
    final var pw = new PrintWriter(sw);
    cause.printStackTrace(pw);
    System.err.println(Error.prettyPrint(cause.getMessage(), sw.toString()));
    System.exit(cause.hashCode());
  }

  /** Format the given messages into a single string with a border around it. */
  private static String prettyPrint(final String... messages) {
    var longestLine = 0;
    final var textBlocks =
        Arrays.stream(messages)
            .map(message -> message.replaceAll("\t", "    "))
            .collect(Collectors.toList());
    final var sb = new StringBuilder();
    for (final var message : textBlocks) {
      final var lines = message.split("\n");
      for (final var line : lines) {
        if (line.length() > longestLine) {
          longestLine = line.length();
        }
      }
    }
    sb.append('╔');
    for (var i = 0; i < longestLine; ++i) {
      sb.append('═');
    }
    var blockCount = 0;
    sb.append("╗\n");
    for (final var message : textBlocks) {
      final var lines = message.split("\n");
      for (final var line : lines) {
        sb.append('║');
        sb.append(line);
        for (var i = line.length(); i < longestLine; ++i) {
          sb.append(' ');
        }
        sb.append("║\n");
      }
      if (++blockCount < textBlocks.size()) {
        sb.append('╠');
        for (var i = 0; i < longestLine; ++i) {
          sb.append('═');
        }
        sb.append("╣\n");
      }
    }
    sb.append('╚');
    for (var i = 0; i < longestLine; ++i) {
      sb.append('═');
    }
    sb.append("╝\n");
    return sb.toString();
  }
}
