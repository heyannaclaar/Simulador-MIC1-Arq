package modelo.cache;

import modelo.MemoriaPrincipal;

public class CacheMapeamentoDireto extends Cache {

    public CacheMapeamentoDireto(String nome, MemoriaPrincipal mp, int linhas, int palavrasPorBloco,
                                 boolean writeBack, int penalidade) {
        super(nome, mp, linhas, 1, palavrasPorBloco, writeBack, penalidade);
    }

    @Override
    public String tipo() {
        return "Mapeamento direto";
    }
}
