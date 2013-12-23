package fodt;


import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
*/
/**
 * 
 * @author Lei
 *
 */
public class TagMap extends DefaultHandler{
	
	// Stock all tags. 
	// key: tag's name. 
	// List<tag>: a tag contains name, attributes. 
	private Map<String, List<tag>> mapTag = null;

	private String preTag = null;
	private tag tagNew = null;
	private List<tag> lstTag = null;
	
	public Map<String, List<tag>> getMapTag() {
		return mapTag;
	}
	

	
	/**
	 * Put a new tag into map
	 * @param tagNew
	 * @return
	 */
	public boolean putTag(tag tagNew){
		
		// Check parameter is valid
		if (tagNew.getTagName().isEmpty()){
			System.out.println("new tag's name is empty");
			return false;
		}
		
		// Find new tag's name in mapTag, if found add new tag into list
		// else create new line in mapTag
		String strTagName = tagNew.getTagName();
		if (mapTag.containsKey(strTagName)){
			List<tag> lstTag = mapTag.get(strTagName);
			if (!lstTag.contains(tagNew)){
				lstTag.add(tagNew);
				mapTag.put(strTagName, lstTag);
			}
		}else{
			List<tag> lstTag = new ArrayList<tag>();
			lstTag.add(tagNew);
			mapTag.put(strTagName, lstTag);
			lstTag = null;
		}
		
		
		return true;
	}
	
		
	public Map<String, List<tag>> readConfig() throws DocumentException, ParserConfigurationException, SAXException, IOException{
	
		String strFilePath = "config/configFile01.xml";
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(strFilePath);
			
		if (input == null){
			System.out.println("Input file is null");
			return null;
		}
			
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		TagMap handler = new TagMap();
		parser.parse(input, handler);
		
		return handler.getMapTag();
	}
	
	
	@Override
	public void startDocument() throws SAXException {
		mapTag = new HashMap<String, List<tag>>();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(qName.contains("text")){

			tagNew = new tag();
			int nAttTotal = attributes.getLength();
			System.out.println("Elturi: " + uri + ", localName: " + localName + ", qName: " + qName);
			attribute attb = new attribute();
			
			for (int i = 0; i< nAttTotal; i++){
				System.out.println("Abt localName: " + attributes.getLocalName(i) + ", value: " + attributes.getValue(i));
				attb.setName(attributes.getLocalName(i));
				attb.setValue(attributes.getValue(i));
			}
			
			tagNew.setTagName(qName);	
			if (!tagNew.putAttribute(attb)){
				System.out.println("attribute can't put into list. tagName: " + qName);
			}
		
		}else if (qName.equals("FodtPath")){
			tagNew = new tag();
			int nAttTotal = attributes.getLength();
			System.out.println("Elturi: " + uri + ", localName: " + localName + ", qName: " + qName);
			attribute attb = new attribute();
			
			for (int i = 0; i< nAttTotal; i++){
				System.out.println("Abt localName: " + attributes.getLocalName(i) + ", value: " + attributes.getValue(i));
				attb.setName(attributes.getLocalName(i));
				attb.setValue(attributes.getValue(i));
			}
			
			tagNew.setTagName(qName);	
			if (!tagNew.putAttribute(attb)){
				System.out.println("attribute can't put into list. tagName: " + qName);
			}
		}else if (qName.equals("DocBook")){
			
		}
		preTag = qName;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.contains("text") || qName.equals("FodtPath")){
			putTag(tagNew);
			tagNew = null;
		}else if (qName.equals("FodtPath")){
			putTag(tagNew);
			tagNew = null;
		}
		preTag = null;
	}
	
	
	private boolean isDocBookTag(String strTag){
		return (strTag.contains("<") && strTag.contains(">"));
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (preTag != null && preTag.contains("text")){
			String strDocBookTag = new String(ch,start,length); 
			if (isDocBookTag(strDocBookTag)){
				System.out.println("Content: " + strDocBookTag);
				tagNew.setObjTag(strDocBookTag);
			}

		}
	}	
	
}
