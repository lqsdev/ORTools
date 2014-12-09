package fodt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



/**
 * 
 * @author Lei
 *
 */
public class DocBookWriter {
	private String strBookName;
	private String strDocBook;
	private File fDocBook;

	
	public void setDocBookPath(String strPath){
//		docBookWriter.setDocBookPath(strPath);
		this.strDocBook = strPath;
	}
	
	public String getDocBookPath(){
//		return docBookWriter.getDocBookPath();
		return this.strDocBook;
	}
	
	public File getFileDocBook(){
		return fDocBook;
	}
	
	public String getBookName() {
		return strBookName;
	}

	public void setBookName(String strBookName) {
		this.strBookName = strBookName;
	}

	public void addTagOpen(String strTag){
		strDocBook.concat(strTag);
	}
	
	public void addContent(String strContent){
		strDocBook.concat(strContent);
	}
	
	public void addTagClose(String strTag){
		strTag.replace("<", "</");
		strDocBook.concat(strTag);
	}

	public void writeFile()
	{
		
		
	}	
	
	public void createFile(String strPath){
		if (strPath.isEmpty())
		{
			System.out.println("Error file's path is empty");
		}
		
		this.fDocBook = new File(strPath);
		
		if (fDocBook == null){
			System.out.println("Error fDocBook is null");
		}
	}
	
	public void saveFile(String strContent) throws IOException{
		FileWriter writer = new FileWriter(fDocBook, true);
		writer.write(strContent);
		writer.close();
	}
	
	public void saveFile(String strFile, String strContent) throws IOException{
		
		File fDoc = new File(strFile);
		File fParente = fDoc.getParentFile();
		if (!fParente.exists() && fParente != null){
			fParente.mkdirs();
		}
		
		FileWriter writer = new FileWriter(fDoc, true);
		writer.write(strContent);
		writer.close();
	}
	
	public void saveImage(String strFile, String strContent) throws IOException{
		File fImage = new File(strFile);
		
		if (fImage.exists())
		{	// If picture is there then don't save it 
			System.out.println("file " + strFile + " is existed!");
			return;	
		}
		
		File fParente = fImage.getParentFile();
		if (!fParente.exists() && fParente != null){
			fParente.mkdirs();
		}
		fImage.createNewFile();
		FileWriter writer = new FileWriter(fImage, false);
		writer.write(strContent);
		writer.close();
	}
	
	
	
}
