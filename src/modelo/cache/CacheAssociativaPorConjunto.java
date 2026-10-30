package modelo.cache;

import modelo.MemoriaPrincipal;

public class CacheAssociativaPorConjunto extends Cache {

    public CacheAssociativaPorConjunto(String nome, MemoriaPrincipal mp, int linhas, int vias, int palavrasPorBloco,
                                       boolean writeBack, int penalidade) {
        super(nome, mp, linhas / vias, vias, palavrasPorBloco, writeBack, penalidade);
    }

    @Override
    public String tipo() {
        return "Associativa por conjunto (" + vias + " vias)";
    }
}
