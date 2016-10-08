package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;

public class UriHostAddressValidation extends HostAddressValidation {
    public UriHostAddressValidation() {
    }

    UriHostAddressValidation(String message) {
        super(message);
    }

    @Override
    protected String getIpv6Pattern() {
        return ValidationUtils.IPV6_FOR_URI;
    }
}
