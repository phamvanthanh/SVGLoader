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
	SVGLoader loader = null;
	public void start(Stage primaryStage) {
		try {
			
			Application.Parameters params = getParameters();
                        java.util.List<String> pl = params.getRaw();
                        
				
			StackPane  pane = new StackPane();
						
			Parent root = new Pane(pane);												
			Scene scene = new Scene(root);
                        
			primaryStage.setScene(scene);			
			primaryStage.setTitle("Test SVGLoader Window");		
			
                           long start = System.currentTimeMillis();
                        if(pl.isEmpty())
                            loader = new SVGLoader("C:/test/Main/1.svg");
                        else
                            loader = new SVGLoader(pl.get(0));	
                     


                        pane.getChildren().addAll(loader.loadSVG());
                        pane.setCache(true);
                        long end = System.currentTimeMillis();
                        primaryStage.show();
//                        loader.close();
			
                        System.out.println("Version: "+com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());
			System.out.println("Total time: "+(end-start)+ " mili secs");
                  	
                       
                      
                        
		} catch(Exception e) {		
		}
		
	}
        public void stop() {
            loader.shutdown();  // shutdown the pool
        }
	public static void main(String[] args) {
		
		launch(args);
		
	}
}