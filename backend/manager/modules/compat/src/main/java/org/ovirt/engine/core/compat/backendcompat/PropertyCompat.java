package org.ovirt.engine.core.compat.backendcompat;

import java.beans.PropertyDescriptor;

// This will be a wrapper for import java.beans.PropertyDescriptor;
public class PropertyCompat extends PropertyInfo {

    public PropertyCompat(PropertyDescriptor pd) {
        super(pd);
    }

}
