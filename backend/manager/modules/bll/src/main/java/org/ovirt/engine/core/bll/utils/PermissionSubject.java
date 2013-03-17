package org.ovirt.engine.core.bll.utils;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class PermissionSubject {
    private Guid objectId;
    private VdcObjectType ObjectType;
    private ActionGroup actionGroup;
    private VdcBllMessages message;

    public PermissionSubject(Guid objectId, VdcObjectType objectType, ActionGroup actionGroup) {
        this(objectId, objectType, actionGroup, VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
    }

    public PermissionSubject(Guid objectId, VdcObjectType objectType, ActionGroup actionGroup,
            VdcBllMessages message) {
        this.objectId = objectId;
        this.ObjectType = objectType;
        this.actionGroup = actionGroup;
        this.message = message;
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

    public VdcBllMessages getMessage(){
        return message;
    }
}
