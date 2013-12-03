package org.ovirt.engine.core.common.utils;

public interface IObjectDescriptorContainer {
    boolean canUpdateField(Object obj, String fieldName, Enum<?> status);
}
