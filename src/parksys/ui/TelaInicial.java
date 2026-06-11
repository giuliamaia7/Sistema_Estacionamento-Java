package parksys.ui;
import parksys.observer.PainelMonitor;
import parksys.services.DadosParkSys;
import parksys.services.GerenciadorArquivo;
import parksys.services.GerenciadorEstacionamento;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;


// p01: pega a instancia unica do gerenciador via getInstance() — padrao Singleton
// p06: registra o PainelMonitor como observer ao abrir e remove ao fechar
// s06: desserializa os dados ao abrir e serializa no windowClosing
public class TelaInicial extends JFframe {

 
    // nome do arquivo .ser onde os dados ficam salvos entre sessoes
    // em js seria o equivalente ao nome da chave no localStorage
    private static final String ARQUIVO = "parksys_dados.ser";
 
    private final GerenciadorEstacionamento gerenciador;
    private final PainelMonitor             painelMonitor;
 
    public TelaInicial() {
        // p01: getInstance() garante que pego a mesma instancia usada por todas as telas
        // em js seria tipo um modulo global exportado como singleton: import { gerenciador } from './store'
        gerenciador = GerenciadorEstacionamento.getInstance();
 
        // s06: carrega os dados salvos ao abrir — em js seria localStorage.getItem('dados')
        DadosParkSys dados = GerenciadorArquivo.desserializar(ARQUIVO);
        gerenciador.restaurarDados(dados.getVagas(), dados.getRegistros(), dados.getMensalistas());
 
        // p06: registro o PainelMonitor como observer — em js seria gerenciador.addEventListener('mudanca', painel)
        painelMonitor = new PainelMonitor();
        gerenciador.addObserver(painelMonitor);
 
        construirTela();
 
        // DO_NOTHING_ON_CLOSE intercepta o X da janela sem fechar automaticamente
        // em js seria window.addEventListener('beforeunload', (e) => { e.preventDefault(); ... })
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // p06: remove o observer antes de fechar — em js seria removeEventListener()
                gerenciador.removeObserver(painelMonitor);
 
                // s06: salva os dados ao fechar — em js seria localStorage.setItem('dados', JSON.stringify(...))
                GerenciadorArquivo.serializar(
                        gerenciador.getVagas(),
                        gerenciador.getRegistros(),
                        gerenciador.getMensalistas(),
                        ARQUIVO
                );
 
                // encerra a JVM — em js seria process.exit(0) no nodejs
                System.exit(0);
            }
        });
    }
 
    private void construirTela() {
        setTitle("Gestao de Estacionamento");
 
        // setSize define largura e altura fixas — em css seria width: 460px; height: 460px;
        setSize(460, 460);
 
        // centraliza na tela — em css seria margin: auto; numa div de tamanho fixo
        setLocationRelativeTo(null);
        setResizable(false);
 
        // PainelFundoImagem e um JPanel customizado que desenha fundo.jpg por baixo de tudo
        // em css seria o div raiz com background-image: url('fundo.jpg'); background-size: cover;
        PainelFundoImagem raiz = new PainelFundoImagem("fundo.jpg");
 
        // FlowLayout CENTER empilha os filhos no centro horizontalmente
        // em css seria display: flex; justify-content: center;
        JPanel cabecalho = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cabecalho.setOpaque(false); // fundo transparente — em css seria background: transparent;
        cabecalho.setBorder(new EmptyBorder(30, 24, 15, 24)); // em css: padding: 30px 24px 15px 24px;
 
        JLabel titulo = new JLabel("ESTACIONAMENTO");
        titulo.setFont(new Font("Arial Black", Font.PLAIN, 32)); // em css: font-family: 'Arial Black'; font-size: 32px;
        titulo.setForeground(Color.WHITE); // em css: color: #ffffff;
        cabecalho.add(titulo);
 
        // GridBagLayout e o layout mais poderoso do swing — equivalente ao display: grid do css
        JPanel menu = new JPanel(new GridBagLayout());
        menu.setOpaque(false);
        menu.setBorder(new EmptyBorder(10, 32, 20, 32));
 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
 
        // RELATIVE faz cada componente adicionado ir pra proxima linha automaticamente
        // em css seria flex-direction: column; com gap automatico
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(8, 0, 8, 0); // em css: margin: 8px 0;
 
        JButton btnEntrada    = criarBotaoArredondado("Registrar Entrada");
        JButton btnSaida      = criarBotaoArredondado("Registrar Saida");
        JButton btnMensalista = criarBotaoArredondado("Cadastrar Mensalista");
        JButton btnRelatorio  = criarBotaoArredondado("Ver Relatorio");
 
        // p05 (MVC): a tela so abre outras telas, sem logica de negocio aqui
        // em js seria btn.addEventListener('click', () => navigate('/entrada'))
        btnEntrada   .addActionListener(e -> new TelaRegistroEntrada(this).setVisible(true));
        btnSaida     .addActionListener(e -> new TelaSaida(this).setVisible(true));
        btnMensalista.addActionListener(e -> new TelaCadastroMensalista(this).setVisible(true));
        btnRelatorio .addActionListener(e -> new TelaRelatorio(this).setVisible(true));
 
        menu.add(btnEntrada,    gbc);
        menu.add(btnSaida,      gbc);
        menu.add(btnMensalista, gbc);
        menu.add(btnRelatorio,  gbc);
 
        // rodape simples no sul da tela — em css seria position: absolute; bottom: 0;
        JLabel rodape = new JLabel("ARQDEOO · TSI 3 semestre", SwingConstants.CENTER);
        rodape.setFont(new Font("SansSerif", Font.BOLD, 12));
        rodape.setForeground(new Color(200, 200, 200));
        rodape.setBorder(new EmptyBorder(0, 0, 20, 0));
 
        // BorderLayout divide o painel em 5 regioes: NORTH, SOUTH, EAST, WEST, CENTER
        // em css seria display: flex; flex-direction: column; com justify-content nos filhos
        raiz.add(cabecalho, BorderLayout.NORTH);
        raiz.add(menu,      BorderLayout.CENTER);
        raiz.add(rodape,    BorderLayout.SOUTH);
        add(raiz);
    }
 
    /*
     * cria um botao arredondado transparente com contorno branco
     * uso uma subclasse anonima de JButton sobrescrevendo o paintComponent
     * em js seria criar um Web Component que estende HTMLButtonElement e reimplementa o render
     * o paintComponent e chamado automaticamente pelo swing toda vez que o botao precisa ser redesenhado
     */
    private JButton criarBotaoArredondado(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                // Graphics2D e a versao avancada do Graphics — igual ao ctx = canvas.getContext('2d') no js
                Graphics2D g2 = (Graphics2D) g.create();
 
                // antialiasing deixa as bordas arredondadas sem serrilhado
                // em css seria -webkit-font-smoothing: antialiased; mas pra formas geometricas
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
                // ButtonModel guarda o estado atual do botao (pressionado, hover, etc.)
                // em css seria os pseudo-seletores :active e :hover
                ButtonModel modelo = getModel();
 
                // estado :active — botao sendo clicado
                if (modelo.isPressed()) {
                    g2.setColor(new Color(255, 255, 255, 255)); // em css: border-color: rgba(255,255,255,1.0)
                    g2.setStroke(new BasicStroke(2.5f));        // em css: border-width: 2.5px
                    // RoundRectangle2D e um retangulo com cantos arredondados
                    // os dois ultimos args (18, 18) sao o raio do arredondamento — em css: border-radius: 18px
                    g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 18, 18));
                }
                // estado :hover — cursor em cima do botao
                else if (modelo.isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 140)); // em css: border-color: rgba(255,255,255,0.55)
                    g2.setStroke(new BasicStroke(1.5f));        // em css: border-width: 1.5px
                    g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 18, 18));
                }
 
                g2.dispose(); // libera o contexto grafico — boa pratica igual ao ctx.restore() no canvas
                super.paintComponent(g);
            }
        };
 
        btn.setFont(new Font("SansSerif", Font.BOLD, 14)); // em css: font-size: 14px; font-weight: bold;
        btn.setForeground(Color.WHITE);                    // em css: color: #ffffff;
        btn.setContentAreaFilled(false); // remove o fundo cinza padrao — em css: background: none;
        btn.setOpaque(false);            // fundo transparente
        btn.setFocusPainted(false);      // remove a borda pontilhada de foco do teclado
        btn.setBorderPainted(false);     // remove a borda padrao do swing
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // em css: cursor: pointer;
        btn.setPreferredSize(new Dimension(280, 48)); // em css: width: 280px; height: 48px;
 
        return btn;
    }
 
    // getter pra as outras telas saberem o nome do arquivo .ser — usado no s06
    public String getArquivo() { return ARQUIVO; }

}
