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

public class QAReader extends DefaultHandler{
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
	private final int CHAPITER = 1;
	private final int ARTICLE = 2;
	private String strChapFileName;
	private String strBlocFileName;
	
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
	
	private int iPhoto = 0;
	private boolean isDedication = false;
	private boolean isNeedCloseAnwser = false;
	private int iDedication = 0;
	private int nPreState = 0;
	private String strPicPath;
	
	private enum TAGTYPE{
		Heading1, Heading2, Heading3, Complimentary, Marginalia, Picture, TextBody, Init
	};
	
	private TAGTYPE tagType = TAGTYPE.Init; 
	
	public QAReader(){
		
	}
	
	public QAReader(Map<String, List<tag>> mapConfig){
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
	
	public void setImgPath(String strPath){
		strPicPath = strPath;
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
	
	private void setChapPath(String strPath){
		this.strChapFileName = strPath;
	}
	
	private void setArticlePath(String strPath){
		this.strBlocFileName = strPath;
	}
	

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
				System.out.println("Err: Please define document's type in configure file");
				return false;
			}			
			
			String strFolderName = getFolderName(strFodtPath);
			strPicPath = strDocBookPath + strFolderName + "/img/";
			strChapFileName = strDocBookPath + strFolderName + "/";
			strBlocFileName = strDocBookPath + strFolderName + "/";
			
		}else{
//			logger.error("MapConfig is null");
			System.out.println("Err: MapConfig is null");
			return false;
		}
		
		File inputFile = new File(strFodtPath);
		if (!inputFile.isFile()){
//			logger.error("File is not existed: " + strFodtPath);
			System.out.println("File is not existed: " + strFodtPath);
			return false;
		}
		
		InputStream input =  new FileInputStream(inputFile); // this.getClass().getResourceAsStream(strFodtPath);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		
		QAReader handlerQAReader = new QAReader();
		handlerQAReader.setDocBookPath(strDocBookPath);
		handlerQAReader.setFodtPath(strFodtPath);
		handlerQAReader.setDocType(strDocType);
		handlerQAReader.setImgPath(strPicPath);
		handlerQAReader.setChapPath(strChapFileName);
		handlerQAReader.setArticlePath(strBlocFileName);
		
		parser.parse(inputFile, handlerQAReader);
			
