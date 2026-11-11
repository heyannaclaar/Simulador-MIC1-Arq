package visao;

import controle.MemoriaDeControle;
import controle.Microinstrucao;
import controle.Processador;
import modelo.MemoriaPrincipal;
import modelo.Registradores;
import util.Arquivos;
import util.ErroDeMontagemException;
import util.MicroMontador;
import util.Montador;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Files;

public class JanelaPrincipal extends JFrame {

    private Processador processador = new Processador();
    private Montador montador = new Montador();
    private boolean montado = false;

    // programa
    private JComboBox<String> cmbExemplos = new JComboBox<>(Arquivos.EXEMPLOS);
    private JTextArea txtAssembly = new JTextArea();
    private JTextArea txtCompilado = new JTextArea();
    private JTextArea txtMicroprograma = new JTextArea();

    // processador
    private PainelDiagrama diagrama = new PainelDiagrama();
    private JTextArea txtHistorico = new JTextArea();
    private TabelaRegistradores modeloRegs = new TabelaRegistradores();
    private TabelaMemoria modeloMemoria = new TabelaMemoria();
    private TabelaMemoriaControle modeloMC = new TabelaMemoriaControle();
    private JTable tabelaMemoria = new JTable(modeloMemoria);
    private JTable tabelaMC = new JTable(modeloMC);
    private PainelCache painelCacheI = new PainelCache();
    private PainelCache painelCacheD = new PainelCache();

    // controles
    private JSpinner spnPausa = new JSpinner(new SpinnerNumberModel(100, 0, 2000, 10));
    private JTextField txtPausarPc = new JTextField(5);
    private JLabel lblCiclos = new JLabel("Ciclos: 0");
    private JLabel lblInstrucoes = new JLabel("Instruções: 0");
    private JLabel lblCpi = new JLabel("CPI: 0");
    private JLabel lblEspera = new JLabel("Ciclos esperando memória: 0");
    private JLabel lblStatus = new JLabel(" ");
    private Timer timer;

