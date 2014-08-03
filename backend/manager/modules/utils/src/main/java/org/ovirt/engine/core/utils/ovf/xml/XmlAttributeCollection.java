package org.ovirt.engine.core.utils.ovf.xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XmlAttributeCollection {

    private NamedNodeMap nodesMap;

    public XmlAttributeCollection(NamedNodeMap nodesMap) {
        this.nodesMap = nodesMap;
    }

    public XmlAttribute get(String nodeName) {
        Node temp = nodesMap.getNamedItem(nodeName);
        if (temp != null) {
            XmlAttribute returnValue = new XmlAttribute(temp);
            return returnValue;
        }
        return null;
    }

}
