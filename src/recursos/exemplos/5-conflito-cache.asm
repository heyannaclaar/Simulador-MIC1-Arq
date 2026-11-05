/ Exemplo 5: conflito na cache de dados
laco:   LODD cont
        JZER fim
        SUBD um
        STOD cont
        LODD 1000
        ADDD 1064
        STOD 1000
        LODD 1064
        ADDD um
        STOD 1064
        JUMP laco
fim:    JUMP fim

cont:   10
um:     1
