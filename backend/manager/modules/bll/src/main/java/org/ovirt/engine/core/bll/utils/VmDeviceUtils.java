package org.ovirt.engine.core.bll.utils;

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

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.validator.VirtIoRngValidator;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
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
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class VmDeviceUtils {

    private static final String EHCI_MODEL = "ich9-ehci";
    private static final String UHCI_MODEL = "ich9-uhci";
    private static final int SLOTS_PER_CONTROLLER = 6;
    private static final int COMPANION_USB_CONTROLLERS = 3;
    private static final int VNC_MIN_MONITORS = 1;
    private static final int SINGLE_QXL_MONITORS = 1;
    public static final Map<String, Object> EMPTY_SPEC_PARAMS = Collections.emptyMap();
    private static OsRepository osRepository;
    private static DbFacade dbFacade;
    private static VmDeviceDao dao;

    static {
        init();
    }

    /**
     * Useful for tests that want to re-initialize the static dependencies using newly mocked objects.
     */
    public static void init() {
        osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        dbFacade = SimpleDependencyInjector.getInstance().get(DbFacade.class);
        dao = dbFacade.getVmDeviceDao();
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
    private static String getCdPath(String srcCdPath, String dstCdPath) {
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
    public static String getCdInterface(VM vm) {
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
    private static void updateCdPath(VmBase oldVmBase, VmBase newVmBase) {
        List<VmDevice> cdList = getCdDevices(oldVmBase.getId());
        if (cdList.size() > 0) { // this is done only for safety, each VM must have at least an empty CD
            VmDevice cd = cdList.get(0); // only one managed CD is currently supported.
            cd.getSpecParams().putAll(getCdDeviceSpecParams("", newVmBase.getIsoPath()));
            dao.update(cd);
        }
    }

    /**
     * Get list of all CD-ROM devices in the VM.
     */
    public static List<VmDevice> getCdDevices(Guid vmId) {
        return dao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.DISK,
                VmDeviceType.CDROM.getName());
    }

    /**
     * Check if the VM has a CD-ROM device.
     */
    public static boolean hasCdDevice(Guid vmId) {
        return !getCdDevices(vmId).isEmpty();
    }

    /**
     * Get CD-ROM device spec params.
     */
    private static Map<String, Object> getCdDeviceSpecParams(String srcCdPath, String dstCdPath) {
        return Collections.<String, Object> singletonMap(VdsProperties.Path, getCdPath(srcCdPath, dstCdPath));
    }

    /**
     * Add CD-ROM device with given CD path to the VM.
     */
    public static VmDevice addCdDevice(Guid vmId, String cdPath) {
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
    public static VmDevice addCdDevice(Guid vmId) {
        return addCdDevice(vmId, "");
    }

    /*
     * Smartcard device
     */

    /**
     * Update smartcard device in the new VM, if its state should be different from the old VM.
     */
    private static void updateSmartcardDevice(VM oldVm, VmBase newVm) {
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
    public static void updateSmartcardDevice(Guid vmId, boolean smartcardEnabled) {
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
    public static List<VmDevice> getSmartcardDevices(Guid vmId) {
        return dao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.SMARTCARD,
                VmDeviceType.SMARTCARD.getName());
    }

    /**
     * Remove all smartcard devices from the VM.
     */
    public static void removeSmartcardDevices(Guid vmId) {
        removeVmDevices(getSmartcardDevices(vmId));
    }

    /**
     * Check if the VM has a smartcard device.
     */
    public static boolean hasSmartcardDevice(Guid vmId) {
        return !getSmartcardDevices(vmId).isEmpty();
    }

    /**
     * Add new smartcard device to the VM.
     */
    public static VmDevice addSmartcardDevice(Guid vmId) {
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
    private static Map<String, Object> getSmartcardDeviceSpecParams() {
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
    public static void updateConsoleDevice(Guid vmId, Boolean consoleEnabled) {
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
    public static List<VmDevice> getConsoleDevices(Guid vmId) {
        return dao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.CONSOLE,
                VmDeviceType.CONSOLE.getName());
    }

    /**
     * Remove all console devices from the VM.
     */
    public static void removeConsoleDevices(Guid vmId) {
        removeVmDevices(getConsoleDevices(vmId));
    }

    /**
     * Check if the VM has a console device.
     */
    public static boolean hasConsoleDevice(Guid vmId) {
        return !getConsoleDevices(vmId).isEmpty();
    }

    /**
     * Add new console device to the VM.
     */
    public static VmDevice addConsoleDevice(Guid vmId) {
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
    private static Map<String, Object> getConsoleDeviceSpecParams() {
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
    public static void updateVirtioScsiController(Guid vmId, Boolean isVirtioScsiEnabled) {
        if (isVirtioScsiEnabled == null) {
            return; //we don't want to update the device
        }

        if (isVirtioScsiEnabled) {
            if (!hasVirtioScsiController(vmId)) {
                addVirtioScsiController(vmId);
            }
        } else {
            removeVirtioScsiControllers(vmId);
        }
    }

    /**
     * Add new VirtIO-SCSI controller to the VM.
     */
    public static VmDevice addVirtioScsiController(Guid vmId) {
        return addManagedDevice(
            new VmDeviceId(Guid.newGuid(), vmId),
            VmDeviceGeneralType.CONTROLLER,
            VmDeviceType.VIRTIOSCSI,
            EMPTY_SPEC_PARAMS,
            true,
            false);
    }

    /**
     * Get list of all VirtIO-SCSI controllers in the VM.
     */
    public static List<VmDevice> getVirtioScsiControllers(Guid vmId) {
        return getVirtioScsiControllers(vmId, null, false);
    }

    /**
     * Get list of VirtIO-SCSI controllers in the VM.
     */
    public static List<VmDevice> getVirtioScsiControllers(Guid vmId, Guid userID, boolean isFiltered) {
        return dao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.VIRTIOSCSI.getName(),
                userID,
                isFiltered);
    }

    /**
     * Remove all VirtIO-SCSI controllers from the VM.
     */
    public static void removeVirtioScsiControllers(Guid vmId) {
        removeVmDevices(getVirtioScsiControllers(vmId));
    }

    /**
     * Check if the VM has a VirtIO-SCSI controller.
     */
    public static boolean hasVirtioScsiController(Guid vmId) {
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
    public static void updateSoundDevice(VmBase oldVmBase, VmBase newVmBase, Version compatibilityVersion,
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
    public static void updateSoundDevice(Guid vmId, int osId, Version compatibilityVersion,
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
    public static List<VmDevice> getSoundDevices(Guid vmId) {
        return dao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.SOUND);
    }

    /**
     * Add new sound device to the VM.
     */
    public static VmDevice addSoundDevice(VmBase vmBase) {
        return addSoundDevice(vmBase.getId(), vmBase.getOsId(), ClusterUtils.getCompatibilityVersion(vmBase));
    }

    /**
     * Add new sound device to the VM.
     */
    public static VmDevice addSoundDevice(Guid vmId, int osId, Version compatibilityVersion) {
        String soundDevice = osRepository.getSoundDevice(osId, compatibilityVersion);
        return addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.SOUND,
                VmDeviceType.getSoundDeviceType(soundDevice),
                EMPTY_SPEC_PARAMS,
                true,
                true);
    }

    /**
     * Check if the VM has a sound device.
     */
    public static boolean hasSoundDevice(Guid vmId) {
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
    public static boolean shouldOverrideSoundDevice(VmStatic vmStatic, Version compatibilityVersion,
                                                    Boolean soundDeviceEnabled) {
        return soundDeviceEnabled == null &&
                osRepository.isSoundDeviceEnabled(vmStatic.getOsId(), compatibilityVersion) &&
                vmStatic.getVmType() == VmType.Desktop;
    }

    /*
     * Video device
     */

    private static void updateVideoDevices(VmBase oldVmBase, VmBase newVmBase) {
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

    private static int getNeededNumberOfVideoDevices(VmBase vmBase) {
        int maxMonitorsSpice = vmBase.getSingleQxlPci() ? SINGLE_QXL_MONITORS : vmBase.getNumOfMonitors();
        int maxMonitorsVnc = Math.max(VNC_MIN_MONITORS, vmBase.getNumOfMonitors());

        return Math.min(maxMonitorsSpice, maxMonitorsVnc);
    }

    /**
     * Add given number of video devices to the VM.
     */
    public static void addVideoDevices(VmBase vmBase, int numberOfVideoDevices) {
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

    /**
     * Returns video device spec params.
     *
     * @return a map of device parameters
     */
    private static Map<String, Object> getVideoDeviceSpecParams(VmBase vmBase) {
        return VideoDeviceSettings.getVideoDeviceSpecParams(vmBase);
    }

    /**
     * Get list of all video devices in the VM.
     */
    public static List<VmDevice> getVideoDevices(Guid vmId) {
        return dao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.VIDEO);
    }

    /**
     * Remove all video devices from the VM.
     */
    public static void removeVideoDevices(Guid vmId) {
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
    public static VmDevice addInterface(Guid vmId, Guid deviceId, boolean plugged, boolean hostDev) {
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
    public static VmDevice addInterface(Guid vmId, Guid deviceId, boolean plugged, boolean hostDev, String address) {
        return addManagedDevice(
            new VmDeviceId(deviceId, vmId),
            VmDeviceGeneralType.INTERFACE,
            hostDev ? VmDeviceType.HOST_DEVICE : VmDeviceType.BRIDGE,
            EMPTY_SPEC_PARAMS,
            plugged,
            false,
            address,
            null,
            false);
    }

    private static boolean canPlugInterface(VmNic iface) {
        VmInterfaceManager vmIfaceManager = new VmInterfaceManager();
        if (vmIfaceManager.existsPluggedInterfaceWithSameMac(iface)) {
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
     * Add given number of USB controllers to the VM.
     */
    public static void addUsbControllers(Guid vmId, int numberOfControllers) {
        // For each controller we need to create one EHCI and companion UHCI controllers
        for (int index = 0; index < numberOfControllers; index++) {
            addManagedDevice(
                    new VmDeviceId(Guid.newGuid(), vmId),
                    VmDeviceGeneralType.CONTROLLER,
                    VmDeviceType.USB,
                    getUsbControllerSpecParams(EHCI_MODEL, 1, index),
                    true,
                    false);
            for (int companionIndex = 1; companionIndex <= COMPANION_USB_CONTROLLERS; companionIndex++) {
                addManagedDevice(
                        new VmDeviceId(Guid.newGuid(), vmId),
                        VmDeviceGeneralType.CONTROLLER,
                        VmDeviceType.USB,
                        getUsbControllerSpecParams(UHCI_MODEL, companionIndex, index),
                        true,
                        false);
            }
        }
    }

    /**
     * Returns USB controller spec params.
     */
    private static Map<String, Object> getUsbControllerSpecParams(String model, int controllerNumber, int index) {
        Map<String, Object> specParams = new HashMap<>();
        specParams.put(VdsProperties.Model, model + controllerNumber);
        specParams.put(VdsProperties.Index, Integer.toString(index));
        return specParams;
    }

    /**
     * Get list of all USB controllers in the VM.
     */
    public static List<VmDevice> getUsbControllers(Guid vmId) {
        return dao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.USB.getName());
    }

    /**
     * Remove all USB controllers from the VM.
     */
    public static void removeUsbControllers(Guid vmId) {
        removeVmDevices(getUsbControllers(vmId));
    }

    /*
     * USB slot
     */

    /**
     * Update USB slots in the new VM, if USB policy of the new VM differs from one of the old VM.
     */
    private static void updateUsbSlots(VmBase oldVm, VmBase newVm) {
        UsbPolicy oldUsbPolicy = UsbPolicy.DISABLED;
        UsbPolicy newUsbPolicy = newVm.getUsbPolicy();
        int currentNumberOfSlots = 0;

        if (oldVm != null) {
            oldUsbPolicy = oldVm.getUsbPolicy();
            currentNumberOfSlots = getUsbSlots(oldVm.getId()).size();
        }

        final int usbSlots = Config.<Integer> getValue(ConfigValues.NumberOfUSBSlots);

        // We add USB slots if they are disabled in oldVm configuration, but enabled in newVm
        if (!oldUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE) && newUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE)) {
            if (usbSlots > 0) {
                removeUsbControllers(newVm.getId());
                addUsbControllers(newVm.getId(), getNeededNumberOfUsbControllers(usbSlots));
                addUsbSlots(newVm.getId(), usbSlots);
            }
        // Remove USB slots and controllers if the policy is to disable or legacy one
        } else if (newUsbPolicy.equals(UsbPolicy.DISABLED) || newUsbPolicy.equals(UsbPolicy.ENABLED_LEGACY)) {
            removeUsbControllers(newVm.getId());
            removeUsbSlots(newVm.getId());
        // If the USB policy is to enable (and was enabled before), we need to update the number of slots
        } else if (newUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE)) {
            if (currentNumberOfSlots < usbSlots) {
                // Add slots and controllers
                if (currentNumberOfSlots == 0) {
                    addUsbControllers(newVm.getId(), getNeededNumberOfUsbControllers(usbSlots));
                }
                addUsbSlots(newVm.getId(), usbSlots - currentNumberOfSlots);
            } else if (currentNumberOfSlots > usbSlots) {
                // Remove slots and controllers
                removeUsbSlots(newVm.getId(), currentNumberOfSlots - usbSlots);
                if (usbSlots == 0) {
                    removeUsbControllers(newVm.getId());
                }
            }
        }
    }

    /**
     * Returns number of USB controllers that needs to be created for the given number of USB slots.
     */
    private static int getNeededNumberOfUsbControllers(int numberOfSlots) {
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
    public static void addUsbSlots(Guid vmId, int numberOfSlots) {
        for (int index = 1; index <= numberOfSlots; index++) {
            addManagedDevice(
                    new VmDeviceId(Guid.newGuid(), vmId),
                    VmDeviceGeneralType.REDIR,
                    VmDeviceType.SPICEVMC,
                    EMPTY_SPEC_PARAMS,
                    true,
                    false);
        }
    }

    /**
     * Get list of all USB slots in the VM.
     */
    public static List<VmDevice> getUsbSlots(Guid vmId) {
        return dao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.REDIR,
                VmDeviceType.SPICEVMC.getName());
    }

    /**
     * Remove all USB slots from the VM.
     */
    public static void removeUsbSlots(Guid vmId) {
        removeVmDevices(getUsbSlots(vmId));
    }

    /**
     * Remove the given number of USB slots from the VM.
     */
    public static void removeUsbSlots(Guid vmId, int numberOfSlotsToRemove) {
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
    public static void updateMemoryBalloon(Guid vmId, Boolean isBalloonEnabled) {
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
    public static VmDevice addMemoryBalloon(Guid vmId) {
        return addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.BALLOON,
                VmDeviceType.MEMBALLOON,
                getMemoryBalloonSpecParams(),
                true,
                true);
    }

    private static Map<String, Object> getMemoryBalloonSpecParams() {
        return Collections.<String, Object> singletonMap(VdsProperties.Model, VdsProperties.Virtio);
    }

    /**
     * Get list of all memory balloon devices in the VM.
     */
    public static List<VmDevice> getMemoryBalloons(Guid vmId) {
        return dao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.BALLOON);
    }

    /**
     * Remove all memory balloon devices from the VM.
     */
    public static void removeMemoryBalloons(Guid vmId) {
        removeVmDevices(getMemoryBalloons(vmId), 1);
    }

    /**
     * Check if the VM has a memory balloon device.
     */
    public static boolean hasMemoryBalloon(Guid vmId) {
        return dao.isMemBalloonEnabled(vmId);
    }

    /*
     * RNG device
     */

    /**
     * Get list of all RNG devices in the VM.
     */
    public static List<VmDevice> getRngDevices(Guid vmId) {
        return dao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.RNG);
    }

    /**
     * Check if the VM has an RNG device.
     */
    public static boolean hasRngDevice(Guid vmId) {
        return !getRngDevices(vmId).isEmpty();
    }

    /*
     * Graphics device
     */

    /**
     * Get list of graphics device types present in the VM.
     */
    public static List<GraphicsType> getGraphicsTypesOfEntity(Guid entityId) {
        List<GraphicsType> result = new ArrayList<>();

        if (entityId != null) {
            List<VmDevice> devices = getGraphicsDevices(entityId);
            if (devices != null) {
                for (VmDevice device : devices) {
                    result.add(GraphicsType.fromString(device.getDevice()));
                }
            }
        }

        return result;
    }

    /**
     * Get list of all graphics devices in the VM.
     */
    public static List<VmDevice> getGraphicsDevices(Guid vmId) {
        return dao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.GRAPHICS);
    }

    /**
     * Get list of graphics devices of the given type present in the VM.
     */
    public static List<VmDevice> getGraphicsDevices(Guid vmId, GraphicsType type) {
        return dao.getVmDeviceByVmIdTypeAndDevice(
                vmId,
                VmDeviceGeneralType.GRAPHICS,
                type.name().toLowerCase());
    }

    /**
     * Check if the VM has a graphics device of the given type.
     */
    public static boolean hasGraphicsDevice(Guid vmId, GraphicsType type) {
        return !getGraphicsDevices(vmId, type).isEmpty();
    }

    /*
     * Watchdog
     */

    /**
     * Get list of all watchdogs in the VM.
     */
    public static List<VmDevice> getWatchdogs(Guid vmId) {
        return dao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.WATCHDOG);
    }

    /**
     * Check if the VM has a watchdog.
     */
    public static boolean hasWatchdog(Guid vmId) {
        return !getWatchdogs(vmId).isEmpty();
    }

    /*
     * Disk device
     */

    /**
     * Copy disk devices from the given list of devices to the destination VM. Device ids are changed in accordance
     * with the mapping given.
     */
    public static void copyDiskDevices(Guid dstId,
                                       List<VmDevice> srcDevices,
                                       Map<Guid, Guid> srcDeviceIdToDstDeviceIdMapping) {
        for (VmDevice device : srcDevices) {
            if (VmDeviceType.DISK.getName().equals(device.getDevice())) {
                if (srcDeviceIdToDstDeviceIdMapping.containsKey(device.getDeviceId())) {
                    Guid dstDeviceId = srcDeviceIdToDstDeviceIdMapping.get(device.getDeviceId());
                    device.setId(new VmDeviceId(dstDeviceId, dstId));
                    device.setSpecParams(EMPTY_SPEC_PARAMS);
                    dao.save(device);
                }
            }
        }
    }

    /**
     * Add a new disk device to the VM.
     */
    public static VmDevice addDiskDevice(Guid vmId, Guid deviceId) {
        return addDiskDevice(vmId, deviceId, true, false, "", false);
    }

    /**
     * Add a new disk device to the VM.
     */
    public static VmDevice addDiskDevice(Guid vmId, Guid deviceId, String address) {
        return addDiskDevice(vmId, deviceId, true, false, address, false);
    }

    /**
     * Add a new disk device to the VM.
     */
    public static VmDevice addDiskDevice(Guid vmId, Guid deviceId, Boolean isPlugged, Boolean isReadOnly,
                                         boolean isUsingScsiReservation) {
        return addDiskDevice(vmId, deviceId, isPlugged, isReadOnly, "", isUsingScsiReservation);
    }

    /**
     * Add a new disk device to the VM.
     */
    public static VmDevice addDiskDevice(Guid vmId, Guid deviceId, Boolean isPlugged, Boolean isReadOnly,
                                         String address, boolean isUsingScsiReservation) {
        return addManagedDevice(
                new VmDeviceId(deviceId, vmId),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK,
                EMPTY_SPEC_PARAMS,
                isPlugged,
                isReadOnly,
                address,
                null,
                isUsingScsiReservation);
    }

    /**
     * Gets a set of disks from disk images. For VM with snapshots, several DiskImage elements may contain
     * the same Disk.
     *
     * @param diskImages collection of DiskImage objects to get the set of Disks from
     * @return the resulting set of disks
     */
    public static Set<BaseDisk> getDisks(Collection<DiskImage> diskImages) {
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
    private static void updateBootOrder(VmBase oldVmBase, VmBase newVmBase) {
        if (oldVmBase.getDefaultBootSequence() != newVmBase.getDefaultBootSequence()) {
            updateBootOrder(newVmBase.getId());
        }
    }

    /**
     * Updates boot order in all devices in the VM according to the default boot sequence.
     * Stores the updated devices in the DB.
     */
    public static void updateBootOrder(Guid vmId) {
        VM vm = dbFacade.getVmDao().get(vmId);
        if (vm != null) {
            // Returns the devices sorted in ascending order
            List<VmDevice> devices = dao.getVmDeviceByVmId(vmId);
            // Reset current boot order
            for (VmDevice device: devices) {
                device.setBootOrder(0);
            }
            VmHandler.updateDisksForVm(vm, dbFacade.getDiskDao().getAllForVm(vmId));
            VmHandler.updateDisksVmDataForVm(vm);
            VmHandler.updateNetworkInterfacesFromDb(vm);
            VmDeviceCommonUtils.updateVmDevicesBootOrder(vm, devices);
            dao.updateBootOrderInBatch(devices);
        }
    }

    /*
     * Generic device methods
     */

    /**
     * Get address of the given managed device in the VM. If the device does not exist, returns empty string.
     */
    public static String getVmDeviceAddress(VmBase vmBase, final Guid deviceId) {
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
    public static void removeVmDevices(List<VmDevice> devices) {
        for (VmDevice device : devices) {
            dao.remove(device.getId());
        }
    }

    /**
     * Remove the given number of devices starting from the end of the list.
     */
    public static void removeVmDevices(List<VmDevice> devices, int numberOfDevicesToRemove) {
        int size = devices.size();
        for (int index = 1; index <= numberOfDevicesToRemove; index++) {
            if (size >= index) {
                dao.remove(devices.get(size - index).getId());
            }
        }
    }

    /**
     * Read devices from the DB and set managed and unmanaged device lists in VmBase.
     */
    public static void setVmDevices(VmBase vmBase) {
        List<VmDevice> devices = dao.getVmDeviceByVmId(vmBase.getId());
        vmBase.setUnmanagedDeviceList(dao.getUnmanagedDevicesByVmId(vmBase.getId()));
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
    private static boolean isDiskOrInterface(VmDevice vmDevice) {
        return VmDeviceCommonUtils.isDisk(vmDevice) || VmDeviceCommonUtils.isNetwork(vmDevice);
    }

    /**
     * Update the VM devices according to the changes made.
     *
     * @param params    changes made
     * @param oldVm     previous state of the VM being modified
     */
    public static void updateVmDevices(VmManagementParametersBase params, VM oldVm) {
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
        updateVirtioScsiController(newVmBase.getId(), params.isVirtioScsiEnabled());
    }

    /**
     * Update the VM devices according to changes made in configuration.
     *
     * This method is executed before running the VM.
     */
    public static void updateVmDevicesOnRun(VmBase vmBase) {
        if (vmBase != null) {
            updateUsbSlots(vmBase, vmBase);
        }
    }

    /**
     * Copy devices from the given VmDevice list to the destination VM/VmBase.
     */
    public static void copyVmDevices(Guid srcId,
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
                                     boolean copyHostDevices) {
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
            cluster = DbFacade.getInstance().getClusterDao().get(dstVmBase.getClusterId());
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
                        // to the new VmStatic params.
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
                    if (!new VirtIoRngValidator().canAddRngDevice(cluster, new VmRngDevice(device)).isValid()) {
                        continue;
                    }
                    specParams.putAll(device.getSpecParams());
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
            dao.save(device);
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
            addVirtioScsiController(dstId);
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
    public static void copyVmDevices(Guid srcId,
                                     Guid dstId,
                                     Map<Guid, Guid> srcDeviceIdToDstDeviceIdMapping,
                                     boolean isSoundEnabled,
                                     boolean isConsoleEnabled,
                                     Boolean isVirtioScsiEnabled,
                                     boolean isBalloonEnabled,
                                     Set<GraphicsType> graphicsToSkip,
                                     boolean copySnapshotDevices) {

        VmBase srcVmBase = getVmBase(srcId);
        VmBase dstVmBase = getVmBase(dstId);
        List<VmDevice> srcDevices = dao.getVmDeviceByVmId(srcId);

        copyVmDevices(srcId, dstId, dstVmBase, srcDevices, srcDeviceIdToDstDeviceIdMapping,
                isSoundEnabled, isConsoleEnabled, isVirtioScsiEnabled, isBalloonEnabled, graphicsToSkip,
                copySnapshotDevices, canCopyHostDevices(srcVmBase, dstVmBase));
    }


    /** @see #canCopyHostDevices(VmBase, VmBase) */
    public static boolean canCopyHostDevices(Guid srcId, VmBase dstVm) {
        return canCopyHostDevices(getVmBase(srcId), dstVm);
    }

    /**
     * Determines whether it is safe to copy host devices from source VM/Template to destination.
     * More specifically it checks that no discrepancy between source and destination 'dedicatedVmForVds' occurred,
     * since host device consistency depends on this value.
     */
    public static boolean canCopyHostDevices(VmBase srcVm, VmBase dstVm) {
        return new HashSet<>(srcVm.getDedicatedVmForVdsList()).equals(new HashSet<>(dstVm.getDedicatedVmForVdsList()));
    }

    /**
     * Returns VmBase object regardless if passed ID is of VM or Template.
     *
     * @param vmId ID of a VM or Template
     * @return VmStatic if a VM of given ID was found, VmTemplate otherwise.
     */
    private static VmBase getVmBase(Guid vmId) {
        VM vm = dbFacade.getVmDao().get(vmId);
        VmBase vmBase = (vm != null) ? vm.getStaticData() : null;

        if (vmBase == null) {
            vmBase = dbFacade.getVmTemplateDao().get(vmId);
        }

        return vmBase;
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
    public static VmDevice addManagedDevice(VmDeviceId id,
                                            VmDeviceGeneralType generalType,
                                            VmDeviceType type,
                                            Map<String, Object> specParams,
                                            boolean isPlugged,
                                            Boolean isReadOnly) {
        return addManagedDevice(id, generalType, type, specParams, isPlugged, isReadOnly, "", null, false);
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
    public static VmDevice addManagedDevice(VmDeviceId id,
                                            VmDeviceGeneralType generalType,
                                            VmDeviceType type,
                                            Map<String, Object> specParams,
                                            Boolean isPlugged,
                                            Boolean isReadOnly,
                                            String address,
                                            Map<String, String> customProps,
                                            boolean isUsingScsiReservation) {
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
                        null,
                        isUsingScsiReservation);
        dao.save(managedDevice);

        // If we've added Disk/Interface/CD/Floppy, we have to recalculate boot order
        if (generalType == VmDeviceGeneralType.DISK || generalType == VmDeviceGeneralType.INTERFACE) {
            updateBootOrder(id.getVmId());
        }

        return managedDevice;
    }

    /**
     * Add devices to an imported VM or template.
     */
    public static void addImportedDevices(VmBase vmBase, boolean isImportAsNewEntity) {
        if (isImportAsNewEntity) {
            setNewIdInImportedCollections(vmBase);
        }

        List<VmDevice> vmDevicesToAdd = new ArrayList<>();
        List<VmDevice> vmDevicesToUpdate = new ArrayList<>();

        addImportedDiskDevices(vmBase, vmDevicesToUpdate);
        addImportedInterfaces(vmBase, vmDevicesToUpdate);
        addImportedOtherDevices(vmBase, vmDevicesToAdd);

        dao.saveAll(vmDevicesToAdd);
        dao.updateAll(vmDevicesToUpdate);
    }

    /**
     * Add disk devices to an imported VM or template.
     *
     * @param vmDevicesToUpdate    list of devices to be updated in the DB
     */
    private static void addImportedDiskDevices(VmBase vmBase, List<VmDevice> vmDevicesToUpdate) {
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
    private static void addImportedInterfaces(VmBase vmBase, List<VmDevice> vmDevicesToUpdate) {

        for (VmNic iface : vmBase.getInterfaces()) {
            Guid deviceId = iface.getId();
            VmDevice vmDevice = addInterface(vmBase.getId(), deviceId, true, iface.isPassthrough(),
                    getVmDeviceAddress(vmBase, deviceId));

            VmDevice exportedDevice = vmBase.getManagedDeviceMap().get(deviceId);
            if (exportedDevice == null) {
                vmBase.getManagedDeviceMap().put(deviceId, vmDevice);
                exportedDevice = vmDevice;
            }

            exportedDevice.setIsPlugged(exportedDevice.getIsPlugged() && canPlugInterface(iface));
            updateImportedVmDevice(vmBase, vmDevice, deviceId, vmDevicesToUpdate);
        }
    }

    /**
     * Add other managed and unmanaged devices to imported VM or template.
     *
     * @param vmDeviceToAdd     list of devices to be added to the DB
     */
    private static void addImportedOtherDevices(VmBase vmBase, List<VmDevice> vmDeviceToAdd) {
        boolean hasCd = false;
        boolean hasSound = false;

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

                case SOUND:
                    hasSound = true;
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
        for (VmDevice vmDevice : vmBase.getUnmanagedDeviceList()) {
            vmDeviceToAdd.add(vmDevice);
        }
    }

    /**
     * Set common properties in a device for imported VM or template and add the device
     * to the list of devices to be updated in the DB.
     *
     * @param vmDevicesToUpdate    list of devices to be updated in the DB
     */
    private static void updateImportedVmDevice(VmBase vmBase,
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
    private static void setNewIdInImportedCollections(VmBase vmBase) {
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
    public static boolean vmDeviceChanged(Guid vmId, VmDeviceGeneralType deviceGeneralType, String deviceTypeName,
                                          boolean deviceEnabled) {
        List<VmDevice> vmDevices = deviceTypeName != null ?
                dao.getVmDeviceByVmIdTypeAndDevice(vmId, deviceGeneralType, deviceTypeName):
                dao.getVmDeviceByVmIdAndType(vmId, deviceGeneralType);

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
    public static boolean vmDeviceChanged(Guid vmId, VmDeviceGeneralType deviceGeneralType, String deviceTypeName,
                                          VmDevice device) {
        List<VmDevice> vmDevices = deviceTypeName != null ?
                dao.getVmDeviceByVmIdTypeAndDevice(vmId, deviceGeneralType, deviceTypeName):
                dao.getVmDeviceByVmIdAndType(vmId, deviceGeneralType);

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
    public static Map<Guid, VmDevice> getVmDevicesForNextRun(VM vm, Object objectWithEditableDeviceFields) {
        setVmDevices(vm.getStaticData());
        Map<Guid, VmDevice> vmManagedDeviceMap = vm.getManagedVmDeviceMap();

        List<VmDeviceUpdate> fieldList =
                VmHandler.getVmDevicesFieldsToUpdateOnNextRun(vm.getId(), vm.getStatus(), objectWithEditableDeviceFields);

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
                                    EMPTY_SPEC_PARAMS,
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

        return vmManagedDeviceMap;
    }

    public static <E extends VmDevice> Map<String, E> vmDevicesByDevice(Collection<E> deviceList) {
        return deviceList == null
                ? Collections.emptyMap()
                : deviceList.stream()
                        .filter(dev -> dev.getDevice() != null)
                        .collect(Collectors.toMap(VmDevice::getDevice, Function.<E>identity()));
    }

}
