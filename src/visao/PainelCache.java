package visao;

import modelo.cache.Cache;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

public class PainelCache extends JPanel {

    private Cache cache;
    private JLabel lblInfo = new JLabel();
    private DefaultTableModel modelo;
    private JTable tabela;

    public PainelCache() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        add(lblInfo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"Conjunto", "Via", "V", "S", "Tag", "Dados"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        tabela.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tabela.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(30);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(20);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(20);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(220);
        tabela.setDefaultRenderer(Object.class, new Cores());
        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    public void setCache(Cache c) {
        cache = c;
        atualiza();
    }

    public void atualiza() {
        if (cache == null) return;
        String t = "<html><b>" + cache.getNome() + "</b><br>";
        if (!cache.isAtiva()) {
            t += "Desligada: todo acesso vai direto para a MP (" + cache.getPenalidade() + " ciclos)";
        } else {
            t += cache.tipo() + " - " + cache.getConjuntos() + " conjunto(s) x " + cache.getVias() + " via(s) x "
                    + cache.getPalavrasPorBloco() + " palavras" + (cache.getNome().contains("Dados")
                    ? " - " + (cache.isWriteBack() ? "write-back" : "write-through") : "") + "<br>";
            t += "Leituras: " + cache.getLeituras() + " &nbsp; Escritas: " + cache.getEscritas()
                    + " &nbsp; Acertos: " + cache.getAcertos() + " &nbsp; Faltas: " + cache.getFaltas()
                    + " &nbsp; Write-backs: " + cache.getWriteBacks()
                    + String.format(" &nbsp; <b>Taxa de acerto: %.1f%%</b>", cache.taxaAcerto());
            if (cache.getUltimoEndereco() >= 0) {
                int e = cache.getUltimoEndereco();
                t += "<br>Último acesso: endereço " + e + " (tag " + cache.tagDo(e) + ", conjunto " + cache.conjuntoDo(e)
                        + ", palavra " + cache.palavraDo(e) + ") - " + (cache.isUltimoAcerto() ? "acerto" : "falta");
            }
        }
        lblInfo.setText(t + "</html>");

        modelo.setRowCount(0);
        Cache.Linha[][] linhas = cache.getLinhas();
        for (int c = 0; c < linhas.length; c++) {
            for (int v = 0; v < linhas[c].length; v++) {
                Cache.Linha l = linhas[c][v];
                String dados = "";
                if (l.valida) {
                    for (int d : l.dados) dados += String.format("%04X ", d);
                }
                modelo.addRow(new Object[]{v == 0 ? String.valueOf(c) : "", v, l.valida ? 1 : 0, l.suja ? 1 : 0,
                        l.valida ? String.valueOf(l.tag) : "-", dados});
            }
        }
    }

    private class Cores extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int linha, int col) {
            Component comp = super.getTableCellRendererComponent(t, valor, sel, foco, linha, col);
            if (!sel) {
                int vias = cache.getVias();
                int conj = linha / vias, via = linha % vias;
                Color cor = Color.WHITE;
                if (cache.isAtiva() && conj == cache.getUltimoConjunto()) {
                    cor = new Color(255, 250, 225);
                    if (via == cache.getUltimaVia()) {
                        cor = cache.isUltimoAcerto() ? new Color(205, 240, 210) : new Color(255, 210, 210);
                    }
                }
                comp.setBackground(cor);
            }
            return comp;
        }
    }
}
