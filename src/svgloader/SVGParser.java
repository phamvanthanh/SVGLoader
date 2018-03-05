package svgloader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

//
public class SVGParser {
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
	
	public Group getObject() {
		String[] S = {fileContent, ""};
		svgObject(S, "svg", 0);
		return buildObject(S[0]);
		
	}
	//Thanh added for test
	public Group buildObject(String content) {
		Group g = new Group();
		if(!content.isEmpty()) {
			String key = "";
			int index = 0,  strlen = 0, length = content.length();
			String[] S = {content, ""};
			
			while(index < length)
			{
				key = findKey(content, index);

				if(key.equals("svg") ) 
				{
					strlen = svgObject(S, "svg", index);
					
		
					if(strlen > 0) {
						index += strlen;	
						double x = getValue(S[0], "x");
						double y = getValue(S[0], "y");
//						
//						g.setTranslateX(x);
//						g.setTranslateY(y);
						
						String cont = getContent(S[1]);
						if(!cont.isEmpty()) {

							Group g1 = buildObject(cont);
							if(g1 != null)
								g.getChildren().add(g1);
						}											
					}
					continue; 		
						
				}
						
				if(!key.equals("svg") && !key.isEmpty())
				{
					 strlen = svgObject(S, key, index);
					 index += strlen;
					 
					 if(strlen > 0) {
						 Shape sh = buildShape(S[1]);
						 if(sh!= null) {					
							g.getChildren().add(sh);
						
						 }
					 }					 
				 }	
				 else {
//					 System.out.printf("At break point Index: %d, Length: %d, S[0]: %s,\n S[1]: %s\n",index, length, S[0], S[1]);
					 break; //IF NO MORE TAGS 
				 }			
				
			}
			 return g;			
		}
		return null;
			
	}
	
