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
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmInfoBuilderBase;

public class VmDeviceUtils {
    private static VmDeviceDAO dao = DbFacade.getInstance().getVmDeviceDAO();
    private final static String VRAM = "vram";
    private final static String EHCI_MODEL = "ich9-ehci";
    private final static String UHCI_MODEL = "ich9-uhci";
    private final static int SLOTS_PER_CONTROLLER = 6;
    private final static int COMPANION_USB_CONTROLLERS = 3;


    /**
     * Update the vm devices according to changes made in vm static for existing VM
     */
    public static <T extends VmBase> void updateVmDevices(T entity, VmBase oldVmBase) {
        VmBase newVmBase = getBaseObject(entity, oldVmBase.getId());
        if (newVmBase != null) {
            updateCdInVmDevice(oldVmBase, newVmBase);
            if (oldVmBase.getdefault_boot_sequence() != newVmBase
                    .getdefault_boot_sequence()) {
                updateBootOrderInVmDevice(newVmBase);
            }
            if (oldVmBase.getnum_of_monitors() != newVmBase
                    .getnum_of_monitors()) {
                updateNumOfMonitorsInVmDevice(oldVmBase, newVmBase);
            }
            updateUSBSlots(oldVmBase, newVmBase);
        }
    }

    /**
     * Update the vm devices according to changes made configuration
     */
    public static <T extends VmBase> void updateVmDevices(T entity) {
        VmBase vmBase = getBaseObject(entity, entity.getId());
        if (vmBase != null) {
            updateUSBSlots(vmBase, vmBase);
        }
    }

    /**
     * Update the vm devices according to changes made in vm static for new VM
     */

    public static <T extends VmBase> void updateVmDevices(T entity, Guid newId) {
        VmBase newVmBase = getBaseObject(entity, newId);
        if (newVmBase != null) {
            updateCdInVmDevice(newVmBase);
            updateBootOrderInVmDevice(newVmBase);
            updateNumOfMonitorsInVmDevice(null, newVmBase);
            updateUSBSlots(null, newVmBase);
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
        VmBase vmBase = DbFacade.getInstance().getVmStaticDAO().get(dstId);
        int diskCount = 0;
        int ifaceCount = 0;
        boolean isVm = (vmBase != null);
        if (!isVm) {
            vmBase = DbFacade.getInstance().getVmTemplateDAO().get(dstId);
        }
        List<VmDevice> devices = dao.getVmDeviceByVmId(srcId);
        String isoPath=vmBase.getiso_path();
        // indicates that VM should have CD either from its own (iso_path) or from the snapshot it was cloned from.
        boolean shouldHaveCD = StringUtils.isNotEmpty(isoPath);
        // indicates if VM has already a non empty CD in DB
        boolean hasAlreadyCD = (!(DbFacade.getInstance().getVmDeviceDAO().getVmDeviceByVmIdTypeAndDevice(vmBase.getId(), VmDeviceType.DISK.getName(), VmDeviceType.CDROM.getName())).isEmpty());
        boolean addCD = (!hasAlreadyCD && shouldHaveCD);
        for (VmDevice device : devices) {
            id = Guid.NewGuid();
            Map<String, Object> specParams = new HashMap<String, Object>();
            if (srcId.equals(Guid.Empty)) {
                // update number of monitors
                updateNumOfMonitorsInVmDevice(null, vmBase);
                //add CD if not exists
                if (addCD) {
                    setCdPath(specParams, "", isoPath);
                    addManagedDevice(new VmDeviceId(Guid.NewGuid(),dstId) , VmDeviceType.DISK, VmDeviceType.CDROM, specParams, true, true);
                }
                // updating USB slots
                updateUSBSlots(null, vmBase);
                break; // skip other Blank template devices
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
            } else if (VmDeviceType.VIDEO.getName().equals(device.getType())) {
                specParams.putAll(getMemExpr(vmBase.getnum_of_monitors()));
            }
            else  if (VmDeviceType.DISK.getName().equals(device.getType())
                    && VmDeviceType.CDROM.getName().equals(device.getDevice())) {
                // check here is source VM had CD (Vm from snapshot)
                String srcCdPath = (String) device.getSpecParams().get(VdsProperties.Path);
                shouldHaveCD = (!srcCdPath.isEmpty() || shouldHaveCD);
                if (!hasAlreadyCD && shouldHaveCD) {
                    setCdPath(specParams, srcCdPath, isoPath);
                }
                else {// CD already exists
                    continue;
                }
            }
            device.setId(new VmDeviceId(id, dstId));
            device.setSpecParams(specParams);
            dao.save(device);
        }
        // if VM does not has CD, add an empty CD
        if (!shouldHaveCD) {
            addEmptyCD(dstId);
        }
        // if destination is a VM , update devices boot order
        if (isVm) {
            updateBootOrderInVmDevice(vmBase);
            // create sound card for a desktop VM if not exists
            if (vmBase.getvm_type() == VmType.Desktop) {
                List<VmDevice> list = DbFacade.getInstance().getVmDeviceDAO().getVmDeviceByVmIdAndType(vmBase.getId(), VmDeviceType.SOUND.getName());
                if (list.size() == 0) {
                    VM vm = DbFacade.getInstance().getVmDAO().get(vmBase.getId());
                    String soundDevice = VmInfoBuilderBase.getSoundDevice(vm);
                    addManagedDevice(new VmDeviceId(Guid.NewGuid(), vmBase.getId()),
                            VmDeviceType.SOUND,
                            VmDeviceType.getSoundDeviceType(soundDevice),
                            new HashMap<String, Object>(),
                            true,
                            true);
                }
            }
        }
    }

    private static void setCdPath(Map<String, Object> specParams, String srcCdPath, String isoPath) {
        // check if CD was set specifically for this VM
        if (!StringUtils.isEmpty(isoPath)){
            specParams.put(VdsProperties.Path, isoPath);
        } else if (!StringUtils.isEmpty(srcCdPath)) { // get the path from the source device spec params
            specParams.put(VdsProperties.Path, srcCdPath);
        }
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
                    isReadOnly);
        dao.save(managedDevice);
        // If we add Disk/Interface/CD/Floppy, we have to recalculate boot order
        if (type.equals(VmDeviceType.DISK) || type.equals(VmDeviceType.INTERFACE )) {
            // recalculate boot sequence
            VmBase vmBase = DbFacade.getInstance().getVmStaticDAO().get(id.getVmId());
            updateBootOrderInVmDevice(vmBase);
        }
        return managedDevice;
    }

