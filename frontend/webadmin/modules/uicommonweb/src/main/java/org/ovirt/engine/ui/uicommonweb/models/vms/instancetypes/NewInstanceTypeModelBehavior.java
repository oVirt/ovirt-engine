package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

public class NewInstanceTypeModelBehavior extends NonClusterModelBehaviorBase {

    @Override
    public void initialize() {
        super.initialize();
        updateNumOfSockets();

        initDisplayTypes(DisplayType.qxl, UnitVmModel.GraphicsTypes.SPICE);

        // no way to pick a specific host
        getModel().getIsAutoAssign().setEntity(true);

        getModel().getIsSoundcardEnabled().setIsChangeable(true);
        getModel().setSelectedMigrationDowntime(null);
        getModel().getMemoryBalloonEnabled().setIsAvailable(true);
        initPriority(0);
        getModel().getTotalCPUCores().setEntity("1"); //$NON-NLS-1$

        getModel().getNicsWithLogicalNetworks().getVnicProfiles().setItems(new ArrayList<>(Arrays.asList(VnicProfileView.EMPTY)));
        getModel().getNicsWithLogicalNetworks().setItems(new ArrayList<VnicInstanceType>());
        getModel().getIsUsbEnabled().setEntity(false);
    }
}
