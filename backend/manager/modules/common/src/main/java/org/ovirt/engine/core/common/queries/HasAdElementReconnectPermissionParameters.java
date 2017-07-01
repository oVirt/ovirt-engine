package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class HasAdElementReconnectPermissionParameters extends QueryParametersBase {

    private static final long serialVersionUID = -2361449851899615454L;

    private Guid adElementId;
    private Guid objectId;

    public HasAdElementReconnectPermissionParameters() {
    }

    public HasAdElementReconnectPermissionParameters(Guid adElementId, Guid objectId) {
        setAdElementId(adElementId);
        setObjectId(objectId);
    }

    public Guid getAdElementId() {
        return adElementId;
    }

    public void setAdElementId(Guid adElementId) {
        this.adElementId = adElementId;
    }

    public Guid getObjectId() {
        return objectId;
    }

    public void setObjectId(Guid objectId) {
        this.objectId = objectId;
    }

}