    /**
     * adds imported VM or Template devices
     * @param entity
     */
    public static <T extends VmBase> void addImportedDevices(T entity) {
        List<VmDevice> vmDeviceToAdd = new ArrayList<VmDevice>();
        List<VmDevice> vmDeviceToUpdate = new ArrayList<VmDevice>();
        VmDeviceDAO dao = DbFacade.getInstance().getVmDeviceDAO();
        addImportedDisks(entity, vmDeviceToUpdate);
        addImportedInterfaces(entity, vmDeviceToUpdate);
        addOtherDevices(entity, vmDeviceToAdd);
        dao.saveAll(vmDeviceToAdd);
        dao.updateAll(vmDeviceToUpdate);
    }

    public static void setVmDevices(VmBase vmBase) {
        Map<Guid, VmDevice> vmManagedDeviceMap = new HashMap<Guid, VmDevice>();
        List<VmDevice> devices = DbFacade.getInstance().getVmDeviceDAO().getVmDeviceByVmId(vmBase.getId());
        vmBase.setUnmanagedDeviceList(DbFacade.getInstance().getVmDeviceDAO().getUnmanagedDevicesByVmId(vmBase.getId()));
        for (VmDevice device : devices) {
            if (device.getIsManaged()) {
                vmManagedDeviceMap.put(device.getDeviceId(), device);
            }
        }
        vmBase.setManagedDeviceMap(vmManagedDeviceMap);
    }

    /**
     * Updates VM boot order in vm device according to the BootSequence enum value.
     * @param vmBase
     */
    public static void updateBootOrderInVmDevice(VmBase vmBase) {
        if (vmBase instanceof VmStatic) {
            List<VmDevice> devices = dao.getVmDeviceByVmId(vmBase.getId());
            // reset current boot order
            for (VmDevice device: devices) {
                device.setBootOrder(0);
            }
            VM vm = DbFacade.getInstance().getVmDAO().get(vmBase.getId());
            VmHandler.updateDisksForVm(vm, DbFacade.getInstance().getDiskDao().getAllForVm(vm.getId()));
            boolean isOldCluster = VmDeviceCommonUtils.isOldClusterVersion(vm.getvds_group_compatibility_version());
            VmDeviceCommonUtils.updateVmDevicesBootOrder(vm, devices, isOldCluster);
            // update boot order in vm device
            for (VmDevice device : devices) {
                dao.update(device);
            }
        }
    }

    /**
     * updates existing VM CD ROM in vm_device
     *
     * @param oldVmBase
     * @param newVmBase
     *            NOTE : Only one CD is currently supported.
     */
    private static void updateCdInVmDevice(VmBase oldVmBase, VmBase newVmBase) {
        List<VmDevice> cdList = DbFacade.getInstance().getVmDeviceDAO().getVmDeviceByVmIdTypeAndDevice(oldVmBase.getId(), VmDeviceType.DISK.getName(), VmDeviceType.CDROM.getName());
        if (cdList.size() > 0){ // this is done only for safety, each VM must have at least an Empty CD
            VmDevice cd = cdList.get(0); // only one managed CD is currently supported.
            cd.getSpecParams().put(VdsProperties.Path, newVmBase.getiso_path());
            dao.update(cd);
        }
    }

    /**
     * updates new VM CD ROM in vm_device
     * @param newVmBase
     */

