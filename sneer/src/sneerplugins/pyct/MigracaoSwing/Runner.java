package sneerplugins.pyct.MigracaoSwing;

import java.io.IOException;

import javax.swing.JFrame;


public class Runner {

	
	public static void main(String[] args) throws NumberFormatException, IOException {
	
		Navegacao executa = new Navegacao();
		executa.Navegacao();
		executa.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
	}

}
