#!/bin/bash

mkdir -p ~/.jack/bin
if mvn clean package && mv target/jack.jar ~/.jack/bin/; then
    echo "Build succeeded."
else
    echo "Build failed. Downloading latest release from GitHub..."
    curl -o ~/.jack/bin/jack.jar https://frank-mayer.github.io/jack/jack.jar
fi

# jar wrapper
cat << 'END_SCRIPT' > ~/.jack/bin/jack
#!/bin/bash
java -jar ~/.jack/bin/jack.jar "$@"
END_SCRIPT

chmod +x ~/.jack/bin/jack

export PATH=$PATH:~/.jack/bin

echo "Make sure to add ~/.jack/bin to your PATH"