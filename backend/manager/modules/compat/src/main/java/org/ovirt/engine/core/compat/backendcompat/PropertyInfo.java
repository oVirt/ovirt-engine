package org.ovirt.engine.core.compat.backendcompat;

import java.beans.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//This will be a wrapper for import java.beans.PropertyDescriptor;
public class PropertyInfo {
    private static final Log log = LogFactory.getLog(PropertyInfo.class);

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
            log.warn("Unable to get value of property: " + pd.getDisplayName() + " for class "
                    + obj.getClass().getName());
        }
        return returnValue == null ? defaultValue : returnValue;
    }

    public boolean getCanWrite() {
        return pd.getWriteMethod() != null;
    }

    public boolean isPropertyInstanceOf(Class<?> clazz) {
        return this.pd.getPropertyType().equals(clazz);
    }
}
