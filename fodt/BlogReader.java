package fodt;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sun.misc.BASE64Decoder;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


public class BlogReader extends DefaultHandler{
	private String strFodtPath;
	private Map<String, List<tag>> mapConfigTag;
	private DocBookWriter docBookWriter;//
	
	private String strParaContent = "";	// A paragraph
	private String preTag;
	private List<String> lstTagPath = new ArrayList<String>();
	
	private int nIndexChapter = 0; // index of chapter
	private int nBlocText = 0;		//	index of article
	private int nNumberIntroduction = 1;
	private int nArticleStep = 0;	// 0:init; 1:title; 2:source; 3:point; 4:text;
	
	private String strChapFileName;
	private String strBlocFileName;
	private String strPicPath;
	
	private boolean bSpanPara = false;
	private boolean bIsText = false;
	private boolean isChapter = false;
	private boolean bIntroduction = false;
	private boolean bEndFile = false;
	private int iPhoto = 0;
	private boolean bIllustration = false;
	private boolean isDedication = false;
	private int iDedication = 0;
	private boolean bComplimentary = false;
	
	private final int CHAPITER = 1;
	private final int ARTICLE = 2;
	
	private enum TAGTYPE{
		Heading1, Heading2, Heading3, Complimentary, Marginalia, Picture, Illustration, TextBody, SubTitle, Init
	};
	
	private TAGTYPE tagType = TAGTYPE.Init;
	
//	private static Logger logger = Logger.getLogger(BlogReader.class);
	
	public BlogReader (){	
	}
	
	public BlogReader(Map<String, List<tag>> mapConfig){
		mapConfigTag = mapConfig;
	}
	
	public void setFodtPath(String strFodtPath) {
		this.strFodtPath = strFodtPath;
	}
	
	public void setChapFileName(String strChapFileName) {
		this.strChapFileName = strChapFileName;
	}


	public void setBlocFileName(String strBlocFileName) {
		this.strBlocFileName = strBlocFileName;
	}
	
	public void setChapImgName(String strImgPath){
		this.strPicPath = strImgPath;
	}
	
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
	
	
	public boolean readFodtFile() throws DocumentException, ParserConfigurationException, SAXException, IOException{		
	
		BlogReader hBlogReader = new BlogReader();
		
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
//				logger.error("File is not existed: " + strFodtPath);
				System.out.println("Err: File is not existed: " + strFodtPath);
				return false;
			}
			
			// Get docbook's path
			String strFolderName = getFolderName(strTemp);
			strTemp = getTagAttribute("DocBookPath", "path"); 
			if (strTemp.isEmpty()){
//				logger.error("DocBook's Path is empt");
				System.out.println("Err: DocBook's Path is empt");
				return false;
			}
			strTemp += strFolderName + "/";
			hBlogReader.setBlocFileName(strTemp);
			hBlogReader.setChapFileName(strTemp);
			hBlogReader.setChapImgName(strTemp + "img/");

