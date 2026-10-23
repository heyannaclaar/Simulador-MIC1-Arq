package controle;

public class Microinstrucao {

    public int amux;
    public int cond;
    public int ula;
    public int desl;
    public int mbr;
    public int mar;
    public int rd;
    public int wr;
    public int enc;
    public int c;
    public int b;
    public int a;
    public int addr;

    public int endereco;
    public String texto = "";

    public int codifica() {
        int w = 0;
        w = (w << 1) | amux;
        w = (w << 2) | cond;
        w = (w << 2) | ula;
        w = (w << 2) | desl;
        w = (w << 1) | mbr;
        w = (w << 1) | mar;
        w = (w << 1) | rd;
        w = (w << 1) | wr;
        w = (w << 1) | enc;
        w = (w << 4) | c;
        w = (w << 4) | b;
        w = (w << 4) | a;
        w = (w << 8) | addr;
        return w;
    }

    public String binario() {
        return bin(amux, 1) + " " + bin(cond, 2) + " " + bin(ula, 2) + " " + bin(desl, 2) + " "
                + bin(mbr, 1) + " " + bin(mar, 1) + " " + bin(rd, 1) + " " + bin(wr, 1) + " " + bin(enc, 1) + " "
                + bin(c, 4) + " " + bin(b, 4) + " " + bin(a, 4) + " " + bin(addr, 8);
    }

    public String hexa() {
        return String.format("%08X", codifica());
    }

    public static String bin(int valor, int bits) {
        String s = Integer.toBinaryString(valor);
        while (s.length() < bits) s = "0" + s;
        if (s.length() > bits) s = s.substring(s.length() - bits);
        return s;
    }
}
