@echo off
if exist bin rmdir /s /q bin
mkdir bin
dir /s /b src\*.java > fontes.txt
javac --release 17 -encoding UTF-8 -d bin @fontes.txt
if errorlevel 1 goto fim
del fontes.txt
xcopy /e /i /q src\recursos bin\recursos > nul
jar cfe SimuladorMIC1.jar visao.JanelaPrincipal -C bin .
echo pronto: java -jar SimuladorMIC1.jar
:fim
