package ie.cognate.sng;

import java.io.PrintWriter;

import processing.core.*;
import processing.pdf.PGraphicsPDF;

public class CompleteImage{

	boolean simple, pattern, verysimple;
	PImage img;
	Palette used;
	Floss [] inOrder;
	int mode, done;
	Sng3 p5;
	
	
	public CompleteImage(Sng3 p5, PImage img, Palette used, int mode){
		this.used = used;
		this.p5 = p5;
		inOrder = used.inOrder(mode);
		simple = (used.size <= 100);
		verysimple = (used.size <= 20); //only needed for b+w patterns
		//this.img = img;
		this.mode = mode;
		pattern = true;
		
		this.img = new PImage(img.width, img.height, PConstants.ARGB);
		this.img.loadPixels(); img.loadPixels();
		for(int i=0; i<img.pixels.length; i++){
			this.img.pixels[i] = img.pixels[i];
		}
		this.img.updatePixels(); img.updatePixels();
		
		for(int i=0; i<inOrder.length; i++){
			inOrder[i].setDraw(used.size, i);
		}
		
	}
	
	public CompleteImage(PImage img){
		//this.img = img;
		simple = false;
		verysimple = false;
		pattern = false;
		this.img.loadPixels(); img.loadPixels();
		for(int i=0; i<img.pixels.length; i++){
			this.img.pixels[i] = img.pixels[i];
		}
		this.img.updatePixels(); img.updatePixels();
	}
	
	public void export(int code, String name, String time){
		
		while(!p5.l.loaded){}
		int num = 0;
		if(code%SngModes.CSV==0){
			exportCSV(name, time);
			num++;
		}
		if(code%SngModes.PDF==0){
			exportPDF(name, time);
			num++;
		}
		if(code%SngModes.SINGLE_BW==0){
			exportPattern(true, name, time);
			num+=3;
		}else if(code%SngModes.MULTI_BW==0){
			exportPattern(false, name, time);
			num+=3;
		}else if(code%SngModes.SINGLE_COL==0){
			int [][] cols = p5.gui.getExportColors();
			exportPattern(true, cols, name, time);
			num+=3;
		}else if(code%SngModes.MULTI_COL==0){
			int [][] cols = p5.gui.getExportColors();
			exportPattern(false, cols, name, time);
			num+=3;
		}else if(code%SngModes.TIFF==0){
			exportTIFF(name, time);
			num++;
		}
		if(num==1)
			p5.gui.writeMessage("preparing "+num+" export");
		else
			p5.gui.writeMessage("preparing "+num+" exports");

	}
	
	void exportPattern(boolean single, String path, String time){
		//image and colorlist always done
		exportTIFF(path, time);
		PatternExport pat = new PatternExport(single, path, time);
		new Thread(pat).start();
	}
	
	void exportPattern(boolean single, int[][]colors, String path, String time){
		//image and colorlist always done
		exportTIFF(path, time);
		PatternExport pat = new PatternExport(single, colors, path, time);
		new Thread(pat).start();	
	}
	
	public void exportCSV(String path, String time){
		
		CSVExport csv = new CSVExport(path, time);
		new Thread(csv).start();	
		
	}
	public void exportPDF(String path, String time){
		
		PDFExport pdf = new PDFExport(path, time);
		new Thread(pdf).start();
		
	}
	
	public void exportTIFF(String path, String time){
		img.save(path+"/image-"+time+".tiff");
		String [] parts = path.split("/");
		String shortname = parts[parts.length-2]+"/"+parts[parts.length-1]+"/image-"+time+".tiff";
		p5.gui.writeMessage("image saved at "+shortname);
		done++;
	}
	
	class PatternExport implements Runnable{
		
		boolean color, single;
		int[][]sym_colors;
		String name, time;
		int size = 10;
		PGraphicsPDF pdf;
		
		PatternExport(boolean single, int[][]colors, String path, String time){
			color = true;
			this.single = single;
			name = path;
			sym_colors = colors;
			this.time = time;
		}
		
		PatternExport(boolean single,  String path, String time){
			color = false;
			this.single = single;
			name = path;
			this.time = time;
			
		}
		
