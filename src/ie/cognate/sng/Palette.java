package ie.cognate.sng;

import ie.cognate.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
//import java.util.Arrays;
import java.util.Comparator;

import processing.core.PImage;

/**
 * Stores Flosses in a HashMap internally using the theoretical colour value as the key (or actual 
 *  colour value in the case of a RestrictedFloss). This HashMap is created in all constructors.\n\n
 *  
 *  Palettes are constructed before adjustments such as dithering take place.\n\n
 *  
 *  Only predefined and user-defined Palettes have a name, are stored in the named Palette HashMap/ArrayList? 
 *  and can be accessed from the Palette-picking DropDown menu. Dynamically created Palettes (created using the 
 *  median cut technique of quantisation) are stored by the SngImage object in a HashMap (for effeciency: avoids 
 *  repeating the median cut operations for the same amount of colours) using the requested number of colours as the key.       
 * 
 * @version 6-6-14
 * @author Andrew
 *
 */

public class Palette{
	
	Sng3 p5;
	boolean user = false;
	boolean restricted = false;
	HashMap <Integer, Floss> hash;
	HashMap <Integer, Integer> allColors;
	String name;
	PImage img;
	int size;
	//Stitcher st;
	
	/**
	 * Constructor for when an initial ArrayList of Floss objects has been already created (possibly by
	 * using the Palette creator option..?) and after image adjustment for use with CompleteImage.
	 * 
	 * @param parent Sng3 PApplet for use mostly for creating new Floss objects
	 * @param flosses The HashMap of Floss objects with Integer keys
	 */
	
	public Palette(Sng3 parent, Floss[] flosses, String name, PImage img){
		p5 = parent;
		this.name = name;
		this.img = img;
		hash = new HashMap <Integer, Floss> (flosses.length);
		for(int i = 0; i<flosses.length; i++){
			hash.put(new Integer(flosses[i].color(SngModes.ACTUAL)), flosses[i]);
		}
		size = hash.size();
		
	}
	
	/**
	 * Constructor for when an initial ArrayList of Floss objects has been already created (possibly by
	 * using the Palette creator option..?) and after image adjustment for use with CompleteImage.
	 * 
	 * @param parent Sng3 PApplet for use mostly for creating new Floss objects
	 * @param flosses The HashMap of Floss objects with Integer keys
	 */
	
	public Palette(Sng3 parent, HashMap <Integer, Floss> flosses){
		p5 = parent;
		hash = flosses;
		size = hash.size();
		
	}
	
	/**
	 * Constructor for when an array of colors has been provided (loading from a JSON file for example).
	 * 
	 * @param parent PApplet for use mostly for creating new Floss objects
	 * @param colors int array of hypothetical colors to create Floss objects
	 * @param name The user-defined / preset name for this palette (for use in the dropdown menu)
	 */
	public Palette(Sng3 parent, int [] colors, String name, PImage img){
		p5 = parent;
		this.name = name;
		this.img = img;
		//this.st = p5.stitch;
		hash = new HashMap <Integer, Floss> (colors.length);
		
		//all flosses are stored in a hash
		for(int i=0; i<colors.length; i++){
			Integer in = new Integer(colors[i]);
			if(!hash.containsKey(in)){
				Floss f = new Floss(p5, in);
				hash.put(in, f);
			}
		}
		
		size = hash.size();
		
	}
	
	/**
	 * Constructor used for creating a Palette from an array of String ids. These are used to create RestictedFloss
	 * objects which are stored in the HashMap.
	 * 
	 * @param parent PApplet for use mostly for creating new Floss objects
	 * @param ids String array of IDs for use creating RestrictedFloss objects
	 * @param name The user-defined name for this palette (for use in the dropdown menu)
	 */
	public Palette(Sng3 parent, String [] ids, String name, PImage img){
		p5 = parent;
		this.name = name;
		this.img = img;
		//this.st = p5.stitch;
		user = true;
		hash = new HashMap <Integer, Floss> (ids.length);
		//all flosses are stored in a hash
		for(int i=0; i<ids.length; i++){
			//Floss f = new RestrictedFloss(p5, ids[i]);
			Floss f = new Floss(p5, ids[i]);
			if(!hash.containsValue(f)){
				hash.put(f.key, f);
			}
		}
		
		size = hash.size();
		restricted = true;
	}
	
