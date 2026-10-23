package controle;

public class MemoriaDeControle {

    public static final int TAMANHO = 256;

    private Microinstrucao[] micro = new Microinstrucao[TAMANHO];

    public Microinstrucao get(int endereco) {
        return micro[endereco & 0xFF];
    }

    public void set(int endereco, Microinstrucao m) {
        micro[endereco & 0xFF] = m;
    }

    public int quantidade() {
        int n = 0;
        for (Microinstrucao m : micro) {
            if (m != null) n++;
        }
        return n;
    }
}
