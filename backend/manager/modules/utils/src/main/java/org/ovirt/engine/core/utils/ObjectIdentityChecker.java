package org.ovirt.engine.core.utils;

import java.util.List;

import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.IObjectDescriptorContainer;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.compat.backendcompat.PropertyInfo;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;

public class ObjectIdentityChecker {
    private IObjectDescriptorContainer mContainer;
    private static java.util.HashMap<String, java.lang.Class> mAliases =
            new java.util.HashMap<String, java.lang.Class>();
    private static java.util.HashMap<java.lang.Class, ObjectIdentityChecker> mIdentities =
            new java.util.HashMap<java.lang.Class, ObjectIdentityChecker>();
    private static java.util.HashMap<java.lang.Class, java.lang.Class> mStatusTypes =
            new java.util.HashMap<java.lang.Class, java.lang.Class>();
    private java.util.HashMap<Enum, java.util.ArrayList<String>> mDictionary =
            new java.util.HashMap<Enum, java.util.ArrayList<String>>();
    private java.util.ArrayList<String> mPermitted = new java.util.ArrayList<String>();

    public ObjectIdentityChecker(java.lang.Class type) {
        mIdentities.put(type, this);
    }

    public ObjectIdentityChecker(java.lang.Class type, Iterable<String> aliases) {
        this(type);
        for (String alias : aliases) {
            mAliases.put(alias, type);
        }
    }

    public ObjectIdentityChecker(java.lang.Class type, Iterable<String> aliases, java.lang.Class enumType) {
        this(type, aliases);
        mStatusTypes.put(type, enumType);
    }

    public final void setContainer(IObjectDescriptorContainer value) {
        mContainer = value;
    }

    public static boolean CanUpdateField(Object fieldContainer, String fieldName, Enum status) {
        return CanUpdateField(fieldContainer.getClass().getSimpleName(), fieldName, status, fieldContainer);
    }

    public static boolean CanUpdateField(String objectType, String fieldName, Enum status, Object fieldContainer) {
        java.lang.Class type = null;
        if ((type = mAliases.get(objectType)) != null) {
            return CanUpdateField(type, fieldName, status, fieldContainer);
        } else {
            throw new RuntimeException(String.format("status type %1$s not exist", type));
        }
    }

    public static boolean CanUpdateField(java.lang.Class objectType, String fieldName, Enum status,
            Object fieldContainer) {
        ObjectIdentityChecker checker = null;
        if ((checker = mIdentities.get(objectType)) != null) {
            return checker.IsFieldUpdatable(status, fieldName, fieldContainer);
        }
        return true;
    }

    public static boolean CanUpdateField(String objectType, String fieldName, String status) {
        java.lang.Class type = null;
        if ((type = mAliases.get(objectType)) != null) {
            java.lang.Class statusType = null;
            if ((statusType = mStatusTypes.get(type)) != null) {
                Enum currentStatus;
                try {
                    currentStatus = (Enum) EnumUtils.valueOf(statusType, status, true);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(String.format("status type %1$s not contain type %2$s", statusType,
                            status));
                }
                return CanUpdateField(type, fieldName, currentStatus);
            } else {
                throw new RuntimeException(String.format("status type %1$s not exist", type));
            }
        } else {
            throw new RuntimeException(String.format("object type %1$s not exist", objectType));
        }
    }

    public final void AddField(Enum status, String fieldName) {
        java.util.ArrayList<String> values = null;
        if (!((values = mDictionary.get(status)) != null)) {
            values = new java.util.ArrayList<String>();
            mDictionary.put(status, values);
        }
        if (!values.contains(fieldName)) {
            values.add(fieldName);
        }
    }

    public final void AddField(Iterable<Enum> statuses, String fieldName) {
        for (Enum status : statuses) {
            AddField(status, fieldName);
        }
    }

    public final void AddFields(Iterable<Enum> statuses, Iterable<String> fields) {
        for (String field : fields) {
            AddField(statuses, field);
        }
    }

    public final void AddPermittedField(String fieldName) {
        if (!mPermitted.contains(fieldName)) {
            mPermitted.add(fieldName);
        }
    }

    public final void AddPermittedFields(String[] fieldNames) {
        for (String fieldName : fieldNames) {
            AddPermittedField(fieldName);
        }
    }

    public final boolean IsFieldUpdatable(String name) {
        return mPermitted.contains(name);
    }

    private boolean IsFieldUpdatable(Enum status, String name, Object fieldContainer) {
        boolean returnValue = true;
        if (!IsFieldUpdatable(name)) {
            if (fieldContainer != null && mContainer != null
                    && !mContainer.CanUpdateField(fieldContainer, name, status)) {
                returnValue = false;
            } else {
                java.util.ArrayList<String> values = null;
                if ((values = mDictionary.get(status)) != null && values != null) {
                    returnValue = values.contains(name);
                } else {
                    returnValue = false;
                }
            }
            if (!returnValue) {
                LogError(name, status);
            }
        }
        return returnValue;
    }

    public final boolean IsUpdateValid(Object source, Object destination) {
        if (source.getClass() != destination.getClass()) {
            return false;
        }
        for (String fieldName : GetChangedFields(source, destination)) {
            if (!IsFieldUpdatable(fieldName)) {
                return false;
            }
        }
        return true;
    }

    public final boolean IsUpdateValid(Object source, Object destination, Enum status) {
        if (source.getClass() != destination.getClass()) {
            return false;
        }
        for (String fieldName : GetChangedFields(source, destination)) {
            if (!IsFieldUpdatable(status, fieldName, null)) {
                log.warn(String.format("ObjectIdentityChecker.IsUpdateValid:: Not updatable field '%1$s' was updated",
                        fieldName));
                return false;
            }
        }
        return true;
    }

    public final boolean IsFieldsUpdated(Object source, Object destination, Iterable<String> fields) {
        java.util.ArrayList<String> changedFields = GetChangedFields(source, destination);
        for (String field : fields) {
            if (changedFields.contains(field)) {
                return true;
            }
        }
        return false;
    }

    public static java.util.ArrayList<String> GetChangedFields(Object source, Object destination) {
        java.util.ArrayList<String> returnValue = new java.util.ArrayList<String>();
        if (source.getClass().isInstance(destination)) {
            Class objectType = source.getClass();
            List<PropertyInfo> properties = TypeCompat.GetProperties(objectType);
            for (PropertyInfo property : properties) {
                Object sourceValue = property.GetValue(source, null);
                Object destinationValue = property.GetValue(destination, null);

                if (!(property.isPropertyInstanceOf(ValueObjectMap.class)) && property.getCanWrite()
                        && sourceValue != null && !sourceValue.equals(destinationValue)
                        || ((sourceValue == null && destinationValue != null))) {
                    returnValue.add(property.getName());
                }
            }
        }
        return returnValue;
    }

    /**
     * Logs the error.
     *
     * @param name
     *            The name.
     * @param status
     *            The status.
     */
    private static void LogError(String name, Enum status) {
        log.errorFormat("Field {0} can not be updated when status is {1}", name, status);
    }

    private static Log log = LogFactory.getLog(ObjectIdentityChecker.class);
}
