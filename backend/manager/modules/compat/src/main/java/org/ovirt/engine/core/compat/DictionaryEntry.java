package org.ovirt.engine.core.compat;

/**
 * @deprecated Use org.apache.commons.collections.KeyValue instead.
 */
@Deprecated
public class DictionaryEntry {

    private String key;
    private Object value;

    public DictionaryEntry(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return StringFormat.format("Dictionary Entry [Key: '%s' Value: '%s']", key, value);
    }

}
