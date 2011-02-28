package org.synyx.mojo.opencms.helper;

import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.opencms.security.CmsAccessControlEntry;

/**
 * Converts the given element.
 * 
 * @author Rainer Steinegger - rainer.steinegger@synyx.de - Synyx GmbH & Co. KG
 */
public class FlagConverter extends A_AttributeConverter {

    /** Attribute name for deleted. */
    public static final String ATTR_DELETED = "deleted";
    /** Attribute name for inherit. */
    public static final String ATTR_INHERIT = "inherit";
    /** Attribute name for overwrite. */
    public static final String ATTR_OVERWRITE = "overwrite";
    /** Attribute name for inherited. */
    public static final String ATTR_INHERITED = "inherited";
    /** Attribute name for user. */
    public static final String ATTR_USER = "user";
    /** Attribute name for group. */
    public static final String ATTR_GROUP = "group";
    /** Attribute name for responsible. */
    public static final String ATTR_RESPONSIBLE = "responsible";
    /** Value of the attribute, so it can be added. */
    public static final String ATTR_TRUE_VALUE = "1";
    
    /**
     * Sets the searched tag name.
     * 
     * @param tagName The name of the searched tag.
     * @param parentName The name of the parent tag.
     */
    public FlagConverter (String tagName, String parentName) {
        super(tagName, parentName);
    }
        
    @Override
    public void convertTag(Element element) {
        int value = 0;
        
        List<Attribute> attributes = (List<Attribute>) element.attributes();
        int size = attributes.size();
        for (int i = 0; i < size; i++) {
            // allways get the first element, because every cycle one attribute becomes deleted
            Attribute attribute = attributes.get(0);
            
            String attributeName = attribute.getName();
            if (attributeName != null) {
                attributeName = attributeName.toLowerCase();
            } else {
                attributeName = "";
            }

            if (ATTR_TRUE_VALUE.equals(attribute.getValue())) {
                if (ATTR_DELETED.equals(attributeName)) {
                    value += CmsAccessControlEntry.ACCESS_FLAGS_DELETED;
                } else if (ATTR_INHERIT.equals(attributeName)) {
                    value += CmsAccessControlEntry.ACCESS_FLAGS_INHERIT;
                } else if (ATTR_OVERWRITE.equals(attributeName)) {
                    value += CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE;
                } else if (ATTR_INHERITED.equals(attributeName)) {
                    value += CmsAccessControlEntry.ACCESS_FLAGS_INHERITED;
                } else if (ATTR_USER.equals(attributeName)) {
                    value += CmsAccessControlEntry.ACCESS_FLAGS_USER;
                } else if (ATTR_GROUP.equals(attributeName)) {
                    value += CmsAccessControlEntry.ACCESS_FLAGS_GROUP;
                } else if (ATTR_RESPONSIBLE.equals(attributeName)) {
                    value += CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE;
                }
            }
                      
            element.remove(attribute);            
            
        }
        
        if (!element.isReadOnly()) { 
            if (element.getText() == null || ! "".equals(element.getText())) {
                element.clearContent();
            }
            element.addText(String.valueOf(value));            
        }
        
    }

}
