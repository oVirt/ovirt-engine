package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;

public class UriHostAddressValidation extends HostAddressValidation {
    @Override
    protected String getIpv6Pattern() {
        return ValidationUtils.IPV6_FOR_URI;
    }
}
