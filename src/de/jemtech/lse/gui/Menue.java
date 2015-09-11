package de.jemtech.lse.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.jemtech.lse.data.ilda.Frame;
import de.jemtech.lse.data.ilda.ILDAImageDataTransferFormat;

public class Menue extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7061828939247467895L;
	
	public Menue(){
		super("Menue");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton loadButton = new JButton("load file");
        loadButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter("ILDA",
			        "ild");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(null);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	loadFile(chooser.getSelectedFile().getAbsolutePath());
			    }
				
			}
		});
        getContentPane().add(loadButton);
        pack();
		setVisible(true);
	}
	

	LSDisplay lsFramedisplay = new LSDisplay();
	List<Frame> lsFrames = null;
	private void loadFile(String fileName){
		ILDAImageDataTransferFormat format = new ILDAImageDataTransferFormat();
		try {
			lsFramedisplay.setFrame(format.parse(fileName).get(0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
