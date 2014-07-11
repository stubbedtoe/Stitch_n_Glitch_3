package ie.cognate.sng;

import java.io.File;
import ie.cognate.Stitcher;
import processing.core.*;
import java.util.ArrayList;
import processing.data.*;
import java.awt.Point;
import java.awt.MouseInfo;



/**
 * The main PApplet class. All I/O handling and animation happens in this class.
 * 
 * 
 * @author Andrew
 *
 */

@SuppressWarnings("serial")
public class Sng3 extends PApplet{
	

	public GUI gui;
	public boolean ready = false;
	boolean still500 = true;
	boolean loaded = false;
	int maxpixels, maxbytes, absMax;
	public SngImage img;
	public Stitcher stitch;
	public ArrayList <Palette> palettes;
	public JSONArray allusers;
	public PShape [] symbols;
	public PShape thankyou, brokenImg;
	public PFont pfont;
	Loader l;
	PImage loading;
	int frameTop, border, smallborder, stroke, simage_w, simage_h, half_w, half_h;
	int [][][] symbol_cols;
	
	/**
	 * Main method used to run the sketch as an application.
	 * 
	 * @param args
	 */
	public static void main(String[]args){
		PApplet.main(new String[] {"ie.cognate.sng.Sng3"});
	}
	
	/**
	 * Called first. Loads basic files such as background image, palettes. Creates GUI object
	 * and instance of Stitcher library. Asks user for an image.  
	 */
	public void setup(){
		
		frame.setTitle("Stitch'n'Glitch");
		
		size(500,500);
		shapeMode(CENTER);
		imageMode(CENTER);
		loading = loadImage("assets/loading.gif");
		
		l = new Loader(this);
		new Thread(l).start();
		
		//load preferences file and set limits..too important to be thread safe
		JSONObject prefs = loadJSONObject("NO_EDIT/preferences.json");
		maxbytes = prefs.getInt("max-bytes");
		maxpixels = prefs.getInt("max-pixels");
		absMax = prefs.getInt("absolute");
		
		//while(!l.loaded){}
		selectInput("Select an image (RGB jpg or gif)","fileSelected");
	}

	
	/**
	 * Main animation loop. Mostly differs according to which gui screen is active.
	 */
	public void draw(){
		
		if(!l.loaded && still500)
			background(loading);
		else
			background(255);
		
		
		if(ready){
			try{
				switch(gui.screen()){
				case 0://glitch
					img.drawBig();break;
				default:
					setMouseXY();
					
					int selected;			
					if(mouseX >= 0 && mouseX < half_w && mouseY >= 0 && mouseY < half_h){
						selected = 0;
					}else if(mouseX >= half_w && mouseX <= width && mouseY >= 0 && mouseY < half_h){
						selected = 1;
					}else if(mouseX >= 0 && mouseX < half_w && mouseY >= half_h && mouseY <= height){
						selected = 2;
					}else if(mouseX >= half_w && mouseX <= width && mouseY >= half_h && mouseY <= height){
						selected = 3;
					}else{
						selected = gui.selectedImage();
					}
					int x, y;	

					img.drawSmall();
					switch(selected){
						case 0:
							x = smallborder/2; y = smallborder/2; break;
						case 1:
							x = width/2; y = smallborder/2; break;
						case 2:
							x = smallborder/2; y = (height-frameTop)/2; break;
						default:
							x = width/2; y = (height-frameTop)/2; break;
					}
					noFill();
					stroke(0,255,0);
					strokeWeight(smallborder);
					rect(x, y, (width/2)-smallborder/2, ((height-frameTop)/2)-smallborder/2);
				}
			}catch(Exception e){
				
			}
		}
	}
	
	void setMouseXY() 
	{
	 // if(mouseX>=0 && mouseX<width && mouseY>=0 && mouseY<height) return;
	  Point mouse, winloc;
	  mouse = MouseInfo.getPointerInfo().getLocation();
	  winloc = frame.getLocation();
	  winloc.y += frameTop;
	  mouseX = mouse.x-winloc.x;
	  mouseY = mouse.y-winloc.y;
	}
	
	public void resize(int w, int h){
		super.resize(w, h+frameTop);
		frame.setSize(w, h+frameTop);
		
	}
	/*
	public void setSize(int w, int h){
		if(w > 0 && h > 0)
			
	}
	*/
	/**
	 * Callback method from the file selection prompt. Checks whether a valid image was chosen and
	 * creates the SngImage agains the prefences. If not, prompts again. If the prompt was cancelled, default image is used. 
	 * 
	 * @param selection The chosen File; can be null(cancelled)
	 */
	public void fileSelected(File selection){
		
		if (selection == null){
			img = new SngImage(this, "assets/notes_small.jpeg");
			ready = true;
			frame.setTitle("default image");
			String message = img.setLimits(maxpixels, maxbytes);
			if(l.loaded)
				gui.writeMessage(message);
		}else{
			String path = selection.getAbsolutePath();
			String [] parts =path.split("/");
			String ext= checkExtension(path);
			byte[]bytes = loadBytes(path);
			if((ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif")) &&  bytes.length < absMax){
				img = new SngImage(this, path);
				frame.setTitle(parts[parts.length-1]);
				ready = true;
				String message = img.setLimits(maxpixels, maxbytes);
				if(l.loaded)
					gui.writeMessage(message);
			}else{
				//an acceptable image was not selected
				selectInput("File too big or not an image (RGB jpg preferred)","fileSelected");
			}
		}

	}
	
	public void saveUserPalette(Palette p){
		
		JSONArray users = loadJSONArray("NO_EDIT/user.json");
		String [] str = p.toStringArray();
		JSONArray flosses = new JSONArray();
		JSONObject palette = new JSONObject();
		for(int i=0; i<str.length; i++){
			flosses.setString(i, str[i]);
		}
		palette.setJSONArray("flosses", flosses);
		palette.setString("name", p.name);
		p.img.save("NO_EDIT/images/"+p.name.replaceAll(" ", "_")+".png");
		palette.setString("url","NO_EDIT/images/"+p.name.replaceAll(" ", "_")+".png");
		users.append(palette);
		saveJSONArray(users, "NO_EDIT/user.json");
		palettes.add(p);
		
	}
	
	public void mouseReleased(){
		 if(gui.screen() == 0){
			 if(img.landscape){
				 	border = mouseX/20;
				 	smallborder = mouseX/30;
					resize(mouseX, (int)(mouseX*img.aspectRatio));
				}else{
					border = (mouseY-frameTop)/20;
					smallborder = (mouseY-frameTop)/30;
					resize((int)(mouseY*img.aspectRatio), mouseY);
				}
			 	half_w = mouseX/2;
				half_h = (mouseY- frameTop)/2;
			 	simage_w = (mouseX/2)-(int)(smallborder*1.5);
				simage_h = ((mouseY-frameTop)/2)-(int)(smallborder*1.5);
		 }else{
			 gui.imageChosen(mouseX, mouseY, width/2, (height - frameTop)/2);
		 }
	 }
	
	

}
