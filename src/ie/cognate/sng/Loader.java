package ie.cognate.sng;

import ie.cognate.Stitcher;

import java.awt.Frame;
import java.awt.Insets;
import java.util.ArrayList;

import processing.core.PShape;
import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class Loader implements Runnable {
	
	Sng3 p5;
	boolean loaded;
	
	public Loader(Sng3 p5){
		this.p5 = p5;
		//p5.loaded = false;
	}

	@Override
	public void run() {
		
		p5.symbol_cols = new int[4][5][2];
		int white = p5.color(255);
		int black = p5.color(0);
		int red = p5.color(242, 55, 55);
		int green = p5.color(59, 177, 74);
		int yellow = p5.color(238, 232, 16);
		int orange = p5.color(245, 120, 52);
		int blue = p5.color(51, 160, 217);
		int purple = p5.color(184, 86, 160);
		int light_purple = p5.color(182, 110, 189);
		int red_orange = p5.color(245, 88, 52);
		int [][]second = {{white, black}, {red, white}, {green, red}, {black, yellow}, {white, orange}};
		int [][]third = {{white, black}, {blue, orange}, {purple, white}, {black, yellow}, {white, red}};
		int [][]forth = {{white, black}, {white, red_orange}, {white, green}, {white, yellow}, {white, light_purple}};
		int [][]first = {{white, black}, {blue, orange}, {red, green}, {yellow, purple}, {black, white}};
		p5.symbol_cols[0] = first;
		p5.symbol_cols[1] = second;
		p5.symbol_cols[2] = third;
		p5.symbol_cols[3] = forth;
		
		Insets in = p5.frame.getInsets();
		p5.frameTop = in.top;
		p5.frameSides = in.left + in.right;
		p5.frameBottom = in.bottom;
		p5.border = p5.width/20;
		p5.smallborder = p5.width/30;
		p5.simage_w = (p5.width/2)-(int)(p5.smallborder*1.5);
		p5.simage_h = ((p5.height-p5.frameTop)/2)-(int)(p5.smallborder*1.5);
		p5.half_w = p5.width/2;
		p5.half_h = (p5.height- p5.frameTop)/2;

		
		p5.smooth();
		p5.noStroke();
		
		
		
		p5.stitch = new Stitcher(p5);
		JSONArray categories = p5.loadJSONArray("actual.json");
		int count = 0;
		for(int i=0; i<categories.size(); i++){
			JSONObject category = categories.getJSONObject(i);
			JSONArray pals = category.getJSONArray("palettes");
			count += pals.size();
		}
		p5.palettes = new ArrayList<Palette>(count);
		count = 0;
		for(int i=0; i<categories.size(); i++ ){
			
			JSONObject category = categories.getJSONObject(i);
			JSONArray pals = category.getJSONArray("palettes");
			
			for(int j=0; j<pals.size(); j++){
				
				JSONObject pal = pals.getJSONObject(j);
				JSONArray cols = pal.getJSONArray("flosses");
				int [] thisPal = new int [cols.size()];
				
				for(int k=0; k<thisPal.length; k++){
					thisPal[k] = PApplet.unhex(cols.getString(k));
				}
				
				String name = pal.getString("name");
				Palette palette = new Palette(p5, thisPal, name, p5.loadImage(pal.getString("url")));
				p5.palettes.add(palette);
				count++;
				
			}
			
		}
		
		//add previously saved palettes
		
		p5.allusers = p5.loadJSONArray("NO_EDIT/user.json");
		
		int number = Math.min(6, p5.allusers.size());
		
		for(int i=p5.allusers.size()-number; i< p5.allusers.size(); i++, count++){
			
			JSONObject pal = p5.allusers.getJSONObject(i);
			JSONArray cols = pal.getJSONArray("flosses");
			Floss [] flosses = new Floss[cols.size()];
			
			for(int k=0; k<cols.size(); k++){
				
				String str = cols.getString(k);
				Floss f;
				
				if(str.contains(":")){
					//f = new RestrictedFloss(this, str);	
					f = new Floss(p5, str);
					
				}else{
					f = new Floss(p5, new Integer( PApplet.unhex(str) ));
				}
					
				flosses[k] = f;
		
			}
			
			String name = pal.getString("name");
			p5.palettes.add(new Palette(p5, flosses, name, p5.loadImage(pal.getString("url"))));
		}
		
		p5.gui = new GUI(p5, 600, 400 + p5.frameTop);
		Frame f = new Frame("control window");
		f.add(p5.gui);
		p5.gui.init();
		f.setTitle("S'n'G controls");
		f.setSize(p5.gui.w, p5.gui.h);
		f.setLocation(100, 100);
		f.setResizable(false);
		f.setVisible(true);
						
		p5.symbols = new PShape[200];
						
		//load 200 symbols
		for(int i=0; i<200; i++){
			p5.symbols[i] = p5.loadShape("complex/symbol"+i+".svg");
		}
				
		p5.thankyou = p5.loadShape("assets/thankyou.svg");
		p5.pfont = p5.createFont("Arial",18,true);
		p5.brokenImg = p5.loadShape("assets/broken.svg");
	
		loaded = true;
	}

}
