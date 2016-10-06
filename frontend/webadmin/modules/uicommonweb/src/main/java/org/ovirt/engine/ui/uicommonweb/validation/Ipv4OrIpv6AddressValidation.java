package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class Ipv4OrIpv6AddressValidation extends AlternativeValidation {

    public Ipv4OrIpv6AddressValidation() {
        super(ConstantsManager.getInstance().getConstants().thisFieldMustContainIpv4OrIpv6AddressMsg(),
                new Ipv4AddressValidation(),
                new Ipv6AddressValidation());
    }
}
