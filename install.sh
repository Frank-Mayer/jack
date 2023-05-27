#!/bin/bash

mvn clean package
mkdir -p ~/.jack/bin
mv target/jack.jar ~/.jack/bin/

# jar wrapper
cat << 'END_SCRIPT' > ~/.jack/bin/jack
#!/bin/bash

java -jar ~/.jack/bin/jack.jar "$@"

END_SCRIPT

chmod +x ~/.jack/bin/jack

export PATH=$PATH:~/.jack/bin

echo "Make sure to add ~/.jack/bin to your PATH"