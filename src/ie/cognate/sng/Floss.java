package ie.cognate.sng;

import processing.core.*;
import processing.pdf.*;
import ie.cognate.Modes;
import ie.cognate.Stitcher;

public class Floss implements SngModes{
	
	Integer key;
	PShape sym = null;
	Sng3 sng;
	int count, order, sym_size, sym_num, res_mode, res_color;
	int [] sym_color = new int[2];
	float unit;
	String [] id;
	String res_id;
	int [] brandColor;
	boolean color, verysimple, simple; 
	final boolean restricted;
	
	public Floss (Sng3 sng, Integer key){
		this.sng = sng;
		Stitcher st = sng.stitch;
		
		count = 0;
		sym_color[0] = sng.color(255);
		sym_color[1] = sng.color(0);//black and white by default;

		this.key = key;
		id = new String[8];
		brandColor = new int[8];
		for(int mode=0; mode<7; mode++){
			id[mode] = st.getClosestID(key.intValue(), mode);
			brandColor[mode] = st.getColor(id[mode]);
		}
		id[7] = PApplet.hex(key.intValue());
		brandColor[7] = key.intValue();
		
		restricted = false;
		
	}
	
	//the old Restricted floss constructor
	public Floss (Sng3 sng, String id){
		
		Stitcher st = sng.stitch;
		
		res_id = id;	
		String [] brandCode = id.split(" : ");
		if(brandCode[0].equals("dmc")){
			res_mode = Modes.DMC;
		}else if(brandCode[0].equals("anchor")){
			res_mode = Modes.ANCHOR;
		}else{
			res_mode = Modes.MADEIRA;
		}
		res_color = st.getColor(id);
		key = new Integer(res_color);
		count = 0;
		
		restricted = true;
	}
	
	/**
	 * Compare flosses based on the amount the have been used (reverse, low to high)
	 * @param f Floss to be compare with
	 * @return int >0 if used less, 0 if used the same, <0 if used more 
	 */
	/*
	public int compareTo(Floss f){	
		return f.count - count;
	}
	*/
	
	public int color(int mode){		
		count++;
		if(restricted)
			return res_color;
		return brandColor[mode];
	}
	
	public String id(int mode){
		if(restricted)
			return res_id;
		return id[mode];
	}
	
	public void reset(){
		count = 0;
	}
	
	public void setShape(PShape sym){
		this.sym = sym;
	}
	
	public void setDraw(int pal_size, int order){
		verysimple = (pal_size <= 20);
		simple = (pal_size <= 100);
		this.order = order;
		if(verysimple){
			sym_num = order;
		}else{
			sym = sng.symbols[order];
			if(simple)
				sym_num = order%20;
		}
	}
	
	public void setSpecific(int [][] colors, int size){
		
		int num = order/20;
		sym_color = colors[((sym_num%colors.length)+num)%colors.length];
		sym_size = size;
		unit = size/5.0f;
		color = true;
	}
	
	//black and white settings
	public void setSpecific(int size){
		sym_color[0] = sng.color(255);
		sym_color[1] = sng.color(0);//black and white
		sym_size = size;
		unit = size/5.0f;
		color = false;
	}
	
	void draw(PGraphicsPDF pdf, int x, int y){
		if(color || verysimple){
			drawSIMPLE(pdf, x, y);
		}else{
			drawBW(pdf, x, y);
		}
	}
	
	void drawBW(PGraphicsPDF pdf, int x, int y){
		pdf.fill(0);
		pdf.noStroke();
		pdf.shape(sym, x, y, sym_size, sym_size);
	}
	
