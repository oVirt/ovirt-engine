package org.ovirt.engine.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.utils.IObjectDescriptorContainer;
import org.ovirt.engine.core.compat.backendcompat.PropertyInfo;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectIdentityChecker {
    private static final Logger log = LoggerFactory.getLogger(ObjectIdentityChecker.class);

    private IObjectDescriptorContainer container;
    private static Map<String, Class<?>> aliases = new HashMap<>();
    private static Map<Class<?>, ObjectIdentityChecker> identities = new HashMap<>();
    private Map<Enum<?>, Set<String>> dictionary = new HashMap<>();
    private Set<String> permitted = new HashSet<>();
    private Map<String, EnumSet<VMStatus>> hotSettableFieldsInStates = new HashMap<>();
    private Set<String> transientFields = new HashSet<>();
    private Set<String> permittedForHostedEngine = new HashSet<>();

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

    public static boolean canUpdateField(Object fieldContainer, String fieldName, Enum<?> status) {
        return canUpdateField(fieldContainer.getClass().getSimpleName(), fieldName, status, fieldContainer);
    }

    public static boolean canUpdateField(String objectType, String fieldName, Enum<?> status, Object fieldContainer) {
        Class<?> type = aliases.get(objectType);
        if (type != null) {
            return canUpdateField(type, fieldName, status, fieldContainer);
        } else {
            throw new RuntimeException("status type [null] not exist");
        }
    }

    public static boolean canUpdateField(Class<?> objectType, String fieldName, Enum<?> status,
            Object fieldContainer) {
        ObjectIdentityChecker checker = identities.get(objectType);
        if (checker != null) {
            return checker.isFieldUpdatable(status, fieldName, fieldContainer);
        }
        return true;
    }

    public final <T extends Enum<T>> void addField(T status, String fieldName) {
        Set<String> values = dictionary.get(status);
        if (values == null) {
            values = new HashSet<>();
            dictionary.put(status, values);
        }
        values.add(fieldName);
    }

    public final <T extends Enum<T>> void addField(Iterable<T> statuses, String fieldName) {
        for (T status : statuses) {
            addField(status, fieldName);
        }
    }

    public final void addHostedEngineFields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            permittedForHostedEngine.add(fieldName);
        }
    }

    public final void addPermittedFields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            permitted.add(fieldName);
        }
    }

    public final void addHotsetField(String fieldName, EnumSet<VMStatus> statuses) {
        hotSettableFieldsInStates.put(fieldName, statuses);
    }

    public final void addTransientFields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            transientFields.add(fieldName);
        }
    }

    public final boolean isFieldUpdatable(String name) {
        return permitted.contains(name);
    }

    public final boolean isHostedEngineFieldUpdatable(String name) {
        return permittedForHostedEngine.contains(name);
    }

    public final boolean isFieldHotSettableInStatus(String fieldName, VMStatus status) {
        final EnumSet<VMStatus> hotSettableStatuses = hotSettableFieldsInStates.get(fieldName);
        if (hotSettableStatuses == null) {
            return false;
        }
        return hotSettableStatuses.contains(status);
    }

    public final boolean isTransientField(String name) {
        return transientFields.contains(name);
    }

    public boolean isFieldUpdatable(Enum<?> status, String name, Object fieldContainer) {
        return isFieldUpdatable(status, name, fieldContainer, false);
    }

    public boolean isFieldUpdatable(Enum<?> status, String name, Object fieldContainer, boolean hotsetEnabled) {
        boolean returnValue = true;
        if (!isFieldUpdatable(name)) {
            if (fieldContainer != null && container != null
                    && !container.canUpdateField(fieldContainer, name, status)) {
                returnValue = false;
            } else {
                Set<String> values = dictionary.get(status);
                returnValue = values != null ? values.contains(name) : false;

                // if field is not updatable in this status, check if it's hotset request and an hotset allowed field
                if (!returnValue && hotsetEnabled) {
                    returnValue = status instanceof VMStatus && isFieldHotSettableInStatus(name, (VMStatus) status);
                }
            }
            if (!returnValue) {
                log.warn("Field '{}' can not be updated when status is '{}'", name, status);
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
    public boolean copyNonEditableFieldsToDestination(
            Object source, Object destination, boolean hotSetEnabled, VMStatus vmStatus) {
        Class<?> cls = source.getClass();
        while (!cls.equals(Object.class)) {
            for (Field srcFld : cls.getDeclaredFields()) {
                try {
                    // copy fields that are non final, and not-editable and not a hotset field or it is but this is not hotset case
                    if (!Modifier.isFinal(srcFld.getModifiers()) &&
                            !isFieldUpdatable(srcFld.getName()) &&
                            (!isFieldHotSettableInStatus(srcFld.getName(), vmStatus) || !hotSetEnabled)) {
                        srcFld.setAccessible(true);

                        Field dstFld = cls.getDeclaredField(srcFld.getName());
                        dstFld.setAccessible(true);
                        dstFld.set(destination, srcFld.get(source));
                    }
                } catch (Exception exp) {
                    log.error("Failed to copy non editable field '{}', error: {}",
                            srcFld.getName(),
                            exp.getMessage());
                    log.debug("Exception", exp);
                    return false;
                }
            }
            cls = cls.getSuperclass();
        }
        return true;
    }

    public final boolean isUpdateValid(Object source, Object destination) {
        if (source.getClass() != destination.getClass()) {
            return false;
        }
        for (String fieldName : getChangedFields(source, destination)) {
            if (!isFieldUpdatable(fieldName)) {
                return false;
            }
        }
        return true;
    }

    public final boolean isHostedEngineUpdateValid(Object source, Object destination) {
        if (source.getClass() != destination.getClass()) {
            return false;
        }
        for (String fieldName : getChangedFields(source, destination)) {
            if (!isHostedEngineFieldUpdatable(fieldName)) {
                return false;
            }
        }
        return true;
    }

    public final boolean isUpdateValid(Object source, Object destination, Enum<?> status) {
        return isUpdateValid(source, destination, status, false);
    }

    public final boolean isUpdateValid(Object source, Object destination, Enum<?> status, boolean hotsetEnabled) {
        if (source.getClass() != destination.getClass()) {
            return false;
        }
        for (String fieldName : getChangedFields(source, destination)) {
            if (!isFieldUpdatable(status, fieldName, null, hotsetEnabled) && !isTransientField(fieldName)) {
                log.warn("ObjectIdentityChecker.isUpdateValid:: Not updatable field '{}' was updated",
                        fieldName);
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
        for (String fieldName : getChangedFields(source, destination)) {
            if (!isFieldUpdatable(status, fieldName, null, false) && !isTransientField(fieldName)) {
                fields.add(fieldName);
            }
        }
        return fields;
    }

    public final boolean isFieldsUpdated(Object source, Object destination, Iterable<String> fields) {
        List<String> changedFields = getChangedFields(source, destination);
        for (String field : fields) {
            if (changedFields.contains(field)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getChangedFields(Object source, Object destination) {
        final List<String> returnValue = new ArrayList<>();
        if (source.getClass().isInstance(destination)) {
            Class<?> objectType = source.getClass();
            List<PropertyInfo> properties = TypeCompat.getProperties(objectType);
            for (PropertyInfo property : properties) {
                Object sourceValue = property.getValue(source, null);
                Object destinationValue = property.getValue(destination, null);

                if (property.getCanWrite()&& !Objects.equals(sourceValue, destinationValue)) {
                    returnValue.add(property.getName());
                }
            }
        }
        return returnValue;
    }
}
