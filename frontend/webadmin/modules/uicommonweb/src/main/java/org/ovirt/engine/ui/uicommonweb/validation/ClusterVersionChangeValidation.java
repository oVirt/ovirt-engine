package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ClusterVersionChangeValidation implements IValidation {
    private boolean hasActiveVm;
    private Version oldClusterVersion;

    public ClusterVersionChangeValidation(boolean hasActiveVm, Version oldClusterVersion) {
        this.hasActiveVm = hasActiveVm;
        this.oldClusterVersion = oldClusterVersion;
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();
        Version newClusterVersion = (Version) value;

        if (value == null || (hasActiveVm && !newClusterVersion.equals(oldClusterVersion))) {
            result.setSuccess(false);
            result.getReasons().add(ConstantsManager.getInstance().getConstants().cannotClusterVersionChangeWithActiveVm());
        }

        return result;
    }
}
