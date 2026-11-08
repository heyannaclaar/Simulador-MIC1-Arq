package visao;

import controle.Processador;
import modelo.cache.Cache;
import modelo.cache.CacheAssociativa;
import modelo.cache.CacheAssociativaPorConjunto;
import modelo.cache.CacheMapeamentoDireto;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class JanelaCache extends JDialog {

    private boolean confirmou = false;
    private Campos ci, cd;

    private class Campos extends JPanel {
        JCheckBox ativa = new JCheckBox("Ligada");
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Mapeamento direto", "Associativa por conjunto", "Totalmente associativa"});
        JComboBox<Integer> linhas = new JComboBox<>(new Integer[]{4, 8, 16, 32, 64});
        JComboBox<Integer> vias = new JComboBox<>(new Integer[]{2, 4, 8});
        JComboBox<Integer> palavras = new JComboBox<>(new Integer[]{1, 2, 4, 8, 16});
        JComboBox<String> escrita = new JComboBox<>(new String[]{"Write-back", "Write-through"});
        JSpinner penalidade = new JSpinner(new SpinnerNumberModel(8, 0, 100, 1));

        Campos(Cache c, boolean mostraEscrita) {
            setLayout(new GridLayout(0, 2, 5, 5));
            setBorder(BorderFactory.createTitledBorder(c.getNome()));
            add(ativa);
            add(new JLabel(""));
            add(new JLabel("Tipo:"));
            add(tipo);
            add(new JLabel("Total de linhas:"));
            add(linhas);
            add(new JLabel("Vias (associativa por conjunto):"));
            add(vias);
            add(new JLabel("Palavras por bloco:"));
            add(palavras);
            if (mostraEscrita) {
                add(new JLabel("Escrita:"));
                add(escrita);
            }
            add(new JLabel("Penalidade de falta (ciclos):"));
            add(penalidade);

            ativa.setSelected(c.isAtiva());
            linhas.setSelectedItem(c.getConjuntos() * c.getVias());
            palavras.setSelectedItem(c.getPalavrasPorBloco());
            penalidade.setValue(c.getPenalidade());
            escrita.setSelectedIndex(c.isWriteBack() ? 0 : 1);
            if (c instanceof CacheMapeamentoDireto) {
                tipo.setSelectedIndex(0);
            } else if (c instanceof CacheAssociativa) {
                tipo.setSelectedIndex(2);
            } else {
                tipo.setSelectedIndex(1);
                vias.setSelectedItem(c.getVias());
            }
            tipo.addActionListener(e -> vias.setEnabled(tipo.getSelectedIndex() == 1));
            vias.setEnabled(tipo.getSelectedIndex() == 1);
        }

        Cache cria(Cache antiga) {
            int total = (Integer) linhas.getSelectedItem();
            int pal = (Integer) palavras.getSelectedItem();
            boolean wb = escrita.getSelectedIndex() == 0;
            int pen = (Integer) penalidade.getValue();
            Cache nova;
            if (tipo.getSelectedIndex() == 0) {
                nova = new CacheMapeamentoDireto(antiga.getNome(), antiga.getMp(), total, pal, wb, pen);
            } else if (tipo.getSelectedIndex() == 2) {
                nova = new CacheAssociativa(antiga.getNome(), antiga.getMp(), total, pal, wb, pen);
            } else {
                int v = Math.min((Integer) vias.getSelectedItem(), total);
                nova = new CacheAssociativaPorConjunto(antiga.getNome(), antiga.getMp(), total, v, pal, wb, pen);
            }
            nova.setAtiva(ativa.isSelected());
            return nova;
        }
    }

    public JanelaCache(JFrame pai, Processador p) {
        super(pai, "Configurar caches", true);
        ci = new Campos(p.cacheInstrucoes, false);
        cd = new Campos(p.cacheDados, true);

        JPanel centro = new JPanel(new GridLayout(1, 2, 10, 10));
        centro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centro.add(ci);
        centro.add(cd);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Aplicar");
        JButton cancelar = new JButton("Cancelar");
        ok.addActionListener(e -> {
            p.cacheInstrucoes = ci.cria(p.cacheInstrucoes);
            p.cacheDados = cd.cria(p.cacheDados);
            confirmou = true;
            dispose();
        });
        cancelar.addActionListener(e -> dispose());
        botoes.add(new JLabel("Obs: aplicar reinicia a simulação.  "));
        botoes.add(ok);
        botoes.add(cancelar);

        add(centro, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(pai);
    }

    public boolean confirmou() {
        return confirmou;
    }
}
