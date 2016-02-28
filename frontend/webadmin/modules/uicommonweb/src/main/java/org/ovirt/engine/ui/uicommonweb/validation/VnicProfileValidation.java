package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VnicProfileValidation implements IValidation {

    private VmInterfaceType vnicType;

    public VnicProfileValidation(VmInterfaceType vnicType) {
        this.vnicType = vnicType;
    }

    @Override
    public ValidationResult validate(Object value) {
        VnicProfileView profile = (VnicProfileView) value;

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
