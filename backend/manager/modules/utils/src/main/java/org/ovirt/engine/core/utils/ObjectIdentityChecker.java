package org.ovirt.engine.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.utils.IObjectDescriptorContainer;
import org.ovirt.engine.core.compat.backendcompat.PropertyInfo;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ObjectIdentityChecker {
    private IObjectDescriptorContainer container;
    private static Map<String, Class<?>> aliases =
            new HashMap<String, Class<?>>();
    private static Map<Class<?>, ObjectIdentityChecker> identities =
            new HashMap<Class<?>, ObjectIdentityChecker>();
    private Map<Enum<?>, Set<String>> dictionary =
            new HashMap<Enum<?>, Set<String>>();
    private Set<String> permitted = new HashSet<String>();
    private Set<String> hotsetAllowedFields = new HashSet<String>();

    public ObjectIdentityChecker(Class<?> type) {
        identities.put(type, this);
    }

    public ObjectIdentityChecker(Class<?> type, Iterable<Class<?>> aliases) {
        this(type);
        for (Class<?> alias : aliases) {
            ObjectIdentityChecker.aliases.put(alias.getSimpleName(), type);
        }
    }

    public final void setContainer(IObjectDescriptorContainer value) {
        container = value;
    }

    public static boolean CanUpdateField(Object fieldContainer, String fieldName, Enum<?> status) {
        return CanUpdateField(fieldContainer.getClass().getSimpleName(), fieldName, status, fieldContainer);
    }

    public static boolean CanUpdateField(String objectType, String fieldName, Enum<?> status, Object fieldContainer) {
        Class<?> type = aliases.get(objectType);
        if (type != null) {
            return CanUpdateField(type, fieldName, status, fieldContainer);
        } else {
            throw new RuntimeException("status type [null] not exist");
        }
    }

    public static boolean CanUpdateField(Class<?> objectType, String fieldName, Enum<?> status,
            Object fieldContainer) {
        ObjectIdentityChecker checker = identities.get(objectType);
        if (checker != null) {
            return checker.IsFieldUpdatable(status, fieldName, fieldContainer);
        }
        return true;
    }

    public final <T extends Enum<T>> void AddField(T status, String fieldName) {
        Set<String> values = dictionary.get(status);
        if (values == null) {
            values = new HashSet<String>();
            dictionary.put(status, values);
        }
        values.add(fieldName);
    }

    public final <T extends Enum<T>> void AddField(Iterable<T> statuses, String fieldName) {
        for (T status : statuses) {
            AddField(status, fieldName);
        }
    }

    public final void AddPermittedFields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            permitted.add(fieldName);
        }
    }

    public final void AddHotsetFields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            hotsetAllowedFields.add(fieldName);
        }
    }

    public final boolean IsFieldUpdatable(String name) {
        return permitted.contains(name);
    }

    public final boolean isHotSetField(String name) {
        return hotsetAllowedFields.contains(name);
    }

    public boolean IsFieldUpdatable(Enum<?> status, String name, Object fieldContainer) {
        return IsFieldUpdatable(status, name, fieldContainer, false);
    }

    public boolean IsFieldUpdatable(Enum<?> status, String name, Object fieldContainer, boolean hotsetEnabled) {
        boolean returnValue = true;
        if (!IsFieldUpdatable(name)) {
            if (fieldContainer != null && container != null
                    && !container.canUpdateField(fieldContainer, name, status)) {
                returnValue = false;
            } else {
                Set<String> values = dictionary.get(status);
                returnValue = values != null ? values.contains(name) : false;

                // if field is not updateable in this status, check if hotset request and its an hotset allowed field
                if (!returnValue && hotsetEnabled) {
                    returnValue = isHotSetField(name);
                }
            }
            if (!returnValue) {
                log.warnFormat("Field {0} can not be updated when status is {1}", name, status);
            }
        }
        return returnValue;
    }

    /**
     * This method will copy all fields that are not @editable from source obj to dest obj
     *
     * @param source object that has values of non editable fields
     * @param destination object to copy the non editable to it
     */
    public boolean copyNonEditableFieldsToDestination(Object source, Object destination, boolean hotSetEnabled) {
        Class<?> cls = source.getClass();
        while (!cls.equals(Object.class)) {
            for (Field srcFld : cls.getDeclaredFields()) {
                try {
                    // copy fields that are non final, and not-editable and not a hotset field or it is but this is not hotset case
                    if (!Modifier.isFinal(srcFld.getModifiers()) &&
                            !IsFieldUpdatable(srcFld.getName()) &&
                            (!isHotSetField(srcFld.getName()) || !hotSetEnabled)) {
                        srcFld.setAccessible(true);

                        Field dstFld = cls.getDeclaredField(srcFld.getName());
                        dstFld.setAccessible(true);
                        dstFld.set(destination, srcFld.get(source));
                    }
                } catch (Exception exp) {
                    log.errorFormat("Failed to copy non editable field {0}, error: {1}",
                            srcFld.getName(),
                            exp.getMessage());
                    return false;
                }
            }
            cls = cls.getSuperclass();
        }
        return true;
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
        return IsUpdateValid(source, destination, status, false);
    }

    public final boolean IsUpdateValid(Object source, Object destination, Enum<?> status, boolean hotsetEnabled) {
        if (source.getClass() != destination.getClass()) {
            return false;
        }
        for (String fieldName : GetChangedFields(source, destination)) {
            if (!IsFieldUpdatable(status, fieldName, null, hotsetEnabled)) {
                log.warn(String.format("ObjectIdentityChecker.IsUpdateValid:: Not updatable field '%1$s' was updated",
                        fieldName));
                return false;
            }
        }
        return true;
    }

    public final List<String> getChangedFieldsForStatus(Object source, Object destination, Enum<?> status) {
        List<String> fields = new ArrayList<>();
        if (source.getClass() != destination.getClass()) {
            return fields;
        }
        for (String fieldName : GetChangedFields(source, destination)) {
            if (!IsFieldUpdatable(status, fieldName, null, false)) {
                fields.add(fieldName);
            }
        }
        return fields;
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
            List<PropertyInfo> properties = TypeCompat.getProperties(objectType);
            for (PropertyInfo property : properties) {
                Object sourceValue = property.getValue(source, null);
                Object destinationValue = property.getValue(destination, null);

                if (property.getCanWrite()
                        && sourceValue != null && !sourceValue.equals(destinationValue)
                        || ((sourceValue == null && destinationValue != null))) {
                    returnValue.add(property.getName());
                }
            }
        }
        return returnValue;
    }

    private static final Log log = LogFactory.getLog(ObjectIdentityChecker.class);
}
