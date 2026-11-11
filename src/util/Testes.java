package util;

import controle.MemoriaDeControle;
import controle.Processador;
import modelo.cache.Cache;
import modelo.cache.CacheAssociativa;
import modelo.cache.CacheAssociativaPorConjunto;
import modelo.cache.CacheMapeamentoDireto;

public class Testes {

    static int falhas = 0;

    public static void main(String[] args) {
        MemoriaDeControle mc = null;
        try {
            mc = new MicroMontador().monta(Arquivos.microprograma());
        } catch (ErroDeMontagemException e) {
            System.out.println("Erro no microprograma: " + e.getMessage());
            System.exit(1);
        }
        System.out.println("Microprograma montado: " + mc.quantidade() + " microinstrucoes");

        testa(mc, "1-soma.asm", "c", 42);
        testa(mc, "2-multiplicacao.asm", "r", 42);
        testa(mc, "3-fatorial.asm", "fat", 120);
        testa(mc, "4-soma-vetor.asm", "soma", 31);
        testa(mc, "5-conflito-cache.asm", "1000", 45);

        if (falhas == 0) {
            System.out.println("Todos os testes passaram");
        } else {
            System.out.println(falhas + " teste(s) falharam");
            System.exit(1);
        }
    }

    static Cache criaCache(String nome, Processador p, int[] c) {
        Cache cache;
        int linhas = c[1] * c[2];
        if (c[2] == 1) {
            cache = new CacheMapeamentoDireto(nome, p.mp, linhas, c[3], c[4] == 1, 8);
        } else if (c[1] == 1) {
            cache = new CacheAssociativa(nome, p.mp, linhas, c[3], c[4] == 1, 8);
        } else {
            cache = new CacheAssociativaPorConjunto(nome, p.mp, linhas, c[2], c[3], c[4] == 1, 8);
        }
        cache.setAtiva(c[0] == 1);
        return cache;
    }

    static void testa(MemoriaDeControle mc, String arquivo, String variavel, int esperado) {
        Montador m = new Montador();
        try {
            m.monta(Arquivos.exemplo(arquivo));
        } catch (ErroDeMontagemException e) {
            System.out.println("FALHOU " + arquivo + ": " + e.getMessage());
            falhas++;
            return;
        }
        int endereco;
        if (variavel.matches("\\d+")) endereco = Integer.parseInt(variavel);
        else if (m.getRotulos().containsKey(variavel)) endereco = m.getRotulos().get(variavel);
        else endereco = m.getVariaveis().get(variavel);

        // {ativa, conjuntos, vias, palavras por bloco, write-back}
        int[][] configs = {
                {1, 16, 1, 4, 1},
                {0, 16, 1, 4, 1},
                {1, 8, 2, 4, 1},
                {1, 1, 8, 4, 0},
                {1, 4, 2, 8, 0},
        };
        String[] nomes = {"direto 16x4 WB", "sem cache", "2 vias WB", "assoc. total WT", "2 vias 8pal WT"};

        for (int i = 0; i < configs.length; i++) {
            int[] c = configs[i];
            Processador p = new Processador();
            p.mc = mc;
            p.cacheInstrucoes = criaCache("Cache de Instruções", p, c);
            p.cacheDados = criaCache("Cache de Dados", p, c);
            p.carregaPrograma(m.getPalavras());
            int limite = 0;
            while (!p.parado && limite < 5000000) {
                p.passaCiclo();
                limite++;
            }
            int valor = p.valorMemoria(endereco);
            boolean ok = p.parado && p.mensagem.startsWith("Fim") && valor == esperado;
            if (!ok) falhas++;
            System.out.printf("%-6s %-22s %-16s %s = %-5d ciclos = %-6d instrucoes = %-5d CPI = %.2f%n",
                    ok ? "OK" : "FALHOU", arquivo, nomes[i], variavel, valor, p.ciclos, p.instrucoes, p.cpi());
        }
    }
}