		public void run(){
			
			setSize(size);
			if(single){
				makeSingle();
			}else{
				makeMulti();
			}
			String [] parts = name.split("/");
			String shortname = parts[parts.length-2]+"/"+parts[parts.length-1]+"/pattern-"+time+".pdf";
			p5.gui.writeMessage("pattern complete at "+shortname);
			done++;
			setSize(size*2);
			makeColorList();
			done++;
			
		}
		
		void setSize(int size){
			for(int i=0; i<inOrder.length; i++){
				if(!color){
					inOrder[i].setSpecific(size);
				}else{
					inOrder[i].setSpecific(sym_colors, size);
				}
			}
		}
		
		void makeColorList(){
			
			int margin = 50;
			int colsX = margin;
			int colsY = margin;
			int rectwidth = 25;
			int rectheight = 20;
			boolean column1 = true;
			
			PGraphicsPDF colsNeeded = (PGraphicsPDF) p5.createGraphics(margin*2+700, 1131, PConstants.PDF, name+"/colorList-"+time+".pdf"); 
			colsNeeded.beginDraw();
			colsNeeded.smooth();
			colsNeeded.textFont(p5.pfont);
			
			for(int i=0; i<inOrder.length; i++){
				
				colsNeeded.noStroke();
				colsNeeded.fill(inOrder[i].color(mode));
				colsNeeded.rect(colsX, colsY, rectwidth, rectheight);
				inOrder[i].draw(colsNeeded, colsX+32, colsY);
				colsNeeded.fill(0);
				colsNeeded.text(inOrder[i].toString(mode)+"; No. of Stitches: "+inOrder[i].count, colsX+60, colsY+15);
				colsY += 35;
				if(colsY > colsNeeded.height-75){
					if(column1){
						colsX += colsNeeded.width/2 - 20;
					}else{
						PGraphicsPDF pdfg = (PGraphicsPDF) colsNeeded; //get renderer
						pdfg.nextPage();
						colsX = margin;
					}
					colsY = margin;
					column1 = !column1;
				}	
			}
			
			//p5.gui.writeMessage("colorlist complete at "+name+"/colorList-"+time+".pdf");
			colsNeeded.dispose();
			colsNeeded.endDraw();
	
		}
		
		
		//these are different enough to have their own methods
		
		
		void makeSingle(){
			
			int margin = 50;
			int posX = margin;
			int posY = margin;
			
			PGraphicsPDF pdf = (PGraphicsPDF) p5.createGraphics(margin*2+img.width*size, margin*2+img.height*size, PConstants.PDF, name+"/pattern-"+time+".pdf");
			pdf.beginDraw();
			pdf.smooth();
			img.loadPixels();
			
			for(int y=0; y<img.height; y++){
				posX = margin;
				for(int x=0; x<img.width; x++){

					int loc = x+y*img.width;
					Integer key = new Integer(img.pixels[loc]);
					if(used.hash.containsKey(key)){
						
						Floss f = (Floss)used.hash.get(key);
						f.draw(pdf, posX, posY);
						
					}else{					
						System.out.println("error, cound not find "+PApplet.hex(img.pixels[loc]));
					}
					
					//dividing lines
					if(x==0){//horizontal
						pdf.strokeWeight(1);
						if(y==img.height/2){//middle
							pdf.stroke(p5.color(255,0,0));
						}else if(y%5==0){//bold
							pdf.stroke(p5.color(30));
						}else{
							pdf.stroke(p5.color(100));
						}
						pdf.line(margin, posY, pdf.width-margin, posY);
					}
					if(y==0){//vertical
						pdf.strokeWeight(1);
						if(x==img.width/2){//middle
							pdf.stroke(p5.color(255,0,0));
						}else if(x%5==0){//bold
							pdf.stroke(p5.color(30));
						}else{
							pdf.stroke(p5.color(100));
						}
						pdf.line(posX, margin, posX, pdf.height-margin);
					}
					
					posX += size;
					
				}
				
				posY+= size;
			}
			
			pdf.stroke(30);
			pdf.line(posX, margin, posX, pdf.height-margin);
			pdf.line(margin, posY, pdf.width-margin, posY);
			
			pdf.dispose();
			pdf.endDraw();
			img.updatePixels();
			System.out.println("pattern finished");
			
		}
		
