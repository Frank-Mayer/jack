package io.frankmayer;

import static io.frankmayer.Error.panic;

import io.frankmayer.project.Project;
import java.io.IOException;

public class JDB {

  public static final int getPort() {
    return 5005;
  }

  private Process jdbProcess;

  public JDB(final int jdbPort, final Project project) {
    try {
      final var procBuilder =
          new ProcessBuilder(
              "jdb", "-sourcepath", project.getSourcePath(), "-attach", String.valueOf(jdbPort));
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
