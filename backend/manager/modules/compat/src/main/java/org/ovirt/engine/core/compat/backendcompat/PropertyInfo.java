package org.ovirt.engine.core.compat.backendcompat;

import java.beans.PropertyDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//This will be a wrapper for import java.beans.PropertyDescriptor;
public class PropertyInfo {
    private static final Logger log = LoggerFactory.getLogger(PropertyInfo.class);

    private PropertyDescriptor pd;

    public PropertyInfo(PropertyDescriptor pd) {
        this.pd = pd;
    }

    public String getName() {
        return pd.getName();
    }

    public Object getValue(Object obj, Object defaultValue) {
        Object returnValue = null;
        try {
            returnValue = pd.getReadMethod().invoke(obj);
        } catch (Exception e) {
            log.warn("Unable to get value of property: '{}' for class {}: {}",
                    pd.getDisplayName(), obj.getClass().getName(), e.getMessage());
            log.debug("Exception", e);
        }
        return returnValue == null ? defaultValue : returnValue;
    }

    public boolean getCanWrite() {
        return pd.getWriteMethod() != null;
    }

}
