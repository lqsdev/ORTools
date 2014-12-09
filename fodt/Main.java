package fodt;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;


//import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import sun.misc.*;



public class Main {
	
	
//	private static Logger logger = Logger.getLogger(Main.class);

	private static String getTagAttribute(List<tag> lstTag, String strAtr){
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
//			logger.error("List<tag> is empty");
			System.out.println("Err: List<tag> is empty");
			return "";
		}
	}
	
	public static void main(String[] args) throws DocumentException, ParserConfigurationException, SAXException, IOException {					
		
		TagMap mapConfig = new TagMap();
		Map<String, List<tag>> mapConf = mapConfig.readConfig();
		
		String strType = getTagAttribute(mapConf.get("DocType"), "type");
		if (strType.equals("Blog")){
				
			BlogReader blogReader = new BlogReader(mapConf);
			if (!blogReader.readFodtFile()){
//				logger.error("Reade blog file is failed");
				System.out.println("Err: Reade blog file is failed");
			}
		}else if (strType.equals("QA")){
			QAReader reader = new QAReader(mapConf);
			if (!reader.readFodtFile()){
//				logger.error("Reader Q&A file is failed!");
				System.out.println("Err: Reader Q&A file is failed!");
			}
		}else if (strType.equals("Diary")){
			DiaryReader diaryReader = new DiaryReader(mapConf);
			if (!diaryReader.readFodtFile()){
//				logger.error("Reader Q&A file is failed!");
				System.out.println("Err: Reader Q&A file is failed!");
			}
		}else if (strType.equals("QSZY")){
			QunShuZhiYaoReader reader = new QunShuZhiYaoReader(mapConf);
			if (!reader.readFodtFile()){
//				logger.error("Reader Q&A file is failed!");
				System.out.println("Err: Reader Q&A file is failed!");
			}			
		}else{
//			logger.error("Can't find document's type, please check config.xml");
			System.out.println("Err: Can't find document's type, please check config.xml");
		}	
		
	}

}
