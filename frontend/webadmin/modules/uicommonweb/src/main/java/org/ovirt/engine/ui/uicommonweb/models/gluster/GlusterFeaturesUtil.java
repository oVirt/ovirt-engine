package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.compat.Version;

public class GlusterFeaturesUtil {

    public static boolean isGlusterSwiftSupported(Version version) {
        return false;
    }

    public static boolean isGlusterBrickProvisioningSupported(Version version) {
        return Version.v3_5.compareTo(version) <= 0;
    }

    public static boolean isStopGlusterProcessesSupported(Version version) {
        return Version.v3_5.lessOrEquals(version);
    }
}
