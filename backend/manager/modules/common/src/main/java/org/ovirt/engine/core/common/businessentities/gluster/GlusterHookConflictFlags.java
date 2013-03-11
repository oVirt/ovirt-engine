package org.ovirt.engine.core.common.businessentities.gluster;

public enum GlusterHookConflictFlags {
    CONTENT_CONFLICT (1), //0b001
    STATUS_CONFLICT(2), //0b010
    MISSING_HOOK (4); //0b100

    private Integer flag;

    private GlusterHookConflictFlags(Integer value) {
        this.flag = value;
    }

    public Integer getValue() {
        return flag;
    }

 }
