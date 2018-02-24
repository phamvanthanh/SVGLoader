package svgloader;
import javafx.application.Application;
import svgloader.SVGColor;
// More about SVG: see https://www.w3.org/TR/SVG/expanded-toc.html
public class TestSVGColor extends Application {
    public void start(Stage stage) throws Exception {
        // load passing arguments
        Application.Parameters params = getParameters();
        java.util.List<String> pl = params.getRaw();
        // >>>>>>>>>>>The new SVGColor API<<<<<<<<<<<<

        SVGColor svgColor = new SVGColor();
        Pane pane = new Pane();
        Circle circle = new Circle(10, svgColor.svgColor("#0000ff"));
        /* or with Opacity degree 0.5 (or 50%)
        Circle circle = new Circle(10, svgColor.svgColor("blue", 0.5d));
        */
        pane.getChildren().add(circle);
        stage.setScene(new Scene(pane, Color.ANTIQUEWHITE));
       stage.show();
    }
}