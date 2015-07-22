package org.ovirt.engine.core.utils;

import java.util.Properties;

@SuppressWarnings("serial")
public class NoDuplicateProperties extends Properties {
    /**
     * This method overrides the put method of Map in order to add the validation
     * of an existing key. Otherwise the Hashtable implementation inherited by
     * Properties class it would just override existing value without indicating
     * the caller that it already exists.
     */
    @Override
    public Object put(Object key, Object value) {
        if(containsKey(key))   {
            throw new DuplicatePropertyException("The key " + key + " already exists");
        }
        return super.put(key, value);
    }
}
