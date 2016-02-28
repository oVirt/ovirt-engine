package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class LiveSnapshotValidator {

    private VDS vds;

    public LiveSnapshotValidator(VDS vds) {
        this.vds = vds;
    }

    public ValidationResult validateSnapshot() {
        // it is possible, even if unlikely, that the QEMU on the host does not support live snapshotting
        if (vds != null && !vds.getLiveSnapshotSupport()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QEMU_UNSUPPORTED_OPERATION);
        }
        return ValidationResult.VALID;
    }
}
