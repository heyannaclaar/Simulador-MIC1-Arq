package modelo.cache;

import modelo.MemoriaPrincipal;

public class CacheAssociativa extends Cache {

    public CacheAssociativa(String nome, MemoriaPrincipal mp, int linhas, int palavrasPorBloco,
                            boolean writeBack, int penalidade) {
        super(nome, mp, 1, linhas, palavrasPorBloco, writeBack, penalidade);
    }

    @Override
    public String tipo() {
        return "Totalmente associativa";
    }
}
