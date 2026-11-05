/ Exemplo 2: r = x * y usando somas sucessivas
laco:   LODD y
        JZER fim
        SUBD um
        STOD y
        LODD r
        ADDD x
        STOD r
        JUMP laco
fim:    JUMP fim

x:      7
y:      6
r:      0
um:     1
