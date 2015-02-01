package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import java.util.Collection;

import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class SharedHostNetworkQosParametersModel extends HostNetworkQosParametersModel {

    @Override
    protected Collection<IValidation> getOutLinkshareValidations() {
        Collection<IValidation> validations = super.getOutLinkshareValidations();
        validations.add(new NotEmptyValidation());
        return validations;
    }

}
