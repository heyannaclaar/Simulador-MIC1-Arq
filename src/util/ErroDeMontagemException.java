package util;

import java.util.ArrayList;

public class ErroDeMontagemException extends Exception {

    private ArrayList<String> erros;

    public ErroDeMontagemException(ArrayList<String> erros) {
        super(String.join("\n", erros));
        this.erros = new ArrayList<>(erros);
    }

    public ArrayList<String> getErros() {
        return erros;
    }
}
