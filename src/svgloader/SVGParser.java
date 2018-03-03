package svgloader;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

//
public class SVGParser {
	/**
	Constructor
	@param svgName String
	@exception Java generic Exception if suffix is neither .svg, nor .svgz or the content does
	not contain any block starting with <svg and ending with </svg>
	*/
	public SVGParser(String svgName) throws Exception {
		byte[] buf = null;
		int length = 0;
		
		if(svgName.endsWith(".svg")) {
			FileInputStream inFile = new FileInputStream(svgName);
			buf = new byte[inFile.available()];
			length = inFile.read(buf);
			
		}
		else if(svgName.endsWith(".svgz")){
			GZIPInputStream inFile = new GZIPInputStream( new FileInputStream(svgName));
			buf = new byte[inFile.available()];
			length = inFile.read(buf);
		}
		
		String svgContent = new String(buf, 0, length);
		/*
		long start1 = System.currentTimeMillis();
			List<String> list1 = getSvgObjectWithRegex(svgContent);
		long end1 = System.currentTimeMillis();
		long time1 = end1-start1;
		*/
     	List<String> list1 = getSvgObjectWithRegex(svgContent); 
		
	}
	/**
	Shaping a geometrical form
	@param S JFX-Shape (Rectangle, Ellipse, Circle, etc.)
	@param s String, the parsing string (e.g. <circle .. style="...."/>)
	*/
	public void shape(Shape S, String s) {
		if(S instanceof Rectangle) {
			((Rectangle)S).setX(getValue(s, "x"));
			((Rectangle)S).setY(getValue(s, "Y"));
			((Rectangle)S).setWidth(getValue(s, "width"));
			((Rectangle)S).setWidth(getValue(s, "height"));			
		}
		
		if(S instanceof Circle) {
			((Circle)S).setCenterX(getValue(s, "cx"));
			((Circle)S).setCenterY(getValue(s, "cy"));
			((Circle)S).setRadius(getValue(s, "r"));
		
		}
		
		if(S instanceof Ellipse) {
			((Ellipse)S).setCenterX(getValue(s, "cx"));
			((Ellipse)S).setCenterY(getValue(s, "cy"));
			((Ellipse)S).setRadiusX(getValue(s, "rx"));
			((Ellipse)S).setRadiusY(getValue(s, "ry"));			
		}
		
		if(S instanceof Line) {			
			((Line)S).setStartX(getValue(s, "x1"));
			((Line)S).setStartY(getValue(s, "y1"));
			((Line)S).setEndX(getValue(s, "x2"));
			((Line)S).setEndY(getValue(s, "y1"));				
		}
		
		if(S instanceof Line) {			
			((Line)S).setStartX(getValue(s, "x1"));
			((Line)S).setStartY(getValue(s, "y1"));
			((Line)S).setEndX(getValue(s, "x2"));
			((Line)S).setEndY(getValue(s, "y1"));				
		}
		
		if(S instanceof Polyline) {				
			((Polyline)S).getPoints().addAll(doubleArray(getString(s, "points")));					
		}
		
		if(S instanceof Polygon) {				
			((Polygon)S).getPoints().addAll(doubleArray(getString(s, "points")));
					
		}
		
		//STYLE CODE
	}
	/**
	search and parse double array for Polyline and Polygon
	@param s String, the parsing string
	@return double array with 6 elements (see JavaFX or SVG doc)
	*/
	public Double[] doubleArray(String s) {
		
		return Arrays.stream(s.split("[\\s,]"))
				.map(Double::valueOf)
                .toArray(Double[]::new);
	}
	/**
	search and parse the shape of a viewBox
	@param s String, the parsing string (e.g. <svg .... viewBox="....." ...</svg>)
	@return double array with Viewbox 4 elements (x, y, width, height)
	*/
	public double[] viewBoxData(String s) {	
				
		String vb = getString(s, "viewBox");
		return Arrays.stream(vb.split(" "))
					 .mapToDouble(Double::parseDouble)
					 .toArray();
		
	}
	/**
	search and parse a value of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key width -> <svg .... width="..." ...</svg> or <svg .... style="...width:...;
	..." ...</svg>)
	@return double the value of the given key
	*/
	public double getValue(String s, String key) {
		String valStr = getString(s, key);
		if(valStr != null) {
			return Double.parseDouble(valStr);
		}
		return 0;		
	}
	


	/**
	search and parse a string of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key rect -> <rect .... .../>)
	@return String the content of the given key
	*/
	public String getString(String s, String key) {

		int index = key.length();
		if(key == "svg") {			
			return "";
		}
		if(s.indexOf("<"+key) > 0) {
			index += s.indexOf("<"+key)+1;
			return s.substring(index, s.indexOf("/>", index));
		}
		else if(s.indexOf(key+"=\"") > 0) {
			index += s.indexOf(key+"=\"")+2;
			return s.substring(index, s.indexOf("\"", index));
		}
		else if(s.indexOf(key+":") > 0) {
			index += s.indexOf(key+":")+1;
			return s.substring(index, s.indexOf(";", index));
		}
		return null;	
				
	}
	/**
	search and parse the color of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key fill -> <rect...fill="blue".../>)
	@return Color JavaFX color of the given key
	*/
	public Color getColor(String s, String key) {
		String color = getString(s, key);
		
		SVGColor svgColor = new SVGColor();
		if (color != null) {
			double op = opacityValue(s, key+"-fill");
			return svgColor.svgColor(color, op); // SVGColor API
		}
		return null;
			
	}
	/**
	search and parse the Opacity Value of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key fill-opacity -> <rect...fill-opacity="0.5".../>)
	@return double opacity value of JavaFX color of the given key*/
	public double opacityValue(String s, String key) {		
		double op = getValue(s, key);
		return op != 0? op: 1;
	}
	
	/**
	search and parse SVG object of the given key
	@param S String array of 2 elements, element 1: Parsing string, element 2: the parsed string from the element-1-string
	(as the 2nd returned string)
	@param key String the designated key (e.g. key rect -> <rect...fill-opacity="0.5".../>)
	@return int the length of element-2-string
	*/
	public int svgObject(String[] S, String key, int pos) {
		
//	
		return 0;
	}
	
	/**
	search, parse and load the content of a path from the given string
	@param s String, the parsing string
	@return String the path content extracted from d="..........."
	*/
	public String svgPathContent(String s) {
		return getString(s, "d");		
	}	
	

	
	protected List<String> getSvgObjectWithRegex(String s) {
		List<String> obList = new ArrayList<String>();
	
		String key = "((rect)|(circle)|(path)|(text)|(ellipse)|(line)|(polyline)|(polygon)|(ellipse)|(circle))";
		Pattern O_REGEX = Pattern.compile("(<("+key+")[^<(/>)>]*>[^<>]*?(<\\2[^<>]*>.*?</\\2>)*[^<>]*?(</\\2>))|(<"+key+"[^<>]*/>)");
		Matcher matcher = O_REGEX.matcher(s);	
		
		while (matcher.find()) {
			obList.add(matcher.group(0));
	    }
		return obList;
	}
	
	
	
	

	
}