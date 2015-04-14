package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VnicProfileValidation implements IValidation {

    private String clusterCompatabilityVersion;
    private VmInterfaceType vnicType;

    public VnicProfileValidation(String clusterCompatabilityVersion, VmInterfaceType vnicType) {
        this.clusterCompatabilityVersion = clusterCompatabilityVersion;
        this.vnicType = vnicType;
    }

    @Override
    public ValidationResult validate(Object value) {
        VnicProfileView profile = (VnicProfileView) value;

        boolean isPassthroughSupported =
                (Boolean) AsyncDataProvider.getInstance()
                        .getConfigValuePreConverted(ConfigurationValues.NetworkSriovSupported,
                                clusterCompatabilityVersion);

        if (profile.isPassthrough() && !isPassthroughSupported) {
            return ValidationResult.fail(ConstantsManager.getInstance()
                    .getMessages()
                    .passthroughNotSupported(clusterCompatabilityVersion));
        }

        if (VmInterfaceType.pciPassthrough.equals(vnicType) && !profile.isPassthrough()) {
            return ValidationResult.fail(ConstantsManager.getInstance()
                    .getMessages()
                    .vnicTypeDoesntMatchNonPassthroughProfile(vnicType.getDescription()));
        }

        if (!VmInterfaceType.pciPassthrough.equals(vnicType) && profile.isPassthrough()) {
            return ValidationResult.fail(ConstantsManager.getInstance()
                    .getMessages()
                    .vnicTypeDoesntMatchPassthroughProfile(vnicType.getDescription()));
        }

        return ValidationResult.ok();
    }

}
