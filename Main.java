package fodt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

public class Main {

	public static void main(String[] args) throws DocumentException, ParserConfigurationException, SAXException, IOException {
		TagMap mapConfig = new TagMap();
		Map<String, List<tag>> test = mapConfig.readConfig();
		
		
		FodtReader fodtReader = new FodtReader(test);
		if (!fodtReader.readFodtFile()){
			System.out.println("Error: Reader fodt file is failed!");
		}
		
		
	}

}
