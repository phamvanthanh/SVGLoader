package svgloader;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import java.util.concurrent.TimeUnit;


import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;


import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;


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
            String[] S = {SVG.toString(), ""};
            svgObject(S, "title", 0);
            return S[1];
	}
	
	/**    loadSVG()    
	 * @return Pane JavaFX Pane with SVG image    
	 * */    
	public Pane loadSVG(){              
            pane.getChildren().addAll(createSVG(SVG, "")); 

//            executor.shutdown();
//            try {
//                
//                executor.awaitTermination(1, TimeUnit.HOURS);
//            } catch (Exception e) {
//
//            }
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
   
	public List<Node> createSVG(String xml, String cas) {        
		String key = findKey(xml, 0, keys); 
                List<Node> nList = new ArrayList<Node>();
        
		if(key.charAt(0) == 's' && key.charAt(2)== 'g') {
			 
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
                                 
                                Group group = new Group();
                                group.setLayoutX(x);
				group.setLayoutY(y);
//                                group(group, xml, cas);
                                executor.submit(new GroupBuilder(group, xml, cas, this));
                             
                                List<String>  list = listObjects(cont, keys);                     
                           
                                attr = removeSVGAttributes(attr) + cas;
                                
                                int index = attr.indexOf("transform");                                    
                                if(index > -1)
                                     attr = attr.replace(attr.substring(index, attr.indexOf(')', index+12)), " ");
                                index = attr.indexOf("clip-path");

                                if(index > -1)
                                    attr = attr.replace(attr.substring(index, attr.indexOf(')', index+12)), " ");

                                index = attr.indexOf("mask");
                                if(index > -1)
                                    attr = attr.replace(attr.substring(index, attr.indexOf(')', index+12)), " ");
                               
                                group.getChildren().addAll(buildObjectList(list, attr));                                 
                                nList.add(group);
                                return nList;
			 }			 
		 }
		 else if(key.charAt(0) == 'g') {
			 
                         String attr = getAttributeString(xml, "g");
                         String cont = getContent(xml);
                         if(validateAttr(attr)){

                            if(!cont.isEmpty()) {      
                                    //Remove un-cascaded attributes
                                    int index = attr.indexOf("transform");                                    
                                    if(index > -1)
                                         attr = attr.replace(attr.substring(index, attr.indexOf(')', index+12)), " ");
                                    index = attr.indexOf("clip-path");
                                    
                                    if(index > -1)
                                        attr = attr.replace(attr.substring(index, attr.indexOf(')', index+12)), " ");
                                    
                                    index = attr.indexOf("mask");
                                    if(index > -1)
                                        attr = attr.replace(attr.substring(index, attr.indexOf(')', index+12)), " ");
                                    
                                    Group group = new Group();
//                                    group(group, xml, cas);
                                    executor.submit(new GroupBuilder(group, xml, cas, this));
                                    nList.add(group);
                                    
                                    List<String>  list = listObjects(cont, keys);
                                    attr = attr + cas; 
                                    
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
//                 else if(key.equals("defs")){
//                     return nList;
//                 }
                 else if(key.charAt(0) == 't' && key.length() == 4){
                     
                    Text text = new Text(); 
//                    text(text, xml, cas);
                    executor.submit(new TextBuilder(text, xml, cas, this));
                    nList.add(text);
                    return nList;     
                 }
                 else if(key.charAt(0) == 'i'){
                   
                    ImageView img = new ImageView();
//                    image(img, xml, cas);
                    executor.submit(new ImageBuilder(img, xml, cas, this));
                    nList.add(img);
                    return nList;
                 }              
		 else if(!key.isEmpty()) {
                    
                    SVGPath shape = new SVGPath();
//                    shape(shape, xml, cas);
                    executor.submit(new ShapeBuilder(shape, xml, cas, this));
              
                    nList.add(shape);  

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
//        protected ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
	public List<Node> buildObjectList(List<String> list, String cas){
              
            List<Node> oList = new ArrayList<Node>();
            
//            RSVGTask rsvgtask = new RSVGTask(list, cas, this);

//            oList = forkJoinPool.invoke(rsvgtask);
             
           
            int length = list.size();
//            if(length < 10000)
            for(int i = 0; i < length; i++) {           
                if(length < 5000){
                    List<Node> nodes = createSVG(list.get(i), cas);                 
                    oList.addAll(nodes); 
                }
                   
            }
        /* 
            else{
                List<SVGTask> threads = new ArrayList<>();
             
                for(int i = 0; i < length; i++){             
                        
                    threads.add(new SVGTask(list.get(i), cas, this));
                }
                try {
                        //launch the threads
                        List<Future<List<Node>>> futures = executor.invokeAll(threads);
                        //read results
                        for (Future<List<Node>> future_ : futures) {
                            oList.addAll(future_.get());
                        }

                    } catch (Exception e) {

                    }                 

            }*/
           
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

class RSVGTask extends RecursiveTask<List<Node>> {
    private List<String> list;
    private String cascade;
    private SVGLoader loader;
    RSVGTask(List<String> lst, String cas, SVGLoader svgloader) {
        list = lst;
        cascade = cas;
        loader = svgloader;
    }
    @Override
    protected List<Node> compute() {
       if(list.size() < 100)
           return loader.buildObjectList(list, cascade);
       else {
           
            List<RSVGTask> subtasks =  new ArrayList<RSVGTask>();
            subtasks.addAll(createSubtasks());
            for(RSVGTask subtask : subtasks){
                subtask.fork();
            }
            
            List<Node> result =  new ArrayList<Node>();
            for(RSVGTask subtask : subtasks) {
                List<Node> r = subtask.join();
               
                result.addAll(r);
            }
            return result;
           
       }
       
    }
    
     private List<RSVGTask> createSubtasks() {
        List<RSVGTask> subtasks =  new ArrayList<RSVGTask>();
        int pivot = (int)(list.size()/2);
        RSVGTask subtask1 = new RSVGTask(list.subList(0, pivot), cascade, loader);
        RSVGTask subtask2 = new RSVGTask(list.subList(pivot+1, list.size()-1), cascade, loader);

        subtasks.add(subtask1);
        subtasks.add(subtask2);
//        System.out.println(subtasks);

        return subtasks;
    }
    
}
