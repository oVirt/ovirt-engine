package org.ovirt.engine.core.common;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

public class PermissionSubject {
    private Guid objectId;
    private VdcObjectType ObjectType;
    private ActionGroup actionGroup;

    public PermissionSubject(Guid objectId, VdcObjectType objectType, ActionGroup actionGroup) {
        super();
        this.objectId = objectId;
        this.ObjectType = objectType;
        this.actionGroup = actionGroup;
    }

    public VdcObjectType getObjectType() {
        return ObjectType;
    }

    public void setObjectType(VdcObjectType ObjectType) {
        this.ObjectType = ObjectType;
    }

    public Guid getObjectId() {
        return objectId;
    }

    public void setObjectId(Guid ObjectId) {
        this.objectId = ObjectId;
    }

    public ActionGroup getActionGroup() {
        return actionGroup;
    }

    public void setActionGroup(ActionGroup actionGroup) {
        this.actionGroup = actionGroup;
    }
}
