package fodt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//import org.apache.log4j.Logger;

import org.dom4j.DocumentException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class FodtReader extends DefaultHandler{
	
	private String strFodtPath;
	private Map<String, List<tag>> mapConfigTag;
	private DocBookWriter docBookWriter;// =  new DocBookWriter();
	private String preTag = null;
//	private tag tagFodt;	// A tag in file fodt
	private String strContent = "";	// Read a part of paragraph
	private String strParaContent = "";	// A paragraph
	private boolean bIsText = false;
	private boolean bSpanPara = false;
	private String strDocType;
	private List<String> lstTagPath = new ArrayList<String>();
	private String strDocBookPath; 
	
//	private static Logger logger = Logger.getLogger(FodtReader.class);
	
	private int nIndexChapter = 0; // save the index of chapter for Q&A document
	private int nBlocText = 0;
	private String strChapFileName = "/Users/Lei/Documents/workspace/fodt/file/book/";
	private String strBlocFileName = "/Users/Lei/Documents/workspace/fodt/file/book/";
	
	private boolean bIsChapterTitle = false;  // identify if a tag '<text:h>' is a chapter title
	private boolean bIsBookTitle = false;
	private boolean bIsQATitle = false;
	private int nQAState = 0;	// 0: initial; 1:question first line; 2: question; 3: answer first line; 4: answer
	private boolean bContentStart = false; // Document QA, when program begin to parse the QA's content
	private boolean bIsCatalog = false;
	private boolean bIsTableContent = false;
	private boolean bIsEnd = false;
	private boolean bIsChapterEnd = false;
	private boolean bIsNeedSetEnd = false;
	//private int nPreQAState = 0;
	private int nSaveStep = 0;
	private int nNumberIntroduction = 2;
	private boolean bEndIntroduction = false;
	
	
	public FodtReader(){
		
	}
	
	public FodtReader(Map<String, List<tag>> mapConfig){
		mapConfigTag = mapConfig;
	}
	
	public void setDocType(String strDocType){
		this.strDocType = strDocType;
	}
	
	public String getFodtPath() {
		return strFodtPath;
	}

	public void setFodtPath(String strFodtPath) {
		this.strFodtPath = strFodtPath;
	}

	public String getDocBookPath() {
		return strDocBookPath;
	}

	public void setDocBookPath(String strDocBookPath) {
		this.strDocBookPath = strDocBookPath;
	}

//	private String getFilePathByName(String strFile){
//		List<tag> lstTag = mapConfigTag.get(strFile);
//		if (lstTag !=  null){
//			tag tagFodtPath = lstTag.get(0);
//			
//			if (strFile.equals("FodtPath")){
//				return tagFodtPath.getAttributeValueByName("path");
//			}else if (strFile.equals("DocBookPath")){
//				return tagFodtPath.getAttributeValueByName("path");
//			}else{
//				return "";
//			}
//		}else{
//			return "";
//		}
//	}
	
	
	/**
	 * 
	 * @param strTagName
	 * @param strAtr
	 * @return
	 */
	private String getTagAttribute(String strTagName, String strAtr){
		List<tag> lstTag = mapConfigTag.get(strTagName);
		if (lstTag !=  null){
			String strValue;
			for (tag t : lstTag){
				strValue = t.getAttributeValueByName(strAtr);
				if (!strValue.isEmpty()){
					return strValue;
				}
			}
//			logger.error("Can't find attribute: " + strAtr);
			System.out.println("Err: Can't find attribute: " + strAtr);
			return "";
			
		}else{
//			logger.error("Can't find tag: " + strTagName);
			System.out.println("Err: Can't find tag: " + strTagName);
			return "";
		}
	}
	
//	@SuppressWarnings("unused")
	public boolean readFodtFile() throws DocumentException, ParserConfigurationException, SAXException, IOException{		
		
		if (mapConfigTag != null){			
			
			strFodtPath = getTagAttribute("FodtFile", "path");
			if (strFodtPath.isEmpty()){
				System.out.println("Fodt file's path is emtpy");
				return false;
			}
				
			
			strDocBookPath = getTagAttribute("DocBookPath", "path"); 
			if (strDocBookPath.isEmpty()){
//				logger.error("DocBook's Path is empt");
				System.out.println("Err: DocBook's Path is empt");
				return false;
			}
			
			strDocType = getTagAttribute("DocType", "type");
			if (strDocType.isEmpty()){
//				logger.error("Please define document's type in configure file");
				System.out.println("Err: DocBook's Path is empt");
				return false;
			}			
		}else{
//			logger.error("MapConfig is null");
			System.out.println("Err: MapConfig is null");
			return false;
		}
		
		File inputFile = new File(strFodtPath);
		if (!inputFile.isFile()){
//			logger.error("File is not existed: " + strFodtPath);
			System.out.println("Err: File is not existed: " + strFodtPath);
			return false;
		}
		
		InputStream input =  new FileInputStream(inputFile); // this.getClass().getResourceAsStream(strFodtPath);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		FodtReader handlerFodtReader = new FodtReader();
		handlerFodtReader.setDocBookPath(strDocBookPath);
		handlerFodtReader.setFodtPath(strFodtPath);
		handlerFodtReader.setDocType(strDocType);
		parser.parse(input, handlerFodtReader);
		input.close();
		return true;
	}
	
	
	@Override
	public void startDocument() throws SAXException {	
		docBookWriter =  new DocBookWriter();
		docBookWriter.createFile(strDocBookPath);
		
		String strLine = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";		
		strLine += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook XML V4.1.2//EN\" ";
		strLine += "\"http://www.oasis-open.org/docbook/xml/4.1.2/docbookx.dtd\">\r\n";
		strLine += "<article>\r\n";
		saveToFile(strLine);	
	}
	
	@Override
	public void endDocument() {
		String strLine = "</article>";
		saveToFile(strLine);
	}
	
	private void startElementBlogType(String uri, String localName, String qName, Attributes attributes)
	{
		if (isInclude("office:text") && !isInclude("text:index-body") && !isInclude("text:table-of-content") ){
			if (qName.equals("text:p")){
				saveToFile("<para>");
				bIsText = true;
			}else if (qName.equals("text:h")){
				saveToFile("<title>");
				bIsText = true;
			}else if (qName.equals("text:span")){
				if (attributes.getLocalName(0).equals("text:style-name") && attributes.getValue(0).equals("T5")){
					bSpanPara = true;
				}
				bIsText = true;
			}else if (!isInclude("text:h") && !isInclude("text:p")){
				bIsText = false;
			}
		}		
	}
	
	private boolean isAttribute(Attributes atr, String strName, String strValue){
		for (int i = 0; i < atr.getLength(); i++){		
//			logger.debug("atr.getQName: " + atr.getQName(i));
//			logger.debug("atr.getLocalName: " + atr.getLocalName(i));
//			logger.debug("atr.getValue: " + atr.getValue(i));
			
			if (atr.getLocalName(i).equals(strName) && atr.getValue(i).equals(strValue)){
				return true;
			}
		}
		return false;
	}
	
	private void startElementQAType(String uri, String localName, String qName, Attributes attributes){
		
		if (isInclude("office:text") && !isInclude("text:index-body") && !isInclude("text:table-of-content") ){

			if (qName.equals("text:p")){
				//saveToFile("<para>");
				bIsText = true;
				if (1 == nSaveStep){
					nSaveStep = 2;
				}
			}else if (qName.equals("text:h")){
				if (isAttribute(attributes, "text:style-name", "P4") && isAttribute(attributes, "text:outline-level", "2")){
					// <text:h text:style-name="P4" text:outline-level="2">					
					nIndexChapter++;		
					bIsChapterTitle = true;
					nSaveStep = 1;
				}else if(isAttribute(attributes, "text:style-name", "P1") && isAttribute(attributes, "text:outline-level", "1")){
					// book's name
					//<text:h text:style-name="P1" text:outline-level="1">
					bIsBookTitle = true;
				}else if (isAttribute(attributes, "text:style-name", "Heading_20_3") && 
						isAttribute(attributes, "text:outline-level", "3")){
					// Q&A's title
					//<text:h text:style-name="Heading_20_3" text:outline-level="3">
					nBlocText++;
					bIsQATitle = true;
					bContentStart = true;
				}else if (isAttribute(attributes, "text:style-name", "P5") && 
						isAttribute(attributes, "text:outline-level", "2")){
					if (bIsCatalog){
						bIsCatalog = false;						
					}

				}if (isAttribute(attributes, "text:style-name","Heading_20_2") &&
						isAttribute(attributes, "text:outline-level", "2")){
					
					if (nNumberIntroduction > -2){
						nNumberIntroduction--;						
					}else if (bContentStart){
						saveToFile("</qandaset>\r\n");
						saveToFile(strBlocFileName, "</qandaset>\r\n");
						bIsEnd = true;
					}
				}if (nIndexChapter > 0 && qName.equals("text:list")){
					bIsChapterEnd = true;
				}
			}else if (qName.equals("text:span")){
				if (attributes.getLocalName(0).equals("text:style-name") && attributes.getValue(0).equals("T5")){
					bSpanPara = true;
				}
			}else if (!isInclude("text:h") && !isInclude("text:p")){
			}
		}		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (!qName.isEmpty()){
			lstTagPath.add(qName);
		}
		
		if (strDocType.equals("Blog") || strDocType.equals("Diary")){
			startElementBlogType(uri, localName, qName, attributes);
		}else if (strDocType.equals("QA")){
			startElementQAType(uri, localName, qName, attributes);
		}else{
//			logger.error("can't identify document's type");
			System.out.println("Err: can't identify document's type");
		}

		preTag = qName;
	}
		
	private void saveToFile(String strContent){
//		try {
//			docBookWriter.saveFile(strContent);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	private void saveToFile(String strFilePath, String strContent){
		try {
			docBookWriter.saveFile(strFilePath, strContent);
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
			
			if (!strContent.isEmpty()){
				strParaContent = strParaContent + strContent;
				strContent = "";
			}			
			
			if (strDocType.equals("QA") && bIsQATitle){
				//saveToFile(strContent);
				//strContent = "";
			}else if (strDocType.equals("QA") && bContentStart){
				
			}else if ((bIsText) && !strContent.isEmpty()){
				//saveToFile(strContent);
				//strContent = "";
			}else{
				bIsText = false;
			}

		}
	}
	
	private void endElementBlogType(String uri, String localName, String qName){
		if (bIsText){
			if (qName.equals("text:p")){
				saveToFile("</para>\r\n");
				bIsText = false;
			}else if (qName.equals("text:h")){
				saveToFile("</title>\r\n");
				bIsText = false;
			}else if (qName.equals("text:span") && bSpanPara){
				saveToFile("</para>\r\n<para>");
				bSpanPara = false;
			}
			
		}
				
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
	
	private String getChapterIndex(int nIndex){
		switch(nIndex){
			case 1: return "一";
			case 2: return "二";
			case 3: return "三";
			case 4: return "四";
			case 5: return "五";
			case 6: return "六";
			case 7: return "七";
			case 8: return "八";
			case 9: return "九";
			case 10: return "十";
			case 11: return "十一";
			case 12: return "十二";
			case 13: return "十三";
			case 14: return "十四";
			case 15: return "十五";
			case 16: return "十六";
			case 17: return "十七";
			case 18: return "十八";
			case 19: return "十九";
			case 20: return "二十";
			default: return "N";
		}
	}
	
	
	private int getQAState(String strCont){
		if (strCont.contains("发表于")){
			return 1;
		}else if (strCont.contains("回复于")){
			return 3;
		}else if ((1 == nQAState || 2 == nQAState) && 
				!strCont.isEmpty() &&
				!strCont.contains("回复于")){
			return 2;
		}else if (nQAState == 3 && !strCont.isEmpty()){
			return 4;
		}
		return 0;
	}
	
	private String deletePageNum(String strContent){
		int nIndex = strContent.indexOf('/');
		if (nIndex > -1){
			return strContent.substring(0, nIndex);
		}
		
		return strContent;
	}
	
	
	
	private String parseQuestion(String strCont){
		if (strCont.isEmpty()){
			return "";
		}
		
		if (strCont.contains("发表于")){
			int nPos = strCont.indexOf("发表于");
			if (-1 != nPos){
				String strTemp = strCont.substring(1, nPos);
				String strRet = "\r\n   <person>" + strTemp.trim() + "</person>\r\n";
				strTemp = strCont.substring(nPos+3);
				strRet += "   <time>" + strTemp.trim() + "</time>\r\n";
				return strRet;
			}
		}else if (strCont.contains("回复于")){
			int nPos = strCont.indexOf("回复于");
			if (-1 != nPos){
				String strTemp = strCont.substring(0, nPos);
				String strRet = "   <person>" + strTemp.trim() + "</person>\r\n";
				strTemp = strCont.substring(nPos+3);
				strRet += "   <time>" + strTemp.trim() + "</time>\r\n";
				return strRet;
			}
		}
		
		
		
		return strCont;
	}
	
	
	private void endElementQAType(String uri, String localName, String qName){
		if ((isInclude("text:h") || isInclude("text:p")) && 
				!isInclude("text:table-of-content") &&
				!bIsEnd){
			
			if (qName.equals("text:p")){
				
				if (0 <= nNumberIntroduction){
					String strCont = "  <para>" + strParaContent + "</para>\r\n";
					saveToFile(strChapFileName, strCont);						
					strParaContent = "";
				}
				
				if (2 == nSaveStep){
					strParaContent = strParaContent.trim();
					
					if (!strParaContent.isEmpty()){
						saveToFile("<abstract>");
						saveToFile(strParaContent);
						saveToFile("</abstract>");
						
						String strCont = "  <para>" + strParaContent + "</para>\r\n </abstract>\r\n</chapter>";
						saveToFile(strChapFileName, strCont);						
						
						strParaContent = "";
						nSaveStep = 0;
					}
				}
				
				
				if (bContentStart){					
					nQAState = getQAState(strParaContent);
					
					String strCont;
					switch (nQAState){
						case 0:
							if (!strParaContent.isEmpty()){
								saveToFile("<para1>");
								saveToFile(strParaContent);
								saveToFile("</para1>");
								strParaContent = "";
							}
							break;
						case 1: 
							saveToFile(" <qandaentry>\r\n  <question>\r\n   <label>");
							saveToFile(strParaContent);
							saveToFile("</label>\r\n");
							
							//strCont = " <qandaentry>\r\n  <question>\r\n   <label>";						
							strCont = " <qandaentry>\r\n  <question>";						
							strCont += parseQuestion(strParaContent);
							//strCont += "   </label>\r\n";
//							strCont += "\r\n";
							saveToFile(strBlocFileName, strCont);
							
							strParaContent= "";
							break;
						case 2: 
							saveToFile("   <para>");
							saveToFile(strParaContent);
							saveToFile("</para>\r\n");
							
							strCont = "   <para>" + strParaContent + "</para>\r\n";
							saveToFile(strBlocFileName, strCont);
							
							strParaContent = "";
							break;
						case 3: 
							saveToFile("  </question>\r\n  <answer>\r\n   <para>");
							saveToFile(strParaContent);
							
							//strCont = "  </question>\r\n  <answer>\r\n   <para>" + parseQuestion(strParaContent);
							strCont = "  </question>\r\n  <answer>\r\n" + parseQuestion(strParaContent) + "   <para>";
							saveToFile(strBlocFileName, strCont);
							
							strParaContent = "";
							break;
						case 4: 
							saveToFile(strParaContent);
							saveToFile("</para>\r\n  </answer>\r\n </qandaentry>\r\n");
							
							strCont = strParaContent + "</para>\r\n  </answer>\r\n </qandaentry>\r\n"; 
							saveToFile(strBlocFileName, strCont);
							strParaContent = "";
							break;
						default: break;
					}
					
				}else if (bIsText && !strParaContent.isEmpty()){
					if (bIsCatalog){
						strParaContent = deletePageNum(strParaContent);
					}
					strParaContent = strParaContent.trim();
					if (!strParaContent.isEmpty()){
						saveToFile("<para>" + strParaContent + "</para>\r\n");
						saveToFile(strBlocFileName, "<para>" + strParaContent + "</para>\r\n");
					}
					
					bIsText = false;
					strParaContent = "";
				}
			}else if (qName.equals("text:h")){

				if (-1 == nNumberIntroduction){
					String strCont = "</dedication>\r\n";
					saveToFile(strChapFileName, strCont);
				}else if (0 <= nNumberIntroduction && !bIsBookTitle){
					int nPos = strChapFileName.lastIndexOf("/");
					if (nPos != -1 && nPos < (strChapFileName.length() - 1)){
						saveToFile(strChapFileName, "</dedication>\r\n");
					}
					
					
					strParaContent = strParaContent.trim();
					
					String strCont = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
					strCont += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
					strCont += "<dedication>\r\n";
					strCont += " <title>" + strParaContent + "</title>\r\n";
					strCont += " <para>\r\n";
					
//					int nPos = strChapFileName.lastIndexOf("/");
					if (-1 != nPos){
						strChapFileName = strChapFileName.substring(0, nPos + 1);
					}
					
					DecimalFormat df = new DecimalFormat("00");
					strChapFileName += df.format(nIndexChapter);
					strChapFileName += ("000-" + strParaContent + ".xml");
					saveToFile(strChapFileName, strCont);
					
					strParaContent = "";
					
				}else if (bIsChapterTitle){
					String strTitle = "第" + getChapterIndex(nIndexChapter) + "章";

					strParaContent = strParaContent.trim();
					
					saveToFile("<chapter>");
					saveToFile(strTitle + " ");
					saveToFile(strParaContent);
					saveToFile("</chapter>\r\n");

					String strCont = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
					strCont += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
					strCont += "<chapter>\r\n";
					strCont += " <title>" + strTitle + " " + strParaContent + "</title>\r\n";
					strCont += " <abstract>\r\n";
					
					int nPos = strChapFileName.lastIndexOf("/");
					if (-1 != nPos){
						strChapFileName = strChapFileName.substring(0, nPos + 1);
					}
					
					DecimalFormat df = new DecimalFormat("00");
					strChapFileName += df.format(nIndexChapter);
					strChapFileName += ("000-" + strTitle + "-" + strParaContent + ".xml");
					saveToFile(strChapFileName, strCont);
					
					
					strParaContent = "";
					bIsChapterTitle = false;
					nBlocText = 0;
										
				}else if (bIsBookTitle){
					saveToFile("<bookinfo><title>" + strParaContent.trim() + "</title></bookinfo>\r\n");
					strParaContent = "";
					bIsBookTitle = false;
				}else if (bIsQATitle){
					if (bIsNeedSetEnd){
						saveToFile("</qandaset>\r\n");
						bIsNeedSetEnd = false;
						
						saveToFile(strBlocFileName, "</qandaset>\r\n");
						
						
					}
					strParaContent = strParaContent.trim(); 
					saveToFile("<qandaset>\r\n <title>");
					saveToFile(strParaContent);
					saveToFile("</title>\r\n");
					
					
					int nPos = strBlocFileName.lastIndexOf("/");
					if (-1 != nPos){
						strBlocFileName = strBlocFileName.substring(0, nPos + 1);
					}
					
					DecimalFormat df = new DecimalFormat("00");
					strBlocFileName += df.format(nIndexChapter);
					
					DecimalFormat dfArticle = new DecimalFormat("000");
					strBlocFileName += dfArticle.format(nBlocText);
					strBlocFileName += ("-" + strParaContent + ".xml");
					
					String strCont = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
					strCont += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
					strCont += "<qandaset>\r\n";
					strCont += " <title>" + strParaContent + "</title>\r\n";
//					strCont += " <qandaset>\r\n";
					
					saveToFile(strBlocFileName, strCont);	
					
					bIsQATitle = false;
					bIsNeedSetEnd = true;
					strParaContent = "";
				}else{
					if (strParaContent.contains("原书目录") && !bIsCatalog){
						bIsCatalog = true;
					}
					
					if (bIsCatalog){
						saveToFile(strParaContent.trim());
						saveToFile("</title>\r\n");
						strParaContent = "";						
					}
					strParaContent = "";

				}
				bIsText = false;
			}else if (qName.equals("text:table-of-content")){
				bIsTableContent = false;
			}else if (bIsChapterEnd){
				saveToFile("</qandaset>\r\n");
				bIsChapterEnd = false;
				
				saveToFile(strChapFileName, "</qandaset>\r\n");
			}
			
		}else{
			strParaContent = "";
		}
		
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
	
		
	/**
	 * Read tag close
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (strDocType.equals("Blog") || strDocType.equals("Diary")){
			endElementBlogType(uri, localName, qName);
		}else if (strDocType.equals("QA")){
			endElementQAType(uri, localName, qName);
		}else{
//			logger.error("can't identify document's type");
			System.out.println("Err: can't identify document's type");
		}
	}	
	
}