			InputStream input =  new FileInputStream(inputFile);
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			
			parser.parse(input, hBlogReader);
			input.close();
			return true;
			
		}else{
//			logger.error("MapConfig is null");
			System.out.println("warning: MapConfig is null");
			return false;
		}
	}

	private String getFolderName(String strFile){
		int nBegin = strFile.lastIndexOf("/");
		if (nBegin == strFile.length()){
//			logger.error("File's name is emtpy");
			System.out.println("Err: File's name is emtpy");
			return "";
		}
		
		int nEnd = strFile.lastIndexOf(".fodt");
		if (nBegin == strFile.length()){
//			logger.error("Can't find fodt file");
			System.out.println("Err: Can't find fodt file");
			return "";
		}
		return strFile.substring(nBegin + 1, nEnd);		
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
	
	
	private void saveToFile(String strFilePath, String strContent){
		try {
			docBookWriter.saveFile(strFilePath, strContent);
		} catch (IOException e) {
			e.printStackTrace();
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
	
	@Override
	public void startDocument() throws SAXException {	
		docBookWriter = new DocBookWriter();
	}
	
	
	@Override
	public void endDocument(){
		if (strChapFileName.contains(".xml")){
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
			}else if (qName.equals("text:p")){
				// Extract text
				if (isAttribute(attributes, "text:style-name", "Text_20_body")){
					tagType = TAGTYPE.TextBody;
				}else if (isAttribute(attributes, "text:style-name", "Salutation")){
					tagType = TAGTYPE.Complimentary;
				}else if (isAttribute(attributes, "text:style-name", "Subtitle")){
					tagType = TAGTYPE.SubTitle;
				}else if (isAttribute(attributes, "text:style-name", "Illustration")){
					bIllustration = true;
				}else if (isAttribute(attributes, "text:style-name", "Marginalia")) {
					tagType = TAGTYPE.Marginalia;
				}else{
					tagType = TAGTYPE.Init;
				}
			}else if (qName.equals("text:h")){				   
				// Extract title
				if (isAttribute(attributes, "text:style-name", "Heading_20_1") && 
						isAttribute(attributes, "text:outline-level", "1")){	
					tagType = TAGTYPE.Heading1;
				}else if (isAttribute(attributes, "text:style-name", "Heading_20_2") && 
						isAttribute(attributes, "text:outline-level", "2")){
					tagType = TAGTYPE.Heading2;
					
				}else if (isAttribute(attributes, "text:style-name", "Heading_20_3") && 
						isAttribute(attributes, "text:outline-level", "3")){
					tagType = TAGTYPE.Heading3;
				}
			}	
		}
		preTag = qName;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (preTag != null){
			
			String strContent = new String(ch, start, length);
			System.out.println("content: " + strContent);
			
			if (!strContent.isEmpty()){
				strParaContent = strParaContent + strContent;
			}			

		}
	}
		
	private void saveIntroduction(){
		int nPos = strChapFileName.lastIndexOf("/");
		if (-1 != nPos && nPos == (strChapFileName.length() - 1)){
			
			strParaContent = strParaContent.trim();
			
			String strCont = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
//			strCont += "<!DOCTYPE article  PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
			strCont += "<dedication>\r\n";
			strCont += " <title>" + strParaContent + "</title>\r\n";
			
			DecimalFormat df = new DecimalFormat("00");
			strChapFileName += df.format(nIndexChapter);
			//nIndexChapter
			strChapFileName += ("000-" + strParaContent + ".xml");
			saveToFile(strChapFileName, strCont);
		}else if (!bIntroduction){
			saveToFile(strChapFileName, "</dedication>\r\n");
			strChapFileName = strChapFileName.substring(0, nPos + 1);
			
		}else if (-1 != nPos && nPos < strChapFileName.length()){
			String strCont = " <para>\r\n";
			strCont += strParaContent.trim() + "\r\n </para>\r\n";
			saveToFile(strChapFileName, strCont);
		}
		strParaContent = "";
	}
	
	private void saveChapter(){
		int nPos = strChapFileName.lastIndexOf("/");
		if (-1 != nPos && nPos == (strChapFileName.length() - 1)){
			
			String strTitle = "第" + getChapterIndex(nIndexChapter) + "章";
			
			String strCont = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
			strCont += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
			strCont += "<chapter>\r\n";
			strCont += " <title>" + strTitle + " " + strParaContent + "</title>\r\n";
						
			DecimalFormat df = new DecimalFormat("00");
			strChapFileName += df.format(nIndexChapter);
			strChapFileName += ("000-" + strTitle + "-" + strParaContent + ".xml");
			saveToFile(strChapFileName, strCont);
		}else if (!isChapter){
			saveToFile(strChapFileName, "</chapter>");
			strChapFileName = strChapFileName.substring(0, nPos + 1);			
		}else if (isChapter && -1 != nPos && nPos < strChapFileName.length()){
//			String strCont = " <abstract>\r\n";
//			strCont += strParaContent + "\r\n </abstract>\r\n";
//			saveToFile(strChapFileName, strCont);
			strParaContent = "";
		}
	}
	
	
	/**
	 * 
	 * @param strContent
	 * @return
	 * @throws IOException
	 */
	private InputStream decoderBase64(String strContent) throws IOException{
		if (strContent == null){
			return null;
		}
		
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] btOutput = decoder.decodeBuffer(strContent);
		return (new ByteArrayInputStream(btOutput));
	
	}
	
	
	 /**
	  * 
	  * @param pathPic
	  * @param image
	  */
	private void createImage(String pathPic, BufferedImage image) {
		try {
			
			File fParente = new File(pathPic).getParentFile();
			if (!fParente.exists() && fParente != null){
				fParente.mkdirs();
			}
			
			
			FileOutputStream fos = new FileOutputStream(pathPic);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
		    encoder.encode(image);
		    bos.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param strContent
	 * @throws IOException
	 */
	private void savePicture(String strContent) throws IOException{
		iPhoto++;
		String strPath = strPicPath + String.valueOf(iPhoto) + ".jpg"; 	
		

		strContent = strContent.replace(" ", "");
		
		InputStream input = decoderBase64(strContent); 
		BufferedImage bufImage = ImageIO.read(input);
		
		createImage(strPath, bufImage);
		
		// 在DocBook中加入画片标签
		String strCont = " <img>" + String.valueOf(iPhoto) + ".jpg</img>\r\n";
		if (strBlocFileName.contains(".xml")){
			saveToFile(strBlocFileName, strCont);
		}else if (strChapFileName.contains(".xml")){
			saveToFile(strChapFileName, strCont);
		}
		
		strParaContent = "";
	}
	
	
	private void clearFileName(int nameType){
		if (nameType == CHAPITER){
			int nPosSlant = strChapFileName.lastIndexOf("/");
			if (-1 != nPosSlant){
				strChapFileName = strChapFileName.substring(0, nPosSlant + 1);
			}
		}else if (nameType == ARTICLE){
			int nPosSlant = strBlocFileName.lastIndexOf("/");
			if (-1 != nPosSlant){
				strBlocFileName = strBlocFileName.substring(0, nPosSlant + 1);
			}
		}
	}
	
	
	private void closeArticleFile(){
		if (strBlocFileName.contains(".xml")){
			saveToFile(strBlocFileName, "</article>");
			clearFileName(ARTICLE);
		}
	}
	
	
	private void saveTextBody(){
		
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty()){
		
			String strCont;
			if (bIllustration){
				strCont = " <illustration>" + strParaContent + "</illustration>\r\n";
				bIllustration = false;
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
	
	
	private void saveDedication(){
		closeDedication();
		
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty()){
			DecimalFormat df = new DecimalFormat("00");
			strChapFileName += df.format(nIndexChapter);
			DecimalFormat dfBloc = new DecimalFormat("000");
			strChapFileName += dfBloc.format(iDedication++);
			strChapFileName += ("-" + strParaContent + ".xml");
			
			String strCont = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
			strCont += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
			strCont += "<dedication>\r\n";
			strCont += " <title>" + strParaContent + "</title>\r\n";
			
			saveToFile(strChapFileName, strCont);
			strParaContent = "";
			isDedication = true;
		}
	}
	
	private void closeDedication(){
		if (strChapFileName.contains(".xml")){
			saveToFile(strChapFileName, "</dedication>");
			clearFileName(CHAPITER);
		}
	}
	
	/**
	 * Dispose type: Heading2.
	 */
	private void saveHeading2(){
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty() && strChapFileName.contains(".xml")){
			String strCont = " <para>" + strParaContent + "</para>\r\n";
			saveToFile(strChapFileName, strCont);
			strParaContent = "";
		}
	}
	
	private void saveComplimentary(){
		strParaContent = strParaContent.trim();
		
		if (!strParaContent.isEmpty() && strBlocFileName.contains(".xml")){
			if (!bComplimentary){
				String strCont = " <abstract>\r\n  <para>\r\n" + strParaContent + "\r\n</para>\r\n";
				saveToFile(strBlocFileName, strCont);
				strParaContent = "";
				bComplimentary = true;
			}else{
				String strCont = "  <para>\r\n" + strParaContent + "\r\n</para>\r\n";
				saveToFile(strBlocFileName, strCont);
				strParaContent = "";
			}
		}

	}
	
	
	private void closeComplimentary(){
		if (strBlocFileName.contains(".xml") && bComplimentary){
			bComplimentary = false;
			saveToFile(strBlocFileName, " </abstract>\r\n");
		}
	}
	
	
	
	private void closeChapiterFile(){
		if (strChapFileName.contains(".xml")){
//			saveToFile(strChapFileName, " </abstract>\r\n</chapiter>");
			saveToFile(strChapFileName, " \r\n</chapiter>");
			clearFileName(CHAPITER);
		}
	}
	
	/**
	 * Dispose type: Heading1. include cover of book or chapiter's title.
	 */
	private void saveChapiterTitle(){
		if (isDedication){
			closeDedication();
		}else{
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
			strCont += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
			strCont += "<chapiter>\r\n";
			strCont += " <title>" + strParaContent + "</title>\r\n";
			saveToFile(strChapFileName, strCont);
			strParaContent = "";
		}
	}
	
	
	
	/**
	 * Dispose type: Heading3. include an article's title.
	 */
	private void saveArticleTitle(){		
		
		closeChapiterFile();
		closeArticleFile();
		
		strParaContent = strParaContent.trim();
		if (strParaContent.isEmpty()){
			return ;
		}
		
		DecimalFormat df = new DecimalFormat("00");
		strBlocFileName += df.format(nIndexChapter);
		
		DecimalFormat dfArticle = new DecimalFormat("000");
		strBlocFileName += dfArticle.format(++nBlocText);
		strBlocFileName += ("-" + strParaContent + ".xml");
		
		String strCont = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
		strCont += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
		strCont += "<article>\r\n";
		strCont += " <title>" + strParaContent + "</title>\r\n";
	
		saveToFile(strBlocFileName, strCont);	
		strParaContent = "";
	}
	
	private void saveSubTitle(){
		strParaContent = strParaContent.trim();
		
		if (!strParaContent.isEmpty() && strChapFileName.contains(".xml")){
			String strCont = " <subtitle>" + strParaContent + "</subtitle>";
			saveToFile(strChapFileName, strCont);
		}
		
		strParaContent = "";
	}

	
	
	private void saveMarginalia(){
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty() && strBlocFileName.contains(".xml")){
			String strCont = " <from>" + strParaContent + "</from>\r\n";
			saveToFile(strBlocFileName, strCont);
		}
		strParaContent = "";
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
							strParaContent.contains("导言")){
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
					saveTextBody();
				}
				break;
			case Init:
				strParaContent = "";
				
				break;
		}
		
	}	
}
