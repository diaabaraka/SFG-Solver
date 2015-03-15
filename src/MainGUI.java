import java.awt.GridLayout;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class MainGUI extends JFrame {
	ArrayList<String> [] edges;
	
	public MainGUI(){
		setLocation(150, 150);
		setSize(1000, 500);
		setVisible(true);
		setTitle("SFG Solver");
		
		
		JPanel edgeJPanel = new JPanel();
		edgeJPanel.setLayout(new GridLayout(1, 3));
		JTextField fromField,toField,gainField;
		fromField = new JTextField();
		toField = new JTextField();
		gainField = new JTextField();
		
		JButton enterEdge = new JButton("Add This Edge");
		
		edgeJPanel.add(fromField);edgeJPanel.add(toField);edgeJPanel.add(gainField);
		edgeJPanel.add(enterEdge);
		add(edgeJPanel);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	public static void main(String[] args) {
		new MainGUI();
	}
	

	
}
