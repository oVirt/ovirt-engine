package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class LinuxMountPointValidation extends BaseI18NValidation {

    @Override
    protected String composeRegex() {
        return start() + ValidationUtils.HOSTNAME_FOR_URI + path() + end();
    }

    private String path() {
        return "\\:/(.*?/|.*?\\\\)?([^\\./|^\\.\\\\]+)(?:\\.([^\\\\]*)|)"; //$NON-NLS-1$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().nfsMountPashIsIllegalMsg();
    }
}
