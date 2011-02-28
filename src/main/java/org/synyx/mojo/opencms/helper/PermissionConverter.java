package org.synyx.mojo.opencms.helper;

import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.opencms.security.CmsPermissionSet;

/**
 * Converts the given element.
 *
 * @author Rainer Steinegger - rainer.steinegger@synyx.de - Synyx GmbH & Co. KG
 */
public class PermissionConverter extends A_AttributeConverter {
    /** Attribute name for tag read. */
    private static final String ATTR_READ = "read";
    /** Attribute name for tag write. */
    private static final String ATTR_WRITE = "write";
    /** Attribute name for tag view. */
    private static final String ATTR_VIEW = "view";
    /** Attribute name for tag control. */
    private static final String ATTR_CONTROL = "control";
    /** Attribute name for tag publish. */
    private static final String ATTR_PUBLISH = "publish";
    /** Value of the attribute, so it can be added. */
    public static final String ATTR_TRUE_VALUE = "1";

    /**
     * Sets the searched tag name.
     * 
     * @param tagName The name of the searched tag.
     * @param parentName The name of the parent tag.
     */
    public PermissionConverter (String tagName, String parentName) {
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
                if (ATTR_READ.equals(attributeName)) {
                    value += CmsPermissionSet.PERMISSION_READ;
                } else if (ATTR_WRITE.equals(attributeName)) {
                    value += CmsPermissionSet.PERMISSION_WRITE;
                } else if (ATTR_VIEW.equals(attributeName)) {
                    value += CmsPermissionSet.PERMISSION_VIEW;
                } else if (ATTR_CONTROL.equals(attributeName)) {
                    value += CmsPermissionSet.PERMISSION_CONTROL;
                } else if (ATTR_PUBLISH.equals(attributeName)) {
                    value += CmsPermissionSet.PERMISSION_DIRECT_PUBLISH;
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
