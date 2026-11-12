# compilacao
FROM eclipse-temurin:17-jdk-jammy AS compilacao
WORKDIR /app
COPY src ./src
RUN mkdir bin \
 && javac -encoding UTF-8 -d bin $(find src -name "*.java") \
 && cp -r src/recursos bin/ \
 && jar cfe SimuladorMIC1.jar visao.JanelaPrincipal -C bin . \
 && java -cp SimuladorMIC1.jar util.Testes

# execucao
FROM eclipse-temurin:17-jre-jammy
ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:0
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb x11vnc fluxbox novnc websockify \
    libxext6 libxrender1 libxtst6 libxi6 fontconfig fonts-dejavu-core \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=compilacao /app/SimuladorMIC1.jar .
COPY src/recursos/exemplos ./exemplos
COPY iniciar.sh .
RUN chmod +x iniciar.sh
EXPOSE 6080 5900
CMD ["./iniciar.sh"]
