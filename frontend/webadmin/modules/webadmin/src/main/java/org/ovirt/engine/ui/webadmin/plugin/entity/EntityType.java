package org.ovirt.engine.ui.webadmin.plugin.entity;

/**
 * Enumerates possible entity types used within the plugin API.
 */
public enum EntityType {

    Cluster,
    DataCenter,
    Disk,
    Host,
    // TODO(vszocs) Pool missing; org.ovirt.engine.core.common.businessentities.vm_pools
    // entity doesn't implement BusinessEntity interface (switch to using IVdcQueryable?)
    Storage,
    Template,
    VirtualMachine,

    Undefined; // Null object

    public static EntityType from(String name) {
        EntityType result = EntityType.Undefined;

        try {
            result = EntityType.valueOf(name);
        } catch (IllegalArgumentException e) {
            // Do nothing
        }

        return result;
    }

}
