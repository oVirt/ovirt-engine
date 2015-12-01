package org.ovirt.engine.core.compat.backendcompat;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This will wrap java beans introspection
public class TypeCompat {

    private static final String CLASS = "class";
    private static final Logger log = LoggerFactory.getLogger(TypeCompat.class);

    public static List<PropertyInfo> getProperties(Class<?> type) {
        List<PropertyInfo> returnValue = new ArrayList<>();
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
                    if(!values.containsKey(propertyName))  {
                        try {
                            value = pd.getReadMethod().invoke(obj);
                            String stringValue = value != null ? value.toString() : null;
                            values.put(propertyName, stringValue);
                        } catch (Exception e) {
                            log.warn("Unable to get value of property: '{}' for class {}: {}",
                                    pd.getDisplayName(), obj.getClass().getName(), e.getMessage());
                            log.debug("Exception", e);
                        }
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
