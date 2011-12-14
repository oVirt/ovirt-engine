package org.ovirt.engine.core.utils;

import java.util.List;

import org.ovirt.engine.core.compat.backendcompat.PropertyInfo;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;

public final class ObjectDescriptor {
    public static String toString(Object obj) {
        Class type = obj.getClass();
        List<PropertyInfo> properties = TypeCompat.GetProperties(type);
        StringBuilder builder = new StringBuilder(String.format("Class Name: %1$s", type.getName()));
        builder.append("\n");
        for (PropertyInfo property : properties) {
            String propertyName = property.getName();
            Object objValue = property.GetValue(obj, null);
            String strObjectValue;
            if (objValue != null) {
                if (!(objValue instanceof String) && !(objValue instanceof java.util.Map)
                        && objValue instanceof Iterable) {
                    StringBuilder tempBuilder = new StringBuilder();
                    for (Object o : (Iterable) objValue) {
                        tempBuilder.append(o.toString());
                        tempBuilder.append("\n");
                    }
                    strObjectValue = tempBuilder.toString();
                } else {
                    strObjectValue = objValue.toString();
                }
            } else {
                strObjectValue = "Null";
            }
            builder.append(String.format("%1$-30s%2$s", propertyName, strObjectValue));
            builder.append("\n");
        }
        java.lang.reflect.Field[] fields = type.getFields();
        for (java.lang.reflect.Field field : fields) {
            String propertyName = field.getName();
            String strObjectValue;

            try {
                field.get(null);
                continue; // ignore static fields
            } catch (IllegalAccessException e) {
            } catch (NullPointerException e) {
            }

            Object objValue = null;
            try {
                objValue = field.get(obj);
            } catch (IllegalAccessException e) {
            }

            if (objValue != null) {
                if (!(objValue instanceof String) && !(objValue instanceof java.util.Map)
                        && objValue instanceof Iterable) {
                    StringBuilder tempBuilder = new StringBuilder();
                    for (Object o : (Iterable) objValue) {
                        tempBuilder.append(o.toString());
                        tempBuilder.append("\n");
                    }
                    strObjectValue = tempBuilder.toString();
                } else {
                    strObjectValue = objValue.toString();
                }
            } else {
                strObjectValue = "Null";
            }
            builder.append(String.format("%1$-30s%2$s", propertyName, strObjectValue));
            builder.append("\n");
        }
        return builder.toString();
    }

}
