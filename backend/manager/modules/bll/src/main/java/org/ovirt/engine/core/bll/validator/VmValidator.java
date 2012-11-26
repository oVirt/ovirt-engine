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

    public ValidationResult vmNotRunningStateless() {
        if (DbFacade.getInstance().getSnapshotDao().exists(vm.getId(), SnapshotType.STATELESS)) {
            VdcBllMessages message = vm.isStatusUp() ? VdcBllMessages.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS :
                    VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_STATELESS_SNAPSHOT_LEFTOVER;
            return new ValidationResult(message);
        }

        return ValidationResult.VALID;
    }
}
