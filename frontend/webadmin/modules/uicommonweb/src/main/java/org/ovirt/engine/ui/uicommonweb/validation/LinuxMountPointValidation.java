package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class LinuxMountPointValidation extends HostAddressValidation {

    @Override
    protected String composeRegex() {
        return start() + hostnameOrIp() + path() + end();
    }

    protected String path() {
        return "\\:/(.*?/|.*?\\\\)?([^\\./|^\\.\\\\]+)(?:\\.([^\\\\]*)|)"; //$NON-NLS-1$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().nfsMountPashIsIllegalMsg();
    }

}
