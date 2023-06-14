package io.frankmayer.project;

import static io.frankmayer.Error.panic;

import java.io.File;
import java.util.Optional;

public abstract class Project {

    protected File projectFile;
    protected File projectRootPath;

    public Project(final File projectFile) {
        this.projectFile = projectFile;
        this.projectRootPath = this.projectFile.getParentFile();
    }

    public static void create(final String string) {
        switch (string.toLowerCase()) {
        case "maven":
            MavenProject.create();
            break;
        case "gradle":
            GradleProject.create();
            break;
        case "intellij":
            IntelliJProject.create();
            break;
        default:
            panic(String.format("Unknown project type: \"%s\"", string));
            break;
        }
    }

    public static String getGitignore() {
    return """
# Compiled class file
target/
*.class

# Log file
*.log

# BlueJ files
*.ctxt

# Mobile Tools for Java (J2ME)
.mtj.tmp/

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# virtual machine crash logs, see http://www.java.com/en/download/help/error_hotspot.xml
hs_err_pid*
replay_pid*

!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

### IntelliJ IDEA ###
.idea/
*.iws
*.iml
*.ipr

### Eclipse ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/
build/
!**/src/main/**/build/
!**/src/test/**/build/

### VS Code ###
.vscode/

### Fleet ###
.fleet/

### Mac OS ###
.DS_Store
        """;
    }

    public File getRootPath() { return this.projectRootPath; }

    public String getProjectFile() { return this.projectFile.getPath(); }

    public abstract Optional<String> getDefaultClassName();

    public abstract void build();

    public abstract void clean();

    public abstract void run();

    public abstract void run(final String className);

    public abstract void run(final String[] args);

    public abstract void run(final String className, final String[] args);

    public abstract void debug();

    public abstract void debug(final String className);

    public abstract void debug(final String[] args);

    public abstract void debug(final String string, final String[] args);

    public abstract String getSourcePath();
}
