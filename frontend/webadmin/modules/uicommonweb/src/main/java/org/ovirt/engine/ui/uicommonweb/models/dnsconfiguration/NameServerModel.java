package org.ovirt.engine.ui.uicommonweb.models.dnsconfiguration;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.Ipv4OrIpv6AddressValidation;

public class NameServerModel extends EntityModel<String> {

    public NameServerModel() {
        this("");
    }

    public NameServerModel(String address) {
        setEntity(address == null ? "" : address);
    }

    public String flush() {
        return getEntity();
    }

    public boolean validate() {
        validateEntity(new IValidation[] { new Ipv4OrIpv6AddressValidation() });
        return getIsValid();
    }

}
