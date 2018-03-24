package svgloader;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class SVGLoader extends SVGParser {   
	/**    Constructor    
	 * @param svgName  String, svg document Name    
	 * */   
        
        long listime = 0;
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
        svgObject(S, "title", 0);
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
//        private final ExecutorService executor = Executors.newFixedThreadPool(5*Runtime.getRuntime().availableProcessors());
    private final ExecutorService executor = Executors.newWorkStealingPool(5*Runtime.getRuntime().availableProcessors());
    public List<Node> createSVG(String xml, String cas) { 
        
        String key = findKey(xml, 0, keys); 
//              
        List<Node> nList = new ArrayList<Node>();
        char fc = key.charAt(0);
        if(fc == 's' && key.charAt(2)== 'g') { //svg
	    xml = removeWeirds(xml);
	    String cont = getContent(xml);
	    if(!cont.isEmpty()) {
	        String attr = getAttributeString(xml, "svg");

	        double x = getValue(attr, "x");
                double y = getValue(attr, "y");
                Group group = new Group();
                nList.add(group);
                group.setLayoutX(x);
	        group.setLayoutY(y);
                                group(group, xml, cas);
//                executor.submit(new GroupBuilder(group, xml, cas, this));
                List<String>  list = listObjects(cont, keys);  

                attr = removeUncascadedttributes(attr) + cas;

                group.getChildren().addAll(buildObjectList(list, attr));                                 
                                
                return nList;
            }			 
	}
	else if(fc == 'g') { //g
                         
            String attr = getAttributeString(xml, "g");
            String cont = getContent(xml);

            if(validateAttr(attr)){

                if(!cont.isEmpty()) {   
                                 
                    Group group = new Group(); 
                    nList.add(group);
                    group(group, xml, cas);
//                    executor.submit(new GroupBuilder(group, xml, cas, this));
                                        
                    List<String>  list = listObjects(cont, keys);
                    attr = removeUncascadedttributes(attr) + cas; 
                                    
                    group.getChildren().addAll(buildObjectList(list, attr));                                  
                    return nList;
                }
                else
                    return nList;
                                
            }
            else {

                List<String> list = listObjects(cont, keys);                       
                return buildObjectList(list, cas);
            }
                         	
	}
        else if(fc == 'u'){
                       
            Group use = new Group(); 
            nList.add(use);
                        use(use, xml, cas);
//            executor.submit(new UseBuilder(use, xml, cas, this));

            String attr = getAttributeString(xml, "use")+cas;
                     
            attr = removeUncascadedttributes(attr);
            use.getChildren().addAll(getSymbol(attr));

            return nList;
                   
        }
        else if(fc == 'd'){ //defs
            return nList;
        }
        else if(fc == 't' && key.length() == 4){ //text
            if(xml.contains("<tspan")){
                String cont = getContent(xml);
                xml = xml.replace("<text", "<g");
                xml = xml.replace("/text>", "/g>");
                        
                Group group = new Group(); 
                nList.add(group);
                group(group, xml, cas);
//                executor.submit(new GroupBuilder(group, xml, cas, this));
                        
                String attr = getAttributeString(xml, "g");

                attr = removeUncascadedttributes(attr) + cas;
                List<String>  list = textSegregate(cont);                 
  
                group.getChildren().addAll(buildObjectList(list, attr)); 
                return nList;
                        
            }
            else {
                Text text = new Text();

                text(text, xml, cas);
//                executor.submit(new TextBuilder(text, xml, cas, this));
                nList.add(text);
                return nList;    
            }
                     
        }
        else if(fc == 't' && key.length() == 5){
                      
            Text text = new Text(); 
            nList.add(text);
          text(text, xml, cas);
//            executor.submit(new tspanBuilder(text, xml, cas, this));
                        
            return nList;   
        }
        else if(fc == 'i'){ // image/img
                    
            ImageView img = new ImageView();
            nList.add(img);   
          image(img, xml, cas);
//            executor.submit(new ImageBuilder(img, xml, cas, this));
                                    
            return nList;
        }              
	    else if(!key.isEmpty()) {
                    
            SVGPath shape = new SVGPath();
            nList.add(shape);
          shape(shape, xml, cas);
//            executor.submit(new ShapeBuilder(shape, xml, cas, this));              
                                
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
      
    public List<Node> buildObjectList(List<String> list, String cas){
       
        List<Node> oList = new ArrayList<Node>();              
              
        int length = list.size();
	for(int i = 0; i < length; i++) { 
                  
            List<Node> nodes = createSVG(list.get(i), cas);
            oList.addAll(nodes); 
        }
        return oList;

    }
        
    public void shutdown(){
        executor.shutdown(); 
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
    private Text text;
    
    TextBuilder(Text tx, String s, String cas, SVGLoader loader){
        xml = s;
        cascade = cas;
        svgloader = loader;
        text = tx;
    }
    @Override
    public void run() {
        svgloader.text(text, xml, cascade);         
    }
}

class tspanBuilder implements Runnable {
	
    private String xml;
    private String cascade;
    private SVGLoader svgloader;
    private Text text;
    
    tspanBuilder(Text tx, String s, String cas, SVGLoader loader){
        xml = s;
        cascade = cas;
        svgloader = loader;
        text = tx;
    }
    @Override
    public void run() {
        svgloader.tspan(text, xml, cascade);         
    }
}

class ImageBuilder implements Runnable {
	
    private String xml;
    private String cascade;
    private SVGLoader svgloader;
    private ImageView image;
    
    ImageBuilder(ImageView img, String s, String cas, SVGLoader loader){
        xml = s;
        cascade = cas;
        svgloader = loader;
        image = img;
    }
    @Override
    public void run() {
        svgloader.image(image, xml, cascade);         
    }
}

class GroupBuilder implements Runnable {
	
    private String xml;
    private String cascade;
    private SVGLoader svgloader;
    private Group group;
    
    GroupBuilder(Group g, String s, String cas, SVGLoader loader){
        xml = s;
        cascade = cas;
        svgloader = loader;
        group = g;
    }
    @Override
    public void run() {
        svgloader.group(group, xml, cascade);         
    }
}

class UseBuilder implements Runnable {
	
    private String xml;
    private String cascade;
    private SVGLoader svgloader;
    private Group group;
    
    UseBuilder(Group g, String s, String cas, SVGLoader loader){
        xml = s;
        cascade = cas;
        svgloader = loader;
        group = g;
    }
    @Override
    public void run() {
        svgloader.use(group, xml, cascade);         
    }
}

class SVGTask implements Callable<List<Node>>{
	
    private String xml;
    private String cascade;
    private SVGLoader loader;
    SVGTask(String s, String cas,  SVGLoader svgloader) {
        xml = s;
        cascade = cas;
        loader = svgloader;
    }

    @Override
    public List<Node> call() throws Exception {
       return loader.createSVG(xml, cascade);
    }
   
    
}