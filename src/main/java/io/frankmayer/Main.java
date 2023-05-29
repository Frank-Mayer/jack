package io.frankmayer;

import static io.frankmayer.Error.panic;

import io.frankmayer.project.Project;

public class Main {

  public static void main(final String[] args) {
    final var parser = new ArgumentParser(args);
    final var projOpt = CommonUtils.getProject();

    switch (parser.getCommand()) {
      case "init":
        final var projectType = parser.getOption("type");
        if (projectType.isEmpty()) {
          panic("Missing project type");
        }
        Project.create(projectType.get());
        break;
      case "build":
        if (projOpt.isEmpty()) {
          panic("No project found");
        }
        projOpt.get().build();
        break;
      case "clean":
        if (projOpt.isEmpty()) {
          panic("No project found");
        }
        projOpt.get().clean();
        break;
      case "rebuild":
        if (projOpt.isEmpty()) {
          panic("No project found");
        }
        projOpt.get().clean();
        projOpt.get().build();
        break;
      case "run":
        if (projOpt.isEmpty()) {
          panic("No project found");
        }
        final var entryPoint = parser.getOption("entry-point");
        final var passArgs = parser.getRemainingArguments();
        if (entryPoint.isPresent()) {
          if (passArgs.isPresent()) {
            projOpt.get().run(entryPoint.get(), passArgs.get());
          } else {
            projOpt.get().run(entryPoint.get());
          }
        } else {
          if (passArgs.isPresent()) {
            projOpt.get().run(passArgs.get());
          } else {
            projOpt.get().run();
          }
        }
        break;
      case "debug":
        if (projOpt.isEmpty()) {
          panic("No project found");
        }
        final var debugEntryPoint = parser.getOption("entry-point");
        final var debugPassArgs = parser.getRemainingArguments();
        if (debugEntryPoint.isPresent()) {
          if (debugPassArgs.isPresent()) {
            projOpt.get().debug(debugEntryPoint.get(), debugPassArgs.get());
          } else {
            projOpt.get().debug(debugEntryPoint.get());
          }
        } else {
          if (debugPassArgs.isPresent()) {
            projOpt.get().debug(debugPassArgs.get());
          } else {
            projOpt.get().debug();
          }
        }
        break;
      case "default-class":
        if (projOpt.isEmpty()) {
          panic("No project found");
        }
        final var defaultClassOpt = projOpt.get().getDefaultClassName();
        if (defaultClassOpt.isPresent()) {
          System.out.println(defaultClassOpt.get());
        } else {
          System.out.println("No default class found");
        }
        break;
      case "help":
      case "h":
      case "?":
        System.out.println("init --type <type>  Create a new project of the given type");
        System.out.println("build               Build the project");
        System.out.println("clean               Clean the project");
        System.out.println("rebuild             Clean and build the project");
        System.out.println("run                 Run the project");
        System.out.println("  --entry-point <entry-point> : Run the given entry point");
        System.out.println("  -- <args...> : Pass the given arguments to the entry point");
        System.out.println("debug               Debug the project");
        System.out.println("  --entry-point <entry-point> : Debug the given entry point");
        System.out.println("  -- <args...> : Pass the given arguments to the entry point");
        System.out.println("default-class       Print the default class name");
        System.out.println("help                Print this help message");
        break;
      default:
        panic(String.format("Unknown command '%s'", parser.getCommand()));
    }
  }
}