#!/bin/sh
rm -rf bin
mkdir bin
javac --release 17 -encoding UTF-8 -d bin $(find src -name "*.java") || exit 1
cp -r src/recursos bin/
jar cfe SimuladorMIC1.jar visao.JanelaPrincipal -C bin .
echo "pronto: java -jar SimuladorMIC1.jar"
