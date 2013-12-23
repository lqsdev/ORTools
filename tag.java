package fodt;

import java.util.ArrayList;
import java.util.List;

/**
 * Stock a tag line like:  <text:outline-level-style text:level="1" style:num-format="">
 * tagname: text:outline-level-style
 * attribute: text:level="1" style:num-format=""
 * @author Lei
 *
 */
public class tag {
	private String strTagName;
	private List<attribute> lstAttribute = new ArrayList<attribute>();
	private String strObjTag;	// DocBook tag's name
	
	public String getTagName() {
		return strTagName;
	}
	
	public void setTagName(String strTagName) {
		this.strTagName = strTagName;
	}
	
	/**
	 * Put an attribute into list
	 * @param att
	 * @return
	 */
	public boolean putAttribute(attribute attr){
		return lstAttribute.add(attr);
	}
	
	/**
	 * If found attribute by name return its index else -1
	 * @param strName An attribute's name
	 * @return -1 if we can't find the attribute else return index
	 */
	public int getIndexByName(String strName){
		for (int nIndex = 0; nIndex < lstAttribute.size(); nIndex++){
			attribute att = lstAttribute.get(nIndex);
			if (strName.equals(att.getName())){
				return nIndex;
			}
		}
		return -1;
	}
	
	/**
	 * Get an attribute by its index
	 * @param nIndex index of an attribute
	 * @return if parameter is valid return the attribute correct 
	 * 			else return an empty attribute
	 */
	public attribute getAttribute(int nIndex){
		if (nIndex < 0 || nIndex > lstAttribute.size()){
			System.out.println("out of the list's range");
			return new attribute();
		}else{
			return lstAttribute.get(nIndex);	
		}
	}
	
	/**
	 * Get a value of attribute by name
	 * @param strName attribute's name
	 * @return the value of attribute
	 */
	public String getAttributeValueByName(String strName){
		for (attribute att : lstAttribute){
			if (att.getName().equals(strName)){
				return att.getValue();
			}
		}
		return "";
	}
	
	public List<attribute> getListAttr(){
		return lstAttribute;
	}
	
	public String getObjTag() {
		return strObjTag;
	}
	
	public void setObjTag(String strObjTag) {
		this.strObjTag = strObjTag;
	}
	
	public boolean isSameTagName(String strName){
		return strName.equals(strTagName);
	}
	
	public boolean isSameAttr(List<attribute> lstAttr){
		return lstAttr.equals(lstAttribute);
	}
}
