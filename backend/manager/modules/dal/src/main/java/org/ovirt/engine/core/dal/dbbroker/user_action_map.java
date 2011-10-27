package org.ovirt.engine.core.dal.dbbroker;

import org.ovirt.engine.core.compat.*;

/**
 * Defines an object that holds a mapping between a user, action, and tag In some cases tagIdField may be null for
 * user_action_maps objects returned from DBFacade (for example - DbFacade.GetActionsByUserId ), therefore the NGuid
 * (nullable Guid) type is used for tagIDField
 */
public class user_action_map {

    private int actionIdField;
    private NGuid tagIdField;
    private Guid userIdField = new Guid();

    public user_action_map() {
    }

    public user_action_map(int actionId, NGuid tagId, Guid userId) {
        this.actionIdField = actionId;
        this.tagIdField = tagId;
        this.userIdField = userId;
    }

    public int getactionId() {
        return this.actionIdField;
    }

    public void setactionId(int value) {
        this.actionIdField = value;
    }

    public void setaction_id(int value) {
        this.setactionId(value);
    }

    public NGuid gettagId() {
        return this.tagIdField;
    }

    public void settagId(NGuid value) {
        this.tagIdField = value;
    }

    public void settag_id(NGuid value) {
        this.settagId(value);
    }

    public Guid getuserId() {
        return this.userIdField;
    }

    public void setuserId(Guid value) {
        this.userIdField = value;
    }

    public void setuser_id(Guid value) {
        this.setuserId(value);
    }
}
