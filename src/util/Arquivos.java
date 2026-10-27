package util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Arquivos {

    public static final String[] EXEMPLOS = {
            "1-soma.asm", "2-multiplicacao.asm", "3-fatorial.asm", "4-soma-vetor.asm", "5-conflito-cache.asm"
    };

    public static String lerRecurso(String caminho) {
        try {
            InputStream in = Arquivos.class.getResourceAsStream("/recursos/" + caminho);
            if (in == null) return "";
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            in.close();
            return out.toString("UTF-8").replace("\r", "");
        } catch (Exception e) {
            return "";
        }
    }

    public static String microprograma() {
        return lerRecurso("microprograma.mal");
    }

    public static String exemplo(String nome) {
        return lerRecurso("exemplos/" + nome);
    }
}
