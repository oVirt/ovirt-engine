package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.*;

public class tags_permissions_map implements Serializable {
    private static final long serialVersionUID = -3406221445028980382L;

    public tags_permissions_map() {
    }

    public tags_permissions_map(Guid permission_id, Guid tag_id) {
        this.permission_idField = permission_id;
        this.tag_idField = tag_id;
    }

    private Guid permission_idField = new Guid();

    public Guid getpermission_id() {
        return this.permission_idField;
    }

    public void setpermission_id(Guid value) {
        this.permission_idField = value;
    }

    private Guid tag_idField;

    public Guid gettag_id() {
        return this.tag_idField;
    }

    public void settag_id(Guid value) {
        this.tag_idField = value;
    }

}
