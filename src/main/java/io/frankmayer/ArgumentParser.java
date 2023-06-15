package io.frankmayer;

import static io.frankmayer.Error.panic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ArgumentParser {

  private final Map<String, String> options = new HashMap<>();
  private final Set<String> unusedOptions = new HashSet<>();
  private final Set<String> flags = new HashSet<>();
  private final Set<String> unusedFlags = new HashSet<>();
  private String command = null;
  private Optional<String[]> remainingArguments;

  public ArgumentParser(final String[] args) {
    String optionName = null;
    for (var i = 0; i < args.length; ++i) {
      final var arg = args[i];

      if (arg.equals("--")) {
        this.remainingArguments =
            Optional.of(Arrays.stream(args, i + 1, args.length).toArray(String[]::new));
        break;
      }

      if (this.command == null) {
        this.command = arg;
        continue;
      }

      if (arg.startsWith("-")) {
        if (optionName == null) {
          optionName = arg.toLowerCase();
        } else {
          if (this.options.containsKey(optionName)) {
            panic(String.format("Duplicate option flag: %s", optionName));
          }
          this.flags.add(optionName);
          optionName = arg.toLowerCase();
        }
      } else {
        if (optionName == null) {
          panic(String.format("Unexpected argument: %s", arg));
        }
        if (this.options.containsKey(optionName)) {
          panic(String.format("Duplicate option: %s", optionName));
        }
        this.options.put(optionName, arg);
        optionName = null;
      }
    }

    if (optionName != null) {
      if (this.options.containsKey(optionName)) {
        panic(String.format("Duplicate option flag: %s", optionName));
      }
      this.flags.add(optionName);
    }

    if (this.command == null) {
      panic("No command given");
    }

    if (this.remainingArguments == null) {
      this.remainingArguments = Optional.empty();
    }

    this.unusedOptions.addAll(this.options.keySet());
    this.unusedFlags.addAll(this.flags);
  }

  public String getCommand() {
    return this.command;
  }

  public Optional<String> getOption(final String... name) {
    Optional<String> result = Optional.empty();
    for (final var n : name) {
      if (this.options.containsKey(n)) {
        if (result.isPresent()) {
          panic(String.format("Duplicate option: %s", n));
        }
        result = Optional.of(this.options.get(n));
        this.unusedOptions.remove(n);
      }
    }
    return result;
  }

  public Optional<String[]> getRemainingArguments() {
    return this.remainingArguments;
  }

  public boolean flag(final String... string) {
    for (final var s : string) {
      if (this.flags.contains(s)) {
        this.unusedFlags.remove(s);
        return true;
      }
    }
    return false;
  }

  public void checkUnused() {
    if (!this.unusedOptions.isEmpty()) {
      System.err.println(String.format("Unused options: %s", this.unusedOptions));
    }
    if (!this.unusedFlags.isEmpty()) {
      System.err.println(String.format("Unused flags: %s", this.unusedFlags));
    }
  }
}
