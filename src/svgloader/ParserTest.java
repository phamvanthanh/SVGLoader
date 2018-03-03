package svgloader;

import java.io.FileInputStream;


public class ParserTest {
	public static void main(String... args) throws Exception {
		if (args.length == 0) {
			System.out.println("Usage: java ParserTest anySVGfule");
		//	System.exit(0);
		}
		FileInputStream fis = new FileInputStream("C:/test/test2.svg");
		byte[] rec = new byte[fis.available()];
		int n = fis.read(rec);String svg = new String(rec, 0, n);
		
		SVGParser parser = new SVGParser("C:/test/test2.svg");
//		parser.getSceneObjects();
		
		System.out.println("key svg:"+parser.getString(svg, "svg")+
		"\nkey viewBox:"+parser.getString(svg, "viewBox")+
		"\nkey circle:"+parser.getString(svg, "circle")+
		"\nkey path:"+parser.getString(svg, "path")+
		"\nkey d:"+parser.getString(svg, "d")+
		"\nkey cy:"+parser.getString(svg, "cy")+
		"\nkey x:"+parser.getValue(svg, "x")+
		"\nkey y:"+parser.getValue(svg, "y")+
		"\nkey width:"+parser.getValue(svg, "width")+
		"\nkey height:"+parser.getValue(svg, "height")+
		"\nkey stroke:"+parser.getString(svg, "stroke")+
		"\nkey fill:"+parser.getString(svg, "fill")
		);
	}
}