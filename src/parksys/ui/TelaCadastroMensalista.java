package parksys.ui;
import parksys.entities.Mensalista;
import parksys.exceptions.VagaOcupadaException;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
 
// p05 (MVC): sem logica de negocio — delega ao gerenciador
// s06: serializa no windowClosing
public class TelaCadastroMensalista extends JDialog {

    private final GerenciadorEstacionamento gerenciador;
    private final TelaInicial               telaInicial;
 
    private JTextField txtNome;
    private JTextField txtPlaca;
    private JTextField txtVaga;
    private JTextField txtMensalidade;
 
    public TelaCadastroMensalista(TelaInicial parent) {
        super(parent, "Cadastrar Mensalista", true);
        this.telaInicial = parent;
 
        // p01: Singleton — pega a mesma instancia do gerenciador
        this.gerenciador = GerenciadorEstacionamento.getInstance();
        construirTela();
 
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { fecharESalvar(); }
        });
    }
 
    private void construirTela() {
        setSize(420, 400); // em css: width: 420px; height: 400px;
        setLocationRelativeTo(getParent());
        setResizable(false);
 
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Color.WHITE);
 
        // cabecalho preto — em css: header { background: #000; }
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(Color.BLACK);
        cab.setBorder(new EmptyBorder(18, 20, 18, 20));
 
        JLabel titulo = new JLabel("Cadastrar Mensalista");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        titulo.setForeground(Color.WHITE);
        cab.add(titulo);
 
        // formulario com GridBagLayout — em css: display: grid;
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 0, 7, 12); // em css: gap: 7px 12px;
        g.fill   = GridBagConstraints.HORIZONTAL;
 
        txtNome        = campo();
        txtPlaca       = campo(); txtPlaca.setToolTipText("Formato: ABC1234 ou ABC1D23");
        txtVaga        = campo(); txtVaga.setToolTipText("Ex: A01, B07");
        txtMensalidade = campo(); txtMensalidade.setToolTipText("Ex: 250.00");
 
        linha(form, g, 0, "Nome completo:",  txtNome);
        linha(form, g, 1, "Placa:",          txtPlaca);
        linha(form, g, 2, "Vaga reservada:", txtVaga);
        linha(form, g, 3, "Mensalidade R$:", txtMensalidade);
 
        // dois botoes lado a lado — em css: display: flex; gap: 8px; justify-content: flex-end;
        JButton btnCadastrar = new JButton("Cadastrar");
        JButton btnRemover   = new JButton("Remover");
        estilizarBotao(btnCadastrar);
        estilizarBotao(btnRemover);
 
        btnCadastrar.addActionListener(e -> cadastrar());
        btnRemover  .addActionListener(e -> remover());
 
        // FlowLayout RIGHT = justify-content: flex-end; no css
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(new EmptyBorder(0, 28, 20, 28));
        rodape.add(btnRemover);
        rodape.add(btnCadastrar);
 
        raiz.add(cab,    BorderLayout.NORTH);
        raiz.add(form,   BorderLayout.CENTER);
        raiz.add(rodape, BorderLayout.SOUTH);
        add(raiz);
    }
 
    // cria um JTextField com borda fina — em css: input { border: 1px solid #c8c8c8; }
    private JTextField campo() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        return f;
    }
 
    // coloca um par label + campo numa linha do grid
    // em css seria uma linha com duas colunas: label e input
    private void linha(JPanel p, GridBagConstraints g, int row, String rotulo, JTextField campo) {
        g.gridwidth = 1;
        g.gridx = 0; g.gridy = row; g.weightx = 0.35;
        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(Color.BLACK);
        p.add(lbl, g);
        g.gridx = 1; g.weightx = 0.65;
        p.add(campo, g);
    }
 
    private void cadastrar() {
        String nome  = txtNome.getText().trim();
        String placa = txtPlaca.getText().trim().toUpperCase();
        String vaga  = txtVaga.getText().trim().toUpperCase();
        String mens  = txtMensalidade.getText().trim();
 
        if (nome.isEmpty() || placa.isEmpty() || vaga.isEmpty() || mens.isEmpty()) {
            erro("Preencha todos os campos."); return;
        }
 
        double mensalidade;
        try {
            // replace(',', '.') aceita virgula como separador decimal
            mensalidade = Double.parseDouble(mens.replace(",", "."));
        } catch (NumberFormatException ex) {
            erro("Mensalidade invalida."); return;
        }
 
        try {
            // p05: delega ao gerenciador — a tela nao toca nas vagas diretamente
            gerenciador.cadastrarMensalista(new Mensalista(nome, placa, vaga, mensalidade));
            JOptionPane.showMessageDialog(this,
                    "Mensalista cadastrado!\n" + nome + " - Vaga " + vaga,
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limpar();
        } catch (VagaOcupadaException ex) {
            erro("Vaga indisponivel: " + ex.getMessage());
        }
    }
 
    private void remover() {
        String placa = txtPlaca.getText().trim().toUpperCase();
        if (placa.isEmpty()) { erro("Informe a placa para remover."); return; }
        // p05: delega ao gerenciador
        gerenciador.removerMensalista(placa);
        JOptionPane.showMessageDialog(this,
                "Mensalista removido (se existia).", "OK", JOptionPane.INFORMATION_MESSAGE);
        limpar();
    }
 
    private void limpar() {
        txtNome.setText(""); txtPlaca.setText("");
        txtVaga.setText(""); txtMensalidade.setText("");
    }
 
    // s06: salva antes de fechar — em js seria localStorage.setItem() no beforeunload
    private void fecharESalvar() {
        GerenciadorArquivo.serializar(
                gerenciador.getVagas(), gerenciador.getRegistros(),
                gerenciador.getMensalistas(), telaInicial.getArquivo());
        dispose();
    }
 
    private void erro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
 
    // botao preto — setUI pinta o fundo direto ignorando o Look and Feel do Linux
    private void estilizarBotao(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.BLACK);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setPreferredSize(new Dimension(120, 38)); // em css: width: 120px; height: 38px;
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
