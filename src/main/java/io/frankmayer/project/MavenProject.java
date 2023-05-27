package io.frankmayer.project;

import static io.frankmayer.Error.panic;

import io.frankmayer.JDB;
import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

public class MavenProject extends Project {

  private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder builder;
  private Document document;

  public MavenProject(final File projectFile) {
    super(projectFile);
    if (MavenProject.builder == null) {
      try {
        MavenProject.builder = MavenProject.factory.newDocumentBuilder();
      } catch (final ParserConfigurationException e) {
        panic(e);
      }
    }
    try {
      this.document = MavenProject.builder.parse(projectFile);
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public Optional<String> getDefaultClassName() {
    // look for project.build.plugins.plugin with groupId =
    // org.codehaus.mojo and artifactId = exec-maven-plugin in this.document
    final var plugins = this.document.getElementsByTagName("plugin");
    for (var i = 0; i < plugins.getLength(); i++) {
      try {
        final var plugin = plugins.item(i);
        final var groupId = plugin.getChildNodes().item(0).getTextContent();
        final var artifactId = plugin.getChildNodes().item(1).getTextContent();
        if (groupId.equals("org.codehaus.mojo") && artifactId.equals("exec-maven-plugin")) {
          // look for configuration -> mainClass
          final var configuration = plugin.getChildNodes().item(2);
          final var mainClass = configuration.getChildNodes().item(0).getTextContent();
          return Optional.of(mainClass);
        }
      } catch (final Exception e) {
        continue;
      }
    }
    return Optional.empty();
  }

  @Override
  public void build() {
    final var processBuilder =
        new ProcessBuilder("mvn", "-f", this.projectFile.toString(), "compile");
    processBuilder.directory(this.projectFile.getParentFile());
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public void clean() {
    final var processBuilder =
        new ProcessBuilder("mvn", "-f", this.projectFile.toString(), "clean");
    processBuilder.directory(this.projectFile.getParentFile());
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public void run() {
    final var processBuilder =
        new ProcessBuilder("mvn", "-f", this.projectFile.toString(), "compile", "exec:java");
    processBuilder.directory(this.projectFile.getParentFile());
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public void run(final String className) {
    final var processBuilder =
        new ProcessBuilder(
            "mvn",
            "-f",
            this.projectFile.toString(),
            "compile",
            "exec:java",
            "-Dexec.mainClass=" + className);
    processBuilder.directory(this.projectFile.getParentFile());
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public void debug() {
    final var defaultClassName = this.getDefaultClassName();
    if (!defaultClassName.isPresent()) {
      panic("No default class name found.");
    }
    this.debug(defaultClassName.get());
  }

  @Override
  public void debug(final String className) {
    try {
      final int jdbPort = 5005;

      final var compileProcessBuilder =
          new ProcessBuilder(
              "mvn", "-f", this.projectFile.toString(), "compile", "-Dmaven.compiler.debug=true");
      final var compileProcess = compileProcessBuilder.start();
      try {
        compileProcess.waitFor();
      } catch (final Exception e) {
        panic(e);
      }

      final var appProcessBuilder =
          new ProcessBuilder(
              "java",
              "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + jdbPort,
              "-cp",
              "target/classes",
              className);
      appProcessBuilder.directory(this.projectFile.getParentFile());
      final var appProcess = appProcessBuilder.start();

      Thread.sleep(1000);

      new JDB(jdbPort);
      appProcess.waitFor();
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public String getSourcePath() {
    final var projectRoot = this.projectFile.getParentFile();
    final var srcDir = new File(projectRoot, Paths.get("src", "main", "java").toString());
    final var srcDirPath = srcDir.getAbsolutePath();
    return srcDirPath;
  }
}