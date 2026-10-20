package modelo;

public class MemoriaPrincipal {

    public static final int TAMANHO = 4096;

    private int[] palavras = new int[TAMANHO];
    private int leituras;
    private int escritas;

    public int ler(int endereco) {
        leituras++;
        return palavras[endereco & 0xFFF];
    }

    public void escrever(int endereco, int valor) {
        escritas++;
        palavras[endereco & 0xFFF] = valor & 0xFFFF;
    }

    public int espiar(int endereco) {
        return palavras[endereco & 0xFFF];
    }

    public void gravar(int endereco, int valor) {
        palavras[endereco & 0xFFF] = valor & 0xFFFF;
    }

    public void limpar() {
        for (int i = 0; i < TAMANHO; i++) {
            palavras[i] = 0;
        }
        leituras = 0;
        escritas = 0;
    }

    public void zerarContadores() {
        leituras = 0;
        escritas = 0;
    }

    public int getLeituras() {
        return leituras;
    }

    public int getEscritas() {
        return escritas;
    }
}
