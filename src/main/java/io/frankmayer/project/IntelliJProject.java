package io.frankmayer.project;

import static io.frankmayer.Error.panic;

import io.frankmayer.CommonUtils;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

public class IntelliJProject extends Project {

  private static class UrlParser {
    private final IntelliJProject project;

    public UrlParser(final IntelliJProject project) {
      this.project = project;
    }

    public String parse(final String url) {
      if (url.startsWith("file://$MODULE_DIR$/")) {
        return this.project.projectFile.getParent() + "/" + url.substring(20);
      }
      return url;
    }
  }

  private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder builder;

  public static void create() {
    panic("IntelliJ is not supported yet");
  }

  private Document modulesXmlDoc;
  private final File modulesXml;
  private Document miscXmlDoc;
  private final IntelliJProject.UrlParser urlParser = new IntelliJProject.UrlParser(this);

  /**
   * @param projectFile .idea directory
   */
  public IntelliJProject(final File projectFile) {
    super(projectFile);
    this.modulesXml = new File(projectFile, "modules.xml");
    if (IntelliJProject.builder == null) {
      try {
        IntelliJProject.builder = IntelliJProject.factory.newDocumentBuilder();
      } catch (final ParserConfigurationException e) {
        panic(e);
      }
    }
    try {
      this.modulesXmlDoc = IntelliJProject.builder.parse(this.projectFile);
      this.miscXmlDoc = IntelliJProject.builder.parse(this.modulesXml);
    } catch (final Exception e) {
      panic(e);
    }
  }

  @Override
  public Optional<String> getDefaultClassName() {
    return Optional.empty();
  }

  @Override
  public void build() {
    final var rootEl = this.modulesXmlDoc.getDocumentElement();
    if (rootEl == null) {
      panic("No root element in modules.xml");
    }
    if (!rootEl.getAttribute("type").equals("JAVA_MODULE")) {
      panic("Not a Java module");
    }
    final var modVersion = rootEl.getAttribute("version");
    switch (modVersion) {
      case "4":
        this.build4();
        break;
      default:
        panic("Unsupported IntelliJ module version: " + modVersion);
    }
  }

  @Override
  public void clean() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'clean'");
  }

  @Override
  public void run() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'run'");
  }

  @Override
  public void run(final String className) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'run'");
  }

  @Override
  public void run(final String[] args) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'run'");
  }

  @Override
  public void run(final String className, final String[] args) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'run'");
  }

  @Override
  public void debug() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'debug'");
  }

  @Override
  public void debug(final String className) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'debug'");
  }

  @Override
  public void debug(final String[] args) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'debug'");
  }

  @Override
  public void debug(final String string, final String[] args) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'debug'");
  }

  @Override
  public String getSourcePath() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getClassPath'");
  }

  private void build4() {
    // find all modules (iml files) from modules.xml
    CommonUtils.findAllNodes(
        this.modulesXmlDoc.getDocumentElement(),
        x -> x.getNodeName().equals("component")
            && CommonUtils.getAttribute(x, "name").equals("ProjectModuleManager"))
        .flatMap(x -> CommonUtils.findAllNodes(x, y -> y.getNodeName().equals("modules")))
        .flatMap(x -> CommonUtils.findAllNodes(x, y -> y.getNodeName().equals("module")))
        .map(
            x -> {
              final var fp = CommonUtils.getAttribute(x, "filepath");
              if (fp == null) {
                final var fu = CommonUtils.getAttribute(x, "fileurl");
                if (fu == null) {
                  return null;
                }
                return this.urlParser.parse(fu);
              }
              return fp;
            })
        .filter(Objects::nonNull)
        // load each iml file
        .map(
            x -> {
              try {
                return IntelliJProject.builder.parse(new File(x));
              } catch (final Exception e) {
                panic(String.format("Failed to load '%s'%n", x), e);
              }
              return null;
            })
        // find all components in each iml file
        .flatMap(
            x -> CommonUtils.findAllNodes(
                x.getDocumentElement(), y -> y.getNodeName().equals("component")))
        // filter for NewModuleRootManager components
        .filter(x -> CommonUtils.getAttribute(x, "name").equals("NewModuleRootManager"))
        // build the components
        .forEach(
            x -> {
              final var name = CommonUtils.getAttribute(x, "name");
            });
  }
}