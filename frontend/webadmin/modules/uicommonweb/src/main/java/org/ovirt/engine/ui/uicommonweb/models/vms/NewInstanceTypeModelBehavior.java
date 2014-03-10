package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

import java.util.Arrays;

public class NewInstanceTypeModelBehavior extends InstanceTypeModelBehaviorBase {

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);

        updateNumOfSockets();

        initDisplayTypes(DisplayType.qxl);

        // no way to pick a specific host
        getModel().getIsAutoAssign().setEntity(true);
        getModel().getUsbPolicy().setItems(Arrays.asList(UsbPolicy.values()));

        getModel().getIsSoundcardEnabled().setIsChangable(true);
        getModel().setSelectedMigrationDowntime(null);
        updateMemoryBalloon(latestCluster());
        initPriority(0);
        getModel().getTotalCPUCores().setEntity("1"); //$NON-NLS-1$
    }

}
