package org.ovirt.engine.core.common.businessentities;

/**
 * this interface will streamline all identified enum members where ordinal value will not be used (relying on ordinal
 * value is considered anti-pattern) for identifiable entities which are not enums consider using {@link BusinessEntity}
 */
public interface Identifiable {

    /**
     * @return the unique ID of the of the enum member
     */
    int getValue();
}
