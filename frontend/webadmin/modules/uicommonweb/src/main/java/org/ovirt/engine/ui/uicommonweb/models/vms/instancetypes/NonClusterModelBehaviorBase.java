package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmResumeBehavior;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;

public class NonClusterModelBehaviorBase extends VmModelBehaviorBase<UnitVmModel> {

    @Override
    public void initialize() {
        super.initialize();

        getModel().getIsVirtioScsiEnabled().setIsAvailable(true);
        getModel().getIsVirtioScsiEnabled().setEntity(false);
        getModel().getLease().setIsAvailable(false);

        getModel().getMemoryBalloonEnabled().setIsAvailable(true);

        getModel().updateWatchdogItems(new HashSet<>(Arrays.asList(VmWatchdogType.values())));

        // no cluster data - init list to 'use cluster default' option
        getModel().getEmulatedMachine().setItems(Arrays.asList("")); //$NON-NLS-1$
        getModel().getCustomCpu().setItems(Arrays.asList("")); //$NON-NLS-1$
        getModel().getResumeBehavior().setItems(Arrays.asList(VmResumeBehavior.values()));
    }

    protected void initDisplayTypes(DisplayType selected, UnitVmModel.GraphicsTypes selectedGrahicsTypes) {
        getModel().initDisplayModels(new HashSet<>(Arrays.asList(DisplayType.values())), selected);
        initGraphicsModel(selectedGrahicsTypes);

        if (selected == DisplayType.none) {
            getModel().getDisplayType().setSelectedItem(DisplayType.qxl);
            getModel().getGraphicsType().setSelectedItem(UnitVmModel.GraphicsTypes.SPICE);
            getModel().getIsHeadlessModeEnabled().setEntity(true);
        }
    }

    private void initGraphicsModel(UnitVmModel.GraphicsTypes selectedGrahicsTypes) {
        List graphicsTypes = new ArrayList();
        graphicsTypes.add(UnitVmModel.GraphicsTypes.SPICE);
        graphicsTypes.add(UnitVmModel.GraphicsTypes.VNC);
        graphicsTypes.add(UnitVmModel.GraphicsTypes.SPICE_AND_VNC);
        getModel().getGraphicsType().setItems(graphicsTypes);
        getModel().getGraphicsType().setSelectedItem(selectedGrahicsTypes);
    }

    @Override
    protected Version getCompatibilityVersion() {
        return latestCluster();
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
    }

    @Override
    public void defaultHost_SelectedItemChanged() {
    }

    @Override
    public void provisioning_SelectedItemChanged() {
    }

    @Override public int getMaxNameLength() {
        return UnitVmModel.VM_TEMPLATE_AND_INSTANCE_TYPE_NAME_MAX_LIMIT;
    }

    @Override
    public VmRngDevice.Source getUrandomOrRandomRngSource() {
        return VmRngDevice.Source.URANDOM;
    }

    @Override
    protected void initializeBiosType() {
        getModel().getBiosType().setItems(Arrays.asList((BiosType) null));
        getModel().getBiosType().setSelectedItem(null);
        getModel().getBiosType().setIsChangeable(false);
    }
}