		void makeMulti(){
			
			int margin = 10;
			int size  =10;
			int img_top, img_bottom, img_left, img_right;
			int syms_wide = 30;
			int syms_high = 40;
			int pageNum = 0;
			boolean evenX = (img.width%syms_wide == 0); //overflows
			boolean evenY = (img.height%syms_high == 0);
			float ratio;
			
			img.loadPixels();
			
			int pagesWide = img.width/syms_wide;
			if(!evenX) pagesWide+=1;
			
			int pagesHigh = img.height/syms_high;
			if(!evenY) pagesHigh+=1;
			
			//to tell where to read from the image
			int [] startX = new int[pagesWide];
			int [] startY = new int[pagesHigh];
			
			for(int w =0; w<pagesWide; w++){ 
				startX[w] = syms_wide*w;
			}
			for(int h=0; h<pagesHigh; h++){
				startY[h] = syms_high*h; 
			}
			
			pdf = (PGraphicsPDF) p5.createGraphics(margin*2+syms_wide*size, 453, PConstants.PDF, name+"/pattern-"+time+".pdf");
			pdf.beginDraw();
			pdf.smooth();
			pdf.noFill();
			
			//title page
			pdf.shape(p5.thankyou, pdf.width-135, pdf.height-60, 125, 50);
			pdf.fill(70);
			pdf.textFont(p5.pfont);
			pdf.textSize(10);
			pdf.textAlign(PConstants.RIGHT);
			pdf.text("stitch-n-glitch.com", pdf.width-10, 20);
			pdf.imageMode(PConstants.CENTER);
			//working out the image size
			if(img.width>=img.height){ //landscape
				ratio = 200/(float)img.width;
				int amount = (int)(img.height*ratio)/2;
				img_top = pdf.height/2 - amount;
				img_bottom = pdf.height/2 + amount;
				img_right  = pdf.width/2 + 100;
				img_left  = pdf.width/2 - 100;
				pdf.image(img, pdf.width/2, pdf.height/2, 200, (int)img.height*ratio);
			}else{
				ratio = 200/(float)img.height;
				int amount = (int)(img.width*ratio)/2;
				img_top = pdf.height/2 - 100;
				img_bottom = pdf.height/2 + 100;
				img_right  = pdf.width/2 + amount;
				img_left  = pdf.width/2 - amount;
				pdf.image(img, pdf.width/2, pdf.height/2, (int)img.width*ratio, 200);
			}
			
			float page_sizeX = syms_wide*ratio;
		    float page_sizeY = syms_high*ratio;
		    float plocY = (float)img_top+page_sizeY;
		    float plocX = (float)img_left+page_sizeX;
		    
		    //draw the dividing lines
		    for(int pX = 0; pX < pagesWide-1; pX++){     
		        pdf.stroke(0,255,255);
		        pdf.line(plocX, img_top-10, plocX, img_bottom+10);
		        plocX += page_sizeX;
		     }
		     for(int pY = 0; pY < pagesHigh-1; pY++){  
		        pdf.stroke(0,255,255);
		        pdf.line(img_left-10, plocY, img_right+10, plocY);
		        plocY += page_sizeY;
		     }
		     
		     pdf.nextPage();
		     
		    
		    //each page
		    for(int pageY = 0; pageY<pagesHigh; pageY++){	
		    	for(int pageX=0; pageX<pagesWide; pageX++){
		    		
		    		int locX = startX[pageX];
		    		int locY = startY[pageY];
		    	//	System.out.println("starting page "+(pageNum+1)+" at pixel "+locX+", "+locY);
		    		int posX = 10;
		    		int posY = 40;
		    		int max_x = locX;
		    		int max_y = locY;
		    		pdf.fill(70);
		    		pdf.text("Page "+(pageNum+1), pdf.width-10, 20);
		    		pdf.noFill();
		    		if(!evenX){
		    			if(pageX==pagesWide -1)//last page
		    				max_x += img.width%syms_wide;
		    			else
		    				max_x += syms_wide;
		    		}else{
		    			max_x += syms_wide;
		    		}
		    		if(!evenY){
		    			if(pageY == pagesHigh -1)//last page
		    				max_y += img.height%syms_high;
		    			else
		    				max_y += syms_high;
		    		}else{
		    			max_y += syms_high;
		    		}
		    		
		    		//now draw the symbols
		    		for(int y = locY; y< max_y; y++){
		    			posX = 10;
		    			for(int x=locX; x<max_x; x++){
		    				
		    				int loc = x+y*img.width;
		    				Integer key = new Integer(img.pixels[loc]);
							if(used.hash.containsKey(key)){
								
								Floss f = (Floss)used.hash.get(key);
								f.draw(pdf, posX, posY);
								
							}else{
								System.out.println("error, cound not find "+PApplet.hex(img.pixels[loc]));
							}
							
							if(x == locX){
		    					pdf.strokeWeight(1);
		    					if(y == img.height/2){
		    						pdf.stroke(255,0,0);
		    					}else if(y%5 ==0){
		    						pdf.stroke(30);
		    					}else{
		    						pdf.stroke(100);
		    					}
		    					pdf.line(10, posY, (max_x-locX)*size + 10, posY);
		    				}
		    				
		    				if(y == locY){
		    					pdf.strokeWeight(1);
		    					if(x == img.width/2){
		    						pdf.stroke(255,0,0);
		    					}else if(x%5 ==0){
		    						pdf.stroke(30);
		    					}else{
		    						pdf.stroke(100);
		    					}
		    					pdf.line(posX, 40, posX, (max_y-locY)*size + 40);
		    				}

		    				posX += size;
		    			}
		    			posY += size;
		    		}
		    		
		    		pdf.stroke(30);
		    		pdf.line(posX, 40, posX, (max_y-locY)*size + 40);
					pdf.line(10, posY, (max_x-locX)*size + 10, posY);
		    		
		    		pageNum++;
		    		if(pageNum != pagesWide*pagesHigh){
		    			nextPage();
		    		}
		    		
		    	}
		    }
		    
		    pdf.dispose();
		    pdf.endDraw();
		    img.updatePixels();
		    System.out.println("pattern finished");
		}
		
