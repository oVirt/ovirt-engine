package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import java.util.Arrays;
import java.util.HashSet;

public class InstanceTypeModelBehaviorBase extends VmModelBehaviorBase<UnitVmModel> {

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);

        getModel().getIsVirtioScsiEnabled().setIsAvailable(true);
        getModel().getIsVirtioScsiEnabled().setEntity(false);

        getModel().getMemoryBalloonDeviceEnabled().setIsAvailable(true);

        getModel().updateWatchdogItems(new HashSet<VmWatchdogType>(Arrays.asList(VmWatchdogType.values())));
    }

    protected void initDisplayTypes(DisplayType selected) {
        getModel().getDisplayProtocol().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                EntityModel<DisplayType> displayType = getModel().getDisplayProtocol().getSelectedItem();
                enableSinglePCI(displayType.getEntity() == DisplayType.qxl);
            }
        });

        getModel().initDisplayProtocolWithTypes(Arrays.asList(DisplayType.values()));

        for (EntityModel<DisplayType> displayType : getModel().getDisplayProtocol().getItems()) {
            if (displayType.getEntity() == selected) {
                getModel().getDisplayProtocol().setSelectedItem(displayType);
                break;
            }
        }
    }

    @Override
    protected Version getClusterCompatibilityVersion() {
        return latestCluster();
    }

    protected Version latestCluster() {
        // instance type always exposes all the features of the latest cluster and if some is not applicable
        // than that particular feature will not be applicable on the instance creation
        return Version.ALL.get(Version.ALL.size() - 1);
    }

    @Override
    public void template_SelectedItemChanged() {

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

    @Override
    public void oSType_SelectedItemChanged() {

    }

    @Override
    public void updateMinAllocatedMemory() {
    }

}
