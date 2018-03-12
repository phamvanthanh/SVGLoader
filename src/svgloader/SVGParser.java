package svgloader;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.FillRule;
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
		

            SVG = (new String(buf, 0, length)).replaceAll("[\\t\\n\\r]+"," ")
                                                .replaceAll(" {2,}", " ")
                                                .replaceAll("\\<\\?xml.+\\?\\>"," ")
                                                .replaceAll("\\<\\?metadata.+\\?\\>"," ")
                                                .replaceAll("<!--[\\s\\S]*?-->", "")
                                                .replaceAll("<!DOCTYPE[^>]*>", "")
                                                .replaceAll("xmlns[^\\s]*\""," ")
                                                .trim();
		  
	}
	/**
	Shaping a geometrical form
	@param S JFX-Shape (Rectangle, Ellipse, Circle, etc.)
	@param s String, the parsing string (e.g. <circle .. style="...."/>)
	*/
	public Shape shape(String s, String cas) { 
            long start = System.nanoTime();
            String attr = "";             
                if(s.indexOf("<path") > -1 ) {   
                    
                    SVGPath sh = new SVGPath();
                    attr = getAttributeString(s, "path")+cas;
		    sh.setContent(svgPathContent(attr));		
		    sh.setFillRule(getFillRule(attr));
		    sh.setStrokeLineCap(getStrokeLineCap(attr));
		    sh.setStrokeLineJoin(getStrokeLineJoin(attr));
		    sh.setStrokeMiterLimit(getStrokeMiterLimit(attr));	
                    setStyle(sh, attr);
                  
                    return sh;
                   
		}
                else if(s.indexOf("<rect") > -1) {
                    
                    attr = getAttributeString(s, "rect")+cas;             
                    SVGPath sh = rectPath(getValue(attr, "x"), 
                                          getValue(attr, "y"), 
                                          getValue(attr, "width"),
                                          getValue(attr, "height"),
                                          getValue(attr, "rx"),
                                          getValue(attr, "ry"));
		    setStyle(sh, attr);
                    return sh;
                    
		}
                else if(s.indexOf("<circle") > -1) {
                    attr = getAttributeString(s, "circle")+cas;
                    SVGPath sh = circlePath(getValue(attr, "cx"),
                                            getValue(attr, "cy"),
                                            getValue(attr, "r"));			
                    setStyle(sh, attr);
                    return sh;
		}
                else if(s.indexOf("<ellipse") > -1) {
                    attr = getAttributeString(s, "ellipse")+cas;
                    SVGPath sh = ellipsePath(getValue(attr, "cx"),
                                             getValue(attr, "cy"),
                                             getValue(attr, "rx"),
                                             getValue(attr, "ry"));
                    attr = getAttributeString(s, "ellipse")+cas;		
                    setStyle(sh, attr);                       
                    return sh;
		}
                else if(s.indexOf("<line") > -1) {
                    attr = getAttributeString(s, "line")+cas;
                    SVGPath sh = linePath(  getValue(attr, "x1"),
                                            getValue(attr, "y1"),
                                            getValue(attr, "x2"),
                                            getValue(attr, "y2"));
                    setStyle(sh, attr);
                    return sh;		
                    
		}
                else if(s.indexOf("<polyline") > -1) {
                    attr = getAttributeString(s, "polyline")+cas;
                    SVGPath sh = polylinePath(doubleArray(getString(attr, "points")));
                    setStyle(sh, attr);
                    return sh;
		}
                else if(s.indexOf("<polygon") > -1) {
                    attr = getAttributeString(s, "polygon")+cas;
                    SVGPath sh = polygonPath(doubleArray(getString(attr, "points")));
		    setStyle(sh, attr);
                    return sh;
		}
		
                else if(s.indexOf("<text") > -1) {
                    attr = getAttributeString(s, "text")+cas;
                    Text sh = new Text();
                    sh.setText(getString(s, "text"));
                    sh.setX(getValue(attr, "x"));
                    sh.setY(getValue(attr, "y"));
                    double fs = getValue(attr, "font-size");
			if(!(fs > 0.0001))
				fs = 14; // Default font size
             
		    sh.setFont(Font.font(getString(attr, "font-family"), fs));
                    setStyle(sh, attr);              
                    return sh;					
		}
                
                
                return null;
				
	}
	/**
	search and parse double array for Polyline and Polygon
	@param s String, the parsing string
	@return double array with 6 elements (see JavaFX or SVG doc)
	*/
	public double[] doubleArray(String s) {
            if(s.indexOf("none") > -1 || s.isEmpty())
                return null;
            return Arrays.stream(s.split("[\\s,]"))
                             .mapToDouble(Double::parseDouble)
                             .toArray();
	}
        
        public Double[] _doubleArray(String s){
            if(s.indexOf("none") > -1 || s.isEmpty())
                return null;
            
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
		ArrayList<String> alStr = split(getString(s, "viewBox"), " ");
                int sz = alStr.size();
                double[] dArr = new double[sz];
                for (int i = 0; i < sz; ++i) 
                    dArr[i] = toDouble(alStr.get(i));
		return dArr;
		
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
			return toDouble(valStr);		
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
			return s.substring(index, s.indexOf('"', index));
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
			int tIndex = s.indexOf('>')+1;
			return s.substring(tIndex, s.indexOf('<', tIndex));
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
	public Paint getColor(String s, String key) {
		
		String color = getString(s, key);		
	
		if (!color.isEmpty()) {
                     
                        if(color.indexOf("url") > -1){
                            
                            String fId = color.substring(color.indexOf('#')+1, color.indexOf(')'));
                            String defs = chaseOut(SVG, fId);                           
                            if(defs.contains("<linearGradient"))
                                return getLinearGradient(defs);
                            else if(defs.contains("<radialGradient")){                             
                                return getRadialGradient(defs);
                            }                                
                        }
			SVGColor svgColor = new SVGColor();
			double op = opacityValue(s, key+"-opacity");
			return svgColor.svgColor(color, op); // SVGColor API
		}
		return null;
			
	}
        
        protected Color getColor(String s, String color, String opacity) {
		
		String c = getString(s, color);		
		
		if (c != null) {
			SVGColor svgColor = new SVGColor();
			double op = opacityValue(s, opacity);	
			
			return svgColor.svgColor(c, op); // SVGColor API
		}
		return null;
			
	}
	/**
	search and parse the Opacity Value of the given key
	@param s String, the parsing string
	@param key String the designated key (e.g. key fill-opacity -> <rect...fill-opacity="0.5".../>)
	@return double opacity value of JavaFX color of the given key*/
	public double opacityValue(String s, String key) {		
		String valStr = getString(s, key);
		if(!valStr.isEmpty())
			return toDouble(valStr);
		return 1;
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
			rst = S[0].substring(start, close);				
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
				
	private int isSelfClose(String s, int index) {
		int close = s.indexOf('>', index);		
		
		if(close > 0) {
			if(s.indexOf("/>", index) == (close -1)) {				
				return close+1;
			}
		}			
		
		return -1;
	}
	
	protected String getContent(String s) { //Get content of a balanced tag

		 int start = s.indexOf('>');
		 int end = s.lastIndexOf('<');
		 if(start > -1 && end > start) {
                        
			 return s.substring(start+1, end);			 
		 }			 
		 return s;
	}	
	
	protected List<String> listObjects(String s) {
		List<String> list = new ArrayList<String>();
		String[] S = {s, ""};
		int index = 0,  strlen = 0, length = S[0].length();	
		String key;
		
		while(index < length)
		{
			key = findKey(s, index);
			
			if(!key.isEmpty())			
			{
				strlen = svgObject(S, key, index);			
				index += strlen;
				list.add(S[1]);
			}
			else {
				return list;
			}			
		}

		return list;
	}
        
        protected List<String> listObjects(String s, String key){
            List<String> list = new ArrayList<String>();
            String[] S = {s, ""};
            int index = 0,  strlen = 0, length = S[0].length();	
            
            while(index < length)
            {                
                strlen = svgObject(S, key, index);
                if(strlen == 0)
                    return list;
                index += strlen;
                list.add(S[1]);                               
            }

            return list;
        }
	
	//THIS FUNCTION MAY USE FOR FLAT SVG STRUCTURE
	protected List<String> regexListObjects(String s) { // Get tag list (for flat svg structure)
            List<String> obList = new ArrayList<String>();
            Pattern O_REGEX = Pattern.compile("(<("+gkey+")[^<(/>)>]*(\\(.*\\))*[^<(/>)>]*>[^<(/>)>]*(</\\2>))|(<"+gkey+"[^<>]*(\\(.*\\))*[^<(/>)>]*/>)");
            Matcher matcher = O_REGEX.matcher(s);	

            while (matcher.find()) {
                   obList.add(matcher.group(0));
            }	

            return obList;
	}
	

	protected String findKey(String s, int index) { // Find nearest tag key (combine methods for better speed)		
		
		int start = s.indexOf('<', index);
		if(start > -1 && start < s.length()-9) {
                        if(s.indexOf("</", index) == start || s.indexOf("< /", index) == start){
                            return loopFindKey(s, index);
                        }
                        else {
                            int end1 = s.indexOf(' ', start+1);
                            int end2 = s.indexOf('>', start+1);
                            if(end1 > -1 && end1 < end2)
                                return s.substring(start+1, end1);
                            else
                                return s.substring(start+1, end2);
                        }            
		} 
                return "";		
		 
	}
        
        private String loopFindKey(String s, int index){
            	int ind = s.length(); int curInd = -1; String key = ""; 
		int length = keys.length;
		for(int i =0; i< length; i++) {
			curInd = s.indexOf("<"+keys[i],index);
			if(curInd > -1 && curInd < ind) {
				ind = curInd;
				key = keys[i];
			}
		}
                return key;
        }
        	
	protected String getAttributeString(String s, String key) { // Get all attribute string of a tag		
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
			
			String arrStr =  trans.substring(trans.indexOf("(")+1, trans.indexOf(")"));
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
        protected String chaseOut(String s, String key){
            int pos = s.indexOf(key);
            
            if(pos > 0){
                int begin = s.lastIndexOf('<', pos);
                String tag = findKey(s, begin);
                String[] S = {s, tag};
                svgObject(S, tag, begin);
                return S[1];          
            }
            return "";
        }
        protected RadialGradient getRadialGradient(String s){
                  
            double cx = 0.5, cy = 0.5, r = 0.5, fa = 0.0, fd = 0.0, fx = 0.5, fy = 0.5;
             String cxS = getString(s,"cx"),
                    cyS = getString(s,"cy"),
                    rS = getString(s,"r"),
                    fxS = getString(s,"fx"),
                    fyS = getString(s,"fy");
             		
            if(!cxS.isEmpty())                
                cx = toDouble(cxS.replace("%", ""))/100;
            if(!cyS.isEmpty())
                cy = toDouble(cyS.replace("%", ""))/100;
            if(!rS.isEmpty())
                r =  toDouble(rS.replace("%", ""))/100;            
                
            if(!fyS.isEmpty()){
                fy = toDouble(fyS.replace("%", ""))/100;                
            }
            if(!fxS.isEmpty()){            	
                fx = toDouble(fxS.replace("%", ""))/100; 
               
                if(fx != 0.5) { 
               	 
                    fa = Math.atan((fy-0.5)/(fx-0.5))*180/Math.PI;
                    if(fx < 0.5)
                       fa += 180;
                    fd = Math.sqrt((fx-0.5)*(fx-0.5)+(fy-0.5)*(fy-0.5));
                 
               }
               else if(fy < 0.5) {
            	   fa = 90;
               }               		
               else if(fy > 0.5) {
            	   fa = 270;
               }                       
            }          		
              
            List<Stop> sList = buildStopList(listObjects(getContent(s), "stop"));
            RadialGradient lg = new RadialGradient(fa, fd, cx, cy, r, true, CycleMethod.NO_CYCLE, sList);
           
            return lg;
        }
        protected LinearGradient getLinearGradient(String s){

            
            double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
            String x1Str = getString(s,"x1").replace("%", "");
            String y1Str = getString(s,"y1").replace("%", "");
            String x2Str = getString(s,"x2").replace("%", "");
            String y2Str = getString(s,"y2").replace("%", "");
            if(!x1Str.isEmpty())
                x1 = toDouble(x1Str)/100;
            if(!y1Str.isEmpty())
                y1 = toDouble(getString(s,"y1").replace("%", ""))/100;
            if(!x2Str.isEmpty())
                x2 = toDouble(getString(s,"x2").replace("%", ""))/100;
            if(!y2Str.isEmpty())
                y2 = toDouble(getString(s,"y2").replace("%", ""))/100;
//            String spmd = getString(s, "spreadMethod");          

           
            List<Stop> sList = buildStopList(listObjects(getContent(s), "stop"));
            LinearGradient lg = new LinearGradient(x1, y1, x2, y2, true, CycleMethod.NO_CYCLE, sList);
            return lg;
        }
        
        protected List<Stop> buildStopList(List<String> list){
            List<Stop> sList = new ArrayList<Stop>();
            
            for(String s: list){            	
                sList.add(new Stop(Double.parseDouble(getString(s,"offset").replace("%", ""))/100, 
                                    getColor(s, "stop-color", "stop-opacity")));
               
            }
          
            return sList;
        }
               
       
        
        protected SVGPath rectPath(double x, double y, double width, double height, double rx, double ry){
          
            String c1 = " A"+rx+" "+ry+" "+45+" "+0+" "+1+" "+(x+rx)+" "+(y);            
            String c2 = " A"+rx+" "+ry+" "+135+" "+0+" "+1+" "+(x+width)+" "+(y+ry);
            String c3 = " A"+rx+" "+ry+" "+90+" "+0+" "+1+" "+(x+width-rx)+" "+(y+height);
            String c4 = " A"+rx+" "+ry+" "+135+" "+0+" "+1+" "+(x)+" "+(y+height-ry); 
            SVGPath p = new SVGPath();
            p.setContent("M"+x+","+(y+ry)+ c1+
                         " L"+(x+width-rx)+","+y+c2+
                         " L"+(x+width)+","+(y+height-ry)+c3+
                         " L"+(x+rx)+","+(y+height)+c4+
                         " z");
            return p;
           
        }
        private SVGPath polylinePath(double[] points){
           int length = points.length;
           String s = "M"+points[0]+","+points[1];
           for(int i = 2; i < length; i = i+2){
                s +=" L"+points[i]+","+points[i+1];
           }
           SVGPath p = new SVGPath();
           p.setContent(s);
           return p;
        }
        private SVGPath polygonPath(double[] points){
           int length = points.length;
           String s = "M"+points[0]+","+points[1];
           for(int i = 2; i < length; i = i+2){
                s +=" L"+points[i]+","+points[i+1];
           }
           SVGPath p = new SVGPath();
           p.setContent(s+" z");
           return p;
        }
        private SVGPath linePath(double x1, double y1, double x2, double y2){
            SVGPath p = new SVGPath();
            p.setContent("M"+x1+","+y1+" L"+x2+","+y2);
            return p;
        }
        private SVGPath circlePath(double cx, double cy, double r){
            String s = "M " + (cx-r) + ", "+cy+
                      " a"+r+","+r+ " 0 1,0 "+(2*r)+",0"+
                      " a "+r+","+r+ " 0 1,0 "+ "-"+(2*r)+",0";  
		
            SVGPath p = new SVGPath();
            p.setContent(s);
            return p;
          
        }
        private SVGPath ellipsePath(double cx, double cy, double rx, double ry){
            String s = "M " + (cx-rx) + ", "+cy+
			" a"+rx+","+ry+ " 0 1,0 "+ (2*rx)+",0"+
			" a "+rx+","+ry+ " 0 1,0 "+(-2*rx)+",0";         
            SVGPath p = new SVGPath();
            p.setContent(s);         
            return p;
             
        }
        private void setStyle(Shape sh, String s){
               
                if(getColor(s, "stroke") == null)
                    sh.setFill(null);    
                else
                     sh.setStroke(getColor(s, "stroke"));
                Paint paint = getColor(s, "fill");
                if(paint == null)
                     sh.setFill(Color.BLACK);              
                else                   
                    sh.setFill(paint);
		double sw = getValue(s, "stroke-width");
		if(!(sw > 0.0000001) )
			sw = 1;
		sh.setStrokeWidth(sw);
		sh.setOpacity(opacityValue(s, "opacity"));
		
		String arr = getString(s, "stroke-dasharray");
                Double[] dArr = _doubleArray(arr);
		if(dArr != null)
			sh.getStrokeDashArray().addAll(dArr);		
                
//		Transform trans = getTransform(s);
//		if(trans != null)
//			sh.getTransforms().add(trans);	
               
        }
        
        private ArrayList<String> split(String S, String k) {
            int b, i = 0;
            int ls = S.length();
            int lk = k.length();
            ArrayList<String> al = new ArrayList<>();
            OUT:for (b = 0; i < ls; ++i) {
                if (S.charAt(i) == k.charAt(0)) {
                    int l, j = i + 1;
                    for (l = 1; l < lk && j < ls; ++l, ++j)
                        if (S.charAt(j) != k.charAt(l)) continue OUT;
                    if (l == lk) {
                        String X = S.substring(b,j).trim();
                        if (X.length() > 0) al.add(X);
                        i = b = j;
                    }
                }
            }
            if (b < ls) {
                String X = S.substring(b,ls).trim();
                al.add(X);
            }
            return al;
        }
    
        private  int toInt(String I) {
            boolean neg;
            int li, i = 0;
            if (I.charAt(0) == '-'){
                neg = true;
                li = 1;
            } else {
                neg = false;
                li = 0;
            }
            for (int b = I.length()-1, f = 1; b >= li; --b) {
                i += (I.charAt(b)&0x0F)*f;
                f *= 10;
            }
            return (neg? -i: i);
        }
        private double toDouble(String D) {
            boolean neg;
            double d = 0;
            int li, i = 0;
            if (D.charAt(0) == '-'){
                neg = true;
                li = 1;
            } else {
                neg = false;
                li = 0;
            }
            if (D != null) {
                int len = D.length();
                for (int b = 0; b < len; ++b)
                if (D.charAt(b) == '.') {
                    double f = 10d;
                    for (int e = b+1; e < len; ++e) {
                        d += (double)(D.charAt(e)&0x0F)/f;
                        f *= 10;
                    }
                    f = 1d;
                    for (--b; b >= li; --b) {
                        d += (double)(D.charAt(b)&0x0F)*f;
                        f *= 10;
                    }
                    return (neg? -d:d);
                }
                double f = 1d;
                for (int b = len-1; b >= li; --b) {
                    d += (D.charAt(b)&0x0F)*f;
                    f *= 10;
                }
            }
            return (neg? -d:d);
        }
			
	protected String SVG;			
	protected StringBuffer gkey = new StringBuffer("((svg)|(g)|(clipPath)|(rect)|(circle)|(ellipse)|(line)|(polyline)|(polygon)|(path)|(text)|(defs))");
	protected String[] keys = {"svg", "g", "clipPath", "polygon", "polyline", "rect", "line", "ellipse", "circle", "path", "text", "defs", "linearGradient", "radialGradient"};

}
