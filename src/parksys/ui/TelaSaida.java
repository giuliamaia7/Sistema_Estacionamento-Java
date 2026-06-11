package parksys.ui;
import parksys.entities.Registro;
import parksys.exceptions.VeiculoNaoEncontradoException;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
 
// p05 (MVC): sem logica de negocio — delega tudo ao gerenciador
// s06: serializa no windowClosing
public class TelaSaida extends JDialog{

 
    private final GerenciadorEstacionamento gerenciador;
    private final TelaInicial               telaInicial;
    private JTextField txtPlaca;
 
    public TelaSaida(TelaInicial parent) {
        // true = modal — bloqueia a janela pai enquanto essa esta aberta
        // em css seria um backdrop com pointer-events: none na janela de baixo
        super(parent, "Registrar Saida", true);
        this.telaInicial = parent;
 
        // p01: Singleton — mesma instancia compartilhada por todas as telas
        this.gerenciador = GerenciadorEstacionamento.getInstance();
        construirTela();
 
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { fecharESalvar(); }
        });
    }
 
    private void construirTela() {
        setSize(420, 260); // em css: width: 420px; height: 260px;
        setLocationRelativeTo(getParent());
        setResizable(false);
 
        // painel raiz branco com BorderLayout
        // BorderLayout divide em 5 regioes: NORTH, SOUTH, EAST, WEST, CENTER
        // em css seria display: flex; flex-direction: column;
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Color.WHITE);
 
        // cabecalho preto — em css: header { background: #000; padding: 18px 20px; }
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(Color.BLACK);
        cab.setBorder(new EmptyBorder(18, 20, 18, 20)); // em css: padding: 18px 20px;
 
        JLabel titulo = new JLabel("Registrar Saida de Veiculo");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        titulo.setForeground(Color.WHITE);
        cab.add(titulo);
 
        // formulario com GridBagLayout — em css: display: grid;
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(28, 28, 20, 28)); // em css: padding: 28px;
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 0, 12);
        g.fill   = GridBagConstraints.HORIZONTAL;
 
        JLabel lbl = new JLabel("Placa do veiculo:");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(Color.BLACK);
 
        txtPlaca = new JTextField();
        txtPlaca.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtPlaca.setBorder(new LineBorder(new Color(200, 200, 200), 1)); // em css: border: 1px solid #c8c8c8;
        txtPlaca.setToolTipText("Formato: ABC1234 ou ABC1D23");
 
        g.gridx = 0; g.gridy = 0; g.weightx = 0.35;
        form.add(lbl, g);
        g.gridx = 1; g.weightx = 0.65;
        form.add(txtPlaca, g);
 
        JButton btn = new JButton("Registrar Saida");
        estilizarBotao(btn);
        btn.addActionListener(e -> registrar());
 
        // FlowLayout RIGHT joga o botao pra direita — em css: justify-content: flex-end;
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(new EmptyBorder(0, 28, 20, 28));
        rodape.add(btn);
 
        raiz.add(cab,    BorderLayout.NORTH);
        raiz.add(form,   BorderLayout.CENTER);
        raiz.add(rodape, BorderLayout.SOUTH);
        add(raiz);
    }
 
    private void registrar() {
        String placa = txtPlaca.getText().trim().toUpperCase();
        if (placa.isEmpty()) { erro("Informe a placa."); return; }
        try {
            // p05: delega ao gerenciador — a tela nao calcula tarifa nem mexe em vagas
            Registro reg = gerenciador.registrarSaida(placa);
            JOptionPane.showMessageDialog(this,
                    String.format("Saida registrada!\nPlaca: %s\nValor cobrado: R$ %.2f",
                            reg.getPlaca(), reg.getValorPago()),
                    "Saida OK", JOptionPane.INFORMATION_MESSAGE);
            txtPlaca.setText("");
        } catch (VeiculoNaoEncontradoException ex) {
            erro("Veiculo nao encontrado: " + ex.getMessage());
        }
    }
 
    // s06: salva antes de fechar — em js seria localStorage.setItem() no beforeunload
    private void fecharESalvar() {
        GerenciadorArquivo.serializar(
                gerenciador.getVagas(), gerenciador.getRegistros(),
                gerenciador.getMensalistas(), telaInicial.getArquivo());
        dispose(); // fecha o dialog e libera memoria — em js seria modal.remove()
    }
 
    private void erro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
 
    // botao preto que nunca muda de cor — setUI sobrescreve o Look and Feel do Linux
    // em js seria forcar background-color: #000 com !important no inline style
    private void estilizarBotao(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.BLACK);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setPreferredSize(new Dimension(160, 40)); // em css: width: 160px; height: 40px;
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // em css: cursor: pointer;
 
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
