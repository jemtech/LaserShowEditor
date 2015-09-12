package de.jemtech.lse.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.jemtech.lse.data.ilda.Frame;
import de.jemtech.lse.data.ilda.ILDAImageDataTransferFormat;

public class Menue extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7061828939247467895L;
	JLabel selectedFrameLabel;
	public Menue(){
		super("Menue");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel pane = new JPanel(new GridBagLayout());
        setContentPane(pane);
        GridBagConstraints c = new GridBagConstraints();
        
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
        c.gridwidth = 2;
        pane.add(loadButton,c);
        
        selectedFrameLabel = new JLabel();
        setSelectetFrame(1);
        c.gridx = 0;
        c.gridwidth = 2;
        pane.add(selectedFrameLabel,c);
        
        JButton prevFrameButton = new JButton("<-");
        prevFrameButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setSelectetFrame(selectedFrame - 1);
			}
		});
        c.gridx = 0;
        c.gridwidth = 1;
        pane.add(prevFrameButton,c);
        
        JButton nextFrameButton = new JButton("->");
        nextFrameButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setSelectetFrame(selectedFrame + 1);
				
			}
		});
        c.gridx = 1;
        c.gridy = 2;
        pane.add(nextFrameButton,c);

        JButton playButton = new JButton(">");
        playButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(autoplay){
					autoplay = false;
					((JButton)arg0.getSource()).setText(">");
				}else{
					autoplay();
					((JButton)arg0.getSource()).setText("||");
				}
			}
		});
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        pane.add(playButton,c);
        
        pack();
		setVisible(true);
	}
	
	public void setSelectetFrame(int nr){
		if(lsFrames == null || lsFrames.isEmpty()){
			selectedFrame = 0;
	        selectedFrameLabel.setText("Frame: no Data");
			lsFramedisplay.setFrame(null);
			return;
		}
		int frameCount = lsFrames.size();
		if(nr < 1){
			nr = frameCount;
		}
		if(nr > frameCount){
			nr = 1;
		}
		selectedFrame = nr;
        selectedFrameLabel.setText("Frame: " + selectedFrame + "/" + frameCount);
		lsFramedisplay.setFrame(lsFrames.get(selectedFrame - 1));
	}
	
	int selectedFrame = 0;
	LSDisplay lsFramedisplay = new LSDisplay();
	List<Frame> lsFrames = null;
	private void loadFile(String fileName){
		ILDAImageDataTransferFormat format = new ILDAImageDataTransferFormat();
		format.addLoadingListener(new ILDAImageDataTransferFormat.LoadingListener() {
			
			@Override
			public void newFrameLoaded(Frame frame) {
				lsFramedisplay.setFrame(frame);
			}
		});
		try {
			lsFrames = format.parse(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setSelectetFrame(1);
	}
	
	boolean autoplay = false;
	public void autoplay(){
		if(autoplay){
			return;
		}
		(new Thread(new AutoplayThread())).start();
	}
	
	public class AutoplayThread implements Runnable{

		@Override
		public void run() {
			autoplay = true;
			while(autoplay){
				setSelectetFrame(selectedFrame + 1);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
