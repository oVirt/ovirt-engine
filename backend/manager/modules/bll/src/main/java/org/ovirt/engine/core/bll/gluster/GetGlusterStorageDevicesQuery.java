package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;

public class GetGlusterStorageDevicesQuery<P extends VdsIdParametersBase> extends QueriesCommandBase<P> {

    public GetGlusterStorageDevicesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Get Device List
        List<StorageDevice> storageDevices =
                getDbFacade().getStorageDeviceDao().getStorageDevicesInHost(getParameters().getVdsId());
        getQueryReturnValue().setReturnValue(filterStorageDevices(storageDevices));

    }

    private List<StorageDevice> filterStorageDevices(List<StorageDevice> storageDevices) {
        List<StorageDevice> filteredStorageDevices = new ArrayList<>();
        List<String> mountPointsToFilterOutList = getMountPointsFilter();
        List<String> fsTypesToFilterOutList = getFsTypesFilter();

        // Filter out the devices which are not going to be used as storage device for gluster.
        for (StorageDevice device : storageDevices) {
            if ((device.getMountPoint() != null && mountPointsToFilterOutList.contains(device.getMountPoint()))
                    || (device.getFsType() != null && fsTypesToFilterOutList.contains(device.getFsType()))) {
                continue;
            }
            if (device.getCanCreateBrick() || device.getMountPoint() != null || device.getFsType() != null) {
                filteredStorageDevices.add(device);
            }
        }
        return filteredStorageDevices;
    }

    private List<String> getMountPointsFilter() {
        return Arrays.asList(Config.<String> getValue(ConfigValues.MountPointsToIgoreInGlusterStorageList).split(","));

    }

    private List<String> getFsTypesFilter() {
        return Arrays.asList(Config.<String> getValue(ConfigValues.FileSystemTypesToIgoreInGlusterStorageList)
                .split(","));
    }
}
