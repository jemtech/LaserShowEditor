package de.jemtech.lse.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
	public static final int EDIT_MODE_IDLE = 0;
	public static final int EDIT_MODE_ADD = 1;
	public static final int EDIT_MODE_MOVE = 2;
	public static final int EDIT_MODE_DELETE = 3;
	public static final int EDIT_MODE_COLOR = 4;
	
	private Color color = Color.BLACK;
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		if(pointUnderChange != null){
			pointUnderChange.setColor(getColor());
		}
	}

	private int pointSize = 4;
	
	private int xWidth = 800;
	private int yWidth = 800;
	
	private Frame frameToDisplay;
	private LSDisplayPanel lsdp;
	private int editMode = EDIT_MODE_IDLE;
	
	private int xBackup;
	private int yBackup;
	public void setEditMode(int mode){
		if(pointUnderChange != null){
			if(editMode == EDIT_MODE_ADD){
				pointUnderChange = null;
			}
			if(editMode == EDIT_MODE_MOVE){
				pointUnderChange.setX(xBackup);
				pointUnderChange.setY(yBackup);
				pointUnderChange = null;
			}
		}
		editMode = mode;
		if(editMode == EDIT_MODE_ADD){
			pointUnderChange = new Coordinate();
			pointUnderChange.setColor(getColor());
		}
		repaint();
	}
	
	public int getEditMode(){
		return editMode;
	}
	
	public void setFrame(Frame frameToDisplay){
		this.frameToDisplay = frameToDisplay;
		this.repaint();
	}
	
	public LSDisplay(){
		super("Laser Show Display");
		setLayout(null);

		lsdp = new LSDisplayPanel();
        setContentPane(lsdp);
        
		setSize(xWidth, yWidth);
		setVisible(true);
		
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				switch(arg0.getKeyCode()){
				case KeyEvent.VK_ESCAPE:{
					setEditMode(EDIT_MODE_IDLE);
					break;
				}
				case KeyEvent.VK_UP:{
					moveUp();
					break;
				}
				case KeyEvent.VK_DOWN:{
					moveDown();
					break;
				}
				case KeyEvent.VK_LEFT:{
					moveLeft();
					break;
				}
				case KeyEvent.VK_RIGHT:{
					moveRight();
					break;
				}
				case KeyEvent.VK_PLUS:{
					setZoom(getZoom() * 1.1f);
					break;
				}
				case KeyEvent.VK_MINUS:{
					setZoom(getZoom() * 0.9f);
					break;
				}
				}//switch end
			}
		});
	}
	
	private int podToIldaX(int x){
		return (int) ((xWidth/2 - x) / zoom) - xCenter;
	}
	
	private int podToIldaY(int y){
		return (int) ((yWidth/2 - y) / zoom) - yCenter;
	}
	
	private Coordinate nextToPod(int x, int y){
		int xMin = (int) ((xWidth/2 - x - (0.5 + pointSize)) / zoom) - xCenter;
		int xMax = (int) ((xWidth/2 - x + (0.5 + pointSize)) / zoom) - xCenter;
		int yMin = (int) ((yWidth/2 - y - (0.5 + pointSize)) / zoom) - yCenter;
		int yMax = (int) ((yWidth/2 - y + (0.5 + pointSize)) / zoom) - yCenter;
		for(Coordinate coordinate : frameToDisplay.getCoordinates()){
			if(xMin < coordinate.getX() && xMax > coordinate.getX() && yMin < coordinate.getY() && yMax > coordinate.getY()){
				return coordinate;
			}
		}
		return null;
	}
	
	void deletePoint(Coordinate delete){
		if(delete == null){
			return;
		}
		frameToDisplay.getCoordinates().remove(delete);
		this.repaint();
	}
	
	Coordinate pointUnderChange = null;
	private class LSDisplayPanel extends JPanel{
        /**
		 * 
		 */
		private static final long serialVersionUID = -9018863880299055900L;
		
		public LSDisplayPanel(){
			addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					if(pointUnderChange != null){
						pointUnderChange.setX(podToIldaX(e.getX()));
						pointUnderChange.setY(podToIldaY(e.getY()));
						if(editMode == EDIT_MODE_MOVE){
							pointUnderChange = null;
						}else if(editMode == EDIT_MODE_ADD){
							frameToDisplay.getCoordinates().add(pointUnderChange);
							pointUnderChange = new Coordinate();
							pointUnderChange.setColor(getColor());
						}
					}else{
						if(editMode == EDIT_MODE_MOVE){
							pointUnderChange = nextToPod(e.getX(), e.getY());
						}
					}
					if(editMode == EDIT_MODE_DELETE){
						deletePoint(nextToPod(e.getX(), e.getY()));
					}
					if(editMode == EDIT_MODE_COLOR){
						Coordinate coordinate = nextToPod(e.getX(), e.getY());
						if(coordinate != null){
							coordinate.setColor(getColor());
							repaint();
						}
					}
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				
				@Override
				public void mouseClicked(MouseEvent arg0) {
				}
			});
			addMouseMotionListener(new MouseMotionListener() {
				
				@Override
				public void mouseMoved(MouseEvent e) {
					if(pointUnderChange != null){
						pointUnderChange.setX(podToIldaX(e.getX()));
						pointUnderChange.setY(podToIldaY(e.getY()));
						repaint();
					}
				}
				
				@Override
				public void mouseDragged(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}
			});
		}

		public void paintComponent(Graphics g){
			//black backround
			g.clearRect(0, 0, xWidth, yWidth);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, xWidth, yWidth);
			paintFrame(g);
		}
	}
	private int fog = 25;
	
	public int getFog() {
		return fog;
	}

	public void setFog(int fog) {
		if(fog > 255){
			fog = 255;
		}
		if(fog < 0){
			fog = 0;
		}
		this.fog = fog;
		this.repaint();
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
		int x1 = Integer.MIN_VALUE;
		int y1 = Integer.MIN_VALUE;
		for(Coordinate coordinate : coordinates){
			int x2 = tICToPOD(coordinate.getX()+xCenter);
			int y2 = tICToPOD(coordinate.getY()+yCenter);
			if(x1 > Integer.MIN_VALUE && y1  > Integer.MIN_VALUE){
				if(coordinate.isBlank()){
					g2d.setColor(Color.LIGHT_GRAY);
			        g2d.setStroke(dashedStroke);
				}else{
			        g2d.setStroke(normalStroke);
			        if(fog > 0){
				        g2d.setColor(new Color(coordinate.getRed(), coordinate.getGreen(), coordinate.getBlue(), fog));
				        int [ ] x = {tICToPOD(xCenter), x1, x2};
				        int [ ] y = {tICToPOD(yCenter), y1, y2};
				        g.fillPolygon(x, y, 3);
			        }
					g2d.setColor(new Color(coordinate.getRed(), coordinate.getGreen(), coordinate.getBlue()));
				}
				g2d.drawLine(x1, y1, x2, y2);
				if(editMode == EDIT_MODE_DELETE || editMode == EDIT_MODE_MOVE || editMode == EDIT_MODE_COLOR){
					g2d.fillRect(x2-pointSize, y2-pointSize, pointSize *2, pointSize * 2);
				}
			}
			x1 = x2;
			y1 = y2;
		}
		if(editMode == EDIT_MODE_ADD && x1 > Integer.MIN_VALUE && y1  > Integer.MIN_VALUE){
			Coordinate coordinate = pointUnderChange;
			int x2 = tICToPOD(coordinate.getX()+xCenter);
			int y2 = tICToPOD(coordinate.getY()+yCenter);
			if(coordinate.isBlank()){
				g2d.setColor(Color.LIGHT_GRAY);
		        g2d.setStroke(dashedStroke);
			}else{
		        g2d.setStroke(normalStroke);
		        if(fog > 0){
			        g2d.setColor(new Color(coordinate.getRed(), coordinate.getGreen(), coordinate.getBlue(), fog));
			        int [ ] x = {tICToPOD(xCenter), x1, x2};
			        int [ ] y = {tICToPOD(yCenter), y1, y2};
			        g.fillPolygon(x, y, 3);
		        }
				g2d.setColor(new Color(coordinate.getRed(), coordinate.getGreen(), coordinate.getBlue()));
			}
			g2d.drawLine(x1, y1, x2, y2);
		}
	}
	
	public float getZoom(){
		return zoom;
	}
	
	public void setZoom(float zoom){
		if(zoom <= 0.0f){
			System.err.println("zoom can't be <= 0");
		}
		this.zoom = zoom;
		this.repaint();
	}
	private int xCenter = 0;
	private int yCenter = 0;
	
	public void moveRight(){
		xCenter += 10 * (1/zoom) + 0.5;
		this.repaint();
	}
	
	public void moveLeft(){
		xCenter -= 10 * (1/zoom) + 0.5;
		this.repaint();
	}
	
	public void moveUp(){
		yCenter -= 10 * (1/zoom) + 0.5;
		this.repaint();
	}
	
	public void moveDown(){
		yCenter += 10 * (1/zoom) + 0.5;
		this.repaint();
	}
	
	private float zoom = xWidth / 65536.0f;
	/**
	 * translate ILDA coordinate to point on display
	 * @param coordinate
	 * @return
	 */
	private int tICToPOD(int coordinate){
		return xWidth/2 - (int) (coordinate * zoom);
	}
	
}
