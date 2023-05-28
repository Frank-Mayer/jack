package io.frankmayer;

import static io.frankmayer.Error.panic;

import io.frankmayer.project.Project;
import java.util.Arrays;

public class Main {

  public static void main(final String[] args) {
    final var proj = CommonUtils.getProject();

    switch (args.length) {
      case 1:
        switch (args[0]) {
          case "build":
            proj.ifPresent(x -> x.build());
            return;
          case "clean":
            proj.ifPresent(x -> x.clean());
            return;
          case "rebuild":
            proj.ifPresent(
                x -> {
                  x.clean();
                  x.build();
                });
            return;
          case "run":
            proj.ifPresent(x -> x.run());
            return;
          case "debug":
            proj.ifPresent(x -> x.debug());
            return;
          case "default-class":
            proj.ifPresent(
                x -> {
                  final var defaultClassName = x.getDefaultClassName();
                  if (defaultClassName.isPresent()) {
                    System.out.println(defaultClassName.get());
                  } else {
                    System.out.println("No default class name found");
                  }
                });
            return;
          case "-v":
          case "--version":
            System.out.println("Version 1.0.0");
            return;
          default:
            panic(String.format("No command named %s takes no arguments", args[0]));
            return;
        }
      case 2:
        switch (args[0]) {
          case "run":
            proj.ifPresent(x -> x.run(args[1]));
            return;
          case "debug":
            proj.ifPresent(x -> x.debug(args[1]));
            return;
          case "init":
            if (proj.isPresent()) {
              System.out.println("There is already a project here: " + proj.get().getProjectFile());
              System.out.println("This could lead to problems");
              if (!CommonUtils.confirm("Do you want to continue?", false)) {
                System.out.println("Aborting");
                return;
              }
            }
            Project.create(args[1]);
            return;
          default:
            panic(String.format("No command named %s takes one argument", args[0]));
        }
      default:
        if (args.length >= 3) {
          // look for parameter "--"
          // to separate the command from the arguments
          int i = 0;
          for (; i < args.length; i++) {
            if (args[i].equals("--")) {
              break;
            }
          }
          final var passArgs = Arrays.stream(args).skip(i + 1).toArray(String[]::new);

          if (i == 1) {
            switch (args[0]) {
              case "run":
                proj.ifPresent(x -> x.run(passArgs));
                return;
              case "debug":
                proj.ifPresent(x -> x.debug(passArgs));
                return;
              default:
                panic(String.format("No command named %s takes arguments", args[0]));
            }
          } else if (i == 2) {
            switch (args[0]) {
              case "run":
                proj.ifPresent(x -> x.run(args[1], passArgs));
                return;
              case "debug":
                proj.ifPresent(x -> x.debug(args[1], passArgs));
                return;
              default:
                panic(String.format("No command named %s takes arguments", args[0]));
            }
          }
        }
    }
    System.out.println("help: ");
    System.out.println("  init <project type>: Create a new project");
    System.out.println("  build: Build the project");
    System.out.println("  clean: Clean the project");
    System.out.println("  rebuild: Clean and build the project");
    System.out.println("  run: Run the project (default class)");
    System.out.println("  run <entry point>: Run the project at the given entry point");
    System.out.println("  debug: Debug the project");
    System.out.println("  default-class: Print the default class name");
  }
}