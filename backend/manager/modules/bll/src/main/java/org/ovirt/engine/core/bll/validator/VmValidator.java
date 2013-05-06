package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/** A Validator for various VM canDoAction needs */
public class VmValidator {
    private VM vm;

    public VmValidator(VM vm) {
        this.vm = vm;
    }

    /** @return Validation result that indicates if the VM is during migration or not. */
    public ValidationResult vmNotDuringMigration() {
        if (vm.getStatus() == VMStatus.MigratingFrom || vm.getStatus() == VMStatus.MigratingTo) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS);
        }

        return ValidationResult.VALID;
    }

    /** @return Validation result that indicates if the VM is down or not. */
    public ValidationResult vmDown() {
        if (!vm.isDown()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotLocked() {
        if (vm.getStatus() == VMStatus.ImageLocked) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_LOCKED);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotSavingRestoring() {
        if (vm.getStatus().isHibernating() || vm.getStatus() == VMStatus.RestoringState) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotIlegal() {
        if (vm.getStatus() == VMStatus.ImageIllegal) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotRunningStateless() {
        if (DbFacade.getInstance().getSnapshotDao().exists(vm.getId(), SnapshotType.STATELESS)) {
            VdcBllMessages message = vm.isRunning() ? VdcBllMessages.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS :
                    VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_STATELESS_SNAPSHOT_LEFTOVER;
            return new ValidationResult(message);
        }

        return ValidationResult.VALID;
    }
}