		void nextPage(){	     
			PGraphicsPDF pdfg = (PGraphicsPDF) pdf;
			pdfg.nextPage();
		}
	}//end of Pattern Export class	
	
	
	class PDFExport implements Runnable{

		String name, time;
		
		PDFExport(String path, String time){
			name = path;
			this.time = time;
		}
		
		public void run(){
			
			int margin = 10;
			PGraphicsPDF pdf = (PGraphicsPDF) p5.createGraphics(margin*2+img.width, margin*2+img.height, PConstants.PDF, name+"/vector-"+time+".pdf");
			String [] parts = name.split("/");
			String shortname = parts[parts.length-2]+"/"+parts[parts.length-1]+"/vector-"+time+".pdf";
			img.loadPixels();
			int posX = margin;
			int posY = margin;
			pdf.beginDraw();
			pdf.smooth();
			pdf.noStroke();
			
			for(int y=0; y<img.height; y++){
				posX = margin; 
				for(int x = 0; x<img.width; x++){
					int loc = x+y*img.width;
					pdf.fill(img.pixels[loc]);
					pdf.rect(posX, posY, 1, 1);
					posX++;
				}
				posY++;
			}
			
			img.updatePixels();
			pdf.dispose();
			pdf.endDraw();
			p5.gui.writeMessage("completed pdf image at: "+shortname);
			done++;
		}
		
	}
	
	class CSVExport implements Runnable{

		String name, time;
		
		CSVExport(String path, String time){
			name = path;
			this.time = time;
		}
		
		public void run(){
			
			PrintWriter output = p5.createWriter(name+"/data-"+time+".csv");
			String [] parts = name.split("/");
			String shortname = parts[parts.length-2]+"/"+parts[parts.length-1]+"/data-"+time+".csv";
			img.loadPixels();
			for(int y=0; y<img.height; y++){
				for(int x = 0; x<img.width; x++){
					int loc = x+y*img.width;
					output.print(PApplet.hex(img.pixels[loc])+",");
				}
				output.println("");
			}
			img.updatePixels();
			output.flush();
			output.close();
			p5.gui.writeMessage("made csv file at: "+shortname);
			done++;
		}
		
	}
	
}
