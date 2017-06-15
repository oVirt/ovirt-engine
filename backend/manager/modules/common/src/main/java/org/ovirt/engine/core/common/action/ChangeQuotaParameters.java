package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ChangeQuotaParameters extends ActionParametersBase {

    private static final long serialVersionUID = -6136710318980549386L;

    private Guid quotaId;
    private Guid objectId;
    private Guid containerId;
    private Guid storagePoolId;

    /**
     * @param quotaId the new quota id
     * @param entityId vm or disk
     * @param containerId in case of vm it's cluster, in case of disk it's storage
     */
    public ChangeQuotaParameters(Guid quotaId, Guid entityId, Guid containerId, Guid storagePoolId) {
        this.quotaId = quotaId;
        this.objectId = entityId;
        this.containerId = containerId;
        this.storagePoolId = storagePoolId;
    }

    public ChangeQuotaParameters() {
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public Guid getObjectId() {
        return objectId;
    }

    public void setObjectId(Guid objectId) {
        this.objectId = objectId;
    }

    public Guid getContainerId() {
        return containerId;
    }

    public void setContainerId(Guid containerId) {
        this.containerId = containerId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

}
