package controle;

public class MMux {

    public int seleciona(boolean desvia, int mpc, int addr) {
        if (desvia) {
            return addr;
        }
        return (mpc + 1) & 0xFF;
    }
}
