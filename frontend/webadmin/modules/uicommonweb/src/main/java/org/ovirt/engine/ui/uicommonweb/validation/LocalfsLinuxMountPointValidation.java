package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class LocalfsLinuxMountPointValidation extends BaseI18NValidation {

    public LocalfsLinuxMountPointValidation() {
        super(ConstantsManager.getInstance().getConstants().localfsMountPashIsIllegalMsg());
    }

    @Override
    protected String composeRegex() {
        return start() + path() + end();
    }

    protected String path() {
        return "/(.*?/|.*?\\\\)?([^\\./|^\\.\\\\]+)(?:\\.([^\\\\]*)|)"; //$NON-NLS-1$
    }
}
