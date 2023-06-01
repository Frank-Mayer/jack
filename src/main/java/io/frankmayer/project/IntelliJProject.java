package io.frankmayer.project;

import static io.frankmayer.Error.panic;

import io.frankmayer.CommonUtils;
import io.frankmayer.JDB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class IntelliJProject extends Project {

  private static class UrlParser {

    private final IntelliJProject project;

    public UrlParser(final IntelliJProject project) {
      this.project = project;
    }

    public String parse(final String url) {
      if (url.startsWith("file://")) {
        return this.parse(url.substring(7));
      }
      return url.replace("$PROJECT_DIR$", this.project.getRootPath().toString())
          .replace("$MODULE_DIR$", this.project.getRootPath().toString());
    }
  }

  private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder builder;

  public static void create() {
    panic("IntelliJ is not supported yet");
  }

  private final File modulesXml;
  private final File miscXml;
  private final IntelliJProject.UrlParser urlParser = new IntelliJProject.UrlParser(this);

  private Document modulesXmlDoc;

  private Document miscXmlDoc;
private Set<String> sourcePath = new HashSet<>();

  /**
   * @param projectFile .idea directory
   */
  public IntelliJProject(final File projectFile) {
    super(projectFile);
    this.modulesXml = new File(projectFile, "modules.xml");
    if (!this.modulesXml.exists()) {
      panic("No modules.xml found in " + projectFile);
    }
    this.miscXml = new File(projectFile, "misc.xml");
    if (!this.miscXml.exists()) {
      panic("No misc.xml found in " + projectFile);
    }
    if (IntelliJProject.builder == null) {
      try {
        IntelliJProject.builder = IntelliJProject.factory.newDocumentBuilder();
      } catch (final ParserConfigurationException e) {
        panic(e);
      }
    }
    try {
      this.modulesXmlDoc = IntelliJProject.builder.parse(this.modulesXml);
    } catch (final Exception e) {
      panic("Failed to read modules.xml", e);
    }
    try {
      this.miscXmlDoc = IntelliJProject.builder.parse(this.miscXml);
    } catch (final Exception e) {
      panic("Failed to read misc.xml", e);
    }
  }

  @Override
  public Optional<String> getDefaultClassName() {
    return Optional.empty();
  }

  @Override
  public void build() {
    final var sourcepath = this.compile();
    if (sourcepath == null) {
      panic("Failed to compile");
      return;
    }
    final var jarProcessArgs = new ArrayList<String>();
    jarProcessArgs.add("jar");
    jarProcessArgs.add("cvf");
    jarProcessArgs.add("out.jar");
    jarProcessArgs.add("-C");
    jarProcessArgs.add(this.getOutputDir());
    jarProcessArgs.add(".");
    final var jarProcessBuilder = new ProcessBuilder(jarProcessArgs);
    jarProcessBuilder.inheritIO();
    jarProcessBuilder.directory(this.getRootPath());
    try {
      final var jarProcess = jarProcessBuilder.start();
      final var exitCode = jarProcess.waitFor();
      if (exitCode != 0) {
        panic("Jar process exited with code " + exitCode);
      }
    } catch (final Exception e) {
      panic("Failed to run", e);
    }
  }

  @Override
  public void clean() {
    final var outDir = new File(this.getOutputDir());
    if (!outDir.exists()) {
      return;
    }
    outDir.delete();
  }

  @Override
  public void run() {
    final var mainClass = this.getDefaultClassName();
    if (mainClass.isEmpty()) {
      panic("No main class found");
      return;
    }
    final var classPath = this.compile();
    if (classPath == null) {
      panic("Failed to compile");
      return;
    }
    final var mainClassName = mainClass.get();
    final var javaProcessArgs = new ArrayList<String>();
    javaProcessArgs.add("java");
    javaProcessArgs.add("-cp");
    javaProcessArgs.add(String.join(File.pathSeparator, classPath));
    javaProcessArgs.add(mainClassName);
    final var javaProcessBuilder = new ProcessBuilder(javaProcessArgs);
    javaProcessBuilder.inheritIO();
    javaProcessBuilder.directory(this.getRootPath());
    try {
      final var javaProcess = javaProcessBuilder.start();
      final var exitCode = javaProcess.waitFor();
      if (exitCode != 0) {
        panic("Java process exited with code " + exitCode);
      }
    } catch (final Exception e) {
      panic("Failed to run", e);
    }
  }

  @Override
  public void run(final String className) {
    final var classPath = this.compile();
    if (classPath == null) {
      panic("Failed to compile");
      return;
    }
    final var javaProcessArgs = new ArrayList<String>();
    javaProcessArgs.add("java");
    javaProcessArgs.add("-cp");
    javaProcessArgs.add(String.join(File.pathSeparator, classPath));
    javaProcessArgs.add(className);
    final var javaProcessBuilder = new ProcessBuilder(javaProcessArgs);
    javaProcessBuilder.inheritIO();
    javaProcessBuilder.directory(this.getRootPath());
    try {
      final var javaProcess = javaProcessBuilder.start();
      final var exitCode = javaProcess.waitFor();
      if (exitCode != 0) {
        panic("Java process exited with code " + exitCode);
      }
    } catch (final Exception e) {
      panic("Failed to run", e);
    }
  }

  @Override
  public void run(final String[] args) {
    final var mainClass = this.getDefaultClassName();
    if (mainClass.isEmpty()) {
      panic("No main class found");
      return;
    }
    final var classPath = this.compile();
    if (classPath == null) {
      panic("Failed to compile");
      return;
    }
    final var javaProcessArgs = new ArrayList<String>();
    javaProcessArgs.add("java");
    javaProcessArgs.add("-cp");
    javaProcessArgs.add(String.join(File.pathSeparator, classPath));
    javaProcessArgs.add(mainClass.get());
    javaProcessArgs.addAll(Arrays.asList(args));
    final var javaProcessBuilder = new ProcessBuilder(javaProcessArgs);
    javaProcessBuilder.inheritIO();
    javaProcessBuilder.directory(this.getRootPath());
    try {
      final var javaProcess = javaProcessBuilder.start();
      final var exitCode = javaProcess.waitFor();
      if (exitCode != 0) {
        panic("Java process exited with code " + exitCode);
      }
    } catch (final Exception e) {
      panic("Failed to run", e);
    }
  }

  @Override
  public void run(final String className, final String[] args) {
    final var classPath = this.compile();
    if (classPath == null) {
      panic("Failed to compile");
      return;
    }
    final var javaProcessArgs = new ArrayList<String>();
    javaProcessArgs.add("java");
    javaProcessArgs.add("-cp");
    javaProcessArgs.add(String.join(File.pathSeparator, classPath));
    javaProcessArgs.add(className);
    javaProcessArgs.addAll(Arrays.asList(args));
    final var javaProcessBuilder = new ProcessBuilder(javaProcessArgs);
    javaProcessBuilder.inheritIO();
    javaProcessBuilder.directory(this.getRootPath());
    try {
      final var javaProcess = javaProcessBuilder.start();
      final var exitCode = javaProcess.waitFor();
      if (exitCode != 0) {
        panic("Java process exited with code " + exitCode);
      }
    } catch (final Exception e) {
      panic("Failed to run", e);
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
    final var classPath = this.compile();
    if (classPath == null) {
      panic("Failed to compile");
      return;
    }
    final var jdbPort = JDB.getPort();
    final var javaProcessArgs = new ArrayList<String>();
    javaProcessArgs.add("java");
    javaProcessArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + jdbPort);
    javaProcessArgs.add("-cp");
    javaProcessArgs.add(String.join(File.pathSeparator, classPath));
    javaProcessArgs.add(className);
    javaProcessArgs.addAll(Arrays.asList(args));
    final var appProcessBuilder = new ProcessBuilder(javaProcessArgs);
    appProcessBuilder.inheritIO();
    appProcessBuilder.directory(this.getRootPath());
    try {
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
      panic("Failed to run", e);
    }
  }

  @Override
  public String getSourcePath() {
    return String.join(CommonUtils.getPathSeparator(), this.sourcePath);
  }

  private Set<String> compile() {
    final var rootEl = this.modulesXmlDoc.getDocumentElement();
    if (rootEl == null) {
      panic("No root element in modules.xml");
    }
    final var modVersion = rootEl.getAttribute("version");
    switch (modVersion) {
      case "4":
        final var sp = this.compile4();
        if (sp == null) {
          panic("Failed to compile");
        }
        
        return sp;
      default:
        panic("Unsupported IntelliJ modules.xml version: " + modVersion);
    }
    return null;
  }

  private Set<String> compile4() {
    final var sourceFiles = new HashSet<String>();
    final var classpath = new HashSet<String>();
    final var modules = this.getModulesFiles();

    for (final var modFile : modules) {
      Document modDoc;
      try {
        modDoc = IntelliJProject.builder.parse(modFile);
      } catch (final Exception e) {
        panic(String.format("Failed to read module file '%s'", modFile), e);
        return null;
      }
      final var root = modDoc.getDocumentElement();
      if (root == null) {
        panic("No root element in module file");
      }
      final var type = root.getAttribute("type");
      if (!type.equals("JAVA_MODULE")) {
        panic(String.format("Unsupported module type '%s' in '%s'", type, modFile));
        continue;
      }
      final var modVersion = root.getAttribute("version");
      switch (modVersion) {
        case "4":
          for (final var componentEl :
              CommonUtils.findChildren(root, "component", false).toList()) {
            this.collectModuleComponent4(
                 sourceFiles,
                classpath, componentEl);
          }
          break;
        default:
          panic("Unsupported IntelliJ module version: " + modVersion);
      }
    }

    final var outputDir = this.getOutputDir();
    CommonUtils.ensureDirectoryExists(outputDir);

    final var javacArguments = new ArrayList<String>();
    javacArguments.add("javac");
    javacArguments.add("-d");
    javacArguments.add(outputDir);
    javacArguments.add("-classpath");
    javacArguments.add(String.join(CommonUtils.getPathSeparator(), classpath));
    javacArguments.add("-sourcepath");
    javacArguments.add(this.getSourcePath());
    javacArguments.addAll(sourceFiles);
    final var javacProcessBuilder = new ProcessBuilder(javacArguments);
    javacProcessBuilder.directory(this.getRootPath());
    javacProcessBuilder.inheritIO();
    try {
      final var javacProcess = javacProcessBuilder.start();
      final var javacExitCode = javacProcess.waitFor();
      if (javacExitCode != 0) {
        panic("javac failed with exit code " + javacExitCode);
      }
      classpath.add(outputDir);
      return classpath;
    } catch (final Exception e) {
      panic("Failed to run javac", e);
    }
    return null;
  }

  private void collectModuleComponent4(
      final HashSet<String> sourceFiles,
      final HashSet<String> classpath,
      final Node componentEl) {
    for (final var orderEntry :
        CommonUtils.findChildren(componentEl, "orderEntry", false).toList()) {
      final var typeOpt = CommonUtils.getAttribute(orderEntry, "type");
      if (typeOpt.isEmpty()) {
        continue;
      }
      final var contentOpt = CommonUtils.findChild(componentEl, "content");
      if (contentOpt.isEmpty()) {
        panic("No content element to look for source folders");
        continue;
      }
      final var contentEl = contentOpt.get();
      switch (typeOpt.get()) {
        case "inheritedJdk":
          // TODO don't know what to do with this
          break;
        case "sourceFolder":
          final var sourceFolderOpt = CommonUtils.findChild(contentEl, "sourceFolder");
          if (sourceFolderOpt.isEmpty()) {
            panic("No sourceFolder element to look for source folders");
            continue;
          }
          final var urlOpt = CommonUtils.getAttribute(sourceFolderOpt.get(), "url");
          if (urlOpt.isEmpty()) {
            panic("No url attribute in sourceFolder element");
            continue;
          }
          final var url = this.urlParser.parse(urlOpt.get());
          this.sourcePath.add(url);
          sourceFiles.addAll(this.collectSourceFiles(url));
          break;
        case "library":
          final var libraryNameOpt = CommonUtils.getAttribute(orderEntry, "name");
          if (libraryNameOpt.isEmpty()) {
            panic("No name attribute in library element");
            continue;
          }
          final var libraryName = libraryNameOpt.get();
          final var contentUrlOpt = CommonUtils.getAttribute(contentEl, "url");
          if (contentUrlOpt.isEmpty()) {
            panic("No url attribute in content element");
            continue;
          }
          final var contentUrl = this.urlParser.parse(contentUrlOpt.get());
          CommonUtils.findFiles(new File(contentUrl), libraryName, "jar")
              .forEach(x -> classpath.add(x.toString()));
          break;
      }
    }
  }

  private List<String> collectSourceFiles(final String sourcePath) {
    try {
      return Files.walk(Paths.get(this.urlParser.parse(sourcePath)))
          .filter(Files::isRegularFile)
          .filter(x -> x.toString().endsWith(".java"))
          .map(x -> x.toString())
          .toList();
    } catch (final IOException e) {
      panic("Failed to collect source files", e);
      return null;
    }
  }

  private List<File> getModulesFiles() {
    return CommonUtils.findChildren(this.modulesXmlDoc.getDocumentElement(), "module")
        .map(
            moduleEl -> {
              final var filepathOpt = CommonUtils.getAttribute(moduleEl, "filepath");
              if (filepathOpt.isPresent()) {
                return filepathOpt.get();
              }
              final var fileurlOpt = CommonUtils.getAttribute(moduleEl, "fileurl");
              if (fileurlOpt.isPresent()) {
                return this.urlParser.parse(fileurlOpt.get());
              }
              panic("Failed to find 'filepath' or 'fileurl' attribute in module element");
              return null;
            })
        .map(this.urlParser::parse)
        .map(x -> new File(x))
        .toList();
  }

  private String getOutputDir() {
    return CommonUtils.findChildren(
            this.miscXmlDoc.getDocumentElement(),
            x -> {
              if (!x.getNodeName().equals("component")) {
                return false;
              }
              final var name = CommonUtils.getAttribute(x, "name");
              if (name.isEmpty()) {
                return false;
              }
              return name.get().equals("ProjectRootManager");
            })
        .flatMap(x -> CommonUtils.findChildren(x, y -> y.getNodeName().equals("output"), false))
        .map(x -> CommonUtils.getAttribute(x, "url"))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(this.urlParser::parse)
        .findAny()
        .orElseGet(() -> this.urlParser.parse("file://$PROJECT_DIR$/out"));
  }
}
