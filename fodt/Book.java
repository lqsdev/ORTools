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

import org.dom4j.DocumentException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sun.misc.BASE64Decoder;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;



public class Book extends DefaultHandler{
	protected String strFodtPath;
	protected Map<String, List<tag>> mapConfigTag;
	protected DocBookWriter docBookWriter;//
	
	protected String strParaContent = "";	// A paragraph
	protected String preTag;
	protected List<String> lstTagPath = new ArrayList<String>();
	
	protected int nIndexChapter = 0; // index of chapter
	protected int nBlocText = 0;		//	index of article
	protected int nNumberIntroduction = 2;
	protected int nArticleStep = 0;	// 0:init; 1:title; 2:source; 3:point; 4:text;
	
	protected String strChapFileName;
	protected String strBlocFileName;
	protected String strPicPath;
	
	protected boolean bSpanPara = false;
	protected boolean bIsText = false;
	protected boolean isIgnore = false;
	protected boolean isChapter = false;
	protected boolean bIntroduction = false;
	protected boolean bEndFile = false;
	protected boolean bIllustration =false;
	protected boolean isDedication = false;
	protected boolean bComplimentary = false;
	protected int iDedication = 0;
	protected int iPhoto = 0;
	
	protected  final int CHAPITER = 1;
	protected  final int ARTICLE = 2;	
	
	protected enum TAGTYPE{
		Heading1, Heading2, Heading3, 
		Complimentary, Marginalia, Picture, 
		Illustration, TextBody, SubTitle, 
		Init, Annotation
	};
	
	protected TAGTYPE tagType = TAGTYPE.Init;

	public Book (){
		
	}
	
	
	protected void setFodtPath(String strFodtPath){
		this.strFodtPath = strFodtPath;
	}
	
	protected void setChapFileName(String strChapFileName){
		this.strChapFileName =  strChapFileName;
	}
	
	protected void setBlocFileName(String strBlocFileName){
		this.strBlocFileName = strBlocFileName;
	}
	
	protected void setChapImgName(String strImgPath){
		this.strPicPath = strImgPath;
	}
	
	
	/**
	 * 
	 */
	protected String getTagAttribute(String strTagName, String strArt){
		List<tag> lstTag = mapConfigTag.get(strTagName);
		if (lstTag != null){
			String strValue;
			for (tag t : lstTag){
				strValue = t.getAttributeValueByName(strArt);
				if (!strValue.isEmpty()){
					return strValue;
				}
			}
			System.out.println("Err: Can't find attribute: " + strArt);
			return "";
		}else{
			System.out.println("Err: list tag is empty");
			return "";
		}
	}
	

	protected String getFolderName(String strFile){
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
	
	
	protected void saveToFile(String strFilePath, String strContent){
		try {
			docBookWriter.saveFile(strFilePath, strContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected String getChapterIndex(int nIndex){
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
	
	protected boolean isAttribute(Attributes atr, String strName, String strValue){
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
	
	/**
	 * Find out a tag in the list tag
	 * @param strTag
	 * @return
	 */
	protected boolean isInclude(String strTag){
		for (String strLstTag : lstTagPath){
			if (strLstTag.equals(strTag)){
				return true;
			}
		}
		return false;
	}

	protected void saveIntroduction(){
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
	
	protected void saveChapter(){
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
	protected InputStream decoderBase64(String strContent) throws IOException{
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
	protected void createImage(String pathPic, BufferedImage image) {
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
	protected void savePicture(String strContent) throws IOException{
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
	
	
	protected void clearFileName(int nameType){
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
	
	
	protected void closeArticleFile(){
		if (strBlocFileName.contains(".xml")){
			saveToFile(strBlocFileName, "</article>");
			clearFileName(ARTICLE);
		}
	}
	
	protected void saveDedication(){
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
	
	protected void closeDedication(){
		if (strChapFileName.contains(".xml")){
			saveToFile(strChapFileName, "</dedication>");
			clearFileName(CHAPITER);
			isDedication =false;
		}
	}
	
	/**
	 * Dispose type: Heading2.
	 */
	protected void saveHeading2(){
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty() && strChapFileName.contains(".xml")){
			String strCont = " <para>" + strParaContent + "</para>\r\n";
			saveToFile(strChapFileName, strCont);
			strParaContent = "";
		}
	}
	
	
	protected void saveComplimentary(){
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
	
	
	protected void closeComplimentary(){
		if (strBlocFileName.contains(".xml") && bComplimentary){
			bComplimentary = false;
			saveToFile(strBlocFileName, " </abstract>\r\n");
		}
	}
	

	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (preTag != null){
			
			String strContent = new String(ch, start, length);
			System.out.println("content: " + strContent);
			
			if (!strContent.isEmpty() && !isIgnore){
				strParaContent = strParaContent + strContent;
			}
			isIgnore = false;

		}
	}
	
	
	
	
	/**
	 * Dispose type: Heading1. include cover of book or chapiter's title.
	 */
	protected void saveChapiterTitle(){
		
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
	
	
	protected void closeChapiterFile(){
		if (strChapFileName.contains(".xml")){	
			saveToFile(strChapFileName, " \r\n</chapiter>");
			clearFileName(CHAPITER);
		}
	}
	
	/**
	 * Dispose type: Heading3. include an article's title.
	 */
	protected void saveArticleTitle(){		
		
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
	
	protected void saveSubTitle(){
		strParaContent = strParaContent.trim();
		
		if (!strParaContent.isEmpty() && strChapFileName.contains(".xml")){
			String strCont = " <subtitle>" + strParaContent + "</subtitle>";
			saveToFile(strChapFileName, strCont);
		}
		
		strParaContent = "";
	}

	
	
	protected void saveMarginalia(){
		strParaContent = strParaContent.trim();
		if (!strParaContent.isEmpty() && strBlocFileName.contains(".xml")){
			String strCont = " <from>" + strParaContent + "</from>\r\n";
			saveToFile(strBlocFileName, strCont);
		}
		strParaContent = "";
	}

}
