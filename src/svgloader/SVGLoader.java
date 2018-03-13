package svgloader;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javafx.scene.Group;
import javafx.scene.Node;


import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class SVGLoader extends SVGParser {   
	/**    Constructor    
	 * @param svgName  String, svg document Name    
	 * */   
        
       
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
        private ExecutorService executor = Executors.newFixedThreadPool(6);
	public List<Node> createSVG(String xml, String cas) {        
		String key = findKey(xml, 0, keys); 
                List<Node> nList = new ArrayList<Node>();
        
		if(key.equals("svg")) {
			 
			 String cont = getContent(xml);
			 if(!cont.isEmpty()) {
				 String attr = getAttributeString(xml, "svg") + cas;
				 String xstr = (getString(attr, "x").split("[a-z]")[0]);
                                 double x = 0, y = 0;
                                 if(!xstr.isEmpty())                                     
                                     x = Tool.toDouble(xstr);                                                     
                                 
				 String ystr = getString(attr, "y").split("[a-z]")[0];
                                 if(!ystr.isEmpty())
                                     y = Tool.toDouble(ystr);
                                 
                                 Group g = new Group();
                                 g.setLayoutX(x);
				 g.setLayoutY(y);  
                                 attr = removeSVGAttributes(attr) + cas;
//				 attr = attr.replaceAll("(x=\"[0-9\\.]*\")|(y=\"[0-9\\.]*\")|(width=\"[^\"]*\")|(height=\"[^\"]*\")", "")+" "+cas;
            
				 List<String> list = listObjects(cont, keys);                                
                                 g.getChildren().addAll(buildObjectList(list, attr));                                 
                                 nList.add(g);
                                 return nList;
			 }			 
		 }
		 else if(key.equals("g")) {
			 
                         String attr = getAttributeString(xml, "g");
                         String cont = getContent(xml);
                         if(validateAttr(attr)){
                            
                            if(!cont.isEmpty()) {
                                
                                    attr = attr + cas;
                                    Group g = new Group();                                    
 
                                    List<String>   list = listObjects(cont, keys);

                                    g.getChildren().addAll( buildObjectList(list, attr));
                                    nList.add(g);  
                                   
                                    return nList;
                            }
                            else
                                return nList;
                                
                         }
                         else {
                             List<String>  list = listObjects(cont, keys);
                             return buildObjectList(list, cas);
                         }
                         	
		 }
                 else if(key.equals("defs")){
                     return nList;
                 }
                 else if(key.equals("text")){
                    Text text = new Text();
                    nList.add(text);
                    executor.submit(new TextBuilder(text, xml, cas, this));
                   
//                    executor.awaitTermination(5, TimeUnit.MINUTES);
                    return nList;     
                 }
		 else if(!key.equals("svg") && !key.equals("g") && !key.isEmpty()) {                       
                    SVGPath shape = new SVGPath();
                    nList.add(shape);
                    executor.submit(new ShapeBuilder(shape, xml, cas, this));
                 
                    return nList;          
                     
                     
		 }
		 return nList;
	}
        
    
        
        /**
	* Search and parse string to Javafx objects
	* @param s String parsing string	
	* @param cas String svg object attribute to cascade style in nested structure
	* @return Javafx Group group contains all parsed Javafx objects
	*/
       
	private List<Node> buildObjectList(List<String> list, String cas){
                
               	List<Node> oList = new ArrayList<Node>();
                int length = list.size();
		for(int i = 0; i < length; i++) {               
           
                    List<Node> nodes = createSVG(list.get(i), cas);                 
                    oList.addAll(nodes); 
		}

		return oList;
	}
         private String removeSVGAttributes(String s){

        
            int index  = s.indexOf("x=");
            if(index > -1)
                s = s.replace(s.substring(index, s.indexOf("\"", index+4)), " ");
           
            index  = s.indexOf("y=");
            if(index > -1)
                s = s.replace(s.substring(index, s.indexOf("\"", index+4)), " ");
            
            index  = s.indexOf("width=");
            if(index > -1)
                s = s.replace(s.substring(index, s.indexOf("\"", index+8)), " ");
            
            index  = s.indexOf("height=");
            if(index > -1)
                s = s.replace(s.substring(index, s.indexOf("\"", index+9)), " ");
            return s;
                
        }
             
	private Pane pane = new Pane();

}

class ShapeBuilder implements Runnable {
    private String xml;
    private String cascade;
    private SVGLoader svgloader;
    private SVGPath sh;
    
    ShapeBuilder(SVGPath shape, String s, String cas, SVGLoader loader){
        xml = s;
        cascade = cas;
        svgloader = loader;
        sh = shape;
    }
    @Override
    public void run() {
        svgloader.shape(sh, xml, cascade);
         
    }
}
class TextBuilder implements Runnable {
    private String xml;
    private String cascade;
    private SVGLoader svgloader;
    private Text sh;
    
    TextBuilder(Text shape, String s, String cas, SVGLoader loader){
        xml = s;
        cascade = cas;
        svgloader = loader;
        sh = shape;
    }
    @Override
    public void run() {
        svgloader.text(sh, xml, cascade);         
    }
}
