package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class HostManagementNetworkModel extends HostInterfaceModel {

    public HostManagementNetworkModel() {
        this(false);
    }

    @Override
    public Network getEntity() {
        return (Network) super.getEntity();
    }

    public void setEntity(Network value) {
        super.setEntity(value);
    }

    public HostManagementNetworkModel(boolean compactMode) {
        super(compactMode);

        getCheckConnectivity().setEntity(true);

        getNetwork().setIsAvailable(false);
        getInterface().setIsAvailable(true);
        getGateway().setIsAvailable(true);
    }

    @Override
    public boolean validate() {
        getInterface().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return super.validate() && getInterface().getIsValid();
    }
}