    private static void updateCdInVmDevice(VmBase newVmBase) {
        if (StringUtils.isNotEmpty(newVmBase.getiso_path())) {
            // new CD was added
            VmDevice cd = new VmDevice(new VmDeviceId(Guid.NewGuid(),
                    newVmBase.getId()), VmDeviceType.DISK.getName(),
                    VmDeviceType.CDROM.getName(), "", 0,
                            Collections.<String, Object> singletonMap(VdsProperties.Path, newVmBase.getiso_path()),
                            true,
                            null,
                            false);
            dao.save(cd);
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
            prevNumOfMonitors = oldVmBase.getnum_of_monitors();
        }
        if (newStatic.getnum_of_monitors() > prevNumOfMonitors) {
            // monitors were added
            for (int i = prevNumOfMonitors; i < newStatic
                    .getnum_of_monitors(); i++) {
                Guid newId = Guid.NewGuid();
                VmDeviceUtils.addManagedDevice(new VmDeviceId(newId, newStatic.getId()),
                        VmDeviceType.VIDEO,
                        VmDeviceType.QXL,
                        getMemExpr(newStatic.getnum_of_monitors()),
                        true,
                        false);
            }
        } else { // delete video cards
            List<VmDevice> list = DbFacade
                    .getInstance()
                    .getVmDeviceDAO()
                    .getVmDeviceByVmIdAndType(newStatic.getId(),
                            VmDeviceType.VIDEO.getName());
            for (int i = 0; i < (prevNumOfMonitors - newStatic
                    .getnum_of_monitors()); i++) {
                dao.remove(list.get(i).getId());
            }
        }
    }

    /**
     * Updates new/existing VM USB slots in vm device
     * Currently assuming the number of slots is between 0 and SLOTS_PER_CONTROLLER, i.e., no more than one controller
     * @param oldVmBase
     * @param newStatic
     */
    private static void updateUSBSlots(VmBase oldVm, VmBase newVm) {
        UsbPolicy oldUsbPolicy = UsbPolicy.DISABLED;
        UsbPolicy newUsbPolicy = newVm.getusb_policy();

        if (oldVm != null) {
            oldUsbPolicy = oldVm.getusb_policy();
        }

        final int usbSlots = Config.<Integer> GetValue(ConfigValues.NumberOfUSBSlots);

        // We add USB slots in case support doesn't exist in the oldVm configuration, but exists in the new one
        if (!oldUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE) && newUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE)) {
            if (usbSlots > 0) {
                addUsbControllers(newVm, getNeededNumberOfUsbControllers(usbSlots));
                addUsbSlots(newVm, usbSlots);
            }
        }
        // Remove USB slots and controllers
        else if (oldUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE) && !newUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE)) {
            removeUsbControllers(newVm);
            removeUsbSlots(newVm);
        } else if (newUsbPolicy.equals(UsbPolicy.ENABLED_NATIVE)) {
            final int currentNumberOfSlots = getUsbRedirectDevices(oldVm).size();

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
     * Returns a VmBase object for the given entity and passed id.
     * @param entity
     *            the entity, may be VmStatic or VmTemplate
     * @param newId
     *            entity Guid
     * @return
     */
    private static <T extends VmBase> VmBase getBaseObject(T entity, Guid newId) {
        VmBase newVmBase = null;
        if (entity instanceof VmStatic) {
            newVmBase = DbFacade.getInstance().getVmDAO().get(newId).getStaticData();
        } else if (entity instanceof VmTemplate) {
            newVmBase = DbFacade.getInstance().getVmTemplateDAO().get(newId);
        }
        return newVmBase;
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
                            false);
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

    private static <T extends VmBase> void updateVmDevice(T entity, VmDevice vmDevice, Guid deviceId, List<VmDevice> vmDeviceToUpdate) {
        VmDevice exportedDevice = entity.getManagedVmDeviceMap().get(deviceId);
        if (exportedDevice != null) {
            vmDevice.setAddress(exportedDevice.getAddress());
            vmDevice.setBootOrder(exportedDevice.getBootOrder());
            vmDevice.setIsPlugged(exportedDevice.getIsPlugged());
            vmDevice.setIsReadOnly(exportedDevice.getIsReadOnly());
            vmDeviceToUpdate.add(vmDevice);
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
                            false);
            updateVmDevice(entity, vmDevice, deviceId, vmDeviceToUpdate);
        }
    }

    /**
     * Adds Special managed devices (monitor/CDROM ) and unmanaged devices
     *
     * @param <T>
     * @param entity
     */
    private static <T extends VmBase> void addOtherDevices(T entity, List<VmDevice> vmDeviceToAdd) {
        boolean hasCD = false;
        for (VmDevice vmDevice : entity.getManagedVmDeviceMap().values()) {
            if ((vmDevice.getDevice().equals(VmDeviceType.DISK.getName()) && vmDevice.getType().equals(VmDeviceType.DISK.getName())) ||
                    (vmDevice.getDevice().equals(VmDeviceType.BRIDGE.getName())
                    && vmDevice.getType().equals(VmDeviceType.INTERFACE.getName()))) {
                continue; // skip disks/interfaces that were added separately.
            }
            vmDevice.setIsManaged(true);
            if (vmDevice.getType().equals(VmDeviceType.VIDEO.getName())) {
                vmDevice.setSpecParams(getMemExpr(entity.getnum_of_monitors()));
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
        for (int index = 0; index < numberOfSlotsToRemove; index++) {
            dao.remove(list.get(index).getId());
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

}

