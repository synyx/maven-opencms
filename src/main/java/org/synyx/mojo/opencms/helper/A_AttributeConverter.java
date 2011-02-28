package org.synyx.mojo.opencms.helper;

import org.dom4j.Element;

/**
 * This helper class is used to convert a tag of an xml file. Therefore the class
 * defines the name of tag, which should be converted. If the name tag was found
 * the class gets the document to convert it.
 *
 * @author Rainer Steinegger - Synyx GmbH & Co. KG - rainer.steinegger@synyx.de
 */
public abstract class A_AttributeConverter {

    /** The searched tag name. */
    private String tagName = null;
    
    /** The tag name of the parent tag. */
    private String parentName = null;
    
    /**
     * Needed to set the tag name. If the parent tag name is null, the isSearchedTag() method 
     * does not evaluate the parent tag name.
     * 
     * @param tagName The name of the searched tag.
     * @param parentName The name of the parent tag.
     */
    public A_AttributeConverter(String tagName, String parentName) {
        this.tagName = tagName;
        this.parentName = parentName;
    }

    /**
     * This method is called, if the tag was found.
     * 
     * @param element The searched element.
     * @throws Exception 
     */
    public abstract void convertTag(Element element) throws Exception;
    
    /**
     * Checks if the given tag is the searched tag. 
     * If the parent tag name is null, the isSearchedTag() method 
     * does not evaluate the parent tag name.
     * 
     * @param element The element to check.
     * @return True if this element is the searched one.
     * @throws Exception 
     */
    public boolean isSearchedTag(Element element) throws Exception {
        boolean isSearchedTag = false;
        if (element != null) {
            Element parent = element.getParent();

            if (parent != null) {
                String currentElementName = element.getName();
                String currentParentName = parent.getName();                
                if (currentElementName.equals(getTagName())) {
                    if (currentParentName == null || 
                            currentParentName.equals(getParentName())) {                        
                        isSearchedTag = true;
                    }                
                }
            }
        }
        
        return isSearchedTag;
    }
    
    /**
     * Getter for the tag name.
     * @return The name of the searched tag.
     */
    public String getTagName() {
        return tagName;
    }
    
    /**
     * Getter for the parent tag name.
     * @return The name of the parent tag.
     */
    public String getParentName() {
        return parentName;
    }
}