	/**
	 * Constructor for dynamically-created Palettes based on the colours already in the SngImage image. 
	 * This calls the Median Cut method and doesn't require a name string as it cannot be selected from the Dropdown
	 * menu.
	 * 
	 * @param parent PApplet for use mostly for creating new Floss objects
	 * @param numColors The maximum number of colours that will be in the palette. This may less sepending on the colors/mode
	 * of the CompleteImage object. 
	 */
	public Palette(Sng3 parent, int numColors){

		System.out.println("creating a palette from "+numColors+" colors");
		p5 = parent;
		allColors = p5.img.allColors;
		int [] colors = medianCut(numColors);
		
		if(colors != null){
		
			hash = new HashMap <Integer, Floss> (colors.length);
			
			//all flosses are stored in a hash
			for(int i=0; i<colors.length; i++){
				Integer in = new Integer(colors[i]);
				if(!hash.containsKey(in)){
					Floss f = new Floss(p5, in);
					hash.put(in, f);
				}
			}
			
			System.out.println("the palette hash has "+hash.size()+" colors");
			
			size = hash.size();
		}
		
	}
	
	/**
	 * Contructor for dynamic creation of new HashMap for median cut due to colors intoduced/removed by glitching
	 * 
	 * @param parent PApplet for use mostly for creating new Floss objects
	 * @param numColors The maximum number of colours that will be in the palette. This may less sepending on the colors/mode
	 * @param cols The HashMap of created by the SngImage from the glitched image
	 */
	
	public Palette(Sng3 parent, int numColors, HashMap<Integer, Integer> cols) {
		
		System.out.println("creating a palette from "+numColors+" colors");
		p5 = parent;
		allColors = cols;
		int [] colors = medianCut(numColors);
		
		if(colors != null){
		
			hash = new HashMap <Integer, Floss> (colors.length);
			
			//all flosses are stored in a hash
			for(int i=0; i<colors.length; i++){
				Integer in = new Integer(colors[i]);
				if(!hash.containsKey(in)){
					Floss f = new Floss(p5, in);
					hash.put(in, f);
				}
			}
		
			System.out.println("the palette hash has "+hash.size()+" colors");
		
			size = hash.size();
		}
	}

	/**
	 * Add a Floss to the internal HashMap
	 * 
	 * @param f the Floss to add
	 */
	public void add(Floss f){
		if(!hash.containsValue(f)){
			hash.put(f.key, f);
		}
	}
	
	/**
	 * Add a new (hypothetical) color of Floss to the HashMap
	 * 
	 * @param in Integer representation of the color used to construct the Floss
	 */
	public void add(Integer in){
		if(!hash.containsKey(in)){
			Floss f = new Floss(p5, in);
			hash.put(in, f);
		}
	}
	
	/**
	 * Sorts the Floss objects based on their usage in this CompleteImage - low to high. This is used
	 * when assigning simplified symbols to Floss objects.  
	 * 
	 * @param mode The mode of the CompleteImage object
	 * @return Floss[] Array of Floss objects sorted accoring to usage 
	 */
	public Floss [] inOrder(int mode){
		
		ArrayList <Floss> list = new ArrayList<Floss>(hash.values());
		
		Collections.sort(list, new Comparator<Floss>() {
			public int compare(Floss f1, Floss f2){
				return f2.count - f1.count;
			}
		});
		
		Floss[]sorted = new Floss[list.size()];
		for(int i=0; i<list.size(); i++){
			sorted[i] = list.get(i);
		//	System.out.println("Floss "+i+" used "+sorted[i].count+" times");
		}
		
		return sorted;
		
	}
	
