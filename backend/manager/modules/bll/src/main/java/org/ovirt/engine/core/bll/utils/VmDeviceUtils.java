package org.ovirt.engine.core.bll.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.smartcard.SmartcardSpecParams;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmInfoBuilderBase;

public class VmDeviceUtils {
    private static VmDeviceDAO dao = DbFacade.getInstance().getVmDeviceDao();
    private final static String VRAM = "vram";
    private final static String EHCI_MODEL = "ich9-ehci";
    private final static String UHCI_MODEL = "ich9-uhci";
    private final static int SLOTS_PER_CONTROLLER = 6;
    private final static int COMPANION_USB_CONTROLLERS = 3;

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
            if (oldVmBase.getDefaultDisplayType() != entity.getDefaultDisplayType()) {
                // delete all video device
                for (VmDevice device : dao.getVmDeviceByVmIdAndType(oldVmBase.getId(), VmDeviceType.VIDEO.getName())) {
                    dao.remove(device.getId());
                }
                // add video device per each monitor
                for (int i = 0; i<entity.getNumOfMonitors();i++) {
                    addManagedDevice(new VmDeviceId(Guid.NewGuid(), entity.getId()),
                            VmDeviceType.VIDEO,
                            entity.getDefaultDisplayType().getVmDeviceType(),
                            getMemExpr(entity.getNumOfMonitors()),
                            true,
                            false);
                }
            } else if (entity.getDefaultDisplayType() == DisplayType.qxl && oldVmBase.getNumOfMonitors() != entity
                    .getNumOfMonitors()) {
                // spice number of monitors has changed
                updateNumOfMonitorsInVmDevice(oldVmBase, entity);
            }
            updateUSBSlots(oldVmBase, entity);
            updateMemoryBalloon(oldVmBase, entity, params.isBalloonEnabled());

