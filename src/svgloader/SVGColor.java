package svgloader;


public class SVGColor {
    /**
    * Constructor
    */
    public SVGColor( )
    /**
    setOpacity sets generally the converted color to this Opacity setting
    @param opacity double, the opacity grad between 0.0 ... 1.0 (None...Full Transparent)
    */
    public void setOpacity(double opacity)
    /*
    getOpacity returns the Opacity setting
    @return double the setting opacity grad between 0.0 ... 1.0 (None...Full Transparent)
    */
    public double getOpacity( )
    /**
    svgColor converts a string to JavaFX color object
    @param color   string, lower case. E.g. white or #ffffff (6x hex)
    @return Color or null if string is invalid...
    */
    public Color svgColor(String color)
    /**
    svgColor converts a string to JavaFX color object
    @param color   string, lower case. E.g. white or #ffffff (6x hex)
    @param opacity double, between 0.0 ... 1.0 (None...Full Transparent)
    @return Color or null if string is invalid...
    */
    public Color svgColor(String color, double opacity)
}