	/**
	 * The array of color ints present in this Palette depending on the given mode. 
	 * This is used when dithering (and creating colorlist?).
	 * 
	 * @param mode Int mode - the mode of the CompleteImage object
	 * @return int[] array of colours of a maximum size of the Palette size
	 */
	public int [] colorList(int mode){
		
		ArrayList <Integer> used = new ArrayList <Integer> (size);

		for (Map.Entry <Integer, Floss> me : hash.entrySet()) {		
			Floss f = me.getValue();
			Integer test;
			test = new Integer(f.color(mode));
			if(!used.contains(test)){
			  used.add(test);
			}
		}
		
		Object [] toArray = used.toArray();
		
		int [] toReturn = new int [used.size()];
		for(int i=0; i<toArray.length; i++){
			Integer temp = (Integer)toArray[i];
			toReturn[i] = temp.intValue();
		}
		return toReturn;
	}
	
	/**
	 * Used when creating a colorlist?
	 * 
	 * @param mode Int mode - the mode of the CompleteImage object
	 * @return String[] all of the ids for the colors given the mode
	 */
	public String [] ids(int mode){
		
		ArrayList <Floss> used = new ArrayList <Floss> (hash.size());

		for (Map.Entry <Integer, Floss> me : hash.entrySet()) {		
			Floss f = me.getValue();
			if(!used.contains(f)){
			  used.add(f);
			}
		}
		
		Object [] toArray = used.toArray();
		String [] toReturn = new String [toArray.length];
		
		for(int i=0; i<toArray.length; i++){
			Floss f = (Floss)toArray[i];
			toReturn[i] = f.id(mode);
		}

		return toReturn;
		
		
	}
	
	public String [] toStringArray(){
		String [] toReturn = new String[hash.size()];
		int index=0;
		for (Map.Entry <Integer, Floss> me : hash.entrySet()) {		
			Floss f = me.getValue();
			toReturn[index] = f.toString();
			index++;
		}
		return toReturn;
	}
	
	//the main loop to subdivide the large colour space into the required amount 
	//of smaller boxes. return the array of the average colour for each of these boxes.
	private int [] medianCut(int numCols){
	
		//aka findRepresentativeColors 
		if(allColors == null){
			return null;
		}else if(allColors.size() <= numCols){
			//num of colours is less than or equal to the max num in the orig image
			//so simply return the colour in an int array
			int [] toReturn = new int[allColors.size()];
			int index = 0;
			for (Map.Entry <Integer, Integer> me : allColors.entrySet()) {
				  toReturn[index] = me.getValue().intValue();
				  index++;
			}
			return toReturn;
		}else{
			//otherwise subdivide the box of colours untl the required number 
			//has been reached
			
			ArrayList <ColorBox> colorBoxes = new ArrayList <ColorBox> (numCols);//where the boxes will be stored
			ColorBox first = new ColorBox(allColors,0);//the largest box (level 0)
			colorBoxes.add(first);
			int k = 1;//we have 1 box
			boolean done = false;
			
			while(k<numCols && !done){
				
				ColorBox next = findBoxToSplit(colorBoxes);
				
				if(next != null){
					
					ColorBox [] boxes = next.splitBox();
					if(colorBoxes.remove(next)){};//finds and removes an element in one
					colorBoxes.add(boxes[0]);
					colorBoxes.add(boxes[1]);
					k++;//we have one more box 
					
				}else{
					done = true;
				}
				
			}
			//get the average colour from each of the boxes in the arraylist
			int [] avgCols = new int[colorBoxes.size()];
			for(int i=0; i<avgCols.length; i++){
				ColorBox cb = (ColorBox)colorBoxes.get(i);
				avgCols[i] = cb.getAvgColor();
			}
			return avgCols;	
		}
	}
	//takes the list of all candidate boxes and returns the one to be split next.
	private ColorBox findBoxToSplit(ArrayList <ColorBox> boxes){
		
		ArrayList <ColorBox> canBeSplit = new ArrayList <ColorBox> (10);
		for(int i=0; i<boxes.size(); i++){
			ColorBox cb = (ColorBox)boxes.get(i);
			if(cb.size > 1){
				canBeSplit.add(cb);
			}
		}
		//only boxes containing more than one colour can be split 
		if(canBeSplit.size() == 0){
			return null; //a null will trigger the end of the subdividing loop
		}else{
			//use the 'level' of each box to ensure they are divided in the correct order.
			//the box with the lowest level is returned.
			ColorBox minBox = (ColorBox)canBeSplit.get(0);
			int minLevel = minBox.level;

			for(int i=1; i<canBeSplit.size(); i++){
				ColorBox test = (ColorBox)canBeSplit.get(i);
				if(minLevel > test.level){
					minLevel = test.level;
					minBox = test;
				}
			}

			return minBox;
		}
		
	}
	