            updateAudioDevice(oldVm, entity);
            updateSmartcardDevice(oldVm, entity);
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
        if (!smartcardEnabled) {
            List<VmDevice> vmDevices =
                    DbFacade.getInstance()
                            .getVmDeviceDao()
                            .getVmDeviceByVmIdTypeAndDevice(vmId,
                                    VmDeviceType.SMARTCARD.getName(),
                                    VmDeviceType.SMARTCARD.getName());
            for (VmDevice device : vmDevices) {
                dao.remove(device.getId());
            }
        } else {
            addSmartcardDevice(vmId);
        }
    }

    public static void addSmartcardDevice(Guid vmId) {
        VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.NewGuid(), vmId),
                VmDeviceType.SMARTCARD,
                VmDeviceType.SMARTCARD,
                new SmartcardSpecParams(),
                true,
                false);
    }

    /**
     * Replace desktop-vm audio device if OS has changed
     *
     * @param oldVm
     * @param newVmBase
     */
    private static void updateAudioDevice(VM oldVm, VmBase newVmBase) {
        // for desktop, if the os type has changed, recreate Audio devices
        if (newVmBase.getVmType() == VmType.Desktop && oldVm.getOs() != newVmBase.getOs()) {
            Guid vmId = oldVm.getId();
            // remove any old sound device
            List<VmDevice> list =
                    DbFacade.getInstance()
                            .getVmDeviceDao()
                            .getVmDeviceByVmIdAndType(vmId, VmDeviceType.SOUND.getName());
            removeNumberOfDevices(list, list.size());

            // create new device
            String soundDevice =
                    VmInfoBuilderBase.getSoundDevice(newVmBase, oldVm.getVdsGroupCompatibilityVersion());
            addManagedDevice(new VmDeviceId(Guid.NewGuid(), vmId),
                    VmDeviceType.SOUND,
                    VmDeviceType.getSoundDeviceType(soundDevice),
                    new HashMap<String, Object>(),
                    true,
                    true);
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
     * Copies relevant entries on "Vm from Template" or "Template from VM" creation.
     *
     * @param srcId
     * @param dstId
     * @param disks
     *            The disks which were saved for the destination VM.
     */
    public static void copyVmDevices(Guid srcId, Guid dstId, List<DiskImage> disks, List<VmNetworkInterface> ifaces) {
        Guid id;
        VM vm = DbFacade.getInstance().getVmDao().get(dstId);
        VmBase vmBase = (vm != null) ? vm.getStaticData() : null;
        int diskCount = 0;
        int ifaceCount = 0;
        boolean isVm = (vmBase != null);
        if (!isVm) {
            vmBase = DbFacade.getInstance().getVmTemplateDao().get(dstId);
        }
        List<VmDevice> devices = dao.getVmDeviceByVmId(srcId);
        String isoPath=vmBase.getIsoPath();
        // indicates that VM should have CD either from its own (iso_path) or from the snapshot it was cloned from.
        boolean shouldHaveCD = StringUtils.isNotEmpty(isoPath);
        // indicates if VM has already a non empty CD in DB
        boolean hasAlreadyCD = (!(DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(vmBase.getId(), VmDeviceType.DISK.getName(), VmDeviceType.CDROM.getName())).isEmpty());
        boolean addCD = (!hasAlreadyCD && shouldHaveCD);
        for (VmDevice device : devices) {
            id = Guid.NewGuid();
            Map<String, Object> specParams = new HashMap<String, Object>();
            if (srcId.equals(Guid.Empty)) {
                //add CD if not exists
                if (addCD) {
                    setCdPath(specParams, "", isoPath);
                    addManagedDevice(new VmDeviceId(Guid.NewGuid(),dstId) , VmDeviceType.DISK, VmDeviceType.CDROM, specParams, true, true);
                }
                // updating USB slots
                updateUSBSlots(null, vmBase);
                // add mem balloon if defined
                updateMemoryBalloon(null, vmBase, vm.isBalloonEnabled());
            }
            if (VmDeviceType.DISK.getName().equals(device.getType())
                    && VmDeviceType.DISK.getName().equals(device.getDevice())) {
                if (diskCount < disks.size()) {
                    id = (disks.get(diskCount++)).getId();
                }
            } else if (VmDeviceType.INTERFACE.getName().equals(device.getType())) {
                if (ifaceCount < ifaces.size()) {
                    id = ifaces.get(ifaceCount++).getId();
                }
            } else if (VmDeviceType.CONTROLLER.getName().equals(device.getType())
                    && VmDeviceType.USB.getName().equals(device.getDevice())) {
                specParams = device.getSpecParams();
            } else if (VmDeviceType.VIDEO.getName().equals(device.getType())) {
                if (isVm) {
                    // src is template and target is VM. video devices will be created according
                    // to the new VMStatic params
                    continue;
                } else {
                    specParams.putAll(getMemExpr(vmBase.getNumOfMonitors()));
                }
            } else if (VmDeviceType.DISK.getName().equals(device.getType())
                    && VmDeviceType.CDROM.getName().equals(device.getDevice())) {
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
            else if (VmDeviceType.BALLOON.getName().equals(device.getType())){
                specParams.put(VdsProperties.Model, VdsProperties.Virtio);
            } else if (VmDeviceType.SMARTCARD.getName().equals(device.getType())) {
                specParams = new SmartcardSpecParams();
            }
            device.setId(new VmDeviceId(id, dstId));
            device.setSpecParams(specParams);
            dao.save(device);
        }
        // if VM does not has CD, add an empty CD
        if (!shouldHaveCD) {
            addEmptyCD(dstId);
        }

        if (isVm) {
            //  update devices boot order
            updateBootOrderInVmDeviceAndStoreToDB(vmBase);

            // create sound card for a desktop VM if not exists
            if (vmBase.getVmType() == VmType.Desktop) {
                List<VmDevice> list = DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdAndType(vmBase.getId(), VmDeviceType.SOUND.getName());
                if (list.size() == 0) {
                    String soundDevice = VmInfoBuilderBase.getSoundDevice(vm.getStaticData(), vm.getVdsGroupCompatibilityVersion());
                    addManagedDevice(new VmDeviceId(Guid.NewGuid(), vmBase.getId()),
                            VmDeviceType.SOUND,
                            VmDeviceType.getSoundDeviceType(soundDevice),
                            new HashMap<String, Object>(),
                            true,
                            true);
                }
            }
            int numOfMonitors = (vm.getDisplayType() == DisplayType.vnc) ? Math.max(1, vm.getNumOfMonitors()) : vm.getNumOfMonitors();
            // create Video device. Multiple if display type is spice
            for (int i = 0; i < numOfMonitors; i++) {
                addVideoDevice(vm);
            }

        }
    }

    private static void addVideoDevice(VM vm) {
        addManagedDevice(
                new VmDeviceId(Guid.NewGuid(),vm.getId()),
                VmDeviceType.VIDEO,
                vm.getDefaultDisplayType().getVmDeviceType(),
                getMemExpr(vm.getNumOfMonitors()),
                true,
                true);
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
        return addManagedDevice(id, VmDeviceType.INTERFACE, VmDeviceType.BRIDGE, Collections.<String, Object> emptyMap(), plugged, false);
    }

    /**
     * @param id
     * @param type
     * @param device
     * @param specParams
     * @param plugged
     * @param readOnly
     * @param address
     * @return newly created VmDevice instance
     */
    public static VmDevice addManagedDevice(VmDeviceId id,
            VmDeviceType type,
            VmDeviceType device,
            Map<String, Object> specParams,
            boolean plugged,
            boolean readOnly,
            String address) {
        VmDevice managedDevice = addManagedDevice(id, type, device, specParams,plugged,readOnly);
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
     * @return New created VmDevice instance
     */
    public static VmDevice addManagedDevice(VmDeviceId id,
            VmDeviceType type,
            VmDeviceType device,
            Map<String, Object> specParams,
            boolean is_plugged,
            boolean isReadOnly) {
        VmDevice managedDevice =
            new VmDevice(id,
                        type.getName(),
                        device.getName(),
                    "",
                    0,
                    specParams,
                    true,
                    is_plugged,
                    isReadOnly,
                    "");
        dao.save(managedDevice);
        // If we add Disk/Interface/CD/Floppy, we have to recalculate boot order
        if (type.equals(VmDeviceType.DISK) || type.equals(VmDeviceType.INTERFACE )) {
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
        for (VmDevice device : devices) {
           dao.update(device);
        }
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
        List<VmDevice> cdList = DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(oldVmBase.getId(), VmDeviceType.DISK.getName(), VmDeviceType.CDROM.getName());
        if (cdList.size() > 0){ // this is done only for safety, each VM must have at least an Empty CD
            VmDevice cd = cdList.get(0); // only one managed CD is currently supported.
            cd.getSpecParams()
                    .put(VdsProperties.Path, (newVmBase.getIsoPath() == null) ? "" : newVmBase.getIsoPath());
            dao.update(cd);
        }
    }

    /**
     * updates new/existing VM video cards in vm device
     * @param oldVmBase
     * @param newStatic
     */
    private static void updateNumOfMonitorsInVmDevice(VmBase oldVmBase,
            VmBase newStatic) {
        int prevNumOfMonitors=0;
        if (oldVmBase != null) {
            prevNumOfMonitors = oldVmBase.getNumOfMonitors();
        }
        if (newStatic.getNumOfMonitors() > prevNumOfMonitors) {
            // monitors were added
            for (int i = prevNumOfMonitors; i < newStatic
                    .getNumOfMonitors(); i++) {
                Guid newId = Guid.NewGuid();
                VmDeviceUtils.addManagedDevice(new VmDeviceId(newId, newStatic.getId()),
                        VmDeviceType.VIDEO,
                        VmDeviceType.QXL,
                        getMemExpr(newStatic.getNumOfMonitors()),
                        true,
                        false);
            }
        } else { // delete video cards
            List<VmDevice> list = DbFacade
                    .getInstance()
                    .getVmDeviceDao()
                    .getVmDeviceByVmIdAndType(newStatic.getId(),
                            VmDeviceType.VIDEO.getName());
            removeNumberOfDevices(list, prevNumOfMonitors - newStatic.getNumOfMonitors());
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

        final int usbSlots = Config.<Integer> GetValue(ConfigValues.NumberOfUSBSlots);

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
                            VmDeviceType.DISK,
                            VmDeviceType.DISK,
                            null,
                            true,
                            false,
                            getAddress(entity, id));
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
    private static boolean canPlugInterface(VmNetworkInterface iface) {
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
        for (VmNetworkInterface iface : entity.getInterfaces()) {
            Guid deviceId = iface.getId();
            VmDevice vmDevice =
                    addManagedDevice(new VmDeviceId(deviceId, id),
                            VmDeviceType.INTERFACE,
                            VmDeviceType.BRIDGE,
                            null,
                            true,
                            false,
                            getAddress(entity, id));

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
        for (VmDevice vmDevice : entity.getManagedDeviceMap().values()) {
            if (isDiskOrInterface(vmDevice)) {
                continue; // skip disks/interfaces that were added separately.
            }
            vmDevice.setIsManaged(true);
            if (vmDevice.getType().equals(VmDeviceType.VIDEO.getName())) {
                vmDevice.setSpecParams(getMemExpr(entity.getNumOfMonitors()));
            }
            if (vmDevice.getDevice().equals(VmDeviceType.CDROM.getName())){
                hasCD = true;
            }
            vmDeviceToAdd.add(vmDevice);
        }
        if (!hasCD) { // add an empty CD
            addEmptyCD(entity.getId());
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
    private static Map<String, Object> getMemExpr(int numOfMonitors) {
        String mem = (numOfMonitors > 2 ? VmDeviceCommonUtils.LOW_VIDEO_MEM : VmDeviceCommonUtils.HIGH_VIDEO_MEM);
        Map<String, Object> specParams = new HashMap<String, Object>();
        specParams.put(VRAM, mem);
        return specParams;
    }

    private static void addUsbSlots(VmBase vm, int numOfSlots) {
        for (int index = 1; index <= numOfSlots; index++) {
            VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                    VmDeviceType.REDIR,
                    VmDeviceType.SPICEVMC,
                    getUsbSlotSpecParams(),
                    true,
                    false);
        }
    }

    private static void addUsbControllers(VmBase vm, int numOfControllers) {
        // For each controller we need to create one EHCI and companion UHCI controllers
        for (int index = 0; index < numOfControllers; index++) {
            VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                    VmDeviceType.CONTROLLER,
                    VmDeviceType.USB,
                    getUsbControllerSpecParams(EHCI_MODEL, 1, index),
                    true,
                    false);
            for (int companionIndex = 1; companionIndex <= COMPANION_USB_CONTROLLERS; companionIndex++) {
                VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                        VmDeviceType.CONTROLLER,
                        VmDeviceType.USB,
                        getUsbControllerSpecParams(UHCI_MODEL, companionIndex, index),
                        true,
                        false);
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
        List<VmDevice> list = dao.getVmDeviceByVmIdTypeAndDevice(vm.getId(),VmDeviceType.REDIR.getName(), VmDeviceType.SPICEVMC.getName());

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
        List<VmDevice> list = dao.getVmDeviceByVmIdTypeAndDevice(vm.getId(), VmDeviceType.CONTROLLER.getName(), VmDeviceType.USB.getName());
        for(VmDevice vmDevice : list) {
            dao.remove(vmDevice.getId());
        }
    }

    /**
     * adds an empty CD in the case that we have no CDROM inside the device
     * @param dstId
     */
    private static void addEmptyCD(Guid dstId) {
        VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.NewGuid(), dstId),
                VmDeviceType.DISK,
                VmDeviceType.CDROM,
                Collections.<String, Object> singletonMap(VdsProperties.Path, ""),
                true,
                true);
    }

    private static void updateMemoryBalloon(VmBase oldVm, VmBase newVm, boolean shouldHaveBalloon) {
        Guid id = newVm.getId();
        boolean hasBalloon = dao.isMemBalloonEnabled(id);
        if (hasBalloon != shouldHaveBalloon) {
            if (!hasBalloon && shouldHaveBalloon) {
                // add a balloon device
                Map<String,Object> specParams = new HashMap<String, Object>();
                specParams.put(VdsProperties.Model, VdsProperties.Virtio);
                addManagedDevice(new VmDeviceId(Guid.NewGuid(),newVm.getId()) , VmDeviceType.BALLOON, VmDeviceType.MEMBALLOON, specParams, true, true);
            }
            else {
                // remove the balloon device
                List<VmDevice> list = DbFacade
                .getInstance()
                .getVmDeviceDao()
                .getVmDeviceByVmIdAndType(newVm.getId(),
                        VmDeviceType.BALLOON.getName());
                removeNumberOfDevices(list,1);
            }
        }
    }

    private static void setNewIdInImportedCollections(VmBase entity) {
        for (VmDevice managedDevice : entity.getManagedDeviceMap().values()){
            if (!isDiskOrInterface(managedDevice)) {
                managedDevice.setId(new VmDeviceId(Guid.NewGuid(), entity.getId()));
            }
        }
        for (VmDevice unMnagedDevice : entity.getUnmanagedDeviceList()) {
            unMnagedDevice.setId(new VmDeviceId(Guid.NewGuid(), entity.getId()));
        }
    }

    private static boolean isDiskOrInterface(VmDevice vmDevice) {
        return(vmDevice.getDevice().equals(VmDeviceType.DISK.getName()) && vmDevice.getType().equals(VmDeviceType.DISK.getName())) ||
        (vmDevice.getDevice().equals(VmDeviceType.BRIDGE.getName())
        && vmDevice.getType().equals(VmDeviceType.INTERFACE.getName()));
    }
}

