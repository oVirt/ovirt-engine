package org.ovirt.engine.core.bll.utils;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.smartcard.SmartcardSpecParams;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.EditableDeviceOnVmStatusField;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VmDeviceUtils {
    private static VmDeviceDAO dao = DbFacade.getInstance().getVmDeviceDao();
    private final static String RAM = "ram";
    private final static String VRAM = "vram";
    private final static String HEADS = "heads";
    private final static String EHCI_MODEL = "ich9-ehci";
    private final static String UHCI_MODEL = "ich9-uhci";
    private final static int SLOTS_PER_CONTROLLER = 6;
    private final static int COMPANION_USB_CONTROLLERS = 3;
    private static OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

    /**
     * Update the vm devices according to changes made in vm static for existing VM
     */
    public static void updateVmDevices(VmManagementParametersBase params, VM oldVm) {
        VmBase oldVmBase = oldVm.getStaticData();
        VmBase entity = params.getVmStaticData();
        if (entity != null) {
            updateCdInVmDevice(oldVmBase, entity);
            if (oldVmBase.getDefaultBootSequence() != entity
                    .getDefaultBootSequence()) {
                updateBootOrderInVmDeviceAndStoreToDB(entity);
            }

            // if the console type has changed, recreate Video devices
            boolean displayTypeChanged = oldVmBase.getDefaultDisplayType() != entity.getDefaultDisplayType();
            boolean numOfMonitorsChanged = entity.getDefaultDisplayType() == DisplayType.qxl && oldVmBase.getNumOfMonitors() != entity.getNumOfMonitors();
            boolean singleQxlChanged = oldVmBase.getSingleQxlPci() != entity.getSingleQxlPci();

            if(displayTypeChanged || numOfMonitorsChanged || singleQxlChanged) {
                // delete all video device
                for (VmDevice device : dao.getVmDeviceByVmIdAndType(oldVmBase.getId(), VmDeviceGeneralType.VIDEO)) {
                    dao.remove(device.getId());
                }
                // add video device per each monitor
                int monitors = entity.getSingleQxlPci() ? 1 : entity.getNumOfMonitors();
                for (int i = 0; i<monitors; i++) {
                    VmDeviceType vmDeviceType = osRepository.getDisplayDevice(entity.getOsId(), ClusterUtils.getCompatibilityVersion(entity), entity.getDefaultDisplayType());
                    addManagedDevice(new VmDeviceId(Guid.newGuid(), entity.getId()),
                            VmDeviceGeneralType.VIDEO,
                            vmDeviceType,
                            getMemExpr(entity.getNumOfMonitors(), entity.getSingleQxlPci()),
                            true,
                            false,
                            null);
                }
            }
            updateUSBSlots(oldVmBase, entity);
            updateMemoryBalloon(entity, isBalloonEnabled(params));
            updateAudioDevice(oldVm.getStaticData(), entity, oldVm.getVdsGroupCompatibilityVersion(), params.isSoundDeviceEnabled());
            updateSmartcardDevice(oldVm, entity);
            updateConsoleDevice(entity, params.isConsoleEnabled());
            updateVirtioScsiController(entity.getId(), params.isVirtioScsiEnabled());
        }
    }

    private static void updateSmartcardDevice(VM oldVm, VmBase newVm) {
        if (newVm.isSmartcardEnabled() == oldVm.isSmartcardEnabled()) {
            // the smartcard device did not changed, do nothing
            return;
        }

        updateSmartcardDevice(newVm.getId(), newVm.isSmartcardEnabled());
    }

    public static void updateSmartcardDevice(Guid vmId, boolean smartcardEnabled) {
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vmId,
                                VmDeviceGeneralType.SMARTCARD,
                                VmDeviceType.SMARTCARD.getName());

        if (!smartcardEnabled) {
            for (VmDevice device : vmDevices) {
                dao.remove(device.getId());
            }
        } else if (vmDevices.isEmpty()) {
            addSmartcardDevice(vmId);
        }
    }

    public static void addSmartcardDevice(Guid vmId) {
        VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.SMARTCARD,
                VmDeviceType.SMARTCARD,
                new SmartcardSpecParams(),
                true,
                false,
                null);
    }

    private static void updateConsoleDevice(VmBase newVmBase, Boolean consoleEnabled) {
            updateConsoleDevice(newVmBase.getId(), consoleEnabled);
    }

    public static void updateConsoleDevice(Guid vmId, Boolean consoleEnabled) {
        if (consoleEnabled == null) {
            return; //we don't want to update the device
        }

        List<VmDevice> consoles = DbFacade.getInstance()
                    .getVmDeviceDao()
                    .getVmDeviceByVmIdTypeAndDevice(vmId,
                            VmDeviceGeneralType.CONSOLE, VmDeviceType.CONSOLE.getName());

        if (consoleEnabled) {
            if (consoles.isEmpty()) {
                addConsoleDevice(vmId);
            }
        } else {
            for (VmDevice device : consoles) {
                dao.remove(device.getId());
            }
        }
    }

    public static void updateVirtioScsiController(Guid vmId, Boolean isVirtioScsiEnabled) {
        if (isVirtioScsiEnabled == null) {
            return; //we don't want to update the device
        }

        List<VmDevice> controllers = DbFacade.getInstance()
                .getVmDeviceDao()
                .getVmDeviceByVmIdTypeAndDevice(vmId,
                        VmDeviceGeneralType.CONTROLLER, VmDeviceType.VIRTIOSCSI.getName());

        if (isVirtioScsiEnabled) {
            if (controllers.isEmpty()) {
                addVirtioScsiController(vmId);
            }
        } else {
            for (VmDevice device : controllers) {
                dao.remove(device.getId());
            }
        }
    }

    private static void addConsoleDevice(Guid vmId) {
        VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.CONSOLE,
                VmDeviceType.CONSOLE,
                new HashMap<String, Object>(),
                true,
                false,
                null);
    }

    private static void addVirtioScsiController(Guid vmId) {
        VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.VIRTIOSCSI,
                new HashMap<String, Object>(),
                true,
                false,
                null);
    }

    /**
     * Replace desktop-vm audio device if OS has changed
     *
     * @param oldVm
     * @param newVmBase
     * @param cluster compatibility version
     * @param sound device enabled - if null, keep old state
     */
    public static void updateAudioDevice(VmBase oldVm, VmBase newVmBase, Version compatibilityVersion, Boolean isSoundDeviceEnabled) {
        boolean removeDevice = false;
        boolean createDevice = false;

        Guid vmId = oldVm.getId();
        boolean osChanged = oldVm.getOsId() != newVmBase.getOsId();

        List<VmDevice> list =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.SOUND);

        // if isSoundDeviceEnabled is null,
        // recreate device only if previously existed and os has changed
        if (isSoundDeviceEnabled == null) {
            if (!list.isEmpty() && osChanged) {
                removeDevice = createDevice = true;
            }
        } else {
            // if soundeDevice disabled or os changed, and device exist, remove
            removeDevice = (!isSoundDeviceEnabled || osChanged) && !list.isEmpty();

            // if soundDevice enabled and missing or os changed, create
            createDevice = isSoundDeviceEnabled && (list.isEmpty() || osChanged);
        }

        if (removeDevice) {
            removeNumberOfDevices(list, list.size());
        }
        if (createDevice) {
            String soundDevice =
                    osRepository.getSoundDevice(newVmBase.getOsId(), compatibilityVersion);
            addManagedDevice(new VmDeviceId(Guid.newGuid(), vmId),
                    VmDeviceGeneralType.SOUND,
                    VmDeviceType.getSoundDeviceType(soundDevice),
                    new HashMap<String, Object>(),
                    true,
                    true,
                    null);
        }
    }

    /**
     * Update the vm devices according to changes made configuration
     */
    public static <T extends VmBase> void updateVmDevices(T entity) {
        if (entity != null) {
            updateUSBSlots(entity, entity);
        }
    }

    /**
     * Copy related data from the given VM/VmBase/VmDevice list to the destination VM/VmTemplate.
     */
    public static void copyVmDevices(Guid srcId,
                                     Guid dstId,
                                     VM vm,
                                     VmBase vmBase,
                                     boolean isVm,
                                     List<VmDevice> devicesDataToUse,
                                     Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping,
                                     boolean soundDeviceEnabled,
                                     boolean isConsoleEnabled,
                                     Boolean isVirtioScsiEnabled,
                                     boolean isBalloonEnabled,
                                     boolean copySnapshotDevices) {
        Guid id;
        String isoPath=vmBase.getIsoPath();
        // indicates that VM should have CD either from its own (iso_path) or from the snapshot it was cloned from.
        boolean shouldHaveCD = StringUtils.isNotEmpty(isoPath);
        // indicates if VM has already a non empty CD in DB
        boolean hasAlreadyCD = (!(DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(vmBase.getId(), VmDeviceGeneralType.DISK, VmDeviceType.CDROM.getName())).isEmpty());
        boolean addCD = (!hasAlreadyCD && shouldHaveCD);
        boolean hasSoundDevice = false;
        boolean hasAlreadyConsoleDevice = false;
        boolean hasVirtioScsiController = false;

        for (VmDevice device : devicesDataToUse) {
            if (device.getSnapshotId() != null && !copySnapshotDevices) {
                continue;
            }

            id = Guid.newGuid();
            Map<String, Object> specParams = new HashMap<String, Object>();
            if (srcId.equals(Guid.Empty)) {
                //add CD if not exists
                if (addCD) {
                    setCdPath(specParams, "", isoPath);
                    addManagedDevice(new VmDeviceId(Guid.newGuid(), dstId) , VmDeviceGeneralType.DISK, VmDeviceType.CDROM, specParams, true, true, null);
                    hasAlreadyCD = true;
                    addCD = false;
                }
                // updating USB slots
                updateUSBSlots(null, vmBase);
                // add mem balloon if defined
                updateMemoryBalloon(vmBase, isBalloonEnabled);
            }

            switch(device.getType()) {
                case DISK:
                    if (VmDeviceType.DISK.getName().equals(device.getDevice())) {
                        if (srcDeviceIdToTargetDeviceIdMapping.containsKey(device.getDeviceId())) {
                            id = srcDeviceIdToTargetDeviceIdMapping.get(device.getDeviceId());
                        }
                    } else if (VmDeviceType.CDROM.getName().equals(device.getDevice())) {
                        // check here is source VM had CD (Vm from snapshot)
                        String srcCdPath = (String) device.getSpecParams().get(VdsProperties.Path);
                        shouldHaveCD = (!StringUtils.isEmpty(srcCdPath) || shouldHaveCD);
                        if (!hasAlreadyCD && shouldHaveCD) {
                            setCdPath(specParams, srcCdPath, isoPath);
                        }
                        else {// CD already exists
                            continue;
                        }
                    }
                    break;

                case INTERFACE:
                    if (srcDeviceIdToTargetDeviceIdMapping.containsKey(device.getDeviceId())) {
                        id = srcDeviceIdToTargetDeviceIdMapping.get(device.getDeviceId());
                    }
                    break;

                case CONTROLLER:
                    if (VmDeviceType.USB.getName().equals(device.getDevice())) {
                        specParams = device.getSpecParams();
                    }
                    else if (VmDeviceType.VIRTIOSCSI.getName().equals(device.getDevice())) {
                        hasVirtioScsiController = true;
                        if (Boolean.FALSE.equals(isVirtioScsiEnabled)) {
                            continue;
                        }
                    }
                    break;

                case VIDEO:
                    if (isVm) {
                        // src is template and target is VM. video devices will be created according
                        // to the new VMStatic params
                        continue;
                    } else {
                    specParams.putAll(getMemExpr(vmBase.getNumOfMonitors(), vmBase.getSingleQxlPci()));
                    }
                    break;

                case BALLOON:
                    specParams.put(VdsProperties.Model, VdsProperties.Virtio);
                    break;

                case SMARTCARD:
                    specParams = new SmartcardSpecParams();
                    break;

                case WATCHDOG:
                    specParams.putAll(device.getSpecParams());
                    break;

                case RNG:
                    if (hasVmRngDevice(dstId)) {
                        continue; // don't copy rng device if we already have it
                    }
                    specParams.putAll(device.getSpecParams());
                    break;

                case CONSOLE:
                    hasAlreadyConsoleDevice = true;
                    if (!isConsoleEnabled) {
                        continue;
                    }
                    break;

                case SOUND:
                    hasSoundDevice = true;
                    if (!soundDeviceEnabled) {
                        continue;
                    }
                    break;

                default:
                    break;
            }
            device.setId(new VmDeviceId(id, dstId));
            device.setSpecParams(specParams);
            dao.save(device);
        }
        // if VM does not has CD, add an empty CD
        if (!shouldHaveCD) {
            addEmptyCD(dstId);
        }

        // if source doesnt have sound device and requested, add it
        if (soundDeviceEnabled && !hasSoundDevice) {
            if (isVm) {
                addSoundCard(vm.getStaticData(), vm.getVdsGroupCompatibilityVersion());
            } else {
                VDSGroup cluster = vmBase.getVdsGroupId() != null ? DbFacade.getInstance().getVdsGroupDao().get(vmBase.getVdsGroupId()) : null;
                if (cluster != null) {
                    addSoundCard(vmBase, cluster.getcompatibility_version());
                }
            }
        }

        if (isConsoleEnabled && !hasAlreadyConsoleDevice) {
            addConsoleDevice(dstId);
        }

        if (Boolean.TRUE.equals(isVirtioScsiEnabled) && !hasVirtioScsiController) {
            addVirtioScsiController(dstId);
        }

        if (isVm) {
            //  update devices boot order
            updateBootOrderInVmDeviceAndStoreToDB(vmBase);

            int numOfMonitors = (vmBase.getDefaultDisplayType() == DisplayType.vnc) ? Math.max(1, vmBase.getNumOfMonitors()) :
                vmBase.getSingleQxlPci() ? 1 : vmBase.getNumOfMonitors();
            // create Video device. Multiple if display type is spice
            for (int i = 0; i < numOfMonitors; i++) {
                addVideoDevice(vmBase);
            }
        }
    }

    private static void addSoundCard(VmBase vmBase) {
        addSoundCard(vmBase, ClusterUtils.getCompatibilityVersion(vmBase));
    }

    private static void addSoundCard(VmBase vmBase, Version vdsGroupCompatibilityVersion) {
        String soundDevice = osRepository.getSoundDevice(vmBase.getOsId(), vdsGroupCompatibilityVersion);
        addManagedDevice(new VmDeviceId(Guid.newGuid(), vmBase.getId()),
                VmDeviceGeneralType.SOUND,
                VmDeviceType.getSoundDeviceType(soundDevice),
                new HashMap<String, Object>(),
                true,
                true,
                null);
    }

    public static void copyVmDevices(Guid srcId,
                                     Guid dstId,
                                     Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping,
                                     boolean soundDeviceEnabled,
                                     boolean isConsoleEnabled,
                                     Boolean isVirtioScsiEnabled,
                                     boolean isBalloonEnabled,
                                     boolean copySnapshotDevices) {
        VM vm = DbFacade.getInstance().getVmDao().get(dstId);
        VmBase vmBase = (vm != null) ? vm.getStaticData() : null;
        boolean isVm = (vmBase != null);

        if (!isVm) {
            vmBase = DbFacade.getInstance().getVmTemplateDao().get(dstId);
        }

        List<VmDevice> devices = dao.getVmDeviceByVmId(srcId);
        copyVmDevices(srcId, dstId, vm, vmBase, isVm, devices, srcDeviceIdToTargetDeviceIdMapping,
                soundDeviceEnabled, isConsoleEnabled, isVirtioScsiEnabled, isBalloonEnabled, copySnapshotDevices);
    }

    public static void copyDiskDevices(Guid dstId, List<VmDevice> devicesDataToUse, Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        for (VmDevice device : devicesDataToUse) {
            if (VmDeviceType.DISK.getName().equals(device.getDevice())) {
                if (srcDeviceIdToTargetDeviceIdMapping.containsKey(device.getDeviceId())) {
                    Guid id = srcDeviceIdToTargetDeviceIdMapping.get(device.getDeviceId());
                    device.setId(new VmDeviceId(id, dstId));
                    device.setSpecParams(new HashMap<String, Object>());
                    dao.save(device);
                }
            }
        }
    }

    private static void addVideoDevice(VmBase vm) {
        VmDeviceType vmDeviceType = osRepository.getDisplayDevice(vm.getOsId(), ClusterUtils.getCompatibilityVersion(vm), vm.getDefaultDisplayType());

        addManagedDevice(
                new VmDeviceId(Guid.newGuid(), vm.getId()),
                VmDeviceGeneralType.VIDEO,
                vmDeviceType,
                getMemExpr(vm.getNumOfMonitors(), vm.getSingleQxlPci()),
                true,
                true,
                null);
    }

    private static void setCdPath(Map<String, Object> specParams, String srcCdPath, String isoPath) {
        // check if CD was set specifically for this VM
        if (!StringUtils.isEmpty(isoPath)){
            specParams.put(VdsProperties.Path, isoPath);
        } else if (!StringUtils.isEmpty(srcCdPath)) { // get the path from the source device spec params
            specParams.put(VdsProperties.Path, srcCdPath);
        } else {
            specParams.put(VdsProperties.Path, "");
        }
    }

    /**
     * Add a NIC device for the VM.
     *
     * @param id
     *            The NIC id (must correspond with the ID of the NIC in the VM).
     * @param plugged
     *            Is the NIC plugged to the VM or not.
     * @return The device that was added.
     */
    public static VmDevice addNetworkInterfaceDevice(VmDeviceId id, boolean plugged) {
        return addManagedDevice(id,
                VmDeviceGeneralType.INTERFACE,
                VmDeviceType.BRIDGE,
                Collections.<String, Object> emptyMap(),
                plugged,
                false,
                null);
    }

    /**
     * @param id
     * @param type
     * @param device
     * @param specParams
     * @param plugged
     * @param readOnly
     * @param address
     * @param customProp device custom properties
     * @return newly created VmDevice instance
     */
    public static VmDevice addManagedDevice(VmDeviceId id,
            VmDeviceGeneralType type,
            VmDeviceType device,
            Map<String, Object> specParams,
            boolean plugged,
            Boolean readOnly,
            String address,
            Map<String, String> customProp) {
        VmDevice managedDevice = addManagedDevice(id, type, device, specParams, plugged, readOnly, customProp);
        if (StringUtils.isNotBlank(address)){
            managedDevice.setAddress(address);
        }
        return managedDevice;
    }

    /**
     * adds managed device to vm_device
     *
     * @param id
     * @param type
     * @param device
     * @param customProp device custom properties
     * @return New created VmDevice instance
     */
    public static VmDevice addManagedDevice(VmDeviceId id,
            VmDeviceGeneralType type,
            VmDeviceType device,
            Map<String, Object> specParams,
            boolean is_plugged,
            Boolean isReadOnly,
            Map<String, String> customProp) {
        VmDevice managedDevice =
            new VmDevice(id,
                    type,
                    device.getName(),
                    "",
                    0,
                    specParams,
                    true,
                    is_plugged,
                    isReadOnly,
                    "",
                    customProp,
                    null);
        dao.save(managedDevice);
        // If we add Disk/Interface/CD/Floppy, we have to recalculate boot order
        if (type == VmDeviceGeneralType.DISK || type == VmDeviceGeneralType.INTERFACE) {
            // recalculate boot sequence
            VmBase vmBase = DbFacade.getInstance().getVmStaticDao().get(id.getVmId());
            updateBootOrderInVmDeviceAndStoreToDB(vmBase);
        }
        return managedDevice;
    }

    /**
     * adds imported VM or Template devices
     * @param entity
     */
    public static <T extends VmBase> void addImportedDevices(T entity, boolean isImportAsNewEntity) {
        if (isImportAsNewEntity) {
            setNewIdInImportedCollections(entity);
        }
        List<VmDevice> vmDeviceToAdd = new ArrayList<VmDevice>();
        List<VmDevice> vmDeviceToUpdate = new ArrayList<VmDevice>();
        VmDeviceDAO dao = DbFacade.getInstance().getVmDeviceDao();
        addImportedDisks(entity, vmDeviceToUpdate);
        addImportedInterfaces(entity, vmDeviceToUpdate);
        addOtherDevices(entity, vmDeviceToAdd);
        dao.saveAll(vmDeviceToAdd);
        dao.updateAll(vmDeviceToUpdate);
    }

    public static void setVmDevices(VmBase vmBase) {
        Map<Guid, VmDevice> vmManagedDeviceMap = new HashMap<Guid, VmDevice>();
        List<VmDevice> devices = DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmId(vmBase.getId());
        vmBase.setUnmanagedDeviceList(DbFacade.getInstance().getVmDeviceDao().getUnmanagedDevicesByVmId(vmBase.getId()));
        for (VmDevice device : devices) {
            if (device.getIsManaged()) {
                vmManagedDeviceMap.put(device.getDeviceId(), device);
            }
        }
        vmBase.setManagedDeviceMap(vmManagedDeviceMap);
    }

    /**
     * Updates VM boot order in vm device according to the BootSequence enum value.
     * Stores the updated devices in DB
     * @param vmBase
     */
    public static void updateBootOrderInVmDeviceAndStoreToDB(VmBase vmBase) {
        List<VmDevice> devices = updateBootOrderInVmDevice(vmBase);
        dao.updateBootOrderInBatch(devices);
    }

    /**
     * Updates boot order in vm device according to the BootSequence enum value.
     * @param vmBase
     * @return the updated VmDevice list
     */
    public static List<VmDevice> updateBootOrderInVmDevice(VmBase vmBase) {
        if (vmBase instanceof VmStatic) {
            //Returns the devices sorted in ascending order
            List<VmDevice> devices = dao.getVmDeviceByVmId(vmBase.getId());
            // reset current boot order
            for (VmDevice device: devices) {
                device.setBootOrder(0);
            }
            VM vm = DbFacade.getInstance().getVmDao().get(vmBase.getId());
            VmHandler.updateDisksForVm(vm, DbFacade.getInstance().getDiskDao().getAllForVm(vm.getId()));
            VmHandler.updateNetworkInterfacesFromDb(vm);
            boolean isOldCluster = VmDeviceCommonUtils.isOldClusterVersion(vm.getVdsGroupCompatibilityVersion());
            VmDeviceCommonUtils.updateVmDevicesBootOrder(vm, devices, isOldCluster);
            return devices;
        }
        return Collections.emptyList();
    }

    /**
     * updates existing VM CD ROM in vm_device
     *
     * @param oldVmBase
     * @param newVmBase
     *            NOTE : Only one CD is currently supported.
     */
    private static void updateCdInVmDevice(VmBase oldVmBase, VmBase newVmBase) {
        List<VmDevice> cdList = DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(oldVmBase.getId(), VmDeviceGeneralType.DISK, VmDeviceType.CDROM.getName());
        if (cdList.size() > 0){ // this is done only for safety, each VM must have at least an Empty CD
            VmDevice cd = cdList.get(0); // only one managed CD is currently supported.
            cd.getSpecParams()
                    .put(VdsProperties.Path, (newVmBase.getIsoPath() == null) ? "" : newVmBase.getIsoPath());
            dao.update(cd);
        }
    }

    /**
     * Updates new/existing VM USB slots in vm device
     * Currently assuming the number of slots is between 0 and SLOTS_PER_CONTROLLER, i.e., no more than one controller
     * @param oldVm
     * @param newVm
     */
    private static void updateUSBSlots(VmBase oldVm, VmBase newVm) {
        UsbPolicy oldUsbPolicy = UsbPolicy.DISABLED;
        UsbPolicy newUsbPolicy = newVm.getUsbPolicy();
        int currentNumberOfSlots = 0;

        if (oldVm != null) {
            oldUsbPolicy = oldVm.getUsbPolicy();
            currentNumberOfSlots = getUsbRedirectDevices(oldVm).size();
        }

        final int usbSlots = Config.<Integer> getValue(ConfigValues.NumberOfUSBSlots);

        // We add USB slots in case support doesn't exist in the oldVm configuration, but exists in the new one
        if (!oldUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE) && newUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE)) {
            if (usbSlots > 0) {
                removeUsbControllers(newVm);
                addUsbControllers(newVm, getNeededNumberOfUsbControllers(usbSlots));
                addUsbSlots(newVm, usbSlots);
            }
        }
        // Remove USB slots and controllers in case we are either in disabled policy or legacy one
        else if (newUsbPolicy.equals(UsbPolicy.DISABLED) || newUsbPolicy.equals(UsbPolicy.ENABLED_LEGACY)) {
            removeUsbControllers(newVm);
            removeUsbSlots(newVm);
        // if the USB policy is enabled (and was enabled before), we need to update the number of slots
        } else if (newUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE)) {
            if (currentNumberOfSlots < usbSlots) {
                // Add slots
                if (currentNumberOfSlots == 0) {
                    addUsbControllers(newVm, getNeededNumberOfUsbControllers(usbSlots));
                }
                addUsbSlots(newVm, usbSlots - currentNumberOfSlots);
            } else if (currentNumberOfSlots > usbSlots) {
                // Remove slots
                removeUsbSlots(newVm, currentNumberOfSlots - usbSlots);
                // Remove controllers
                if (usbSlots == 0) {
                    removeUsbControllers(newVm);
                }
            }
        }
    }

    private static int getNeededNumberOfUsbControllers(int numberOfSlots) {
        int numOfcontrollers = numberOfSlots / SLOTS_PER_CONTROLLER;
        // Need to add another controller in case mod result is not 0
        if (numberOfSlots % SLOTS_PER_CONTROLLER != 0) {
            numOfcontrollers++;
        }
        return numOfcontrollers;
    }
    /**
     * Adds imported disks to VM devices
     * @param entity
     */
    private static <T extends VmBase> void addImportedDisks(T entity, List<VmDevice> vmDeviceToUpdate) {
        final Guid id = entity.getId();
        for (BaseDisk disk : getDisks(entity.getImages())) {
            Guid deviceId = disk.getId();
            VmDevice vmDevice =
                    addManagedDevice(new VmDeviceId(deviceId, id),
                            VmDeviceGeneralType.DISK,
                            VmDeviceType.DISK,
                            null,
                            true,
                            false,
                            getAddress(entity, id),
                            null);
            updateVmDevice(entity, vmDevice, deviceId, vmDeviceToUpdate);
        }
    }

    /**
     * Gets a set of disks from disk images. For VM with snapshots, several DiskImage elements may contain the same Disk
     *
     * @param diskImages
     *            collection DiskImage objects to get a set of Disks from
     * @return set of disks of the images collection
     */
    protected static Set<BaseDisk> getDisks(Collection<DiskImage> diskImages) {
        Map<Guid, BaseDisk> diskMap = new HashMap<Guid, BaseDisk>();
        for (Disk diskImage : diskImages) {
            diskMap.put(diskImage.getId(), diskImage);
        }
        return new HashSet<BaseDisk>(diskMap.values());
    }

    private static <T extends VmBase> void updateVmDevice(T entity,
            VmDevice vmDevice,
            Guid deviceId,
            List<VmDevice> vmDeviceToUpdate) {
        // update device information only if ovf support devices - from 3.1
        Version ovfVer = new Version(entity.getOvfVersion());
        if (!VmDeviceCommonUtils.isOldClusterVersion(ovfVer)) {
            VmDevice exportedDevice = entity.getManagedDeviceMap().get(deviceId);
            if (exportedDevice != null) {
                vmDevice.setAddress(exportedDevice.getAddress());
                vmDevice.setBootOrder(exportedDevice.getBootOrder());
                vmDevice.setIsPlugged(exportedDevice.getIsPlugged());
                vmDevice.setIsReadOnly(exportedDevice.getIsReadOnly());
                vmDeviceToUpdate.add(vmDevice);
            }
        }
    }

    /**
     * If another plugged network interface has the same MAC address, return false, otherwise returns true
     *
     * @param iface
     *            the network interface to check if can be plugged
     */
    private static boolean canPlugInterface(VmNic iface) {
        VmInterfaceManager vmIfaceManager = new VmInterfaceManager();
        if (vmIfaceManager.existsPluggedInterfaceWithSameMac(iface)) {
            vmIfaceManager.auditLogMacInUseUnplug(iface);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Adds imported interfaces to VM devices
     * @param entity
     */
    private static <T extends VmBase> void addImportedInterfaces(T entity, List<VmDevice> vmDeviceToUpdate) {
        final Guid id = entity.getId();
        for (VmNic iface : entity.getInterfaces()) {
            Guid deviceId = iface.getId();
            VmDevice vmDevice =
                    addManagedDevice(new VmDeviceId(deviceId, id),
                            VmDeviceGeneralType.INTERFACE,
                            VmDeviceType.BRIDGE,
                            null,
                            true,
                            false,
                            getAddress(entity, id),
                            null);

            VmDevice exportedDevice = entity.getManagedDeviceMap().get(deviceId);
            if (exportedDevice == null) {
                entity.getManagedDeviceMap().put(deviceId, vmDevice);
                exportedDevice = vmDevice;
            }

            exportedDevice.setIsPlugged(exportedDevice.getIsPlugged() && canPlugInterface(iface));
            updateVmDevice(entity, vmDevice, deviceId, vmDeviceToUpdate);
        }
    }

    private static <T extends VmBase> String getAddress(T entity, final Guid id) {
        VmDevice device = entity.getManagedDeviceMap().get(id);
        if (device != null)
            return device.getAddress();
        else
            return StringUtils.EMPTY;
    }

    /**
     * Adds Special managed devices (monitor/CDROM ) and unmanaged devices
     *
     * @param <T>
     * @param entity
     */
    private static <T extends VmBase> void addOtherDevices(T entity, List<VmDevice> vmDeviceToAdd) {
        boolean hasCD = false;
        boolean hasSoundCard = false;
        for (VmDevice vmDevice : entity.getManagedDeviceMap().values()) {
            if (isDiskOrInterface(vmDevice)) {
                continue; // skip disks/interfaces that were added separately.
            }
            vmDevice.setIsManaged(true);
            if (vmDevice.getType() == VmDeviceGeneralType.VIDEO) {
                vmDevice.setSpecParams(getMemExpr(entity.getNumOfMonitors(), entity.getSingleQxlPci()));
            }
            if (vmDevice.getDevice().equals(VmDeviceType.CDROM.getName())){
                hasCD = true;
            }
            if (VmDeviceCommonUtils.isSound(vmDevice)) {
                hasSoundCard = true;
            }
            vmDeviceToAdd.add(vmDevice);
        }
        if (!hasCD) { // add an empty CD
            addEmptyCD(entity.getId());
        }

        // add sound card for desktops imported from old versions only, since devices didnt exist
        Version ovfVer = new Version(entity.getOvfVersion());
        if (!hasSoundCard && VmDeviceCommonUtils.isOldClusterVersion(ovfVer) && entity.getVmType() == VmType.Desktop) {
            addSoundCard(entity);
        }
        for (VmDevice vmDevice : entity.getUnmanagedDeviceList()) {
            vmDeviceToAdd.add(vmDevice);
        }
    }

    /**
     * gets Monitor memory expression
     *
     * @param numOfMonitors
     *            Number of monitors
     * @return
     */
    private static Map<String, Object> getMemExpr(int numOfMonitors, boolean singleQxlPci) {
        int heads = singleQxlPci ? numOfMonitors : 1;
        Map<String, Object> specParams = new HashMap<String, Object>();
        specParams.put(HEADS, String.valueOf(heads));
        specParams.put(VRAM, VmDeviceCommonUtils.singlePciVRamByHeads(heads));
        if (singleQxlPci) {
            specParams.put(RAM, VmDeviceCommonUtils.singlePciRamByHeads(heads));
        }
        return specParams;
    }

    private static void addUsbSlots(VmBase vm, int numOfSlots) {
        for (int index = 1; index <= numOfSlots; index++) {
            VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                    VmDeviceGeneralType.REDIR,
                    VmDeviceType.SPICEVMC,
                    getUsbSlotSpecParams(),
                    true,
                    false,
                    null);
        }
    }

    private static void addUsbControllers(VmBase vm, int numOfControllers) {
        // For each controller we need to create one EHCI and companion UHCI controllers
        for (int index = 0; index < numOfControllers; index++) {
            VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                    VmDeviceGeneralType.CONTROLLER,
                    VmDeviceType.USB,
                    getUsbControllerSpecParams(EHCI_MODEL, 1, index),
                    true,
                    false,
                    null);
            for (int companionIndex = 1; companionIndex <= COMPANION_USB_CONTROLLERS; companionIndex++) {
                VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                        VmDeviceGeneralType.CONTROLLER,
                        VmDeviceType.USB,
                        getUsbControllerSpecParams(UHCI_MODEL, companionIndex, index),
                        true,
                        false,
                        null);
            }
        }
    }

    /**
     * gets USB controller
     *
     * @return
     */
    private static Map<String, Object> getUsbControllerSpecParams(String model, int controllerNumber, int index) {
        Map<String, Object> specParams = new HashMap<String, Object>();
        specParams.put(VdsProperties.Model, model + controllerNumber);
        specParams.put(VdsProperties.Index, Integer.toString(index));
        return specParams;
    }

    /**
     * gets USB slot specParams
     *
     * @return
     */
    private static Map<String, Object> getUsbSlotSpecParams() {
        Map<String, Object> specParams = new HashMap<String, Object>();
        return specParams;
    }

    private static List<VmDevice> getUsbRedirectDevices(VmBase vm) {
        List<VmDevice> list = dao.getVmDeviceByVmIdTypeAndDevice(vm.getId(), VmDeviceGeneralType.REDIR, VmDeviceType.SPICEVMC.getName());

        return list;
    }
    private static void removeUsbSlots(VmBase vm) {
        List<VmDevice> list = getUsbRedirectDevices(vm);
        for(VmDevice vmDevice : list) {
            dao.remove(vmDevice.getId());
        }
    }

    private static void removeUsbSlots(VmBase vm, int numberOfSlotsToRemove) {
        List<VmDevice> list = getUsbRedirectDevices(vm);
        removeNumberOfDevices(list, numberOfSlotsToRemove);
    }

    private static void removeNumberOfDevices(List<VmDevice> devices, int numberOfDevicesToRemove) {
        int size = devices.size();
        for (int index = 1; index <= numberOfDevicesToRemove; index++) {
            if (size >= index) {
                dao.remove(devices.get(size - index).getId());
            }
        }
    }

    private static void removeUsbControllers(VmBase vm) {
        List<VmDevice> list = dao.getVmDeviceByVmIdTypeAndDevice(vm.getId(), VmDeviceGeneralType.CONTROLLER, VmDeviceType.USB.getName());
        for(VmDevice vmDevice : list) {
            dao.remove(vmDevice.getId());
        }
    }

    /**
     * adds an empty CD in the case that we have no CDROM inside the device
     * @param dstId
     */
    private static void addEmptyCD(Guid dstId) {
        VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.newGuid(), dstId),
                VmDeviceGeneralType.DISK,
                VmDeviceType.CDROM,
                Collections.<String, Object> singletonMap(VdsProperties.Path, ""),
                true,
                true,
                null);
    }

    private static void updateMemoryBalloon(VmBase newVm, boolean shouldHaveBalloon) {
        updateMemoryBalloon(newVm.getId(), shouldHaveBalloon);

    }

    public static void updateMemoryBalloon(Guid id, boolean shouldHaveBalloon) {
        boolean hasBalloon = dao.isMemBalloonEnabled(id);
        if (hasBalloon != shouldHaveBalloon) {
            if (!hasBalloon && shouldHaveBalloon) {
                // add a balloon device
                Map<String, Object> specParams = new HashMap<String, Object>();
                specParams.put(VdsProperties.Model, VdsProperties.Virtio);
                addManagedDevice(new VmDeviceId(Guid.newGuid(), id) , VmDeviceGeneralType.BALLOON, VmDeviceType.MEMBALLOON, specParams, true, true, null);
            }
            else {
                // remove the balloon device
                List<VmDevice> list = DbFacade
                        .getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(id,
                                VmDeviceGeneralType.BALLOON);
                removeNumberOfDevices(list, 1);
            }
        }
    }

    private static void setNewIdInImportedCollections(VmBase entity) {
        for (VmDevice managedDevice : entity.getManagedDeviceMap().values()){
            if (!isDiskOrInterface(managedDevice)) {
                managedDevice.setId(new VmDeviceId(Guid.newGuid(), entity.getId()));
            }
        }
        for (VmDevice unMnagedDevice : entity.getUnmanagedDeviceList()) {
            unMnagedDevice.setId(new VmDeviceId(Guid.newGuid(), entity.getId()));
        }
    }

    private static boolean isDiskOrInterface(VmDevice vmDevice) {
        return VmDeviceCommonUtils.isDisk(vmDevice) || VmDeviceCommonUtils.isBridge(vmDevice);
    }

    private static boolean isBalloonEnabled(VmManagementParametersBase params) {
        Boolean balloonEnabled = params.isBalloonEnabled();
        return balloonEnabled != null ? balloonEnabled :
            osRepository.isBalloonEnabled(params.getVmStaticData().getOsId(),
                    ClusterUtils.getCompatibilityVersion(params.getVmStaticData()));
    }

    public static boolean isVirtioScsiControllerAttached(Guid vmId) {
        return !getVirtioScsiControllers(vmId).isEmpty();
    }

    public static boolean hasWatchdog(Guid vmId) {
        return !getWatchdogs(vmId).isEmpty();
    }

    public static boolean isBalloonEnabled(Guid vmId) {
        return !getBalloonDevices(vmId).isEmpty();
    }

    public static boolean isSoundDeviceEnabled(Guid vmId) {
        return !getSoundDevices(vmId).isEmpty();
    }

    public static boolean hasVmRngDevice(Guid vmId) {
        return !DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdAndType(vmId,
                VmDeviceGeneralType.RNG).isEmpty();
    }

    public static List<VmDevice> getSoundDevices(Guid vmId) {
        return DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdAndType(vmId,
                VmDeviceGeneralType.SOUND);
    }

    public static List<VmDevice> getBalloonDevices(Guid vmId) {
        return DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdAndType(vmId,
                VmDeviceGeneralType.BALLOON);
    }

    public static List<VmDevice> getWatchdogs(Guid vmId) {
        return DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdAndType(vmId,
                VmDeviceGeneralType.WATCHDOG);
    }

    public static List<VmDevice> getVirtioScsiControllers(Guid vmId) {
        return getVirtioScsiControllers(vmId, null, false);
    }

    public static List<VmDevice> getVirtioScsiControllers(Guid vmId, Guid userID, boolean isFiltered) {
        return dao.getVmDeviceByVmIdTypeAndDevice(
                vmId, VmDeviceGeneralType.CONTROLLER, VmDeviceType.VIRTIOSCSI.getName(), userID, isFiltered);
    }

    /**
     * Determines whether a VM device change has been request by the user.
     * @param deviceType VmDeviceGeneralType.
     * @param device VmDeviceType name.
     * @param deviceEnabled indicates whether the user asked to enable the device.
     * @return true if a change has been requested; otherwise, false
     */
    public static boolean vmDeviceChanged(Guid vmId, VmDeviceGeneralType deviceType, String device, boolean deviceEnabled) {
        List<VmDevice> vmDevices = device != null ?
                dao.getVmDeviceByVmIdTypeAndDevice(vmId, deviceType, device):
                dao.getVmDeviceByVmIdAndType(vmId, deviceType);

        return deviceEnabled == vmDevices.isEmpty();
    }

    public static boolean vmDeviceChanged(Guid vmId, VmDeviceGeneralType deviceType, boolean deviceEnabled) {
        return vmDeviceChanged(vmId, deviceType, null, deviceEnabled);
    }

    /**
     * Returns a map (device ID to VmDevice) of devices that are relevant for next run by examining
     * properties that are annotated by EditableDeviceOnVmStatusField.
     * @param vm the relevant VM
     * @param objectWithEditableDeviceFields object that contains properties which are annotated with
     *                                       EditableDeviceField (e.g. parameters file)
     * @return a map of device ID to VmDevice object of relevant devices for next run
     */
    public static Map<Guid, VmDevice> getVmDevicesForNextRun(VM vm, Object objectWithEditableDeviceFields) {
        VmDeviceUtils.setVmDevices(vm.getStaticData());
        Map<Guid, VmDevice> vmManagedDeviceMap = vm.getManagedVmDeviceMap();

        List<Pair<EditableDeviceOnVmStatusField, Boolean>> fieldList =
                VmHandler.getVmDevicesFieldsToUpdateOnNextRun(vm.getId(), vm.getStatus(), objectWithEditableDeviceFields);

        // Add the enabled devices and remove the disabled ones
        for (Pair<EditableDeviceOnVmStatusField, Boolean> pair : fieldList) {
            final EditableDeviceOnVmStatusField field = pair.getFirst();
            Boolean isEnabled = pair.getSecond();

            if (Boolean.TRUE.equals(isEnabled)) {
                VmDevice device =
                        new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                                field.generalType(),
                                field.type().getName(),
                                "",
                                0,
                                new HashMap<String, Object>(),
                                true,
                                true,
                                field.isReadOnly(),
                                "",
                                null,
                                null);

                vmManagedDeviceMap.put(device.getDeviceId(), device);
            }
            else {
                vmManagedDeviceMap.remove(getVmDeviceIdByName(vmManagedDeviceMap, field.generalType(), field.type().getName()));
            }
        }

        return vmManagedDeviceMap;
    }

    private static Guid getVmDeviceIdByName(Map<Guid, VmDevice> vmManagedDeviceMap, VmDeviceGeneralType generalType, String name) {
        for (Map.Entry<Guid, VmDevice> entry : vmManagedDeviceMap.entrySet()) {
            // first check by specific name
            // if no match, and specific name is unknown, look by general type
            if (entry.getValue().getDevice().equals(name)
                    || (VmDeviceType.UNKNOWN.getName().equals(name) && entry.getValue().getType() == generalType)) {
                return entry.getKey();
            }
        }

        return null;
    }
}
