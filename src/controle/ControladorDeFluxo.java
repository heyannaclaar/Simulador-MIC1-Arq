package controle;

public class ControladorDeFluxo {

    public static final int NAO_DESVIA = 0, DESVIA_SE_N = 1, DESVIA_SE_Z = 2, DESVIA_SEMPRE = 3;

    private boolean n;
    private boolean z;

    public void setN(boolean n) {
        this.n = n;
    }

    public void setZ(boolean z) {
        this.z = z;
    }

    public boolean getN() {
        return n;
    }

    public boolean getZ() {
        return z;
    }

    public boolean desvia(int cond) {
        if (cond == DESVIA_SEMPRE) return true;
        if (cond == DESVIA_SE_N && n) return true;
        if (cond == DESVIA_SE_Z && z) return true;
        return false;
    }

    public void reset() {
        n = false;
        z = false;
    }
}
