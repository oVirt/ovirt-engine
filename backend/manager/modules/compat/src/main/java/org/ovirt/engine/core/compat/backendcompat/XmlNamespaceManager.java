package org.ovirt.engine.core.compat.backendcompat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class XmlNamespaceManager implements NamespaceContext {

    private Map<String, String> prefixToUri;

    public XmlNamespaceManager(Object nameTable) {
        prefixToUri = new HashMap<String, String>();
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
        for (Map.Entry<String, String> prexiToUriEntry : prefixToUri.entrySet()) {
            if (prexiToUriEntry.getValue().equals(namespaceURI)) {
                return prexiToUriEntry.getKey();
            }
        }

        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        List<String> prefixes = new LinkedList<String>();

        for (Map.Entry<String, String> prefixToUriEntry : prefixToUri.entrySet()) {
            if (prefixToUriEntry.getValue().equals(namespaceURI)) {
                prefixes.add(prefixToUriEntry.getKey());
            }
        }

        return prefixes.iterator();
    }
}
