package parksys.ui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/*
 * essa classe estende JPanel para virar um painel que desenha uma imagem de fundo
 * em js seria tipo um div com background-image: url('fundo.jpg') no css
 * a diferenca e que no swing nao existe background-image nativo, entao preciso
 * sobrescrever o metodo paintComponent que e chamado automaticamente pelo swing
 * toda vez que a tela precisa ser redesenhada (equivalente ao repaint no canvas do js)
 */
public class PainelFundoImagem extends JPanel {

    // guarda a imagem carregada em memoria — em js seria como um new Image() no dom
    private Image imagem;

    /*
     * ClassLoader e o mecanismo que o java usa pra achar arquivos dentro do projeto
     * funciona igual ao import ou fetch de um arquivo estatico no js/webpack
     * uso getContextClassLoader() pra pegar o loader da thread atual (padrao seguro)
     * classLoader.getResource() procura o arquivo pelo nome dentro do classpath
     * que e a pasta onde o java acha os .class e recursos — equivalente ao /public no react
     */
    public PainelFundoImagem(String nomeArquivo) {
        // chama o construtor do pai (JPanel) ja passando o layout
        // em js seria tipo super() no constructor de uma classe que estende outra
        super(new BorderLayout());

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL urlImagem = classLoader.getResource(nomeArquivo);

        if (urlImagem != null) {
            // ImageIcon e o jeito do swing de carregar imagem a partir de uma URL
            // .getImage() pega o objeto Image de dentro do ImageIcon
            // em js seria: const img = new Image(); img.src = url;
            this.imagem = new ImageIcon(urlImagem).getImage();
        } else {
            // arquivo nao encontrado — loga no console igual ao console.warn() do js
            System.out.println("aviso: imagem '" + nomeArquivo + "' nao encontrada no classpath");
        }
    }

    /*
     * paintComponent e o metodo que o swing chama toda vez que precisa redesenhar esse painel
     * equivalente ao metodo draw() de um canvas no html5 ou ao render() de um componente react
     * o @Override avisa que estou substituindo o comportamento padrao do JPanel
     * super.paintComponent(g) chama o metodo original do pai primeiro (limpa o fundo)
     * depois disso desenho a imagem por cima ocupando o tamanho total do painel
     * getWidth() e getHeight() dao o tamanho atual do painel — em css seria 100% width e height
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imagem != null) {
            // g.drawImage estica a imagem pra cobrir o painel inteiro
            // o ultimo arg 'this' e o ImageObserver — avisa quando a imagem termina de carregar
            g.drawImage(imagem, 0, 0, getWidth(), getHeight(), this);
        }
    }
}