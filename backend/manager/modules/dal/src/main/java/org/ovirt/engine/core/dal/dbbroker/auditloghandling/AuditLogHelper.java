package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.ArrayList;
import java.util.List;

public class AuditLogHelper {

    public static List<String> getCustomLogFields(Class<?> type, boolean inherit) {
        List<String> returnValue = null;

        // Look for inhertied ones
        if (inherit) {
            Class<?>[] interfaces = type.getInterfaces();
            Class<?> superClass = type.getSuperclass();
            if (superClass != null) {
                returnValue = AuditLogHelper.merge(returnValue, getCustomLogFields(superClass, true));
            }
            for (Class<?> clazz : interfaces) {
                if (!clazz.equals(type)) {
                    returnValue = AuditLogHelper.merge(returnValue, getCustomLogFields(clazz, true));
                }

            }
        }

        // Add any you find on this class
        CustomLogField field = (CustomLogField) type.getAnnotation(CustomLogField.class);
        CustomLogFields fields = (CustomLogFields) type.getAnnotation(CustomLogFields.class);
        List<String> myAnnotations = new ArrayList<String>();
        if (field != null) {
            myAnnotations.add(field.value().toLowerCase());
        }
        if (fields != null) {
            for (CustomLogField inner : fields.value()) {
                myAnnotations.add(inner.value().toLowerCase());
            }
        }
        return merge(returnValue, myAnnotations);
    }

    protected static <T> List<T> merge(List<T> list, List<T> items) {
        if (list != null) {
            if (items != null) {
                for (T item : items) {
                    if (!list.contains(item)) {
                        list.add(item);
                    }
                }
            }
        } else {
            return items;
        }
        return list;
    }
}
