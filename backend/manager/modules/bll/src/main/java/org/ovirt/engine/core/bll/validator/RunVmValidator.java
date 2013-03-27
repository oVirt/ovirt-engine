package org.ovirt.engine.core.bll.validator;

import java.util.List;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;

public class RunVmValidator {

    public boolean validateVmProperties(VM vm, List<String> messages) {
        List<VmPropertiesUtils.ValidationError> validationErrors =
                getVmPropertiesUtils().validateVMProperties(
                vm.getVdsGroupCompatibilityVersion(),
                vm.getStaticData());

        if (!validationErrors.isEmpty()) {
            VmHandler.handleCustomPropertiesError(validationErrors, messages);
            return false;
        }

        return true;
    }

    protected VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    // Compatibility method for static VmPoolCommandBase.canRunPoolVm
    // who uses the same validation as runVmCommand
    public boolean canRunVm(VM vm, List<String> messages) {
        return validateVmProperties(vm, messages);
    }

}
