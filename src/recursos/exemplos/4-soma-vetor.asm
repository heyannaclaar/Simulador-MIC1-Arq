/ Exemplo 4: soma os 8 elementos do vetor v
        LOCO v
        STOD p
laco:   LODD n
        JZER fim
        SUBD um
        STOD n
        LODD p
        PSHI
        POP
        ADDD soma
        STOD soma
        LODD p
        ADDD um
        STOD p
        JUMP laco
fim:    JUMP fim

n:      8
p:      0
soma:   0
um:     1
v:      3
        1
        4
        1
        5
        9
        2
        6
