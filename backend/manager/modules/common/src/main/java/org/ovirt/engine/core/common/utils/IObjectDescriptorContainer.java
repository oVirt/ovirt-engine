package org.ovirt.engine.core.common.utils;

public interface IObjectDescriptorContainer {
    boolean CanUpdateField(Object obj, String fieldName, Enum<?> status);
}
