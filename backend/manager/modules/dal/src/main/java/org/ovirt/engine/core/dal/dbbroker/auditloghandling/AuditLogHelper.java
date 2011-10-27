package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuditLogHelper {
    public static CustomLogField[] getCustomLogFields(Class<?> type, boolean inherit) {
        CustomLogField[] returnValue = new CustomLogField[0];

        // Look for inhertied ones
        if (inherit) {
            Class<?>[] interfaces = type.getInterfaces();
            Class<?> superClass = type.getSuperclass();
            if (superClass != null) {
                returnValue = AuditLogHelper.merge(returnValue, AuditLogHelper.getCustomLogFields(superClass, true));
            }
            for (Class<?> clazz : interfaces) {
                if (!clazz.equals(type)) {
                    returnValue = AuditLogHelper.merge(returnValue, AuditLogHelper.getCustomLogFields(clazz, true));
                }

            }
        }

        // Add any you find on this class
        CustomLogField field = (CustomLogField) type.getAnnotation(CustomLogField.class);
        CustomLogFields fields = (CustomLogFields) type.getAnnotation(CustomLogFields.class);
        ArrayList<CustomLogField> myAnnotations = new ArrayList<CustomLogField>();
        if (field != null) {
            myAnnotations.add(field);
        }
        if (fields != null) {
            for (CustomLogField inner : fields.value()) {
                myAnnotations.add(inner);
            }
        }
        CustomLogField[] myAnns = myAnnotations.toArray(new CustomLogField[0]);
        returnValue = AuditLogHelper.merge(returnValue, myAnns);
        return returnValue;
    }

    protected static <T> T[] merge(T[] list, T[] items) {
        List<T> listList = new ArrayList<T>(Arrays.asList(list));
        for (T item : items) {
            if (!listList.contains(item)) {
                listList.add(item);
            }
        }
        return listList.toArray(Arrays.copyOf(list, 0));
    }
}
