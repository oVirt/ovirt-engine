package org.ovirt.engine.core.bll.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
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
        }
    }

    /**
     * Copies relevamt entries on Vm from Template or Tempalte from VM creation.
     *
     * @param srcId
     * @param dstId
     * @param disks
     *            The disks which were saved for the destination VM.
     */
    public static void copyVmDevices(Guid srcId, Guid dstId, List<DiskImage> disks) {
        Guid id;
        VmBase vmBase = DbFacade.getInstance().getVmStaticDAO().get(dstId);
        List<VmNetworkInterface> ifaces;
        int diskCount = 0;
        int ifaceCount = 0;
        boolean isVm = (vmBase != null);
        if (isVm) {
            ifaces = DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForVm(dstId);
        } else {
            vmBase = DbFacade.getInstance().getVmTemplateDAO().get(dstId);
            ifaces = DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForTemplate(dstId);
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
            String specParams = "";
            if (srcId.equals(Guid.Empty)) {
                // update number of monitors if this is a desktop
                if (vmBase.getvm_type() == VmType.Desktop) {
                    updateNumOfMonitorsInVmDevice(null, vmBase);
                }
                //add CD if not exists
                if (addCD) {
                    specParams = setCdPath(specParams, "", isoPath);
                    addManagedDevice(new VmDeviceId(Guid.NewGuid(),dstId) , VmDeviceType.DISK, VmDeviceType.CDROM, specParams, true, true);
                }
                break; // skip other Blank template devices
            }
            if (VmDeviceType.DISK.getName().equals(device.getType())
                    && VmDeviceType.DISK.getName().equals(device.getDevice())) {
                if (diskCount < disks.size()) {
                    id = (disks.get(diskCount++)).getimage_group_id();
                }
            } else if (VmDeviceType.INTERFACE.getName().equals(device.getType())) {
                if (ifaceCount < ifaces.size()) {
                    id = ifaces.get(ifaceCount++).getId();
                }
            } else if (VmDeviceType.VIDEO.getName().equals(device.getType())) {
                specParams = getMemExpr(vmBase.getnum_of_monitors());
            }
            else  if (VmDeviceType.DISK.getName().equals(device.getType())
                    && VmDeviceType.CDROM.getName().equals(device.getDevice())) {
                // check here is source VM had CD (Vm from snapshot)
                String srcCdPath=org.ovirt.engine.core.utils.StringUtils.string2Map(device.getSpecParams()).get(VdsProperties.Path);
                shouldHaveCD = (!srcCdPath.isEmpty() || shouldHaveCD);
                if (!hasAlreadyCD && shouldHaveCD) {
                    specParams = setCdPath(specParams, srcCdPath, isoPath);
                }
                else {// CD already exists
                    continue;
                }
            }
            device.setId(new VmDeviceId(id, dstId));
            device.setSpecParams(appendDeviceIdToSpecParams(id, specParams));
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
                    addManagedDevice(new VmDeviceId(Guid.NewGuid(),vmBase.getId()), VmDeviceType.SOUND,VmDeviceType.getSoundDeviceType(soundDevice), "", true, true);
                }
            }
        }
    }

    private static String setCdPath(String specParams, String srcCdPath, String isoPath) {
        StringBuilder sb = new StringBuilder();
        sb.append(VdsProperties.Path);
        sb.append("=");
        // check if CD was set specifically for this VM
        if (!StringUtils.isEmpty(isoPath)){
            sb.append(isoPath);
            specParams = sb.toString();
        }
 else if (!StringUtils.isEmpty(srcCdPath)) { // get the path from the source device spec params
            sb.append(srcCdPath);
            specParams = sb.toString();
        }
        return specParams;
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
            String specParams,
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

    /**
     * set device Id in special parameters
     *
     * @param deviceId
     * @param specParams
     * @return
     */
    public static String appendDeviceIdToSpecParams(Guid deviceId, String specParams) {
        final String SEP = ",";
        StringBuilder sb = new StringBuilder();
        if (specParams.length() > 0) {
            sb.append(specParams);
            sb.append(SEP);
        }
        sb.append("deviceId=");
        sb.append(deviceId);
        return sb.toString();
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
            Map<String, String> specParamsMap = org.ovirt.engine.core.utils.StringUtils.string2Map(cd.getSpecParams());
            String path = newVmBase.getiso_path();
            specParamsMap.put(VdsProperties.Path, path);
            cd.setSpecParams(org.ovirt.engine.core.utils.StringUtils.map2String(specParamsMap));
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
                    newVmBase.getiso_path(), true, null, false);
            dao.save(cd);
        }
    }

    /**
     * Updates VM boot order in vm device according to the BootSequence enum value.
     * @param vmBase
     */
    private static void updateBootOrderInVmDevice(VmBase vmBase) {
        if (vmBase instanceof VmStatic) {
            List<VmDevice> devices = dao.getVmDeviceByVmId(vmBase.getId());
            // reset current boot order
            for (VmDevice device: devices) {
                device.setBootOrder(0);
            }
            VM vm = DbFacade.getInstance().getVmDAO().get(vmBase.getId());
            boolean isOldCluster = VmDeviceCommonUtils.isOldClusterVersion(vm.getvds_group_compatibility_version());
            VmDeviceCommonUtils.updateVmDevicesBootOrder(vmBase, devices, vmBase.getdefault_boot_sequence(), isOldCluster);
            // update boot order in vm device
            for (VmDevice device : devices) {
                dao.update(device);
            }
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
                StringBuilder sb =
                        new StringBuilder(appendDeviceIdToSpecParams(newId, getMemExpr(newStatic.getnum_of_monitors())));
                VmDeviceUtils.addManagedDevice(new VmDeviceId(newId, newStatic.getId()),
                        VmDeviceType.VIDEO,
                        VmDeviceType.QXL,
                        sb.toString(),
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
            String specParams = appendDeviceIdToSpecParams(deviceId, "");
            VmDevice vmDevice =
                    addManagedDevice(new VmDeviceId(deviceId, id),
                            VmDeviceType.DISK,
                            VmDeviceType.DISK,
                            specParams,
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
        Set<BaseDisk> disks = new HashSet<BaseDisk>();
        for (DiskImage diskImage : diskImages) {
            disks.add(diskImage.getDisk());
        }
        return disks;
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
            String specParams = appendDeviceIdToSpecParams(deviceId, "");
            VmDevice vmDevice =
                    addManagedDevice(new VmDeviceId(deviceId, id),
                            VmDeviceType.INTERFACE,
                            VmDeviceType.BRIDGE,
                            specParams,
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
        String memExpr = getMemExpr(entity.getnum_of_monitors());
        for (VmDevice vmDevice : entity.getManagedVmDeviceMap().values()) {
            if ((vmDevice.getDevice().equals(VmDeviceType.DISK.getName()) && vmDevice.getType().equals(VmDeviceType.DISK.getName())) ||
                    (vmDevice.getDevice().equals(VmDeviceType.BRIDGE.getName())
                    && vmDevice.getType().equals(VmDeviceType.INTERFACE.getName()))) {
                continue; // skip disks/interfaces that were added separately.
            }
            vmDevice.setIsManaged(true);
            if (vmDevice.getType().equals(VmDeviceType.VIDEO.getName())) {
                vmDevice.setSpecParams(memExpr);
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
    private static String getMemExpr(int numOfMonitors) {
        String mem = (numOfMonitors > 2 ? VmDeviceCommonUtils.LOW_VIDEO_MEM : VmDeviceCommonUtils.HIGH_VIDEO_MEM);
        StringBuilder sb = new StringBuilder();
        sb.append(VRAM);
        sb.append("=");
        sb.append(mem);
        return sb.toString();
    }

    /**
     * adds an empty CD in the case that we have no CDROM inside the device
     * @param dstId
     */
    private static void addEmptyCD(Guid dstId) {
        StringBuilder sb = new StringBuilder();
        sb.append(VdsProperties.Path);
        sb.append("=");
        VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.NewGuid(),dstId), VmDeviceType.DISK, VmDeviceType.CDROM, sb.toString(), true, true);
    }
}
