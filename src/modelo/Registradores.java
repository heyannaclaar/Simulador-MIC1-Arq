package modelo;

public class Registradores {

    public static final int PC = 0, AC = 1, SP = 2, IR = 3, TIR = 4, ZERO = 5, MAIS_UM = 6, MENOS_UM = 7,
            AMASK = 8, SMASK = 9, A = 10, B = 11, C = 12, D = 13, E = 14, F = 15;

    public static final String[] NOMES = {"PC", "AC", "SP", "IR", "TIR", "0", "+1", "-1",
            "AMASK", "SMASK", "A", "B", "C", "D", "E", "F"};

    public static final int SP_INICIAL = 4095;

    private int[] valores = new int[16];

    public Registradores() {
        reset();
    }

    public void reset() {
        for (int i = 0; i < 16; i++) {
            valores[i] = 0;
        }
        valores[MAIS_UM] = 1;
        valores[MENOS_UM] = 0xFFFF;
        valores[AMASK] = 0x0FFF;
        valores[SMASK] = 0x00FF;
        valores[SP] = SP_INICIAL;
    }

    public int get(int r) {
        return valores[r];
    }

    public void set(int r, int valor) {
        if (ehConstante(r)) {
            return;
        }
        valores[r] = valor & 0xFFFF;
    }

    public static boolean ehConstante(int r) {
        return r >= ZERO && r <= SMASK;
    }

    public static int numero(String nome) {
        String n = nome.trim().toLowerCase();
        if (n.equals("(-1)") || n.equals("-1")) return MENOS_UM;
        if (n.equals("1") || n.equals("+1") || n.equals("(+1)")) return MAIS_UM;
        if (n.equals("0")) return ZERO;
        for (int i = 0; i < NOMES.length; i++) {
            if (NOMES[i].toLowerCase().equals(n)) return i;
        }
        return -1;
    }
}
