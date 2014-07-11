package ie.cognate.sng;

import processing.core.*;
import ie.cognate.Modes;
import ie.cognate.Stitcher;
import java.util.HashMap;


/**
 * This class encapsulates image-specific properties and methods to open the possibility
 * for easy progression to allowing a multiple-image setup.
 * 
 * @author Andrew
 * @version 7-6-14
 */

public class SngImage implements Runnable, SngModes{
	
	public Sng3 p5;
	public GUI gui;
	public boolean landscape, pattern, glitchDone, doGlitch;
	public PImage pimg, img, glitched;
	public int width, height, pixelLength;
	public CompleteImage [] history;
	public float aspectRatio;
	public String path;
	public boolean glitch = true;
	public boolean broken = false;
	public boolean busy = false;
	public int done;
	public int inOriginal;
	public Thread t;
	public HashMap <Integer, Integer> allColors;
	public HashMap <Integer, Palette> dynamicPalettes;
	
	/**
	 * main constructor.
	 * 
	 * @param parent
	 * @param path
	 */
	public SngImage(Sng3 parent, String path){
		p5 = parent;
		//p5.registerDispose(this);
		gui = p5.gui;
		this.path = path;
		glitchDone = false;
		try{
			pimg = p5.loadImage(path);
		}catch(Exception e){
			pimg = p5.loadImage("/assets/notes_small.jpeg");
			p5.gui.writeMessage("Problem found; default image being used");
		}
		
		width = pimg.width;
		height = pimg.height;
		landscape = (width > height);
		pixelLength = pimg.pixels.length;
		
		//backup
		img = new PImage(width,height, PConstants.ARGB);
		glitched = new PImage(width,height, PConstants.ARGB);
		img.loadPixels(); pimg.loadPixels(); glitched.loadPixels();
		for(int i=0; i<pixelLength; i++){
			img.pixels[i] = pimg.pixels[i];
			glitched.pixels[i] =  pimg.pixels[i];
		}
		img.updatePixels(); pimg.updatePixels();glitched.updatePixels();
		p5.still500 = false;
		
		history = new CompleteImage [4];
		for(int i=0; i<4; i++){
			history[i] = null;
		}
		done = 0;
		allColors = findOrigHash(pimg);
		inOriginal = allColors.size();
		dynamicPalettes = new HashMap<Integer, Palette>(10);
		
		if(landscape){
			aspectRatio = (float)height/(float)width;
			p5.resize(500, (int)(500*aspectRatio));
		}else{
			aspectRatio = (float)width/(float)height;
			p5.resize((int)(500*aspectRatio), 500);
		}
		PApplet.println("there are "+inOriginal+" out of "+pixelLength+" colors in this image");
	}

	
	public HashMap <Integer, Integer> findOrigHash(PImage theImage){
		
		try{
		
		HashMap <Integer, Integer> toReturn = new HashMap <Integer, Integer> (200);
		theImage.loadPixels();
		for(int i=0; i<pixelLength; i++){
			Integer test = new Integer(theImage.pixels[i]);
			if(!toReturn.containsKey(test)){
				toReturn.put(test, test);
			}
		}
		theImage.updatePixels();
		
		return toReturn;
		
		}catch(Exception e){
			PApplet.println("exception in findOrigHash "+e);
			p5.gui.writeMessage("image broken: reduce glitch or try again", true);
			broken = true;
			return null;
		}
		
	}
	
	/**
	 * 
	 */
	public void drawBig(){
		//p5.image(pimg, p5.width/2, p5.height/2, p5.width-20, p5.height-10); 
		if(broken){
			p5.copy(img, 0, 0, width, height, p5.border, p5.border, p5.width - p5.border*2, p5.height-p5.border*2-p5.frameTop);
			p5.shape(p5.brokenImg, p5.width/2, (p5.height-p5.frameTop)/2, p5.half_w, p5.half_w);
		}else{
			if(pimg.width <= 0 || pimg.height <= 0 ){
				broken = true;
				p5.gui.writeMessage("image broken: reduce glitch or try again", true);
			}else{
				p5.copy(pimg, 0, 0, width, height, p5.border, p5.border, p5.width - p5.border*2, p5.height-p5.border*2-p5.frameTop);
			}
		}
	}
	
