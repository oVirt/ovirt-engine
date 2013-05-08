package org.ovirt.engine.core.compat.backendcompat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class XmlNamespaceManager implements NamespaceContext {

    private Map<String, String> prefixToUri = new HashMap<String, String>();

    public XmlNamespaceManager(Object nameTable) {
        // JTODO What can we do about name table ?
    }

    public void AddNamespace(String prefix, String uri) {
        prefixToUri.put(prefix, uri);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return prefixToUri.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for (String prefix : prefixToUri.keySet()) {
            if (prefixToUri.get(prefix).equals(namespaceURI))
                return prefix;
        }

        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        List<String> prefixes = new LinkedList<String>();

        for (String prefix : prefixToUri.keySet()) {
            if (prefixToUri.get(prefix).equals(namespaceURI))
                prefixes.add(prefix);
        }

        return prefixes.iterator();
    }
}
