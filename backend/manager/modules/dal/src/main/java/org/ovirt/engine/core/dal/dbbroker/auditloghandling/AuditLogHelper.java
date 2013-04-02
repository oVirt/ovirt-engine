package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.HashSet;
import java.util.Set;

public class AuditLogHelper {

    public static Set<String> getCustomLogFields(Class<?> type, boolean inherit) {
        Set<String> returnValue = null;

        // Look for inherited ones
        if (inherit) {
            Class<?>[] interfaces = type.getInterfaces();
            Class<?> superClass = type.getSuperclass();
            if (superClass != null) {
                returnValue = getCustomLogFields(superClass, true);
            }
            for (Class<?> clazz : interfaces) {
                if (!clazz.equals(type)) {
                    returnValue = merge(returnValue, getCustomLogFields(clazz, true));
                }
            }
        }

        // Add any you find on this class
        CustomLogFields fields = (CustomLogFields) type.getAnnotation(CustomLogFields.class);
        Set<String> myAnnotations = null;
        if (fields != null) {
            myAnnotations = new HashSet<String>();
            for (String inner : fields.value()) {
                myAnnotations.add(inner.toLowerCase());
            }
        }
        return merge(returnValue, myAnnotations);
    }

    public static <T> Set<T> merge(Set<T> list, Set<T> items) {
        if (list != null) {
            if (items != null) {
                list.addAll(items);
            }
        } else {
            return items;
        }
        return list;
    }
}
