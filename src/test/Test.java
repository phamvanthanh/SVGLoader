package test;
	
import javafx.application.Application;

import javafx.stage.Stage;
import svgloader.SVGParser;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;


public class Test extends Application {
	
	public void start(Stage primaryStage) {
		try {
			
			Application.Parameters params = getParameters();
	        java.util.List<String> pl = params.getRaw();
	        SVGParser parser = null;
	        if(pl== null)
	        	parser = new SVGParser("C:/SVG/src/test/test3.svg");
	        else
	        	parser = new SVGParser(pl.get(0));
	        
			long start = System.currentTimeMillis();
			Group g = parser.getObject();	
			long end = System.currentTimeMillis();
			System.out.println("Time build: "+(end-start));
			
			Pane  pane = new Pane();
			pane.getChildren().addAll(g);
			
			Parent root = new Pane(pane);												
			Scene scene = new Scene(root);			
			primaryStage.setScene(scene);			
			primaryStage.setTitle("Test Window");		
			primaryStage.show();
						
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		
		launch(args);
		
	}
}