	public void drawSmall(){
		int end  = 0;
		for(int y=0; y<2; y++){
			for(int x=0; x<2; x++){
				if(end < done){
					int offX = p5.smallborder; 
					if(x == 1)
						offX = (p5.width/2)+(p5.smallborder/2);
					int offY = p5.smallborder; 
					if(y == 1)
						offY = ((p5.height- p5.frameTop)/2)+(p5.smallborder/2);
					p5.copy(history[end].img, 0 , 0 , width, height, offX, offY, (p5.width/2)-(int)(p5.smallborder*1.5f), ((p5.height- p5.frameTop)/2)-(int)(p5.smallborder*1.5f));
				}
				end++;
			}
		}
	}
	
	public void run(){
		
		PApplet.println("glitch screen: "+glitch);
		PApplet.println("previous broken: "+broken);
		PApplet.println("update rather than glitch: "+!doGlitch);
		System.gc();
		
		if(glitch){ //"Glitch" pressed
			if(broken){ //reload with original pixels
				pimg = new PImage(width, height, PConstants.ARGB);
			}
			if(doGlitch || broken || !glitchDone){ //have pimg the same as original
				
				PApplet.println("reloading from original");
				img.loadPixels(); pimg.loadPixels();
				for(int i=0; i<pixelLength; i++){
					pimg.pixels[i] = img.pixels[i];
				}
				img.updatePixels(); pimg.updatePixels();
				broken = false;
				if(doGlitch){ //but only glitch if specified to do so
					PImage temp = glitch();
					try{
						temp.loadPixels();pimg.loadPixels();
						for(int i=0; i<pixelLength; i++){
							pimg.pixels[i] = temp.pixels[i];
						}
						temp.updatePixels();pimg.updatePixels();
					}catch(Exception e){
						PApplet.println("exception in run,glitch/broken,doglitch: "+e);
						gui.writeMessage("image broken: reduce glitch or try again", true);
						broken = true;
					}
				}
			}else{
				//use the previously used glitched image for an update to dither/palette/mode 
				glitched.loadPixels(); pimg.loadPixels();
				PApplet.println("reloading from previos glitch");
				try{
					for(int i=0; i<pixelLength; i++){
						pimg.pixels[i] = glitched.pixels[i];
					}
				}catch(Exception e){
					PApplet.println("exception in run,glitch/broken: "+e);
					gui.writeMessage("image broken: reduce glitch or try again", true);
					broken = true;
				}	
				glitched.updatePixels(); pimg.updatePixels();
			}
			
			Palette toUse;
			int mode = gui.getMode();
			if(gui.dynamicPal()){ //used the number slider
				
				PApplet.println("in dynamic palette");
				
				int colsNum = gui.howManyColors();
				Integer key = new Integer(colsNum);
				if(!dynamicPalettes.containsKey(key) || (gui.getGlitchAmt() > 0 && doGlitch)){ //different number than ever
					if(gui.getGlitchAmt() > 0 && doGlitch){
						//new palette from new survey of colors in image
						toUse = new Palette(p5, colsNum, findOrigHash(pimg));
					}else{
						//add if there was no glitch - can be used again
						toUse = new Palette(p5, colsNum);
						dynamicPalettes.put(key, toUse);
					}	
				}else{ //used number before + no glitch
					toUse = (Palette)dynamicPalettes.get(key);
				}
			}else{//used predefined palette
				toUse = (Palette)p5.palettes.get(gui.paletteKey());
			}
			
			//PApplet.println("The palette has "+toUse.size+" colors");
			CompleteImage ci;
			if(toUse.size <= 1 || toUse == null){
				PApplet.println("paletteSize <= 1 or palette is null");
				gui.writeMessage("image broken: reduce glitch or try again", true);
				broken = true;
				ci = null;
			}else{
				ci = dither(gui.getDitherType(), toUse, mode);
			}

			if(!broken && ci != null){
				for(int move = 2; move >= 0; move--){
					if(history[move]!=null)
						history[move+1] = history[move];
				}
				history[0] = ci;//always puts the most recent in the 0 slot
				done++;
			}
		}else{
			

		}
		busy = false;	
	}
	
	public String setLimits(int maxpixels, int maxsize){
		byte[] byteSize = p5.loadBytes(path);
		int byteLength = byteSize.length;
		pattern = (byteLength < maxsize && pixelLength < maxpixels);
		if(pattern){
			return "This image is small enough to create a .pdf pattern";
		}else{
			return "This image is too large to fully Stitch'n'Glitch";
		}
	}
	
	public void startGlitch(boolean toBeGlitched){
		if(!busy){
			glitch = true;
			/*
			if(toBeGlitched)
				doGlitch = true;
			else
				doGlitch = (!glitchDone && !broken);
			*/	
			doGlitch = toBeGlitched;
			busy = true;
			t = new Thread(this);
			t.start();
		}
	}
	
