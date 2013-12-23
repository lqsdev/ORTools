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

	
	public void saveFile(String strContent) throws IOException{
		FileWriter writer = new FileWriter(new File("/Users/Lei/Documents/workspace/fodt/bin/File/book/testDocbook.txt"), true);
		writer.write(strContent);
		writer.close();
	}
	
	
}
