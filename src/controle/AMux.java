package controle;

public class AMux {

    private int saida;

    public int seleciona(int controle, int latchA, int mbr) {
        if (controle == 1) {
            saida = mbr;
        } else {
            saida = latchA;
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
