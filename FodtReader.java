package fodt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.DocumentException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FodtReader extends DefaultHandler{
	
	private String strFodtPath;
	private Map<String, List<tag>> mapConfigTag;
	private DocBookWriter docBookWriter;
	private String preTag = null;
	private tag tagFodt;	// A tag in file fodt
	private String strContent = null;
	private boolean bIsText = false;
	private List<String> lstTagPath = new ArrayList<String>();
	
	

	public FodtReader(){
		
	}
	
	public FodtReader(Map<String, List<tag>> mapConfig){
		mapConfigTag = mapConfig;
	}
	
	
	public boolean readFodtFile() throws DocumentException, ParserConfigurationException, SAXException, IOException{
		if (mapConfigTag != null){
			List<tag> lstTag = mapConfigTag.get("FodtPath");
			if (lstTag !=  null){
				tag tagFodtPath = lstTag.get(0);
				strFodtPath = tagFodtPath.getAttributeValueByName("path");
				if (strFodtPath.isEmpty()){
					System.out.println("Error: fodt's path is emtpy");
					return false;
				}
			}
		}else{
			System.out.println("Error: MapConfig is null");
			return false;
		}
		
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(strFodtPath);
			
		if (input == null){
			System.out.println("Input fodt file is null, check the file's path please!");
			return false;
		}
			
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		FodtReader handlerFodtReader = new FodtReader();
		parser.parse(input, handlerFodtReader);
		
		return true;
	}
	
	
	@Override
	public void startDocument() throws SAXException {
		docBookWriter = new DocBookWriter();
	}
	
	private void printAttr(String uri, String localName, String qName, Attributes attributes){
		int nAttTotal = attributes.getLength();
		System.out.println("Fodt Elturi: " + uri + ", localName: " + localName + ", qName: " + qName);
		
		for (int i = 0; i< nAttTotal; i++){
			System.out.println("Fodt Abt localName: " + attributes.getLocalName(i) + ", value: " + attributes.getValue(i));
		}
	}
	
	private void addAttr(String uri, String localName, String qName, Attributes attributes){

//		System.out.println("Fodt Elturi: " + uri + ", localName: " + localName + ", qName: " + qName);
		if (!qName.isEmpty()){
			tagFodt.setTagName(qName);	
		}else{
			System.out.println("Error:  qName is empty!");
			return;
		}
		
		int nAttTotal = attributes.getLength();		
		for (int i = 0; i< nAttTotal; i++){
//			System.out.println("Fodt Abt localName: " + attributes.getLocalName(i) + ", value: " + attributes.getValue(i));
			attribute attb = new attribute();
			attb.setName(attributes.getLocalName(i));
			attb.setValue(attributes.getValue(i));			
		
			if (!tagFodt.putAttribute(attb)){
				System.out.println("Fodt attribute can't put into list. tagName: " + qName);
			}
			attb = null;
		}		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (!qName.isEmpty()){
			lstTagPath.add(qName);
		}
	
//		bIsText = true;
		
		if (isInclude("office:text") && !isInclude("text:index-body") && !isInclude("text:table-of-content") && 
				(qName.equals("text:p") || qName.equals("text:h") || qName.equals("text:span"))){
			bIsText = true;
		}
//		else if(qName.contains("text")){
		//	printAttr(uri, localName, qName, attributes);
//		}else{
		//	printAttr(uri, localName, qName, attributes);	
//		}
		
		
		preTag = qName;
	}
		
	private void saveToFile(String strContent){
		try {
			docBookWriter.saveFile(strContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Find out a tag in the list tag
	 * @param strTag
	 * @return
	 */
	private boolean isInclude(String strTag){
		for (String strLstTag : lstTagPath){
			if (strLstTag.equals(strTag)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (preTag != null){
			
			strContent = new String(ch, start, length);
			System.out.println("content: " + strContent);
			//strContent.trim();
			
			if ((bIsText) && !strContent.isEmpty()){
				saveToFile(strContent);
				
			}else{
				bIsText = false;
			}
			
			/*String strDocBookTag = new String(ch,start,length); 
			if (isDocBookTag(strDocBookTag)){
				System.out.println("Content: " + strDocBookTag);
				tagNew.setObjTag(strDocBookTag);
			}*/

		}
	}

	private int  isSaved(){
		if (mapConfigTag == null){
			System.out.println("Error: tag map is null");
			return -1;
		}

		List<tag> lstTag = mapConfigTag.get(tagFodt.getTagName());
		
		for (int index = 0; index < lstTag.size(); index++){
			if (lstTag.get(index).isSameTagName(tagFodt.getTagName()) && 
					lstTag.get(index).isSameAttr(lstTag.get(index).getListAttr())){
				return index; 
			}
		}
		
		return -1;
	}
	
	private void printLstTag()
	{
		System.out.println("");
		for (int i = 0; i < lstTagPath.size(); i++){
			System.out.print(lstTagPath.get(i) + " ");
		}
		System.out.println("");
	}
	/**
	 * Read tag close
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if (bIsText && (qName.contains("text:p") || qName.contains("text:h"))){
			bIsText = false;
			if (!strContent.startsWith("\n") && !strContent.endsWith("\n")){
				saveToFile("\r\n");
			}
			
			if (strContent.contains("做人天师表105")){
				strContent = null;
				
			}
		}
//		}else if (qName.contains("text")){
//			int nIndex = isSaved();
//			if (nIndex > -1){
//				String strDocBookTag = mapConfigTag.get(tagFodt.getTagName()).get(nIndex).getObjTag();
//				docBookWriter.addTagOpen(strDocBookTag);
//				docBookWriter.addContent(strContent);
//				docBookWriter.addTagClose(strDocBookTag);
//			}
//		}else{
//			System.out.println("tag " + qName + " is terminated");
//		}
				
		if (!qName.isEmpty()){
			lstTagPath.remove(qName);
		}		
		
		if (lstTagPath.isEmpty() || qName.equals("text:p") || qName.equals("text:h")){
			preTag = null;
		}
		else{
			preTag = lstTagPath.get(lstTagPath.size() - 1);
		}
	}	
	
}
