package svgloader;

import java.io.FileInputStream;


public class ParserTest {
	public static void main(String... args) throws Exception {
		if (args.length == 0) {
			System.out.println("Usage: java ParserTest anySVGfule");
			System.exit(0);
		}
		FileInputStream fis = new FileInputStream(args[0]);
		byte[] rec = new byte[fis.available()];
		int n = fis.read(rec);String svg = new String(rec, 0, n);
		
		SVGParser parser = new SVGParser(svg);
		
		System.out.println("key svg:"+parser.getString(svg, "svg")+
		"\nkey circle:"+parser.getString(svg, "circle")+
		"\nkey cy:"+parser.getString(svg, "cy")+
		"\nkey height:"+parser.getString(svg, "height")+
		"\nkey stroke:"+parser.getColor(svg, "stroke").toString());
		fis.close();
	}
}