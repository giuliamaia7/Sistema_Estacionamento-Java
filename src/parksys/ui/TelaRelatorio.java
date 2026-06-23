package parksys.ui;
import parksys.entities.Registro;
import parksys.entities.Vaga;
import parksys.enums.StatusVaga;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


// c06: usa entrySet() do HashMap e for-each sobre TreeSet e lista por receita
// s04: botao exportar chama GerenciadorArquivo.exportarRelatorioTxt()
// p05 (MVC): sem logica de negocio
// s06: serializa no windowClosing
public class TelaRelatorio extends JDialog {

    private final GerenciadorEstacionamento gerenciador;
    private final TelaInicial               telaInicial;
 
    // DateTimeFormatter formata LocalDateTime pra string — em js seria date.toLocaleDateString('pt-BR')
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
 
    public TelaRelatorio(TelaInicial parent) {
        super(parent, "Relatorio do Estacionamento", true);
        this.telaInicial = parent;
 
        // p01: Singleton
        this.gerenciador = GerenciadorEstacionamento.getInstance();
        construirTela();
 
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { fecharESalvar(); }
        });
    }
 
    private void construirTela() {
        setSize(640, 520); // em css: width: 640px; height: 520px;
        setLocationRelativeTo(getParent());
        setResizable(true);
 
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Color.WHITE);
 
        // cabecalho preto — em css: header { background: #000; }
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(Color.BLACK);
        cab.setBorder(new EmptyBorder(18, 20, 18, 20));
        JLabel titulo = new JLabel("Relatorio do Estacionamento");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        titulo.setForeground(Color.WHITE);
        cab.add(titulo);
 
        // JTabbedPane e o componente de abas do swing — em js seria um sistema de tabs com data-tab
        JTabbedPane abas = new JTabbedPane();
        abas.setBackground(Color.WHITE);
        abas.setFont(new Font("SansSerif", Font.PLAIN, 12));
 
        abas.addTab("Mapa de Vagas",  painelVagas());
        abas.addTab("Cronologico",    painelCronologico());
        abas.addTab("Por Receita",    painelReceita());
        abas.addTab("Resumo",         painelResumo());
 
        JButton btnExportar = new JButton("Exportar .txt");
        JButton btnFechar   = new JButton("Fechar");
        estilizarBotao(btnExportar);
        estilizarBotao(btnFechar);
        btnExportar.addActionListener(e -> exportar());
        btnFechar  .addActionListener(e -> fecharESalvar());
 
        // FlowLayout RIGHT = justify-content: flex-end; no css
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.setBackground(Color.WHITE);
        rodape.add(btnExportar);
        rodape.add(btnFechar);
 
        raiz.add(cab,    BorderLayout.NORTH);
        raiz.add(abas,   BorderLayout.CENTER);
        raiz.add(rodape, BorderLayout.SOUTH);
        add(raiz);
    }
 
    /*
     * c06: uso entrySet() do HashMap pra percorrer todas as vagas
     * entrySet() retorna um Set de pares chave-valor (Map.Entry)
     * em js seria Object.entries(vagas).forEach(([id, vaga]) => { ... })
     * conto livres, ocupadas e reservadas no mesmo loop sem precisar de 3 iteracoes separadas
     */
    private JPanel painelVagas() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
 
        String[] colunas = {"Vaga", "Status"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            // isCellEditable false impede o usuario de editar as celulas da tabela
            // em js seria como desabilitar o contenteditable numa td
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
 
        // c06: entrySet() percorre o HashMap completo
        Map<String, Vaga> vagas = gerenciador.getVagas();
        long livres = 0, ocupadas = 0, reservadas = 0;
        for (Map.Entry<String, Vaga> entry : vagas.entrySet()) {
            modelo.addRow(new Object[]{ entry.getKey(), entry.getValue().getStatus().getDescricao() });
            StatusVaga s = entry.getValue().getStatus();
            if      (s == StatusVaga.LIVRE)     livres++;
            else if (s == StatusVaga.OCUPADA)   ocupadas++;
            else if (s == StatusVaga.RESERVADA) reservadas++;
        }
 
        JTable tabela = tabelaEstilizada(modelo);
 
        JLabel resumo = new JLabel(String.format(
                "Livres: %d  |  Ocupadas: %d  |  Reservadas: %d", livres, ocupadas, reservadas));
        resumo.setFont(new Font("SansSerif", Font.BOLD, 12));
        resumo.setForeground(Color.BLACK);
        resumo.setBorder(new EmptyBorder(8, 4, 4, 4));
 
        p.add(new JScrollPane(tabela), BorderLayout.CENTER);
        p.add(resumo, BorderLayout.SOUTH);
        return p;
    }
 
    /*
     * c06: for-each sobre TreeSet — o Comparable do Registro garante ordem cronologica automatica
     * nao preciso chamar sort() pq o TreeSet ja mantem os elementos ordenados ao inserir
     * em js seria como um array que se auto-ordena ao receber um novo elemento
     */
    private JPanel painelCronologico() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
 
        String[] colunas = {"Placa", "Tipo", "Entrada", "Saida", "Valor R$"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
 
        // c06: for-each sobre TreeSet — ja ordenado por dataEntrada via Comparable
        // em js seria registros.sort((a,b) => a.dataEntrada - b.dataEntrada).forEach(...)
        for (Registro r : gerenciador.getRegistrosOrdenados()) {
            modelo.addRow(new Object[]{
                    r.getPlaca(),
                    r.getTipoVeiculo().getNomeExibicao(),
                    r.getDataEntrada().format(FMT),
                    r.getDataSaida() != null ? r.getDataSaida().format(FMT) : "-",
                    r.getDataSaida() != null ? String.format("R$ %.2f", r.getValorPago()) : "-"
            });
        }
 
        p.add(new JScrollPane(tabelaEstilizada(modelo)), BorderLayout.CENTER);
        return p;
    }
 
    /*
     * c06: for-each sobre lista ordenada por Comparator (valorPago decrescente)
     * Comparator e diferente de Comparable — aqui uso uma regra externa sem modificar a classe Registro
     * em js seria registros.sort((a,b) => b.valorPago - a.valorPago).forEach(...)
     */
    private JPanel painelReceita() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
 
        String[] colunas = {"Placa", "Tipo", "Valor R$", "Saida"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
 
        // c06: lista ja vem ordenada do gerenciador via Comparator de valorPago desc
        List<Registro> porReceita = gerenciador.getRegistrosPorReceita();
        for (Registro r : porReceita) {
            modelo.addRow(new Object[]{
                    r.getPlaca(),
                    r.getTipoVeiculo().getNomeExibicao(),
                    String.format("R$ %.2f", r.getValorPago()),
                    r.getDataSaida().format(FMT)
            });
        }
 
        // c06: soma total de receita via calcularReceita() do gerenciador
        double total = gerenciador.calcularReceita();
        JLabel lblTotal = new JLabel(String.format("Receita total: R$ %.2f", total));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTotal.setForeground(Color.BLACK);
        lblTotal.setBorder(new EmptyBorder(8, 4, 4, 4));
 
        p.add(new JScrollPane(tabelaEstilizada(modelo)), BorderLayout.CENTER);
        p.add(lblTotal, BorderLayout.SOUTH);
        return p;
    }
 
    private JPanel painelResumo() {
        // GridLayout(5,1) = 5 linhas, 1 coluna — em css: display: grid; grid-template-rows: repeat(5, 1fr);
        JPanel p = new JPanel(new GridLayout(5, 1, 0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 28, 20, 28));
 
        long totalVeiculos = gerenciador.getRegistros().size();
        long emAndamento   = gerenciador.getRegistros().stream()
                .filter(r -> r.getDataSaida() == null).count();
        long finalizados   = totalVeiculos - emAndamento;
        long mensalistas   = gerenciador.getMensalistas().size();
        double receita     = gerenciador.calcularReceita();
 
        p.add(linhaResumo("Total de registros:",  String.valueOf(totalVeiculos)));
        p.add(linhaResumo("Em andamento:",         String.valueOf(emAndamento)));
        p.add(linhaResumo("Finalizados:",           String.valueOf(finalizados)));
        p.add(linhaResumo("Mensalistas ativos:",    String.valueOf(mensalistas)));
        p.add(linhaResumo("Receita total:",         String.format("R$ %.2f", receita)));
        return p;
    }
 
    // linha com label e valor lado a lado — em css: div { display: flex; justify-content: space-between; }
    private JPanel linhaResumo(String rotulo, String valor) {
        JPanel l = new JPanel(new BorderLayout());
        l.setBackground(new Color(245, 245, 245)); // em css: background-color: #f5f5f5;
        l.setBorder(new EmptyBorder(8, 14, 8, 14));
 
        JLabel lblR = new JLabel(rotulo);
        lblR.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblR.setForeground(Color.BLACK);
 
        JLabel lblV = new JLabel(valor, SwingConstants.RIGHT);
        lblV.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblV.setForeground(Color.BLACK);
 
        l.add(lblR, BorderLayout.WEST);
        l.add(lblV, BorderLayout.EAST);
        return l;
    }
 
    /*
     * cria uma JTable com cabecalho preto e linhas alternadas
     * nao uso setBackground no JTableHeader pq o Look and Feel do Linux ignora isso
     * a solucao e sobrescrever o DefaultRenderer do header — assim pinto cada celula manualmente
     * em css seria thead tr { background: #000; } e tr:nth-child(even) { background: #f5f5f5; }
     */
    private JTable tabelaEstilizada(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabela.setRowHeight(24); // em css: height: 24px; por linha
 
        // renderer customizado pro cabecalho — igual a um render prop no react mas pra cada celula do thead
        tabela.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(val != null ? val.toString() : "");
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(Color.BLACK);
                lbl.setOpaque(true); // precisa ser true pra o fundo aparecer
                lbl.setBorder(new EmptyBorder(6, 8, 6, 8)); // em css: padding: 6px 8px;
                return lbl;
            }
        });
 
        // renderer pras linhas do corpo — alterna branco e cinza claro
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    // row % 2 == 0 checa se a linha e par — em css: tr:nth-child(even)
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
 
        tabela.setGridColor(new Color(220, 220, 220)); // em css: border-color: #dcdcdc;
        tabela.setShowGrid(true);
        return tabela;
    }
 
    // s04: abre dialogo pra salvar o arquivo .txt e chama o GerenciadorArquivo
    // em js seria equivalente a criar um <a download> e chamar .click() no dom
    private void exportar() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("relatorio_parksys.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            // p05: delega ao GerenciadorArquivo — a tela nao escreve arquivo diretamente
            GerenciadorArquivo.exportarRelatorioTxt(gerenciador.getRegistros(), path);
            JOptionPane.showMessageDialog(this,
                    "Relatorio exportado!\n" + path, "Exportado", JOptionPane.INFORMATION_MESSAGE);
        }
    }
 
    // s06: salva antes de fechar — em js seria localStorage.setItem() no beforeunload
    private void fecharESalvar() {
        GerenciadorArquivo.serializar(
                gerenciador.getVagas(), gerenciador.getRegistros(),
                gerenciador.getMensalistas(), telaInicial.getArquivo());
        dispose();
    }
 
    private void estilizarBotao(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.BLACK);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setPreferredSize(new Dimension(140, 36)); // em css: width: 140px; height: 36px;
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
                super.paint(g, c);
            }
        });
    }
}
