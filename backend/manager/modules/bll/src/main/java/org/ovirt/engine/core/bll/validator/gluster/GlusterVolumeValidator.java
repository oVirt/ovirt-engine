package org.ovirt.engine.core.bll.validator.gluster;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.compat.Version;

public class GlusterVolumeValidator {

    public ValidationResult isForceCreateVolumeAllowed(Version clusterVersion, boolean isForce) {
        if (isForce && !GlusterFeatureSupported.glusterForceCreateVolumeSupported(clusterVersion)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ADD_BRICK_FORCE_NOT_SUPPORTED);
        }
        return ValidationResult.VALID;
    }
}
