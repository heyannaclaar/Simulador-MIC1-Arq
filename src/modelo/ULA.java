package modelo;

public class ULA {

    public static final String[] OPERACOES = {"A + B", "A AND B", "A", "NOT A"};

    private int resultado;
    private boolean n;
    private boolean z;

    public int calcula(int operacao, int a, int b) {
        switch (operacao) {
            case 0:
                resultado = (a + b) & 0xFFFF;
                break;
            case 1:
                resultado = a & b;
                break;
            case 2:
                resultado = a;
                break;
            default:
                resultado = (~a) & 0xFFFF;
                break;
        }
        n = (resultado & 0x8000) != 0;
        z = resultado == 0;
        return resultado;
    }

    public int getResultado() {
        return resultado;
    }

    public boolean getN() {
        return n;
    }

    public boolean getZ() {
        return z;
    }

    public void reset() {
        resultado = 0;
        n = false;
        z = false;
    }
}
