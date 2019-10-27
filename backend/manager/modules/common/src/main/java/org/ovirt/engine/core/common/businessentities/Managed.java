package org.ovirt.engine.core.common.businessentities;

/**
 * Indicates if the business entity is managed by ovirt-engine or is it being managed by an external provider.
*/
public interface Managed {

    default boolean isManaged() {
        return true;
    }
}
