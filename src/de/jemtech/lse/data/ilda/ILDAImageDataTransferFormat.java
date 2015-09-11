package de.jemtech.lse.data.ilda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class ILDAImageDataTransferFormat {

	static int PARSE_STATE_SEARCHING_HEADER = -1; //Starts with (7 Bytes): I,L,D,A,0x00,0x00,0x00
	static int PARSE_STATE_PARSING_HEADER = 0;
	
	static char ILDA_3D_COORD_HEADER_TYPE = 0x00;
	static char ILDA_2D_COORD_HEADER_TYPE = 0x01;
	static char ILDA_COLOUR_PALETTE_HEADER_TYPE = 0x02;
	static char ILDA_3D_COORD_TRUE_COL_HEADER_TYPE = 0x04;
	static char ILDA_2D_COORD_TRUE_COL_HEADER_TYPE = 0x05;
	static char HEADER_START[] = {'I','L','D','A',0x00,0x00,0x00};
	
	public List<Frame> parse(String path) throws IOException{
		File file = new File(path);
		if(!file.exists()){
			System.err.println("File not existing: " + path);
			return null;
		}
		if(!file.canRead()){
			System.err.println("Can't read file: " + path);
			return null;
		}
		FileInputStream fileInputStream = new FileInputStream(file);
		return parse(fileInputStream);
	}
	
	public List<Frame> parse(InputStream in) throws IOException{
		return readILDAHeader(in);
	}
	
	private int readTwoByteInt(InputStream in) throws IOException{
		int ch = in.read();
		short i = (short)((ch & 0xff) << 8);
		ch = in.read();
		i |= ch & 0xff;
		return i;
	}
	
	private int colourTable[][] = null;
	private void readRGBByColourIndex(InputStream in, Coordinate coordinate) throws IOException{
		int ch = in.read();
		if(colourTable == null ){
			// no colour Tabel use default
			coordinate.setRed(ILDA_DEFAULT_COLOUR_PALETTE[ch][0]);
			coordinate.setGreen(ILDA_DEFAULT_COLOUR_PALETTE[ch][1]);
			coordinate.setBlue(ILDA_DEFAULT_COLOUR_PALETTE[ch][2]);
		}else{
			coordinate.setRed(colourTable[ch][0]);
			coordinate.setGreen(colourTable[ch][1]);
			coordinate.setBlue(colourTable[ch][2]);
		}
	}
	
	/**
		0 to 3 	Signature ("ILDA") 	This makes a positive identification that this is the start of a header section, it should be the ASCII characters "ILDA". It can also be used to quickly identify a file as a valid ILDA file since the first section of such a file must be a header section.
		4 to 6 	Not used 	Supposed to be set to 0
		7 	Format type 	This indicates the type of data section that will follow the header. This must be either 0 (3D coordinate section), 1 (2D coordinate section), or 2 (colour palette section). The structure of these is described in more detail later.
		8 to 15 	Name 	This 8 byte string indicates the name (if there is one) of the current frame for format type 0 and 1, and the palette name for format type 2. If no name is appropriate then all the bytes should be set to 0.
		16 to 23 	Company name 	The name of the company who created the frame or palette. If no name is appropriate then all the bytes should be set to 0.
		24 and 25 	Total number of entries in data section 	For coordinates this is the number of points in the following data section, for colour palettes it is the number of entries in the palette. Using this along with the known size for the data section entries allows a parsing program to skip over sections they weren't interested.
		26 and 27 	Current frame number 	For files that contain a number of frames, eg: library of graphical shapes, collection of colour palettes, or an animation sequence, this is the current number. It ranges from 0 up to one the total number of frames minus 1.
		28 and 29 	Total number of frames 	The total number of frames and is not used for colour palette format types. This is set to 0 in a "null header" to indicate the end of the ILDA file.
		30 	Scanner head 	Used for systems with multiple scanners or heads, otherwise set to 0 for the default device.
		31 	Not used 	Supposed to be set to
	 * @throws IOException 
	*/
	public List<Frame> readILDAHeader(InputStream in) throws IOException{
		if( in == null ){
			System.err.println("no stream");
			return null;
		}
		List<Frame> frames = new LinkedList<Frame>();
		int byteNr = 0;
		int chTmp;
		while( ( chTmp = in.read() ) != -1){
			char ch = (char) chTmp;//actual byte
			if(ch == HEADER_START[byteNr]){
				//match go next
				byteNr++;
				if(byteNr == 7){
					//header identified start parsing heder info
					int dataType;
					String name = "";
					String companyName = "";
					int numberOfDataEntries;
					int number;
					int totalNumber;
					int scannerHead;
					
					dataType = in.read();
					Frame frame = new Frame();
					for(int i = 0; i < 8; i++){
						name += (char) in.read();
					}
					for(int i = 0; i < 8; i++){
						companyName += (char) in.read();
					}
					numberOfDataEntries = 256*in.read();
					numberOfDataEntries += in.read();
					number = 256*in.read();
					number += in.read();
					totalNumber = 256*in.read();
					totalNumber += in.read();
					scannerHead = in.read();
					in.read();//last (not used) header byte: Data beginns
					if(dataType == ILDA_3D_COORD_HEADER_TYPE){
						parse3DCoordData(in, numberOfDataEntries, frame);
					}else if(dataType == ILDA_2D_COORD_HEADER_TYPE){
						parse2DCoordData(in, numberOfDataEntries, frame);
					}else if(dataType == ILDA_COLOUR_PALETTE_HEADER_TYPE){
						parseColourPaletteData(in, numberOfDataEntries);
						frame = null; //is not a real frame
					}else if(dataType == ILDA_3D_COORD_TRUE_COL_HEADER_TYPE){
						parse3DCoordTrueColData(in, numberOfDataEntries, frame);
					}else if(dataType == ILDA_2D_COORD_TRUE_COL_HEADER_TYPE){
						parse2DCoordTrueColData(in, numberOfDataEntries, frame);
					}else{
						System.err.println("Ignorring unknowm Data Type: " + (int) dataType + "\n");
						frame = null;
					}
					if(frame != null){
						frames.add(frame);
					}
					//data is parsed start again
					byteNr = 0;
				}
			}else{
				//no match
				byteNr = 0;
			}
		}
		return frames;
	}
	

	/**
		0-1 	X coordinate 	A 16-bit binary twos complement (signed) number. Extreme left is -32768; extreme right is +32767. (All directions stated using front projection.)
		
		2-3 	Y coordinate 	A 16-bit binary twos complement (signed) number. Extreme bottom is -32768; extreme top is +32767.
		4-5 	Z coordinate 	A 16-bit binary twos complement (signed) number. Extreme rear (away from viewer; behind screen) is -32768; extreme front (towards viewer; in front of screen) is +32767.
		6 	Status code (MSB 0)
				Bit 0 is the "last point" bit. This bit is set to 0 for all points except the last point. A 1 indicates end of image data. This was done for compatibility with certain existing systems; note that a zero in bytes 25-26 (Total Points) is the official end-of-file indication.
				Bit 1 is the blanking bit. If this is a 0, then the laser is on (draw). If this is a 1, then the laser is off (blank). Note that all systems must write this bit, even if a particular system uses only bits 0-7 for blanking/colour information.
				Bits 2-7 are unassigned and should be set to 0 (reserved).
		7	ColourIndex
				0-255 indicate the point's colour number. This value is used as an index into a colour lookup table containing red, green and blue values. See ILDA Colour Lookup Table Header section for more information.
	 * @throws IOException 
	*/
	private void parse3DCoordData(InputStream in, int numberEntries, Frame frame) throws IOException{
	
		for(int i = 0; i<numberEntries; i++){
			Coordinate coordinate = new Coordinate();
			coordinate.setX(readTwoByteInt(in));
			coordinate.setY(readTwoByteInt(in));
			coordinate.setZ(readTwoByteInt(in));
			StatusByte status = readStatusByte(in);
			readRGBByColourIndex(in, coordinate);
			if(status.blanking){
				coordinate.setRed(0);
				coordinate.setGreen(0);
				coordinate.setBlue(0);
			}
			frame.getCoordinates().add(coordinate);
		}
	}

	/**
		0-1 	X coordinate
		2-3 	Y coordinate
		4 	Status code
		5	ColourIndex
	 * @throws IOException 
	*/
	private void parse2DCoordData(InputStream in, int numberEntries, Frame frame) throws IOException{
		for(int i = 0; i<numberEntries; i++){
			Coordinate coordinate = new Coordinate();
			coordinate.setX(readTwoByteInt(in));
			coordinate.setY(readTwoByteInt(in));
			coordinate.setZ(0);
			StatusByte status = readStatusByte(in);
			readRGBByColourIndex(in, coordinate);
			if(status.blanking){
				coordinate.setRed(0);
				coordinate.setGreen(0);
				coordinate.setBlue(0);
			}
			frame.getCoordinates().add(coordinate);
		}
	}


	/**
		0 	Red value 	Intensity value of red. Value ranges from 0 (off) to 255 (full on).
		1 	Green value 	Intensity value of green.
		2 	Blue value 	Intensity value of blue.
	 * @throws IOException 
	*/
	private void parseColourPaletteData(InputStream in, int numberEntries) throws IOException{
		int colourTable[][] = new int[numberEntries][3];
		for(int i = 0; i<numberEntries; i++){
			colourTable[i][0] = in.read();
			colourTable[i][1] = in.read();
			colourTable[i][2] = in.read();
		}
		this.colourTable = colourTable;
	}

	/**
		0-1 	X coordinate
		2-3 	Y coordinate
		4-5 	z coordinate
		6 	Status code
		7	blue
				This value is the point’s blue color component.  A value of 0 indicates “zero brightness”
	and a value of 255 indicates “maximum brightness”.
		8	green
				This value is the point’s green color component. A value of 0 indicates “zero brightness”
	and a value of 255 indicates “maximum brightness”.
		9	red
				This value is the point’s red color component.  A value of 0 indicates “zero brightness”
	and a value of 255 indicates “maximum brightness”.
	 * @throws IOException 
	*/
	private void parse3DCoordTrueColData(InputStream in, int numberEntries, Frame frame) throws IOException{
	
		for(int i = 0; i<numberEntries; i++){
			Coordinate coordinate = new Coordinate();
			coordinate.setX(readTwoByteInt(in));
			coordinate.setY(readTwoByteInt(in));
			coordinate.setZ(readTwoByteInt(in));
			StatusByte status = readStatusByte(in);
			coordinate.setBlue(in.read());
			coordinate.setGreen(in.read());
			coordinate.setRed(in.read());
			if(status.blanking){
				coordinate.setRed(0);
				coordinate.setGreen(0);
				coordinate.setBlue(0);
			}
			frame.getCoordinates().add(coordinate);
		}
	}

	/**
		0-1 	X coordinate
		2-3 	Y coordinate
		4 	Status code
		5	blue
		6	green
		7	red
	 * @throws IOException 
	*/
	private void parse2DCoordTrueColData(InputStream in, int numberEntries, Frame frame) throws IOException{
		for(int i = 0; i<numberEntries; i++){
			Coordinate coordinate = new Coordinate();
			coordinate.setX(readTwoByteInt(in));
			coordinate.setY(readTwoByteInt(in));
			coordinate.setZ(0);
			StatusByte status = readStatusByte(in);
			coordinate.setBlue(in.read());
			coordinate.setGreen(in.read());
			coordinate.setRed(in.read());
			if(status.blanking){
				coordinate.setRed(0);
				coordinate.setGreen(0);
				coordinate.setBlue(0);
			}
			frame.getCoordinates().add(coordinate);
		}
	}
	
	/**
	MSB 0
		Bit 0 is the "last point" bit. This bit is set to 0 for all points except the last point. A 1 indicates end of image data. This was done for compatibility with certain existing systems; note that a zero in bytes 25-26 (Total Points) is the official end-of-file indication.
		Bit 1 is the blanking bit. If this is a 0, then the laser is on (draw). If this is a 1, then the laser is off (blank). Note that all systems must write this bit, even if a particular system uses only bits 0-7 for blanking/colour information.
		Bits 2-7 are unassigned and should be set to 0 (reserved).
	 * @throws IOException 
	*/
	private StatusByte readStatusByte(InputStream in) throws IOException{
		int ch = in.read();
		StatusByte status = new StatusByte();
		status.lastEntry = (((ch & 0x80) >> 7) == 1);
		status.blanking = (((ch & 0x40) >> 6) == 1);
		return status;
	}
	
	private class StatusByte{
		boolean blanking;
		boolean lastEntry;
	}
	
	// ilda standard color palette (r,g,b)
	static int ILDA_DEFAULT_COLOUR_PALETTE[][]={
	{ 0, 0, 0 }, // Black/blanked (fixed)
	{ 255, 255, 255 }, // White (fixed)
	{ 255, 0, 0 }, // Red (fixed)
	{ 255, 255, 0 }, // Yellow (fixed)
	{ 0, 255, 0 }, // Green (fixed)
	{ 0, 255, 255 }, // Cyan (fixed)
	{ 0, 0, 255 }, // Blue (fixed)
	{ 255, 0, 255 }, // Magenta (fixed)
	{ 255, 128, 128 }, // Light red
	{ 255, 140, 128 },
	{ 255, 151, 128 },
	{ 255, 163, 128 },
	{ 255, 174, 128 },
	{ 255, 186, 128 },
	{ 255, 197, 128 },
	{ 255, 209, 128 },
	{ 255, 220, 128 },
	{ 255, 232, 128 },
	{ 255, 243, 128 },
	{ 255, 255, 128 }, // Light yellow
	{ 243, 255, 128 },
	{ 232, 255, 128 },
	{ 220, 255, 128 },
	{ 209, 255, 128 },
	{ 197, 255, 128 },
	{ 186, 255, 128 },
	{ 174, 255, 128 },
	{ 163, 255, 128 },
	{ 151, 255, 128 },
	{ 140, 255, 128 },
	{ 128, 255, 128 }, // Light green
	{ 128, 255, 140 },
	{ 128, 255, 151 },
	{ 128, 255, 163 },
	{ 128, 255, 174 },
	{ 128, 255, 186 },
	{ 128, 255, 197 },
	{ 128, 255, 209 },
	{ 128, 255, 220 },
	{ 128, 255, 232 },
	{ 128, 255, 243 },
	{ 128, 255, 255 }, // Light cyan
	{ 128, 243, 255 },
	{ 128, 232, 255 },
	{ 128, 220, 255 },
	{ 128, 209, 255 },
	{ 128, 197, 255 },
	{ 128, 186, 255 },
	{ 128, 174, 255 },
	{ 128, 163, 255 },
	{ 128, 151, 255 },
	{ 128, 140, 255 },
	{ 128, 128, 255 }, // Light blue
	{ 140, 128, 255 },
	{ 151, 128, 255 },
	{ 163, 128, 255 },
	{ 174, 128, 255 },
	{ 186, 128, 255 },
	{ 197, 128, 255 },
	{ 209, 128, 255 },
	{ 220, 128, 255 },
	{ 232, 128, 255 },
	{ 243, 128, 255 },
	{ 255, 128, 255 }, // Light magenta
	{ 255, 128, 243 },
	{ 255, 128, 232 },
	{ 255, 128, 220 },
	{ 255, 128, 209 },
	{ 255, 128, 197 },
	{ 255, 128, 186 },
	{ 255, 128, 174 },
	{ 255, 128, 163 },
	{ 255, 128, 151 },
	{ 255, 128, 140 },
	{ 255, 0, 0 }, // Red (cycleable)
	{ 255, 23, 0 },
	{ 255, 46, 0 },
	{ 255, 70, 0 },
	{ 255, 93, 0 },
	{ 255, 116, 0 },
	{ 255, 139, 0 },
	{ 255, 162, 0 },
	{ 255, 185, 0 },
	{ 255, 209, 0 },
	{ 255, 232, 0 },
	{ 255, 255, 0 }, //Yellow (cycleable)
	{ 232, 255, 0 },
	{ 209, 255, 0 },
	{ 185, 255, 0 },
	{ 162, 255, 0 },
	{ 139, 255, 0 },
	{ 116, 255, 0 },
	{ 93, 255, 0 },
	{ 70, 255, 0 },
	{ 46, 255, 0 },
	{ 23, 255, 0 },
	{ 0, 255, 0 }, // Green (cycleable)
	{ 0, 255, 23 },
	{ 0, 255, 46 },
	{ 0, 255, 70 },
	{ 0, 255, 93 },
	{ 0, 255, 116 },
	{ 0, 255, 139 },
	{ 0, 255, 162 },
	{ 0, 255, 185 },
	{ 0, 255, 209 },
	{ 0, 255, 232 },
	{ 0, 255, 255 }, // Cyan (cycleable)
	{ 0, 232, 255 },
	{ 0, 209, 255 },
	{ 0, 185, 255 },
	{ 0, 162, 255 },
	{ 0, 139, 255 },
	{ 0, 116, 255 },
	{ 0, 93, 255 },
	{ 0, 70, 255 },
	{ 0, 46, 255 },
	{ 0, 23, 255 },
	{ 0, 0, 255 }, // Blue (cycleable)
	{ 23, 0, 255 },
	{ 46, 0, 255 },
	{ 70, 0, 255 },
	{ 93, 0, 255 },
	{ 116, 0, 255 },
	{ 139, 0, 255 },
	{ 162, 0, 255 },
	{ 185, 0, 255 },
	{ 209, 0, 255 },
	{ 232, 0, 255 },
	{ 255, 0, 255 }, // Magenta (cycleable)
	{ 255, 0, 232 },
	{ 255, 0, 209 },
	{ 255, 0, 185 },
	{ 255, 0, 162 },
	{ 255, 0, 139 },
	{ 255, 0, 116 },
	{ 255, 0, 93 },
	{ 255, 0, 70 },
	{ 255, 0, 46 },
	{ 255, 0, 23 },
	{ 128, 0, 0 }, // Dark red
	{ 128, 12, 0 },
	{ 128, 23, 0 },
	{ 128, 35, 0 },
	{ 128, 47, 0 },
	{ 128, 58, 0 },
	{ 128, 70, 0 },
	{ 128, 81, 0 },
	{ 128, 93, 0 },
	{ 128, 105, 0 },
	{ 128, 116, 0 },
	{ 128, 128, 0 }, // Dark yellow
	{ 116, 128, 0 },
	{ 105, 128, 0 },
	{ 93, 128, 0 },
	{ 81, 128, 0 },
	{ 70, 128, 0 },
	{ 58, 128, 0 },
	{ 47, 128, 0 },
	{ 35, 128, 0 },
	{ 23, 128, 0 },
	{ 12, 128, 0 },
	{ 0, 128, 0 }, // Dark green
	{ 0, 128, 12 },
	{ 0, 128, 23 },
	{ 0, 128, 35 },
	{ 0, 128, 47 },
	{ 0, 128, 58 },
	{ 0, 128, 70 },
	{ 0, 128, 81 },
	{ 0, 128, 93 },
	{ 0, 128, 105 },
	{ 0, 128, 116 },
	{ 0, 128, 128 }, // Dark cyan
	{ 0, 116, 128 },
	{ 0, 105, 128 },
	{ 0, 93, 128 },
	{ 0, 81, 128 },
	{ 0, 70, 128 },
	{ 0, 58, 128 },
	{ 0, 47, 128 },
	{ 0, 35, 128 },
	{ 0, 23, 128 },
	{ 0, 12, 128 },
	{ 0, 0, 128 }, // Dark blue
	{ 12, 0, 128 },
	{ 23, 0, 128 },
	{ 35, 0, 128 },
	{ 47, 0, 128 },
	{ 58, 0, 128 },
	{ 70, 0, 128 },
	{ 81, 0, 128 },
	{ 93, 0, 128 },
	{ 105, 0, 128 },
	{ 116, 0, 128 },
	{ 128, 0, 128 }, // Dark magenta
	{ 128, 0, 116 },
	{ 128, 0, 105 },
	{ 128, 0, 93 },
	{ 128, 0, 81 },
	{ 128, 0, 70 },
	{ 128, 0, 58 },
	{ 128, 0, 47 },
	{ 128, 0, 35 },
	{ 128, 0, 23 },
	{ 128, 0, 12 },
	{ 255, 192, 192 }, // Very light red
	{ 255, 64, 64 }, // Light-medium red
	{ 192, 0, 0 }, // Medium-dark red
	{ 64, 0, 0 }, // Very dark red
	{ 255, 255, 192 }, // Very light yellow
	{ 255, 255, 64 }, // Light-medium yellow
	{ 192, 192, 0 }, // Medium-dark yellow
	{ 64, 64, 0 }, // Very dark yellow
	{ 192, 255, 192 }, // Very light green
	{ 64, 255, 64 }, // Light-medium green
	{ 0, 192, 0 }, // Medium-dark green
	{ 0, 64, 0 }, // Very dark green
	{ 192, 255, 255 }, // Very light cyan
	{ 64, 255, 255 }, // Light-medium cyan
	{ 0, 192, 192 }, // Medium-dark cyan
	{ 0, 64, 64 }, // Very dark cyan
	{ 192, 192, 255 }, // Very light blue
	{ 64, 64, 255 }, // Light-medium blue
	{ 0, 0, 192 }, // Medium-dark blue
	{ 0, 0, 64 }, // Very dark blue
	{ 255, 192, 255 }, // Very light magenta
	{ 255, 64, 255 }, // Light-medium magenta
	{ 192, 0, 192 }, // Medium-dark magenta
	{ 64, 0, 64 }, // Very dark magenta
	{ 255, 96, 96 }, // Medium skin tone
	{ 255, 255, 255 }, // White (cycleable)
	{ 245, 245, 245 },
	{ 235, 235, 235 },
	{ 224, 224, 224 }, // Very light gray (7/8 intensity)
	{ 213, 213, 213 },
	{ 203, 203, 203 },
	{ 192, 192, 192 }, // Light gray (3/4 intensity)
	{ 181, 181, 181 },
	{ 171, 171, 171 },
	{ 160, 160, 160 }, // Medium-light gray (5/8 int.)
	{ 149, 149, 149 },
	{ 139, 139, 139 },
	{ 128, 128, 128 }, // Medium gray (1/2 intensity)
	{ 117, 117, 117 },
	{ 107, 107, 107 },
	{ 96, 96, 96 }, // Medium-dark gray (3/8 int.)
	{ 85, 85, 85 },
	{ 75, 75, 75 },
	{ 64, 64, 64 }, // Dark gray (1/4 intensity)
	{ 53, 53, 53 },
	{ 43, 43, 43 },
	{ 32, 32, 32 }, // Very dark gray (1/8 intensity)
	{ 21, 21, 21 },
	{ 11, 11, 11 },
	{ 0, 0, 0 } // Black
	};
}
