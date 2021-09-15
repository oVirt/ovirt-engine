package org.ovirt.engine.core.bll;

import javax.enterprise.context.RequestScoped;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RequestScoped
public class WindowsVmClusterEditChecker extends AbstractWindowsVmClusterEditChecker {

    @Override
    public boolean check(VM vm) {
        return isLinux(vm) || hasCustomCompatibilityVersion(vm) || hasCustomEmulatedMachine(vm);
    }

    @Override
    public String getMainMessage() {
        return EngineMessage.CLUSTER_WARN_VM_DUE_TO_EMULATED_MACHINE_CHANGE_ON_WINDOWS.name();
    }

    @Override
    public String getDetailMessage(VM vm) {
        return osRepository.getOsName(vm.getOs());
    }
}
