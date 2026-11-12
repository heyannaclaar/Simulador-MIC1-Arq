#!/bin/sh

Xvfb :0 -screen 0 1920x1080x24 &
sleep 1
fluxbox > /dev/null 2>&1 &
x11vnc -display :0 -forever -shared -nopw -quiet > /dev/null 2>&1 &
websockify --web /usr/share/novnc 6080 localhost:5900 > /dev/null 2>&1 &

echo "Simulador rodando. Abra no navegador: http://localhost:6080/vnc.html"

cd /app/exemplos
while true; do
    java -jar /app/SimuladorMIC1.jar
    sleep 1
done
