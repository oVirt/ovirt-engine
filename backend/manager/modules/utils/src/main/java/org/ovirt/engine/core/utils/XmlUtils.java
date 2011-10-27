package org.ovirt.engine.core.utils;

@Deprecated
public class XmlUtils {
    /**
     * Gets the node Attribute value using XPath.
     *
     * @param doc
     *            The doc.
     * @param xPath
     *            The x path pattern to the node.
     * @param attribute
     *            The attribute name.
     * @param error
     *            The error string (if occured).
     * @return
     */
    // public static String GetNodeAttributeValue(XmlDocument doc, String xPath,
    // String attribute, RefObject<String> error)
    // {
    // throw new NotImplementedException() ;
    // String value = "";
    // error.argvalue = "";
    //
    // try
    // {
    // XmlNode node = doc.SelectSingleNode(xPath);
    // if (node != null)
    // {
    // value = node.Attributes[attribute].getValue();
    // }
    // }
    // catch (RuntimeException ex)
    // {
    // error.argvalue = ex.getMessage();
    // }
    // return value;
    // }

    /**
     * Sets the node Attribute value using XPath.
     *
     * @param doc
     *            The doc.
     * @param xPath
     *            The x path pattern to the node.
     * @param attribute
     *            The attribute name.
     * @param value
     *            The value.
     * @param error
     *            The error string (if occured).
     */
    // public static void SetNodeAttributeValue(XmlDocument doc, String xPath,
    // String attribute, String value, RefObject<String> error)
    // {
    // throw new NotImplementedException() ;
    // error.argvalue = "";
    // try
    // {
    // XmlNode node = doc.SelectSingleNode(xPath);
    // if (node != null)
    // {
    // node.Attributes[attribute].setValue(value);
    // }
    // }
    // catch (RuntimeException ex)
    // {
    // String.format("Unable to update certificate finger print in %1$s/[%2$s\n]",
    // xPath, attribute);
    // error.argvalue += ex.getMessage();
    // }
    // }
}
