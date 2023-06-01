package io.frankmayer;

import static io.frankmayer.Error.panic;

import io.frankmayer.project.GradleProject;
import io.frankmayer.project.IntelliJProject;
import io.frankmayer.project.MavenProject;
import io.frankmayer.project.Project;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CommonUtils {

  private static final Scanner scanner = new Scanner(System.in);
  private static Optional<File> projectFileCache = Optional.empty();
  private static Optional<Project> projectCache = Optional.empty();

  private CommonUtils() {}

  public static String fixJavaPackageName(final String packageName) {
    return Arrays.stream(packageName.split("\\."))
        .map(CommonUtils::fixJavaArtifactName)
        .filter(x -> !x.isEmpty())
        .reduce((x, y) -> x + "." + y)
        .orElse("");
  }

  public static String fixJavaArtifactName(final String artifactName) {
    return artifactName
        .replaceAll("[-]+", "_")
        .replaceAll("[^a-zA-Z0-9_]", "")
        .replaceAll("^(?=[0-9])", "_");
  }

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
      if (projectFile.exists() && projectFile.isFile()) {
        CommonUtils.projectCache = Optional.of(new MavenProject(projectFile));
        return CommonUtils.projectFileCache = Optional.of(projectFile);
      }
      projectFile = new File(currentDirectory, "build.gradle");
      if (projectFile.exists() && projectFile.isFile()) {
        CommonUtils.projectCache = Optional.of(new GradleProject(projectFile));
        return CommonUtils.projectFileCache = Optional.of(projectFile);
      }
      for (final var file : currentDirectory.listFiles()) {
        if (file.getName().equals(".idea") && file.isDirectory()) {
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
      final var sttyOutput =
          new BufferedReader(new InputStreamReader(sttyProcess.getInputStream()));
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
      final var sttyOutput =
          new BufferedReader(new InputStreamReader(sttyProcess.getInputStream()));
      final var sttyDimensions = sttyOutput.readLine().split(" ");
      return Integer.parseInt(sttyDimensions[0]);
    } catch (final Exception e) {
      // ignore
    }

    return -1;
  }

  public static void setCursorPosition(final int x, final int y) {
    System.out.print("\033[" + y + ';' + x + 'H');
  }

  public static String askString(final String string) {
    System.out.print(string + ": ");
    return CommonUtils.scanner.nextLine().trim();
  }

  public static String askString(final String string, final String defaultValue) {
    System.out.print(string + " [" + defaultValue + "]: ");
    final var input = CommonUtils.scanner.nextLine().trim();
    return input.isEmpty() ? defaultValue : input;
  }

  public static boolean confirm(final String string, final boolean defaultValue) {
    while (true) {
      if (defaultValue) {
        System.out.print(string + " [Y/n]: ");
      } else {
        System.out.print(string + " [y/N]: ");
      }
      final var input = CommonUtils.scanner.nextLine().trim();
      switch (input.trim().toLowerCase()) {
        case "y":
        case "yes":
          return true;
        case "n":
        case "no":
          return false;
        case "":
          return defaultValue;
        default:
          System.out.println("Please answer with yes or no");
      }
    }
  }

  public static boolean confirm(final String string) {
    while (true) {
      System.out.print(string + " [y/n]: ");
      final var input = CommonUtils.scanner.nextLine().trim();
      switch (input.trim().toLowerCase()) {
        case "y":
        case "yes":
          return true;
        case "n":
        case "no":
          return false;
        default:
          System.out.println("Please answer with yes or no");
      }
    }
  }

  public static String getJavaMajorVersion() {
    try {
      final var javaVersion = System.getProperty("java.version");
      final var javaVersionParts = javaVersion.split("\\.");
      return javaVersionParts[0];
    } catch (final Exception e) {
      panic("Could not determine Java version", e);
      return null;
    }
  }

  public static boolean containsElement(final Node parent, final String tagName) {
    return CommonUtils.stream(parent.getChildNodes())
        .anyMatch(node -> node.getNodeName().equals(tagName));
  }

  public static Optional<Node> findChild(final Node parent, final String tagName) {
    return CommonUtils.stream(parent.getChildNodes())
        .filter(node -> node.getNodeName().equals(tagName))
        .findFirst();
  }

  /** Find a child Node with a given testing function */
  public static Optional<Node> findChild(
      final Node parent, final Predicate<Node> test, final boolean deep) {
    if (deep) {
      return CommonUtils.stream(parent.getChildNodes())
          .filter(test)
          .flatMap(
              node ->
                  Stream.concat(
                      Stream.of(node), CommonUtils.findChildren(node, test, true).limit(1)))
          .findFirst();
    }
    return CommonUtils.stream(parent.getChildNodes()).filter(test).findFirst();
  }

  /** Find a child Node with a given testing function */
  public static Optional<Node> findChild(final Node parent, final Predicate<Node> test) {
    return CommonUtils.findChild(parent, test, true);
  }

  public static Stream<Node> findChildren(final Node parent, final String tagName) {
    return CommonUtils.findChildren(parent, tagName, true);
  }

  public static Stream<Node> findChildren(final Node parent, final Predicate<Node> test) {
    return CommonUtils.findChildren(parent, test, true);
  }

  public static Stream<Node> findChildren(
      final Node parent, final Predicate<Node> test, final boolean deep) {
    if (deep) {
      return CommonUtils.stream(parent.getChildNodes())
          .filter(test)
          .flatMap(
              node -> Stream.concat(Stream.of(node), CommonUtils.findChildren(node, test, true)));
    } else {
      return CommonUtils.stream(parent.getChildNodes()).filter(test);
    }
  }

  public static Stream<Node> stream(final NodeList nodeList) {
    return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item);
  }

  /** Returns the attribute value of the given node with the given name. */
  public static Optional<String> getAttribute(final Node x, final String string) {
    try {
      return Optional.ofNullable(x.getAttributes().getNamedItem(string).getNodeValue());
    } catch (final Exception ignore) {
      return Optional.empty();
    }
  }

  /** Find all files with a given name */
  public static Stream<Path> findFiles(final File root, final String name) {
    try {
      return Files.walk(root.toPath()).filter(path -> path.getFileName().toString().equals(name));
    } catch (final IOException e) {
      return Stream.empty();
    }
  }

  public static Stream<Path> findFiles(final File root, final String name, final String ext) {
    return CommonUtils.findFiles(root, name + "." + ext);
  }

  private static Stream<Node> findChildren(Node parent, String tagName, boolean deep) {
    final var test = (Predicate<Node>) node -> node.getNodeName().equals(tagName);
    return CommonUtils.findChildren(parent, test, deep);
  }

  /** ; for windows, : for unix */
  public static char getPathSeparator() {
    return System.getProperty("os.name").toLowerCase().contains("windows") ? ';' : ':';
  }
}
