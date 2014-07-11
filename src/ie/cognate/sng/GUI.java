package ie.cognate.sng;

import controlP5.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PGraphics;
import ie.cognate.Stitcher;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class GUI extends PApplet implements SngModes{
	
	final int DMC = 0;
	final int ANCHOR = 1;
	final int MADEIRA = 2;
	final int ACTUAL = 3;
	
	Sng3 p5;
	ControlP5 cp5;
	Stitcher st;
	Group preset_group, palette_first, palette_second, bwpattern, colorpattern, anyexport;
	Slider color_amt, glitch_amt, hue_slide;
	RadioButton dither_radio, choose_image, pattern_options;
	//Slider2D picker;
	Picker picker;
	Button glitch, export, update, dynamic, preset, review, savePal, back;
	Button [] swatch;
	CColor magenta_cc;
	Textarea txt;
	Textfield search_box, name_box, export_name;
	Textlabel search_label, patternoption;
	Textarea [] message = new Textarea[3];
	Textarea messageDefault, messagePalette, messageExport;
	MultiList palette_list, symbol_list;
	MultiListButton [] categories, symbol_colors;
	MultiListButton current_colors;
	PImage [] sym_img;
	int count, nextposition, symcolors;
	DropdownList palette, symbols, search;
	TempPalette tempPal;
	CheckBox brand, non_patterns;
	PFont big_font, medium_font, small_font, very_small;
	int orange_passive, orange_active, bg_grey, w, h;
	int red, green, green_dark, ditherType, paletteType, mode, black, white, magenta, magenta_dark, cyan, cyan_dark, yellow, yellow_dark;
	boolean images_loaded;
	String exportName = "default";
	
	public GUI(Sng3 parent, int theWidth, int theHeight){
		p5 = parent;
		w = theWidth;
		h = theHeight;
		st = p5.stitch;
		
	}
	
	public void setup(){
		size(w, h);
		colorMode(PConstants.HSB, 360, 100, 100, 100);
		rectMode(CENTER);
		frameRate(25);
		cp5 = new ControlP5(this);

		nextposition = 90;
		magenta = color(300, 100, 100);
		magenta_dark = color(300, 100, 90);
		cyan = color(180,100,100);
		cyan_dark = color(180,100,90);
		yellow = color(60,100,100);
		yellow_dark = color(60,100,90);
		black = color(0,0,0);
		white = color(0,0,100);
		red = color(0,100,100);
		green = color(120, 100, 100);
		green_dark = color(120, 100, 90);
		background(white);
		magenta_cc = new CColor(magenta_dark, black,magenta, black, white);
		cp5.setColor(magenta_cc);
		big_font = createFont("Courier", 24, true);
		medium_font = createFont("Courier", 18, true);
		small_font = createFont("Courier", 14,true);
		very_small = createFont("Courier", 10, true);
		tempPal = new TempPalette();
		
		cp5.setFont(small_font);
		
		cp5.addTab("Palette")
		.setColorForeground(cyan_dark)
		.setColorActive(cyan)
		.activateEvent(true)
		.setValue(2)
	    .setWidth((width/3)-3)
	    .setHeight(40)
	    .setColorLabel(white)
		.getCaptionLabel().setFont(big_font);
		
		cp5.addTab("Export")
		.setColorForeground(green_dark)
		.setColorActive(green)
		.activateEvent(true)
	    .setValue(3)
	    .setWidth(width/3)
	    .setHeight(40)
	    .setColorLabel(white)
		.getCaptionLabel().setFont(big_font);

		cp5.getTab("default")
		.activateEvent(true)
		.setLabel("Glitch")
		.setValue(1)
		.setWidth((width/3)-3)
		.setHeight(40)
		.setColorLabel(white)
		.getCaptionLabel().setFont(big_font);
		
		cp5.getTab("Palette").getCaptionLabel().setColor(black);
		cp5.getTab("Export").getCaptionLabel().setColor(black);
		
		preset_group = cp5.addGroup("pg")
				.hideBar();
		
		for(int i=0; i<3; i++){
			message[i] = cp5.addTextarea("message"+i)
					.setWidth(w)
					.setHeight(40)
					.setPosition(0, 360)
					.setLineHeight(20)
					;
			
			message[i].getValueLabel().setFont(medium_font).setColor(black);

		}
		message[0].setTab("default");
		message[1].setTab("Palette");
		message[2].setTab("Export");
		
		color_amt = cp5.addSlider("max. number of colours")
				.setRange(2, 200)
				.setPosition(20,130)
				.setSize(330,40)
				.setDecimalPrecision(0)
				.setValue(25);
		
		dither_radio = cp5.addRadioButton("dither type")
				.setPosition(410,60)
				.setItemWidth(15)
				.setItemHeight(15)
				.setSpacingRow(5)
				.addItem("no dither", 0)
				.addItem("floyd steinberg", 1)
				.addItem("atkinson", 2)
				.addItem("jarvis judice ninke", 3)
				.addItem("stucki", 4)
				.addItem("clustered", 5)
				.addItem("bayer 2", 6)
				.addItem("bayer 4", 7)
				.addItem("bayer 8", 8)
				.addItem("randomised", 9)
				.setColorLabel(black)
				.activate(0)
                .setNoneSelectedAllowed(false);

		brand = cp5.addCheckBox("brand")
				.setPosition(65,300)
				.setItemWidth(20)
				.setItemHeight(20)
				.addItem("dmc", 0)
				.addItem("anchor", 1)
				.addItem("madeira", 2)
				.setColorLabel(black)
				.activate(0)
				.setItemsPerRow(3)
				.setSpacingColumn(50);
		
		List<Toggle>bi = brand.getItems();
		for(int i=0; i<bi.size(); i++){
			Toggle t = (Toggle)bi.get(i);
			t.getCaptionLabel().align(ControlP5.CENTER, ControlP5.TOP_OUTSIDE);
		}
		
		
		glitch_amt = cp5.addSlider("glitch amount")
				.setRange(0, 30)
				.setPosition(310, 300)
				.setSize(280, 20)
				.setDecimalPrecision(0)
				.setValue(5);
		
		glitch = cp5.addButton("glitch")
			     .setPosition(350,330)
			     .setSize(200,30);
		update = cp5.addButton("update")
				.setPosition(50, 330)
				.setSize(200, 30);
		
		color_amt.getCaptionLabel().setColor(black).align(ControlP5.CENTER, ControlP5.TOP_OUTSIDE);
		color_amt.getValueLabel().alignX(ControlP5.RIGHT);
		glitch_amt.getCaptionLabel().setColor(black).align(ControlP5.CENTER, ControlP5.TOP_OUTSIDE);
		glitch_amt.getValueLabel().alignX(ControlP5.RIGHT);
		
		
		glitch.getCaptionLabel().setFont(medium_font).alignX(ControlP5.CENTER);
		update.getCaptionLabel().setFont(medium_font).alignX(ControlP5.CENTER);
		
		
		
		palette_list = cp5.addMultiList("", 20, 90, 100, 25);
		palette_list.setGroup(preset_group);
		
		categories = new MultiListButton[7];
		count = 0;
		for(int i=0; i<categories.length; i++){
			String label;
			int num = 7;
			
			switch(i){
			case 0:
				label = "monochrome"; break;
			case 1:
				label = "8-colours"; break;
			case 2:
				label = "6-colours"; break;
			case 3:
				label = "5-colours"; break;
			case 4:
				label = "3-colours"; break;
			case 5:
				label = "monitor"; break;
			default:
				label = "user"; num=p5.palettes.size()-42; 
			}
			categories[i] = palette_list.add(label,i);
			
			MultiListButton [] pals = new MultiListButton[num];
			nextposition = 90;
			for(int j=0; j<num; j++){
				//pals[j] = addPaletteButton(p5.palettes[count], j, count); 
				Palette p = (Palette)p5.palettes.get(count);
				pals[j] = categories[i].add(p.name, count);
				pals[j].add(i+"img"+j,count).setImage(p.img).setPosition(222, nextposition);
				pals[j].setColorBackground(magenta_dark)
					.setColorForeground(magenta);
				pals[j].setPosition(121, nextposition);
				pals[j].getCaptionLabel().setFont(very_small);
				count++;
				nextposition+=26;
			}
			
			
		}
		
		dynamic = cp5.addButton("dynamic")
				.setPosition(80, 50)
				.setSize(90, 30)
				.setColorBackground(magenta);
		preset = cp5.addButton("preset")
				.setPosition(225, 50)
				.setSize(90, 30);
		dynamic.getCaptionLabel().alignX(ControlP5.CENTER);
		preset.getCaptionLabel().alignX(ControlP5.CENTER);
		preset_group.hide();
		
		//testing
		ditherType = 0;
		paletteType = 0;
		mode = 0;
		
		
		//palette
		
		palette_first = cp5.addGroup("pf")
				.setTab("Palette")
				.hideBar();
		
		
		swatch = new Button[4];
		for(int i=0; i<4; i++){
			swatch[i] = cp5.addButton("swatch_"+i)
					.setPosition(410, 90+(60*i))
					.setSize(180, 30)
					.setGroup("pf")
					.setColorForeground(cyan);
			swatch[i].getCaptionLabel()
					.setPaddingY(5)
					.setColor(black)
					.align(ControlP5.CENTER, ControlP5.TOP_OUTSIDE);
			cp5.getTooltip().register(swatch[i], "add to palette");
		}
		
		
		hue_slide = cp5.addSlider("hue_slider")
				.setPosition(210, 280)
				.setSize(180, 10)
				.setRange(0,360)
				.setSliderMode(Slider.FLEXIBLE)
				.setColorActive(cyan)
				.setHandleSize(20)
				.setColorForeground(black)	
				.setColorBackground(white)
				.setCaptionLabel("HUE: "+180+" DEGREES")
				.setGroup("pf")
				.setValue(180)
				;
		
		hue_slide.getValueLabel().hide();
		
		hue_slide.getCaptionLabel()
				.setPaddingY(15)
				.setColor(black)
				.align(ControlP5.CENTER, ControlP5.TOP_OUTSIDE);
		
		picker = new Picker(210, 60, 180, 180);
		
		//tooltips
		cp5.getTooltip().setColorLabel(white).setColorBackground(black);
		
		search_box = cp5.addTextfield("searchBox")
				.setPosition(10, 140)
				.setSize(180, 30)
				.setColorBackground(white)
				.setGroup("pf")
				.setColorForeground(cyan_dark)
				.setColorActive(cyan)
				.setColorCursor(black)
				.setColor(black)
				;
		cp5.getTooltip().register(search_box, "and press enter");
		
		search_box.getCaptionLabel().hide();
		
		name_box = cp5.addTextfield("name_box")
				.setPosition(10,210)
				.setSize(180, 30)
				.setColorBackground(white)
				.setGroup("pf")
				.setColorForeground(cyan_dark)
				.setColorActive(cyan)
				.setColorCursor(black)
				.setCaptionLabel("NAME THIS PALETTE:")
				.setColor(black)
				;
		
		cp5.getTooltip().register(name_box, "and press enter");
		
		name_box.getCaptionLabel()
			.setPaddingY(12)
			.setColor(black)
			.align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE);
		
		search = cp5.addDropdownList("DMC")
				.setPosition(92, 109)
				.setSize(80, 125)
				.setColorActive(cyan)
				.setGroup("pf")
				.setColorBackground(white)
				.setColorForeground(cyan)
				.setColorLabel(black)
				.setItemHeight(25)
				.setBarHeight(20)
				.setValue(DMC)
				.setColorValue(cyan)
				;
		search.addItem("DMC", DMC);
		search.addItem("ANCHOR", ANCHOR);
		search.addItem("MADEIRA", MADEIRA);
		search.addItem("HEX", ACTUAL);
		
		review = cp5.addButton("reviewSave")
			     .setPosition(10,260)
			     .setSize(180,40)
			     .setCaptionLabel("review and save")
			     .setColorForeground(cyan_dark)
			     .setColorActive(cyan)
			     .setGroup("pf");
				
		review.getCaptionLabel().alignX(ControlP5.CENTER);
		
		palette_second = cp5.addGroup("ps")
				.setTab("Palette")
				.hide()
				.hideBar();
		
		savePal = cp5.addButton("saveUse")
				.setPosition(460, 50)
				.setSize(100, 30)
				.setCaptionLabel("save + use")
				.setColorForeground(cyan_dark)
				.setColorActive(cyan)
				.setGroup("ps");
		
		back = cp5.addButton("back")
				.setPosition(370, 50)
				.setSize(70, 30)
				.setColorForeground(cyan_dark)
				.setColorActive(cyan)
				.setGroup("ps");
		
		savePal.getCaptionLabel().alignX(ControlP5.CENTER);
		back.getCaptionLabel().alignX(ControlP5.CENTER);
		
		//export tab
		choose_image = cp5.addRadioButton("choose_image")
				.setPosition(40, 80)
				.setItemWidth(50)
				.setItemHeight(50)
				.setItemsPerRow(2)
				.setSpacingRow(10)
				.setSpacingColumn(10)
				.addItem("Image 1", 0)
				.addItem("Image 2", 1)
				.addItem("Image 3", 2)
				.addItem("Image 4", 3)
				.hideLabels()
				.activate(0)
				.setColorForeground(green_dark)
				.setColorActive(green)
				.setNoneSelectedAllowed(false)
				.setCaptionLabel("CHOOSE IMAGE:")
				.setColorLabel(black)
				.setTab("Export");
		
		choose_image.getCaptionLabel().show().setColor(black).setPaddingY(10).align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE);
		
		anyexport = cp5.addGroup("ae")
				.setTab("Export")
				.hide()
				.hideBar();
		
	 colorpattern = cp5.addGroup("cp")
				.setTab("Export")
				.hide()
				.hideBar();
	 
	 bwpattern = cp5.addGroup("bw")
				.setTab("Export")
				.hide()
				.hideBar();
		
		export_name = cp5.addTextfield("export_name")
				.setPosition(30,240)
				.setSize(150, 30)
				.setColorBackground(white)
				.setColorForeground(green_dark)
				.setColorActive(green)
				.setColorCursor(black)
				.setTab("Export")
				.setColorCaptionLabel(black)
				.setCaptionLabel("NAME EXPORT\nFOLDER:")
				.setColor(black)
				.setGroup("ae")
				;
		
		cp5.getTooltip().register(export_name, "and press enter");
		
		export_name.getCaptionLabel().setColor(black).setPaddingY(17).align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE);
		
		 non_patterns = cp5.addCheckBox("non pattern options")
				 .setPosition(200,  80)
				 .setItemWidth(15)
				 .setItemHeight(15)
				 .setSpacingRow(5)
				 .addItem("PDF IMAGE", 0)
				 .addItem("CSV FILE", 1)
				 .addItem("TIFF IMAGE", 2)
				 .setColorForeground(green_dark)
				 .setColorActive(green)
				 .setColorLabels(black)
				 .setTab("Export")
				 .setGroup("ae");
		 
		 non_patterns.getCaptionLabel().setColor(black).align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE);
				 
		pattern_options = cp5.addRadioButton("pattern_options")
				.setPosition(400, 80)
				.setItemWidth(15)
				.setItemHeight(15)
				.setSpacingRow(5)
				.addItem("Multipage b+w", SngModes.MULTI_BW)
				.addItem("Single page b+w", SngModes.SINGLE_BW)
				.addItem("Multipage colour", SngModes.MULTI_COL)
				.addItem("Single page colour", SngModes.SINGLE_COL)
				.setColorForeground(green_dark)
				.setColorActive(green)
				.setColorLabels(black)
				.setTab("Export");
		
		List<Toggle>pi = pattern_options.getItems();
		for(int i=0; i<pi.size(); i++){
			Toggle t = (Toggle)pi.get(i);
			t.setPosition(400, 80+i*20);
			if(i<2)
				t.setGroup("bw");
			else
				t.setGroup("cp");	
		}

		export = cp5.addButton("export")
				.setPosition(220, 310)
				.setSize(160, 40)
				.setColorForeground(green_dark)
				.setColorActive(green)
				.setTab("Export")
				.setGroup("ae");
		export.getCaptionLabel().setFont(medium_font).align(ControlP5.CENTER, ControlP5.CENTER);
		
		symbol_list = cp5.addMultiList("s", 200, 197, 199, 40);
		symbol_list.setGroup("cp");
		current_colors = symbol_list.add("currsym",0);
		symcolors = 0;
		current_colors.setColorActive(green).setColorForeground(green_dark);
		symbol_colors = new MultiListButton[4];
		sym_img = new PImage[4];
		for(int i=0;i<4; i++){
			symbol_colors[i] = current_colors.add(i+"",i);
			sym_img[i] = loadImage("assets/symbols/colors"+i+".gif");
			symbol_colors[i]
					.setColorForeground(green_dark)
					.setImage(sym_img[i]);
			cp5.getTooltip().register(symbol_colors[i], "colours for pattern");
		}
		current_colors.setImage(sym_img[0]);	
	}

	public void draw(){
		
		background(white);
		if(cp5.getTab("default").isActive()){
			stroke(color(0,0,50));
			line(400,60,400,260);
			noStroke();
			fill(black);
			textFont(medium_font);
			text("OR", 185, 72);
		}else if(cp5.getTab("Palette").isActive()){
			if(cp5.getGroup("pf").isVisible()){
				picker.draw();
				fill(black);
				textFont(small_font);
				text("SEARCH BY\nCODE:", 10, 100);
				fill(cyan);
				rect(132, 99, 85, 25);
				tempPal.draw(40);
				if(tempPal.name.length() > 0){
					fill(black);
					text("NAME:\n"+tempPal.name, 10, 200);
				}
				if(!search_box.isFocus()){
					search_box.setValue(picker.getCode((int)search.getValue()));
				}
			}else{
				fill(black);
				textFont(small_font);
				text("CLICK COLOUR TO REMOVE FROM PALETTE", 10, 70);
				tempPal.draw(270);
				savePal.setVisible(tempPal.size() > 1);
			}	
		}else{ //export
			fill(black);
			textFont(small_font);
			text("CHOOSE IMAGE:", 40, 70);
			if(p5.img.history[selectedImage()] != null){
				text("NON PATTERN OPTIONS:", 200, 70);
				cp5.getGroup("ae").setVisible(true);
				if(p5.img.history[selectedImage()].pattern && p5.img.history[selectedImage()].mode != SngModes.ACTUAL){
					text("PATTERN OPTIONS:", 400, 70);
					cp5.getGroup("bw").setVisible(true);
					if(p5.img.history[selectedImage()].simple){
						text("COLOURS FOR SYMBOLS:", 200, 185);
						cp5.getGroup("cp").setVisible(true);
					}else{
						cp5.getGroup("cp").setVisible(false);
					}
				}else{
					cp5.getGroup("cp").setVisible(false);
					cp5.getGroup("bw").setVisible(false);
				}
			}else{
				cp5.getGroup("ae").setVisible(false);
				cp5.getGroup("cp").setVisible(false);
				cp5.getGroup("bw").setVisible(false);
			}
		}
		
		
	}
	
	public String getExportName(){
		return exportName;
	}
	
	public void export_name(String theText){
		theText = theText.trim();
		if(theText.length() > 0){
			exportName = theText;
			writeMessage("named folder for export: exports/"+exportName+"/");
		}else{
			exportName = "default";
			writeMessage("using default folder for export: exports/default/");
		}
	}
	
	public void name_box(String theText){
		theText = theText.trim();
		if(theText.length() > 0){
			name_box.setVisible(false);
			tempPal.name = theText;
			name_box.setFocus(false);
		}
	}
	
	public void searchBox(String theText){
		
		search_box.setFocus(false);
		String prefix;
		theText = theText.trim();
		int mode = (int)search.getValue();
		int col = color(0);
		if(mode == ACTUAL){
			if(theText.startsWith("#"))
				theText = theText.substring(1);
			try{
				col = unhex("FF"+theText.trim());
			}catch(Exception e){
				writeMessage("Enter a RRGGBB hex code");
			}
		}else{
			switch(mode){
			case DMC:
				prefix = "dmc : ";break;
			case ANCHOR:
				prefix = "anchor : "; break;
			default:
				prefix = "madeira : "; break;
			}
			col = st.getColor(prefix+theText.trim());
		}
		picker.setColor(col);
	}
	
	
	public void saveUse(){
		if(tempPal.size() > 1){
			cp5.getTab("Palette").setActive(false);
			cp5.getTab("default").setActive(true);
			cp5.getTab("Palette").getCaptionLabel().setColor(black);
			cp5.getTab("default").getCaptionLabel().setColor(white);
			back();
			Palette pal = tempPal.export();
			name_box.setVisible(true);
			MultiListButton p = categories[6].add(pal.name, count);
			p.add(6+"img"+count,count).setImage(pal.img).setPosition(222, nextposition);
			p.setColorBackground(magenta_dark)
			.setColorForeground(magenta);	
			p.getCaptionLabel().setFont(very_small);
			p.setPosition(121, nextposition);
			p5.saveUserPalette(pal);
			tempPal.clear();
			paletteType = count;
			count++;
			nextposition += 26;
		}else{
			writeMessage("Palette must have at least 2 colours", true);
		}
	}
	
	public void export(){
		int exportCode = 1;
		if(non_patterns.getState(0))
			exportCode *= SngModes.PDF;
		if(non_patterns.getState(1))
			exportCode *= SngModes.CSV;
		if(non_patterns.getState(2))
			exportCode *= SngModes.TIFF;
		if((int)pattern_options.getValue() != 0)
			exportCode *= (int)pattern_options.getValue();
		if(exportCode < 0)
			exportCode *= -1;
		/*//Test export code is working
		int [] tests = {SngModes.PDF, SngModes.CSV, SngModes.TIFF, SngModes.SINGLE_BW, SngModes.SINGLE_COL, SngModes.MULTI_BW, SngModes.MULTI_COL};
		String [] prints = {"PDF : ", "CSV : ", "TIFF : ", "single BW : ", "single color : ", "multi bw : ", "multi color : "};
		for(int i=0; i<tests.length; i++){
			if(exportCode%tests[i] == 0)
				println(prints[i]);
		}*/
		p5.img.history[selectedImage()].export(exportCode, sketchPath+"/exports/"+getExportName(), nf(hour(),2)+nf(minute(),2)+nf(second(),2));
			
	}
	
	public int selectedImage(){
		return (int)choose_image.getValue();
	}
	
	public int [][] getExportColors(){
		//int [] colors = {black, green, cyan_dark, red};
		return p5.symbol_cols[symcolors];
	}
	
	public void imageChosen(int x, int y, int h, int w){
		if(x >= 0 && x < w && y >= 0 && y < h){
			choose_image.activate(0);
		}else if(x >= w && x <= w*2 && y >= 0 && y < h){
			choose_image.activate(1);
		}else if(x >= 0 && x < w && y >= h && y <= h*2){
			choose_image.activate(2);
		}else if(x >= w && x <= w*2 && y >= h && y <= h*2){
			choose_image.activate(3);
		}
	}
	
	public void reviewSave(){
		palette_first.hide();
		palette_second.show();
	}
	
	public void back(){
		palette_second.hide();
		palette_first.show();
	}
	
	public void dynamic(){
		preset_group.hide();
		preset.setColorBackground(black);
		dynamic.setColorBackground(magenta);
		color_amt.setVisible(true);
	}
	
	public void preset(){
		color_amt.setVisible(false);
		preset.setColorBackground(magenta);
		dynamic.setColorBackground(black);
		preset_group.show();
	}
	
	public void glitch(){
		p5.img.startGlitch(true);
	}
	
	public void update(){
		p5.img.startGlitch(false);
	}

	public void handleEvent(ControlEvent theEvent){
		
	}
	
	public void writeMessage(String str){
		if(cp5.getTab("default").isActive()){
			message[0].setText(str);
		}else if(cp5.getTab("Palette").isActive()){
			message[1].setText(str);
		}else{
			message[2].setText(str);
		}
	}
	
	public void writeMessage(String str, boolean error){
		//doesn't do anything special just now
		writeMessage(str);
		if(error){
			message[0].setColorBackground(red);
			message[0].getValueLabel().setColor(white);
		}else{
			message[0].setColorBackground(white);
			message[0].getValueLabel().setColor(black);
		}
	}
	
	public int getMode(){
		boolean dmc = brand.getState(0);
		boolean anchor = brand.getState(1);
		boolean madeira = brand.getState(2);
		if(dmc && anchor && madeira){
			return SngModes.ALL;
		}else if(dmc && anchor){
			return SngModes.NOTMADEIRA;
		}else if(dmc && madeira){
			return SngModes.NOTANCHOR;
		}else if(anchor && madeira){
			return SngModes.NOTDMC;
		}else if(dmc){
			return SngModes.DMC;
		}else if(anchor){
			return SngModes.ANCHOR;
		}else if(madeira){
			return SngModes.MADEIRA;
		}else{
			return SngModes.ACTUAL;
		}
	}
	
	public int getGlitchAmt(){
		return (int)glitch_amt.getValue();
	}
	
	public int getDitherType(){
		return (int)dither_radio.getValue();
	}
	
	public boolean dynamicPal(){
		return !preset_group.isVisible();
	}
	
	public int howManyColors(){
		
		return (int)color_amt.getValue();
	}
	
	public int paletteKey(){
		return paletteType;
	}
	
	public boolean glitchOpen(){
		return cp5.getTab("default").isActive();
	}
	
	public int screen(){
		if(cp5.getTab("Export").isActive())
			return 1;
		return 0;//glitch
	}
	
	public int chosenImage(){
		return 0;
	}
	
	public int getExportCode(){
		return 1;
	}
	
	public void mouseDragged(){
		if(cp5.getTab("Palette").isActive() && picker.over(mouseX, mouseY)){
			picker.update(mouseX, mouseY);
		}
	}
	
	public void mousePressed(){
		if(cp5.getTab("Palette").isActive()){
			if(cp5.getGroup("pf").isVisible() && tempPal.over(true, mouseX, mouseY)){
				reviewSave();
			}else if(cp5.getGroup("ps").isVisible() && tempPal.over(false, mouseX, mouseY) && tempPal.size() > 0){
				tempPal.remove(mouseX);
				
			}
		}
	}
	
	public void controlEvent(ControlEvent theEvent){
		if(theEvent.isFrom(palette_list)){
			
			Palette p = (Palette)p5.palettes.get((int)theEvent.getValue());
			writeMessage("setting palette to "+p.name);
			paletteType = (int)theEvent.getValue();
			
		}else if(theEvent.isFrom(symbol_list)){
			
			int response = (int)theEvent.getValue();
			current_colors.setImage(sym_img[response]);
			symcolors = response;
			writeMessage("using symbol palette "+(symcolors+1));
			System.out.println("now symcolors is "+symcolors);
			
		}else if(theEvent.isTab()){
			int val = (int)theEvent.getValue();
			String open, close1, close2;
			switch(val){
			case 1:
				open = "default"; close1 = "Palette"; close2 = "Export"; break;
			case 2:
				open = "Palette"; close1 = "default"; close2 = "Export"; break;
			default:
				open = "Export"; close1 = "default"; close2 = "Palette";
			}
			cp5.getTab(open).setActive(true);
			cp5.getTab(close1).setActive(false);
			cp5.getTab(close2).setActive(false);
			cp5.getTab(open).getCaptionLabel().setColor(white);
			cp5.getTab(close1).getCaptionLabel().setColor(black);
			cp5.getTab(close2).getCaptionLabel().setColor(black);
		}
	}
	
	public void swatch_0(){
		//DMC
		tempPal.add(new Selection(picker.ids[DMC]));
		writeMessage(picker.ids[DMC]+" added to palette");
	}
	public void swatch_1(){
		//ANCHOR
		tempPal.add(new Selection(picker.ids[ANCHOR]));
		writeMessage(picker.ids[ANCHOR]+" added to palette");
	}
	public void swatch_2(){
		//MADEIRA
		tempPal.add(new Selection(picker.ids[MADEIRA]));
		writeMessage(picker.ids[MADEIRA]+" added to palette");
	}
	public void swatch_3(){
		//ACTUAL
		tempPal.add(new Selection(picker.colors[ACTUAL]));
		writeMessage(picker.ids[ACTUAL]+" added to palette");
	}
	
	public void error(int frame){
		message[0].setColorBackground(getColorError(frame));
	}
	
	public int getColorError(int frame){
		if(p5.img.broken){
			return p5.color(0, 100, p5.frameCount - frame);
		}else{
			return white;
		}
	}
	
	public class TempPalette{
		
		String name = "";
		ArrayList<Selection>list;
		public TempPalette(){
			list = new ArrayList<Selection>(10);
		}
		
		boolean over(boolean first, int x, int y){
			if(x >= 0 && x <= width && y >= 90 && y <= 360){
				if(!first){
					return true;
				}else if(y >= 320){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}
		
		void draw(int h){
			float w = width/(float)list.size();
			for(int i=0; i<list.size(); i++){
				Selection s = (Selection)list.get(i);
				fill(s.color);
				rect((w*i)+(w/2), 360-(h/2), w, h);
			}
		}
		
		void remove(int x){
			float w = width/(float)list.size();
			int to_remove = (int)(x/w);
			Selection s = (Selection)list.get(to_remove);
			list.remove(to_remove);
			if(s.restricted)
				writeMessage(s.id+" removed from palette");
			else
				writeMessage(hex(s.color)+" removed from palette");
			
		}
		
		void add(Selection s){
			list.add(s);
		}
		
		int size(){
			return list.size();
		}
		
		Palette export(){
			
			if(name.length() == 0)
				name = (new Date()).toString();
			PGraphics bitmap = createGraphics(170,25);
            bitmap.beginDraw();
            bitmap.noStroke();
            bitmap.rectMode(CORNER);
			Floss [] flosses = new Floss[list.size()];
			for(int i=0; i<list.size(); i++){
				Selection s = (Selection)list.get(i);
				float fl = bitmap.width/flosses.length;
				int c = s.color;
				bitmap.fill(c);
                bitmap.rect(fl*i, 0, fl, bitmap.height);
                Floss f;
                if(s.restricted)
                	//f = new RestrictedFloss(p5, s.id);
                	f = new Floss(p5, s.id);
                else
                	f = new Floss(p5, new Integer(c));
                flosses[i] = f;
			}
			bitmap.endDraw();
			return new Palette(p5, flosses, name, bitmap);
			
		}
		
		void clear(){
			name = "";
			list.clear();
		}
		
	}
	
	public class Selection{
		boolean restricted;
		String id;
		int color;
		public Selection(String id){
			restricted = true;
			this.id = id;
			this.color = st.getColor(id);
		}
		public Selection(int color){
			restricted = false;
			this.color = color;
		}
	}
	
	public class Picker implements Runnable{
		int x, y, w, h, hue, sat, bri, saturation, brightness;
		String [] ids;
		int [] colors;
		boolean ready;
		//Slider hue_slide;
		PImage [] img;
		PImage spectrum;
		
		public Picker(int x, int y, int w, int h){
	
			this.x = x; this.y = y;
			this.h = h; this.w = w;
			ready = false;
			img = new PImage[361];
			Thread t = new Thread(this);
			t.start();
			ids = new String[4];
			colors = new int[4];
			spectrum = loadImage("assets/slider.png");
			sat = x+(w/2);
			bri = y+(h/2);
			saturation = 50;
			brightness = 50;
			hue = 180;
			update();
			
		}
		
		public void run(){
			for(int i=0; i<img.length; i++){
				img[i] = loadImage("assets/picker/pal-3-"+i+".gif");
			}
			ready = true;
		}
		
		public void draw(){
			int newHue = getHue();
			if(hue != newHue){
				hue = newHue;
				update();
			}
			if(ready)
				image(img[getHue()], x, y);
			fill(cyan);
			rect(sat, bri, 20, 20);
			image(spectrum,x,270);
		}
		
		public void update(int newX, int newY){
			saturation = newX < x ? 0 : newX > x+w ? 100 : percent(newX - x);
			brightness = newY < y ? 100 : newY > y+h ? 0 : 100-percent(newY - y);
			sat = newX < x ? x : newX > x+w ? x+w : newX;
			bri = newY < y ? y : newY > y+h ? y+h : newY;
			colors[ACTUAL] = color(hue, saturation, brightness);
			update();
			//println("saturation: "+saturation+" brightness: "+brightness);
		}
		
		public void update(){
			hue = getHue();
			hue_slide.setCaptionLabel("HUE : "+hue+" DEGREES");
			colors[ACTUAL] = color(hue, saturation, brightness);
			ids[ACTUAL] = hex(colors[ACTUAL]);
			ids[DMC] = st.getClosestID(colors[ACTUAL], SngModes.DMC);
			ids[ANCHOR] = st.getClosestID(colors[ACTUAL], SngModes.ANCHOR);
			ids[MADEIRA] = st.getClosestID(colors[ACTUAL], SngModes.MADEIRA);
			for(int i=0;i<3;i++){
				colors[i] = st.getColor(ids[i]);
				swatch[i].setColorBackground(colors[i])
				.setColorActive(colors[i])
				.setCaptionLabel(ids[i]);
			}
			swatch[ACTUAL].setColorBackground(colors[ACTUAL])
			.setColorActive(colors[ACTUAL])
			.setCaptionLabel("HEX : #"+ids[ACTUAL].substring(2));
			
		}
		
		private int percent(int in){
			return (int)((in/(float)w)*100);
		}

		public int getHue(){
			return (int)hue_slide.getValue();
		}
		
		/**
		 * If the user is over the colour-picking square or close enough outside
		 * 
		 * @param mx mouseX
		 * @param my mouseY
		 * @return if the position qulaifies as "over"
		 */
		public boolean over(int mx, int my){
			return (mx >= x-10 && mx <= (x+w+10) && my >= y-10 && my <= (y+h+10));
		}
		
		/**
		 * The string for the swatch labels
		 * 
		 * @param mode -mode of the swatch
		 * @return the String id for setting the label
		 */
		public String getCode(int mode){
			if(mode == ACTUAL){
				return (ids[ACTUAL].substring(2));
			}else{
				String [] parts = ids[mode].split(" : ");
				return parts[1];
			}
		}
		
		public void setColor(int theColor){
			colors[ACTUAL] = theColor;
			ids[ACTUAL] = hex(theColor);
			hue_slide.setValue(hue(theColor));
			hue = getHue();
			hue_slide.setCaptionLabel("HUE: "+hue+" DEGREES");
			
			sat = (int) (x + saturation(theColor));
			bri = (int) (y + w - (brightness(theColor)/100.0*w));
			ids[DMC] = st.getClosestID(colors[ACTUAL], SngModes.DMC);
			ids[ANCHOR] = st.getClosestID(colors[ACTUAL], SngModes.ANCHOR);
			ids[MADEIRA] = st.getClosestID(colors[ACTUAL], SngModes.MADEIRA);
			for(int i=0;i<3;i++){
				colors[i] = st.getColor(ids[i]);
				swatch[i].setColorBackground(colors[i])
				.setColorActive(colors[i])
				.setCaptionLabel(ids[i]);
			}
			swatch[ACTUAL].setColorBackground(colors[ACTUAL])
			.setColorActive(colors[ACTUAL])
			.setCaptionLabel("HEX : #"+ids[ACTUAL].substring(2));
		}
		
	}

}
