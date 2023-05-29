package io.frankmayer;

import static io.frankmayer.Error.panic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArgumentParser {

  private String command = null;
  private final Map<String, String> options = new HashMap<>();

  private Optional<String[]> remainingArguments;

  public ArgumentParser(final String[] args) {
    String optionName = null;
    for (var i = 0; i < args.length; ++i) {
      final var arg = args[i];

      if (arg.equals("--")) {
        this.remainingArguments = Optional.of(Arrays.stream(args, i + 1, args.length).toArray(String[]::new));
        break;
      }

      if (this.command == null) {
        this.command = arg;
        continue;
      }

      if (optionName == null) {
        if (!arg.startsWith("--")) {
          panic(String.format("Invalid option name '%s' at position %s", arg, i));
        }
        optionName = arg.substring(2).toLowerCase();
      } else {
        options.put(optionName, arg);
        optionName = null;
      }
    }

    if (optionName != null) {
      panic(String.format("Missing value for option '%s'", optionName));
    }

    if (this.command == null) {
      panic("No command given");
    }

    if (this.remainingArguments == null) {
      this.remainingArguments = Optional.empty();
    }
  }

  public String getCommand() {
    return command;
  }

  public Optional<String> getOption(final String name) {
    if (!options.containsKey(name)) {
      return Optional.empty();
    }
    return Optional.of(options.get(name));
  }

  public Optional<String[]> getRemainingArguments() {
    return remainingArguments;
  }
}