package modelo.cache;

import modelo.MemoriaPrincipal;

public abstract class Cache {

    public class Linha {
        public boolean valida;
        public boolean suja;
        public int tag;
        public int[] dados;
        public long ultimoUso;
    }

    private String nome;
    private MemoriaPrincipal mp;

    private boolean ativa = true;
    protected int conjuntos;
    protected int vias;
    protected int palavrasPorBloco;
    private boolean writeBack;
    private int penalidade;

    private Linha[][] linhas;
    private long relogio;

    private int leituras, escritas, acertos, faltas, writeBacks;
    private int esperaAcumulada;

    private int ultimoEndereco = -1;
    private int ultimoConjunto = -1;
    private int ultimaVia = -1;
    private int viaEncontrada = -1;
    private boolean ultimoAcerto;
    private boolean ultimoFoiEscrita;

    public Cache(String nome, MemoriaPrincipal mp, int conjuntos, int vias, int palavrasPorBloco,
                 boolean writeBack, int penalidade) {
        this.nome = nome;
        this.mp = mp;
        this.conjuntos = conjuntos;
        this.vias = vias;
        this.palavrasPorBloco = palavrasPorBloco;
        this.writeBack = writeBack;
        this.penalidade = penalidade;
        criaLinhas();
    }

    public abstract String tipo();

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
        criaLinhas();
    }

    private void criaLinhas() {
        linhas = new Linha[conjuntos][vias];
        for (int c = 0; c < conjuntos; c++) {
            for (int v = 0; v < vias; v++) {
                linhas[c][v] = new Linha();
                linhas[c][v].dados = new int[palavrasPorBloco];
            }
        }
        relogio = 0;
        leituras = escritas = acertos = faltas = writeBacks = 0;
        esperaAcumulada = 0;
        ultimoEndereco = ultimoConjunto = ultimaVia = -1;
    }

    public void reset() {
        criaLinhas();
    }

    public int conjuntoDo(int endereco) {
        return (endereco / palavrasPorBloco) % conjuntos;
    }

    public int tagDo(int endereco) {
        return (endereco / palavrasPorBloco) / conjuntos;
    }

    public int palavraDo(int endereco) {
        return endereco % palavrasPorBloco;
    }

    public int ler(int endereco) {
        endereco = endereco & 0xFFF;
        if (!ativa) {
            esperaAcumulada += penalidade;
            ultimoEndereco = endereco;
            ultimoAcerto = false;
            ultimoFoiEscrita = false;
            return mp.ler(endereco);
        }
        leituras++;
        ultimoFoiEscrita = false;
        Linha l = buscaOuCarrega(endereco);
        return l.dados[palavraDo(endereco)];
    }

    public void escrever(int endereco, int valor) {
        endereco = endereco & 0xFFF;
        valor = valor & 0xFFFF;
        if (!ativa) {
            esperaAcumulada += penalidade;
            ultimoEndereco = endereco;
            ultimoAcerto = false;
            ultimoFoiEscrita = true;
            mp.escrever(endereco, valor);
            return;
        }
        escritas++;
        ultimoFoiEscrita = true;
        if (writeBack) {
            Linha l = buscaOuCarrega(endereco);
            l.dados[palavraDo(endereco)] = valor;
            l.suja = true;
        } else {
            // write-through
            Linha l = procura(endereco);
            ultimoEndereco = endereco;
            ultimoConjunto = conjuntoDo(endereco);
            if (l != null) {
                acertos++;
                ultimoAcerto = true;
                ultimaVia = viaEncontrada;
                l.dados[palavraDo(endereco)] = valor;
                l.ultimoUso = ++relogio;
            } else {
                faltas++;
                ultimoAcerto = false;
                ultimaVia = -1;
            }
            mp.escrever(endereco, valor);
            esperaAcumulada += penalidade;
        }
    }

    private Linha procura(int endereco) {
        int c = conjuntoDo(endereco);
        int tag = tagDo(endereco);
        for (int v = 0; v < vias; v++) {
            if (linhas[c][v].valida && linhas[c][v].tag == tag) {
                viaEncontrada = v;
                return linhas[c][v];
            }
        }
        return null;
    }

    private Linha buscaOuCarrega(int endereco) {
        int c = conjuntoDo(endereco);
        int tag = tagDo(endereco);
        ultimoEndereco = endereco;
        ultimoConjunto = c;

        Linha l = procura(endereco);
        if (l != null) {
            acertos++;
            ultimoAcerto = true;
            ultimaVia = viaEncontrada;
            l.ultimoUso = ++relogio;
            return l;
        }

        // falta (LRU)
        faltas++;
        ultimoAcerto = false;
        int escolhida = 0;
        for (int v = 0; v < vias; v++) {
            if (!linhas[c][v].valida) {
                escolhida = v;
                break;
            }
            if (linhas[c][v].ultimoUso < linhas[c][escolhida].ultimoUso) {
                escolhida = v;
            }
        }
        l = linhas[c][escolhida];
        if (l.valida && l.suja) {
            int base = (l.tag * conjuntos + c) * palavrasPorBloco;
            for (int i = 0; i < palavrasPorBloco; i++) {
                mp.escrever(base + i, l.dados[i]);
            }
            writeBacks++;
            esperaAcumulada += penalidade;
        }
        int base = (endereco / palavrasPorBloco) * palavrasPorBloco;
        for (int i = 0; i < palavrasPorBloco; i++) {
            l.dados[i] = mp.ler(base + i);
        }
        esperaAcumulada += penalidade;
        l.valida = true;
        l.suja = false;
        l.tag = tag;
        l.ultimoUso = ++relogio;
        ultimaVia = escolhida;
        return l;
    }

    public void atualizaSeTiver(int endereco, int valor) {
        if (!ativa) return;
        Linha l = procura(endereco & 0xFFF);
        if (l != null) {
            l.dados[palavraDo(endereco & 0xFFF)] = valor & 0xFFFF;
        }
    }

    public int espiar(int endereco) {
        if (ativa) {
            int c = conjuntoDo(endereco);
            int tag = tagDo(endereco);
            for (int v = 0; v < vias; v++) {
                if (linhas[c][v].valida && linhas[c][v].tag == tag) {
                    return linhas[c][v].dados[palavraDo(endereco)];
                }
            }
        }
        return mp.espiar(endereco);
    }

    public int pegaEspera() {
        int e = esperaAcumulada;
        esperaAcumulada = 0;
        return e;
    }

    public double taxaAcerto() {
        int total = acertos + faltas;
        if (total == 0) return 0;
        return 100.0 * acertos / total;
    }

    public String getNome() { return nome; }
    public MemoriaPrincipal getMp() { return mp; }
    public boolean isAtiva() { return ativa; }
    public int getConjuntos() { return conjuntos; }
    public int getVias() { return vias; }
    public int getPalavrasPorBloco() { return palavrasPorBloco; }
    public boolean isWriteBack() { return writeBack; }
    public int getPenalidade() { return penalidade; }
    public Linha[][] getLinhas() { return linhas; }
    public int getLeituras() { return leituras; }
    public int getEscritas() { return escritas; }
    public int getAcertos() { return acertos; }
    public int getFaltas() { return faltas; }
    public int getWriteBacks() { return writeBacks; }
    public int getUltimoEndereco() { return ultimoEndereco; }
    public int getUltimoConjunto() { return ultimoConjunto; }
    public int getUltimaVia() { return ultimaVia; }
    public boolean isUltimoAcerto() { return ultimoAcerto; }
    public boolean isUltimoFoiEscrita() { return ultimoFoiEscrita; }
}
