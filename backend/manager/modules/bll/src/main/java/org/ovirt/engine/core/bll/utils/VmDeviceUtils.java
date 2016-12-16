package org.ovirt.engine.core.bll.utils;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.bll.validator.VirtIoRngValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
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
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.VmDeviceUpdate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class VmDeviceUtils {

    private static final String EHCI_MODEL = "ich9-ehci";
    private static final String UHCI_MODEL = "ich9-uhci";
    private static final int SLOTS_PER_CONTROLLER = 6;
    private static final int COMPANION_USB_CONTROLLERS = 3;
    private static final int VNC_MIN_MONITORS = 1;
    private static final int SINGLE_QXL_MONITORS = 1;

    private final VmDao vmDao;
    private final VmDeviceDao vmDeviceDao;
    private final DiskDao diskDao;
    private final ClusterDao clusterDao;
    private final VmTemplateDao vmTemplateDao;
    private final VmHandler vmHandler;
    private final MacPoolPerCluster macPoolPerCluster;

    private OsRepository osRepository;

    @Inject
    VmDeviceUtils(VmDao vmDao,
            VmDeviceDao vmDeviceDao,
            DiskDao diskDao,
            ClusterDao clusterDao,
            VmTemplateDao vmTemplateDao,
            VmHandler vmHandler,
            MacPoolPerCluster macPoolPerCluster) {
        this.vmDao = vmDao;
        this.vmDeviceDao = vmDeviceDao;
        this.diskDao = diskDao;
        this.clusterDao = clusterDao;
        this.vmTemplateDao = vmTemplateDao;
        this.vmHandler = vmHandler;
        this.macPoolPerCluster = macPoolPerCluster;
        init();
    }

    public void init() {
        osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
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
        return osRepository.getCdInterface(
                vm.getOs(),
                vm.getCompatibilityVersion(),
                ChipsetType.fromMachineType(vm.getEmulatedMachine()));
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
            vmDeviceDao.update(cd);
        }
    }

    /**
     * Get list of all CD-ROM devices in the VM.
     */
    public List<VmDevice> getCdDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.DISK,
                VmDeviceType.CDROM.getName());
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
                VmDeviceType.SMARTCARD.getName());
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
                VmDeviceType.CONSOLE.getName());
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
                getConsoleDeviceSpecParams(),
                true,
                false);
    }

    /**
     * Returns console device spec params.
     */
    private Map<String, Object> getConsoleDeviceSpecParams() {
        Map<String, Object> specParams = new HashMap<>();
        specParams.put("enableSocket", "true");
        specParams.put("consoleType", "serial");
        return specParams;
    }


    /*
     * VirtIO-SCSI controller
     */

    /**
     * Enable/disable VirtIO-SCSI controller in the VM.
     *
     * @param isVirtioScsiEnabled    true/false to enable/disable device respectively, null to leave it untouched
     */
    public void updateVirtioScsiController(VmBase vm, Boolean isVirtioScsiEnabled) {
        if (isVirtioScsiEnabled == null) {
            return; //we don't want to update the device
        }

        removeVirtioScsiControllers(vm.getId());
        if (isVirtioScsiEnabled) {
            addVirtioScsiController(vm, getVmCompatibilityVersion(vm));
        }
    }

    /**
     * Add new VirtIO-SCSI controllers to the VM.
     */
    public void addVirtioScsiController(VmBase vm, Version version) {
        boolean hasIoThreads = vm.getNumOfIoThreads() > 0 && FeatureSupported.virtioScsiIoThread(version);
        int numOfScsiControllers = hasIoThreads ? vm.getNumOfIoThreads() : 1;

        for (int i = 0; i < numOfScsiControllers; i++) {
            Map<String, Object> specParams = new HashMap<>();
            if (hasIoThreads) {
                specParams.put(VdsProperties.ioThreadId, i);
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
     * case, if OS has been changed.
     *
     * @param compatibilityVersion  cluster compatibility version
     * @param isSoundDeviceEnabled  true/false to enable/disable device respectively, null to leave it untouched
     */
    public void updateSoundDevice(VmBase oldVmBase, VmBase newVmBase, Version compatibilityVersion,
                                         Boolean isSoundDeviceEnabled) {
        boolean osChanged = oldVmBase.getOsId() != newVmBase.getOsId();
        updateSoundDevice(newVmBase.getId(), newVmBase.getOsId(), compatibilityVersion, isSoundDeviceEnabled, osChanged);
    }

    /**
     * Enable/disable sound device in the VM.
     *
     * @param compatibilityVersion  cluster compatibility version
     * @param isSoundDeviceEnabled  true/false to enable/disable device respectively, null to leave it untouched
     * @param recreate              true to recreate the device even if it already exists
     */
    public void updateSoundDevice(Guid vmId, int osId, Version compatibilityVersion,
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
            addSoundDevice(vmId, osId, compatibilityVersion);
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
    public VmDevice addSoundDevice(VmBase vmBase) {
        return addSoundDevice(vmBase.getId(), vmBase.getOsId(), ClusterUtils.getCompatibilityVersion(vmBase));
    }

    /**
     * Add new sound device to the VM.
     */
    public VmDevice addSoundDevice(Guid vmId, int osId, Version compatibilityVersion) {
        String soundDevice = osRepository.getSoundDevice(osId, compatibilityVersion);
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

    private void updateVideoDevices(VmBase oldVmBase, VmBase newVmBase) {
        boolean displayTypeChanged = oldVmBase.getDefaultDisplayType() != newVmBase.getDefaultDisplayType();
        boolean numOfMonitorsChanged = newVmBase.getDefaultDisplayType() == DisplayType.qxl &&
                oldVmBase.getNumOfMonitors() != newVmBase.getNumOfMonitors();
        boolean singleQxlChanged = oldVmBase.getSingleQxlPci() != newVmBase.getSingleQxlPci();
        boolean guestOsChanged = oldVmBase.getOsId() != newVmBase.getOsId();

        if (displayTypeChanged || numOfMonitorsChanged || singleQxlChanged || guestOsChanged) {
            removeVideoDevices(oldVmBase.getId());
            addVideoDevices(newVmBase, getNeededNumberOfVideoDevices(newVmBase));
        }
    }

    private int getNeededNumberOfVideoDevices(VmBase vmBase) {
        int maxMonitorsSpice = vmBase.getSingleQxlPci() ? SINGLE_QXL_MONITORS : vmBase.getNumOfMonitors();
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

    /**
     * Returns video device spec params.
     *
     * @return a map of device parameters
     */
    private Map<String, Object> getVideoDeviceSpecParams(VmBase vmBase) {
        return VideoDeviceSettings.getVideoDeviceSpecParams(vmBase);
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
            vmIfaceManager.auditLogMacInUseUnplug(iface);
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
     */
    public void addSpiceUsbControllers(Guid vmId, int numberOfControllers) {
        // For each controller we need to create one EHCI and companion UHCI controllers
        for (int index = 0; index < numberOfControllers; index++) {
            addManagedDevice(
                    new VmDeviceId(Guid.newGuid(), vmId),
                    VmDeviceGeneralType.CONTROLLER,
                    VmDeviceType.USB,
                    getSpiceUsbControllerSpecParams(EHCI_MODEL, 1, index),
                    true,
                    false);
            for (int companionIndex = 1; companionIndex <= COMPANION_USB_CONTROLLERS; companionIndex++) {
                addManagedDevice(
                        new VmDeviceId(Guid.newGuid(), vmId),
                        VmDeviceGeneralType.CONTROLLER,
                        VmDeviceType.USB,
                        getSpiceUsbControllerSpecParams(UHCI_MODEL, companionIndex, index),
                        true,
                        false);
            }
        }
    }
    private Map<String, Object> getSpiceUsbControllerSpecParams(String model, int controllerNumber, int index) {
        return createUsbControllerSpecParams(model + controllerNumber, index);
    }


    /**
     * Returns USB controller spec params.
     */
    private Map<String, Object> createUsbControllerSpecParams(String model, int index) {
        final HashMap<String, Object> specParams = new HashMap<>();
        specParams.put(VdsProperties.Model, model);
        specParams.put(VdsProperties.Index, Integer.toString(index));
        return specParams;
    }

    /**
     * Get list of all USB controllers in the VM.
     */
    public List<VmDevice> getUsbControllers(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.USB.getName());
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
     */
    private void updateUsbSlots(VmBase oldVm, VmBase newVm) {
        UsbPolicy oldUsbPolicy = UsbPolicy.DISABLED;
        UsbPolicy newUsbPolicy = newVm.getUsbPolicy();
        int oldNumberOfSlots = 0;

        if (oldVm != null) {
            oldUsbPolicy = oldVm.getUsbPolicy();
            oldNumberOfSlots = getUsbSlots(oldVm.getId()).size();
        }

        final int newNumberOfUsbSlots = Config.<Integer> getValue(ConfigValues.NumberOfUSBSlots);

        if (UsbPolicy.DISABLED == oldUsbPolicy && UsbPolicy.ENABLED_NATIVE == newUsbPolicy) {
            disableNormalUsb(newVm.getId());
            enableSpiceUsb(newVm.getId(), newNumberOfUsbSlots);
            return;
        }
        if (UsbPolicy.ENABLED_NATIVE == oldUsbPolicy && UsbPolicy.ENABLED_NATIVE == newUsbPolicy) {
            updateSpiceUsb(newVm.getId(), oldNumberOfSlots, newNumberOfUsbSlots);
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

    private void disableNormalUsb(Guid vmId) {
        removeUsbControllers(vmId);
    }

    private void enableSpiceUsb(Guid vmId, int newNumberOfUsbSlots) {
        if (newNumberOfUsbSlots > 0) {
            addSpiceUsbControllers(vmId, getNeededNumberOfUsbControllers(newNumberOfUsbSlots));
            addUsbSlots(vmId, newNumberOfUsbSlots);
        }
    }

    private void updateSpiceUsb(Guid vmId, int oldNumberOfSlots, int newNumberOfUsbSlots) {
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
                addSpiceUsbControllers(vmId, getNeededNumberOfUsbControllers(newNumberOfUsbSlots));
            }
            addUsbSlots(vmId, newNumberOfUsbSlots - oldNumberOfSlots);
            return;
        }
    }

    private void disableSpiceUsb(Guid vmId) {
        removeUsbControllers(vmId);
        removeUsbSlots(vmId);
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
        final List<VmDevice> usbControllers = getUsbControllers(vmBase.getId());
        if (usbControllers.size() > 1) {
            throw new IllegalStateException(format("At most one USB controller expected for VM=%s(%s), found=%s",
                    vmBase.getName(),
                    vmBase.getId(),
                    usbControllers));
        }
        final UsbControllerModel newUsbControllerModel = getUsbControllerModel(vmBase);
        if ((usbControllers.isEmpty() && newUsbControllerModel == null)
            || (!usbControllers.isEmpty()
                && newUsbControllerModel != null
                && newUsbControllerModel.libvirtName.equals(
                        getUsbControllerModelName(usbControllers.get(0))))) {
            return;
        }
        disableNormalUsb(vmBase.getId());
        enableNormalUsb(vmBase);
    }

    /**
     * @return usb controller model defined as defined in osinfo file for VM's OS and effective compatibility version
     */
    /*
     * TODO: It would be cleaner to return a value denoting unknown model for instance type input since instance types
     * doesn't actually have any operating system set. Current solution works since no usb controller devices are
     * created for instance types.
     */
    private UsbControllerModel getUsbControllerModel(VmBase vmBase) {
        final Version version = vmBase.getCustomCompatibilityVersion() != null
                ? vmBase.getCustomCompatibilityVersion()
                : vmBase.getClusterId() != null
                        ? clusterDao.get(vmBase.getClusterId()).getCompatibilityVersion()
                        : null;
        return osRepository.getOsUsbControllerModel(vmBase.getOsId(), version);
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
     * Get list of all USB slots in the VM.
     */
    public List<VmDevice> getUsbSlots(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.REDIR,
                VmDeviceType.SPICEVMC.getName());
    }

    /**
     * Remove all USB slots from the VM.
     */
    public void removeUsbSlots(Guid vmId) {
        removeVmDevices(getUsbSlots(vmId));
    }

    /**
     * Remove the given number of USB slots from the VM.
     */
    public void removeUsbSlots(Guid vmId, int numberOfSlotsToRemove) {
        removeVmDevices(getUsbSlots(vmId), numberOfSlotsToRemove);
    }

    /*
     * Memory balloon device
     */

    /**
     * Enable/disable memory balloon device in the VM.
     *
     * @param isBalloonEnabled    true/false to enable/disable device respectively, null to leave it untouched
     */
    public void updateMemoryBalloon(Guid vmId, Boolean isBalloonEnabled) {
        if (isBalloonEnabled == null) {
            return; //we don't want to update the device
        }

        if (isBalloonEnabled) {
            if (!hasMemoryBalloon(vmId)) {
                addMemoryBalloon(vmId);
            }
        } else {
            removeMemoryBalloons(vmId);
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
     * Remove all memory balloon devices from the VM.
     */
    public void removeMemoryBalloons(Guid vmId) {
        removeVmDevices(getMemoryBalloons(vmId), 1);
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
                    vmDeviceDao.save(device);
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
     * Boot order
     */

    /**
     * If default boot sequence differs in the old and new VMs, updates boot order in all devices in the new VM
     * according to the new default boot sequence. Stores the updated devices in the DB.
     */
    private void updateBootOrder(VmBase oldVmBase, VmBase newVmBase) {
        if (oldVmBase.getDefaultBootSequence() != newVmBase.getDefaultBootSequence()) {
            updateBootOrder(newVmBase.getId());
        }
    }

    /**
     * Updates boot order in all devices in the VM according to the default boot sequence.
     * Stores the updated devices in the DB.
     */
    public void updateBootOrder(Guid vmId) {
        VM vm = vmDao.get(vmId);
        if (vm != null) {
            // Returns the devices sorted in ascending order
            List<VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(vmId);
            // Reset current boot order
            for (VmDevice device: devices) {
                device.setBootOrder(0);
            }
            vmHandler.updateDisksForVm(vm, diskDao.getAllForVm(vmId));
            vmHandler.updateDisksVmDataForVm(vm);
            vmHandler.updateNetworkInterfacesFromDb(vm);
            VmDeviceCommonUtils.updateVmDevicesBootOrder(vm, devices);
            vmDeviceDao.updateBootOrderInBatch(devices);
        }
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
        for (VmDevice device : devices) {
            vmDeviceDao.remove(device.getId());
        }
    }

    /**
     * Remove the given number of devices starting from the end of the list.
     */
    public void removeVmDevices(List<VmDevice> devices, int numberOfDevicesToRemove) {
        int size = devices.size();
        for (int index = 1; index <= numberOfDevicesToRemove; index++) {
            if (size >= index) {
                vmDeviceDao.remove(devices.get(size - index).getId());
            }
        }
    }

    /**
     * Read devices from the DB and set managed and unmanaged device lists in VmBase.
     */
    public void setVmDevices(VmBase vmBase) {
        List<VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(vmBase.getId());
        vmBase.setUnmanagedDeviceList(vmDeviceDao.getUnmanagedDevicesByVmId(vmBase.getId()));
        Map<Guid, VmDevice> vmManagedDeviceMap = new HashMap<>();
        for (VmDevice device : devices) {
            if (device.getIsManaged()) {
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

        updateCdPath(oldVmBase, newVmBase);
        updateBootOrder(oldVmBase, newVmBase);
        updateVideoDevices(oldVmBase, newVmBase);
        updateUsbSlots(oldVmBase, newVmBase);
        updateMemoryBalloon(newVmBase.getId(), params.isBalloonEnabled());
        updateSoundDevice(oldVmBase, newVmBase, oldVm.getCompatibilityVersion(),
                params.isSoundDeviceEnabled());
        updateSmartcardDevice(oldVm, newVmBase);
        updateConsoleDevice(newVmBase.getId(), params.isConsoleEnabled());
        updateVirtioScsiController(newVmBase, params.isVirtioScsiEnabled());
    }

    /**
     * Update the VM devices according to changes made in configuration.
     *
     * This method is executed before running the VM.
     */
    public void updateVmDevicesOnRun(VmBase vmBase) {
        if (vmBase != null) {
            updateUsbSlots(vmBase, vmBase);
        }
    }

    /**
     * Copy devices from the given VmDevice list to the destination VM/VmBase.
     */
    public void copyVmDevices(Guid srcId,
                                     Guid dstId,
                                     VmBase dstVmBase,
                                     List<VmDevice> srcDevices,
                                     Map<Guid, Guid> srcDeviceIdToDstDeviceIdMapping,
                                     boolean isSoundEnabled,
                                     boolean isConsoleEnabled,
                                     Boolean isVirtioScsiEnabled,
                                     boolean isBalloonEnabled,
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
        boolean hasConsole = false;
        boolean hasVirtioScsi = false;
        boolean hasBalloon = false;
        boolean hasRng = hasRngDevice(dstId);

        Cluster cluster = null;
        if (dstVmBase.getClusterId() != null) {
            cluster = clusterDao.get(dstVmBase.getClusterId());
        }

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
                    if (!isBalloonEnabled) {
                        continue;
                    }
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
                            cluster, new VmRngDevice(device), dstVmBase.getCustomCompatibilityVersion()).isValid()) {
                        continue;
                    }
                    final VmRngDevice rngDevice = new VmRngDevice(device);
                    if (versionToUpdateRngDeviceWith != null) {
                        rngDevice.updateSourceByVersion(versionToUpdateRngDeviceWith);
                    }
                    specParams.putAll(rngDevice.getSpecParams());
                    break;

                case CONSOLE:
                    if (!isConsoleEnabled) {
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

        // if copying from Blank template, adding USB slots to the destination
        // according to the destination USB policy
        if (srcId.equals(Guid.Empty)) {
            updateUsbSlots(null, dstVmBase);
        }

        if (isSoundEnabled && !hasSound) {
            if (dstIsVm) {
                addSoundDevice(dstVmBase);
            } else {
                addSoundDevice(dstVmBase.getId(), dstVmBase.getOsId(),
                        cluster != null ? cluster.getCompatibilityVersion() : null);
            }
        }

        if (isConsoleEnabled && !hasConsole) {
            addConsoleDevice(dstId);
        }

        if (Boolean.TRUE.equals(isVirtioScsiEnabled) && !hasVirtioScsi) {
            addVirtioScsiController(dstVmBase, getVmCompatibilityVersion(dstVmBase));
        }

        if (isBalloonEnabled && !hasBalloon) {
            addMemoryBalloon(dstId);
        }

        if (dstIsVm) {
            updateBootOrder(dstVmBase.getId());
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
                                     boolean isConsoleEnabled,
                                     Boolean isVirtioScsiEnabled,
                                     boolean isBalloonEnabled,
                                     Set<GraphicsType> graphicsToSkip,
                                     boolean copySnapshotDevices,
                                     Version versionToUpdateRndDeviceWith) {

        VmBase srcVmBase = getVmBase(srcId);
        VmBase dstVmBase = getVmBase(dstId);
        List<VmDevice> srcDevices = vmDeviceDao.getVmDeviceByVmId(srcId);

        copyVmDevices(srcId, dstId, dstVmBase, srcDevices, srcDeviceIdToDstDeviceIdMapping,
                isSoundEnabled, isConsoleEnabled, isVirtioScsiEnabled, isBalloonEnabled, graphicsToSkip,
                copySnapshotDevices, canCopyHostDevices(srcVmBase, dstVmBase),
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
        VM vm = vmDao.get(vmId);
        VmBase vmBase = (vm != null) ? vm.getStaticData() : null;

        if (vmBase == null) {
            vmBase = vmTemplateDao.get(vmId);
        }

        return vmBase;
    }

    private Version getVmCompatibilityVersion(VmBase base) {
        if (base.getCustomCompatibilityVersion() != null) {
            return base.getCustomCompatibilityVersion();
        }
        if (base.getClusterId() != null) {
            return clusterDao.get(base.getClusterId()).getCompatibilityVersion();
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
                        0,
                        specParams,
                        true,
                        isPlugged,
                        isReadOnly,
                        "",
                        customProps,
                        null,
                        null);
        vmDeviceDao.save(managedDevice);

        // If we've added Disk/Interface/CD/Floppy, we have to recalculate boot order
        if (generalType == VmDeviceGeneralType.DISK || generalType == VmDeviceGeneralType.INTERFACE) {
            updateBootOrder(id.getVmId());
        }

        return managedDevice;
    }

    /**
     * Add devices to an imported VM or template.
     */
    public void addImportedDevices(VmBase vmBase, boolean isImportAsNewEntity) {
        if (isImportAsNewEntity) {
            setNewIdInImportedCollections(vmBase);
        }

        List<VmDevice> vmDevicesToAdd = new ArrayList<>();
        List<VmDevice> vmDevicesToUpdate = new ArrayList<>();

        addImportedDiskDevices(vmBase, vmDevicesToUpdate);
        addImportedInterfaces(vmBase, vmDevicesToUpdate);
        addImportedOtherDevices(vmBase, vmDevicesToAdd);

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

            exportedDevice.setIsPlugged(exportedDevice.getIsPlugged() && canPlugInterface(iface, vmBase));
            updateImportedVmDevice(vmBase, vmDevice, deviceId, vmDevicesToUpdate);
        }
    }

    /**
     * Add other managed and unmanaged devices to imported VM or template.
     *
     * @param vmDeviceToAdd     list of devices to be added to the DB
     */
    private void addImportedOtherDevices(VmBase vmBase, List<VmDevice> vmDeviceToAdd) {
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
                    break;

                case HOSTDEV:
                    // it is currently unsafe to import host devices, due to possibility of invalid dedicatedVmForVds
                    continue;

            }
            vmDevice.setIsManaged(true);
            vmDeviceToAdd.add(vmDevice);
        }

        if (!hasCd) { // add an empty CD
            addCdDevice(vmBase.getId());
        }

        // add unmanaged devices
        vmDeviceToAdd.addAll(vmBase.getUnmanagedDeviceList());
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
            vmDevice.setBootOrder(exportedDevice.getBootOrder());
            vmDevice.setIsPlugged(exportedDevice.getIsPlugged());
            vmDevice.setIsReadOnly(exportedDevice.getIsReadOnly());
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
     * @return a map of device ID to VmDevice
     */
    public Map<Guid, VmDevice> getVmDevicesForNextRun(VM vm, Object objectWithEditableDeviceFields) {
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
                                    0,
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
                        device.setDeviceId(Guid.newGuid());
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
        // as all other devices
        VmDevice device = VmDeviceCommonUtils.findVmDeviceByGeneralType(vmManagedDeviceMap, VmDeviceGeneralType.VIDEO);
        if (device != null) {
            vmManagedDeviceMap.remove(device.getDeviceId());
        }

        return vmManagedDeviceMap;
    }

    public <E extends VmDevice> Map<String, E> vmDevicesByDevice(Collection<E> deviceList) {
        return deviceList == null
                ? Collections.emptyMap()
                : deviceList.stream()
                        .filter(dev -> dev.getDevice() != null)
                        .collect(Collectors.toMap(VmDevice::getDevice, Function.identity()));
    }

}
