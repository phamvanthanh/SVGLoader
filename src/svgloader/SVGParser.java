package svgloader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

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
			  
			  fileContent = (new String(buf, 0, length)).replaceAll("[\\t\\n\\r]+"," ");		
	}
	

	/**
	Shaping a geometrical form
	@param S JFX-Shape (Rectangle, Ellipse, Circle, etc.)
	@param s String, the parsing string (e.g. <circle .. style="...."/>)
	*/
	public void shape(Shape S, String s, String cas) { 
		String attr = "";
		
		if(S instanceof Rectangle) {
			attr = getAttributeString(s, "rect")+cas;
			((Rectangle)S).setX(getValue(attr, "x"));
			((Rectangle)S).setY(getValue(attr, "y"));
			((Rectangle)S).setWidth(getValue(attr, "width"));		
			((Rectangle)S).setHeight(getValue(attr, "height"));	
			((Rectangle)S).setArcHeight(getValue(attr, "ry"));
			((Rectangle)S).setArcWidth(getValue(attr, "ry"));
			
		}
		
		if(S instanceof Circle) {
			attr = getAttributeString(s, "circle")+cas;
			((Circle)S).setCenterX(getValue(attr, "cx"));
			((Circle)S).setCenterY(getValue(attr, "cy"));
			((Circle)S).setRadius(getValue(attr, "r"));
		
		}
		
		if(S instanceof Ellipse) {
			attr = getAttributeString(s, "ellipse")+cas;
			((Ellipse)S).setCenterX(getValue(attr, "cx"));
			((Ellipse)S).setCenterY(getValue(attr, "cy"));
			((Ellipse)S).setRadiusX(getValue(attr, "rx"));
			((Ellipse)S).setRadiusY(getValue(attr, "ry"));			
		}
		
		if(S instanceof Line) {	
			attr = getAttributeString(s, "line")+cas;
			((Line)S).setStartX(getValue(attr, "x1"));
			((Line)S).setStartY(getValue(attr, "y1"));
			((Line)S).setEndX(getValue(attr, "x2"));
			((Line)S).setEndY(getValue(attr, "y2"));				
		}
				
		if(S instanceof Polyline) {	
			attr = getAttributeString(s, "polyline")+cas;
			((Polyline)S).getPoints().addAll(doubleArray(getString(attr, "points")));					
		}
		
		if(S instanceof Polygon) {
			attr = getAttributeString(s, "polygon")+cas;
			((Polygon)S).getPoints().addAll(doubleArray(getString(attr, "points")));
		}
		
		if(S instanceof SVGPath) {		
			attr = getAttributeString(s, "path")+cas;
			((SVGPath)S).setContent(svgPathContent(attr));		
			((SVGPath)S).setFillRule(getFillRule(attr));
			((SVGPath)S).setStrokeLineCap(getStrokeLineCap(attr));
			((SVGPath)S).setStrokeLineJoin(getStrokeLineJoin(attr));
			((SVGPath)S).setStrokeMiterLimit(getStrokeMiterLimit(attr));
					
		}
				
	
		if(S instanceof Text) {		
			attr = getAttributeString(s, "text")+cas;
			((Text)S).setText(getString(s, "text"));
			((Text)S).setX(getValue(attr, "x"));
			((Text)S).setY(getValue(attr, "y"));
			double fz = getValue(attr, "font-size");
			if(!(fz > 0.0001))
				fz = 14; // Default font size
	
			((Text)S).setFont(Font.font(getString(attr, "font-family"), fz));
	
		}
	
		S.setStroke(getColor(attr, "stroke"));	
	
		S.setFill(getColor(attr, "fill"));
		double sw = getValue(attr, "stroke-width");
		if(!(sw > 0.0000001) )
			sw = 1;
		S.setStrokeWidth(sw);
		S.setOpacity(opacityValue(attr, "opacity"));
		
		String arr = getString(attr, "stroke-dasharray");
		if(!arr.isEmpty())
			S.getStrokeDashArray().addAll(doubleArray(arr));
		
		Transform trans = getTransform(attr);
		if(trans != null)
			S.getTransforms().add(trans);	
				
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

		int index = key.length();
		if(s.indexOf(" "+key+"=\"") > -1) {
			index += s.indexOf(" "+key+"=\"")+3;
			return s.substring(index, s.indexOf("\"", index));
		}
		
		else if(s.indexOf(key+":") > -1) { //CASE OF CSS FORMAT
			
			if(s.indexOf("style")>-1)
				s = getString(s, "style")+";";
			
			int ind = s.indexOf(key+":");
			
			if(ind > -1) {
				if(ind > 0 && s.charAt(ind-1) != ';' && s.charAt(ind-1) != ' ')
					return "";
				
				index +=ind + 1;
				return s.substring(index, s.indexOf(";", index));
			}			
			return "";
		}
		
		else if(key == "text") {			
			int tIndex = s.indexOf(">")+1;
			return s.substring(tIndex, s.indexOf("<", tIndex));
		}
		else if(s.indexOf("<"+key+" ") > -1) {
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
		
		if (color != null) {
			SVGColor svgColor = new SVGColor();
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
			
		String rst = "";
		if(start > -1) {	
			int close = isSelfClose(S[0], start);
			if( close < 0) { 	// Not self close tag														
				close = S[0].indexOf("/"+key+">", start)+key.length()+2; 	//First close		

				int oInd = start + key.length() + 1; 
			
				while(oInd < close && oInd > 0){
					int open = S[0].indexOf("<"+key, oInd); // next open
					
					if(open > -1 && open+key.length()+1 < close) {
					
						oInd = open+key.length()+1;
						if(isSelfClose(S[0], oInd) < 0 ) {
							int c = S[0].indexOf("/"+key+">", close); //next close
							if(c > -1) {
								close = c+key.length()+2;	
							}
							else {
								System.out.println("SVG file contains errors");
								return close - start;								
							}								
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

	
	
	/**
	search, parse and load the content of a path from the given string
	@param s String, the parsing string
	@return String the path content extracted from d="..........."
	*/
	public String svgPathContent(String s) {
		return getString(s, "d");			
	}	
	/*------------------Thanh ADDED METHODS--------------------*/
	/**
	 * Return Group contains all SVG object
	 */
		   
	public Group getObject() {
		String[] S = {fileContent, ""};	
		String cas = ""; //Cascading style
		return buildObject(S[0], cas);
	}
	/**
	* Search and parse string to Javafx objects
	* @param s String parsing string	
	* @param cas String svg object attribute to cascade style in nested structure
	* @return Javafx Group group contains all parsed Javafx objects
	*/
	public Group buildObject(String content, String cas) {

		Group g = new Group();
		ObservableList<Node> list = g.getChildren();
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

						String cont = getContent(S[1]);
						if(!cont.isEmpty()) {
							String attr = getAttributeString(S[1], "svg");
							double x = getValue(attr, "x");
							double y = getValue(attr, "y");	
							
							attr = attr.replaceAll("(x=\"[^\"]*\")|(y=\"[^\"]*\")|(width=\"[^\"]*\")|(height=\"[^\"]*\")", "");
							//x, y, width, height are not style attributes
						
							Group g1 = buildObject(cont, attr);
							
							if(g1 != null) {				
														
								g1.setTranslateX(x);							
								g1.setTranslateY(y);
								list.add(g1);
							}								
						}											
					}
					continue; 		
						
				}
				else if(key.equals("g")) {
					strlen = svgObject(S, "g", index);
					if(strlen > 0) {
						index += strlen;	
						String s = getAttributeString(S[1], "g")+cas;
						String cont = getContent(S[1]);
						
						if(!cont.isEmpty()) {
						
							Group g1 = buildObject(cont, s);							
							if(g1 != null) {																
								list.add(g1);
							}								
						}											
					}
				}
			
				else if(!key.equals("svg") && !key.isEmpty())
				{
					 strlen = svgObject(S, key, index);
					 					 
					 if(strlen > 0) {
						 index += strlen;
						 Shape sh = buildShape(S[1], cas);
						 if(sh!= null) {					
							list.add(sh);						
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
	
	
	public Shape buildShape(String s, String cas) {
		Shape sh = null;

		if(s.indexOf("<rect") > -1) {
			sh = new Rectangle();			
			shape(sh, s, cas);			
		}
		if(s.indexOf("<circle") > -1) {
			sh = new Circle();
			shape(sh, s, cas);
		}
		if(s.indexOf("<ellipse") > -1) {
			sh = new Ellipse();
			shape(sh, s, cas);
		}
		if(s.indexOf("<line") > -1) {
			sh = new Line();
			shape(sh, s, cas);
		}
		if(s.indexOf("<polyline") > -1) {
			sh = new Polyline();
			shape(sh, s, cas);
		}
		if(s.indexOf("<polygon") > -1) {
			sh = new Polygon();
			shape(sh, s, cas);
		}
	
		if(s.indexOf("<path") > -1) {			
			sh = new SVGPath();
			shape(sh, s, cas);
		}
		if(s.indexOf("<text") > -1) {		
			sh = new Text();
			shape(sh, s, cas);
		}
		return sh;
		
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
	
	protected String getContent(String s) { //Get content of a balanced tag

		 int start = s.indexOf(">");
		 int end = s.lastIndexOf("<");
		 if(start > -1 && end > -1)
			 return s.substring(start+1, end-1);
		 return s;
	}	
	
	//THIS FUNCTION MAY USE FOR FLAT SVG STRUCTURE
	protected List<String> getSvgObjectWithRegex(String s) { // Get tag list (for flat svg structure)
		List<String> obList = new ArrayList<String>();		
					
		String key = "((rect)|(circle)|(ellipse)|(line)|(polyline)|(polygon)|(path)|(svg)|(text))";
		Pattern O_REGEX = Pattern.compile("(<("+key+")[^<(/>)>]*>[^<>]*?(<\\2[^<>]*>([^<>]*?(\n))*[^<>]*?</\\2>)*[^<>]*?(</\\2>))|(<"+key+"[^<>]*/>)");

	    Matcher matcher = O_REGEX.matcher(s);	
	
		while (matcher.find()) {
			obList.add(matcher.group(0));
	    }

		return obList;
	}
	

	protected String findKey(String s, int index) { // Find near tag key
		
		String key = "((svg)|(rect)|(circle)|(ellipse)|(line)|(polyline)|(polygon)|(path)|(text)|(g))";
		Pattern K_REGEX = Pattern.compile(".{"+index+"}(<"+key+" )", Pattern.DOTALL);		
		Matcher matcher = K_REGEX.matcher(s);	
		
		if(matcher.find()) {
			String rs = matcher.group(0);
			return rs.substring(rs.lastIndexOf("<")+1, rs.length()-1);
		}
			
		return "";
	}
	
	private String getAttributeString(String s, String key) { // Get all attribute string of a tag
		return s.substring(s.indexOf("<")+key.length()+1, s.indexOf(">"));
	}
	
	public FillRule getFillRule(String s) { // Get fill rule attribute
		
		String valString = getString(s, "fill-rule");
		if(valString.equals("evenodd"))
			return FillRule.EVEN_ODD;
		
		return FillRule.NON_ZERO;
	}
	/**
	* Search and parse stroke line cap
	* @param s String parsing string	
	* @return StrokeLineCap JavaFx Path stroke line cap
	*/
	public StrokeLineCap getStrokeLineCap(String s) { //
		
		s = getString(s, "stroke-linecap");
		if(s.equals("round"))
			return StrokeLineCap.ROUND;
		else if(s.equals("square"))
			return StrokeLineCap.SQUARE;
	
		return StrokeLineCap.BUTT;
	}
	/**
	 * Search and parse stroke line join 
	 * @param s String parsing string
	 * @return StrokeLineJoin JavaFX Path stroke line join
	 */
	public StrokeLineJoin getStrokeLineJoin(String s) {
	
		s = getString(s, "stroke-linejoin");
		
		if(s.equals("miter"))
			return StrokeLineJoin.MITER;
		else if(s.equals("bevel"))
			return StrokeLineJoin.BEVEL;
		
		return StrokeLineJoin.ROUND;
	
	}
	/**
	 * Search and parse stroke miter limit
	 * @param s String parsing string
	 * @return double stroke miter limit value
	 */
	public double getStrokeMiterLimit(String s) {
		return getValue(s, "stroke-miterlimit");
	}
	/**
	 * Search and parse transform
	 * @param s search string
	 * @return Javafx Transform object
	 */
	public Transform getTransform(String s) {

		String trans = getString(s, "transform");
		if(!trans.isEmpty()) {
			
			String arrStr =  trans.substring(trans.indexOf("(")+1, trans.indexOf(")")-1);
			double [] arr =  Arrays.stream(arrStr.split("[\\s,]"))
					 .mapToDouble(Double::parseDouble)
					 .toArray();
			int len = arr.length;
			if(trans.indexOf("rotate") > -1) {
				if(len == 1)
					return new Rotate(arr[0]);
				else if(len == 3)
					return new Rotate(arr[0], arr[1], arr[2]);
				else if(len == 4)
					return new Rotate(arr[0], arr[1], arr[2], arr[3]);				
			}
			else if(trans.indexOf("matrix")>-1) {
				if(len==6)
					return new Affine(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
			}
			return null;
		}
		return null;
	
	}
			
	private String fileContent;			
}