	public PImage glitch(){
		
		int amt = gui.getGlitchAmt();
		
		if(amt > 0){
			
			byte [] bits = p5.loadBytes(path);
			
			if(bits != null){
			
				byte [] bCopy = new byte[bits.length];
				PApplet.arrayCopy(bits, bCopy);
				int scrambleStart = 10;
				int scrambleEnd = bits.length;
				
				for(int i=0; i<amt; i++){
					int PosA = (int)p5.random(scrambleStart, scrambleEnd);
					int PosB = (int)p5.random(scrambleStart, scrambleEnd);
					byte tmp = bCopy[PosA];
					bCopy[PosA] = bCopy[PosB];
					bCopy[PosB] = tmp;
				}
				
				p5.saveBytes("NO_EDIT/corrupt.jpg", bCopy);
				glitched = p5.loadImage("NO_EDIT/corrupt.jpg");
		
				glitchDone = true;
				if(glitched != null){
					PApplet.println("glitch finished");
					return glitched;
				}else{
					PApplet.println("image loaded to null");
					return pimg;
				}		
			}else{
				PApplet.println("bytes loaded to null");
				return pimg;
			}
			
		}else{
			PApplet.println("glitch zero: returning original");
			
			try{
				glitched.loadPixels(); pimg.loadPixels();
				for(int i=0; i<pixelLength; i++){
					glitched.pixels[i] = pimg.pixels[i];
				}
				glitched.updatePixels(); pimg.updatePixels();
			}catch(Exception e){
				PApplet.println("exception in glitch: "+e);
				gui.writeMessage("image broken: reduce glitch or try again", true);
				broken = true;
				return pimg;
			}
			return pimg;
		}
		
	}
	
