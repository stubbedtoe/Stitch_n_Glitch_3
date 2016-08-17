# Stitch'n'Glitch
## open-source image corruption and cross stitch pattern software
### http://www.stitch-n-glitch.com version3 - summer 2014

#### Designed and coded by Andrew Healy ([andrewhealy.com](http://www.andrewhealy.com), [email](mailto:werdnah19@gmail.com))

![home sweet home](http://stitch-n-glitch.com/images/homesweethome.jpg)

## OVERVIEW:

This program was intended as a combined image-corruption and cross-stitch pattern generator. Along the way some other functionalities were added but it remains a simple, small application to preform this very specific task. There are other, more powerful cross-stitch pattern generators available both online and off as well as various image-corruption facilities. This application was never intended as the fastest, most powerful or fullest-featured pattern generator - rather a product of my personal artistic exploration into the aesthetic possibilities of mixing the electronic "glitched" image with a hand-crafted cross-stitch.

This is an open-source project in appreciation of textile art's history of sharing techniques and patterns. Feel free to modify for your own use in keeping with the same spirit.

This document just covers the basics. For some useful tips & tricks, videos, faqs etc., visit [stitch-n-glitch.com/docs/](http://www.stitch-n-glitch.com/docs)

### System Requirements:
You need to have the Java Runtime installed. It can be downloaded from http://java.com/en/download/index.jsp . Mac users can check if their Java is up to date by requesting a software update check.

##### INSTALLATION:
Uncompress the .zip file to a writable location. Make sure the directory structure of the root folder and the source folder is maintained.

***************************************************************************

## BASIC USAGE:

* Double-click the `Stitch_n_Glitch.jar` file to launch the program.
* You will be prompted for an image. It must be an RGB .jpg or .gif file.
* To make use of all the options, the image must be less than 80kB in file size and less than 500,000 pixels when measured `width` * `height`.
* To just use *Stitch'n'Glitch* as a glitching/dithering/image export tool, the image must be less than 100kB
* Make any adjustments you desire to the Glitch slider, brand check boxes, Dither type, Colour Reduction / Preset Palette, and press either "Glitch" or "Update". __Note: No glitch will be applied if the Glitch amount slider is set to 0. No colour conversion will happen unless at least one of the brand checkboxes id selected.__
* If you get the "Broken image" symbol over your image and a message saying "Reduce Glitch or Try again", don't worry. A broken image can be the result of the random glitching process. As the message advises: "Reduce Glitch or Try again".
* You can do this as may times as you like; your previous 4 successful glitches are stored in memory.
* When you get one you'd like to stitch, click on the "Export" tab (along the top).
- Here select the image you'd like to export by using the buttons on the left or by clicking directly on the image.
* Make the selections for the files you'd like to export. The options can vary according to the amount of colours in the image, brand of floss selected and file size.
* Enter a name for the folder you'd like these files to be written to and press ENTER. Your files will be written to a folder of that name inside the `exports` folder (that sits beside the application). If you didn't enter a folder name, they will be written to either the previously entered name or a folder called `default`.
* All files are given a timestamp to further distinguish between exports and aid grouping of exports from the same image.
* Your files will be called (`XXX` denotes the timestame):
  * `pattern-XXX.pdf` (either multipage/single page cross stitch pattern. B&W or colour symbols)
  * `colorList-XXX.pdf` (key for the pattern. Contains colours, brand codes, assigned symbols, num. of stitches for each colour)
  * `image-XXX.tiff` (bitmap image export. Exported automatically with patterns)
  * `data-XXX.csv` (comma separted values for each pixel's hexidecimal notation. For further data processing perhaps)
  * `vector-XXX.pdf` (vector image. Useful for large-scale printing)

This is only a brief run-though (there was no mention of the Palette features, even). For more detail, visit the [docs](http://www.stitch-n-glitch.com/docs/) or view some [video tutorials](https://vimeo.com/album/2964391).

![Boring in, amazing out](http://stitch-n-glitch.com/images/timer.gif)


## COMPILING FROM SOURCE (in Eclipse):

* Open Eclipse and create a new Java Project (__File__ -> __New__ -> __Java Project__)
* Name it whatever you want
* Uncheck *Use default location* and instead select the `source` folder inside the download
* Leave the rest as default and click __Finish__
* Open `Sng3.java` in the editor. This is the class containing the `main` method
* Click __Run__ -> __Run As__ -> __Java Application__
* The first time, you my be prompted for the location of the main method: select the `Sng3` class

## ACKNOWLEDGEMENTS

* Coded 2012 (revised 2013, revised again 2014) by Andrew Healy ([andrewhealy.com](http://andrewhealy.com))
* Built with [Processing](http://www.processing.org) and Eclipse using the [controlP5](http://sojamo.de/libraries/controlP5/) library for GUI components.
* Image-corruption code adapted from the *Corrupt* project by [Benjamin Gaulon](http://recyclism.com).
* DMC floss to RGB/HEX conversion from a document at http://www.radicalcrossstitch.com/
* Dithering algorithms ported to Java from the Macro code found at http://fiji.sc/wiki/index.php/Dithering
* Many thanks to the Processing community for sharing their knowledge.
* Project inspired by the *Lost Threads* research project and [blog](http://lostthreads.tumblr.com)
