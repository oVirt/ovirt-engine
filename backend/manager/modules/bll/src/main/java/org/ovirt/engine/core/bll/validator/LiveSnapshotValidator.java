package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;

public class LiveSnapshotValidator {

    private Version compatibilityVersion;
    private VDS vds;

    public LiveSnapshotValidator(Version compatibilityVersion, VDS vds) {
        this.compatibilityVersion = compatibilityVersion;
        this.vds = vds;
    }

    public ValidationResult validateSnapshot() {
        if (!isLiveSnapshotEnabled(compatibilityVersion)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DATA_CENTER_VERSION_DOESNT_SUPPORT_LIVE_SNAPSHOT);
        }

        // it is possible, even if unlikely, that the QEMU on the host does not support live snapshotting
        if (vds != null && !vds.getLiveSnapshotSupport()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QEMU_UNSUPPORTED_OPERATION);
        }
        return ValidationResult.VALID;
    }

    /**
     * @return If DC level does not support live snapshots.
     */
    private static boolean isLiveSnapshotEnabled(Version version) {
        return Config.<Boolean> getValue(ConfigValues.LiveSnapshotEnabled, version.getValue());
    }
}
