package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ChangeQuotaParameters extends VdcActionParametersBase {

    private static final long serialVersionUID = -6356486086345390742L;

    private Guid storagePoolId;
    private Guid containerId;
    private Guid quotaId;
    private Guid objectId;

    /**
     * @param quotaId the new quota id
     * @param entityId vm or disk
     * @param containerId in case of vm it's cluster, in case of disk it's storage
     * @param storagePoolId
     */
    public ChangeQuotaParameters(Guid quotaId, Guid entityId, Guid containerId, Guid storagePoolId){
        this.quotaId = quotaId;
        this.objectId = entityId;
        this.containerId = containerId;
        this.storagePoolId = storagePoolId;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public Guid getObjectId() {
        return objectId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public Guid getContainerId(){
        return containerId;
    }

}
