/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package svgloader;

/**
 *
 * @author Thanh
 */

	
import javafx.application.Application;

import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;


public class JavaFxTest extends Application {
	
	public void start(Stage primaryStage) {
		try {
			
			Application.Parameters params = getParameters();
                        java.util.List<String> pl = params.getRaw();
                        SVGLoader loader = null;
	
			
			StackPane  pane = new StackPane();
			
			
			Parent root = new Pane(pane);												
			Scene scene = new Scene(root);			
			primaryStage.setScene(scene);			
			primaryStage.setTitle("Test Window");		
			
                       
                        if(pl.isEmpty())
                            loader = new SVGLoader("C:/test/9.svg");
                        else
                            loader = new SVGLoader(pl.get(0));	
                        long start = System.currentTimeMillis();
                        Pane svgPane = loader.loadSVG();
//                        svgPane.setScaleX(0.5);
//                        svgPane.setScaleY(0.5);
                        pane.getChildren().addAll(svgPane);
                        svgPane.setCache(true);
                        long end = System.currentTimeMillis();
                        primaryStage.show();
			
                        System.out.println("Version: "+com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());
			System.out.println("Total time: "+(end-start)+ " mili secs");
                  	System.out.println("Measure time: "+loader.time/1000000+ " mili secs");	
                        System.out.println("Measure count: "+loader.count+ " times");
                        Runtime rt = Runtime.getRuntime(); 
                        long total_mem = rt.totalMemory(); 
                        long free_mem = rt.freeMemory(); 
                        long used_mem = total_mem - free_mem; 
                        System.out.println("Amount of used memory: " + used_mem/1000000); 
                        
		} catch(Exception e) {		
		}
		
	}
	public static void main(String[] args) {
		
		launch(args);
		
	}
}