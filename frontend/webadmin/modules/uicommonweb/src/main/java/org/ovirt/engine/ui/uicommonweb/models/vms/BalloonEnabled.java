package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.ExistingBlankTemplateModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.ExistingInstanceTypeModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.NewInstanceTypeModelBehavior;

public class BalloonEnabled {

    public static boolean balloonEnabled(UnitVmModel model) {
        Cluster cluster = model.getSelectedCluster();
        Integer osType = model.getOSType().getSelectedItem();

        Boolean deviceEnabled = Boolean.TRUE.equals(model.getMemoryBalloonDeviceEnabled().getEntity());
        if (cluster == null || osType == null) {
            if (model.getBehavior() instanceof ExistingBlankTemplateModelBehavior ||
                    model.getBehavior() instanceof ExistingInstanceTypeModelBehavior ||
                    model.getBehavior() instanceof NewInstanceTypeModelBehavior) {
                return deviceEnabled;
            }

            return false;
        }

        return deviceEnabled
                && AsyncDataProvider.getInstance().isBalloonEnabled(osType,
                model.getCompatibilityVersion());
    }
}
