package de.jemtech.lse.data.ilda;

public class Coordinate {
	private int x;
	private int y;
	private int z;
	private int r;
	private int g;
	private int b;
	
	public boolean isBlank(){
		return r==0 && b == 0 && g==0;
	}
	
	public int getRed(){
		return r;
	}
	
	public int getGreen(){
		return g;
	}
	
	public int getBlue(){
		return b;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getZ(){
		return z;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setRed(int r) {
		this.r = r;
	}
	
	public void setGreen(int g) {
		this.g = g;
	}
	
	public void setBlue(int b) {
		this.b = b;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
}
