package de.jemtech.lse.gui;

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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        
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
        c.gridwidth = 1;
        pane.add(loadButton,c);
        
        JButton saveButton = new JButton("save file");
        saveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter("ILDA",
			        "ild");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(null);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	saveToFile(chooser.getSelectedFile().getAbsolutePath());
			    }
				
			}
		});
        c.gridx = 1;
        pane.add(saveButton,c);
        
        selectedFrameLabel = new JLabel();
        setSelectetFrame(1);
        c.gridx = 0;
        c.gridwidth = 2;
        pane.add(selectedFrameLabel,c);
        
        JButton prevFrameButton = new JButton("\u21d0");
        prevFrameButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setSelectetFrame(selectedFrame - 1);
			}
		});
        c.gridx = 0;
        c.gridwidth = 1;
        pane.add(prevFrameButton,c);
        
        JButton nextFrameButton = new JButton("\u21d2");
        nextFrameButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setSelectetFrame(selectedFrame + 1);
				
			}
		});
        c.gridx = 1;
        c.gridy = 2;
        pane.add(nextFrameButton,c);

        JButton playButton = new JButton("\u27a4");
        playButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(autoplay){
					autoplay = false;
					((JButton)arg0.getSource()).setText("\u27a4");
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
        
        JLabel zoomLabel = new JLabel("Zoom");
        setSelectetFrame(1);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        pane.add(zoomLabel,c);
        
        JButton zoomInButton = new JButton("+");
        zoomInButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lsFramedisplay.setZoom(lsFramedisplay.getZoom() * 1.1f);
			}
		});
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        pane.add(zoomInButton,c);
        
        JButton zoomOutButton = new JButton("-");
        zoomOutButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lsFramedisplay.setZoom(lsFramedisplay.getZoom() * 0.9f);
			}
		});
        c.gridx = 1;
        pane.add(zoomOutButton,c);
        
        JLabel navigationLabel = new JLabel("Navigation");
        setSelectetFrame(1);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        pane.add(navigationLabel,c);
        
        JButton centerUpButton = new JButton("\u2191");
        centerUpButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lsFramedisplay.moveUp();
			}
		});
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        pane.add(centerUpButton,c);

        JButton centerLeftButton = new JButton("\u2190");
        centerLeftButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lsFramedisplay.moveLeft();
			}
		});
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        pane.add(centerLeftButton,c);

        JButton centerRightButton = new JButton("\u2192");
        centerRightButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lsFramedisplay.moveRigth();
			}
		});
        c.gridx = 1;
        pane.add(centerRightButton,c);
        
        JButton centerDownButton = new JButton("\u2193");
        centerDownButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lsFramedisplay.moveDown();
			}
		});
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        pane.add(centerDownButton,c);
        
        JLabel fogLabel = new JLabel("Fog");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        pane.add(fogLabel,c);
        
        JSlider fogSlider = new JSlider(JSlider.HORIZONTAL,
                0, 100, 25);
        fogSlider.setValue(lsFramedisplay.getFog()*100/255);
        fogSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				lsFramedisplay.setFog(((JSlider)arg0.getSource()).getValue() * 255 / 100);
			}
		});
        fogSlider.setMajorTickSpacing(25);
        fogSlider.setMinorTickSpacing(10);
        fogSlider.setPaintTicks(true);
        fogSlider.setPaintLabels(true);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        pane.add(fogSlider,c);
        
        JLabel editLabel = new JLabel("Edit Point");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        pane.add(editLabel,c);
        
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lsFramedisplay.setEditMode(LSDisplay.EDIT_MODE_ADD);
			}
		});
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        pane.add(addButton,c);

        JButton moveButton = new JButton("Move");
        moveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lsFramedisplay.setEditMode(LSDisplay.EDIT_MODE_MOVE);
			}
		});
        c.gridx = 1;
        c.gridwidth = 1;
        pane.add(moveButton,c);
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lsFramedisplay.setEditMode(LSDisplay.EDIT_MODE_DELETE);
			}
		});
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        pane.add(deleteButton,c);
        
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
	
	private void saveToFile(String fileName){
		if(lsFrames == null){
			return;
		}
		ILDAImageDataTransferFormat format = new ILDAImageDataTransferFormat();
		try {
			format.write(fileName, lsFrames);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
