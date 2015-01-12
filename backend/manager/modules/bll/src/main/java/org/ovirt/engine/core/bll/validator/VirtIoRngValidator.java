package org.ovirt.engine.core.bll.validator;


import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Version;

public class VirtIoRngValidator {

    public ValidationResult canAddRngDevice(VDSGroup cluster, VmRngDevice rngDevice) {
        VmRngDevice.Source source = rngDevice.getSource();
        boolean supported = cluster != null &&
                isFeatureSupported(cluster.getcompatibility_version()) &&
                cluster.getRequiredRngSources().contains(source);

        if (!supported) {
            return new ValidationResult(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
        }

        return ValidationResult.VALID;

    }

    protected boolean isFeatureSupported(Version clusterVersion) {
        return FeatureSupported.virtIoRngSupported(clusterVersion);
    }
}
