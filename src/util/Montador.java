package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Montador {

    static final HashMap<String, String> OPCODES = new HashMap<>();
    static final HashMap<String, Integer> BITS_OPERANDO = new HashMap<>();

    static {
        poe("LODD", "0000", 12);
        poe("STOD", "0001", 12);
        poe("ADDD", "0010", 12);
        poe("SUBD", "0011", 12);
        poe("JPOS", "0100", 12);
        poe("JZER", "0101", 12);
        poe("JUMP", "0110", 12);
        poe("LOCO", "0111", 12);
        poe("LODL", "1000", 12);
        poe("STOL", "1001", 12);
        poe("ADDL", "1010", 12);
        poe("SUBL", "1011", 12);
        poe("JNEG", "1100", 12);
        poe("JNZE", "1101", 12);
        poe("CALL", "1110", 12);
        poe("PSHI", "1111000000000000", 0);
        poe("POPI", "1111001000000000", 0);
        poe("PUSH", "1111010000000000", 0);
        poe("POP", "1111011000000000", 0);
        poe("RETN", "1111100000000000", 0);
        poe("SWAP", "1111101000000000", 0);
        poe("INSP", "11111100", 8);
        poe("DESP", "11111110", 8);
    }

    private static void poe(String nome, String opcode, int bits) {
        OPCODES.put(nome, opcode);
        BITS_OPERANDO.put(nome, bits);
    }

    private class Linha {
        int numeroLinha;
        int endereco;
        String instrucao;
        String operando;
        int valorDado;
        String texto;
    }

    private ArrayList<String> erros = new ArrayList<>();
    private ArrayList<Linha> linhas = new ArrayList<>();
    private HashMap<String, Integer> rotulos = new HashMap<>();
    private LinkedHashMap<String, Integer> variaveis = new LinkedHashMap<>();
    private int[] palavras = new int[0];
    private ArrayList<String> listagem = new ArrayList<>();
    private int[] linhaDoEndereco = new int[0];

    public void monta(String codigo) throws ErroDeMontagemException {
        erros.clear();
        linhas.clear();
        rotulos.clear();
        variaveis.clear();
        listagem.clear();

        // 1a passada
        String[] texto = codigo.split("\n");
        int endereco = 0;
        for (int i = 0; i < texto.length; i++) {
            String s = tiraComentario(texto[i]).trim();
            if (s.isEmpty()) continue;

            int dp = s.indexOf(':');
            if (dp >= 0) {
                String rotulo = s.substring(0, dp).trim();
                if (!rotulo.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    erros.add("Linha " + (i + 1) + ": rótulo inválido '" + rotulo + "'");
                    continue;
                }
                if (rotulos.containsKey(rotulo)) {
                    erros.add("Linha " + (i + 1) + ": rótulo '" + rotulo + "' repetido");
                }
                rotulos.put(rotulo, endereco);
                s = s.substring(dp + 1).trim();
                if (s.isEmpty()) continue;
            }

            Linha l = new Linha();
            l.numeroLinha = i + 1;
            l.endereco = endereco;
            l.texto = texto[i].trim();
            String[] partes = s.split("\\s+");
            String primeira = partes[0].toUpperCase();

            if (OPCODES.containsKey(primeira)) {
                l.instrucao = primeira;
                int bits = BITS_OPERANDO.get(primeira);
                if (bits == 0 && partes.length > 1) {
                    erros.add("Linha " + (i + 1) + ": " + primeira + " não tem operando");
                } else if (bits > 0 && partes.length != 2) {
                    erros.add("Linha " + (i + 1) + ": " + primeira + " precisa de um operando");
                } else if (bits > 0) {
                    l.operando = partes[1];
                }
            } else if (partes.length == 1 && ehNumero(partes[0])) {
                l.valorDado = Integer.parseInt(partes[0]);
                if (l.valorDado < -32768 || l.valorDado > 65535) {
                    erros.add("Linha " + (i + 1) + ": valor não cabe em 16 bits");
                }
            } else {
                erros.add("Linha " + (i + 1) + ": instrução desconhecida '" + partes[0] + "'");
                continue;
            }
            linhas.add(l);
            endereco++;
        }

        // variaveis
        for (Linha l : linhas) {
            if (l.operando != null && !ehNumero(l.operando) && !rotulos.containsKey(l.operando)
                    && !variaveis.containsKey(l.operando)) {
                if (!l.operando.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    erros.add("Linha " + l.numeroLinha + ": operando inválido '" + l.operando + "'");
                    continue;
                }
                variaveis.put(l.operando, endereco);
                endereco++;
            }
        }
        if (endereco > 4096) {
            erros.add("Programa não cabe na memória (4096 palavras)");
        }
        if (!erros.isEmpty()) throw new ErroDeMontagemException(erros);

        // 2a passada
        palavras = new int[endereco];
        linhaDoEndereco = new int[endereco];
        for (Linha l : linhas) {
            linhaDoEndereco[l.endereco] = l.numeroLinha;
            if (l.instrucao == null) {
                palavras[l.endereco] = l.valorDado & 0xFFFF;
            } else {
                String opcode = OPCODES.get(l.instrucao);
                int bits = BITS_OPERANDO.get(l.instrucao);
                int valor = Integer.parseInt(opcode, 2) << bits;
                if (bits > 0) {
                    int op = valorOperando(l);
                    int maximo = (1 << bits) - 1;
                    if (op < 0 || op > maximo) {
                        erros.add("Linha " + l.numeroLinha + ": operando " + op + " fora de 0.." + maximo);
                    }
                    valor = valor | (op & maximo);
                }
                palavras[l.endereco] = valor;
            }
            listagem.add(String.format("%4d  %s  %s", l.endereco, binario(palavras[l.endereco]), l.texto));
        }
        for (String v : variaveis.keySet()) {
            int e = variaveis.get(v);
            listagem.add(String.format("%4d  %s  %s (variável)", e, binario(0), v));
        }
        if (!erros.isEmpty()) throw new ErroDeMontagemException(erros);
    }

    private int valorOperando(Linha l) {
        String op = l.operando;
        if (ehNumero(op)) return Integer.parseInt(op);
        if (rotulos.containsKey(op)) return rotulos.get(op);
        return variaveis.get(op);
    }

    private boolean ehNumero(String s) {
        return s.matches("-?\\d+");
    }

    private String tiraComentario(String s) {
        int a = s.indexOf('/');
        if (a >= 0) s = s.substring(0, a);
        int b = s.indexOf(';');
        if (b >= 0) s = s.substring(0, b);
        return s;
    }

    public static String binario(int valor) {
        String s = Integer.toBinaryString(valor & 0xFFFF);
        while (s.length() < 16) s = "0" + s;
        return s;
    }

    public String compilado() {
        StringBuilder sb = new StringBuilder();
        for (int p : palavras) {
            sb.append(binario(p)).append("\n");
        }
        return sb.toString();
    }

    public int[] getPalavras() { return palavras; }
    public ArrayList<String> getErros() { return erros; }
    public ArrayList<String> getListagem() { return listagem; }
    public HashMap<String, Integer> getRotulos() { return rotulos; }
    public LinkedHashMap<String, Integer> getVariaveis() { return variaveis; }

    public int linhaDoEndereco(int endereco) {
        if (endereco < 0 || endereco >= linhaDoEndereco.length) return 0;
        return linhaDoEndereco[endereco];
    }

    public String nomeDoEndereco(int endereco) {
        for (String v : variaveis.keySet()) {
            if (variaveis.get(v) == endereco) return v;
        }
        for (String r : rotulos.keySet()) {
            if (rotulos.get(r) == endereco) return r;
        }
        return "";
    }
}
