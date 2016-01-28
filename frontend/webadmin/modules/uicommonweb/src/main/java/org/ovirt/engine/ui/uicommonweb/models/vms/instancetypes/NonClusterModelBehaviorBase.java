package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class NonClusterModelBehaviorBase extends VmModelBehaviorBase<UnitVmModel> {

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);

        getModel().getIsVirtioScsiEnabled().setIsAvailable(true);
        getModel().getIsVirtioScsiEnabled().setEntity(false);

        getModel().getMemoryBalloonDeviceEnabled().setIsAvailable(true);

        getModel().updateWatchdogItems(new HashSet<>(Arrays.asList(VmWatchdogType.values())));

        // no cluster data - init list to 'use cluster default' option
        getModel().getEmulatedMachine().setItems(Arrays.asList("")); //$NON-NLS-1$
        getModel().getCustomCpu().setItems(Arrays.asList("")); //$NON-NLS-1$
    }

    protected void initDisplayTypes(DisplayType selected, UnitVmModel.GraphicsTypes selectedGrahicsTypes) {
        getModel().getDisplayType().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                enableSinglePCI(getModel().getDisplayType().getSelectedItem() == DisplayType.qxl);
            }
        });

        List<Pair<GraphicsType, DisplayType>> allGraphicsAndDisplays = new ArrayList<>();
        for (GraphicsType graphicsType : GraphicsType.values()) {
            for (DisplayType displayType : DisplayType.values()) {
                allGraphicsAndDisplays.add(new Pair<>(graphicsType, displayType));
            }
        }

        getModel().initDisplayModels(allGraphicsAndDisplays);
        initGraphicsModel(selectedGrahicsTypes);

        if (getModel().getDisplayType().getItems().contains(selected)) {
            getModel().getDisplayType().setSelectedItem(selected);
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

}
