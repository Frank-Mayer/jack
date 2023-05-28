# Jack

Unified wrapper for common Java utils

Currently there is only support for maven. PRs are welcome!

[![Java CI with Maven](https://github.com/Frank-Mayer/jack/actions/workflows/maven.yml/badge.svg)](https://github.com/Frank-Mayer/jack/actions/workflows/maven.yml)

[![Deploy jar file to Pages](https://github.com/Frank-Mayer/jack/actions/workflows/deploy.yml/badge.svg)](https://github.com/Frank-Mayer/jack/actions/workflows/deploy.yml)

## Installation

You can [download the latest `jack.jar` file](https://frank-mayer.github.io/jack/jack.jar) but it is recommended to use the installation script.

```bash
bash <(curl -s https://raw.githubusercontent.com/Frank-Mayer/jack/main/install.sh)
```

## Usage

- `jack build` Compiles the project
- `jack run` Compiles the project and runs it
- `jack debug` Compiles the project, runs it and attaches a debugger
- `jack default-class` Print the default class name

More detailed information is provided in this [repos wiki](https://github.com/Frank-Mayer/jack/wiki).

## The tools you want to use need to be installed and avaliable in PATH

You can install them from the websites or using your package manager

- java ([Oracle JDK](https://www.oracle.com/de/java/technologies/downloads/)/[OpenJDK](https://openjdk.org/)/[Amazon Corretto](https://aws.amazon.com/de/corretto/?filtered-posts.sort-by=item.additionalFields.createdDate&filtered-posts.sort-order=desc)/…)
- [jdb](https://docs.oracle.com/en/java/javase/11/tools/jdb.html)
- [maven](https://maven.apache.org/)