	public CompleteImage dither(int dither_type, Palette pal, int mode){
		
		
		if(dither_type != 0){ //0 == no dither
			
			int [] locations = {};
			float [] divisors = {};
			int bayer =0;
			
			switch (dither_type){
			case 1:
				//flyod-steinburg
				float [] fs1 = {7/16.0f, 3/16.0f, 5/16.0f, 1/16.0f};
				int [] fs2 = {1, width-1, width, width+1};
				locations = fs2;
				divisors = fs1;
				break;
			case 2:
				//atkinson
				float [] at1 = {1/8.0f, 1/8.0f, 1/8.0f, 1/8.0f, 1/8.0f, 1/8.0f};
				int [] at2 = {1, 2, width-1, width, width+1, width*2};
				locations = at2;
				divisors = at1;
				break;
			case 3:
				//jarvis			0		1		2		  3			4		5		6		  7			8		9		10			11
				float [] jjn1 = {7/48.0f, 5/48.0f, 3/48.0f, 5/48.0f, 7/48.0f, 5/48.0f, 3/48.0f, 1/48.0f, 3/48.0f, 5/48.0f, 3/48.0f, 1/48.0f};
				int [] jjn2 = 	{1, 2, width-2, width-1, width, width+1, width+2, (width*2)-2, (width*2)-1, width*2, (width*2)+1, (width*2)+2};
				locations = jjn2;
				divisors = jjn1;
				break;
			case 4:
				//stucki			0		1		2		3			4		5		6		7		8		9			10		11
				float [] sk1 = {7/42.0f, 5/42.0f, 2/42.0f, 4/42.0f, 8/42.0f, 4/42.0f, 2/42.0f, 1/42.0f, 2/42.0f, 4/42.0f, 2/42.0f, 1/42.0f};
				int [] sk2 = 	{1, 2, width-2, width-1, width, width+1, width+2, (width*2)-2, (width*2)-1, width*2, (width*2)+1, (width*2)+2};
				locations = sk2;
				divisors = sk1;
				break;
			case 5:
				//clustered
				float [] clus = {0.75f, 0.375f, 0.625f, 0.25f, 0.0625f, 1.f, 0.875f, 0.4375f, 0.5f, 0.8125f, 0.9375f, 0.1250f, 0.1875f, 0.5625f, 0.3125f, 0.6875f};
				for(int i=0; i<clus.length; i++){
					clus[i] *= 255;
				}
				divisors = clus;
				bayer = 4;
				break;
			case 6:
				//bayer2
				float [] by2 = {2.0f*64-1, 3.0f*64-1, 4.0f*64-1, 1.0f*64-1};
				bayer = 2;
				divisors = by2;
				break;
			case 7:
				//bayer4
				float [] by4 = {0.125f, 1.f, 0.1875f, 0.8125f, 0.625f, 0.375f, 0.6875f, 0.4375f,0.25f, 0.875f, 0.0625f, 0.9375f, 0.75f, 0.5f, 0.5625f, 0.3125f};
				for(int i=0; i<by4.length; i++){
					by4[i] *= 255;
				}
				divisors = by4;
				bayer = 4;
				break;
			case 8:
				//bayer 8
				float [] by8 = {1f, 33f, 9f, 41f, 3f,  35f, 11f, 43f, 49f, 17f, 57f, 25f, 51f, 19f, 59f, 27f, 13f, 45f, 5f, 37f, 15f, 47f, 7f, 39f, 61f, 29f, 53f, 21f, 63f, 31f, 55f, 23f, 4f, 36f, 12f, 44f, 2f, 34f, 10f, 42f, 52f, 20f, 60f, 28f, 50f, 18f, 58f, 26f, 16f, 48f, 8f, 40f, 14f, 46f, 6f, 38f, 64f, 32f, 56f, 24f, 62f, 30f,54f, 22f};
				for(int i=0; i<by8.length; i++){
					by8[i] = by8[i]*4-1;
				}
				divisors = by8;
				bayer = 8;
				break;
			default:
				bayer = 9;
			}
			
			return do_dither(divisors, locations,  bayer, pal, mode);
		
		}else{
			//convert without dithering
			
			//try{
			
				if(pattern){
					
					int [] palette = pal.colorList(mode);
					HashMap <Integer, Floss> flosses = new HashMap <Integer, Floss> (palette.length); 
					
					pimg.loadPixels();
					for(int i=0; i<pixelLength; i++){
						
						Integer key = new Integer(Stitcher.getClosest(pimg.pixels[i], palette)); 
						
						if(flosses.containsKey(key)){
							Floss f = (Floss)flosses.get(key);
							pimg.pixels[i] = f.color(mode);
						}else{
							Floss fl;
							//check if is in pal hash 
							if(pal.hash.containsKey(key)){
								fl = (Floss)pal.hash.get(key);
							}else{
								//if not, create a new restricted floss
								String id = p5.stitch.getClosestID(key.intValue());
								fl = new Floss(p5, id);
							}
							
							// check if the two give the same result - avoid problems with the pattern
							Floss f = new Floss(p5, key);
							if(fl.color(mode) != f.color(mode)){
								//use the more specific
								flosses.put(new Integer(fl.color(mode)), fl);
								pimg.pixels[i] = fl.color(mode);
							}else{
								flosses.put(key, f);
								pimg.pixels[i] = f.color(mode);
							}
							
							
						}
					}
					
					pimg.updatePixels();
					
					if(flosses.size()<=1){
						gui.writeMessage("image broken: reduce glitch or try again", true);
						broken = true;
						return null;
					}else{
						gui.writeMessage(flosses.size()+" colours given this mode", false);
						Palette p = new Palette(p5, flosses);
						return new CompleteImage(p5, pimg, p, mode);	
					}
				}else{
					return new CompleteImage(pimg);
				}
			/*
			}catch(Exception e){
				PApplet.println("exception in dither: "+e);
				gui.writeMessage("image broken: reduce glitch or try again", true);
				broken = true;
				return null;
			}*/
		}
	}
	
