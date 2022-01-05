package org.ovirt.engine.ui.uicommonweb.models.configure;

import org.ovirt.engine.ui.uicommonweb.ViewFilter;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public enum PermissionFilter implements ViewFilter<PermissionFilter> {

    ALL_PERMISSIONS(ConstantsManager.getInstance().getConstants().allPermissions()) {
    },

    DIRECT_PERMISSIONS(ConstantsManager.getInstance().getConstants().directPermissions()) {
    };

    private String text;

    PermissionFilter(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public PermissionFilter getValue() {
        return this;
    }

}
