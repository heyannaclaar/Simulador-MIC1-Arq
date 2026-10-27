package util;

import controle.MemoriaDeControle;
import controle.Microinstrucao;
import modelo.Registradores;

import java.util.ArrayList;

public class MicroMontador {

    private ArrayList<String> erros = new ArrayList<>();

    public ArrayList<String> getErros() {
        return erros;
    }

    public MemoriaDeControle monta(String codigo) throws ErroDeMontagemException {
        erros.clear();
        MemoriaDeControle mc = new MemoriaDeControle();
        String[] linhas = codigo.split("\n");
        int proximo = 0;

        for (int i = 0; i < linhas.length; i++) {
            String linha = tiraComentario(linhas[i]).trim();
            if (linha.isEmpty()) continue;

            int endereco = proximo;
            int doisPontos = linha.indexOf(':');
            if (doisPontos > 0 && linha.substring(0, doisPontos).trim().matches("\\d+")) {
                endereco = Integer.parseInt(linha.substring(0, doisPontos).trim());
                linha = linha.substring(doisPontos + 1).trim();
            }
            if (endereco > 255) {
                erros.add("Linha " + (i + 1) + ": endereço maior que 255");
                continue;
            }
            if (mc.get(endereco) != null) {
                erros.add("Linha " + (i + 1) + ": endereço " + endereco + " repetido");
            }
            try {
                Microinstrucao m = montaLinha(linha);
                m.endereco = endereco;
                m.texto = linha;
                mc.set(endereco, m);
            } catch (Exception e) {
                erros.add("Linha " + (i + 1) + ": " + e.getMessage());
            }
            proximo = endereco + 1;
        }
        if (mc.get(0) == null && erros.isEmpty()) {
            erros.add("O microprograma precisa começar no endereço 0");
        }
        if (!erros.isEmpty()) throw new ErroDeMontagemException(erros);
        return mc;
    }

    private String tiraComentario(String s) {
        int h = s.indexOf('#');
        if (h >= 0) s = s.substring(0, h);
        return s.replaceAll("\\{[^}]*\\}", "");
    }

    private Microinstrucao montaLinha(String linha) throws Exception {
        Microinstrucao m = new Microinstrucao();
        String origemMar = null;
        String expressao = null;
        ArrayList<String> destinos = new ArrayList<>();

        String[] partes = linha.split(";");
        for (String p : partes) {
            String cmd = p.trim().toLowerCase();
            if (cmd.isEmpty()) continue;

            if (cmd.equals("rd")) {
                m.rd = 1;
            } else if (cmd.equals("wr")) {
                m.wr = 1;
            } else if (cmd.startsWith("if")) {
                String[] t = cmd.split("\\s+");
                if (t.length != 5 || !t[2].equals("then") || !t[3].equals("goto")) {
                    throw new Exception("desvio mal escrito: " + p.trim());
                }
                if (t[1].equals("n")) m.cond = 1;
                else if (t[1].equals("z")) m.cond = 2;
                else throw new Exception("condição deve ser n ou z");
                m.addr = numero(t[4]);
            } else if (cmd.startsWith("goto")) {
                m.cond = 3;
                m.addr = numero(cmd.substring(4).trim());
            } else if (cmd.contains(":=")) {
                String[] lr = cmd.split(":=");
                String dst = lr[0].trim();
                String exp = lr[1].replace(" ", "");
                if (dst.equals("mar")) {
                    origemMar = exp;
                } else {
                    if (expressao != null && !expressao.equals(exp)) {
                        throw new Exception("só pode ter uma operação da ULA por microinstrução");
                    }
                    expressao = exp;
                    destinos.add(dst);
                }
            } else {
                throw new Exception("comando desconhecido: " + p.trim());
            }
        }
        if (m.rd == 1 && m.wr == 1) throw new Exception("rd e wr na mesma microinstrução");

        // MAR
        int regB = -1;
        if (origemMar != null) {
            regB = Registradores.numero(origemMar);
            if (regB < 0) throw new Exception("mar só pode receber um registrador");
            m.mar = 1;
            m.b = regB;
        }

        if (expressao != null) {
            montaExpressao(m, expressao, regB);
        } else {
            m.ula = 2;
        }

        for (String d : destinos) {
            if (d.equals("mbr")) {
                m.mbr = 1;
            } else if (d.equals("alu")) {
                // n e z
            } else {
                int r = Registradores.numero(d);
                if (r < 0) throw new Exception("registrador desconhecido: " + d);
                if (Registradores.ehConstante(r)) throw new Exception("não pode escrever no registrador " + d);
                if (m.enc == 1 && m.c != r) throw new Exception("o barramento C só grava um registrador");
                m.enc = 1;
                m.c = r;
            }
        }
        return m;
    }

    // ULA e deslocador
    private void montaExpressao(Microinstrucao m, String exp, int regB) throws Exception {
        if (exp.startsWith("lshift(") && exp.endsWith(")")) {
            m.desl = 2;
            exp = exp.substring(7, exp.length() - 1);
        } else if (exp.startsWith("rshift(") && exp.endsWith(")")) {
            m.desl = 1;
            exp = exp.substring(7, exp.length() - 1);
        }

        String op1, op2 = null;
        if (exp.startsWith("band(") && exp.endsWith(")")) {
            m.ula = 1;
            String[] ops = exp.substring(5, exp.length() - 1).split(",");
            if (ops.length != 2) throw new Exception("band precisa de dois operandos");
            op1 = ops[0];
            op2 = ops[1];
        } else if (exp.startsWith("inv(") && exp.endsWith(")")) {
            m.ula = 3;
            op1 = exp.substring(4, exp.length() - 1);
        } else if (exp.indexOf('+', 1) > 0) {
            m.ula = 0;
            int mais = exp.indexOf('+', 1);
            op1 = exp.substring(0, mais);
            op2 = exp.substring(mais + 1);
        } else {
            m.ula = 2;
            op1 = exp;
        }

        if (op2 == null) {
            colocaNoA(m, op1);
            if (regB < 0) m.b = 0;
            return;
        }

        if (op1.equals("mbr") || op2.equals("mbr")) {
            String outro = op1.equals("mbr") ? op2 : op1;
            m.amux = 1;
            int r = registrador(outro);
            if (regB >= 0 && regB != r) throw new Exception("conflito no barramento B (mar e ULA)");
            m.b = r;
        } else if (regB >= 0) {
            int r1 = registrador(op1), r2 = registrador(op2);
            if (r1 == regB) {
                m.a = r2;
            } else if (r2 == regB) {
                m.a = r1;
            } else {
                throw new Exception("conflito no barramento B (mar e ULA)");
            }
            m.b = regB;
        } else {
            m.a = registrador(op1);
            m.b = registrador(op2);
        }
    }

    private void colocaNoA(Microinstrucao m, String op) throws Exception {
        if (op.equals("mbr")) {
            m.amux = 1;
        } else {
            m.a = registrador(op);
        }
    }

    private int registrador(String nome) throws Exception {
        int r = Registradores.numero(nome);
        if (r < 0) throw new Exception("registrador desconhecido: " + nome);
        return r;
    }

    private int numero(String s) throws Exception {
        try {
            int n = Integer.parseInt(s.trim());
            if (n < 0 || n > 255) throw new Exception("endereço de desvio fora de 0..255");
            return n;
        } catch (NumberFormatException e) {
            throw new Exception("endereço de desvio inválido: " + s);
        }
    }
}