		input.close();
		return true;
	}
	
	@Override
	public void startDocument() throws SAXException {	
		docBookWriter =  new DocBookWriter();
		docBookWriter.createFile(strDocBookPath);	
	}
	
	
	@Override
	public void endDocument() {
		
		saveToFile(strChapFileName,  "  </abstract>\r\n </chapiter>\r\n");
	}
	
	/**
	 * Extract the text in tag "<text:p>" or "<text:h>" 
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param attributes
	 */
	private void startElementBlogType(String uri, String localName, String qName, Attributes attributes)
	{
		if (isInclude("office:text")){
			if (qName.equals("text:p") || qName.equals("text:h")){
				bIsText = true;
			}else {
				bIsText = false;
			}
		}		
	}
	
	/**
	 * 
	 * @param atr
	 * @param strName
	 * @param strValue
	 * @return
	 */
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
		
		if (isInclude("office:text")){

			if (qName.equals("office:binary-data")){
				tagType = TAGTYPE.Picture;
			}else if (qName.equals("text:p")){
				
				if (isAttribute(attributes, "text:style-name", "Text_20_body")){
					tagType = TAGTYPE.TextBody;
				}else if (isAttribute(attributes, "text:style-name", "Salutation")){
					tagType = TAGTYPE.Complimentary;
				}else{
					tagType = TAGTYPE.Init;
				}
			}else if (qName.equals("text:h")){				   
				   
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
			System.out.println("can't identify document's type");
		}

		preTag = qName;
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

			}else if (strDocType.equals("QA") && bContentStart){
				
			}else if ((bIsText) && !strContent.isEmpty()){

			}else{
				bIsText = false;
			}

		}
	}
	
	private void endElementBlogType(String uri, String localName, String qName){
		if (bIsText){
			if (qName.equals("text:p")){
				bIsText = false;
			}else if (qName.equals("text:h")){
				bIsText = false;
			}else if (qName.equals("text:span") && bSpanPara){
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
	
	private int getQAState(String strCont){
		if (strCont.contains("学诚法师 发表于")){
			return 3;
		}
	
		if (strCont.contains("发表于")){
			return 1;
		}else if (strCont.contains("回复于")){
			return 3;
		}else if ((1 == nQAState || 2 == nQAState) && 
				!strCont.isEmpty() &&
				!strCont.contains("回复于")){
			return 2;
		}else if (isNeedCloseAnwser || (nQAState == 3 && !strCont.isEmpty())){
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
				String strTemp = strCont.substring(0, nPos);
				String strRet = "\r\n   <person>" + strTemp.trim() + "</person>\r\n";
				
				int nTimePos = strCont.indexOf(".20");
				if (-1 != nTimePos){
					strTemp = strCont.substring(nTimePos + 1);
				}else{
					strTemp = strCont.substring(nPos+3);
				}
				
				strRet += "   <time>" + strTemp.trim() + "</time>\r\n";
				return strRet;
			}
		}else if (strCont.contains("回复于")){
			int nPos = strCont.indexOf("回复于");
			if (-1 != nPos){
				String strTemp = strCont.substring(0, nPos);
				String strRet = "   <person>" + strTemp.trim() + "</person>\r\n";
				int nTimePos = strCont.indexOf(".20");
				if (-1 != nTimePos){
					strTemp = strCont.substring(nTimePos + 1);
				}else{
					strTemp = strCont.substring(nPos+3);
				}
				strRet += "   <time>" + strTemp.trim() + "</time>\r\n";
				return strRet;
			}
		}
		return strCont;
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
	
	private void closeDedication(){
		if (strChapFileName.contains(".xml")){
			saveToFile(strChapFileName, "</dedication>");
			clearFileName(CHAPITER);
		}
	}
	
	private void closeChapiterFile(){
		if (strChapFileName.contains(".xml")){
			saveToFile(strChapFileName, " </abstract>\r\n</chapiter>");
			clearFileName(CHAPITER);
		}
	}
	
	private void closeArticleFile(){
		if (strBlocFileName.contains(".xml")){
			
			if (isNeedCloseAnwser){
				saveToFile(strBlocFileName,  "  </answer>\r\n </qandaentry>\r\n");
				isNeedCloseAnwser = false;
			}
		
			saveToFile(strBlocFileName, "</qandaset>");
			clearFileName(ARTICLE);
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
		
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty()){
			DecimalFormat df = new DecimalFormat("00");
			strChapFileName += df.format(++nIndexChapter);
			strChapFileName += ("000-" + strParaContent + ".xml");
			
			String strCont = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
			strCont += "<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook V4.2//EN\">\r\n";
			strCont += "<chapiter>\r\n";
			strCont += " <title>" + strParaContent + "</title>\r\n <abstract>";
			
			saveToFile(strChapFileName, strCont);
			strParaContent = "";
		}
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
	
	/**
	 * Dispose type: Heading2.
	 */
	private void saveHeading2(){
		strParaContent = strParaContent.trim();
		if (strParaContent.contains("封面")){
			String strCont = " <para>" + strParaContent + "</para>\r\n";
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
		strCont += "<qandaset>\r\n";
		strCont += " <title>" + strParaContent + "</title>\r\n";
	
		saveToFile(strBlocFileName, strCont);	
		strParaContent = "";
	}
	
	
	private void saveComplimentary(){
		strParaContent = strParaContent.trim();
		
		if (!strParaContent.isEmpty()){
			String strCont = "<para>\r\n" + strParaContent + "\r\n</para>\r\n";
			saveToFile(strChapFileName, strCont);
			strParaContent = "";
		}

	}
	
	private void saveMarginalia(){
		
	}
	
	private InputStream decoderBase64(String strContent) throws IOException{
		if (strContent == null){
			return null;
		}
		
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] btOutput = decoder.decodeBuffer(strContent);
		return (new ByteArrayInputStream(btOutput));
	
	}
	
	
	 
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
		
	private void savePicture() throws IOException{

		strParaContent = strParaContent.trim();
		if (strParaContent.isEmpty()){
//			logger.error("Picture is empty");
			System.out.println("Picture is empty");
			return ;
		}
			
		String strImgPath = strPicPath + String.valueOf(iPhoto) + ".jpg"; 	
		strContent = strParaContent.replace(" ", "");
		InputStream input = decoderBase64(strContent); 
		BufferedImage bufImage = ImageIO.read(input);
		createImage(strImgPath, bufImage);		
		
		// 在DocBook中加入画片标签
		String strCont = " <img>" + String.valueOf(iPhoto++) + ".jpg</img>\r\n";
		if (strChapFileName.contains(".xml")){
			saveToFile(strChapFileName, strCont);
		}else if (strBlocFileName.contains(".xml")){
			saveToFile(strBlocFileName, strCont);
		}
	
		strParaContent = "";
		tagType = TAGTYPE.TextBody;
	}
	
	
	private void saveQAContent(int nQAState){
		String strCont;
		switch (nQAState){
		case 1: 			
			if (isNeedCloseAnwser){
				strCont = "  </answer>\r\n </qandaentry>\r\n";
				strCont += " <qandaentry>\r\n  <question>";
				isNeedCloseAnwser = false;
			}else if (nPreState == 2){
				strCont = "  </question>\r\n <question>"; 
			}else{
				strCont = " <qandaentry>\r\n  <question>";
			}
					
			strCont += parseQuestion(strParaContent);
			saveToFile(strBlocFileName, strCont);
			strParaContent= "";
			break;
		case 2: 			
			strCont = "   <para>" + strParaContent + "</para>\r\n";
			saveToFile(strBlocFileName, strCont);
			strParaContent = "";
			break;
		case 3: 
			strCont = "  </question>\r\n  <answer>\r\n" + parseQuestion(strParaContent);
			saveToFile(strBlocFileName, strCont);
			strParaContent = "";
			break;
		case 4: 

			strCont = "   <para>" + strParaContent + "</para>\r\n"; 
			saveToFile(strBlocFileName, strCont);
			isNeedCloseAnwser = true;
			strParaContent = "";
			break;
		default: break;
		}
	}
	
	
	private void saveTextBody(){
		
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty()){
			nQAState = getQAState(strParaContent);
			
			if (0 < nQAState && 5 > nQAState){
				saveQAContent(nQAState);
			}else{
				String strCont = " <para>" + strParaContent + "</para>\r\n";
				if (strChapFileName.contains(".xml")){
					saveToFile(strChapFileName, strCont);
				}else if (strBlocFileName.contains(".xml")){
					saveToFile(strBlocFileName, strCont);
				}
			}
			nPreState = nQAState;
		}
		strParaContent = "";
	}
	
	private void shieldPicture(){
		strParaContent = "";
		if (strBlocFileName.contains(".xml")){
			saveToFile(strBlocFileName, "<img></img>\r\n");
		}else if (strChapFileName.contains(".xml")){
			saveToFile(strChapFileName, "<img></img>\r\n");
		}
	}
	
	private void endElementQAType(String uri, String localName, String qName) throws IOException{
		
		System.out.println("endElementQAType: " + qName);
		
		if (strParaContent.contains("图书在版编目")){
			int i = 0;
			i = 9 + i;
		}

		switch (tagType){
			case Heading1: 
				if (qName.equals("text:h")){
					if (strParaContent.contains("序") || strParaContent.contains("封面")){
						saveDedication();
					}else if (strParaContent.contains("图书在版编目")){
						closeArticleFile();
						saveChapiterTitle();
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
			case Complimentary:
				saveComplimentary();
				break;
			case Marginalia:
				saveMarginalia();
				break;
			case Picture:
				savePicture();
//				shieldPicture();
				tagType = TAGTYPE.TextBody;
				break;
			case TextBody:
				if (qName.equals("text:p")){
					saveTextBody();
				}
				break;
			case Init:
//				if (strParaContent.contains("图书在版编目")){
//					saveToFile(strBlocFileName,  "  </answer>\r\n </qandaentry>\r\n");
//					closeArticleFile();
//				}
				strParaContent = "";
				
				break;
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
			try {
				endElementQAType(uri, localName, qName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
//			logger.error("can't identify document's type");
			System.out.println("can't identify document's type");
		}
	}		
}
