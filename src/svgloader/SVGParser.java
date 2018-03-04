package svgloader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

//
public class SVGParser {
	private static final int DEFAULT_ATTR_GROUP=2,
							CSS_ATTR_GROUP=4;
	private String fileContent;//TODO: find a specific use
	/**
	Constructor
	@param svgName String
	@exception Java generic Exception if suffix is neither .svg, nor .svgz or the content does
	not contain any block starting with <svg and ending with </svg>
	*/
	public SVGParser(String svgName) throws Exception {
		if(!(svgName.endsWith(".svg")||svgName.endsWith(".svgz")))
			throw new Exception();
		byte[] buf = null;
		int length = 0;
		InputStream inFile=null;
		if(svgName.endsWith(".svg"))
			inFile = new FileInputStream(svgName);
		else if(svgName.endsWith(".svgz"))
			inFile = new GZIPInputStream( new FileInputStream(svgName));
		

		buf = new byte[inFile.available()];
		length = inFile.read(buf);
		fileContent = new String(buf, 0, length);
		
	}
	/**
	Shaping a geometrical form
	@param S JFX-Shape (Rectangle, Ellipse, Circle, etc.)
	@param s String, the parsing string (e.g. <circle .. style="...."/>)
	*/
	public void shape(Shape S, String s) {
		
	}
	/**
	search and parse double array for Polyline and Polygon
	@param s String, the parsing string
	@return double array with 6 elements (see JavaFX or SVG doc)
	*/
	public double[] doubleArray(String s) {
		return null;
		
	}
	/**
	search and parse the shape of a viewBox
	@param s String, the parsing string (e.g. <svg .... viewBox="....." ...</svg>)
	@return double array with Viewbox 4 elements (x, y, width, height)
	*/
	public double[] viewBoxData(String s) {
		String[] values=getString(s,"viewBox").split("\\s");
		double[] array=new double[values.length];
		for(int i=0;i<values.length;++i){
			array[i]=Double.parseDouble(values[i]);
		}
		return array;
		
	}
	/**
	search and parse a value of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key width -> <svg .... width="..." ...</svg> or <svg .... style="...width:...;
	..." ...</svg>)
	@return double the value of the given key
	*/
	public double getValue(String s, String key) {
		String value=getString(s,key);
		if(value.endsWith("cm"))
			return Double.parseDouble(value.substring(0,value.length()-2))*38.5;//Change to pixel
		return Double.parseDouble(value)*0.385;
	}
	
	private String getAttribute(String s,String key){//TODO: is case sensitive?
		Pattern p= Pattern.compile("(\\b"+key+"=\"([^\"]*)\")|(style=\"[\\w\\W]*"
									+key+":([\\S^;]*)\\b[\\w\\W])");
		Matcher m=p.matcher(s);
		if(m.find()){
			return (m.group(DEFAULT_ATTR_GROUP)!=null)? m.group(DEFAULT_ATTR_GROUP)
					:((m.group(CSS_ATTR_GROUP)!=null)? m.group(CSS_ATTR_GROUP):null);
		}
		return null;
	}
	
	private String getTag(String s, String key){//TODO: is case sensitive?
		int openingTag=0
			,beginIndex=-1
			,endIndex=-1
			,index=0;
		while(true){
			index=s.indexOf(key,index);
			if(index==-1)
				break;
			if(s.charAt(index-1)=='<'){//if it is a begin tag
				if(openingTag==0){//self-closing tag
					int endOfTag=s.indexOf('>',index);
					if(s.charAt(endOfTag-1)=='/')
						return s.substring(index-1,endOfTag+1);
					else
						beginIndex=index-1;
				}
				openingTag++;
			}else if(s.substring(index-2, index+key.length()+1).equals("</"+key+">")){//if it is a close tag
				openingTag--;
				if(openingTag==0){
					endIndex=index+key.length()+1;
					break;
				}
			}
			index++;
		}
		return (beginIndex!=-1 && endIndex!=-1)?s.substring(beginIndex, endIndex):null;
	}
	/**
	search and parse a string of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key rect -> <rect .... .../>)
	@return String the content of the given key
	*/
	public String getString(String s, String key) {
		int firstIndex=s.indexOf("<"+key);
		if(firstIndex!=-1)
			return getTag(s,key);
		else
			return getAttribute(s,key);
	}
	/**
	search and parse the color of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key fill -> <rect...fill="blue".../>)
	@return Color JavaFX color of the given key
	*/
	public Color getColor(String s, String key) {
		String color = getString(s, key);
		if (color != null) {
			return new SVGColor().svgColor(color); 
		}
		return null;		
	}
	/**
	search and parse the Opacity Value of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key fill-opacity -> <rect...fill-opacity="0.5".../>)
	@return double opacity value of JavaFX color of the given key*/
	public double opacityValue(String s, String key) {
		return getValue(s,key);
		
	}
	
	/**
	search and parse SVG object of the given key
	@param S String array of 2 elements, element 1: Parsing string, element 2: the parsed string from the element-1-string
	(as the 2nd returned string)
	@param key String the designated key (e.g. key rect -> <rect...fill-opacity="0.5".../>)
	@return int the length of element-2-string
	*/
	public int svgObject(String[] S, String key) {
		S[1]=getString(S[0],key);
		return S[1].length();
	}
	
	/**
	search, parse and load the content of a path from the given string
	@param s String, the parsing string
	@return String the path content extracted from d="..........."
	*/
	public String svgPathContent(String s) {
		return getString(s,"d");
		
	}
}