package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

public class NewInstanceTypeModelBehavior extends NonClusterModelBehaviorBase {

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);
        updateNumOfSockets();

        initDisplayTypes(DisplayType.qxl, UnitVmModel.GraphicsTypes.SPICE);

        // no way to pick a specific host
        getModel().getIsAutoAssign().setEntity(true);
        getModel().getUsbPolicy().setItems(Arrays.asList(UsbPolicy.values()));

        getModel().getIsSoundcardEnabled().setIsChangeable(true);
        getModel().setSelectedMigrationDowntime(null);
        getModel().getMemoryBalloonDeviceEnabled().setIsAvailable(true);
        initPriority(0);
        getModel().getTotalCPUCores().setEntity("1"); //$NON-NLS-1$

        getModel().getNicsWithLogicalNetworks().getVnicProfiles().setItems(new ArrayList<>(Arrays.asList(VnicProfileView.EMPTY)));
        getModel().getNicsWithLogicalNetworks().setItems(new ArrayList<VnicInstanceType>());
    }
}
