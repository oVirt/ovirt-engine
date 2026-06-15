package org.ovirt.engine.core.bll.exportimport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DeviceInfoReturn;

@NonTransactiveCommandAttribute
public class MbsExtractOvaCommand<T extends ConvertOvaParameters> extends ExtractOvaCommand<T> {

    @Inject
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;

    public MbsExtractOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void prepareDisksBeforeExtract() {
        Map<Guid, Map<String, Object>> fromParent = getParameters().getPreAttachedManagedBlockDevicesByDiskId();
        if (fromParent != null) {
            for (DiskImage disk : getDiskList()) {
                if (disk instanceof ManagedBlockStorageDisk) {
                    Map<String, Object> dev = fromParent.get(disk.getId());
                    if (dev != null) {
                        ((ManagedBlockStorageDisk) disk).setDevice(new HashMap<>(dev));
                    }
                }
            }
        }
        List<ManagedBlockStorageDisk> mbsDisks =
                DisksFilter.filterManagedBlockStorageDisks(getDiskList());
        if (mbsDisks.isEmpty()) {
            return;
        }
        if (mbsDisks.stream().noneMatch(d -> d.getDevice() == null)) {
            return;
        }
        VDS host = getVds();
        if (host == null) {
            throw new EngineException(EngineError.GeneralException, "OVA extract requires a proxy host");
        }
        boolean parentSentDevices = fromParent != null && !fromParent.isEmpty();
        if (parentSentDevices) {
            throw new EngineException(
                    EngineError.StorageException,
                    "Managed-block import passed device metadata but disks still lack device after apply; "
                            + "check disk id alignment with the parent import.");
        }
        if (!managedBlockStorageCommandUtil.attachManagedBlockStorageDisksOnHost(
                mbsDisks,
                host,
                getParameters().getVmId())) {
            throw new EngineException(
                    EngineError.StorageException,
                    "Failed to connect or attach managed-block volumes on the OVA extract host");
        }
    }

    @Override
    protected String prepareImagePath(DiskImage image) {
        if (image instanceof ManagedBlockStorageDisk) {
            return managedBlockImagePath((ManagedBlockStorageDisk) image);
        }
        return super.prepareImagePath(image);
    }

    @Override
    protected void teardownImage(DiskImage image) {
        if (image instanceof ManagedBlockStorageDisk) {
            Guid proxy = getParameters().getProxyHostId();
            if (proxy != null && !Guid.isNullOrEmpty(proxy)) {
                managedBlockStorageCommandUtil.disconnectManagedBlockStorageDeviceFromHost(image, proxy);
            }
            return;
        }
        super.teardownImage(image);
    }

    private String managedBlockImagePath(ManagedBlockStorageDisk disk) {
        Map<String, Object> device = disk.getDevice();
        if (device == null) {
            throw new EngineException(
                    EngineError.StorageException,
                    "Managed-block volume has no device on the proxy host; attach volumes before OVA extract.");
        }

        String path = (String) device.get(DeviceInfoReturn.MANAGED_PATH);
        if (StringUtils.isEmpty(path)) {
            path = (String) device.get(DeviceInfoReturn.PATH);
        }
        if (StringUtils.isEmpty(path)) {
            throw new EngineException(
                    EngineError.StorageException,
                    "Managed-block device path missing in volume metadata after attach.");
        }
        return path;
    }
}
