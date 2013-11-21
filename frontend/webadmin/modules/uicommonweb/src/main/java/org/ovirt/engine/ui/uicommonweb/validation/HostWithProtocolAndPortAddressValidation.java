package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class HostWithProtocolAndPortAddressValidation extends HostAddressValidation {
    @Override
    protected String composeRegex() {
        return start() + protocol() + hostnameOrIp() + port() + end();
    }

    private String protocol() {
        return "([" + asciiLetters() + "]+://)?"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String port() {
        return "(:[0-9]{1,5})?"; //$NON-NLS-1$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().portHostnameOrIpPort();
    }
}