	//inner class for the median cut
	class ColorBox{
		
		HashMap <Integer, Integer> cols;
		int level;
		int [] [] rgb;
		int size;
		
		public ColorBox(HashMap <Integer, Integer> cols, int level){
			this.cols = cols;
			this.level = level;
			size = cols.size();
			
			rgb  = new int [3] [size];
			
			int index = 0;
			for (Map.Entry <Integer, Integer> me : cols.entrySet()) {
				  rgb [Modes.RED][index] = (me.getValue().intValue() >> 16) & 0xFF;
				  rgb [Modes.GREEN][index] = (me.getValue().intValue() >> 8) & 0xFF;
				  rgb [Modes.BLUE][index] = me.getValue().intValue() & 0xFF;
				  index++;
			}
		}
		
		public int min(int channel){
			int min_found = rgb[channel][0];
			for(int i=1; i<size; i++){
				int test = rgb[channel][i];
				if(test < min_found){
					min_found = test;
				}
			}
			return min_found; 	
		}
		
		public int max(int channel){
			int max_found = rgb[channel][0];
			for(int i=1; i<size; i++){
				int test = rgb[channel][i];
				if(test > max_found){
					max_found = test;
				}
			}
			return max_found; 	
		}
		//get the average colour of all the colours contained by the given box
		public int getAvgColor(){
			
			int [] r_g_b = {0,0,0};
			
			for(int i=0; i<3; i++){
				for(int j=0; j<size; j++){
					r_g_b[i] += rgb[i][j];//sum of each channel stored separately
				}
			}
			
			float avgRed = r_g_b[0]/(float)size;
			float avgGreen = r_g_b[1]/(float)size;
			float avgBlue = r_g_b[2]/(float)size;
			
			return p5.color(avgRed, avgGreen, avgBlue);
			
		}
		//the method to find and return the longest axis of the box as the one to divide along.
		int maxDimension(){
			//the length of each is measured as the (max value - min value)
			int diffRed = max(Modes.RED) - min(Modes.RED);
			int diffGreen = max(Modes.GREEN) - min(Modes.GREEN);
			int diffBlue = max(Modes.BLUE) - min(Modes.BLUE);
			if(diffRed > diffGreen && diffRed > diffBlue){
				return Modes.RED;
			}else if(diffGreen > diffRed && diffGreen > diffBlue){
				return Modes.GREEN;
			}else{
				return Modes.BLUE;
			}
		}
		
		//divide a box along its longest RGB axis to create 2 smaller boxes and return these
		ColorBox [] splitBox(){
			
			int dim = maxDimension();
			int c = 0;
			for(int i=0; i<size; i++){
				c += rgb[dim][i];
			}
			float median = c/(float)size;//get the median only counting along the longest RGB dimension
			//the two Hashmaps to contain all the colours in the original box
			HashMap <Integer, Integer> left = new HashMap <Integer, Integer> (size);
			HashMap <Integer, Integer> right = new HashMap <Integer, Integer> (size);
			for (Map.Entry <Integer, Integer> me : cols.entrySet()) {
				Integer key = me.getKey();
				Integer value = me.getValue();
				//putting each colour in the appropriate box
				switch (dim){
				case Modes.RED:
					if(((key.intValue() >> 16) & 0xFF) <= median){
						left.put(key,value);
					}else{
						right.put(key,value);
					}
					break;
				case Modes.GREEN:
					if(((key.intValue() >> 8) & 0xFF) <= median){
						left.put(key,value);
					}else{
						right.put(key,value);
					}
					break;
				default:
					if((key.intValue() & 0xFF) <= median){
						left.put(key,value);
					}else{
						right.put(key,value);
					}					
				}
			}
			
			ColorBox [] toReturn = new ColorBox[2];
			toReturn[0] = new ColorBox(left,level+1);
			toReturn[1] = new ColorBox(right,level+1);
			return toReturn;
			
		}
		
		
	}

}
