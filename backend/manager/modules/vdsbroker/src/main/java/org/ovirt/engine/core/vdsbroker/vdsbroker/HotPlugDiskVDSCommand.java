package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.ovirt.engine.core.vdsbroker.architecture.GetControllerIndices;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

public class HotPlugDiskVDSCommand<P extends HotPlugDiskVDSParameters> extends VdsBrokerCommand<P> {

    protected Map<String, Object> sendInfo = new HashMap<String, Object>();

    public HotPlugDiskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        buildSendDataToVdsm();
        status = getBroker().hotplugDisk(sendInfo);
        proceedProxyReturnValue();
    }

    protected void buildSendDataToVdsm() {
        sendInfo.put("vmId", getParameters().getVmId().toString());
        sendInfo.put("drive", initDriveData());
    }

    private Map<String, Object> initDriveData() {
        Map<String, Object> drive = new HashMap<String, Object>();
        Disk disk = getParameters().getDisk();
        VmDevice vmDevice = getParameters().getVmDevice();

        drive.put(VdsProperties.Type, VmDeviceType.DISK.getName());
        addAddress(drive, getParameters().getVmDevice().getAddress());
        drive.put(VdsProperties.INTERFACE, disk.getDiskInterface().getName());
        drive.put(VdsProperties.Shareable,
                (vmDevice.getSnapshotId() != null && FeatureSupported.hotPlugDiskSnapshot(getParameters().getVm()
                        .getVdsGroupCompatibilityVersion())) ? VdsProperties.Transient
                        : String.valueOf(disk.isShareable()));
        drive.put(VdsProperties.Optional, Boolean.FALSE.toString());
        drive.put(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
        drive.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());

        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) disk;
            drive.put(VdsProperties.Device, VmDeviceType.DISK.getName());
            drive.put(VdsProperties.Format, diskImage.getVolumeFormat().toString().toLowerCase());
            drive.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
            drive.put(VdsProperties.PoolId, diskImage.getStoragePoolId().toString());
            drive.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
            drive.put(VdsProperties.ImageId, diskImage.getId().toString());
            drive.put(VdsProperties.PropagateErrors, disk.getPropagateErrors().toString().toLowerCase());
        } else {
            LunDisk lunDisk = (LunDisk) disk;

            // If SCSI pass-through is enabled (VirtIO-SCSI/DirectLUN disk and SGIO is defined),
            // set device type as 'lun' (instead of 'disk') and set the specified SGIO
            boolean isVirtioScsi = getParameters().getDisk().getDiskInterface() == DiskInterface.VirtIO_SCSI;
            boolean isScsiPassthrough = getParameters().getDisk().isScsiPassthrough();
            if (isVirtioScsi) {
                if (isScsiPassthrough) {
                    drive.put(VdsProperties.Device, VmDeviceType.LUN.getName());
                    drive.put(VdsProperties.Sgio, getParameters().getDisk().getSgio().toString().toLowerCase());
                }
                else {
                    drive.put(VdsProperties.Device, VmDeviceType.DISK.getName());
                }
            }
            else {
                drive.put(VdsProperties.Device, VmDeviceType.LUN.getName());
            }

            drive.put(VdsProperties.Guid, lunDisk.getLun().getLUN_id());
            drive.put(VdsProperties.Format, VolumeFormat.RAW.toString().toLowerCase());
            drive.put(VdsProperties.PropagateErrors, PropagateErrors.Off.toString()
                    .toLowerCase());
        }

        return drive;
    }

    private void addAddress(Map<String, Object> map, String address) {
        if (getParameters().getDisk().getDiskInterface() != DiskInterface.VirtIO_SCSI &&
                getParameters().getDisk().getDiskInterface() != DiskInterface.SPAPR_VSCSI) {
            if (StringUtils.isNotBlank(address)) {
                map.put("address", XmlRpcStringUtils.string2Map(address));
            }
        } else {
            VM vm = DbFacade.getInstance().getVmDao().get(getParameters().getVmId());

            Map<DiskInterface, Integer> controllerIndexMap =
                    ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();

            int virtioScsiIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);
            int sPaprVscsiIndex = controllerIndexMap.get(DiskInterface.SPAPR_VSCSI);

            if (getParameters().getDisk().getDiskInterface() == DiskInterface.VirtIO_SCSI) {
                Map<VmDevice, Integer> vmDeviceUnitMap = VmInfoBuilder.getVmDeviceUnitMapForVirtioScsiDisks(getParameters().getVm());

                addAddressForScsiDisk(map, address, vmDeviceUnitMap, virtioScsiIndex, false);
            } else if (getParameters().getDisk().getDiskInterface() == DiskInterface.SPAPR_VSCSI) {
                Map<VmDevice, Integer> vmDeviceUnitMap = VmInfoBuilder.getVmDeviceUnitMapForSpaprScsiDisks(getParameters().getVm());

                addAddressForScsiDisk(map, address, vmDeviceUnitMap, sPaprVscsiIndex, true);
            }
        }
    }

    private void addAddressForScsiDisk(Map<String, Object> map,
            String address,
            Map<VmDevice, Integer> vmDeviceUnitMap,
            int controllerIndex,
            boolean reserveFirstAddress) {
        int availableUnit = VmInfoBuilder.getAvailableUnitForScsiDisk(vmDeviceUnitMap, reserveFirstAddress);

        // If address has been already set before, verify its uniqueness;
        // Otherwise, set address according to the next available unit.
        if (StringUtils.isNotBlank(address)) {
            Map<String, String> addressMap = XmlRpcStringUtils.string2Map(address);
            int unit = Integer.valueOf(addressMap.get(VdsProperties.Unit));
            if (!vmDeviceUnitMap.containsValue(unit)) {
                map.put(VdsProperties.Address, addressMap);
            }
            else {
                map.put(VdsProperties.Address, VmInfoBuilder.createAddressForScsiDisk(controllerIndex, availableUnit));
            }
        }
        else {
            map.put(VdsProperties.Address, VmInfoBuilder.createAddressForScsiDisk(controllerIndex, availableUnit));
        }
    }
}
