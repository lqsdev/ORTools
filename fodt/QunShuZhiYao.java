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

import org.dom4j.DocumentException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class QunShuZhiYao extends Book{

	private boolean isExplanation = false;
	private boolean isAuthor = false;
	private List<String> lstAnnotation= new ArrayList<String>();
	private String strContentTmp = new String();
	private int iAnnotation = 0;

	private boolean isYiWen = false;
	

	
	public QunShuZhiYao(){}
	
	public QunShuZhiYao(Map<String, List<tag>> map){
		mapConfigTag = map;
	}
	
	protected boolean readFodtFile() throws DocumentException, ParserConfigurationException, SAXException, IOException{
		
		QunShuZhiYao hReader = new QunShuZhiYao();
		 
		if (mapConfigTag != null){
			String strTemp;
			
			// Get OpenOffice file's path
			strTemp = getTagAttribute("FodtFile", "path");
			if (strTemp.isEmpty()){
				System.out.println("Err: Fodt file's path is emtpy");
				return false;
			}
			
			// Read OpenOffice file
			File inputFile = new File(strTemp);
			if (!inputFile.isFile()){
	//			logger.error("File is not existed: " + strFodtPath);
				System.out.println("Err: File is not existed: " + strFodtPath);
				return false;
			}
			
			// Get docbook's path
			String strFolderName = getFolderName(strTemp);
			strTemp = getTagAttribute("DocBookPath", "path"); 
			if (strTemp.isEmpty()){
	//			logger.error("DocBook's Path is empt");
				System.out.println("Err: DocBook's Path is empt");
				return false;
			}
			
			strTemp += strFolderName  + "/";
			hReader.setBlocFileName(strTemp);
			hReader.setChapFileName(strTemp);
			hReader.setChapImgName(strTemp + "img/");
			
			InputStream input = new FileInputStream(inputFile);
	
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			
			parser.parse(input, hReader);
			input.close();
			return true;
		}
		 
		System.out.println("warning: MapConfig is null");
		return false;
	}
	
	
	
	
	
	/**
	 * 给有注释的地方标识
	 */
	private void addTagAnnotation(){
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty()){
			iAnnotation++;
			strContentTmp += strParaContent+ "(#" + iAnnotation + ")";
		}
		
		strParaContent = "";
	}
	
	
	private void checkTag(){
		if (strChapFileName.contains(".xml")){
			if (isAuthor) {
				saveToFile(strChapFileName, "</author>");
				isAuthor = false;
			}
			
			if (isYiWen){
				saveToFile(strChapFileName, " </YiWen>\r\n</chapiter>");
				isYiWen = false;
			}
		}
	}
	
	
	/**
	 * Dispose type: Heading1. include cover of book or chapiter's title.
	 */
	protected void saveChapiterTitle(){
		
		if (isDedication){
			closeDedication();
		}else{
			checkTag();
			closeChapiterFile();
		}
		clearFileName(CHAPITER);
		
		closeArticleFile();
		
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty()){
			DecimalFormat df = new DecimalFormat("00");
			strChapFileName += df.format(++nIndexChapter);
			strChapFileName += ("000-" + strParaContent + ".xml");
			
			String strCont = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
//			strCont += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
			strCont += "<chapiter>\r\n";
			strCont += " <title>" + strParaContent + "</title>\r\n";
			saveToFile(strChapFileName, strCont);
			strParaContent = "";
		}
	}
	
	
	protected void saveTextBody(){
		
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty()){
		
			String strCont = "";

			if (bIllustration){
				strCont = " <illustration>" + strParaContent + "</illustration>\r\n";
				bIllustration = false;
			}else if (strParaContent.contains("題解")){
				strCont = "<explanation><tijie>"+strParaContent+"</tijie>\r\n";
				isExplanation = true;
				
			}else if (strParaContent.contains("作者介绍")){
				if (isExplanation){
					strCont = "</explanation>\r\n";
					isExplanation = false;
				}
				
				strCont += "<author><ZuoZheJieShao>" + strParaContent + "</ZuoZheJieShao>\r\n";
				isAuthor = true;
			}else if (strParaContent.contains("卦旨")) {
				strCont = "<GuaZhi>" + strParaContent + "</GuaZhi>\r\n";
			}else if (strParaContent.contains("原文")){
				if (isYiWen){
					saveToFile(strChapFileName, "</YiWen>\r\n");
					isYiWen = false;
				}
				strCont = "<YuanWen>" + strParaContent + "</YuanWen>";				
			}else if (strParaContent.contains("譯文")){
				if (!lstAnnotation.isEmpty()){
					strCont = "<ZhuShi>";
					for (int i = 0; i < lstAnnotation.size(); i++){
						strCont += "<para>(#" + (i+1) + ")" + lstAnnotation.get(i) + "</para>\r\n";
					}
					strCont += "</ZhuShi>\r\n"; 
					lstAnnotation.clear();
					iAnnotation = 0;
				}
				strCont += "<YiWen>\r\n";
				isYiWen = true;
			}else{
				strCont = " <para>" + strParaContent + "</para>\r\n";
			}
			
			if (strChapFileName.contains(".xml")){
				saveToFile(strChapFileName, strCont);
			}else if (strBlocFileName.contains(".xml")){
				saveToFile(strBlocFileName, strCont);
			}
		}
		strParaContent = "";
	}

	
	
	@Override
	public void startDocument() throws SAXException {	
		docBookWriter = new DocBookWriter();
	}

	@Override
	public void endDocument(){
		if (strChapFileName.contains(".xml")){
			if (isYiWen){
				saveToFile(strChapFileName, "</YiWen>");
				isYiWen = false;
			}
			saveToFile(strChapFileName, "\r\n</chapiter>");
			clearFileName(CHAPITER);
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (!qName.isEmpty()){
			lstTagPath.add(qName);
		}
		
		if (isInclude("office:text")){

			if (qName.equals("office:binary-data")){
				// Extract picture
				tagType = TAGTYPE.Picture;
			}else if (qName.equals("dc:creator") || qName.equals("dc:date")){
				isIgnore = true;
			}else if (qName.equals("text:p")){
				// Extract text
				if (isAttribute(attributes, "text:style-name", "P3") ||
						isAttribute(attributes, "text:style-name", "P4") ||
						isAttribute(attributes, "text:style-name", "Text_20_body")){
					tagType = TAGTYPE.TextBody;
				}

			}else if (qName.equals("text:h")){				   
				// Extract title
				if (isAttribute(attributes, "text:style-name", "P16") && 
						isAttribute(attributes, "text:outline-level", "1")){	
					tagType = TAGTYPE.Heading1;
				}if (isAttribute(attributes, "text:style-name", "Heading_20_1") && 
						isAttribute(attributes, "text:outline-level", "1")){
					tagType = TAGTYPE.Heading1;
				}if (isAttribute(attributes, "text:style-name", "P5") && 
						isAttribute(attributes, "text:outline-level", "1")){
					tagType = TAGTYPE.Heading1;					
				}
			}else if (qName.equals("office:annotation")) {
				addTagAnnotation();
				
			}else if (qName.equals("text:list-item")){
			
				tagType = TAGTYPE.Annotation;
				
			}	
		}
		preTag = qName;
	}
	
		
	/**
	 * 
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		switch (tagType){
			case Heading1: 
				if (qName.equals("text:h")){
					if (strParaContent.contains("序") || 
							strParaContent.contains("封面") ||
							strParaContent.contains("导言") ||
							strParaContent.contains("前言") ){
						saveDedication();
					}else{
						saveChapiterTitle();
					}
				}
				
				if (nBlocText != 0){
					nBlocText = 0;
				}
				break;
			case Heading2:
				if (qName.equals("text:h")){
					saveHeading2();
				}
				break;
			case Heading3:
				if (qName.equals("text:h")){
					saveArticleTitle();
				}
				break;
			case SubTitle:
				if (qName.equals("text:p")){
					saveSubTitle();
				}
				break;
			case Complimentary:
				if (qName.equals("text:p")){
					saveComplimentary();					
				}
				break;
			case Marginalia:
				saveMarginalia();
				break;
			case Picture:
				if (bComplimentary){
					closeComplimentary();
				}
				try {
					savePicture(strParaContent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				strParaContent = "";
				tagType = TAGTYPE.TextBody;
				break;
			case TextBody:
				if (qName.equals("text:p")){
					if (bComplimentary){
						closeComplimentary();
					}
					
					if (!strContentTmp.isEmpty()){
						String strTmp = strParaContent;
						strParaContent = strContentTmp;
						saveTextBody();
						strContentTmp = "";
						strParaContent = strTmp;
					}
					
					saveTextBody();
				}
				break;
			case Annotation:
				if (qName.equals("text:p")) {
					strParaContent = strParaContent.trim();
					lstAnnotation.add(strParaContent);
					strParaContent = "";
					tagType = TAGTYPE.Init;
				}
				break;
			case Init:
				if (!strContentTmp.isEmpty()){
					// 如果原文没有结束，就追加新的段落。
					strContentTmp += strParaContent.trim();
				}
				
				strParaContent = "";
				
				break;	
		}
	}	
	

}
