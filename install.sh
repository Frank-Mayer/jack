#!/bin/bash

cat << 'END_INTRO'

     ██╗ █████╗  ██████╗██╗  ██╗
     ██║██╔══██╗██╔════╝██║ ██╔╝
     ██║███████║██║     █████╔╝
██   ██║██╔══██║██║     ██╔═██╗
╚█████╔╝██║  ██║╚██████╗██║  ██╗
 ╚════╝ ╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝

END_INTRO

command -v java >/dev/null 2>&1 || {
  echo "Java is not installed or not in your PATH. Jack depends on Java being installed."
  exit 1
}
command -v mvn >/dev/null 2>&1 || {
  echo "Maven is not installed or not in your PATH. Jack depends on Maven being installed."
  exit 1
}

echo "Creating directory $HOME/.jack/bin"
mkdir -p "$HOME/.jack/bin" || {
  echo "Failed to create directory $HOME/.jack/bin"
  echo "Installation failed"
  exit 1
}
echo "Done"

if test -f ./pom.xml; then
  echo "Building jack from source"
  if mvn clean package --quiet && mv target/jack.jar $HOME/.jack/bin/; then
    echo "Done"
  else
      echo "Build failed"
      read -p "Do you want to download the latest release from GitHub instead? [y/N] " -n 1 -r
      echo
      if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Downloading latest release from GitHub"
        curl -o $HOME/.jack/bin/jack.jar https://frank-mayer.github.io/jack/jack.jar || {
          echo "Failed to download latest release from GitHub"
          echo "Installation failed"
          exit 1
        }
        echo "Done"
      else
        echo "Aborting"
        echo "Installation failed"
        exit 1
      fi
  fi
else
  echo "Downloading latest release from GitHub"
  curl -o $HOME/.jack/bin/jack.jar https://frank-mayer.github.io/jack/jack.jar || {
    echo "Failed to download latest release from GitHub"
    echo "Installation failed"
    exit 1
  }
  echo "Done"
fi

echo "Creating wrapper script for jar file"
cat << 'END_SCRIPT' > $HOME/.jack/bin/jack
#!/bin/bash
java -jar $HOME/.jack/bin/jack.jar "$@"
END_SCRIPT
chmod +x $HOME/.jack/bin/jack || {
  echo "Failed to create wrapper script"
  echo "Installation failed"
  exit 1
}
echo "Done"

# find default shells rc file
rcfile="$HOME/.profile"
case "$SHELL" in
  */zsh) rcfile="$HOME/.zshrc" ;;
  */bash) rcfile="$HOME/.bashrc" ;;
  */fish) rcfile="$XDG_CONFIG_HOME/fish/config.fish" ;;
  */csh) rcfile="$HOME/.cshrc" ;;
  */dash) rcfile="$HOME/.dashrc" ;;
esac

[[ $(which jack) == $HOME/.jack/bin/jack ]] || {
  echo "Jack is not in your PATH"

  read -p "Do you want to add jack to PATH in '$rcfile'? [y/N] " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "export PATH=\$PATH:$HOME/.jack/bin" >> "$rcfile"
  else
    echo "You can add jack to your PATH manually by adding the following line to your rc file:"
    echo "export PATH=\$PATH:$HOME/.jack/bin"
  fi
}

echo "Writing man pages"
if test -f ./man/jack.1; then
  cp man/* /usr/local/share/man/man1/ || {
    echo "Failed to add man page"
    exit 1
  }
else
  curl -o /usr/local/share/man/man1/jack.1 https://raw.githubusercontent.com/Frank-Mayer/jack/main/man/jack.1 || {
    echo "Failed to add man page"
    exit 1
  }
fi
echo "Done"

echo "Installation complete"

command -v java >/dev/null 2>&1 || {
  echo "Java is not installed or not in your PATH"
  exit 1
}
command -v mvn >/dev/null 2>&1 || {
  echo "Maven is not installed or not in your PATH"
  ask_install maven
  exit 1
}