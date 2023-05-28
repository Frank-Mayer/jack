package io.frankmayer;

import java.io.File;
import java.util.Optional;

import io.frankmayer.project.GradleProject;
import io.frankmayer.project.IntelliJProject;
import io.frankmayer.project.MavenProject;
import io.frankmayer.project.Project;

public class CommonUtils {

  private static Optional<File> projectFileCache = Optional.empty();
  private static Optional<Project> projectCache = Optional.empty();

  /**
   * Looks for project file like pom.xml or build.gradle in the current directory.
   *
   * <p>Goes up the directory tree until it finds a project file or the root directory.
   */
  public static Optional<File> getProjectFile() {
    if (CommonUtils.projectFileCache.isPresent()) {
      return CommonUtils.projectFileCache;
    }

    var currentDirectory = new File(System.getProperty("user.dir"));
    while (currentDirectory != null) {
      var projectFile = new File(currentDirectory, "pom.xml");
      if (projectFile.exists()) {
        CommonUtils.projectCache = Optional.of(new MavenProject(projectFile));
        return CommonUtils.projectFileCache = Optional.of(projectFile);
      }
      projectFile = new File(currentDirectory, "build.gradle");
      if (projectFile.exists()) {
        CommonUtils.projectCache = Optional.of(new GradleProject(projectFile));
        return CommonUtils.projectFileCache = Optional.of(projectFile);
      }
      for (final var file : currentDirectory.listFiles()) {
        if (file.getName().endsWith(".iml") && file.isFile()) {
          CommonUtils.projectCache = Optional.of(new IntelliJProject(file));
          return CommonUtils.projectFileCache = Optional.of(file);
        }
      }
      currentDirectory = currentDirectory.getParentFile();
    }
    return CommonUtils.projectFileCache;
  }

  /**
   * Tries to find the project for the current project.
   *
   * @return The project for the current project.
   */
  public static Optional<Project> getProject() {
    CommonUtils.getProjectFile();
    return CommonUtils.projectCache;
  }

  /**
   * Tries to get the current terminal width.
   *
   * @return The current terminal width. -1 if it could not be determined.
   */
  public static int getTerminalWidth() {
    // try env variable COLUMNS
    final var terminalWidth = System.getenv("COLUMNS");
    if (terminalWidth != null) {
      return Integer.parseInt(terminalWidth);
    }

    // try stty
    try {
      final var sttyProcess = new ProcessBuilder("stty", "size").start();
      final var sttyOutput = new java.io.BufferedReader(new java.io.InputStreamReader(sttyProcess.getInputStream()));
      final var sttyDimensions = sttyOutput.readLine().split(" ");
      return Integer.parseInt(sttyDimensions[1]);
    } catch (final Exception e) {
      // ignore
    }

    return -1;
  }

  /**
   * Tries to get the current terminal height.
   *
   * @return The current terminal height. -1 if it could not be determined.
   */
  public static int getTerminalHeight() {
    // try env variable LINES
    final var terminalHeight = System.getenv("LINES");
    if (terminalHeight != null) {
      return Integer.parseInt(terminalHeight);
    }

    // try stty
    try {
      final var sttyProcess = new ProcessBuilder("stty", "size").start();
      final var sttyOutput = new java.io.BufferedReader(new java.io.InputStreamReader(sttyProcess.getInputStream()));
      final var sttyDimensions = sttyOutput.readLine().split(" ");
      return Integer.parseInt(sttyDimensions[0]);
    } catch (final Exception e) {
      // ignore
    }

    return -1;
  }

  public static void setCursorPosition(final int x, final int y) {
    System.out.print("\033[" + y + ";" + x + "H");
  }

  private CommonUtils() {}
}