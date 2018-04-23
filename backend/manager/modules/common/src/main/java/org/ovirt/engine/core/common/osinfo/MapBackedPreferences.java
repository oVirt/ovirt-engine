package org.ovirt.engine.core.common.osinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.AbstractPreferences;

public class MapBackedPreferences extends AbstractPreferences implements Serializable {

    private static final long serialVersionUID = -6359144559146465048L;
    Map<String, String> preferencesStore;
    List<String> childNodes;

    /**
     * Creates a preference node with the specified parent and the specified
     * name relative to its parent.
     *
     * @see AbstractPreferences
     */
    public MapBackedPreferences(AbstractPreferences parent, String name) {
        super(parent, name);
        preferencesStore = new HashMap<>();
        childNodes = new ArrayList<>();
    }

    @Override
    protected void putSpi(String key, String value) {
        preferencesStore.put(key, value);
    }

    @Override
    protected String getSpi(String key) {
        return preferencesStore.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        preferencesStore.remove(key);
    }

    @Override
    protected void removeNodeSpi() {
        if (parent() == null) {
            throw new IllegalArgumentException("Cannot remove root node");
        }
        parent().remove(this.name());
    }

    @Override
    protected String[] keysSpi() {
        return preferencesStore.keySet().toArray(new String[preferencesStore.size()]);
    }

    @Override
    protected String[] childrenNamesSpi() {
        return childNodes.toArray(new String[childNodes.size()]);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        childNodes.add(name);
        return new MapBackedPreferences(this, name);
    }

    @Override
    protected void syncSpi() {
        throw new UnsupportedOperationException("This implementation doesn't support a backing store");
    }

    @Override
    protected void flushSpi() {
        throw new UnsupportedOperationException("This implementation doesn't support a backing store");
    }

}
