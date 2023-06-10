#!/bin/bash

N='\033[0m'
R='\033[31m'
B='\033[34m'
echo -e "${N} ┌───────────────────────┐"
echo -e "${N} │ J                     │"
echo -e "${N} │ A                     │"
echo -e "${N} │ C                     │"
echo -e "${N} │ K   ${R}        (   ${N}      │"
echo -e "${N} │     ${R}       (#   ${N}      │"
echo -e "${N} │     ${R}     ###    ${N}      │"
echo -e "${N} │     ${R}   ##  #,   ${N}      │"
echo -e "${N} │     ${R}   #/ ##    ${N}      │"
echo -e "${N} │     ${R}    #  ##   ${N}      │"
echo -e "${N} │ ${B}   ************   *${N}   │"
echo -e "${N} │ ${B}    **********  ** ${N}   │"
echo -e "${N} │ ${B}     ********,     ${N}   │"
echo -e "${N} │ ${B} *****,    ,***** *${N} ꓘ │"
echo -e "${N} │                     Ɔ │"
echo -e "${N} │                     Ɐ │"
echo -e "${N} │                     ᒋ │"
echo -e "${N} └───────────────────────┘"
echo -e "${N}"

command -v java >/dev/null 2>&1 || {
  echo "Java is not installed or not in your PATH. Jack depends on Java being installed."
  exit 1
}
command -v mvn >/dev/null 2>&1 || {
  echo "Maven is not installed or not in your PATH. Jack depends on Maven being installed."
  exit 1
}

JACK_HOME="/opt/jack"
BIN_DIR="$JACK_HOME/bin"

echo "Creating directory $BIN_DIR"
sudo mkdir -p "$BIN_DIR" || {
  echo "Failed to create directory $BIN_DIR"
  echo "Installation failed"
  exit 1
}
echo "Done"

echo "Setting permissions for directory $BIN_DIR"
sudo chmod -R 755 "$BIN_DIR" || {
  echo "Failed to set permissions for directory $BIN_DIR"
  echo "Installation failed"
  exit 1
}
sudo chown -R $(whoami) "$JACK_HOME" || {
  echo "Failed to set permissions for directory $JACK_HOME"
  echo "Installation failed"
  exit 1
}
echo "Done"

if test -f ./pom.xml; then
  echo "Building jack from source"
  if mvn clean package --quiet && mv target/jack.jar $BIN_DIR/; then
    echo "Done"
  else
      echo "Build failed"
      read -p "Do you want to download the latest release from GitHub instead? [y/N] " -n 1 -r
      echo
      if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Downloading latest release from GitHub"
        curl -o "$BIN_DIR/jack.jar" https://frank-mayer.github.io/jack/jack.jar || {
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
  curl -o "$BIN_DIR/jack.jar" https://frank-mayer.github.io/jack/jack.jar || {
    echo "Failed to download latest release from GitHub"
    echo "Installation failed"
    exit 1
  }
  echo "Done"
fi

echo "Creating wrapper script for jar file"
echo "#!/bin/bash" > "$BIN_DIR/jack"
echo "java -jar $BIN_DIR/jack.jar \"\$@\"" >> "$BIN_DIR/jack"
chmod +x $BIN_DIR/jack || {
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

[[ $(which jack) == "$BIN_DIR/jack" ]] || {
  echo "Jack is not in your PATH"

  read -p "Do you want to add jack to PATH in '$rcfile'? [y/N] " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "export PATH=$BIN_DIR:\$PATH" >> "$rcfile"
  else
    echo "You can add jack to your PATH manually by adding the following line to your rc file:"
    echo "export PATH=$BIN_DIR:\$PATH"
  fi
}

echo "Writing man pages"
function install_man_page {
  if test -f ./man/jack.1; then
    sudo cp man/* "$1" || {
      echo "Failed to add man page"
      exit 1
    }
  else
    sudo curl -o "$1/jack.1" https://raw.githubusercontent.com/Frank-Mayer/jack/main/man/jack.1 || {
      echo "Failed to add man page"
      exit 1
    }
  fi
  echo "Done"
}
if sudo test -d /usr/local/share/man/man1/; then
  install_man_page "/usr/local/share/man/man1"
elif sudo test -d /usr/share/man/man1/; then
  install_man_page "/usr/share/man/man1/"
else
  echo "No target for man pages found"
fi

echo "Installation complete"

