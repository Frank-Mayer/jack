package io.frankmayer.project;

import static io.frankmayer.Error.panic;

import io.frankmayer.CommonUtils;
import io.frankmayer.JDB;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
      this.document = MavenProject.builder.parse(this.projectFile);
    } catch (final Exception e) {
      panic(e);
    }
  }

  public static void create() {
    // check if pom.xml exists
    final var pomXmlFile = new File("pom.xml");

    if (MavenProject.builder == null) {
      try {
        MavenProject.builder = MavenProject.factory.newDocumentBuilder();
      } catch (final ParserConfigurationException e) {
        panic(e);
      }
    }

    // create pom.xml document
    final var pomXmlDocument = MavenProject.builder.newDocument();
    pomXmlDocument.setXmlStandalone(true);
    pomXmlDocument.setXmlVersion("1.0");
    pomXmlDocument.setDocumentURI("http://maven.apache.org/POM/4.0.0");
    pomXmlDocument.setStrictErrorChecking(true);

    // create root element
    final var projectElement = pomXmlDocument.createElement("project");
    projectElement.setAttribute("xmlns", "http://maven.apache.org/POM/4.0.0");
    projectElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    projectElement.setAttribute(
        "xsi:schemaLocation",
        "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
    pomXmlDocument.appendChild(projectElement);

    // create modelVersion element
    final var modelVersionElement = pomXmlDocument.createElement("modelVersion");
    modelVersionElement.setTextContent("4.0.0");
    projectElement.appendChild(modelVersionElement);

    // create groupId element
    final var groupIdElement = pomXmlDocument.createElement("groupId");
    final var groupId = CommonUtils.fixJavaPackageName(CommonUtils.askString("groupId"));
    groupIdElement.setTextContent(groupId);
    projectElement.appendChild(groupIdElement);

    // create artifactId element
    final var artifactIdElement = pomXmlDocument.createElement("artifactId");
    final var artifactId = CommonUtils.fixJavaArtifactName(CommonUtils.askString("artifactId"));
    artifactIdElement.setTextContent(artifactId);
    projectElement.appendChild(artifactIdElement);

    // create version element
    final var versionElement = pomXmlDocument.createElement("version");
    versionElement.setTextContent(CommonUtils.askString("version", "1.0-SNAPSHOT"));
    projectElement.appendChild(versionElement);

    // create properties element
    final var propertiesElement = pomXmlDocument.createElement("properties");
    projectElement.appendChild(propertiesElement);

    // create project.properties.maven.compiler.source element
    final var compilerSourceElement = pomXmlDocument.createElement("maven.compiler.source");
    final var javaSourceVersion =
        CommonUtils.askString("source", CommonUtils.getJavaMajorVersion());
    compilerSourceElement.setTextContent(javaSourceVersion);
    propertiesElement.appendChild(compilerSourceElement);

    // create project.properties.maven.compiler.target element
    final var compilerTargetElement = pomXmlDocument.createElement("maven.compiler.target");
    compilerTargetElement.setTextContent(CommonUtils.askString("target", javaSourceVersion));
    propertiesElement.appendChild(compilerTargetElement);

    // create project.build.sourceEncoding element
    final var sourceEncodingElement = pomXmlDocument.createElement("project.build.sourceEncoding");
    sourceEncodingElement.setTextContent("UTF-8");
    propertiesElement.appendChild(sourceEncodingElement);

    // create build element
    final var buildElement = pomXmlDocument.createElement("build");
    projectElement.appendChild(buildElement);

    // create build.finalName element
    final var finalNameElement = pomXmlDocument.createElement("finalName");
    finalNameElement.setTextContent(artifactId);
    buildElement.appendChild(finalNameElement);

    // create build.plugins element
    final var pluginsElement = pomXmlDocument.createElement("plugins");
    buildElement.appendChild(pluginsElement);

    // create build.plugins.plugin element for exec-maven-plugin
    final var mojoPluginElement = pomXmlDocument.createElement("plugin");
    pluginsElement.appendChild(mojoPluginElement);

    // create build.plugins.plugin.groupId element
    final var mojoPluginGroupIdElement = pomXmlDocument.createElement("groupId");
    mojoPluginGroupIdElement.setTextContent("org.codehaus.mojo");
    mojoPluginElement.appendChild(mojoPluginGroupIdElement);

    // create build.plugins.plugin.artifactId element
    final var mojoPluginArtifactIdElement = pomXmlDocument.createElement("artifactId");
    mojoPluginArtifactIdElement.setTextContent("exec-maven-plugin");
    mojoPluginElement.appendChild(mojoPluginArtifactIdElement);

    // create build.plugins.plugin.version element
    final var mojoPluginVersionElement = pomXmlDocument.createElement("version");
    mojoPluginVersionElement.setTextContent("3.1.0");
    mojoPluginElement.appendChild(mojoPluginVersionElement);

    // create build.plugins.plugin.configuration element
    final var mojoPluginConfigurationElement = pomXmlDocument.createElement("configuration");
    mojoPluginElement.appendChild(mojoPluginConfigurationElement);

    // create build.plugins.plugin.configuration.mainClass element
    final var mojoPluginMainClassElement = pomXmlDocument.createElement("mainClass");
    mojoPluginMainClassElement.setTextContent(groupId + ".Main");
    mojoPluginConfigurationElement.appendChild(mojoPluginMainClassElement);

    // create build.plugins.plugin element for maven-jar-plugin
    final var jarPluginElement = pomXmlDocument.createElement("plugin");
    pluginsElement.appendChild(jarPluginElement);

    // create build.plugins.plugin.groupId element
    final var jarPluginGroupIdElement = pomXmlDocument.createElement("groupId");
    jarPluginGroupIdElement.setTextContent("org.apache.maven.plugins");
    jarPluginElement.appendChild(jarPluginGroupIdElement);

    // create build.plugins.plugin.artifactId element
    final var jarPluginArtifactIdElement = pomXmlDocument.createElement("artifactId");
    jarPluginArtifactIdElement.setTextContent("maven-jar-plugin");
    jarPluginElement.appendChild(jarPluginArtifactIdElement);

    // create build.plugins.plugin.version element
    final var jarPluginVersionElement = pomXmlDocument.createElement("version");
    jarPluginVersionElement.setTextContent("3.4.1");
    jarPluginElement.appendChild(jarPluginVersionElement);

    // create build.plugins.plugin.executions element
    final var jarPluginExecutionsElement = pomXmlDocument.createElement("executions");
    jarPluginElement.appendChild(jarPluginExecutionsElement);

    // create build.plugins.plugin.executions.execution element
    final var jarPluginExecutionElement = pomXmlDocument.createElement("execution");
    jarPluginExecutionsElement.appendChild(jarPluginExecutionElement);

    // create build.plugins.plugin.executions.execution.phase element
    final var jarPluginExecutionPhaseElement = pomXmlDocument.createElement("phase");
    jarPluginExecutionPhaseElement.setTextContent("package");
    jarPluginExecutionElement.appendChild(jarPluginExecutionPhaseElement);

    // create build.plugins.plugin.executions.execution.goals element
    final var jarPluginExecutionGoalsElement = pomXmlDocument.createElement("goals");
    jarPluginExecutionElement.appendChild(jarPluginExecutionGoalsElement);

    // create build.plugins.plugin.executions.execution.goals.goal element
    final var jarPluginExecutionGoalElement = pomXmlDocument.createElement("goal");
    jarPluginExecutionGoalElement.setTextContent("shade");
    jarPluginExecutionGoalsElement.appendChild(jarPluginExecutionGoalElement);

    // create build.plugins.plugin.configuration element
    final var jarPluginConfigurationElement = pomXmlDocument.createElement("configuration");
    jarPluginElement.appendChild(jarPluginConfigurationElement);

    // create build.plugins.plugin.configuration.finalName element
    final var jarPluginFinalNameElement = pomXmlDocument.createElement("finalName");
    jarPluginFinalNameElement.setTextContent(artifactId);
    jarPluginConfigurationElement.appendChild(jarPluginFinalNameElement);

    // write document to pom.xml file
    MavenProject.writeDocumentToFile(pomXmlDocument, pomXmlFile);

    // create source directory
    final var currentPath = Paths.get(System.getProperty("user.dir"));
    final var sourceDir = Paths.get(currentPath.toString(), "src", "main", "java");
    try {
      Files.createDirectories(sourceDir);
    } catch (final Exception e) {
      panic(e);
    }

    // convert package name to directory name
    final var packageDir =
        Paths.get(sourceDir.toString(), groupId.replace('.', File.separatorChar));
    try {
      Files.createDirectories(packageDir);
    } catch (final Exception e) {
      panic(e);
    }

    // create Main.java file
    final var mainJavaFile = Paths.get(packageDir.toString(), "Main.java").toFile();
    if (!mainJavaFile.exists()) {
      try {
        mainJavaFile.createNewFile();
      } catch (final Exception e) {
        panic(e);
      }

      // write Main.java file
      try (final var writer = new FileWriter(mainJavaFile)) {
        writer.write("package " + groupId + ";\n\n");
        writer.write("public class Main {\n");
        writer.write("    public static void main(String[] args) {\n");
        writer.write("        System.out.println(\"Hello World!\");\n");
        writer.write("    }\n");
        writer.write("}\n");
      } catch (final Exception e) {
        panic(e);
      }
    }

    // create test directory
    final var testDir = Paths.get(currentPath.toString(), "src", "test", "java");
    try {
      Files.createDirectories(testDir);
    } catch (final Exception e) {
      panic(e);
    }

    // convert package name to directory name
    final var testPackageDir =
        Paths.get(testDir.toString(), groupId.replace('.', File.separatorChar));
    try {
      Files.createDirectories(testPackageDir);
    } catch (final Exception e) {
      panic(e);
    }

    // download .editorconfig from github
    final var editorConfigFile = Paths.get(currentPath.toString(), ".editorconfig").toFile();
    if (!editorConfigFile.exists()) {
      try (final var in =
          new URI("https://raw.githubusercontent.com/Frank-Mayer/jack/main/.editorconfig")
              .toURL()
              .openStream()) {
        Files.copy(in, editorConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (final Exception ignore) {
      }
    }

    final var gitIgnoreFile = Paths.get(currentPath.toString(), ".gitignore").toFile();
    if (!gitIgnoreFile.exists()) {
      try {
        gitIgnoreFile.createNewFile();
        try (final var writer = new FileWriter(gitIgnoreFile)) {
          writer.write(Project.getGitignore());
        }
      } catch (final Exception ignore) {
      }
    }

    System.out.println("Done");
  }

  private static void writeDocumentToFile(final Document doc, final File xmlFile) {
    try {
      final var transformerFactory = TransformerFactory.newInstance();
      final var transformer = transformerFactory.newTransformer();

      // Set properties to enable indentation and additional formatting
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

      final var source = new DOMSource(doc);
      final var result = new StreamResult(xmlFile);
      transformer.transform(source, result);
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public Optional<String> getDefaultClassName() {
    // look for project.build.plugins.plugin
    // groupId = org.codehaus.mojo
    // and artifactId = exec-maven-plugin in this.document
    final var plugins = this.document.getElementsByTagName("plugin");
    for (var i = 0; i < plugins.getLength(); ++i) {
      try {
        final var plugin = plugins.item(i);
        final var children = plugin.getChildNodes();
        Node configurationEl = null;
        for (var j = 0; j < children.getLength(); ++j) {
          final var el = children.item(j);
          if (el.getNodeName().equals("groupId")
              && !el.getTextContent().equals("org.codehaus.mojo")) {
            continue;
          }
          if (el.getNodeName().equals("artifactId")
              && !el.getTextContent().equals("exec-maven-plugin")) {
            continue;
          }
          if (el.getNodeName().equals("configuration")) {
            configurationEl = el;
          }
        }
        if (configurationEl == null) {
          continue;
        }
        final var configElChildren = configurationEl.getChildNodes();
        for (var j = 0; j < configElChildren.getLength(); ++j) {
          final var el = configElChildren.item(j);
          if (el.getNodeName().equals("mainClass")) {
            return Optional.of(el.getTextContent());
          }
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
    processBuilder.directory(this.projectRootPath);
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
      final var exitCode = process.exitValue();
      if (exitCode != 0) {
        panic("Build failed with exit code " + exitCode);
      }
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public void clean() {
    final var processBuilder =
        new ProcessBuilder("mvn", "-f", this.projectFile.toString(), "clean");
    processBuilder.directory(this.projectRootPath);
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
      final var exitCode = process.exitValue();
      if (exitCode != 0) {
        panic("Clean failed with exit code " + exitCode);
      }
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public void run() {
    final var entryPoint = this.getDefaultClassName();
    if (entryPoint.isEmpty()) {
      panic("No entry point found");
    }
    final var processBuilder =
        new ProcessBuilder(
            "mvn",
            "-f",
            this.projectFile.toString(),
            "compile",
            "exec:java",
            "-Dexec.mainClass=" + entryPoint.get());
    processBuilder.directory(this.projectRootPath);
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
      final var exitCode = process.exitValue();
      if (exitCode != 0) {
        panic("Run failed with exit code " + exitCode);
      }
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
    processBuilder.directory(this.projectRootPath);
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
      final var exitCode = process.exitValue();
      if (exitCode != 0) {
        panic("Run failed with exit code " + exitCode);
      }
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public void run(final String[] args) {
    final var entryPoint = this.getDefaultClassName();
    if (entryPoint.isEmpty()) {
      panic("No entry point found");
    }
    final var processBuilder =
        new ProcessBuilder(
            "mvn",
            "-f",
            this.projectFile.toString(),
            "compile",
            "exec:java",
            "-Dexec.mainClass=" + entryPoint.get(),
            "-Dexec.args='" + String.join("' '", args) + "'");
    processBuilder.directory(this.projectRootPath);
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
      final var exitCode = process.exitValue();
      if (exitCode != 0) {
        panic("Run failed with exit code " + exitCode);
      }
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public void run(final String className, final String[] args) {
    final var processBuilder =
        new ProcessBuilder(
            "mvn",
            "-f",
            this.projectFile.toString(),
            "compile",
            "exec:java",
            "-Dexec.mainClass=" + className,
            "-Dexec.args='" + String.join("' '", args) + "'");
    processBuilder.directory(this.projectRootPath);
    processBuilder.inheritIO();
    try {
      final var process = processBuilder.start();
      process.waitFor();
      final var exitCode = process.exitValue();
      if (exitCode != 0) {
        panic("Run failed with exit code " + exitCode);
      }
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public void debug() {
    final var defaultClassName = this.getDefaultClassName();
    if (!defaultClassName.isPresent()) {
      panic("No default class name found");
    }
    this.debug(defaultClassName.get());
  }

  @Override
  public void debug(final String className) {
    this.debug(className, new String[0]);
  }

  @Override
  public void debug(final String[] args) {
    final var defaultClassName = this.getDefaultClassName();
    if (!defaultClassName.isPresent()) {
      panic("No default class name found");
    }
    this.debug(defaultClassName.get(), args);
  }

  @Override
  public void debug(final String className, final String[] args) {
    try {
      final int jdbPort = 5005;

      final var compileProcessBuilder =
          new ProcessBuilder(
              "mvn", "-f", this.projectFile.toString(), "compile", "-Dmaven.compiler.debug=true");
      compileProcessBuilder.directory(this.projectRootPath);
      final var compileProcess = compileProcessBuilder.start();
      try {
        compileProcess.waitFor();
      } catch (final Exception e) {
        panic(e);
      }

      final var compileExitCode = compileProcess.exitValue();
      if (compileExitCode != 0) {
        panic("Compile failed with exit code " + compileExitCode);
      }

      final var appProcessBuilder =
          new ProcessBuilder(
              Stream.concat(
                      Arrays.asList(
                          "java",
                          "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address="
                              + jdbPort,
                          "-cp",
                          "target/classes",
                          className)
                          .stream(),
                      Arrays.stream(args))
                  .toArray(String[]::new));
      appProcessBuilder.directory(this.projectRootPath);
      final var appProcess = appProcessBuilder.start();

      Thread.sleep(1000);

      if (appProcess.isAlive()) {
        System.out.println("Application is running");
      } else {
        final var exitCode = appProcess.exitValue();
        panic("Application exited with code " + exitCode);
      }

      new JDB(jdbPort, this);
      appProcess.waitFor();
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public String getSourcePath() {
    // src/main/java
    final var joiner = new StringJoiner(File.separator);
    joiner.add("src");
    joiner.add("main");
    joiner.add("java");
    return joiner.toString();
  }
}
