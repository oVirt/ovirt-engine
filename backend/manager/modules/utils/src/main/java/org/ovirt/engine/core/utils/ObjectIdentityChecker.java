package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.IObjectDescriptorContainer;
import org.ovirt.engine.core.compat.backendcompat.PropertyInfo;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ObjectIdentityChecker {
    private IObjectDescriptorContainer mContainer;
    private static Map<String, Class<?>> mAliases =
            new HashMap<String, Class<?>>();
    private static Map<Class<?>, ObjectIdentityChecker> mIdentities =
            new HashMap<Class<?>, ObjectIdentityChecker>();
    private static Map<Class<?>, Class<?>> mStatusTypes =
            new HashMap<Class<?>, Class<?>>();
    private Map<Enum<?>, List<String>> mDictionary =
            new HashMap<Enum<?>, List<String>>();
    private List<String> mPermitted = new ArrayList<String>();

    public ObjectIdentityChecker(Class<?> type) {
        mIdentities.put(type, this);
    }

    public ObjectIdentityChecker(Class<?> type, Iterable<String> aliases) {
        this(type);
        for (String alias : aliases) {
            mAliases.put(alias, type);
        }
    }

    public ObjectIdentityChecker(Class<?> type, Iterable<String> aliases, Class<?> enumType) {
        this(type, aliases);
        mStatusTypes.put(type, enumType);
    }

    public final void setContainer(IObjectDescriptorContainer value) {
        mContainer = value;
    }

    public static boolean CanUpdateField(Object fieldContainer, String fieldName, Enum<?> status) {
        return CanUpdateField(fieldContainer.getClass().getSimpleName(), fieldName, status, fieldContainer);
    }

    public static boolean CanUpdateField(String objectType, String fieldName, Enum<?> status, Object fieldContainer) {
        Class<?> type = null;
        if ((type = mAliases.get(objectType)) != null) {
            return CanUpdateField(type, fieldName, status, fieldContainer);
        } else {
            throw new RuntimeException(String.format("status type %1$s not exist", type));
        }
    }

    public static boolean CanUpdateField(Class<?> objectType, String fieldName, Enum<?> status,
            Object fieldContainer) {
        ObjectIdentityChecker checker = null;
        if ((checker = mIdentities.get(objectType)) != null) {
            return checker.IsFieldUpdatable(status, fieldName, fieldContainer);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean CanUpdateField(String objectType, String fieldName, String status) {
        Class<?> type = null;
        if ((type = mAliases.get(objectType)) != null) {
            @SuppressWarnings("rawtypes")
            final Class statusType = mStatusTypes.get(type);
            if (statusType != null) {
                Enum<?> currentStatus;
                try {
                    currentStatus = (Enum<?>) EnumUtils.valueOf(statusType, status, true);
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

    public final void AddField(Enum<?> status, String fieldName) {
        List<String> values = null;
        if (!((values = mDictionary.get(status)) != null)) {
            values = new ArrayList<String>();
            mDictionary.put(status, values);
        }
        if (!values.contains(fieldName)) {
            values.add(fieldName);
        }
    }

    public final void AddField(Iterable<Enum<?>> statuses, String fieldName) {
        for (Enum<?> status : statuses) {
            AddField(status, fieldName);
        }
    }

    public final void AddFields(Iterable<Enum<?>> statuses, Iterable<String> fields) {
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

    private boolean IsFieldUpdatable(Enum<?> status, String name, Object fieldContainer) {
        boolean returnValue = true;
        if (!IsFieldUpdatable(name)) {
            if (fieldContainer != null && mContainer != null
                    && !mContainer.CanUpdateField(fieldContainer, name, status)) {
                returnValue = false;
            } else {
                List<String> values = null;
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

    public final boolean IsUpdateValid(Object source, Object destination, Enum<?> status) {
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
        List<String> changedFields = GetChangedFields(source, destination);
        for (String field : fields) {
            if (changedFields.contains(field)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> GetChangedFields(Object source, Object destination) {
        final List<String> returnValue = new ArrayList<String>();
        if (source.getClass().isInstance(destination)) {
            Class<?> objectType = source.getClass();
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
    private static void LogError(String name, Enum<?> status) {
        log.errorFormat("Field {0} can not be updated when status is {1}", name, status);
    }

    private static Log log = LogFactory.getLog(ObjectIdentityChecker.class);
}
