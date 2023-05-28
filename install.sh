#!/bin/bash

echo "Installing jack"
echo "Creating directory $HOME/.jack/bin"
mkdir -p $HOME/.jack/bin
echo "Done"

echo "Building jack from source"
if mvn clean package --quiet && mv target/jack.jar $HOME/.jack/bin/; then
  echo "Done"
else
    echo "Build failed"
    read -r -p "Do you want to download the latest release from GitHub instead? [y/N] " response
    case "$response" in
      [yY][eE][sS]|[yY]) 
        echo "Downloading latest release from GitHub"
        curl -o $HOME/.jack/bin/jack.jar https://frank-mayer.github.io/jack/jack.jar
        echo "Done"
        ;;
      *)
        echo "Aborting"
        echo "Installation failed"
        exit 1
        ;;
    esac
fi

echo "Creating wrapper script for jar file"
cat << 'END_SCRIPT' > $HOME/.jack/bin/jack
#!/bin/bash
java -jar $HOME/.jack/bin/jack.jar "$@"
END_SCRIPT
chmod +x $HOME/.jack/bin/jack
echo "Done"

command -v jack >/dev/null 2>&1 || {
  echo "Jack is not on your PATH."
  # find default shells rc file
  rcfile="$HOME/.profile"
  case "$SHELL" in
    */zsh) rcfile="$HOME/.zshrc" ;;
    */bash) rcfile="$HOME/.bashrc" ;;
  esac

  read -r -p "Do you want to add jack to PATH in '$rcfile'? [y/N] " response
  case "$response" in
    [yY][eE][sS]|[yY]) 
      echo "" >> "$rcfile"
      echo "# jack" >> "$rcfile"
      echo "export PATH=\$PATH:$HOME/.jack/bin" >> "$rcfile"
      ;;
    *)
      echo "You can add jack to your PATH manually by adding the following line to your rc file:"
      echo "export PATH=\$PATH:$HOME/.jack/bin"
      ;;
  esac
}

echo "Installation complete"