package org.ovirt.engine.core.bll.utils;

import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class PermissionSubject {
    private Guid objectId;
    private VdcObjectType ObjectType;
    private ActionGroup actionGroup;
    private EngineMessage message;

    public PermissionSubject(Guid objectId, VdcObjectType objectType, ActionGroup actionGroup) {
        this(objectId, objectType, actionGroup, EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
    }

    public PermissionSubject(Guid objectId, VdcObjectType objectType, ActionGroup actionGroup,
            EngineMessage message) {
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

    public EngineMessage getMessage(){
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof PermissionSubject) {
            PermissionSubject other = (PermissionSubject) o;
            return Objects.equals(other.objectId, objectId) &&
                    Objects.equals(other.ObjectType, ObjectType) &&
                    Objects.equals(other.actionGroup, actionGroup);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, ObjectType, actionGroup);
    }
}
