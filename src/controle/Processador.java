package controle;

import modelo.cache.Cache;
import modelo.cache.CacheMapeamentoDireto;
import modelo.Deslocador;
import modelo.MemoriaPrincipal;
import modelo.Registradores;
import modelo.ULA;

public class Processador {

    public Registradores regs = new Registradores();
    public ULA ula = new ULA();
    public Deslocador deslocador = new Deslocador();
    public MemoriaPrincipal mp = new MemoriaPrincipal();
    public MemoriaDeControle mc = new MemoriaDeControle();
    public AMux amux = new AMux();
    public MMux mmux = new MMux();
    public ControladorDeFluxo controlador = new ControladorDeFluxo();
    public Cache cacheInstrucoes = new CacheMapeamentoDireto("Cache de Instruções", mp, 16, 4, true, 8);
    public Cache cacheDados = new CacheMapeamentoDireto("Cache de Dados", mp, 16, 4, true, 8);

    public int mar, mbr;
    public int mpc;
    public Microinstrucao mir;
    public int latchA, latchB;
    public int saidaAmux;
    public boolean n, z;

    public int subciclo = 0;
    public int ciclos;
    public int instrucoes;
    public int ciclosEsperando;
    public int espera;
    public boolean esperandoMemoria;

    private int rdSeguidos, wrSeguidos;
    private boolean marVeioDoPc;

    public boolean leuMemoria, escreveuMemoria, acessoInstrucao, acertouCache, desviou;

    public boolean parado;
    public String mensagem = "";

    private int[] programa = new int[0];

    public void carregaPrograma(int[] palavras) {
        programa = palavras;
        reset();
    }

    public void reset() {
        regs.reset();
        ula.reset();
        deslocador.reset();
        amux.reset();
        controlador.reset();
        mp.limpar();
        for (int i = 0; i < programa.length; i++) {
            mp.gravar(i, programa[i]);
        }
        mp.zerarContadores();
        cacheInstrucoes.reset();
        cacheDados.reset();
        mar = mbr = 0;
        mpc = 0;
        mir = null;
        latchA = latchB = saidaAmux = 0;
        n = z = false;
        subciclo = 0;
        ciclos = instrucoes = ciclosEsperando = 0;
        espera = 0;
        esperandoMemoria = false;
        rdSeguidos = wrSeguidos = 0;
        marVeioDoPc = false;
        limpaAtividade();
        parado = false;
        mensagem = "Pronto para executar";
    }

    private void limpaAtividade() {
        leuMemoria = escreveuMemoria = acessoInstrucao = acertouCache = desviou = false;
    }

    public boolean passaSubciclo() {
        if (parado) return false;

        // espera da memoria
        if (subciclo == 0 && espera > 0) {
            espera--;
            ciclos++;
            ciclosEsperando++;
            esperandoMemoria = true;
            mensagem = "Esperando a memória principal (faltam " + espera + " ciclos)";
            return true;
        }
        esperandoMemoria = false;

        subciclo++;
        switch (subciclo) {
            case 1:
                subciclo1();
                break;
            case 2:
                subciclo2();
                break;
            case 3:
                subciclo3();
                break;
            case 4:
                subciclo4();
                subciclo = 0;
                break;
        }
        return !parado;
    }

    public void passaCiclo() {
        do {
            passaSubciclo();
        } while (subciclo != 0 && !parado);
    }

    public void passaInstrucao() {
        int limite = 0;
        do {
            passaCiclo();
            limite++;
        } while (!parado && !(mpc == 0 && espera == 0) && limite < 10000);
    }

    private void subciclo1() {
        limpaAtividade();
        if (mpc == 0) {
            // fim do programa
            int instr = mp.espiar(regs.get(Registradores.PC));
            if (instr == (0x6000 | regs.get(Registradores.PC))) {
                parado = true;
                subciclo = 0;
                mensagem = "Fim do programa (JUMP no endereço " + regs.get(Registradores.PC) + ")";
                return;
            }
            instrucoes++;
        }
        mir = mc.get(mpc);
        if (mir == null) {
            parado = true;
            subciclo = 0;
            mensagem = "Erro: posição " + mpc + " da memória de controle está vazia";
            return;
        }
        mensagem = "Subciclo 1: MIR recebe a microinstrução " + mpc;
    }

    private void subciclo2() {
        latchA = regs.get(mir.a);
        latchB = regs.get(mir.b);
        mensagem = "Subciclo 2: latch A = " + Registradores.NOMES[mir.a] + ", latch B = " + Registradores.NOMES[mir.b];
    }

    private void subciclo3() {
        saidaAmux = amux.seleciona(mir.amux, latchA, mbr);
        ula.calcula(mir.ula, saidaAmux, latchB);
        n = ula.getN();
        z = ula.getZ();
        controlador.setN(n);
        controlador.setZ(z);
        deslocador.desloca(mir.desl, ula.getResultado());
        if (mir.mar == 1) {
            mar = latchB & 0xFFF;
            marVeioDoPc = mir.b == Registradores.PC;
        }
        mensagem = "Subciclo 3: ULA (" + ULA.OPERACOES[mir.ula] + ") e deslocador";
    }

    private void subciclo4() {
        int resultado = deslocador.getSaida();
        if (mir.enc == 1) {
            regs.set(mir.c, resultado);
        }
        if (mir.mbr == 1) {
            mbr = resultado;
        }

        // rd
        if (mir.rd == 1) {
            rdSeguidos++;
            if (rdSeguidos == 2) {
                Cache cache = marVeioDoPc ? cacheInstrucoes : cacheDados;
                mbr = cache.ler(mar);
                leuMemoria = true;
                acessoInstrucao = marVeioDoPc;
                acertouCache = cache.isAtiva() && cache.isUltimoAcerto();
                espera += cache.pegaEspera();
                rdSeguidos = 0;
            }
        } else {
            rdSeguidos = 0;
        }

        // wr
        if (mir.wr == 1) {
            wrSeguidos++;
            if (wrSeguidos == 2) {
                cacheDados.escrever(mar, mbr);
                cacheInstrucoes.atualizaSeTiver(mar, mbr);
                escreveuMemoria = true;
                acertouCache = cacheDados.isAtiva() && cacheDados.isUltimoAcerto();
                espera += cacheDados.pegaEspera();
                wrSeguidos = 0;
            }
        } else {
            wrSeguidos = 0;
        }

        // MMUX
        desviou = controlador.desvia(mir.cond);
        mpc = mmux.seleciona(desviou, mpc, mir.addr);
        ciclos++;
        mensagem = "Subciclo 4: barramento C, memória e próximo MPC = " + mpc;
    }

    public int valorMemoria(int endereco) {
        return cacheDados.espiar(endereco);
    }

    public double cpi() {
        if (instrucoes == 0) return 0;
        return (double) ciclos / instrucoes;
    }
}
