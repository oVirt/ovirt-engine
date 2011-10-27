package org.ovirt.engine.core.compat.backendcompat;

import org.w3c.dom.Node;

public class XmlAttribute extends XmlNode {

    public XmlAttribute(Node node) {
        super(node);
    }

    public String getValue() {
        return node.getTextContent();
    }
}
