package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class LocalfsLinuxMountPointValidation extends BaseI18NValidation {

    @Override
    protected String composeRegex() {
        return start() + path() + end();
    }

    protected String path() {
        return "/(.*?/|.*?\\\\)?([^\\./|^\\.\\\\]+)(?:\\.([^\\\\]*)|)"; //$NON-NLS-1$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().localfsMountPashIsIllegalMsg();
    }

}
