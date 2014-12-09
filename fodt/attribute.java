package fodt;
/**
 * An attribute
 * For example:  <text:outline-level-style text:level="1">
 * name: text:level
 * value: 1
 * @author Lei
 *
 */
public class attribute {
	private String strName;		// attribute's name of a fodt file
	private String strValue;	// Attribute's value of a fodt file
	
	
	public String getName() {
		return strName;
	}
	public void setName(String strName) {
		this.strName = strName;
	}
	public String getValue() {
		return strValue;
	}
	public void setValue(String strValue) {
		this.strValue = strValue;
	}

	
}
