package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class HostWithProtocolAndPortAddressValidation extends BaseI18NValidation {
    public HostWithProtocolAndPortAddressValidation() {
        super(ConstantsManager.getInstance().getConstants().portHostnameOrIpPort());
    }

    HostWithProtocolAndPortAddressValidation(String message) {
        super(message);
    }

    @Override
    protected String composeRegex() {
        return start() + protocol() + ValidationUtils.HOSTNAME_FOR_URI + port() + end();
    }

    private String protocol() {
        return "([" + asciiLetters() + "]+://)?"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String port() {
        return "(:[0-9]{1,5})?"; //$NON-NLS-1$
    }
}
