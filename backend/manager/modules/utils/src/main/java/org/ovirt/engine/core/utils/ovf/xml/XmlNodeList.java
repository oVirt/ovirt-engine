package org.ovirt.engine.core.utils.ovf.xml;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.NodeList;

public class XmlNodeList implements Iterable<XmlNode> {

    private NodeList nodeList;

    public XmlNodeList(NodeList nodeList) {
        this.nodeList = nodeList;
    }

    @Override
    public Iterator<XmlNode> iterator() {
        List<XmlNode> list = new LinkedList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(new XmlNode(nodeList.item(i)));
        }
        return list.iterator();
    }
}
