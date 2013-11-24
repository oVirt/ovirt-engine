package org.ovirt.engine.core.common.businessentities;


/**
 * This enum is used for determining the entity type of the VM
 */
public enum VmEntityType {
    VM,
    TEMPLATE,
    INSTANCE_TYPE,
    IMAGE_TYPE;

    /**
     * Check if entity type is of a VM
     * @return true if entity type is of a VM
     */
    public boolean isVmType() {
        return this == VM;
    }

    /**
     * Check if entity type is of a Template
     * @return true if entity type is of a Template
     */
    public boolean isTemplateType() {
        return this == TEMPLATE ||
                this == INSTANCE_TYPE ||
                this == IMAGE_TYPE;
    }
}
