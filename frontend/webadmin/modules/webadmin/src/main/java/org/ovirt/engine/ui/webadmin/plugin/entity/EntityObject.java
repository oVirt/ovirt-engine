package org.ovirt.engine.ui.webadmin.plugin.entity;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsMutableObjectWithProperties;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type representing a business entity passed through plugin API as native JS object.
 */
public final class EntityObject extends JsMutableObjectWithProperties {

    protected EntityObject() {
    }

    protected static <T> EntityObject create(T businessEntity) {
        EntityObject obj = JavaScriptObject.createObject().cast();

        String entityId = ""; //$NON-NLS-1$
        if (businessEntity instanceof BusinessEntity) {
            entityId = ((BusinessEntity<?>) businessEntity).getId().toString();
        } else if (businessEntity instanceof Queryable) {
            entityId = ((Queryable) businessEntity).getQueryableId().toString();
        }
        obj.setValueAsString("id", entityId); //$NON-NLS-1$

        return obj;
    }

    // TODO(vszocs) refactor if-else & instanceof code, logic that fills
    // entity-specific properties should be tied with EntityType enum
    public static <T> EntityObject from(T businessEntity) {
        EntityObject obj = create(businessEntity);

        if (businessEntity instanceof StoragePool) {
            obj.setValueAsString("name", ((StoragePool) businessEntity).getName()); //$NON-NLS-1$
        } else if (businessEntity instanceof Cluster) {
            obj.setValueAsString("name", ((Cluster) businessEntity).getName()); //$NON-NLS-1$
            obj.setValueAsBoolean("managed", ((Cluster) businessEntity).isManaged()); //$NON-NLS-1$
        } else if (businessEntity instanceof VDS) {
            obj.setValueAsString("name", ((VDS) businessEntity).getName()); //$NON-NLS-1$
            obj.setValueAsString("hostname", ((VDS) businessEntity).getHostName()); //$NON-NLS-1$
            obj.setValueAsBoolean("managed", ((VDS) businessEntity).isManaged()); //$NON-NLS-1$
        } else if (businessEntity instanceof Network) {
            obj.setValueAsString("name", ((Network) businessEntity).getName()); //$NON-NLS-1$
        } else if (businessEntity instanceof StorageDomain) {
            obj.setValueAsString("name", ((StorageDomain) businessEntity).getStorageName()); //$NON-NLS-1$
        } else if (businessEntity instanceof Disk) {
            // No custom properties for now
        } else if (businessEntity instanceof VM) {
            obj.setValueAsString("name", ((VM) businessEntity).getName()); //$NON-NLS-1$
            obj.setValueAsString("ipaddress", ((VM) businessEntity).getIp()); //$NON-NLS-1$
            obj.setValueAsString("status", ((VM) businessEntity).getStatus().name()); //$NON-NLS-1$
            obj.setValueAsBoolean("managed", ((VM) businessEntity).isManaged()); //$NON-NLS-1$
        } else if (businessEntity instanceof VmPool) {
            obj.setValueAsString("name", ((VmPool) businessEntity).getName()); //$NON-NLS-1$
        } else if (businessEntity instanceof VmTemplate) {
            obj.setValueAsString("name", ((VmTemplate) businessEntity).getName()); //$NON-NLS-1$
        } else if (businessEntity instanceof GlusterVolumeEntity) {
            obj.setValueAsString("name", ((GlusterVolumeEntity) businessEntity).getName()); //$NON-NLS-1$
            obj.setValueAsString("clusterId", ((GlusterVolumeEntity) businessEntity).getClusterId().toString());//$NON-NLS-1$
        } else if (businessEntity instanceof Provider) {
            obj.setValueAsString("name", ((Provider) businessEntity).getName()); //$NON-NLS-1$
        } else if (businessEntity instanceof DbUser) {
            obj.setValueAsString("username", ((DbUser) businessEntity).getLoginName()); //$NON-NLS-1$
            obj.setValueAsString("domain", ((DbUser) businessEntity).getDomain()); //$NON-NLS-1$
        } else if (businessEntity instanceof Quota) {
            obj.setValueAsString("name", ((Quota) businessEntity).getQuotaName()); //$NON-NLS-1$
        } else if (businessEntity instanceof AuditLog) {
            obj.setValueAsString("correlationId", ((AuditLog) businessEntity).getCorrelationId()); //$NON-NLS-1$
            obj.setValueAsString("message", ((AuditLog) businessEntity).getMessage()); //$NON-NLS-1$
            obj.setValueAsString("callStack", ((AuditLog) businessEntity).getCallStack()); //$NON-NLS-1$
            obj.setValueAsString("customEventId", String.valueOf(((AuditLog) businessEntity).getCustomEventId())); //$NON-NLS-1$
            obj.setValueAsString("toString", ((AuditLog) businessEntity).toStringForLogging()); //$NON-NLS-1$
        }

        return obj;
    }

    public static <T> JsArray<EntityObject> arrayFrom(List<T> businessEntityList) {
        List<T> nullSafeList = businessEntityList != null ? businessEntityList : new ArrayList<>();
        JsArray<EntityObject> result = JavaScriptObject.createArray().cast();

        for (T businessEntity : nullSafeList) {
            result.push(EntityObject.from(businessEntity));
        }

        return result;
    }

}
