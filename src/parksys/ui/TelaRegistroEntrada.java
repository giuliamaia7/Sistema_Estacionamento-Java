package parksys.ui;
import parksys.entities.Vaga;
import parksys.enums.TipoVeiculo;
import parksys.exceptions.PlacaInvalidaException;
import parksys.exceptions.VagaOcupadaException;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


// t05: ComboBox preenchido com TipoVeiculo.values() — nunca com strings fixas
// p05 (MVC): sem logica de negocio — delega tudo ao gerenciador
// s06: serializa no windowClosing e desserializa no construtor
public class TelaRegistroEntrada extends JDialog{
     
    private final GerenciadorEstacionamento gerenciador;
    private final TelaInicial               telaInicial;
 
    private JTextField             txtPlaca;
    private JComboBox<TipoVeiculo> cmbTipo;   // t05: generico com o enum TipoVeiculo
    private JComboBox<String>      cmbVaga;
 
    // dividi o label de info em dois pra poder atualizar cada linha separado
    // em js seria dois elementos span diferentes no dom com .textContent separado
    private JLabel lblTarifa;
    private JLabel lblVagasQtd;
 
    public TelaRegistroEntrada(TelaInicial parent) {
        // super(parent, titulo, modal) — em js seria super() no constructor de uma classe que estende outra
        // o true de modal bloqueia a janela pai enquanto essa esta aberta — em css seria pointer-events: none no backdrop
        super(parent, "Registrar Entrada", true);
        this.telaInicial = parent;
 
        // p01: pega a mesma instancia do gerenciador — em js seria import { gerenciador } from './store'
        this.gerenciador = GerenciadorEstacionamento.getInstance();
        construirTela();
 
        // intercepta o fechar da janela pra salvar antes — em js seria window.addEventListener('beforeunload', ...)
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { fecharESalvar(); }
        });
    }
 
    private void construirTela() {
        setSize(420, 420); // em css: width: 420px; height: 420px;
        setLocationRelativeTo(getParent());
        setResizable(false);
 
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Color.WHITE);
 
        // cabecalho preto com texto branco — em css: header { background: #000; color: #fff; padding: 18px 20px; }
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(Color.BLACK);
        cab.setBorder(new EmptyBorder(18, 20, 18, 20)); // em css: padding: 18px 20px;
 
        JLabel titulo = new JLabel("Registrar Entrada");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        cab.add(titulo);
 
        // GridBagLayout e o display: grid do swing — permite posicionar cada elemento com coordenada
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(15, 24, 15, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 6); // em css: gap: 8px 6px; no grid
        g.fill   = GridBagConstraints.HORIZONTAL;
 
        txtPlaca = new JTextField();
        txtPlaca.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtPlaca.setToolTipText("Formato: ABC1234 ou ABC1D23");
 
        // t05: TipoVeiculo.values() retorna todas as constantes do enum automaticamente
        // nunca coloco strings fixas como "Carro", "Moto" — se o enum mudar, o combobox atualiza sozinho
        // em js seria um select preenchido com Object.keys(TipoVeiculo).map(...)
        cmbTipo = new JComboBox<>(TipoVeiculo.values());
        estilizarComboBox(cmbTipo);
 
        cmbVaga = new JComboBox<>();
        estilizarComboBox(cmbVaga);
 
        // labels cinzas de informacao — em css: color: #8c8c8c; font-style: italic;
        lblTarifa = new JLabel(" ");
        lblTarifa.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblTarifa.setForeground(new Color(140, 140, 140));
 
        lblVagasQtd = new JLabel(" ");
        lblVagasQtd.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblVagasQtd.setForeground(new Color(140, 140, 140));
 
        campo(form, g, 0, "Placa:",        txtPlaca);
        campo(form, g, 1, "Tipo:",         cmbTipo);
        campo(form, g, 2, "Vaga inicial:", cmbVaga);
 
        // gridwidth = 2 faz o label ocupar as duas colunas do grid
        // em css seria grid-column: span 2;
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        form.add(lblTarifa, g);
 
        g.gridy = 4;
        form.add(lblVagasQtd, g);
 
        JButton btn = new JButton("Registrar Entrada");
        estilizarBotaoPreto(btn);
        btn.addActionListener(e -> registrar());
 
        // coloco o botao dentro do proprio grid pra nao sumir com layouts pequenos
        // em css seria align-self: flex-end; no ultimo item do form
        g.gridy     = 5;
        g.gridwidth = 2;
        g.fill      = GridBagConstraints.NONE;
        g.anchor    = GridBagConstraints.LINE_END; // em css: align-self: flex-end;
        g.insets    = new Insets(20, 0, 0, 0);    // em css: margin-top: 20px;
        form.add(btn, g);
 
        raiz.add(cab,  BorderLayout.NORTH);
        raiz.add(form, BorderLayout.CENTER);
        add(raiz);
 
        atualizarInfo();
    }
 
    // adiciona um par label + campo numa linha do GridBagLayout
    // em css seria uma linha do display: grid com duas colunas
    private void campo(JPanel p, GridBagConstraints g, int linha, String rotulo, JComponent campo) {
        g.gridwidth = 1;
        g.gridx = 0; g.gridy = linha; g.weightx = 0.3;
        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(Color.BLACK); // em css: color: #000;
        p.add(lbl, g);
        g.gridx = 1; g.weightx = 0.7;
        p.add(campo, g);
    }
 
    private void atualizarInfo() {
        // casting necessario pq getSelectedItem() retorna Object — em js nao precisaria pq e dinamico
        TipoVeiculo tipo = (TipoVeiculo) cmbTipo.getSelectedItem();
        if (tipo == null) return;
 
        // t03: dados vem do enum via getters — sem valores fixos no codigo
        // em js seria lblTarifa.textContent = `Tarifa: R$ ${tipo.tarifaHora}/h`
        lblTarifa.setText(String.format("Tarifa: R$ %.2f/h", tipo.getTarifaHora()));
        lblVagasQtd.setText(String.format("Vagas necessarias: %d", tipo.getVagasOcupadas()));
        atualizarVagas();
    }
 
    private void atualizarVagas() {
        cmbVaga.removeAllItems(); // limpa o select antes de repovoar — em js seria select.innerHTML = ''
        // p05: pede a lista de vagas ao gerenciador — a tela nao acessa o HashMap diretamente
        List<Vaga> disponiveis = gerenciador.getVagasDisponiveis();
        for (Vaga v : disponiveis)
            cmbVaga.addItem(v.getId() + "  [" + v.getStatus().getDescricao() + "]");
        if (cmbVaga.getItemCount() == 0)
            cmbVaga.addItem("(sem vagas disponiveis)");
    }
 
    private void registrar() {
        String placa     = txtPlaca.getText().trim().toUpperCase();
        TipoVeiculo tipo = (TipoVeiculo) cmbTipo.getSelectedItem();
        String vagaItem  = (String) cmbVaga.getSelectedItem();
 
        if (placa.isEmpty()) { erro("Informe a placa."); return; }
        if (vagaItem == null || vagaItem.startsWith("(sem")) { erro("Sem vagas disponiveis."); return; }
 
        // pega so o id da vaga do texto "A01  [Livre]" — em js seria vagaItem.split(/\s+/)[0]
        String idVaga = vagaItem.split("\\s+")[0];
        try {
            // p05: delega ao gerenciador — a tela nao decide nada sobre vagas
            gerenciador.registrarEntrada(placa, tipo, idVaga);
            JOptionPane.showMessageDialog(this,
                    "Entrada registrada!\nPlaca: " + placa +
                    "\nVaga: " + idVaga +
                    "\nVagas ocupadas: " + tipo.getVagasOcupadas(),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            txtPlaca.setText("");
            atualizarVagas();
        } catch (VagaOcupadaException | PlacaInvalidaException ex) {
            erro(ex.getMessage());
        }
    }
 
    // s06: salva antes de fechar — em js seria localStorage.setItem() no beforeunload
    private void fecharESalvar() {
        GerenciadorArquivo.serializar(
                gerenciador.getVagas(), gerenciador.getRegistros(),
                gerenciador.getMensalistas(), telaInicial.getArquivo());
        dispose(); // fecha o JDialog e libera memoria — em js seria modal.remove() do dom
    }
 
    private void erro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
 
    /*
     * estilizo o ComboBox pra parecer um input moderno sem a linha vertical cinza da seta
     * o swing usa o padrao de UI delegates: cada componente tem um objeto "UI" que decide
     * como ele e desenhado. sobrescrevo o BasicComboBoxUI pra remover a borda do botao da seta
     * em css seria como remover o appearance: auto de um <select> e estilizar do zero
     */
    private void estilizarComboBox(JComboBox<?> cmb) {
        cmb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cmb.setBackground(Color.WHITE);
        cmb.setForeground(Color.BLACK);
 
        cmb.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton seta = super.createArrowButton();
                seta.setBorder(BorderFactory.createEmptyBorder()); // em css: border: none; na seta
                seta.setBackground(Color.WHITE); // fundo branco unificado com o campo
                return seta;
            }
 
            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                popup.setBorder(new LineBorder(new Color(200, 200, 200), 1)); // em css: border: 1px solid #c8c8c8;
                return popup;
            }
        });
 
        // borda cinza fina no combobox inteiro — em css: border: 1px solid #c8c8c8;
        cmb.setBorder(new LineBorder(new Color(200, 200, 200), 1));
 
        // atualiza os dados quando o tipo muda — em js seria select.addEventListener('change', ...)
        cmb.addActionListener(e -> {
            if (cmb == cmbTipo) atualizarInfo();
        });
    }
 
    /*
     * botao preto com texto branco que nunca muda de cor
     * o setUI sobrescreve o metodo paint() que e chamado pelo swing pra desenhar o botao
     * sem isso o Look and Feel do linux fica sobrescrevendo a cor com o tema do sistema
     * em js seria forcar btn.style.backgroundColor = '#000' com !important no inline style
     */
    private void estilizarBotaoPreto(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.BLACK);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // em css: padding: 10px 20px;
        btn.setPreferredSize(new Dimension(180, 42)); // em css: width: 180px; height: 42px;
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // em css: cursor: pointer;
 
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, c.getWidth(), c.getHeight()); // pinta o fundo preto antes de tudo
                super.paint(g, c); // depois renderiza o texto por cima
            }
        });
    }

}
