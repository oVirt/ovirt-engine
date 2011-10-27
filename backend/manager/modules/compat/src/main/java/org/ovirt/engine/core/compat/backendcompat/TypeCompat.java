package org.ovirt.engine.core.compat.backendcompat;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;

import org.ovirt.engine.core.compat.CompatException;

// This will wrap java beans introspection
public class TypeCompat {
    public static PropertyInfo[] GetProperties(Class type) {
        ArrayList<PropertyInfo> returnValue = new ArrayList<PropertyInfo>();
        try {
            PropertyDescriptor[] pds = Introspector.getBeanInfo(type).getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                // Class is a bogud property, remove it
                if (!pd.getName().equals("class")) {
                    returnValue.add(new PropertyInfo(pd));
                }
            }
        } catch (Exception e) {
            throw new CompatException(e);
        }
        return returnValue.toArray(new PropertyInfo[returnValue.size()]);
    }

    public static PropertyCompat GetProperty(Class type, String idField) {
        try {
            PropertyDescriptor[] pds = Introspector.getBeanInfo(type).getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equalsIgnoreCase(idField)) {
                    return new PropertyCompat(pd);
                }
            }
        } catch (Exception e) {
            throw new CompatException(e);
        }
        return null;
    }
}
