package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.gluster.StorageDeviceDao;

public class GetGlusterStorageDevicesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private StorageDeviceDao storageDeviceDao;

    public GetGlusterStorageDevicesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        // Get Device List
        List<StorageDevice> storageDevices = storageDeviceDao.getStorageDevicesInHost(getParameters().getId());
        getQueryReturnValue().setReturnValue(filterStorageDevices(storageDevices));

    }

    private List<StorageDevice> filterStorageDevices(List<StorageDevice> storageDevices) {
        List<StorageDevice> filteredStorageDevices = new ArrayList<>();
        Pattern mountPointsFilterPattern = Pattern.compile(getMountPointsFilterPattern());
        List<String> fsTypesToFilterOutList = getFsTypesFilter();
        // Filter out the devices which are not going to be used as storage device for gluster.
        for (StorageDevice device : storageDevices) {
            if ((device.getMountPoint() != null && mountPointsFilterPattern.matcher(device.getMountPoint()).matches())
                    || (device.getFsType() != null && fsTypesToFilterOutList.contains(device.getFsType()))) {
                continue;
            }
            if (device.getCanCreateBrick() || device.getMountPoint() != null || device.getFsType() != null) {
                filteredStorageDevices.add(device);
            }
        }
        return filteredStorageDevices;
    }

    private String getMountPointsFilterPattern() {
        String[] mountPointsToIgnore = Config.<String> getValue(ConfigValues.GlusterStorageDeviceListMountPointsToIgnore).split(",");
        // Mounts to be ignored can be exact mount point or a regular expression which should be with the starting part
        // of the mount point. So create a regex which can match against any given pattern in the list.
        StringBuilder pattern = new StringBuilder();
        for(String mointPoint:mountPointsToIgnore){
            pattern.append("^");
            pattern.append(mointPoint);
            pattern.append("$");
            pattern.append("|");
        }
        return pattern.toString();
    }

    private List<String> getFsTypesFilter() {
        return Arrays.asList(Config.<String> getValue(ConfigValues.GlusterStorageDeviceListFileSystemTypesToIgnore)
                .split(","));
    }
}
