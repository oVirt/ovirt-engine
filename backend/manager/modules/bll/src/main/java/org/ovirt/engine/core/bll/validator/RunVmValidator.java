package org.ovirt.engine.core.bll.validator;

import java.util.List;

import org.ovirt.engine.core.bll.IsoDomainListSyncronizer;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
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

    public ValidationResult validateBootSequence(VM vm, BootSequence bootSequence, List<Disk> vmDisks) {
        BootSequence boot_sequence = (bootSequence != null) ?
                bootSequence : vm.getDefaultBootSequence();
        Guid storagePoolId = vm.getStoragePoolId();
        // Block from running a VM with no HDD when its first boot device is
        // HD and no other boot devices are configured
        if (boot_sequence == BootSequence.C && vmDisks.size() == 0) {
            return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK);
        } else {
            // If CD appears as first and there is no ISO in storage
            // pool/ISO inactive -
            // you cannot run this VM

            if (boot_sequence == BootSequence.CD
                    && getIsoDomainListSyncronizer().findActiveISODomain(storagePoolId) == null) {
                return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
            } else {
                // if there is network in the boot sequence, check that the
                // vm has network,
                // otherwise the vm cannot be run in vdsm
                if (boot_sequence == BootSequence.N
                        && getVmNetworkInterfaceDao().getAllForVm(vm.getId()).size() == 0) {
                    return new ValidationResult(VdcBllMessages.VM_CANNOT_RUN_FROM_NETWORK_WITHOUT_NETWORK);
                }
            }
        }
        return ValidationResult.VALID;

    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected IsoDomainListSyncronizer getIsoDomainListSyncronizer() {
        return IsoDomainListSyncronizer.getInstance();
    }

    protected VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    // Compatibility method for static VmPoolCommandBase.canRunPoolVm
    // who uses the same validation as runVmCommand
    public boolean canRunVm(VM vm, List<String> messages, List<Disk> vmDisks, BootSequence bootSequence) {
        if (!validateVmProperties(vm, messages)) {
            return false;
        }
        ValidationResult result = validateBootSequence(vm, bootSequence, vmDisks);
        if (!result.isValid()) {
            messages.add(result.getMessage().toString());
            return false;
        }
        result = new VmValidator(vm).vmNotLocked();
        if (!result.isValid()) {
            messages.add(result.getMessage().toString());
            return false;
        }
        result = new SnapshotsValidator().vmNotDuringSnapshot(vm.getId());
        if (!result.isValid()) {
            messages.add(result.getMessage().toString());
            return false;
        }

        return true;
    }

}
