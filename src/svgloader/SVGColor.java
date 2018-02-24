package svgloader;

import javafx.scene.paint.Color;

public class SVGColor {
    /**
    * Constructor
    */
	private Color color;
	private double opacity = 1;
    public SVGColor( ) {

    }
    /**
    setOpacity sets generally the converted color to this Opacity setting
    @param opacity double, the opacity grad between 0.0 ... 1.0 (None...Full Transparent)
    */
    public void setOpacity(double value) {
    	value = value > 1? 1:(value < 0? 0:value);
    	this.opacity = value;
    }
    /*
    getOpacity returns the Opacity setting
    @return double the setting opacity grad between 0.0 ... 1.0 (None...Full Transparent)
    */
    public double getOpacity( ) {
		return this.opacity;

    }
    /**
    svgColor converts a string to JavaFX color object
    @param color   string, lower case. E.g. white or #ffffff (6x hex)
    @return Color or null if string is invalid...
    */
    public Color svgColor(String color) {
		try {
			this.color = Color.web(color.toLowerCase(), this.opacity);
			return this.color;
		}
		catch (NullPointerException|IllegalArgumentException e){
			System.out.println("Input color string is not valid");
			e.printStackTrace();
			return Color.BLACK;
		}
    }
    /**
    svgColor converts a string to JavaFX color object
    @param color   string, lower case. E.g. white or #ffffff (6x hex)
    @param opacity double, between 0.0 ... 1.0 (None...Full Transparent)
    @return Color or null if string is invalid...
    */
    public Color svgColor(String color, double value) {
    	value = value > 1? 1:(value < 0? 0:value);
    	this.opacity = value;
			return svgColor(color);   	
    }
}