	//Thanh added for test
	public Shape buildShape(String s) {
		Shape sh = null;
	
		if(s.indexOf("<rect") > -1) {
			sh = new Rectangle();			
			shape(sh, s);			
		}
		if(s.indexOf("<circle") > -1) {
			sh = new Circle();
			shape(sh, s);
		}
		if(s.indexOf("<line") > -1) {
			sh = new Rectangle();
			shape(sh, s);
		}
		if(s.indexOf("<polyline") > -1) {
			sh = new Polyline();
			shape(sh, s);
		}
		if(s.indexOf("<polygon") > -1) {
			sh = new Polygon();
			shape(sh, s);
		}
	
		if(s.indexOf("<path") > -1) {
			
			sh = new SVGPath();
			shape(sh, s);
		}
		if(s.indexOf("<text") > -1) {			
			sh = new Text();
			shape(sh, s);
		}
		return sh;
		
	}
	/**
	Shaping a geometrical form
	@param S JFX-Shape (Rectangle, Ellipse, Circle, etc.)
	@param s String, the parsing string (e.g. <circle .. style="...."/>)
	*/
	public void shape(Shape S, String s) {
		if(S instanceof Rectangle) {
			((Rectangle)S).setX(getValue(s, "x"));
			((Rectangle)S).setY(getValue(s, "y"));
			((Rectangle)S).setWidth(getValue(s, "width"));		
			((Rectangle)S).setHeight(getValue(s, "height"));			
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
				
		if(S instanceof Polyline) {				
			((Polyline)S).getPoints().addAll(doubleArray(getString(s, "points")));					
		}
		
		if(S instanceof Polygon) {				
			((Polygon)S).getPoints().addAll(doubleArray(getString(s, "points")));					
		}
		
		if(S instanceof SVGPath) {	
	
			((SVGPath)S).setContent(svgPathContent(s));					
		}
		
	
		if(S instanceof Text) {				
			((Text)S).setText(getString(s, "text"));
			((Text)S).setX(getValue(s, "x"));
			((Text)S).setY(getValue(s, "y"));
			//SET-FONT
		}
		//STYLE CODE FOR SHAPES
		S.setStroke(getColor(s, "stroke"));
		System.out.printf("String: %s, Color: %s", s,  getColor(s, "fill"));
		S.setFill(getColor(s, "fill"));
		
		//TRANSFORMATION CODES
		//FILL-RULE CODES
				
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
		if(!valStr.isEmpty())
			return Double.parseDouble(valStr);
		
		return 0;		
	}
	
	/**
	search and parse a string of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key rect -> <rect .... .../>)
	@return String the content of the given key
	*/
	public String getString(String s, String key) {
		s = s.replaceAll("[\\t\\n\\r]+"," ");
		int index = key.length();
		if(s.indexOf(" "+key+"=\"") > 0) {
			index += s.indexOf(" "+key+"=\"")+3;
			return s.substring(index, s.indexOf("\"", index));

		}
		
		else if(s.indexOf(key+":") > 0) { //CASE OF CSS FORMAT
			if(s.indexOf("style")>0)
				s = getString(s, "style")+";";
			
			int ind = s.indexOf(key+":");
			if(ind > 0) {
				index +=ind + 1;
				return s.substring(index, s.indexOf(";", index));
			}				
				
			return "";
		

		}
		
		else if(key == "text") {			
			int tIndex = s.indexOf(">")+1;
			return s.substring(tIndex, s.indexOf("<", tIndex));
		}
		else if(s.indexOf("<"+key+" ") > 0) {
			index += s.indexOf("<"+key)+1;
			return s.substring(index, s.indexOf("/>", index));
		}
		
		return "";	
				
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
			double op = opacityValue(s, key+"-opacity");
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
	public int svgObject(String[] S, String key, int index) {

		int start = S[0].indexOf("<"+key, index);	
		int close = isSelfClose(S[0], start);	
		String rst = "";
		if(start > -1) {	
			
			if( close < 0) { 	// Not self close tag														
				close = S[0].indexOf("/"+key+">", start)+key.length()+2; 	//First close		
			
				int oInd = index + key.length() + 1; 

				while(oInd < close){
					int open = S[0].indexOf("<"+key, oInd); //count from second open
					
					if(open > 0 && open < close) {						
						oInd = open+key.length()+1;
						if(isSelfClose(S[0], oInd) < 0) {
							int c = S[0].indexOf("/"+key+">", close); //count from second close
							if(c > 0) 
								close = c+key.length()+2;												
						}					
					}
					else
						break;						
				}				
			}			

			rst = S[0].substring(start, close).trim();	
			
		}
				
		S[1]= rst;	
		return rst.length();
	}
	private int isSelfClose(String s, int index) {
		int close = s.indexOf(">", index);		
		
		if(close > 0) {
			if(s.indexOf("/>", index) == (close -1)) {				
				return close+1;
			}
		}			
		
		return -1;
	}
	
	
	/**
	search, parse and load the content of a path from the given string
	@param s String, the parsing string
	@return String the path content extracted from d="..........."
	*/
	public String svgPathContent(String s) {
		return getString(s, "d");	
		
	
	}	
	
	public String getContent(String s) {
		 return s.substring(s.indexOf(">")+1, s.lastIndexOf("<")-1);		
	}	
	
	//Thanh added function
	protected List<String> getSvgObjectWithRegex(String s) {
		List<String> obList = new ArrayList<String>();		
					
		String key = "((rect)|(circle)|(ellipse)|(line)|(polyline)|(polygon)|(path)|(svg))";
		Pattern O_REGEX = Pattern.compile("(<("+key+")[^<(/>)>]*>[^<>]*?(<\\2[^<>]*>([^<>]*?(\n))*[^<>]*?</\\2>)*[^<>]*?(</\\2>))|(<"+key+"[^<>]*/>)");

	    Matcher matcher = O_REGEX.matcher(s);	
	
		while (matcher.find()) {
			obList.add(matcher.group(0));
	    }
//		System.out.println(obList);
		return obList;
	}
	
	//Thanh added function 
	protected String findKey(String s, int index) {
		
		String key = "((svg)|(rect)|(circle)|(ellipse)|(line)|(polyline)|(polygon)|(path))";
		Pattern K_REGEX = Pattern.compile(".{"+index+"}(<"+key+" )", Pattern.DOTALL);

		
		Matcher matcher = K_REGEX.matcher(s);	
		if(matcher.find()) {
			String rs = matcher.group(0);
			rs = rs.substring(rs.lastIndexOf("<")+1, rs.length()-1);
			
			return rs;
		}
			
		return "";
	}
	
	private String fileContent;			
}