package svgloader;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javafx.scene.layout.Pane;

public class SVGLoader extends SVGParser {   
	/**    Constructor    
	 * @param svgName  String, svg document Name    
	 * */   
         public long time =0;
	public SVGLoader(String svgName) throws Exception {        
		super(svgName);
		
              	
	}    
	
	/**    Constructor    
	 * @param svgName  String, svg document Name    
	 * @param scaled double, scaling graphics between min.0.05 .... max. 5.0 (5x larger)    
	 * */    
	public SVGLoader(String svgName, double scaled) throws Exception {        
		super(svgName);      
		 
	}    
	
	/**    svgTitle    
	 * @return String the SVG title    
	 * */    
	public String svgTitle(){
            String[] S = {SVG, ""};
            svgObject(S, "titile", 0);
            return S[1];
	}
	
	/**    loadSVG()    
	 * @return Pane JavaFX Pane with SVG image    
	 * */    
	public Pane loadSVG(){              
            pane.getChildren().addAll(createSVG(SVG, "")); 
            return pane;
	}
	
	/**    bufferedSVGImage converts the SVG into JavaFX image    
	 * @return BufferedImage of displaying SVG image    
	 * */    
	public BufferedImage bufferedSVGImage(){
            return null;
	}
	
	/**    save the converted SVG as PNG image    
	 * @param name the name of png file (auto-ending with .png. Exp. 'test' becomes 'test.png'    
	 * */    
	public void saveSVGImage(String name){
		
	}
	
	/* root  is the Pane where the converted SVG graphics are placed    
	 * XML is the string content of SVG document (see SVGParser Constructor)    
	 * idx is the current index (before submerging into next recursive level)    
	 * */    
	private Node createSVG(String xml, String cas) {        
		String key = findKey(xml, 0); 
                
		if(key.equals("svg")) {			 
			 String cont = getContent(xml);
			 if(!cont.isEmpty()) {
				 String attr = getAttributeString(xml, "svg");
				 String xstr = (getString(attr, "x").split("[a-z]")[0]);
                                 double x = 0, y = 0;
                                 if(!xstr.isEmpty())                                     
                                     x = Tool.toDouble(xstr);                                                     
                                 
				 String ystr = getString(attr, "y").split("[a-z]")[0];
                                 if(!ystr.isEmpty())
                                     y = Tool.toDouble(ystr);
                                 
                                 Group g = new Group();
                                 g.setTranslateX(x);
				 g.setTranslateY(y);				
				 attr = attr.replaceAll("(x=\"[0-9\\.]*\")|(y=\"[0-9\\.]*\")|(width=\"[^\"]*\")|(height=\"[^\"]*\")", "")+" "+cas;
                                 		 
				List<String>  list = listObjects(cont); 
                                 
				 g.getChildren().addAll(buildObjectList(list, attr));
				 
				 return g;
			 }			 
		 }
		 else if(key.equals("g")) {
			 String cont = getContent(xml);

			 if(!cont.isEmpty()) {
				
				String attr = getAttributeString(xml, "g")+cas;
				 			
				Group g = new Group();		
				List<String> list = listObjects(cont); 			 				 
				 g.getChildren().addAll(buildObjectList(list, attr));
				 return g;
			 }		
		 }
		 else if(!key.equals("svg") && !key.equals("g") && !key.isEmpty()) {                       
                     long start = System.nanoTime();
                     Shape sh =  shape(xml, cas);
                     long end  = System.nanoTime();
                     time += (end-start);
                     return sh;
		 }
		 return null;
	}
        
    
        
        /**
	* Search and parse string to Javafx objects
	* @param s String parsing string	
	* @param cas String svg object attribute to cascade style in nested structure
	* @return Javafx Group group contains all parsed Javafx objects
	*/
	private List<Node> buildObjectList(List<String> list, String cas){

                List<Node> oList = new ArrayList<Node>();                
                for(String el: list) {	
                    Node n = createSVG(el, cas);
                    if(n!=null)
                        oList.add(n);
                    else
                        return oList;
                }

                return oList;
	}
        
     
	private Pane pane = new Pane();

}