	void drawSIMPLE(PGraphicsPDF pdf, int x, int y){
		
		if(sym_color[0] != sng.color(255)){
			pdf.noStroke();
			pdf.fill(sym_color[0]);
			pdf.rect(x, y, sym_size, sym_size);
		}
		
		pdf.fill(sym_color[1]);
		pdf.stroke(sym_color[1]);
		
		switch (sym_num) {
		case 0:
			//x
			pdf.noFill();	
			pdf.strokeWeight(unit/2.0f);
			pdf.strokeCap(PConstants.ROUND);
			pdf.line(x+unit, y+unit, x+sym_size-unit, y+sym_size-unit);
			pdf.line(x+sym_size-unit, y+unit, x+unit, y+sym_size-unit);
			break;
		case 1:
			//circle
			pdf.noStroke();
			pdf.ellipseMode(PConstants.CENTER);
			pdf.ellipse(x+sym_size/2.0f, y+sym_size/2.0f, unit*4, unit*4);
			break;
		case 2:
			//#
			pdf.noFill();
			pdf.strokeCap(PConstants.SQUARE);
			pdf.strokeWeight(unit/3f);
			pdf.line(x+unit*2.5f, y+unit, x+unit*1.5f, y+sym_size-unit);
			pdf.line(x+sym_size-unit*1.5f, y+unit, x+unit*2.5f, y+sym_size-unit);
			pdf.line(x+unit, y+unit*2f, x+sym_size-unit,  y+unit*2f);
			pdf.line(x+unit, y+unit*3, x+sym_size-unit,  y+unit*3);
			break;
		case 3 :
			//tick
			pdf.noFill();
			pdf.strokeWeight(unit);
			pdf.strokeCap(PConstants.ROUND);
			pdf.line(x+unit, y+unit*3, x+unit*1.5f, y+sym_size-unit);
			//pdf.strokeWeight(unit/1.5);
			pdf.line(x+unit*1.5f, y+sym_size-unit, x+sym_size-unit, y+unit);
			break;
		case 4:
			//pdf.triangle
			pdf.noStroke();
			pdf.triangle(x+sym_size/2.0f, y+unit, x+unit, y+sym_size-unit, x+sym_size-unit, y+sym_size-unit);
			break;
		case 5:
			//star
			pdf.noStroke();
			pdf.triangle(x+sym_size/2.0f, y+unit/2, x+unit, y+sym_size-unit*1.5f, x+sym_size-unit, y+sym_size-unit*1.5f);
			pdf.triangle(x+sym_size/2.0f, y+sym_size-unit/2f, x+unit, y+unit*1.5f, x+sym_size-unit, y+unit*1.5f);
			break;
		case 6:
			//''
			pdf.noFill();
			pdf.strokeWeight(unit/2f);
			pdf.strokeCap(PConstants.ROUND);
			pdf.arc(x+unit*2, y+sym_size/2, unit*2, unit*3, PConstants.PI, PConstants.PI+PConstants.HALF_PI);
			pdf.arc(x+sym_size-unit, y+sym_size/2, unit*2, unit*3, PConstants.PI, PConstants.PI+PConstants.HALF_PI);
			pdf.fill(sym_color[1]);
			pdf.rect(x+unit, y+sym_size/2f, unit, unit*1.5f);
			pdf.rect(x+unit*3, y+sym_size/2f, unit, unit*1.5f);
			break;
		case 7:
			//square
			pdf.noStroke();
			pdf.rect(x+unit, y+unit, unit*3, unit*3);
			break;
		case 8:
			//ring with hole
			pdf.strokeWeight(unit/3);
			pdf.ellipseMode(PConstants.CENTER);
			pdf.noFill();
			pdf.ellipse(x+sym_size/2, y+sym_size/2, unit*3, unit*3);
			pdf.noStroke();
			pdf.fill(sym_color[1]);
			pdf.ellipse(x+sym_size/2f, y+sym_size/2, unit*1.5f, unit*1.5f);
			break;
		case 9:
			//:)
			pdf.noStroke();
			pdf.ellipseMode(PConstants.CENTER);
			pdf.ellipse(x+unit*2, y+unit*1.5f, unit/2f, unit);
			pdf.ellipse(x+unit*3, y+unit*1.5f, unit/2f, unit);
			pdf.stroke(sym_color[1]);
			pdf.strokeCap(PConstants.ROUND);
			pdf.strokeWeight(unit/3);
			pdf.noFill();
			pdf.arc(x+sym_size/2f, y+unit*2, unit*3, unit*4, 0, PConstants.PI);
			break;
		case 10:
			//hollow square
			pdf.noFill();
			pdf.strokeWeight(unit/3);
			pdf.rect(x+unit, y+unit, unit*3, unit*3);
			break;
		case 11:
			//*
			pdf.pushMatrix();
			pdf.translate(x+sym_size/2f, y+sym_size/2f);
			pdf.strokeWeight(unit/3f);
			pdf.strokeCap(PConstants.SQUARE);
			pdf.line(0, -unit/2f, 0, -unit*2);
			pdf.rotate(PApplet.radians(60));
			pdf.line(0, -unit/2f, 0, -unit*2);
			pdf.rotate(PApplet.radians(60));
			pdf.line(0, -unit/2f, 0, -unit*2);
			pdf.rotate(PApplet.radians(60));
			pdf.line(0, -unit/2f, 0, -unit*2);
			pdf.rotate(PApplet.radians(60));
			pdf.line(0, -unit/2f, 0, -unit*2);
			pdf.rotate(PApplet.radians(60));
			pdf.line(0, -unit/2f, 0, -unit*2);
			pdf.popMatrix();
			break;
		case 12:
			//pointer
			pdf.noStroke();
			pdf.triangle(x+unit, y+unit, x+unit*2, y+sym_size-unit*1.5f, x+sym_size-unit*1.5f, y+unit*2);
			pdf.stroke(sym_color[1]);
			pdf.strokeWeight(unit/3);
			pdf.strokeCap(PConstants.PROJECT);
			pdf.line(x+sym_size/2, y+sym_size/2, x+sym_size-unit, y+sym_size-unit);
			break;
		case 13:
			//2 squares
			pdf.noStroke();
			pdf.rect(x+sym_size/2f, y+unit, unit*1.5f, unit*1.5f);
			pdf.rect(x+unit, y+sym_size/2f, unit*1.5f, unit*1.5f);
			break;
		case 14:
			//vertical pdf.lines
			pdf.strokeCap(PConstants.PROJECT);
			pdf.strokeWeight(unit/3f);
			pdf.line(x+unit, y+unit*2, x+unit, y+unit*3);
			pdf.line(x+unit*1.75f, y+unit*1.5f, x+unit*1.75f, y+sym_size-unit*1.5f);
			pdf.line(x+sym_size/2, y+unit, x+sym_size/2, y+sym_size-unit);
			pdf.line(x+sym_size-unit*1.75f, y+unit*1.5f, x+sym_size-unit*1.75f, y+sym_size-unit*1.5f);
			pdf.line(x+sym_size-unit, y+unit*2, x+sym_size-unit, y+unit*3);
			break;
		case 15:
			//ae
			pdf.noFill();
			pdf.strokeCap(PConstants.ROUND);
			pdf.strokeWeight(unit/3);
			pdf.bezier(x+unit, y+unit, x+sym_size-unit, y+unit, x+unit, y+sym_size-unit, x+sym_size-unit, y+sym_size-unit);
			pdf.line(x+unit/2f, y+sym_size/2f, x+sym_size-unit/2f, y+sym_size/2f);
			pdf.arc(x+unit*1.5f, y+sym_size/2f, unit*2, unit*3, 0, PConstants.PI);
			pdf.arc(x+sym_size-unit*1.5f, y+sym_size/2f, unit*2, unit*3, PConstants.PI, PConstants.TWO_PI);
			break;
		case 16:
			//+
			pdf.noFill();
			pdf.strokeWeight(unit);
			pdf.strokeCap(PConstants.SQUARE);
			pdf.line(x+sym_size/2.0f, y+unit, x+sym_size/2.0f, y+sym_size-unit);
			pdf.line(x+unit, y+sym_size/2.0f, x+sym_size-unit, y+sym_size/2.0f);	
			break;	
			
		case 17:
			//4 squares
			pdf.noStroke();
			pdf.pushMatrix();
			pdf.translate(x, y);
			pdf.rotate(PApplet.radians(135));
			pdf.rect(unit/4, -unit*5, unit*1.25f, unit*1.25f);
			pdf.rect(-unit*1.5f, -unit*5, unit*1.25f, unit*1.25f);
			pdf.rect(unit/4f, -unit*3.25f, unit*1.25f, unit*1.25f);
			pdf.rect(-unit*1.5f, -unit*3.25f, unit*1.25f, unit*1.25f);
			pdf.popMatrix();
			break;
		case 18:
			//hollow pdf.triangle
			pdf.noFill();
			pdf.strokeWeight(unit/3.f);
			pdf.strokeCap(PConstants.ROUND);
			pdf.triangle(x+unit, y+unit, x+unit, y+sym_size-unit, x+sym_size-unit, y+sym_size/2);
			break;
		default :
			//!
			pdf.noStroke();
			pdf.quad(x+unit*1.5f, y+unit, x+sym_size-unit*1.5f, y+unit, x+unit*3, y+unit*3, x+unit*2, y+unit*3);
			pdf.rect(x+unit*2, y+sym_size-unit*1.5f, unit, unit/2f);
			
			
	}
		
		
	}
	
	@Override
	public String toString(){
		if(restricted)
			return res_id;
		else
			return PApplet.hex(brandColor[SngModes.ACTUAL], 8);
	}
	
	public String toString(int mode){
		if(restricted){
			System.out.println("restricted : printing "+res_id);
			return res_id;
		}else{
			System.out.println("printing "+id[mode]);
			return id[mode];
		}
	}

	
	
}