	public CompleteImage do_dither(float [] divs, int [] locs, int bayer, Palette pal, int mode){
		
		//PImage _img = new PImage(width, height, PConstants.ARGB);
		HashMap <Integer, Floss> flosses = new HashMap <Integer, Floss> (10);
		int [] palette = pal.colorList(mode);

		try{
		
			pimg.loadPixels();
			
			//HashMap<Integer, Integer> used = new HashMap<Integer, Integer>(pal.size);
			
			for(int y=0; y < height; y++) {
			    for (int x = 0; x < width; x++) {
			    	
			    	int loc = x+y*width;//origin
			    	
			    	Integer key;
			    	int new_color;
			    	int [] olds = {(pimg.pixels[loc] >> 16) & 0xFF, (pimg.pixels[loc] >> 8) & 0xFF, pimg.pixels[loc] & 0xFF};
	
			    	
			    	if(bayer != 0){ // bayer2,4,8/clustered/random
			    		
			    		float change;
				    	
				    	if(bayer == 9){
				    		change = p5.random(256);
				    	}else{
				    		change = divs[y%bayer * bayer + x%bayer];
				    	}
				    		
				    	int approx = p5.color( olds[Modes.RED] + change - 128, olds[Modes.GREEN] + change -128, olds[Modes.BLUE] + change -128);
				    	
				    	if(pattern){
				    		
				    		new_color = Stitcher.getClosest (approx, palette);
				    		key = new Integer(new_color);
			    		
				    	}else{
				    		
				    		int nr = ((approx >> 16) & 0xFF) > 128 ? 255 : 0;
				    		int ng = ((approx >> 8) & 0xFF) > 128 ? 255 : 0;
				    		int nb = (approx & 0xFF) > 128 ? 255 : 0;
				    		new_color = p5.color(nr, ng, nb);
				    		key = new Integer(0); //to avoid errors
				    		
				    	}
				    		
			    	}else{
			    		
			    		if(pattern){
			    		
			    			new_color = Stitcher.getClosest(pimg.pixels[loc], palette);
			    			
			    		}else{
			    			
			    			int nr = ((pimg.pixels[loc] >> 16) & 0xFF) > 128 ? 255 : 0;
				    		int ng = ((pimg.pixels[loc] >> 8) & 0xFF) > 128 ? 255 : 0;
				    		int nb = (pimg.pixels[loc] & 0xFF) > 128 ? 255 : 0;
				    		new_color = p5.color(nr, ng, nb);
			    			
			    		}
			    		
			    		 int [] news = {(new_color >> 16) & 0xFF, (new_color >> 8) & 0xFF, new_color & 0xFF};
					     int [] diffs = {olds[Modes.RED] - news[Modes.RED], olds[Modes.GREEN] - news[Modes.GREEN], olds[Modes.BLUE] - news[Modes.BLUE]};
					        
					     key = new Integer(new_color);
					     
					     for(int i=0; i<locs.length; i++){
					        int tmp_loc = loc+locs[i];
					        if(tmp_loc < pixelLength){
					        	int c = pimg.pixels[tmp_loc];
					        	pimg.pixels[tmp_loc] = p5.color( ((c >> 16) & 0xFF)+divs[i] * diffs[Modes.RED], ((c >> 8) & 0xFF)+divs[i] * diffs[Modes.GREEN], (c & 0xFF)+divs[i] * diffs[Modes.BLUE] );	
					        }
					     }
			    		
			    	}
			    	
			    	if(pattern){

							
						if(flosses.containsKey(key)){
							Floss f = (Floss)flosses.get(key);
							pimg.pixels[loc] = f.color(mode);
						}else{
							Floss fl;
							//check if is in pal hash 
							if(pal.hash.containsKey(key)){
								fl = (Floss)pal.hash.get(key);
							}else{
								//if not, create a new restricted floss
								String id = p5.stitch.getClosestID(key.intValue());
								fl = new Floss(p5, id);
							}
							
							// check if the two give the same result - avoid problems with the pattern
							Floss f = new Floss(p5, key);
							if(fl.color(mode) != f.color(mode)){
								//use the more specific
								flosses.put(new Integer(fl.color(mode)), fl);
								pimg.pixels[loc] = fl.color(mode);
							}else{
								flosses.put(key, f);
								pimg.pixels[loc] = f.color(mode);
							}
						}
						
						
				    	
			    	}else{
			    		pimg.pixels[loc] = new_color;
			    	}
			    }
			}
			
			if(flosses.size() <= 1){
				gui.writeMessage("image broken: reduce glitch or try again", true);
				broken = true;
				return null;
			}else{
			
				gui.writeMessage(flosses.size()+" colours given this mode", false);
				
				if(pattern){
				
					Palette p = new Palette(p5, flosses);
					
					for(int i=0; i<pixelLength; i++){
			    		pimg.pixels[i] = Stitcher.getClosest(pimg.pixels[i], palette); 
			    	}
					
					pimg.updatePixels();
					return new CompleteImage(p5, pimg, p, mode);
				
				}else{
					
					for(int i=0; i<pixelLength; i++){
						int nr = ((pimg.pixels[i] >> 16) & 0xFF) > 128 ? 255 : 0;
			    		int ng = ((pimg.pixels[i] >> 8) & 0xFF) > 128 ? 255 : 0;
			    		int nb = (pimg.pixels[i] & 0xFF) > 128 ? 255 : 0;
			    		pimg.pixels[i] = p5.color(nr, ng, nb);
			    	}
					
					pimg.updatePixels();
					return new CompleteImage(pimg);
				}
			
			}
		
		}catch(Exception e){
			gui.writeMessage("image broken: reduce glitch or try again", true);
			broken = true;
			return null;
		}
		
		
	}
	
	public void startExport(){
		if(!busy){
			glitch = false;
			busy = true;
			t = new Thread (this);
			t.start();
		}
	}
	
	 public void stop() {
		 t = null;
	 }
	 
	 public void dispose() {
		 stop();
	 }
	 
	 
	
	
	
}
