package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.compat.Version;

public class GlusterFeaturesUtil {

    public static boolean glusterArbiterVolumeSupported(Version version) {
        return Version.v4_1.compareTo(version) <= 0;
    }
}
