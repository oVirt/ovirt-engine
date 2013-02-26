package org.ovirt.engine.core.compat.backendcompat;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// This will wrap java beans introspection
public class TypeCompat {

    private static final String CLASS = "class";
    private static final Log log = LogFactory.getLog(TypeCompat.class);

    public static List<PropertyInfo> GetProperties(Class<?> type) {
        List<PropertyInfo> returnValue = new ArrayList<PropertyInfo>();
        try {
            PropertyDescriptor[] pds = Introspector.getBeanInfo(type).getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                // Class is a bogus property, remove it
                if (!CLASS.equals(pd.getName())) {
                    returnValue.add(new PropertyInfo(pd));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return returnValue;
    }

    public static PropertyInfo GetProperty(Class<?> type, String idField) {
        try {
            PropertyDescriptor[] pds = Introspector.getBeanInfo(type).getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equalsIgnoreCase(idField)) {
                    return new PropertyInfo(pd);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * The following method will get values for properties of provided object and will keep them in map
     * @param obj - this is an object which values will be taken
     * @param properties - a set of properties names
     * @param values - a map which will contains all values of properties
     */
    public static void getPropertyValues(Object obj, Set<String> properties, Map<String, String> values) {
        try {
            PropertyDescriptor[] pds = Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors();
            int hitCount = 0;
            for (PropertyDescriptor pd : pds) {
                String propertyName = pd.getName().toLowerCase();
                if (properties.contains(propertyName)) {
                    Object value = null;
                    hitCount++;
                    try {
                        value = pd.getReadMethod().invoke(obj);
                        String stringValue = value != null ? value.toString() : null;
                        values.put(propertyName, stringValue);
                    } catch (Exception e) {
                        log.warn("Unable to get value of property: " + pd.getDisplayName() + " for class "
                                + obj.getClass().getName());
                    }
                    if (hitCount == properties.size()) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
