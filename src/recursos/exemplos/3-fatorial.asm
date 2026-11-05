/ Exemplo 3: fat = fatorial(n), recursivo, usando a pilha

        LODD n
        PUSH
        CALL fatorial
        INSP 1
        STOD fat
fim:    JUMP fim

fatorial: LODL 1
        SUBD um
        JZER base
        JNEG base
        PUSH
        CALL fatorial
        INSP 1
        PUSH
        LODL 2
        PUSH
        LOCO 0
        PUSH
mult:   LODL 1
        JZER fimmult
        SUBD um
        STOL 1
        LODL 0
        ADDL 2
        STOL 0
        JUMP mult
fimmult: LODL 0
        INSP 3
        RETN
base:   LOCO 1
        RETN

n:      5
fat:    0
um:     1
