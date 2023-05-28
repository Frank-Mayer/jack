package io.frankmayer;

import static io.frankmayer.Error.panic;

import java.io.IOException;

public class JDB {

  private Process jdbProcess;

  public JDB(final int jdbPort) {
    try {
      final var procBuilder =
          new ProcessBuilder(
              "jdb", "-sourcepath", "./src/main/java", "-attach", String.valueOf(jdbPort));
      procBuilder.inheritIO();
      this.jdbProcess = procBuilder.start();
      try {
        this.jdbProcess.waitFor();
      } catch (final InterruptedException e) {
        panic("Error waiting for JDB process");
      }
    } catch (final IOException e) {
      panic("Error starting JDB process");
    }
  }
}