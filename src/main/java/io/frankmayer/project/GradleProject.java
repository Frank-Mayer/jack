package io.frankmayer.project;

import static io.frankmayer.Error.panic;

import java.io.File;
import java.util.Optional;

public final class GradleProject extends Project {

  public GradleProject(final File projectFile) {
    super(projectFile);
    panic("Gradle is not supported yet");
  }

  public static final void create() {
    panic("Gradle is not supported yet");
  }

  @Override
  public final Optional<String> getDefaultClassName() {
    return Optional.empty();
  }

  @Override
  public final void build() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'build'");
  }

  @Override
  public final void clean() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'clean'");
  }

  @Override
  public final void run() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'run'");
  }

  @Override
  public final void run(final String className) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'run'");
  }

  @Override
  public final void run(final String[] args) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'run'");
  }

  @Override
  public final void run(final String className, final String[] args) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'run'");
  }

  @Override
  public final void debug() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'debug'");
  }

  @Override
  public final void debug(final String className) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'debug'");
  }

  @Override
  public final void debug(final String[] args) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'debug'");
  }

  @Override
  public final void debug(final String string, final String[] args) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'debug'");
  }

  @Override
  public final String getSourcePath() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getClassPath'");
  }
}
