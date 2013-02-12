package org.ovirt.engine.ui.webadmin.plugin.entity;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.NGuid;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type representing an entity passed through the plugin API as native JS object.
 */
public final class EntityObject extends JavaScriptObject {

    protected EntityObject() {
    }

    /**
     * Creates new entity object using following representation:
     * <pre>
     * { id: "[BusinessEntityGuidAsString]" }
     * </pre>
     * Note that additional properties can be {@linkplain #setProperty set} for specific entity types.
     */
    protected static native EntityObject create(BusinessEntity<? extends NGuid> businessEntity) /*-{
        var guid = businessEntity.@org.ovirt.engine.core.common.businessentities.BusinessEntity::getId()();
        var guidAsString = guid.@org.ovirt.engine.core.compat.NGuid::toString()();
        return { id: guidAsString };
    }-*/;

    protected native void setProperty(String name, String value) /*-{
        this[name] = value;
    }-*/;

    public static EntityObject from(BusinessEntity<? extends NGuid> businessEntity) {
        EntityObject obj = create(businessEntity);

        // Cluster
        if (businessEntity instanceof VDSGroup) {
            obj.setProperty("name", ((VDSGroup) businessEntity).getname()); //$NON-NLS-1$
        }

        // DataCenter
        else if (businessEntity instanceof storage_pool) {
            obj.setProperty("name", ((storage_pool) businessEntity).getname()); //$NON-NLS-1$
        }

        // Disk
        else if (businessEntity instanceof Disk) {
            // No custom properties for now
        }

        // Host
        else if (businessEntity instanceof VDS) {
            obj.setProperty("name", ((VDS) businessEntity).getName()); //$NON-NLS-1$
            obj.setProperty("hostname", ((VDS) businessEntity).getHostName()); //$NON-NLS-1$
        }

        // Storage
        else if (businessEntity instanceof StorageDomain) {
            obj.setProperty("name", ((StorageDomain) businessEntity).getStorageName()); //$NON-NLS-1$
        }

        // Template
        else if (businessEntity instanceof VmTemplate) {
            obj.setProperty("name", ((VmTemplate) businessEntity).getName()); //$NON-NLS-1$
        }

        // VirtualMachine
        else if (businessEntity instanceof VM) {
            obj.setProperty("name", ((VM) businessEntity).getName()); //$NON-NLS-1$
            obj.setProperty("ipaddress", ((VM) businessEntity).getVmIp()); //$NON-NLS-1$
        }

        return obj;
    }

    public static <T extends BusinessEntity<? extends NGuid>> JsArray<EntityObject> arrayFrom(List<T> businessEntityList) {
        JsArray<EntityObject> result = JavaScriptObject.createArray().cast();
        for (T businessEntity : businessEntityList) {
            result.push(EntityObject.from(businessEntity));
        }
        return result;
    }

}
