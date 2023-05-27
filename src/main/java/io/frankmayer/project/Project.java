package io.frankmayer.project;

import java.io.File;
import java.util.Optional;

public abstract class Project {

  protected final File projectFile;

  public Project(final File projectFile) {
    this.projectFile = projectFile;
  }

  public abstract Optional<String> getDefaultClassName();

  public abstract void build();

  public abstract void clean();

  public abstract void run();

  public abstract void run(final String className);

  public abstract void debug();

  public abstract void debug(final String className);

  public abstract String getSourcePath();
}