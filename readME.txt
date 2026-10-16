SIMULADOR MIC-1
Trabalho de Arquitetura de Computadores

Integrantes:
- Anna Clara Santos Dias Pimentel
- Beatriz de Moura Barboza
- Gabriel Camara Ribeiro Lange
- Joao Gabriel Bastos Aidê
- Jorge Rodrigo Rosário de Almeida

O que tem no trabalho
---------------------
Simulador grafico do MIC-1 (o da aula, com a ISA MAC-1), feito em Java com Swing.

- Montador: transforma o programa em assembly do MAC-1 (LODD, STOD, ADDD, ..., INSP, DESP)
  em binario. O resultado aparece no campo "Compilado" e depois e gravado na MP.
  Tambem tem um montador do microprograma (MAL) que gera as microinstrucoes de 32 bits
  da memoria de controle.
- Microarquitetura desenhada na tela: os 16 registradores, barramentos A, B e C,
  Latch A, Latch B, AMUX, ULA, deslocador, N e Z, MAR, MBR, memoria de controle,
  MIR (com todos os campos), MPC, incrementador (+1), MMUX, decoders A, B e C e a
  logica de controle de fluxo. O que esta sendo usado no subciclo atual fica colorido.
- Cache de instrucoes e cache de dados entre o processador e a MP. Da para escolher
  mapeamento direto, associativa por conjunto ou totalmente associativa, numero de linhas,
  palavras por bloco, write-back ou write-through e a penalidade de falta.

O microprograma usado e o de 79 microinstrucoes da aula (pasta src/recursos/microprograma.mal).


Como rodar (Docker)
-------------------
Precisa ter o Docker instalado. Na pasta do projeto:

    docker compose up --build

Depois abrir no navegador:

    http://localhost:6080/vnc.html

e clicar em "Connect". O simulador abre dentro dessa tela.
(tambem da para conectar com um cliente VNC em localhost:5900)

Sem o docker compose:

    docker build -t simulador-mic1 .
    docker run -p 6080:6080 -p 5900:5900 simulador-mic1


Como rodar sem Docker
---------------------
Precisa do JDK 17 ou mais novo.

    Windows:  compilar.bat
    Linux:    ./compilar.sh

    java -jar SimuladorMIC1.jar

Para rodar os testes (roda todos os exemplos com varias configuracoes de cache e confere
o resultado na memoria):

    java -cp SimuladorMIC1.jar util.Testes


Como usar
---------
1. Escolher um exemplo ou escrever o programa no campo "Assembly".
2. Clicar em "Montar" e depois em "Gravar código na MP".
3. Executar:
   - Executar / Pausar / Resetar
   - Passar um subciclo, Passar um ciclo, Passar uma instrução
   - "Pausa entre subciclos" controla a velocidade (0 = o mais rapido possivel)
   - "Pausar quando PC =" para a execucao quando o PC chega naquele endereco
4. Nas abas da direita da para ver os registradores, a memoria principal, as caches,
   a memoria de controle e o microprograma.

O programa termina quando chega num JUMP para ele mesmo (ex: "fim: JUMP fim").

Sintaxe do assembly:
    / comentario
    rotulo: INSTRUCAO operando
    variavel: 10          (reserva uma palavra com valor 10)
    5                     (linha so com numero tambem reserva uma palavra, bom para vetor)
O operando pode ser numero ou nome. Se o nome nao foi declarado ele vira uma variavel
com valor 0 no final do programa.


Detalhes da simulacao
---------------------
- Palavra de 16 bits, MP com 4096 palavras, memoria de controle com 256 x 32 bits.
- Cada ciclo tem 4 subciclos:
    1: carrega o MIR
    2: carrega os latches A e B
    3: ULA e deslocador (MAR recebe o latch B se MAR = 1)
    4: grava no barramento C / MBR, acesso a memoria e o MMUX escolhe o proximo MPC
- Leitura e escrita precisam de rd (ou wr) em duas microinstrucoes seguidas, igual na aula.
- Quando o MAR e carregado a partir do PC o acesso vai para a cache de instrucoes,
  senao vai para a cache de dados.
- Em uma falta na cache o processador fica parado "penalidade" ciclos esperando a MP.
  Com a cache desligada todo acesso paga a penalidade.
- SP comeca em 4095 e a pilha cresce para baixo.


Exemplos (pasta src/recursos/exemplos)
--------------------------------------
1-soma.asm             c = a + b
2-multiplicacao.asm    multiplicacao por somas sucessivas
3-fatorial.asm         fatorial recursivo com CALL/RETN e variaveis locais na pilha
4-soma-vetor.asm       soma de vetor usando PSHI
5-conflito-cache.asm   mostra conflito na cache de mapeamento direto
                       (comparar os ciclos com a cache associativa por conjunto de 2 vias)


Organizacao do codigo
---------------------
src/modelo         registradores, ULA, deslocador e memoria principal
src/modelo/cache   caches (mapeamento direto, associativa e associativa por conjunto)
src/controle       microinstrucao, memoria de controle, AMUX, MMUX, controlador de fluxo
                   e o processador (ciclo e subciclos)
src/util           montador do MAC-1, montador do microprograma, leitura de arquivos e testes
src/visao      telas (janela principal, desenho do MIC-1, tabela das caches, config. das caches)
src/recursos   microprograma e exemplos
