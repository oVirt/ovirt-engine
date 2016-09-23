package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class VirtIoRngValidator {

    public ValidationResult canAddRngDevice(Cluster cluster, VmRngDevice rngDevice) {
        VmRngDevice.Source source = rngDevice.getSource();
        boolean supported = cluster != null &&
                cluster.getRequiredRngSources().contains(source);

        if (!supported) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_GENERATOR_NOT_SUPPORTED_BY_CLUSTER);
        }

        return ValidationResult.VALID;

    }
}
