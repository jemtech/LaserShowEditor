package de.jemtech.lse.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.jemtech.lse.data.ilda.Coordinate;
import de.jemtech.lse.data.ilda.Frame;

public class LSDisplay extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1744667576778214950L;
	
	private Frame frameToDisplay;
	private LSDisplayPanel lsdp;
	
	public void setFrame(Frame frameToDisplay){
		this.frameToDisplay = frameToDisplay;
		this.repaint();
	}
	
	public LSDisplay(){
		super("Laser Show Display");
		setLayout(null);

		lsdp = new LSDisplayPanel();
        setContentPane(lsdp);
        
		setSize(800, 800);
		setVisible(true);
	}
	
	private class LSDisplayPanel extends JPanel{
        /**
		 * 
		 */
		private static final long serialVersionUID = -9018863880299055900L;

		public void paintComponent(Graphics g){
			//black backround
			g.clearRect(0, 0, 800, 800);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 800, 800);
			paintFrame(g);
		}
	}
	
	final static float dash1[] = {3.0f};
    static Stroke dashedStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f, dash1, 0.0f);
    static Stroke normalStroke = new BasicStroke();
	private void paintFrame(Graphics g){
		if(frameToDisplay == null){
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
        
		List<Coordinate> coordinates = frameToDisplay.getCoordinates();
		Coordinate lastCoordinate = null;
		for(Coordinate coordinate : coordinates){
			if(lastCoordinate != null){
				if(coordinate.isBlank()){
					g2d.setColor(Color.LIGHT_GRAY);
			        g2d.setStroke(dashedStroke);
				}else{
					g2d.setColor(new Color(coordinate.getRed(), coordinate.getGreen(), coordinate.getBlue()));
			        g2d.setStroke(normalStroke);
				}
				g2d.drawLine(tICToPOD(lastCoordinate.getX()), tICToPOD(lastCoordinate.getY()), tICToPOD(coordinate.getX()), tICToPOD(coordinate.getY()));
			}
			lastCoordinate = coordinate;
		}
	}
	float zoom = 800.0f / 65536.0f;
	/**
	 * translate ILDA coordinate to point on display
	 * @param coordinate
	 * @return
	 */
	private int tICToPOD(int coordinate){
		return 800 - (int) ((coordinate + 32768) * zoom);
	}
	
}
