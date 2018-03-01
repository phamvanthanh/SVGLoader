package svgloader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import svgloader.SVGColor;

// More about SVG: see https://www.w3.org/TR/SVG/expanded-toc.html
public class TestSVGColor extends Application {
	@Override
	public void start(Stage stage) throws Exception {
        // load passing arguments
        Application.Parameters params = getParameters();
        java.util.List<String> pl = params.getRaw();
        // >>>>>>>>>>>The new SVGColor API<<<<<<<<<<<<

        SVGColor svgColor = new SVGColor();
        Pane pane = new Pane();
        
        svgColor.setOpacity(1);

        Color c = svgColor.svgColor("#bcbcbc");
//        System.out.println(c);
        
        Circle circle = new Circle(100, c);
        
        circle.setLayoutX(100);
        circle.setLayoutY(100);
        
        pane.getChildren().add(circle);
        stage.setScene(new Scene(pane, Color.ANTIQUEWHITE));
       stage.show();
    }
	
	public static void main(String[] args){
	  Application.launch(args);
	}


}