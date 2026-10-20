package modelo;

public class Deslocador {
    public static final String[] OPERACOES = {"-", "rshift", "lshift", "?"};
    private int saida;
    public int desloca(int operacao, int entrada) {
        if (operacao == 1) {
            saida = (entrada >>> 1) & 0xFFFF;
        } else if (operacao == 2) {
            saida = (entrada << 1) & 0xFFFF;
        } else {
            saida = entrada & 0xFFFF;
        }
        return saida;
    }

    public int getSaida() {
        return saida;
    }

    public void reset() {
        saida = 0;
    }
}