    public JanelaPrincipal() {
        super("Simulador MIC-1");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        timer = new Timer(100, e -> executaPasso());
        montaTela();

        txtMicroprograma.setText(Arquivos.microprograma());
        montaMicroprograma();
        carregaExemplo();

        setSize(1500, 900);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);
    }

    private void montaTela() {
        Font mono = new Font("Monospaced", Font.PLAIN, 13);

        // Programa
        JPanel painelPrograma = new JPanel(new BorderLayout(5, 5));
        painelPrograma.setBorder(BorderFactory.createTitledBorder("Programa"));

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Exemplos:"));
        topo.add(cmbExemplos);
        cmbExemplos.addActionListener(e -> carregaExemplo());
        JButton btnAbrir = new JButton("Abrir");
        JButton btnSalvar = new JButton("Salvar");
        btnAbrir.addActionListener(e -> abrir());
        btnSalvar.addActionListener(e -> salvar());
        topo.add(btnAbrir);
        topo.add(btnSalvar);
        painelPrograma.add(topo, BorderLayout.NORTH);

        txtAssembly.setFont(mono);
        txtAssembly.setTabSize(4);
        txtCompilado.setFont(mono);
        txtCompilado.setEditable(false);
        JPanel pAssembly = new JPanel(new BorderLayout());
        pAssembly.add(new JLabel("Assembly:"), BorderLayout.NORTH);
        pAssembly.add(new JScrollPane(txtAssembly), BorderLayout.CENTER);
        JPanel pCompilado = new JPanel(new BorderLayout());
        pCompilado.add(new JLabel("Compilado:"), BorderLayout.NORTH);
        pCompilado.add(new JScrollPane(txtCompilado), BorderLayout.CENTER);
        pCompilado.setPreferredSize(new Dimension(150, 100));
        JSplitPane codigos = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pAssembly, pCompilado);
        codigos.setResizeWeight(0.7);
        painelPrograma.add(codigos, BorderLayout.CENTER);

        JPanel botoesPrograma = new JPanel(new GridLayout(2, 1, 5, 5));
        JButton btnMontar = new JButton("Montar");
        JButton btnGravar = new JButton("Gravar código na MP");
        btnMontar.addActionListener(e -> montar());
        btnGravar.addActionListener(e -> gravarNaMP());
        botoesPrograma.add(btnMontar);
        botoesPrograma.add(btnGravar);
        painelPrograma.add(botoesPrograma, BorderLayout.SOUTH);
        painelPrograma.setPreferredSize(new Dimension(340, 600));

        // Diagrama e historico
        txtHistorico.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtHistorico.setEditable(false);
        JPanel pHistorico = new JPanel(new BorderLayout());
        pHistorico.setBorder(BorderFactory.createTitledBorder("Histórico de Microinstruções"));
        pHistorico.add(new JScrollPane(txtHistorico), BorderLayout.CENTER);
        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT, diagrama, pHistorico);
        centro.setResizeWeight(0.82);
        diagrama.setProcessador(processador);

        // Abas
        JTabbedPane abas = new JTabbedPane();
        JTable tabelaRegs = new JTable(modeloRegs);
        tabelaRegs.setFont(mono);
        tabelaRegs.setRowHeight(20);
        tabelaRegs.getColumnModel().getColumn(0).setPreferredWidth(70);
        tabelaRegs.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabelaRegs.getColumnModel().getColumn(2).setPreferredWidth(60);
        tabelaRegs.getColumnModel().getColumn(3).setPreferredWidth(50);
        abas.addTab("Registradores", new JScrollPane(tabelaRegs));

        tabelaMemoria.setFont(mono);
        tabelaMemoria.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaMemoria.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabelaMemoria.setDefaultRenderer(Object.class, new CoresMemoria());
        JPanel pMemoria = new JPanel(new BorderLayout());
        JLabel obs = new JLabel("<html>Azul: pilha (de SP até 4095). Cinza: endereço do PC. Roxo: endereço no MAR.</html>");
        obs.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pMemoria.add(obs, BorderLayout.NORTH);
        pMemoria.add(new JScrollPane(tabelaMemoria), BorderLayout.CENTER);
        abas.addTab("Memória Principal", pMemoria);

        JSplitPane caches = new JSplitPane(JSplitPane.VERTICAL_SPLIT, painelCacheI, painelCacheD);
        caches.setResizeWeight(0.5);
        abas.addTab("Caches", caches);

        tabelaMC.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tabelaMC.setDefaultRenderer(Object.class, new CoresMC());
        tabelaMC.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabelaMC.getColumnModel().getColumn(1).setPreferredWidth(260);
        tabelaMC.getColumnModel().getColumn(2).setPreferredWidth(260);
        abas.addTab("Memória de Controle", new JScrollPane(tabelaMC));

        txtMicroprograma.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JPanel pMicro = new JPanel(new BorderLayout());
        JButton btnMontarMicro = new JButton("Montar microprograma");
        btnMontarMicro.addActionListener(e -> {
            if (montaMicroprograma()) gravarNaMP();
        });
        pMicro.add(new JScrollPane(txtMicroprograma), BorderLayout.CENTER);
        pMicro.add(btnMontarMicro, BorderLayout.SOUTH);
        abas.addTab("Microprograma", pMicro);
        abas.setPreferredSize(new Dimension(420, 600));

        painelCacheI.setCache(processador.cacheInstrucoes);
        painelCacheD.setCache(processador.cacheDados);

        // Controles da simulacao
        JPanel controles = new JPanel(new BorderLayout());
        controles.setBorder(BorderFactory.createTitledBorder("Controles da Simulação"));
        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnExecutar = new JButton("Executar");
        JButton btnPausar = new JButton("Pausar");
        JButton btnResetar = new JButton("Resetar");
        JButton btnSubciclo = new JButton("Passar um subciclo");
        JButton btnCiclo = new JButton("Passar um ciclo");
        JButton btnInstrucao = new JButton("Passar uma instrução");
        JButton btnCaches = new JButton("Configurar caches");
        btnExecutar.addActionListener(e -> executar());
        btnPausar.addActionListener(e -> pausar());
        btnResetar.addActionListener(e -> resetar());
        btnSubciclo.addActionListener(e -> {
            pausar();
            processador.passaSubciclo();
            if (processador.subciclo == 0) registraHistorico();
            atualizaTela();
        });
        btnCiclo.addActionListener(e -> {
            pausar();
            processador.passaCiclo();
            registraHistorico();
            atualizaTela();
        });
        btnInstrucao.addActionListener(e -> passarInstrucao());
        btnCaches.addActionListener(e -> configurarCaches());
        linha1.add(btnExecutar);
        linha1.add(btnPausar);
        linha1.add(btnResetar);
        linha1.add(new JLabel("   "));
        linha1.add(btnSubciclo);
        linha1.add(btnCiclo);
        linha1.add(btnInstrucao);
        linha1.add(new JLabel("   Pausa entre subciclos (ms):"));
        spnPausa.setPreferredSize(new Dimension(70, 25));
        linha1.add(spnPausa);
        linha1.add(new JLabel("   Pausar quando PC ="));
        linha1.add(txtPausarPc);
        linha1.add(new JLabel("   "));
        linha1.add(btnCaches);

        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 2));
        linha2.add(lblCiclos);
        linha2.add(lblInstrucoes);
        linha2.add(lblCpi);
        linha2.add(lblEspera);
        lblStatus.setForeground(new Color(0, 90, 160));
        linha2.add(lblStatus);
        controles.add(linha1, BorderLayout.NORTH);
        controles.add(linha2, BorderLayout.SOUTH);

        JSplitPane meioDireita = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centro, abas);
        meioDireita.setResizeWeight(1.0);
        JSplitPane tudo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelPrograma, meioDireita);
        add(tudo, BorderLayout.CENTER);
        add(controles, BorderLayout.SOUTH);
    }

    // programa

    private void carregaExemplo() {
        pausar();
        String nome = (String) cmbExemplos.getSelectedItem();
        txtAssembly.setText(Arquivos.exemplo(nome));
        txtAssembly.setCaretPosition(0);
        if (montar()) gravarNaMP();
    }

    private void abrir() {
        JFileChooser fc = new JFileChooser(new File("."));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String s = new String(Files.readAllBytes(fc.getSelectedFile().toPath()), "UTF-8");
                txtAssembly.setText(s.replace("\r", ""));
                txtCompilado.setText("");
                montado = false;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível abrir o arquivo.");
            }
        }
    }

    private void salvar() {
        JFileChooser fc = new JFileChooser(new File("."));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();
                if (!f.getName().contains(".")) f = new File(f.getPath() + ".asm");
                Files.write(f.toPath(), txtAssembly.getText().getBytes("UTF-8"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível salvar o arquivo.");
            }
        }
    }

    private boolean montar() {
        try {
            montador.monta(txtAssembly.getText());
            montado = true;
        } catch (ErroDeMontagemException e) {
            montado = false;
            txtCompilado.setText("");
            JOptionPane.showMessageDialog(this, "Ocorreu um erro ao montar o programa:\n\n" + e.getMessage(),
                    "Erro de montagem", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        txtCompilado.setText(montador.compilado());
        txtCompilado.setCaretPosition(0);
        lblStatus.setText("Programa montado: " + montador.getPalavras().length + " palavras. Clique em Gravar código na MP.");
        return true;
    }

    private void gravarNaMP() {
        pausar();
        if (!montado) {
            JOptionPane.showMessageDialog(this, "Monte o programa antes de gravar na MP.");
            return;
        }
        processador.carregaPrograma(montador.getPalavras());
        txtHistorico.setText("");
        atualizaTela();
        lblStatus.setText("Código gravado na MP a partir do endereço 0.");
    }

    private boolean montaMicroprograma() {
        MemoriaDeControle mc;
        try {
            mc = new MicroMontador().monta(txtMicroprograma.getText());
        } catch (ErroDeMontagemException e) {
            JOptionPane.showMessageDialog(this, "Erro no microprograma:\n\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        processador.mc = mc;
        modeloMC.fireTableDataChanged();
        return true;
    }

    // simulacao

    private void executar() {
        if (processador.parado) {
            lblStatus.setText(processador.mensagem + ". Clique em Resetar para executar de novo.");
            return;
        }
        int pausa = (Integer) spnPausa.getValue();
        timer.setDelay(pausa == 0 ? 15 : pausa);
        timer.start();
    }

    private void pausar() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            atualizaTela();
        }
    }

    private void resetar() {
        pausar();
        processador.reset();
        txtHistorico.setText("");
        atualizaTela();
    }

    private void passarInstrucao() {
        pausar();
        int limite = 0;
        do {
            processador.passaCiclo();
            registraHistorico();
            limite++;
        } while (!processador.parado && !inicioDeInstrucao() && limite < 10000);
        atualizaTela();
    }

    private boolean inicioDeInstrucao() {
        return processador.mpc == 0 && processador.subciclo == 0 && processador.espera == 0;
    }

    private void executaPasso() {
        int pausa = (Integer) spnPausa.getValue();
        int vezes = pausa == 0 ? 4000 : 1;
        for (int i = 0; i < vezes; i++) {
            if (processador.parado) break;
            processador.passaSubciclo();
            if (processador.subciclo == 0) {
                if (pausa > 0) registraHistorico();
                if (inicioDeInstrucao() && chegouNoPcDePausa()) {
                    timer.stop();
                    lblStatus.setText("Pausado: PC chegou em " + processador.regs.get(Registradores.PC));
                    break;
                }
            }
        }
        if (processador.parado) timer.stop();
        atualizaTela();
    }

    private boolean chegouNoPcDePausa() {
        String t = txtPausarPc.getText().trim();
        if (t.isEmpty()) return false;
        try {
            return processador.regs.get(Registradores.PC) == Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void configurarCaches() {
        pausar();
        JanelaCache j = new JanelaCache(this, processador);
        j.setVisible(true);
        if (j.confirmou()) {
            painelCacheI.setCache(processador.cacheInstrucoes);
            painelCacheD.setCache(processador.cacheDados);
            resetar();
        }
    }

    private void registraHistorico() {
        if (processador.esperandoMemoria) {
            txtHistorico.append(String.format("ciclo %5d   esperando memória%n", processador.ciclos));
        } else if (processador.mir != null) {
            Microinstrucao m = processador.mir;
            txtHistorico.append(String.format("ciclo %5d   %3d: %s%n", processador.ciclos, m.endereco, m.texto));
        }
        if (txtHistorico.getLineCount() > 2000) {
            try {
                txtHistorico.replaceRange("", 0, txtHistorico.getLineEndOffset(500));
            } catch (Exception e) {
            }
        }
    }

    private void atualizaTela() {
        diagrama.repaint();
        modeloRegs.fireTableDataChanged();
        modeloMemoria.fireTableDataChanged();
        modeloMC.fireTableDataChanged();
        painelCacheI.atualiza();
        painelCacheD.atualiza();
        lblCiclos.setText("Ciclos: " + processador.ciclos);
        lblInstrucoes.setText("Instruções: " + processador.instrucoes);
        lblCpi.setText(String.format("CPI: %.2f", processador.cpi()));
        lblEspera.setText("Ciclos esperando memória: " + processador.ciclosEsperando);
        if (processador.parado || !timer.isRunning()) {
            lblStatus.setText(processador.mensagem);
        }
        txtHistorico.setCaretPosition(txtHistorico.getDocument().getLength());
        int linhaMC = linhaAtualMC();
        if (linhaMC >= 0) tabelaMC.scrollRectToVisible(tabelaMC.getCellRect(linhaMC, 0, true));
    }

    private int linhaAtualMC() {
        int atual = processador.mir != null ? processador.mir.endereco : processador.mpc;
        int linha = 0;
        for (int e = 0; e < MemoriaDeControle.TAMANHO; e++) {
            if (processador.mc.get(e) == null) continue;
            if (e == atual) return linha;
            linha++;
        }
        return -1;
    }

    // tabelas

    private class TabelaRegistradores extends AbstractTableModel {
        String[] colunas = {"Registrador", "Valor Binário", "Decimal", "Hexadecimal"};

        public int getRowCount() { return 19; }
        public int getColumnCount() { return colunas.length; }
        public String getColumnName(int c) { return colunas[c]; }

        public Object getValueAt(int linha, int col) {
            String nome;
            int valor;
            if (linha < 16) {
                nome = Registradores.NOMES[linha];
                valor = processador.regs.get(linha);
            } else if (linha == 16) {
                nome = "MAR";
                valor = processador.mar;
            } else if (linha == 17) {
                nome = "MBR";
                valor = processador.mbr;
            } else {
                nome = "MPC";
                valor = processador.mpc;
            }
            switch (col) {
                case 0: return nome;
                case 1: return Montador.binario(valor);
                case 2: return String.valueOf((short) valor);
                default: return String.format("%04X", valor & 0xFFFF);
            }
        }
    }

    private class TabelaMemoria extends AbstractTableModel {
        String[] colunas = {"Endereço", "Nome", "Valor Binário (C2)", "Valor Decimal", "Valor Hexadecimal"};

        public int getRowCount() { return MemoriaPrincipal.TAMANHO; }
        public int getColumnCount() { return colunas.length; }
        public String getColumnName(int c) { return colunas[c]; }

        public Object getValueAt(int linha, int col) {
            int v = processador.valorMemoria(linha);
            switch (col) {
                case 0: return String.valueOf(linha);
                case 1: return montado ? montador.nomeDoEndereco(linha) : "";
                case 2: return Montador.binario(v);
                case 3: return String.valueOf((short) v);
                default: return String.format("%04X", v);
            }
        }
    }

    private class CoresMemoria extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int linha, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foco, linha, col);
            if (!sel) {
                Color cor = Color.WHITE;
                int sp = processador.regs.get(Registradores.SP);
                if (linha >= sp && linha < Registradores.SP_INICIAL) cor = new Color(210, 225, 255);
                if (linha == processador.regs.get(Registradores.PC)) cor = new Color(215, 215, 215);
                if (linha == processador.mar && processador.mir != null && (processador.mir.rd == 1 || processador.mir.wr == 1)) {
                    cor = new Color(235, 215, 245);
                }
                c.setBackground(cor);
            }
            return c;
        }
    }

    private class TabelaMemoriaControle extends AbstractTableModel {
        String[] colunas = {"End.", "Microinstrução", "Binário (32 bits)"};

        public int getRowCount() {
            return processador.mc.quantidade();
        }
        public int getColumnCount() { return colunas.length; }
        public String getColumnName(int c) { return colunas[c]; }

        public Object getValueAt(int linha, int col) {
            Microinstrucao m = microDaLinha(linha);
            if (m == null) return "";
            switch (col) {
                case 0: return String.valueOf(m.endereco);
                case 1: return m.texto;
                default: return m.binario();
            }
        }
    }

    private Microinstrucao microDaLinha(int linha) {
        int n = 0;
        for (int e = 0; e < MemoriaDeControle.TAMANHO; e++) {
            Microinstrucao m = processador.mc.get(e);
            if (m == null) continue;
            if (n == linha) return m;
            n++;
        }
        return null;
    }

    private class CoresMC extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int linha, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foco, linha, col);
            if (!sel) {
                c.setBackground(linha == linhaAtualMC() ? new Color(215, 245, 220) : Color.WHITE);
            }
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
            }
            new JanelaPrincipal().setVisible(true);
        });
    }
}
