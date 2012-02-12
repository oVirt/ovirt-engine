package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.backendcompat.PropertyInfo;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public final class ObjectCompareUtils {
    /**
     * Compare two collections from same type and check if their data are same
     *
     * @param from
     *            first collection
     * @param to
     *            second collection
     * @param idField
     *            name of the identifier field in underline object
     * @param properties
     *            list of properties to compare. If all the properties of all objects have the same value - collections
     *            decided to be equal
     * @return
     */
    public static boolean IsDataChanged(java.util.Collection from, java.util.Collection to, String idField,
            java.util.ArrayList<String> properties) {
        boolean returnValue = true;
        try {
            // First check if both collection have same count of members
            if (from.size() == to.size()) {
                // Create temporary hashtable from first collection to easy data
                // access
                // Hash table structure: kye:Id value: object
                java.util.Hashtable table = CreateHashtable(from, idField);
                /**
                 * Check if in both lists stored members with same Ids
                 */
                if (IsListsSame(table, to, idField)) {
                    /**
                     * Check if in both lists members have same value of identification properties
                     */
                    returnValue = !IsListsEqual(table, to, idField, properties);
                }
            }
        }
        /**
         * if one of functions failed - data wrong. Data decided be changed
         */
        catch (java.lang.Exception e) {
        }
        return returnValue;
    }

    private static java.util.Hashtable CreateHashtable(java.util.Collection collection, String idField) {
        java.util.Hashtable table = new java.util.Hashtable();
        if (collection != null) {
            for (Object obj : collection) {
                java.lang.Class type = obj.getClass();
                Object id = TypeCompat.GetProperty(type, idField).GetValue(obj, null);
                table.put(id, obj);
            }
        }
        return table;
    }

    private static boolean IsListsSame(java.util.Hashtable table, java.util.Collection collection, String idField) {
        for (Object obj : collection) {
            java.lang.Class type = obj.getClass();
            Object id = TypeCompat.GetProperty(type, idField).GetValue(obj, null);
            if (!table.containsKey(id)) {
                return false;
            }
        }
        return true;
    }

    private static boolean IsListsEqual(java.util.Hashtable table, java.util.Collection collection, String idField,
            java.util.ArrayList<String> props) {
        for (Object obj : collection) {
            java.lang.Class type = obj.getClass();
            Object id = TypeCompat.GetProperty(type, idField).GetValue(obj, null);
            Object second = table.get(id);
            if (!IsObjectsEqual(obj, second, props)) {
                return false;
            }
        }
        return true;
    }

    public static boolean IsObjectsEqual(Object first, Object second, java.util.ArrayList<String> props) {
        java.lang.Class type = first.getClass();
        if (type != second.getClass()) {
            return false;
        }
        for (String property : props) {
            PropertyInfo firstProperty = TypeCompat.GetProperty(type, property);
            if (firstProperty == null) {
                log.errorFormat(
                        "A property with name {0} was not found on object of type {1}. Please remove it from the Changable Property List.",
                        property,
                        type.getName());
                continue;
            }

            Object firstValue = firstProperty.GetValue(first, null);
            Object secondValue = TypeCompat.GetProperty(type, property).GetValue(second, null);

            if (firstValue != null && !firstValue.equals(secondValue) || ((firstValue == null && secondValue != null))) {
                return false;
            }
        }
        return true;
    }

    public static void GetDiffBetweenGroups(java.util.Collection OriginalItems, java.util.Collection UpdatedItems,
            String IdentifierAttribute, RefObject<java.util.ArrayList> NewItems,
            RefObject<java.util.ArrayList> DeletedItems) {
        NewItems.argvalue = new java.util.ArrayList();
        DeletedItems.argvalue = new java.util.ArrayList();
        java.util.Hashtable htOriginal = CreateHashtable(OriginalItems, IdentifierAttribute);
        java.util.Hashtable htUpdated = CreateHashtable(UpdatedItems, IdentifierAttribute);
        for (Object oldItemKey : htOriginal.keySet()) {
            if (!htUpdated.contains(oldItemKey)) {
                DeletedItems.argvalue.add(htOriginal.get(oldItemKey));
            }
        }
        for (Object newItemKey : htUpdated.keySet()) {
            if (!htOriginal.contains(newItemKey)) {
                NewItems.argvalue.add(htUpdated.get(newItemKey));
            }
        }
    }

    public static void GetDiffBetweenGroups(java.util.List OriginalItems, java.util.List UpdatedItems,
            RefObject<java.util.ArrayList> NewItems, RefObject<java.util.ArrayList> DeletedItems) {
        NewItems.argvalue = new java.util.ArrayList();
        DeletedItems.argvalue = new java.util.ArrayList();
        for (Object oldItem : OriginalItems) {
            if (!UpdatedItems.contains(oldItem)) {
                DeletedItems.argvalue.add(oldItem);
            }
        }
        for (Object newItem : UpdatedItems) {
            if (!OriginalItems.contains(newItem)) {
                NewItems.argvalue.add(newItem);
            }
        }
    }

    private static Log log = LogFactory.getLog(ObjectCompareUtils.class);
}
