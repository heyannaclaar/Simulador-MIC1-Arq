package visao;

import controle.Microinstrucao;
import controle.Processador;
import modelo.cache.Cache;
import modelo.Deslocador;
import modelo.Registradores;
import modelo.ULA;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

public class PainelDiagrama extends JPanel {

    private static final int LARGURA = 1000, ALTURA = 720;

    private static final Color FUNDO = new Color(250, 250, 250);
    private static final Color PRETO = new Color(40, 40, 40);
    private static final Color CINZA = new Color(170, 170, 170);
    private static final Color AZUL = new Color(30, 100, 210);
    private static final Color AZUL_CLARO = new Color(215, 230, 255);
    private static final Color VERMELHO = new Color(200, 40, 40);
    private static final Color VERMELHO_CLARO = new Color(255, 220, 220);
    private static final Color VERDE = new Color(30, 140, 60);
    private static final Color VERDE_CLARO = new Color(215, 245, 220);
    private static final Color AMARELO = new Color(255, 245, 200);
    private static final Color LARANJA = new Color(200, 120, 0);
    private static final Color ROXO = new Color(120, 40, 160);
    private static final Color ROXO_CLARO = new Color(240, 225, 250);

    private static final Font FONTE = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONTE_NEGRITO = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONTE_TITULO = new Font("SansSerif", Font.BOLD, 15);
    private static final Font FONTE_MONO = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font FONTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 10);

    private static final int REG_X = 60, REG_Y = 40, REG_L = 170, REG_A = 25;
    private static final int BARRA_A = 275, BARRA_B = 375, BARRA_C = 30;
    private static final int LATCH_Y = 470;

    private Processador p;

    public PainelDiagrama() {
        setBackground(FUNDO);
    }

    public void setProcessador(Processador p) {
        this.p = p;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (p == null) return;
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double escala = Math.min(getWidth() / (double) LARGURA, getHeight() / (double) ALTURA);
        g.translate((getWidth() - LARGURA * escala) / 2, (getHeight() - ALTURA * escala) / 2);
        g.scale(escala, escala);

        desenhaViaDeDados(g);
        desenhaUnidadeDeControle(g);
        desenhaMemoria(g);
    }

    private int fase() {
        if (p.mir == null || p.esperandoMemoria) return 0;
        return p.subciclo == 0 ? 4 : p.subciclo;
    }

    // VD

    private void desenhaViaDeDados(Graphics2D g) {
        Microinstrucao m = p.mir;
        int f = fase();
        boolean barrasAB = f >= 2;
        boolean escreveC = f >= 4 && m.enc == 1;

        g.setFont(FONTE_TITULO);
        g.setColor(PRETO);
        g.drawString("Via de Dados (VD)", 60, 22);

        // barramentos A e B
        linha(g, barrasAB ? AZUL : CINZA, barrasAB ? 3 : 1.5f, BARRA_A, REG_Y, BARRA_A, LATCH_Y);
        linha(g, barrasAB ? AZUL : CINZA, barrasAB ? 3 : 1.5f, BARRA_B, REG_Y, BARRA_B, LATCH_Y);
        g.setFont(FONTE_PEQUENA);
        g.setColor(CINZA);
        g.drawString("Barr. A", BARRA_A - 18, REG_Y - 4);
        g.drawString("Barr. B", BARRA_B - 18, REG_Y - 4);

        // barramento C
        Color corC = escreveC ? VERMELHO : CINZA;
        float espC = escreveC ? 3 : 1.5f;
        linha(g, corC, espC, 325, 665, 325, 690);
        linha(g, corC, espC, 325, 690, BARRA_C, 690);
        linha(g, corC, espC, BARRA_C, 690, BARRA_C, REG_Y + 12);
        g.setFont(FONTE_PEQUENA);
        g.setColor(corC);
        g.drawString("Barr. C", BARRA_C + 4, 685);

        // registradores
        for (int i = 0; i < 16; i++) {
            int y = REG_Y + i * REG_A;
            boolean noA = barrasAB && m.a == i && m.amux == 0 && usaA(m);
            boolean noB = barrasAB && m.b == i && usaB(m);
            boolean gravado = escreveC && m.c == i;

            linha(g, gravado ? VERMELHO : CINZA, gravado ? 2.5f : 1, BARRA_C, y + 12, REG_X, y + 12);
            linha(g, noA ? AZUL : CINZA, noA ? 2.5f : 1, REG_X + REG_L, y + 8, BARRA_A, y + 8);
            linha(g, noB ? AZUL : CINZA, noB ? 2.5f : 1, REG_X + REG_L, y + 16, BARRA_B, y + 16);

            Color fundo = Color.WHITE;
            if (noA || noB) fundo = AZUL_CLARO;
            if (gravado) fundo = VERMELHO_CLARO;
            caixa(g, REG_X, y, REG_L, REG_A - 3, fundo, gravado ? VERMELHO : PRETO);
            g.setFont(FONTE_NEGRITO);
            g.setColor(PRETO);
            g.drawString(Registradores.NOMES[i], REG_X + 6, y + 16);
            g.setFont(FONTE_MONO);
            int v = p.regs.get(i);
            direita(g, String.format("%04X  %6d", v, (short) v), REG_X + REG_L - 6, y + 16);
        }

        // latches
        boolean latchAUsado = barrasAB && usaA(m) && m.amux == 0;
        boolean latchBUsado = barrasAB && (usaB(m) || m.mar == 1);
        caixa(g, BARRA_A - 45, LATCH_Y, 90, 26, latchAUsado ? AZUL_CLARO : Color.WHITE, PRETO);
        caixa(g, BARRA_B - 45, LATCH_Y, 90, 26, latchBUsado ? AZUL_CLARO : Color.WHITE, PRETO);
        g.setFont(FONTE);
        g.setColor(PRETO);
        g.drawString("Latch A " + hexa(p.latchA), BARRA_A - 42, LATCH_Y + 17);
        g.drawString("Latch B " + hexa(p.latchB), BARRA_B - 42, LATCH_Y + 17);

        // MAR e MBR
        boolean marCarregado = f >= 3 && m.mar == 1;
        boolean mbrCarregado = (f >= 4 && m.mbr == 1) || (f >= 4 && p.leuMemoria);
        linha(g, marCarregado ? AZUL : CINZA, marCarregado ? 2.5f : 1, BARRA_B + 45, LATCH_Y + 13, 450, LATCH_Y + 13);
        caixa(g, 450, LATCH_Y, 85, 26, marCarregado ? AZUL_CLARO : Color.WHITE, PRETO);
        g.setColor(PRETO);
        g.setFont(FONTE_NEGRITO);
        g.drawString("MAR", 456, LATCH_Y + 17);
        g.setFont(FONTE_MONO);
        direita(g, String.valueOf(p.mar), 530, LATCH_Y + 17);

        int mbrY = 525;
        caixa(g, 450, mbrY, 85, 26, mbrCarregado ? ROXO_CLARO : Color.WHITE, mbrCarregado ? ROXO : PRETO);
        g.setColor(PRETO);
        g.setFont(FONTE_NEGRITO);
        g.drawString("MBR", 456, mbrY + 17);
        g.setFont(FONTE_MONO);
        direita(g, hexa(p.mbr), 530, mbrY + 17);

        // AMUX
        boolean amuxUsado = f >= 3 && usaA(m);
        Polygon amux = new Polygon(new int[]{BARRA_A - 40, BARRA_A + 40, BARRA_A + 30, BARRA_A - 30},
                new int[]{530, 530, 552, 552}, 4);
        g.setColor(amuxUsado ? AMARELO : Color.WHITE);
        g.fill(amux);
        g.setColor(PRETO);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(amux);
        g.setFont(FONTE);
        g.drawString("AMUX", BARRA_A - 16, 546);
        linha(g, latchAUsado ? AZUL : CINZA, latchAUsado ? 2.5f : 1, BARRA_A, LATCH_Y + 26, BARRA_A, 530);
        // MBR -> AMUX
        boolean mbrNoAmux = f >= 3 && m.amux == 1;
        linha(g, mbrNoAmux ? ROXO : CINZA, mbrNoAmux ? 2.5f : 1, 450, mbrY + 13, 330, mbrY + 13);
        linha(g, mbrNoAmux ? ROXO : CINZA, mbrNoAmux ? 2.5f : 1, 330, mbrY + 13, 305, 530);

        // ULA
        boolean ulaUsada = f >= 3;
        Polygon ula = new Polygon(new int[]{235, 415, 385, 265}, new int[]{580, 580, 625, 625}, 4);
        g.setColor(ulaUsada ? AMARELO : Color.WHITE);
        g.fill(ula);
        g.setColor(PRETO);
        g.draw(ula);
        linha(g, amuxUsado ? LARANJA : CINZA, amuxUsado ? 2.5f : 1, BARRA_A, 552, BARRA_A, 580);
        linha(g, latchBUsado && usaB(m) ? AZUL : CINZA, 2, BARRA_B, LATCH_Y + 26, BARRA_B, 580);
        g.setFont(FONTE_NEGRITO);
        g.drawString("ULA", 312, 597);
        g.setFont(FONTE);
        String opUla = m != null ? ULA.OPERACOES[m.ula] : "";
        g.drawString(opUla, 325 - g.getFontMetrics().stringWidth(opUla) / 2, 615);

        // N e Z
        caixa(g, 430, 585, 40, 18, ulaUsada && m.cond == 1 ? AMARELO : Color.WHITE, PRETO);
        caixa(g, 430, 606, 40, 18, ulaUsada && m.cond == 2 ? AMARELO : Color.WHITE, PRETO);
        g.setColor(PRETO);
        g.setFont(FONTE);
        g.drawString("N = " + (p.n ? 1 : 0), 434, 599);
        g.drawString("Z = " + (p.z ? 1 : 0), 434, 620);
        linha(g, CINZA, 1, 408, 602, 430, 602);

        // deslocador
        boolean deslUsado = f >= 3 && m.desl != 0;
        linha(g, ulaUsada ? PRETO : CINZA, 1.5f, 325, 625, 325, 638);
        caixa(g, 255, 638, 140, 27, deslUsado ? AMARELO : Color.WHITE, PRETO);
        g.setColor(PRETO);
        g.drawString("Deslocador " + (m != null && m.desl != 0 ? Deslocador.OPERACOES[m.desl] : ""), 265, 656);

        // deslocador -> MBR
        boolean mbrDaUla = f >= 4 && m.mbr == 1;
        linha(g, mbrDaUla ? VERMELHO : CINZA, mbrDaUla ? 2.5f : 1, 395, 651, 492, 651);
        linha(g, mbrDaUla ? VERMELHO : CINZA, mbrDaUla ? 2.5f : 1, 492, 651, 492, mbrY + 26);

        if (f >= 3) {
            g.setFont(FONTE_MONO);
            g.setColor(VERMELHO);
            g.drawString("saída = " + hexa(p.deslocador.getSaida()), 330, 680);
        }
    }

    private boolean usaA(Microinstrucao m) {
        if (m == null) return false;
        return m.enc == 1 || m.mbr == 1 || m.cond == 1 || m.cond == 2 || m.ula != 2 || m.a != 0;
    }

    private boolean usaB(Microinstrucao m) {
        if (m == null) return false;
        return (m.ula == 0 || m.ula == 1) && usaA(m);
    }

    // UC

    private void desenhaUnidadeDeControle(Graphics2D g) {
        Microinstrucao m = p.mir;
        int f = fase();
        int x = 570;

        g.setFont(FONTE_TITULO);
        g.setColor(PRETO);
        g.drawString("Unidade de Controle (UC)", x, 22);

        // memoria de controle
        int mcY = 35, mcL = 270, mcA = 165;
        caixa(g, x, mcY, mcL, mcA, f == 1 ? VERDE_CLARO : Color.WHITE, PRETO);
        g.setFont(FONTE_NEGRITO);
        g.setColor(PRETO);
        g.drawString("Memória de Controle (256 x 32)", x + 6, mcY + 15);
        int atual = m != null ? m.endereco : p.mpc;
        g.setFont(FONTE_MONO);
        int linhaY = mcY + 33;
        for (int e = Math.max(0, atual - 3); e <= Math.min(255, atual + 3); e++) {
            Microinstrucao mi = p.mc.get(e);
            if (mi == null) continue;
            if (e == atual && m != null) {
                g.setColor(VERDE_CLARO);
                g.fillRect(x + 3, linhaY - 12, mcL - 6, 16);
            }
            g.setColor(PRETO);
            String t = String.format("%2d: %s", e, mi.texto);
            if (t.length() > 34) t = t.substring(0, 34) + "...";
            g.drawString(t, x + 6, linhaY);
            linhaY += 18;
        }

        // MPC, +1 e MMUX
        int mpcX = 880;
        caixa(g, mpcX, 50, 90, 28, f == 1 || f == 4 ? VERDE_CLARO : Color.WHITE, PRETO);
        g.setFont(FONTE_NEGRITO);
        g.setColor(PRETO);
        g.drawString("MPC", mpcX + 6, 69);
        g.setFont(FONTE_MONO);
        direita(g, String.valueOf(p.mpc), mpcX + 84, 69);
        seta(g, PRETO, mpcX, 64, x + mcL, 64);
        g.setFont(FONTE_PEQUENA);
        g.drawString("8", x + mcL + 10, 60);

        caixa(g, mpcX + 20, 105, 50, 24, Color.WHITE, PRETO);
        g.setFont(FONTE_NEGRITO);
        g.drawString("+1", mpcX + 36, 122);
        seta(g, PRETO, mpcX + 45, 78, mpcX + 45, 105);

        Polygon mmux = new Polygon(new int[]{mpcX - 5, mpcX + 95, mpcX + 80, mpcX + 10},
                new int[]{160, 160, 185, 185}, 4);
        g.setColor(f == 4 ? VERDE_CLARO : Color.WHITE);
        g.fill(mmux);
        g.setColor(PRETO);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(mmux);
        g.setFont(FONTE);
        g.drawString("MMUX", mpcX + 28, 177);
        seta(g, PRETO, mpcX + 45, 129, mpcX + 45, 160);
        // MMUX -> MPC
        linha(g, PRETO, 1.2f, mpcX + 45, 185, mpcX + 45, 200);
        linha(g, PRETO, 1.2f, mpcX + 45, 200, mpcX + 110, 200);
        linha(g, PRETO, 1.2f, mpcX + 110, 200, mpcX + 110, 64);
        seta(g, PRETO, mpcX + 110, 64, mpcX + 90, 64);

        // MIR
        int mirY = 250;
        seta(g, PRETO, x + mcL / 2, mcY + mcA, x + mcL / 2, mirY);
        g.setFont(FONTE_NEGRITO);
        g.setColor(PRETO);
        g.drawString("MIR", x - 35, mirY + 25);
        String[] campos = {"AMUX", "COND", "ULA", "DESL", "MBR", "MAR", "RD", "WR", "ENC", "C", "B", "A", "ADDR"};
        int[] bits = {1, 2, 2, 2, 1, 1, 1, 1, 1, 4, 4, 4, 8};
        String[] valores = new String[13];
        if (m != null) {
            int[] v = {m.amux, m.cond, m.ula, m.desl, m.mbr, m.mar, m.rd, m.wr, m.enc, m.c, m.b, m.a, m.addr};
            for (int i = 0; i < 13; i++) valores[i] = Microinstrucao.bin(v[i], bits[i]);
        }
        double larguraBit = 400.0 / 32;
        double cx = x;
        int addrX = 0, condX = 0;
        for (int i = 0; i < 13; i++) {
            int w = (int) Math.round(bits[i] * larguraBit);
            boolean campoAtivo = m != null && f >= 1 && campoUsado(m, i);
            caixa(g, (int) cx, mirY, w, 40, campoAtivo ? AMARELO : Color.WHITE, PRETO);
            g.setFont(FONTE_PEQUENA);
            g.setColor(PRETO);
            int lw = g.getFontMetrics().stringWidth(campos[i]);
            if (bits[i] <= 2) {
                java.awt.geom.AffineTransform antes = g.getTransform();
                g.rotate(-Math.PI / 2, (int) cx + w / 2 + 4, mirY - 3);
                g.drawString(campos[i], (int) cx + w / 2 + 4, mirY - 3);
                g.setTransform(antes);
            } else {
                g.drawString(campos[i], (int) cx + (w - lw) / 2, mirY - 3);
            }
            if (valores[i] != null) {
                g.setFont(new Font("Monospaced", Font.PLAIN, 10));
                int vw = g.getFontMetrics().stringWidth(valores[i]);
                g.drawString(valores[i], (int) cx + (w - vw) / 2, mirY + 25);
            }
            if (i == 1) condX = (int) cx + w / 2;
            if (i == 12) addrX = (int) cx + w / 2;
            cx += w;
        }
        // ADDR -> MMUX
        linha(g, CINZA, 1.2f, addrX + 20, mirY, addrX + 20, 215);
        linha(g, CINZA, 1.2f, addrX + 20, 215, mpcX + 70, 215);
        seta(g, CINZA, mpcX + 70, 215, mpcX + 70, 185);

        // decoders
        int decY = 330;
        decoder(g, x, decY, "Decoder A", m != null && f >= 2 ? Registradores.NOMES[m.a] : "", f >= 2);
        decoder(g, x + 100, decY, "Decoder B", m != null && f >= 2 ? Registradores.NOMES[m.b] : "", f >= 2);
        decoder(g, x + 200, decY, "Decoder C",
                m != null && f >= 4 && m.enc == 1 ? Registradores.NOMES[m.c] : "", f >= 4 && m != null && m.enc == 1);
        g.setColor(CINZA);
        for (int i = 0; i < 3; i++) {
            linha(g, CINZA, 1, x + 40 + i * 100, mirY + 40, x + 40 + i * 100, decY);
        }
        g.setFont(FONTE_PEQUENA);
        g.drawString("(para os registradores da VD)", x, decY + 52);

        // controle de fluxo
        int lcX = 868, lcY = 320;
        boolean testou = m != null && f >= 4 && m.cond != 0;
        caixa(g, lcX, lcY, 125, 55, testou ? AMARELO : Color.WHITE, PRETO);
        g.setColor(PRETO);
        g.setFont(FONTE);
        g.drawString("Lógica para o", lcX + 8, lcY + 17);
        g.drawString("Controle de Fluxo", lcX + 8, lcY + 32);
        if (m != null && f >= 4) {
            g.setFont(FONTE_PEQUENA);
            g.drawString(p.desviou ? "desvia para ADDR" : "MPC + 1", lcX + 8, lcY + 48);
        }
        linha(g, CINZA, 1.2f, condX, mirY + 40, condX, 305);
        linha(g, CINZA, 1.2f, condX, 305, lcX + 30, 305);
        seta(g, CINZA, lcX + 30, 305, lcX + 30, lcY);
        // saida -> MMUX
        linha(g, CINZA, 1.2f, lcX + 125, lcY + 27, 998, lcY + 27);
        linha(g, CINZA, 1.2f, 998, lcY + 27, 998, 172);
        seta(g, CINZA, 998, 172, mpcX + 88, 172);
        // N e Z
        g.setFont(FONTE_PEQUENA);
        g.setColor(CINZA);
        linha(g, CINZA, 1, 470, 600, 500, 600);
        linha(g, CINZA, 1, 500, 600, 500, 400);
        linha(g, CINZA, 1, 500, 400, lcX + 70, 400);
        seta(g, CINZA, lcX + 70, 400, lcX + 70, lcY + 55);
        g.drawString("N, Z", 505, 395);

        // info
        int infoY = 425;
        caixa(g, x, infoY, 410, 95, Color.WHITE, CINZA);
        g.setColor(PRETO);
        g.setFont(FONTE_NEGRITO);
        if (p.esperandoMemoria) {
            g.setColor(LARANJA);
            g.drawString("Esperando a memória (falta na cache)", x + 8, infoY + 20);
            g.setFont(FONTE);
            g.drawString("Faltam " + p.espera + " ciclo(s)", x + 8, infoY + 40);
        } else if (m == null) {
            g.drawString("Processador parado", x + 8, infoY + 20);
        } else {
            g.drawString("Microinstrução " + m.endereco + " - subciclo " + (f == 0 ? "-" : String.valueOf(f)), x + 8, infoY + 20);
            g.setFont(FONTE_MONO);
            g.drawString(m.texto, x + 8, infoY + 40);
            g.drawString(m.binario(), x + 8, infoY + 58);
        }
        g.setFont(FONTE);
        g.setColor(PRETO);
        String msg = p.mensagem;
        if (msg.length() > 62) msg = msg.substring(0, 62) + "...";
        g.drawString(msg, x + 8, infoY + 82);
    }

    private boolean campoUsado(Microinstrucao m, int campo) {
        switch (campo) {
            case 0: return m.amux == 1;
            case 1: return m.cond != 0;
            case 3: return m.desl != 0;
            case 4: return m.mbr == 1;
            case 5: return m.mar == 1;
            case 6: return m.rd == 1;
            case 7: return m.wr == 1;
            case 8: return m.enc == 1;
            case 12: return m.cond != 0;
            default: return false;
        }
    }

    private void decoder(Graphics2D g, int x, int y, String nome, String reg, boolean ativo) {
        caixa(g, x, y, 85, 40, ativo ? AZUL_CLARO : Color.WHITE, PRETO);
        g.setColor(PRETO);
        g.setFont(FONTE);
        g.drawString(nome, x + 8, y + 16);
        g.setFont(FONTE_NEGRITO);
        g.drawString(reg, x + 8, y + 33);
    }

    // MP e caches

    private void desenhaMemoria(Graphics2D g) {
        int f = fase();
        boolean acessou = f >= 4 && (p.leuMemoria || p.escreveuMemoria);
        boolean instrucao = acessou && p.acessoInstrucao && p.leuMemoria;
        boolean dado = acessou && !instrucao;

        int x = 570, y = 540;
        cacheBox(g, p.cacheInstrucoes, x, y, instrucao);
        cacheBox(g, p.cacheDados, x, y + 85, dado);

        // MAR/MBR -> caches
        Color c = acessou ? ROXO : CINZA;
        linha(g, c, acessou ? 2.5f : 1, 535, LATCH_Y + 13, 550, LATCH_Y + 13);
        linha(g, c, acessou ? 2.5f : 1, 550, LATCH_Y + 13, 550, 680);
        linha(g, c, acessou ? 2.5f : 1, 535, 538, 550, 538);
        seta(g, instrucao ? ROXO : CINZA, 550, 575, x, 575);
        seta(g, dado ? ROXO : CINZA, 550, 660, x, 660);
        g.setFont(FONTE_PEQUENA);
        g.setColor(CINZA);
        g.drawString("MAR/MBR", 505, 700);

        // MP
        int mpX = 830, mpY = 560;
        boolean mpOcupada = p.esperandoMemoria || p.espera > 0;
        caixa(g, mpX, mpY, 150, 120, mpOcupada ? AMARELO : Color.WHITE, PRETO);
        g.setColor(PRETO);
        g.setFont(FONTE_NEGRITO);
        g.drawString("MP", mpX + 8, mpY + 18);
        g.setFont(FONTE);
        g.drawString("4096 x 16 bits", mpX + 8, mpY + 36);
        g.drawString("leituras: " + p.mp.getLeituras(), mpX + 8, mpY + 56);
        g.drawString("escritas: " + p.mp.getEscritas(), mpX + 8, mpY + 72);
        g.drawString("esperando: " + p.ciclosEsperando + " ciclos", mpX + 8, mpY + 92);
        if (mpOcupada) {
            g.setColor(LARANJA);
            g.drawString("ocupada...", mpX + 8, mpY + 110);
        }
        linha(g, CINZA, 1.2f, x + 220, 590, mpX, 590);
        linha(g, CINZA, 1.2f, x + 220, 660, mpX, 660);
    }

    private void cacheBox(Graphics2D g, Cache c, int x, int y, boolean ativa) {
        Color fundo = Color.WHITE, borda = PRETO;
        if (ativa) {
            boolean acerto = c.isAtiva() && c.isUltimoAcerto();
            fundo = acerto ? VERDE_CLARO : VERMELHO_CLARO;
            borda = acerto ? VERDE : VERMELHO;
        }
        caixa(g, x, y, 220, 75, fundo, borda);
        g.setColor(PRETO);
        g.setFont(FONTE_NEGRITO);
        g.drawString(c.getNome(), x + 8, y + 17);
        g.setFont(FONTE);
        if (!c.isAtiva()) {
            g.drawString("desligada (acessa direto a MP)", x + 8, y + 35);
        } else {
            g.drawString(c.tipo(), x + 8, y + 35);
            g.drawString(String.format("acertos %d  faltas %d  (%.1f%%)", c.getAcertos(), c.getFaltas(), c.taxaAcerto()),
                    x + 8, y + 52);
        }
        if (ativa && c.getUltimoEndereco() >= 0) {
            g.setFont(FONTE_NEGRITO);
            g.setColor(borda);
            String r = c.isAtiva() ? (c.isUltimoAcerto() ? "ACERTO" : "FALTA") : "MP";
            g.drawString((c.isUltimoFoiEscrita() ? "escrita" : "leitura") + " end. " + c.getUltimoEndereco() + ": " + r,
                    x + 8, y + 69);
        }
    }

    private static String hexa(int v) {
        return String.format("%04X", v & 0xFFFF);
    }

    private static void caixa(Graphics2D g, int x, int y, int w, int h, Color fundo, Color borda) {
        g.setColor(fundo);
        g.fillRect(x, y, w, h);
        g.setColor(borda);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRect(x, y, w, h);
    }

    private static void linha(Graphics2D g, Color c, float espessura, int x1, int y1, int x2, int y2) {
        g.setColor(c);
        g.setStroke(new BasicStroke(espessura));
        g.drawLine(x1, y1, x2, y2);
    }

    private static void seta(Graphics2D g, Color c, int x1, int y1, int x2, int y2) {
        linha(g, c, 1.2f, x1, y1, x2, y2);
        double ang = Math.atan2(y2 - y1, x2 - x1);
        int t = 8;
        int xa = (int) (x2 - t * Math.cos(ang - 0.4)), ya = (int) (y2 - t * Math.sin(ang - 0.4));
        int xb = (int) (x2 - t * Math.cos(ang + 0.4)), yb = (int) (y2 - t * Math.sin(ang + 0.4));
        g.fillPolygon(new int[]{x2, xa, xb}, new int[]{y2, ya, yb}, 3);
    }

    private static void direita(Graphics2D g, String s, int x, int y) {
        g.drawString(s, x - g.getFontMetrics().stringWidth(s), y);
    }
}
