package io.frankmayer.project;

import static io.frankmayer.Error.panic;

import io.frankmayer.CommonUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

public class IntelliJProject extends Project {

  private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder builder;
  private final File modulesXml;
  private final IntelliJProject.UrlParser urlParser = new IntelliJProject.UrlParser(this);
  private Document modulesXmlDoc;
  private Document miscXmlDoc;

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

  public static void create() {
    panic("IntelliJ is not supported yet");
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
    final var outputDir = this.getOutputDir();
    // find all modules (iml files) from modules.xml
    CommonUtils.findChildren(
            this.modulesXmlDoc.getDocumentElement(),
            x -> {
              if (!x.getNodeName().equals("component")) {
                return false;
              }
              final var name = CommonUtils.getAttribute(x, "name");
              if (name.isEmpty()) {
                return false;
              }
              return name.get().equals("ProjectModuleManager");
            })
        .flatMap(x -> CommonUtils.findChildren(x, y -> y.getNodeName().equals("modules"), false))
        .flatMap(x -> CommonUtils.findChildren(x, y -> y.getNodeName().equals("module"), false))
        .map(
            x -> {
              final var fp = CommonUtils.getAttribute(x, "filepath");
              if (fp.isEmpty()) {
                final var fu = CommonUtils.getAttribute(x, "fileurl");
                if (fu.isEmpty()) {
                  return null;
                }
                return this.urlParser.parse(fu.get());
              }
              return this.urlParser.parse(fp.get());
            })
        .filter(Objects::nonNull)
        // load each iml file
        .map(
            x -> {
              try {
                System.out.printf("Loading '%s'%n", x);
                return IntelliJProject.builder.parse(new File(x));
              } catch (final Exception e) {
                panic(String.format("Failed to load '%s'%n", x), e);
              }
              return null;
            })
        // find all components in each iml file
        .flatMap(
            x ->
                CommonUtils.findChildren(
                    x.getDocumentElement(),
                    y -> {
                      if (!y.getNodeName().equals("component")) {
                        return false;
                      }
                      // filter for NewModuleRootManager components
                      final var name = CommonUtils.getAttribute(y, "name");
                      if (name.isEmpty()) {
                        return false;
                      }
                      return name.get().equals("NewModuleRootManager");
                    }))
        // build the components
        .forEach(
            (component) -> {
              final var excludeOutput = CommonUtils.containsElement(component, "exclude-output");
              final var contentTypes = new HashMap<String, String>();
              final var contentUrl =
                  CommonUtils.findChild(component, "content")
                      .flatMap(x -> CommonUtils.getAttribute(x, "url"))
                      .map(this.urlParser::parse);
              if (contentUrl.isEmpty()) {
                panic(String.format("No content url for component '%s'", component));
              }
              CommonUtils.findChildren(component, "content")
                  .flatMap(x -> CommonUtils.stream(x.getChildNodes()))
                  .forEach(
                      x -> {
                        final var key = x.getNodeName();
                        final var value = CommonUtils.getAttribute(x, "url");
                        if (value.isEmpty()) {
                          return;
                        }
                        if (contentTypes.containsKey(key)) {
                          contentTypes.put(
                              key,
                              contentTypes.get(key) + CommonUtils.getPathSeparator() + value.get());
                        }
                        contentTypes.put(key, value.get());
                      });
              CommonUtils.findChildren(component, "orderEntry")
                  .map(
                      entry -> {
                        final var type = CommonUtils.getAttribute(entry, "type");
                        if (type.isEmpty()) {
                          return null;
                        }
                        switch (type.get()) {
                          case "library":
                            final var name = CommonUtils.getAttribute(entry, "name");
                            if (name.isEmpty()) {
                              return null;
                            }
                            return this.findLibrary(name.get(), contentUrl.get());
                          case "sourceFolder":
                            final var sourceFolder = CommonUtils.getAttribute(entry, "url");
                            if (sourceFolder.isEmpty()) {
                              return null;
                            }
                            return null;
                          case "inheritedJdk":
                            return null;
                          default:
                            panic(String.format("Unknown orderEntry type '%s'%n", type.get()));
                            return null;
                        }
                      });
            });
  }

  private String findLibrary(final String libName, final String contentUrl) {
    final var libOpt =
        CommonUtils.findFiles(new File(contentUrl), libName, "jar")
            .map(x -> x.toString())
            .findAny();
    if (libOpt.isEmpty()) {
      panic(String.format("Failed to find library '%s' in '%s'%n", libName, contentUrl));
    }
    return libOpt.get();
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
}
