package org.ovirt.engine.core.bll.utils;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.bll.validator.VirtIoRngValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ConsoleTargetType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.UsbControllerModel;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.VmDeviceUpdate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.ovirt.engine.core.vdsbroker.monitoring.VmDevicesMonitoring;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class VmDeviceUtils {

    private static final String EHCI_MODEL = "ich9-ehci";
    private static final String UHCI_MODEL = "ich9-uhci";
    private static final String XHCI_MODEL = "qemu-xhci";
    private static final int SLOTS_PER_CONTROLLER = 6;
    private static final int COMPANION_USB_CONTROLLERS = 3;
    private static final int VNC_MIN_MONITORS = 1;
    private static final int SINGLE_QXL_MONITORS = 1;
    private static final Logger log = LoggerFactory.getLogger(VmDeviceUtils.class);

    private final VmStaticDao vmStaticDao;
    private final VmDao vmDao;
    private final VmDeviceDao vmDeviceDao;
    private final ClusterDao clusterDao;
    private final VmTemplateDao vmTemplateDao;
    private final VmHandler vmHandler;
    private final MacPoolPerCluster macPoolPerCluster;
    private final RngDeviceUtils rngDeviceUtils;
    private final VideoDeviceSettings videoDeviceSettings;

    private OsRepository osRepository;

    private CompensationContext compensationContext;

    @Inject
    private VmDevicesMonitoring vmDevicesMonitoring;

    @Inject
    VmDeviceUtils(VmStaticDao vmStaticDao,
                  VmDao vmDao,
                  VmDeviceDao vmDeviceDao,
                  ClusterDao clusterDao,
                  VmTemplateDao vmTemplateDao,
                  VmHandler vmHandler,
                  MacPoolPerCluster macPoolPerCluster,
                  RngDeviceUtils rngDeviceUtils,
                  VideoDeviceSettings videoDeviceSettings,
                  OsRepository osRepository) {
        this.vmStaticDao = vmStaticDao;
        this.vmDao = vmDao;
        this.vmDeviceDao = vmDeviceDao;
        this.clusterDao = clusterDao;
        this.vmTemplateDao = vmTemplateDao;
        this.vmHandler = vmHandler;
        this.macPoolPerCluster = macPoolPerCluster;
        this.rngDeviceUtils = rngDeviceUtils;
        this.videoDeviceSettings = videoDeviceSettings;
        this.osRepository = osRepository;
    }

    public CompensationContext getCompensationContext() {
        return compensationContext;
    }

    public void setCompensationContext(CompensationContext compensationContext) {
        this.compensationContext = compensationContext;
    }

    /*
     * CD-ROM device
     */

    /**
     * Select the CD path to be used on the destination VM when copying CD device from source to
     * destination VM. If the CD path on the destination VM is already set to non-empty value,
     * it is used. Otherwise, the CD path on the source VM is used. If both paths are empty or null,
     * the result will be an empty string.
     *
     * @param srcCdPath CD path on the source VM
     * @param dstCdPath CD path on the destination VM
     * @return  CD path to be used on the destination VM
     */
    private String getCdPath(String srcCdPath, String dstCdPath) {
        if (!StringUtils.isEmpty(dstCdPath)) {
            return dstCdPath;
        } else if (!StringUtils.isEmpty(srcCdPath)) {
            return srcCdPath;
        } else {
            return "";
        }
    }

    /**
     * Determine the bus interface (IDE, SCSI, SATA etc.) to be used for CD device in the given VM.
     */
    public String getCdInterface(VM vm) {
        ChipsetType chipset = vm.getBiosType() == null ? null : vm.getBiosType().getChipsetType();
        return osRepository.getCdInterface(
                vm.getOs(),
                vm.getCompatibilityVersion(),
                chipset);
    }

    /**
     * Copy CD path from old to new VM.
     *
     * <b>Note:</b> Only one CD is currently supported.
     */
    private void updateCdPath(VmBase oldVmBase, VmBase newVmBase) {
        List<VmDevice> cdList = getCdDevices(oldVmBase.getId());
        if (cdList.size() > 0) { // this is done only for safety, each VM must have at least an empty CD
            VmDevice cd = cdList.get(0); // only one managed CD is currently supported.
            cd.getSpecParams().putAll(getCdDeviceSpecParams("", newVmBase.getIsoPath()));
            CompensationUtils.updateEntity(cd, vmDeviceDao, compensationContext);
        }
    }

    /**
     * Get list of all CD-ROM devices in the VM.
     */
    public List<VmDevice> getCdDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.DISK,
                VmDeviceType.CDROM);
    }

    /**
     * Check if the VM has a CD-ROM device.
     */
    public boolean hasCdDevice(Guid vmId) {
        return !getCdDevices(vmId).isEmpty();
    }

    /**
     * Get CD-ROM device spec params.
     */
    private Map<String, Object> getCdDeviceSpecParams(String srcCdPath, String dstCdPath) {
        return Collections.singletonMap(VdsProperties.Path, getCdPath(srcCdPath, dstCdPath));
    }

    /**
     * Add CD-ROM device with given CD path to the VM.
     */
    public VmDevice addCdDevice(Guid vmId, String cdPath) {
        return addManagedDevice(
            new VmDeviceId(Guid.newGuid(), vmId),
            VmDeviceGeneralType.DISK,
            VmDeviceType.CDROM,
            getCdDeviceSpecParams("", cdPath),
            true,
            true);
    }

    /**
     * Add CD-ROM device with empty CD path to the VM.
     */
    public VmDevice addCdDevice(Guid vmId) {
        return addCdDevice(vmId, "");
    }

    /*
     * Smartcard device
     */

    /**
     * Update smartcard device in the new VM, if its state should be different from the old VM.
     */
    private void updateSmartcardDevice(VM oldVm, VmBase newVm) {
        if (newVm.isSmartcardEnabled() == oldVm.isSmartcardEnabled()) {
            return;
        }

        updateSmartcardDevice(newVm.getId(), newVm.isSmartcardEnabled());
    }

    /**
     * Enable/disable smartcard device in a VM.
     *
     * @param vmId  id of the VM to be modified
     * @param smartcardEnabled  enable/disable flag
     */
    public void updateSmartcardDevice(Guid vmId, boolean smartcardEnabled) {
        if (smartcardEnabled) {
            if (!hasSmartcardDevice(vmId)) {
                addSmartcardDevice(vmId);
            }
        } else {
            removeSmartcardDevices(vmId);
        }
    }

    /**
     * Get list of all smartcard devices in the VM.
     */
    public List<VmDevice> getSmartcardDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.SMARTCARD,
                VmDeviceType.SMARTCARD);
    }

    /**
     * Remove all smartcard devices from the VM.
     */
    public void removeSmartcardDevices(Guid vmId) {
        removeVmDevices(getSmartcardDevices(vmId));
    }

    /**
     * Check if the VM has a smartcard device.
     */
    public boolean hasSmartcardDevice(Guid vmId) {
        return !getSmartcardDevices(vmId).isEmpty();
    }

    /**
     * Add new smartcard device to the VM.
     */
    public VmDevice addSmartcardDevice(Guid vmId) {
        return addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.SMARTCARD,
                VmDeviceType.SMARTCARD,
                getSmartcardDeviceSpecParams(),
                true,
                false);
    }

    /**
     * Returns smartcard device spec params.
     */
    private Map<String, Object> getSmartcardDeviceSpecParams() {
        Map<String, Object> specParams = new HashMap<>();
        specParams.put("mode", "passthrough");
        specParams.put("type", "spicevmc");
        return specParams;
    }

    /*
     * Console device
     */

    /**
     * Enable/disable console device in the VM.
     *
     * @param consoleEnabled true/false to enable/disable device respectively, null to leave it untouched
     */
    public void updateConsoleDevice(Guid vmId, Boolean consoleEnabled) {
        if (consoleEnabled == null) {
            return; //we don't want to update the device
        }

        if (consoleEnabled) {
            if (!hasConsoleDevice(vmId)) {
                addConsoleDevice(vmId);
            }
        } else {
            removeConsoleDevices(vmId);
        }
    }

    /**
     * Get list of all console devices in the VM.
     */
    public List<VmDevice> getConsoleDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.CONSOLE,
                VmDeviceType.CONSOLE);
    }

    /**
     * Remove all console devices from the VM.
     */
    public void removeConsoleDevices(Guid vmId) {
        removeVmDevices(getConsoleDevices(vmId));
    }

    /**
     * Check if the VM has a console device.
     */
    public boolean hasConsoleDevice(Guid vmId) {
        return !getConsoleDevices(vmId).isEmpty();
    }

    /**
     * Add new console device to the VM.
     */
    public VmDevice addConsoleDevice(Guid vmId) {
        return addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.CONSOLE,
                VmDeviceType.CONSOLE,
                getConsoleDeviceSpecParams(vmId),
                true,
                false);
    }

    /**
     * Returns console device spec params.
     */
    private Map<String, Object> getConsoleDeviceSpecParams(Guid vmId) {
        Map<String, Object> specParams = new HashMap<>();
        VmBase vmBase = getVmBase(vmId);
        ConsoleTargetType targetType = osRepository.getOsConsoleTargetType(
                vmBase.getOsId(),
                CompatibilityVersionUtils.getEffective(
                        vmBase,
                        () -> getCluster(vmBase.getClusterId())));
        specParams.put("enableSocket", "true");
        specParams.put("consoleType",
                targetType == null ? "serial" : targetType.libvirtName);
        return specParams;
    }

    /*
     * VirtIO-SCSI controller
     */

    /**
     * Enable/disable VirtIO-SCSI controllers in the VM.
     *
     * @param isVirtioScsiEnabled    true/false to enable/disable device respectively, null to keep the current status
     */
    public void updateVirtioScsiController(VmBase vm, Boolean isVirtioScsiEnabled) {
        boolean enabled = isVirtioScsiEnabled != null ? isVirtioScsiEnabled : hasVirtioScsiController(vm.getId());

        removeVirtioScsiControllers(vm.getId());
        if (enabled) {
            addVirtioScsiController(vm, getVmCompatibilityVersion(vm));
        }
    }

    /**
     * Add new VirtIO-SCSI controllers to the VM.
     */
    public void addVirtioScsiController(VmBase vm, Version version) {
        boolean hasIoThreads = vm.getNumOfIoThreads() > 0;
        int numOfScsiControllers = hasIoThreads ? vm.getNumOfIoThreads() : 1;

        for (int i = 0; i < numOfScsiControllers; i++) {
            Map<String, Object> specParams = new HashMap<>();
            if (hasIoThreads) {
                // i + 1 because libvirt is indexing the io threads from 1 to N, not 0 to N - 1
                specParams.put(VdsProperties.ioThreadId, i + 1);
            }
            VmDevice device = addManagedDevice(
                    new VmDeviceId(Guid.newGuid(), vm.getId()),
                    VmDeviceGeneralType.CONTROLLER,
                    VmDeviceType.VIRTIOSCSI,
                    specParams,
                    true,
                    false);

        }
    }

    /**
     * Get list of all VirtIO-SCSI controllers in the VM.
     */
    public List<VmDevice> getVirtioScsiControllers(Guid vmId) {
        return getVirtioScsiControllers(vmId, null, false);
    }

    /**
     * Get list of VirtIO-SCSI controllers in the VM.
     */
    public List<VmDevice> getVirtioScsiControllers(Guid vmId, Guid userID, boolean isFiltered) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.VIRTIOSCSI.getName(),
                userID,
                isFiltered);
    }

    /**
     * Remove all VirtIO-SCSI controllers from the VM.
     */
    public void removeVirtioScsiControllers(Guid vmId) {
        removeVmDevices(getVirtioScsiControllers(vmId));
    }

    /**
     * Check if the VM has a VirtIO-SCSI controller.
     */
    public boolean hasVirtioScsiController(Guid vmId) {
        return !getVirtioScsiControllers(vmId).isEmpty();
    }

    /*
     * Sound device
     */

    /**
     * Update sound device in the new VM, if its state should be different from the old VM. Recreate the device in any
     * case, if OS or chipset has been changed.
     *
     * @param compatibilityVersion  cluster compatibility version
     * @param isSoundDeviceEnabled  true/false to enable/disable device respectively, null to leave it untouched
     */
    public void updateSoundDevice(VmBase oldVmBase, VmBase newVmBase, Version compatibilityVersion,
                                  Boolean isSoundDeviceEnabled) {

        ChipsetType oldChipset = oldVmBase.getBiosType() == null ? null : oldVmBase.getBiosType().getChipsetType();
        ChipsetType newChipset = newVmBase.getBiosType() == null ? null : newVmBase.getBiosType().getChipsetType();

        boolean chipsetChanged = oldChipset != newChipset;
        boolean osChanged = oldVmBase.getOsId() != newVmBase.getOsId();
        updateSoundDevice(newVmBase.getId(), newVmBase.getOsId(), compatibilityVersion,
                newChipset, isSoundDeviceEnabled, osChanged || chipsetChanged);
    }

    /**
     * Enable/disable sound device in the VM.
     *
     * @param compatibilityVersion  cluster compatibility version
     * @param isSoundDeviceEnabled  true/false to enable/disable device respectively, null to leave it untouched
     * @param recreate              true to recreate the device even if it already exists
     */
    public void updateSoundDevice(Guid vmId, int osId, Version compatibilityVersion, ChipsetType chipset,
                                  Boolean isSoundDeviceEnabled, boolean recreate) {
        boolean removeDevice = false;
        boolean createDevice = false;

        List<VmDevice> list = getSoundDevices(vmId);

        if (isSoundDeviceEnabled == null) {
            if (!list.isEmpty() && recreate) {
                removeDevice = createDevice = true;
            }
        } else {
            // if sound device is to be disabled or must be recreated, and the device exists, remove it
            removeDevice = (!isSoundDeviceEnabled || recreate) && !list.isEmpty();

            // if sound device is to be enabled or must be recreated, and the device does not exist, create it
            createDevice = isSoundDeviceEnabled && (list.isEmpty() || recreate);
        }

        if (removeDevice) {
            removeVmDevices(list);
        }
        if (createDevice) {
            addSoundDevice(vmId, osId, compatibilityVersion, chipset);
        }
    }

    /**
     * Get list of all sound devices in the VM.
     */
    public List<VmDevice> getSoundDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.SOUND);
    }

    /**
     * Add new sound device to the VM.
     */
    public VmDevice addSoundDevice(VmBase vmBase, Supplier<Cluster> clusterSupplier) {
        Version compatibilityVersion = CompatibilityVersionUtils.getEffective(vmBase, clusterSupplier);
        ChipsetType chipsetType = vmBase.getBiosType() == null ? null : vmBase.getBiosType().getChipsetType();
        return addSoundDevice(vmBase.getId(), vmBase.getOsId(), compatibilityVersion, chipsetType);
    }

    /**
     * Add new sound device to the VM.
     */
    public VmDevice addSoundDevice(Guid vmId, int osId, Version compatibilityVersion, ChipsetType chipset) {
        String soundDevice = osRepository.getSoundDevice(osId, compatibilityVersion, chipset);
        return addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.SOUND,
                VmDeviceType.getSoundDeviceType(soundDevice),
                Collections.emptyMap(),
                true,
                true);
    }

    /**
     * Check if the VM has a sound device.
     */
    public boolean hasSoundDevice(Guid vmId) {
        return !getSoundDevices(vmId).isEmpty();
    }

    /**
     * Checks if a sound device must be added automatically.
     *
     * A sound device must be added automatically, if all of the following conditions
     * are true:
     * <ul>
     *  <li>User has not specified explicitly to enable/disable the device</li>
     *  <li>Sound device is supported in the given compatibility version</li>
     *  <li>VM is of desktop type</li>
     * </ul>
     */
    public boolean shouldOverrideSoundDevice(VmStatic vmStatic, Version compatibilityVersion,
                                                    Boolean soundDeviceEnabled) {
        return soundDeviceEnabled == null &&
                osRepository.isSoundDeviceEnabled(vmStatic.getOsId(), compatibilityVersion) &&
                vmStatic.getVmType() == VmType.Desktop;
    }

    /*
     * Video device
     */

    public void updateVideoDevices(VmBase oldVmBase, VmBase newVmBase) {
        boolean displayTypeChanged = oldVmBase.getDefaultDisplayType() != newVmBase.getDefaultDisplayType();
        boolean numOfMonitorsChanged = newVmBase.getDefaultDisplayType() == DisplayType.qxl &&
                oldVmBase.getNumOfMonitors() != newVmBase.getNumOfMonitors();
        boolean guestOsChanged = oldVmBase.getOsId() != newVmBase.getOsId();

        if (displayTypeChanged || numOfMonitorsChanged || guestOsChanged) {
            removeVideoDevices(oldVmBase.getId());
            addVideoDevices(newVmBase, getNeededNumberOfVideoDevices(newVmBase));
        } else {
            // fix vm's without video devices
            addVideoDevicesOnlyIfNoVideoDeviceExists(newVmBase);
        }
    }

    private int getNeededNumberOfVideoDevices(VmBase vmBase) {
        int maxMonitorsSpice = VmDeviceCommonUtils.isSingleQxlPci(vmBase) ? SINGLE_QXL_MONITORS : vmBase.getNumOfMonitors();
        int maxMonitorsVnc = Math.max(VNC_MIN_MONITORS, vmBase.getNumOfMonitors());

        return Math.min(maxMonitorsSpice, maxMonitorsVnc);
    }

    /**
     * Add given number of video devices to the VM.
     */
    public void addVideoDevices(VmBase vmBase, int numberOfVideoDevices) {
        if (vmBase.getDefaultDisplayType() != DisplayType.none) {
            for (int i = 0; i < numberOfVideoDevices; i++) {
                addManagedDevice(
                        new VmDeviceId(Guid.newGuid(), vmBase.getId()),
                        VmDeviceGeneralType.VIDEO,
                        vmBase.getDefaultDisplayType().getDefaultVmDeviceType(),
                        getVideoDeviceSpecParams(vmBase),
                        true,
                        false);
            }
        }
    }

    public void addVideoDevicesOnlyIfNoVideoDeviceExists(VmBase vmBase) {
        if (getVideoDevices(vmBase.getId()).isEmpty()) {
            addVideoDevices(vmBase, getNeededNumberOfVideoDevices(vmBase));
        }
    }

    /**
     * Returns video device spec params.
     *
     * @return a map of device parameters
     */
    private Map<String, Object> getVideoDeviceSpecParams(VmBase vmBase) {
        return videoDeviceSettings.getVideoDeviceSpecParams(vmBase, VmDeviceCommonUtils.isSingleQxlPci(vmBase));
    }

    /**
     * Get list of all video devices in the VM.
     */
    public List<VmDevice> getVideoDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.VIDEO);
    }

    /**
     * Remove all video devices from the VM.
     */
    public void removeVideoDevices(Guid vmId) {
        removeVmDevices(getVideoDevices(vmId));
    }

    /*
     * TPM device
     */

    /**
     * Enable/disable Tpm device in the VM.
     *
     * @param newVm new configuration
     * @param newVmCluster new cluster
     * @param tpmEnabled whether to enable or disable the device; if null then remove the device
              if it cannot be supported and leave it unchanged otherwise
     */
    public void updateTpmDevice(VmBase newVm, Cluster newVmCluster, Boolean tpmEnabled) {
        final Guid vmId = newVm.getId();
        if (!isTpmDeviceSupported(newVm, newVmCluster)) {
            tpmEnabled = false;
        }
        if (tpmEnabled == null) {
            return;
        }

        if (tpmEnabled) {
            if (!hasTpmDevice(vmId)) {
                addTpmDevice(vmId);
            }
        } else {
            removeTpmDevices(vmId);
        }
    }

    /**
     * Add new TPM device to the VM.
     */
    public VmDevice addTpmDevice(Guid vmId) {
        return addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.TPM,
                VmDeviceType.TPM,
                Collections.emptyMap(),
                true,
                true);
    }

    /**
     * Get list of all TPM devices in the VM.
     */
    public List<VmDevice> getTpmDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.TPM);
    }

    /**
     * Remove all TPM devices from the VM.
     */
    public void removeTpmDevices(Guid vmId) {
        removeVmDevices(getTpmDevices(vmId));
    }

    /**
     * Check if the VM has a TPM device.
     */
    public boolean hasTpmDevice(Guid vmId) {
        return !getTpmDevices(vmId).isEmpty();
    }

    /**
     * Check if a TPM device is supported for the given VM in the given cluster.
     */
    public boolean isTpmDeviceSupported(VmBase vm, Cluster vmCluster) {
        if (!osRepository.isTpmAllowed(vm.getOsId())) {
            return false;
        }

        final Version version = CompatibilityVersionUtils.getEffective(vm, vmCluster);
        boolean isOvmf = vm.getBiosType() == null ? false : vm.getBiosType().isOvmf();

        return vmCluster == null || FeatureSupported.isTpmDeviceSupported(version, vmCluster.getArchitecture())
                && (vmCluster.getArchitecture().getFamily() != ArchitectureType.x86 || isOvmf);
    }

    /**
     * Check if a TPM device should be enabled for the given VM in the given cluster.
     */
    public boolean isTpmDeviceEnabled(VmBase vm, Cluster vmCluster) {
        return isTpmDeviceSupported(vm, vmCluster) && hasTpmDevice(vm.getId());
    }

    /*
     * Network interface
     */

    /**
     * Add new network interface to the VM.
     *
     * @param deviceId    the NIC id in the VM
     * @param plugged     is NIC plugged to the VM or not
     * @param hostDev     true if NIC is a host device, false if it is a bridge
     * @return  the device added
     */
    public VmDevice addInterface(Guid vmId, Guid deviceId, boolean plugged, boolean hostDev) {
        return addInterface(vmId, deviceId, plugged, hostDev, null);
    }

    /**
     * Add new network interface to the VM.
     *
     * @param deviceId    the NIC id in the VM
     * @param plugged     is NIC plugged to the VM or not
     * @param hostDev     true if NIC is a host device, false if it is a bridge
     * @return  the device added
     */
    public VmDevice addInterface(Guid vmId, Guid deviceId, boolean plugged, boolean hostDev, String address) {
        return addManagedDevice(
            new VmDeviceId(deviceId, vmId),
            VmDeviceGeneralType.INTERFACE,
            hostDev ? VmDeviceType.HOST_DEVICE : VmDeviceType.BRIDGE,
            Collections.emptyMap(),
            plugged,
            false,
            address,
            null);
    }

    private boolean canPlugInterface(VmNic iface, VmBase vmBase) {
        ReadMacPool macPool = macPoolPerCluster.getMacPoolForCluster(vmBase.getClusterId());
        VmInterfaceManager vmIfaceManager = new VmInterfaceManager();

        if (vmIfaceManager.tooManyPluggedInterfaceWithSameMac(iface, macPool)) {
            vmIfaceManager.auditLogMacInUseUnplug(iface, vmBase.getName());
            return false;
        } else {
            return true;
        }
    }

    /*
     * USB controller
     */

    /**
     * Add given number of sets of USB controllers suitable for SPICE USB redirection to the VM.
     * For Q35 we ignore numberOfControllers and always create only one.
     */
    public void addSpiceUsbControllers(Guid vmId, ChipsetType chipset, int numberOfControllers) {
        if (chipset == ChipsetType.Q35) {
            addManagedDevice(
                    new VmDeviceId(Guid.newGuid(), vmId),
                    VmDeviceGeneralType.CONTROLLER,
                    VmDeviceType.USB,
                    createSingleUsbControllerSpecParams(XHCI_MODEL),
                    true,
                    false);

        } else {
            // For each EHCI controller we need to create companion UHCI controllers
            for (int index = 0; index < numberOfControllers; index++) {
                addManagedDevice(
                        new VmDeviceId(Guid.newGuid(), vmId),
                        VmDeviceGeneralType.CONTROLLER,
                        VmDeviceType.USB,
                        createSpiceUsbControllerSpecParams(EHCI_MODEL, 1, index),
                        true,
                        false);
                for (int companionIndex = 1; companionIndex <= COMPANION_USB_CONTROLLERS; companionIndex++) {
                    addManagedDevice(
                            new VmDeviceId(Guid.newGuid(), vmId),
                            VmDeviceGeneralType.CONTROLLER,
                            VmDeviceType.USB,
                            createSpiceUsbControllerSpecParams(UHCI_MODEL, companionIndex, index),
                            true,
                            false);
                }
            }
        }
    }

    private Map<String, Object> createSpiceUsbControllerSpecParams(String model, int controllerNumber, int index) {
        return createUsbControllerSpecParams(model + controllerNumber, index);
    }

    /**
     * Returns USB controller spec params.
     */
    private Map<String, Object> createUsbControllerSpecParams(String model, int index) {
        final Map<String, Object> specParams = new HashMap<>();
        specParams.put(VdsProperties.Model, model);
        specParams.put(VdsProperties.Index, Integer.toString(index));
        return specParams;
    }

    /**
     * Returns single USB controller spec params.
     */
    private Map<String, Object> createSingleUsbControllerSpecParams(String model) {
        final Map<String, Object> specParams = new HashMap<>();
        specParams.put(VdsProperties.Model, model);
        return specParams;
    }

    /**
     * Get list of all USB controllers in the VM.
     */
    public List<VmDevice> getUsbControllers(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.USB);
    }

    /**
     * Remove all USB controllers from the VM.
     */
    public void removeUsbControllers(Guid vmId) {
        removeVmDevices(getUsbControllers(vmId));
    }

    /*
     * USB slot
     */

    /**
     * Update USB slots and controllers in the new VM, if USB policy of the new VM differs from one of the old VM.
     * @param oldVm old configuration, may not be null, won't be modified
     * @param newVm new configuration, may not be null, only devices if this entity will be modified
     * @param newVmCluster new cluster
     */
    public void updateUsbSlots(VmBase oldVm, VmBase newVm, Cluster newVmCluster) {
        final UsbPolicy oldUsbPolicy = oldVm.getUsbPolicy();
        final UsbPolicy newUsbPolicy = newVm.getUsbPolicy();
        final int oldNumberOfSlots = getUsbSlots(oldVm.getId()).size();

        final int newNumberOfUsbSlots = Config.<Integer> getValue(ConfigValues.NumberOfUSBSlots);

        ChipsetType oldChipset = oldVm.getBiosType() == null ? null : oldVm.getBiosType().getChipsetType();
        ChipsetType newChipset = newVm.getBiosType() == null ? null : newVm.getBiosType().getChipsetType();

        if (UsbPolicy.DISABLED == newUsbPolicy && newVm.getVmType() == VmType.HighPerformance) {
            disableAnyUsb(oldVm, newVm);
            return;
        }
        if (UsbPolicy.DISABLED == oldUsbPolicy && UsbPolicy.ENABLED_NATIVE == newUsbPolicy) {
            disableNormalUsb(newVm.getId());
            enableSpiceUsb(newVm.getId(), newChipset, newNumberOfUsbSlots);
            return;
        }
        if (UsbPolicy.ENABLED_NATIVE == oldUsbPolicy && UsbPolicy.ENABLED_NATIVE == newUsbPolicy) {
            if (oldChipset != newChipset) {
                disableSpiceUsb(newVm.getId());
                enableSpiceUsb(newVm.getId(), newChipset, newNumberOfUsbSlots);
            } else {
                updateSpiceUsb(newVm.getId(), newChipset, oldNumberOfSlots, newNumberOfUsbSlots);
            }
            return;
        }

        if (UsbPolicy.ENABLED_NATIVE == oldUsbPolicy && UsbPolicy.DISABLED == newUsbPolicy) {
            disableSpiceUsb(newVm.getId());
            enableNormalUsb(newVm);
            return;
        }
        if (UsbPolicy.DISABLED == oldUsbPolicy && UsbPolicy.DISABLED == newUsbPolicy) {
            updateNormalUsb(newVm);
            return;
        }
        throw new RuntimeException(
                format("Unexpected state: oldUsbPolicy=%s, newUsbPolicy=%s", oldUsbPolicy, newUsbPolicy));
    }

    private void disableAnyUsb(VmBase oldVm, VmBase newVm) {
        if (UsbPolicy.DISABLED == oldVm.getUsbPolicy() && VmType.HighPerformance == oldVm.getVmType()
                && isUsbControllerDisabled(newVm.getId())) {
            return;
        }

        switch (oldVm.getUsbPolicy()) {
        case ENABLED_NATIVE:
            disableSpiceUsb(newVm.getId());
            break;

        case DISABLED:
            disableNormalUsb(newVm.getId());
            break;
        }

        addDisableUsbControllers(newVm.getId());
    }

    public boolean isUsbControllerDisabled(Guid vmId) {
        final List<VmDevice> usbControllers = getUsbControllers(vmId);
        return usbControllers != null && usbControllers.size() == 1
                && UsbControllerModel.NONE.libvirtName.equals(getUsbControllerModelName(usbControllers.get(0)));
    }

    public void addDisableUsbControllers(Guid vmId) {
        addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.USB,
                createUsbControllerSpecParams(UsbControllerModel.NONE.libvirtName, 0),
                true,
                false);
    }

    private void disableNormalUsb(Guid vmId) {
        removeUsbControllers(vmId);
    }

    private void enableSpiceUsb(Guid vmId, ChipsetType chipset, int newNumberOfUsbSlots) {
        if (newNumberOfUsbSlots > 0) {
            addSpiceUsbControllers(vmId, chipset, getNeededNumberOfUsbControllers(newNumberOfUsbSlots));
            addUsbSlots(vmId, newNumberOfUsbSlots);
        }
    }

    private void updateSpiceUsb(Guid vmId, ChipsetType chipset, int oldNumberOfSlots, int newNumberOfUsbSlots) {
        if (oldNumberOfSlots > newNumberOfUsbSlots) {
            // Remove slots and controllers
            removeUsbSlots(vmId, oldNumberOfSlots - newNumberOfUsbSlots);
            if (newNumberOfUsbSlots == 0) {
                removeUsbControllers(vmId);
            }
            return;
        }
        if (oldNumberOfSlots < newNumberOfUsbSlots) {
            // Add slots and controllers
            if (oldNumberOfSlots == 0) {
                // there may be remaining of unmanaged controllers from previous versions
                removeUsbControllers(vmId);
                addSpiceUsbControllers(vmId, chipset, getNeededNumberOfUsbControllers(newNumberOfUsbSlots));
            }
            addUsbSlots(vmId, newNumberOfUsbSlots - oldNumberOfSlots);
            return;
        }
    }

    private void disableSpiceUsb(Guid vmId) {
        removeUsbControllers(vmId);
        removeUsbSlots(vmId);
        removeUsbChannels(vmId);
    }

    private void enableNormalUsb(VmBase vmBase) {
        final UsbControllerModel controllerModel = getUsbControllerModel(vmBase);
        if (controllerModel == null) {
            return;
        }
        addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmBase.getId()),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.USB,
                createUsbControllerSpecParams(controllerModel.libvirtName, 0),
                true,
                false);
    }

    private void updateNormalUsb(VmBase vmBase) {
        final Collection<VmDevice> usbControllers = getUsbControllers(vmBase.getId());

        final List<VmDevice> unmanagedControllers = usbControllers.stream().filter(d -> !d.isManaged()).collect(Collectors.toList());
        final List<VmDevice> managedUsbControllers = usbControllers.stream().filter(VmDevice::isManaged).collect(Collectors.toList());

        if (unmanagedControllers.size() > 0) {
            acquireUnmanagedUsbController(vmBase, managedUsbControllers, unmanagedControllers);
            return;
        }

        final UsbControllerModel controllerModel = getUsbControllerModel(vmBase);

        if ((managedUsbControllers.isEmpty() && controllerModel == null)
            || (managedUsbControllers.size() == 1
                && controllerModel != null
                && controllerModel.libvirtName.equals(
                        getUsbControllerModelName(managedUsbControllers.get(0))))) {
            return;
        }

        disableNormalUsb(vmBase.getId());
        enableNormalUsb(vmBase);
    }

    /**
     * If there is an existing unmanaged usb controller, it drops all managed ones and acquires it.
     *
     * <p>This can be dropped together with support of USB controllers without specified model. Used till engine 3.6.
     * they may survive as part of long-running VMs and snapshots.</p>
     */
    private void acquireUnmanagedUsbController(
            VmBase vmBase,
            List<VmDevice> managedUsbControllers,
            List<VmDevice> unmanagedControllers) {
        if (unmanagedControllers.size() > 1) {
            throw new IllegalStateException(format("At most one unmanaged USB controller expected for VM=%s(%s), found=%s",
                    vmBase.getName(),
                    vmBase.getId(),
                    unmanagedControllers));
        }

        if (unmanagedControllers.isEmpty()) {
            return;
        }

        UsbControllerModel controllerModel = getUsbControllerModel(vmBase);

        // should not be here but due to https://bugzilla.redhat.com/1438188 can appear one
        // remove it
        removeVmDevices(managedUsbControllers);

        // has been created on pre 4.0 engine by VDSM, adopt it as ours
        VmDevice device = unmanagedControllers.iterator().next();

        CompensationUtils.<VmDeviceId, VmDevice>updateEntity(device, dev -> {
            dev.setManaged(true);
            dev.setPlugged(true);
            dev.setReadOnly(false);
            dev.setSpecParams(createUsbControllerSpecParams(controllerModel.libvirtName, 0));
        }, vmDeviceDao, compensationContext);
    }

    /**
     * @return usb controller model defined as defined in osinfo file for VM's OS, effective compatibility version
     * and chipset
     */
    /*
     * TODO: It would be cleaner to return a value denoting unknown model for instance type input since instance types
     * doesn't actually have any operating system set. Current solution works since no usb controller devices are
     * created for instance types.
     */
    private UsbControllerModel getUsbControllerModel(VmBase vmBase) {
        MemoizingSupplier<Cluster> clusterSupplier =
                new MemoizingSupplier<>(() -> getCluster(vmBase.getClusterId()));
        Version version = CompatibilityVersionUtils.getEffective(vmBase, clusterSupplier);
        ChipsetType chipset = vmBase.getBiosType() == null ? null : vmBase.getBiosType().getChipsetType();
        return osRepository.getOsUsbControllerModel(vmBase.getOsId(), version, chipset);
    }

    private String getUsbControllerModelName(VmDevice usbControllerDevice) {
        return (String) usbControllerDevice.getSpecParams().get(VdsProperties.Model);
    }

    /**
     * Returns number of USB controllers that needs to be created for the given number of USB slots.
     */
    private int getNeededNumberOfUsbControllers(int numberOfSlots) {
        int numberOfControllers = numberOfSlots / SLOTS_PER_CONTROLLER;
        // Need to add another controller if we have a remainder
        if (numberOfSlots % SLOTS_PER_CONTROLLER != 0) {
            numberOfControllers++;
        }
        return numberOfControllers;
    }

    /**
     * Add given number of USB slots to the VM
     */
    public void addUsbSlots(Guid vmId, int numberOfSlots) {
        for (int index = 1; index <= numberOfSlots; index++) {
            addManagedDevice(
                    new VmDeviceId(Guid.newGuid(), vmId),
                    VmDeviceGeneralType.REDIR,
                    VmDeviceType.SPICEVMC,
                    Collections.emptyMap(),
                    true,
                    false);
        }
    }

    /**
     * Get list of all USB slots in the VM or template.
     */
    public List<VmDevice> getUsbSlots(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.REDIR,
                VmDeviceType.SPICEVMC);
    }

    public List<VmDevice> getUsbChannels(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.REDIRDEV,
                VmDeviceType.SPICEVMC);
    }

    /**
     * Remove all USB slots from the VM.
     */
    public void removeUsbSlots(Guid vmId) {
        removeVmDevices(getUsbSlots(vmId));
    }

    /**
     * Remove all USB redir channels.
     */
    public void removeUsbChannels(Guid vmId) {
        removeVmDevices(getUsbChannels(vmId));
    }

    /**
     * Remove the given number of USB slots from the VM.
     */
    public void removeUsbSlots(Guid vmId, int numberOfSlotsToRemove) {
        removeVmDevices(getUsbSlots(vmId), numberOfSlotsToRemove);
    }

    /**
     * Remove unmanaged devices that are no longer necessary in the current configuration.
     */
    public void removeLeftOverDevices(VmBase vmBase) {
        if (!hasGraphicsDevice(vmBase.getId(), GraphicsType.SPICE)) {
            // remove spice channel if we are no longer using spice
            List<VmDevice> spiceChannels = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                    vmBase.getId(), VmDeviceGeneralType.CHANNEL,
                    VmDeviceType.SPICEVMC);
            removeVmDevices(spiceChannels);
        }
    }

    /*
     * Memory balloon device
     */

    /**
     * Enable memory balloon device in the VM.
     */
    public void addMemoryBalloonIfNeeded(Guid vmId) {
        if (!hasMemoryBalloon(vmId)) {
            addMemoryBalloon(vmId);
        }
    }

    /**
     * Add new memory balloon device to the VM.
     */
    public VmDevice addMemoryBalloon(Guid vmId) {
        return addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.BALLOON,
                VmDeviceType.MEMBALLOON,
                getMemoryBalloonSpecParams(),
                true,
                true);
    }

    private Map<String, Object> getMemoryBalloonSpecParams() {
        return Collections.singletonMap(VdsProperties.Model, VdsProperties.Virtio);
    }

    /**
     * Get list of all memory balloon devices in the VM.
     */
    public List<VmDevice> getMemoryBalloons(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.BALLOON);
    }

    /**
     * Check if the VM has a memory balloon device.
     */
    public boolean hasMemoryBalloon(Guid vmId) {
        return vmDeviceDao.isMemBalloonEnabled(vmId);
    }

    /*
     * RNG device
     */

    /**
     * Get list of all RNG devices in the VM.
     */
    public List<VmDevice> getRngDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.RNG);
    }

    /**
     * Check if the VM has an RNG device.
     */
    public boolean hasRngDevice(Guid vmId) {
        return !getRngDevices(vmId).isEmpty();
    }

    /*
     * Graphics device
     */

    /**
     * Get list of graphics device types present in the VM.
     */
    public List<GraphicsType> getGraphicsTypesOfEntity(Guid entityId) {
        List<GraphicsType> result = new ArrayList<>();

        if (entityId != null) {
            List<VmDevice> devices = getGraphicsDevices(entityId);
            if (devices != null) {
                result.addAll(devices.stream().map(device -> GraphicsType.fromString(device.getDevice())).collect(Collectors.toList()));
            }
        }

        return result;
    }

    /**
     * Get list of all graphics devices in the VM.
     */
    public List<VmDevice> getGraphicsDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.GRAPHICS);
    }

    /**
     * Get list of all memory devices in the VM.
     */
    public List<VmDevice> getMemoryDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.MEMORY);
    }

    /**
     * Get list of graphics devices of the given type present in the VM.
     */
    public List<VmDevice> getGraphicsDevices(Guid vmId, GraphicsType type) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.GRAPHICS,
                type.name().toLowerCase());
    }

    /**
     * Check if the VM has a graphics device of the given type.
     */
    public boolean hasGraphicsDevice(Guid vmId, GraphicsType type) {
        return !getGraphicsDevices(vmId, type).isEmpty();
    }

    /*
     * Watchdog
     */

    /**
     * Add new watchdog device to the VM.
     *
     * @param vmId the ID of the VM to add the watchdog device to
     * @param specParams the spec params of the watchdog device
     * @return the added device
     */
    public VmDevice addWatchdogDevice(Guid vmId, Map<String, Object> specParams) {
        return addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.WATCHDOG,
                VmDeviceType.WATCHDOG,
                specParams,
                true,
                false);
    }

    /**
     * Get list of all watchdogs in the VM.
     */
    public List<VmDevice> getWatchdogs(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.WATCHDOG);
    }

    /**
     * Check if the VM has a watchdog.
     */
    public boolean hasWatchdog(Guid vmId) {
        return !getWatchdogs(vmId).isEmpty();
    }

    /*
     * Disk device
     */

    /**
     * Copy disk devices from the given list of devices to the destination VM. Device ids are changed in accordance
     * with the mapping given.
     */
    public void copyDiskDevices(Guid dstId,
            List<VmDevice> srcDevices,
            Map<Guid, Guid> srcDeviceIdToDstDeviceIdMapping) {
        for (VmDevice device : srcDevices) {
            if (VmDeviceType.DISK.getName().equals(device.getDevice())) {
                if (srcDeviceIdToDstDeviceIdMapping.containsKey(device.getDeviceId())) {
                    Guid dstDeviceId = srcDeviceIdToDstDeviceIdMapping.get(device.getDeviceId());
                    device.setId(new VmDeviceId(dstDeviceId, dstId));
                    device.setSpecParams(Collections.emptyMap());
                    CompensationUtils.saveEntity(device, vmDeviceDao, compensationContext);
                }
            }
        }
    }

    /**
     * Add a new disk device to the VM.
     */
    public VmDevice addDiskDevice(Guid vmId, Guid deviceId) {
        return addDiskDevice(vmId, deviceId, true, false, "");
    }

    /**
     * Add a new disk device to the VM.
     */
    public VmDevice addDiskDevice(Guid vmId, Guid deviceId, String address) {
        return addDiskDevice(vmId, deviceId, true, false, address);
    }

    /**
     * Add a new disk device to the VM.
     */
    public VmDevice addDiskDevice(Guid vmId, Guid deviceId, Boolean isPlugged, Boolean isReadOnly) {
        return addDiskDevice(vmId, deviceId, isPlugged, isReadOnly, "");
    }

    /**
     * Add a new disk device to the VM.
     */
    public VmDevice addDiskDevice(Guid vmId, Guid deviceId, Boolean isPlugged, Boolean isReadOnly,
                                         String address) {
        return addManagedDevice(
                new VmDeviceId(deviceId, vmId),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK,
                Collections.emptyMap(),
                isPlugged,
                isReadOnly,
                address,
                null);
    }

    /**
     * Gets a set of disks from disk images. For VM with snapshots, several DiskImage elements may contain
     * the same Disk.
     *
     * @param diskImages collection of DiskImage objects to get the set of Disks from
     * @return the resulting set of disks
     */
    public Set<BaseDisk> getDisks(Collection<DiskImage> diskImages) {
        Map<Guid, BaseDisk> diskMap = new HashMap<>();
        for (Disk diskImage : diskImages) {
            diskMap.put(diskImage.getId(), diskImage);
        }
        return new HashSet<>(diskMap.values());
    }

    /*
     * Generic device methods
     */

    /**
     * Get address of the given managed device in the VM. If the device does not exist, returns empty string.
     */
    public String getVmDeviceAddress(VmBase vmBase, final Guid deviceId) {
        VmDevice device = vmBase.getManagedDeviceMap().get(deviceId);
        if (device != null) {
            return device.getAddress();
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Remove all devices in the list.
     */
    public void removeVmDevices(List<VmDevice> devices) {
        CompensationUtils.removeEntities(devices, vmDeviceDao, compensationContext);
    }

    /**
     * Remove all devices addresses in the list.
     */
    public void removeVmDevicesAddress(Guid vmId) {
        List<VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(vmId);
        for (VmDevice device : devices) {
            CompensationUtils.<VmDeviceId, VmDevice>updateEntity(device, dev -> {
                device.setAddress("");
            }, vmDeviceDao, compensationContext);
        }
    }

    public void resetVmDevicesHash(Guid vmId) {
        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(System.nanoTime());
        change.updateVm(vmId, VmDevicesMonitoring.EMPTY_HASH);
        change.flush();
    }

    /**
     * Remove the given number of devices starting from the end of the list.
     */
    public void removeVmDevices(List<VmDevice> devices, int numberOfDevicesToRemove) {
        int size = devices.size();
        removeVmDevices(devices.subList(Math.max(size - numberOfDevicesToRemove, 0), size));
    }

    /**
     * Read devices from the DB and set managed and unmanaged device lists in VmBase.
     */
    public void setVmDevices(VmBase vmBase) {
        List<VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(vmBase.getId());
        vmBase.setUnmanagedDeviceList(vmDeviceDao.getUnmanagedDevicesByVmId(vmBase.getId()));
        Map<Guid, VmDevice> vmManagedDeviceMap = new HashMap<>();
        for (VmDevice device : devices) {
            if (device.isManaged()) {
                vmManagedDeviceMap.put(device.getDeviceId(), device);
            }
        }
        vmBase.setManagedDeviceMap(vmManagedDeviceMap);
    }

    /**
     * Check if the given device is disk or network interface device.
     */
    private boolean isDiskOrInterface(VmDevice vmDevice) {
        return VmDeviceCommonUtils.isDisk(vmDevice) || VmDeviceCommonUtils.isNetwork(vmDevice);
    }

    /**
     * Update the VM devices according to the changes made.
     *
     * @param params    changes made
     * @param oldVm     previous state of the VM being modified
     */
    public void updateVmDevices(VmManagementParametersBase params, VM oldVm) {
        VmBase oldVmBase = oldVm.getStaticData();
        VmBase newVmBase = params.getVmStaticData();
        if (newVmBase == null) {
            return;
        }

        final Cluster oldVmCluster = getCluster(oldVmBase.getClusterId());
        final Cluster newVmCluster = getCluster(newVmBase.getClusterId(), oldVmCluster);

        updateCdPath(oldVmBase, newVmBase);
        updateVideoDevices(oldVmBase, newVmBase);
        updateUsbSlots(oldVmBase, newVmBase, newVmCluster);
        addMemoryBalloonIfNeeded(newVmBase.getId());
        updateSoundDevice(oldVmBase, newVmBase, oldVm.getCompatibilityVersion(),
                params.isSoundDeviceEnabled());
        updateTpmDevice(newVmBase, newVmCluster, params.isTpmEnabled());
        updateSmartcardDevice(oldVm, newVmBase);
        updateConsoleDevice(newVmBase.getId(), params.isConsoleEnabled());
        updateVirtioScsiController(newVmBase, params.isVirtioScsiEnabled());
    }

    /**
     * Update devices information in {@link VmManagementParametersBase}
     *
     * @param params destination parameters
     * @param devices list of devices
     */
    public void updateVmDevicesInParameters(VmManagementParametersBase params, List<VmDevice> devices) {
        params.setSoundDeviceEnabled(containsDeviceWithType(devices, VmDeviceGeneralType.SOUND));
        params.setConsoleEnabled(containsDeviceWithType(devices, VmDeviceGeneralType.CONSOLE));
        params.setVirtioScsiEnabled(containsDeviceWithType(devices, VmDeviceGeneralType.CONTROLLER, VmDeviceType.VIRTIOSCSI));

        updateVmGraphicDevicesInParameters(params, devices);
        updateWatchdogInParameters(params, devices);
        updateRngInParameters(params, devices);
    }

    /**
     * Update watchdog information in {@link VmManagementParametersBase}
     * @param params destination parameters
     * @param devices list of devices
     */
    private void updateWatchdogInParameters(VmManagementParametersBase params, List<VmDevice> devices) {
        VmDevice watchDogDevice = getFirstDeviceWithType(devices, VmDeviceGeneralType.WATCHDOG, null);
        params.setUpdateWatchdog(true);
        if (watchDogDevice != null) {
            params.setWatchdog(new VmWatchdog(watchDogDevice));
        }
    }

    /**
     * Update RNG information in {@link VmManagementParametersBase}
     * @param params destination parameters
     * @param devices list of devices
     */
    private void updateRngInParameters(VmManagementParametersBase params, List<VmDevice> devices) {
        VmDevice rngDevice = getFirstDeviceWithType(devices, VmDeviceGeneralType.RNG, null);
        params.setUpdateRngDevice(true);
        if (rngDevice != null) {
            params.setRngDevice(new VmRngDevice(rngDevice));
        }
    }

    /**
     * Update graphic devices information in {@link VmManagementParametersBase}
     * @param params destination parameters
     * @param devices list of devices
     */
    private void updateVmGraphicDevicesInParameters(VmManagementParametersBase params, List<VmDevice> devices) {
        for (GraphicsType graphicsType : GraphicsType.values()) {
            params.getGraphicsDevices().put(graphicsType, null); // prevent copying from the template
        }

        for (VmDevice device : devices) {
            if (device.getType() == VmDeviceGeneralType.GRAPHICS) {
                GraphicsDevice graphicsDevice = new GraphicsDevice(device);
                params.getGraphicsDevices().put(graphicsDevice.getGraphicsType(), graphicsDevice);
            }
        }
    }

    /**
     * Update the VM devices according to changes made in configuration.
     *
     * This method is executed before running the VM.
     */
    public void updateVmDevicesOnRun(VM vm) {
        if (vm != null) {
            Cluster cluster = getCluster(vm.getClusterId());
            updateUsbSlots(vm.getStaticData(), vm.getStaticData(), cluster);
            removeLeftOverDevices(vm.getStaticData());
            updateRngDevice(vm);
        }
    }

    private void updateRngDevice(VM vm) {
        // is it random and should be urandom?
        final Optional<VmRngDevice> rngDevices = getRngDevices(vm.getId())
                .stream()
                .map(VmRngDevice::new)
                .findFirst();

        if (!rngDevices.isPresent()) {
            return;
        }

        Optional<VmRngDevice> rngDeviceToUpdate =
                rngDeviceUtils.updateRngDevice(vm.getCompatibilityVersion(), rngDevices.get());

        if (!rngDeviceToUpdate.isPresent()) {
            return;
        }

        vmDeviceDao.update(rngDeviceToUpdate.get());
    }

    public void convertVmDevicesToNewChipset(Guid vmId, ChipsetType newChipsetType, boolean useCompensation) {
        removeUnmanagedDevices(vmId, useCompensation);
        convertVmDevicesToNewChipsetInternal(vmId, newChipsetType, useCompensation);
        resetVmDevicesHash(vmId);
    }

    private void convertVmDevicesToNewChipsetInternal(Guid vmId, ChipsetType chipset, boolean useCompensation) {
        log.info("Converting all devices for VM with id {} to new chipset {}", vmId, chipset);
        List<VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(vmId);
        for (VmDevice device : devices) {
            if (useCompensation) {
                CompensationUtils.<VmDeviceId, VmDevice> updateEntity(device, dev -> {
                    convertVmDeviceToNewChipsetInternal(dev, chipset);
                }, vmDeviceDao, compensationContext);
            } else {
                convertVmDeviceToNewChipsetInternal(device, chipset);
            }
        }

        if (!useCompensation) {
            vmDeviceDao.updateAllInBatch(devices);
        }
    }

    private void convertVmDeviceToNewChipsetInternal(VmDevice device, ChipsetType newChipsetType) {
        device.setAddress("");
        // note that a chipset change is handled for sound and usb in updateSoundDevice and updateUsbSlots methods
    }

    private void removeUnmanagedDevices(Guid vmId, boolean useCompensation) {
        log.info("Removing all unmanaged devices for VM with id: {}", vmId);
        if (useCompensation) {
            List<VmDevice> devices = vmDeviceDao.getUnmanagedDevicesByVmId(vmId);
            CompensationUtils.<VmDeviceId, VmDevice> removeEntities(devices, vmDeviceDao, compensationContext);
        } else {
            vmDeviceDao.removeAllUnmanagedDevicesByVmId(vmId);
        }
    }

    /**
     * Copy devices from the given VmDevice list to the destination VM/VmBase.
     */
    public void copyVmDevices(Guid srcId,
                              Guid dstId,
                              VmBase srcVmBase,
                              VmBase dstVmBase,
                              List<VmDevice> srcDevices,
                              Map<Guid, Guid> srcDeviceIdToDstDeviceIdMapping,
                              boolean isSoundEnabled,
                              Boolean isTpmEnabled,
                              Boolean isConsoleEnabled,
                              Boolean isVirtioScsiEnabled,
                              Set<GraphicsType> graphicsToSkip,
                              boolean copySnapshotDevices,
                              boolean copyHostDevices,
                              Version versionToUpdateRngDeviceWith) {
        if (graphicsToSkip == null) {
            graphicsToSkip = Collections.emptySet();
        }

        String dstCdPath = dstVmBase.getIsoPath();
        boolean dstIsVm = !(dstVmBase instanceof VmTemplate);
        boolean hasCd = hasCdDevice(dstVmBase.getId());
        boolean hasSound = false;
        boolean hasTpm = false;
        boolean hasConsole = false;
        boolean hasVirtioScsi = false;
        boolean hasBalloon = false;
        boolean hasRng = hasRngDevice(dstId);

        final Cluster srcCluster = getCluster(srcVmBase.getClusterId());
        final Cluster dstCluster = getCluster(dstVmBase.getClusterId(), srcCluster);

        for (VmDevice device : srcDevices) {
            if (device.getSnapshotId() != null && !copySnapshotDevices) {
                continue;
            }

            Guid deviceId = Guid.newGuid();
            Map<String, Object> specParams = new HashMap<>();

            switch (device.getType()) {
                case DISK:
                    if (VmDeviceType.DISK.getName().equals(device.getDevice())) {
                        if (srcDeviceIdToDstDeviceIdMapping.containsKey(device.getDeviceId())) {
                            deviceId = srcDeviceIdToDstDeviceIdMapping.get(device.getDeviceId());
                        }
                    } else if (VmDeviceType.CDROM.getName().equals(device.getDevice())) {
                        if (!hasCd) {
                            hasCd = true;
                            // check here is source VM had CD (VM from snapshot)
                            String srcCdPath = (String) device.getSpecParams().get(VdsProperties.Path);
                            specParams.putAll(getCdDeviceSpecParams(srcCdPath, dstCdPath));
                        } else { // CD already exists
                            continue;
                        }
                    }
                    break;

                case INTERFACE:
                    if (srcDeviceIdToDstDeviceIdMapping.containsKey(device.getDeviceId())) {
                        deviceId = srcDeviceIdToDstDeviceIdMapping.get(device.getDeviceId());
                    }
                    break;

                case CONTROLLER:
                    if (VmDeviceType.USB.getName().equals(device.getDevice())) {
                        specParams = device.getSpecParams();
                    } else if (VmDeviceType.PCI.getName().equals(device.getDevice())) {
                        specParams = device.getSpecParams();
                    } else if (VmDeviceType.VIRTIOSCSI.getName().equals(device.getDevice())) {
                        hasVirtioScsi = true;
                        if (Boolean.FALSE.equals(isVirtioScsiEnabled)) {
                            continue;
                        }
                    }
                    break;

                case VIDEO:
                    if (dstIsVm) {
                        // Source is template and target is VM. Video devices will be created according
                        // to the new Vm params.
                        continue;
                    }
                    specParams.putAll(getVideoDeviceSpecParams(dstVmBase));
                    break;

                case BALLOON:
                    hasBalloon = true;
                    specParams.putAll(getMemoryBalloonSpecParams());
                    break;

                case SMARTCARD:
                    specParams.putAll(getSmartcardDeviceSpecParams());
                    break;

                case WATCHDOG:
                    specParams.putAll(device.getSpecParams());
                    break;

                case RNG:
                    if (hasRng) {
                        continue;
                    }
                    if (!new VirtIoRngValidator().canAddRngDevice(
                            dstCluster, new VmRngDevice(device)).isValid()) {
                        continue;
                    }
                    final VmRngDevice rngDevice = new VmRngDevice(device);
                    if (versionToUpdateRngDeviceWith != null) {
                        rngDevice.updateSourceByVersion(versionToUpdateRngDeviceWith);
                    }
                    specParams.putAll(rngDevice.getSpecParams());
                    break;

                case CONSOLE:
                    if (!Boolean.TRUE.equals(isConsoleEnabled)) {
                        continue;
                    }
                    specParams.putAll(device.getSpecParams());
                    hasConsole = true;
                    break;

                case SOUND:
                    if (!isSoundEnabled) {
                        continue;
                    }
                    hasSound = true;
                    break;

                case TPM:
                    if (Boolean.FALSE.equals(isTpmEnabled)) {
                        continue;
                    }
                    hasTpm = true;
                    break;

                case GRAPHICS:
                    GraphicsType type = GraphicsType.fromVmDeviceType(VmDeviceType.getByName(device.getDevice()));
                    // don't add device from the template if it should be skipped (i.e. it's overridden in params)
                    // OR if we already have it
                    if (graphicsToSkip.contains(type) ||
                            hasGraphicsDevice(dstId, GraphicsType.fromString(device.getDevice()))) {
                        continue;
                    }
                    break;

                case HOSTDEV:
                    if (!copyHostDevices) {
                        continue;
                    }
                    specParams.putAll(device.getSpecParams());
                    break;

                default:
                    break;
            }
            device.setId(new VmDeviceId(deviceId, dstId));
            device.setSpecParams(specParams);
            vmDeviceDao.save(device);
        }

        if (!hasCd) {
            addCdDevice(dstId, dstCdPath);
        }

        updateUsbSlots(srcVmBase, dstVmBase, dstCluster);

        if (isSoundEnabled) {
            if (hasSound) {
                updateSoundDevice(srcVmBase, dstVmBase, CompatibilityVersionUtils.getEffective(dstVmBase,  dstCluster), isSoundEnabled);
            } else {
                addSoundDevice(dstVmBase, () -> dstCluster);
            }
        }

        if (Boolean.TRUE.equals(isTpmEnabled) && !hasTpm) {
            addTpmDevice(dstId);
        }

        if (Boolean.TRUE.equals(isConsoleEnabled) && !hasConsole) {
            addConsoleDevice(dstId);
        }

        if (Boolean.TRUE.equals(isVirtioScsiEnabled) && !hasVirtioScsi) {
            addVirtioScsiController(dstVmBase, getVmCompatibilityVersion(dstVmBase));
        }

        if (!hasBalloon) {
            addMemoryBalloon(dstId);
        }

        if (dstIsVm) {
            addVideoDevices(dstVmBase, getNeededNumberOfVideoDevices(dstVmBase));
        }
    }

    /**
     * Copy devices from the source to the destination VM.
     */
    public void copyVmDevices(Guid srcId,
                                     Guid dstId,
                                     Map<Guid, Guid> srcDeviceIdToDstDeviceIdMapping,
                                     boolean isSoundEnabled,
                                     Boolean isTpmEnabled,
                                     Boolean isConsoleEnabled,
                                     Boolean isVirtioScsiEnabled,
                                     Set<GraphicsType> graphicsToSkip,
                                     boolean copySnapshotDevices,
                                     Version versionToUpdateRndDeviceWith) {

        VmBase srcVmBase = getVmBase(srcId);
        VmBase dstVmBase = getVmBase(dstId);
        List<VmDevice> srcDevices = vmDeviceDao.getVmDeviceByVmId(srcId);

        copyVmDevices(srcId, dstId, srcVmBase, dstVmBase, srcDevices, srcDeviceIdToDstDeviceIdMapping,
                isSoundEnabled, isTpmEnabled, isConsoleEnabled, isVirtioScsiEnabled,
                graphicsToSkip, copySnapshotDevices, canCopyHostDevices(srcVmBase, dstVmBase),
                versionToUpdateRndDeviceWith);
    }


    /** @see #canCopyHostDevices(VmBase, VmBase) */
    public boolean canCopyHostDevices(Guid srcId, VmBase dstVm) {
        return canCopyHostDevices(getVmBase(srcId), dstVm);
    }

    /**
     * Determines whether it is safe to copy host devices from source VM/Template to destination.
     * More specifically it checks that no discrepancy between source and destination 'dedicatedVmForVds' occurred,
     * since host device consistency depends on this value.
     */
    public boolean canCopyHostDevices(VmBase srcVm, VmBase dstVm) {
        return new HashSet<>(srcVm.getDedicatedVmForVdsList()).equals(new HashSet<>(dstVm.getDedicatedVmForVdsList()));
    }

    /**
     * Returns VmBase object regardless if passed ID is of VM or Template.
     *
     * @param vmId ID of a VM or Template
     * @return VmStatic if a VM of given ID was found, VmTemplate otherwise.
     */
    private VmBase getVmBase(Guid vmId) {
        VmStatic vmStatic = vmStaticDao.get(vmId);
        return vmStatic != null ? vmStatic : vmTemplateDao.get(vmId);
    }

    private Cluster getCluster(Guid clusterId) {
        return clusterId != null ? clusterDao.get(clusterId) : null;
    }

    private Cluster getCluster(Guid clusterId, Cluster cluster) {
        return cluster != null && clusterId == cluster.getId() ? cluster : getCluster(clusterId);
    }

    private Version getVmCompatibilityVersion(VmBase base) {
        if (base.getCustomCompatibilityVersion() != null) {
            return base.getCustomCompatibilityVersion();
        }
        if (base.getClusterId() != null) {
            return getCluster(base.getClusterId()).getCompatibilityVersion();
        }
        return Version.getLast();
    }

    /**
     * Create new managed device.
     *
     * @param id            device id
     * @param generalType   device general type
     * @param type          device type
     * @param specParams    device spec params
     * @param isPlugged     is device plugged-in
     * @param isReadOnly    is device read-only
     * @return      newly created VmDevice instance
     */
    public VmDevice addManagedDevice(VmDeviceId id,
                                            VmDeviceGeneralType generalType,
                                            VmDeviceType type,
                                            Map<String, Object> specParams,
                                            boolean isPlugged,
                                            Boolean isReadOnly) {
        return addManagedDevice(id, generalType, type, specParams, isPlugged, isReadOnly, "", null);
    }

    /**
     * Create new managed device.
     *
     * @param id            device id
     * @param generalType   device general type
     * @param type          device type
     * @param specParams    device spec params
     * @param isPlugged     is device plugged-in
     * @param isReadOnly    is device read-only
     * @param address       device address
     * @param customProps   device custom properties
     * @param isUsingScsiReservation    is device using SCSI reservation
     * @return      newly created VmDevice instance
     */
    public VmDevice addManagedDevice(VmDeviceId id,
                                            VmDeviceGeneralType generalType,
                                            VmDeviceType type,
                                            Map<String, Object> specParams,
                                            Boolean isPlugged,
                                            Boolean isReadOnly,
                                            String address,
                                            Map<String, String> customProps) {
        VmDevice managedDevice =
                new VmDevice(
                        id,
                        generalType,
                        type.getName(),
                        StringUtils.isNotBlank(address) ? address : "",
                        specParams,
                        true,
                        isPlugged,
                        isReadOnly,
                        "",
                        customProps,
                        null,
                        null);
        CompensationUtils.saveEntity(managedDevice, vmDeviceDao, compensationContext);

        return managedDevice;
    }

    /**
     * Add devices to an imported VM or template.
     *
     * @param withMemory true ~ VM is imported from snapshot with memory, false otherwise
     */
    public void addImportedDevices(VmBase vmBase, boolean isImportAsNewEntity, boolean withMemory) {
        if (isImportAsNewEntity) {
            setNewIdInImportedCollections(vmBase);
        }

        List<VmDevice> vmDevicesToAdd = new ArrayList<>();
        List<VmDevice> vmDevicesToUpdate = new ArrayList<>();

        addImportedDiskDevices(vmBase, vmDevicesToUpdate);
        addImportedInterfaces(vmBase, vmDevicesToUpdate);
        addImportedOtherDevices(vmBase, vmDevicesToAdd, withMemory);

        vmDeviceDao.saveAll(vmDevicesToAdd);
        vmDeviceDao.updateAll(vmDevicesToUpdate);
    }

    /**
     * Add disk devices to an imported VM or template.
     *
     * @param vmDevicesToUpdate    list of devices to be updated in the DB
     */
    private void addImportedDiskDevices(VmBase vmBase, List<VmDevice> vmDevicesToUpdate) {
        final Guid vmId = vmBase.getId();
        for (BaseDisk disk : getDisks(vmBase.getImages())) {
            Guid deviceId = disk.getId();
            VmDevice vmDevice = addDiskDevice(vmId, deviceId, getVmDeviceAddress(vmBase, vmId));
            updateImportedVmDevice(vmBase, vmDevice, deviceId, vmDevicesToUpdate);
        }
    }

    /**
     * Add network interfaces to an imported VM or template.
     *
     * @param vmDevicesToUpdate    list of devices to be updated in the DB
     */
    private void addImportedInterfaces(VmBase vmBase, List<VmDevice> vmDevicesToUpdate) {

        for (VmNic iface : vmBase.getInterfaces()) {
            Guid deviceId = iface.getId();
            VmDevice vmDevice = addInterface(vmBase.getId(), deviceId, true, iface.isPassthrough(),
                    getVmDeviceAddress(vmBase, deviceId));

            VmDevice exportedDevice = vmBase.getManagedDeviceMap().get(deviceId);
            if (exportedDevice == null) {
                vmBase.getManagedDeviceMap().put(deviceId, vmDevice);
                exportedDevice = vmDevice;
            }

            exportedDevice.setPlugged(exportedDevice.isPlugged() && canPlugInterface(iface, vmBase));
            updateImportedVmDevice(vmBase, vmDevice, deviceId, vmDevicesToUpdate);
        }
    }

    /**
     * Add other managed and unmanaged devices to imported VM or template.
     *
     * @param vmDeviceToAdd list of devices to be added to the DB
     * @param withMemory true ~ devices are being added to a VM that is imported from snapshot with memory
     */
    private void addImportedOtherDevices(VmBase vmBase, List<VmDevice> vmDeviceToAdd, boolean withMemory) {
        boolean hasCd = false;

        for (VmDevice vmDevice : vmBase.getManagedDeviceMap().values()) {
            switch (vmDevice.getType()) {
                case DISK:
                    if (VmDeviceType.CDROM.getName().equals(vmDevice.getDevice())) {
                        hasCd = true;
                    } else {
                        // disks are added separately
                        continue;
                    }
                    break;

                case INTERFACE:
                    // network interfaces are added separately
                    continue;

                case VIDEO:
                    vmDevice.setSpecParams(getVideoDeviceSpecParams(vmBase));
                    vmDevice.setPlugged(true);
                    break;

                case GRAPHICS:
                case CONSOLE:
                    vmDevice.setPlugged(true);
                    break;

                case HOSTDEV:
                    if (isPinngedToSingleValidHost(vmBase)) {
                        vmDevice.setPlugged(true);
                        break;
                    }
                    continue;

            }
            vmDevice.setManaged(true);
            vmDeviceToAdd.add(vmDevice);
        }

        if (!hasCd) { // add an empty CD
            addCdDevice(vmBase.getId());
        }

        // add unmanaged devices

        final List<VmDevice> unmanagedDevicesToAdd = vmBase.getUnmanagedDeviceList().stream()
                // preserve memory device only if importing from snapshot with memory
                .filter(device -> !VmDeviceCommonUtils.isMemory(device) || withMemory)
                .collect(Collectors.toList());
        vmDeviceToAdd.addAll(unmanagedDevicesToAdd);
    }

    /**
     * Checks if the VM has one dedicated host when having host device attached to it.
     *
     * @param vmBase The VM we check.
     * @return true, if the VM has the dedicated host in the cluster, and only pinned to that host.
     */
    private boolean isPinngedToSingleValidHost(VmBase vmBase) {
        return vmBase.getDedicatedVmForVdsList().size() == 1
                && vmHandler.validateDedicatedVdsExistOnSameCluster(vmBase).isValid();
    }

    /**
     * Set common properties in a device for imported VM or template and add the device
     * to the list of devices to be updated in the DB.
     *
     * @param vmDevicesToUpdate    list of devices to be updated in the DB
     */
    private void updateImportedVmDevice(VmBase vmBase,
                                               VmDevice vmDevice,
                                               Guid deviceId,
                                               List<VmDevice> vmDevicesToUpdate) {
        VmDevice exportedDevice = vmBase.getManagedDeviceMap().get(deviceId);
        if (exportedDevice != null) {
            vmDevice.setAddress(exportedDevice.getAddress());
            vmDevice.setPlugged(exportedDevice.isPlugged());
            vmDevice.setReadOnly(exportedDevice.getReadOnly());
            vmDevicesToUpdate.add(vmDevice);
        }
    }

    /**
     * Set newly generated ids for all devices in the VM, except disks and network interfaces.
     */
    private void setNewIdInImportedCollections(VmBase vmBase) {
        for (VmDevice managedDevice : vmBase.getManagedDeviceMap().values()) {
            if (!isDiskOrInterface(managedDevice)) {
                managedDevice.setId(new VmDeviceId(Guid.newGuid(), vmBase.getId()));
            }
        }
        for (VmDevice unmanagedDevice : vmBase.getUnmanagedDeviceList()) {
            unmanagedDevice.setId(new VmDeviceId(Guid.newGuid(), vmBase.getId()));
        }
    }

    /**
     * Determines whether a VM device change has been requested by the user.
     *
     * @param deviceGeneralType VmDeviceGeneralType.
     * @param deviceTypeName VmDeviceType name.
     * @param deviceEnabled indicates whether the user asked to enable the device.
     * @return true if a change has been requested; otherwise, false
     */
    public boolean vmDeviceChanged(Guid vmId, VmDeviceGeneralType deviceGeneralType, String deviceTypeName,
                                          boolean deviceEnabled) {
        List<VmDevice> vmDevices = deviceTypeName != null ?
                vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(vmId, deviceGeneralType, deviceTypeName):
                vmDeviceDao.getVmDeviceByVmIdAndType(vmId, deviceGeneralType);

        return deviceEnabled == vmDevices.isEmpty();
    }

    /**
     * Determines whether a VM device change has been requested by the user.
     *
     * @param deviceGeneralType VmDeviceGeneralType.
     * @param deviceTypeName VmDeviceType name.
     * @param device device object provided by user
     * @return true if a change has been requested; otherwise, false
     */
    public boolean vmDeviceChanged(Guid vmId, VmDeviceGeneralType deviceGeneralType, String deviceTypeName,
                                          VmDevice device) {
        List<VmDevice> vmDevices = deviceTypeName != null ?
                vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(vmId, deviceGeneralType, deviceTypeName):
                vmDeviceDao.getVmDeviceByVmIdAndType(vmId, deviceGeneralType);

        if (device == null) {
            return !vmDevices.isEmpty();
        }
        if (vmDevices.isEmpty()) { // && device != null
            return true;
        }
        if (device.getSpecParams() != null) { // if device.getSpecParams() == null, it is not used for comparison
            for (VmDevice vmDevice : vmDevices) {
                if (!vmDevice.getSpecParams().equals(device.getSpecParams())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns a map (device ID to VmDevice) of devices that are relevant for next run.
     *
     * The list of devices is built by examining properties that are annotated with EditableDeviceOnVmStatusField
     * annotation.
     *
     * @param vm the relevant VM
     * @param objectWithEditableDeviceFields object that contains properties which are annotated with
     *                                       EditableDeviceOnVmStatusField (e.g. parameters file)
     * @param vmVideoDeviceTypeforNextRun VM video device type for next run
     * @return a map of device ID to VmDevice
     */
    public Map<Guid, VmDevice> getVmDevicesForNextRun(VM vm, Object objectWithEditableDeviceFields, DisplayType vmVideoDeviceTypeforNextRun) {
        setVmDevices(vm.getStaticData());
        Map<Guid, VmDevice> vmManagedDeviceMap = vm.getManagedVmDeviceMap();

        List<VmDeviceUpdate> fieldList =
                vmHandler.getVmDevicesFieldsToUpdateOnNextRun(vm.getId(), vm.getStatus(), objectWithEditableDeviceFields);

        // Add the enabled devices and remove the disabled ones
        for (VmDeviceUpdate update : fieldList) {
            if (update.isEnable()) {
                VmDevice device;
                if (update.getDevice() == null) {
                    device = new VmDevice(
                                    new VmDeviceId(Guid.newGuid(), vm.getId()),
                                    update.getGeneralType(),
                                    update.getType().getName(),
                                    "",
                                    Collections.emptyMap(),
                                    true,
                                    true,
                                    update.isReadOnly(),
                                    "",
                                    null,
                                    null,
                                    null);
                } else {
                    device = update.getDevice();
                    if (device.getVmId() == null) {
                        device.setVmId(vm.getId());
                    }
                    if (device.getDeviceId() == null) {
                        if (isSingletonDevice(device)) {
                            // only one singleton device is allowed per VM
                            VmDevice mapDevice = VmDeviceCommonUtils.findVmDeviceByType(vmManagedDeviceMap, update.getType());
                            device.setDeviceId(mapDevice != null ? mapDevice.getDeviceId() : Guid.newGuid());
                        } else {
                            device.setDeviceId(Guid.newGuid());
                        }
                    }
                }

                vmManagedDeviceMap.put(device.getDeviceId(), device);
            } else {
                VmDevice device;
                if (update.getType() != VmDeviceType.UNKNOWN) {
                    device = VmDeviceCommonUtils.findVmDeviceByType(vmManagedDeviceMap, update.getType());
                } else {
                    device = VmDeviceCommonUtils.findVmDeviceByGeneralType(vmManagedDeviceMap, update.getGeneralType());
                }
                if (device != null) {
                    vmManagedDeviceMap.remove(device.getDeviceId());
                }
            }
        }

        // @TODO - this was added to handle the headless VM since the VIDEO devices were added anyway with the DB value instead of the
        // new configuration value. Should be handled correctly while the task of removing the static.displaytype and handling the VIDEO device
        // as all other devices will be done
        if (vmVideoDeviceTypeforNextRun == DisplayType.none) {
            vmManagedDeviceMap = vmManagedDeviceMap.entrySet()
                    .stream().filter(entry -> !entry.getValue().getType().equals(VmDeviceGeneralType.VIDEO))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else if (vm.getDefaultDisplayType() == DisplayType.none) {
            VmDevice videoDevice = new VmDevice(
                    new VmDeviceId(Guid.newGuid(), vm.getId()),
                    VmDeviceGeneralType.VIDEO,
                    vmVideoDeviceTypeforNextRun.toString(),
                    "",
                    Collections.emptyMap(),
                    true,
                    true,
                    false,
                    "",
                    null,
                    null,
                    null);

            vmManagedDeviceMap.put(videoDevice.getDeviceId(), videoDevice);
        }

        return vmManagedDeviceMap;
    }

    private boolean isSingletonDevice(VmDevice device) {
        return VmDeviceGeneralType.RNG.equals(device.getType())
                        || VmDeviceGeneralType.WATCHDOG.equals(device.getType());
    }

    public <E extends VmDevice> Map<String, E> vmDevicesByDevice(Collection<E> deviceList) {
        return deviceList == null
                ? Collections.emptyMap()
                : deviceList.stream()
                        .filter(dev -> dev.getDevice() != null)
                        .collect(Collectors.toMap(VmDevice::getDevice, Function.identity()));
    }

    public VmDevice getFirstDeviceWithType(List<VmDevice> devices, VmDeviceGeneralType generalType, VmDeviceType deviceType) {
        for (VmDevice device : devices) {
            if (device.getType() == generalType) {
                if (deviceType == null || (deviceType.getName() != null && deviceType.getName().equals(device.getDevice()))) {
                    return device;
                }
            }
        }

        return null;
    }

    public boolean containsDeviceWithType(List<VmDevice> devices, VmDeviceGeneralType generalType, VmDeviceType deviceType) {
        VmDevice foundDevice = getFirstDeviceWithType(devices, generalType, deviceType);
        return foundDevice != null;
    }

    public boolean containsDeviceWithType(List<VmDevice> devices, VmDeviceGeneralType type) {
        return containsDeviceWithType(devices, type, null);
    }

    public void copyVmExternalData(Guid sourceVmId, Guid targetVmId) {
        if (hasTpmDevice(targetVmId)) {
            vmDao.copyTpmData(sourceVmId, targetVmId);
        }
        if (getVmBase(targetVmId).getBiosType() == BiosType.Q35_SECURE_BOOT) {
            vmDao.copyNvramData(sourceVmId, targetVmId);
        }
    }

    public void updateVmExternalData(VM vm) {
        Map<VmExternalDataKind, String> vmExternalData = vm.getVmExternalData();
        if (vmExternalData == null) {
            return;
        }
        Guid vmId = vm.getId();
        String tpmData = vmExternalData.get(VmExternalDataKind.TPM);
        if (tpmData == null) {
            vmDao.deleteTpmData(vmId);
        } else {
            vmDao.updateTpmData(vmId, tpmData, "");
        }
        String nvramData = vmExternalData.get(VmExternalDataKind.NVRAM);
        if (nvramData == null) {
            vmDao.deleteNvramData(vmId);
        } else {
            vmDao.updateNvramData(vmId, nvramData, "");
        }
    }
}
