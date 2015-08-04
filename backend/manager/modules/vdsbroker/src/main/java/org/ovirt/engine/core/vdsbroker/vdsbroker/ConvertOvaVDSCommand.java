package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.vdscommands.ConvertOvaVDSParameters;

public class ConvertOvaVDSCommand<T extends ConvertOvaVDSParameters> extends VdsBrokerCommand<T> {

    public ConvertOvaVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().convertVmFromOva(
                getParameters().getOvaPath(),
                createVmProperties(),
                getParameters().getVmId().toString());
        proceedProxyReturnValue();
        setReturnValue(getVDSReturnValue().getSucceeded() ? getParameters().getVmId() : null);
    }

    private Map<String, Object> createVmProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put(VdsProperties.vm_name, getParameters().getVmName());
        map.put(VdsProperties.PoolId, getParameters().getStoragePoolId().toString());
        map.put(VdsProperties.DomainId, getParameters().getStorageDomainId().toString());
        map.put(VdsProperties.vm_disks, getDisksProperties());
        // Currently we send global volume format and type for all disks:
        for (DiskImage diskImage : getParameters().getDisks()) {
            map.put(VdsProperties.Format, diskImage.getVolumeFormat().toString());
            map.put(VdsProperties.DISK_ALLOCATION, diskImage.getVolumeType().toString());
            break;
        }
        if (getParameters().getVirtioIsoPath() != null) {
            map.put(VdsProperties.VIRTIO_ISO_PATH, getParameters().getVirtioIsoPath());
        }
        return map;
    }

    private List<?> getDisksProperties() {
        List<Map<String, String>> disks = new ArrayList<>();
        for (final DiskImage disk : getParameters().getDisks()) {
            disks.add(new HashMap<String, String>() {{
                put(VdsProperties.ImageId, disk.getId().toString());
                put(VdsProperties.VolumeId, disk.getImageId().toString());
            }});
        }
        return disks;
    }
}
