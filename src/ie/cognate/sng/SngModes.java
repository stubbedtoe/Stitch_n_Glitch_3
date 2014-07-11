package ie.cognate.sng;

import ie.cognate.Modes;

public interface SngModes extends Modes {

	public final int ACTUAL = 7;
	//to determine export options using prime numbers 
	public final int CSV = 2;
	public final int PDF = 3;
	public final int TIFF = 5;
	public final int SINGLE_BW =7;
	public final int MULTI_BW = 11;
	public final int SINGLE_COL = 13;
	public final int MULTI_COL = 17;
	//for messages - indices to a string array
	public final int COLORS = 0;
	public final int DITHER = 1;
	public final int GLITCH = 2;
	public final int BRANDS = 3;
}
