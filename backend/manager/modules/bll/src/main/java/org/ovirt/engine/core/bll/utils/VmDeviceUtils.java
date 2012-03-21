package org.ovirt.engine.core.bll.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskType;
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

public class VmDeviceUtils {
    private final static String LOW_VIDEO_MEM = "32768";
    private final static String HIGH_VIDEO_MEM = "65536";
    private static VmBase vmBaseInstance;
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
        for (VmDevice device : devices) {
            id = Guid.NewGuid();
            String specParams = "";
            if (srcId.equals(Guid.Empty)) {
                // only update number of monitors if this is a desktop
                if (vmBase.getvm_type() == VmType.Desktop) {
                    updateNumOfMonitorsInVmDevice(null, vmBase);
                }
                continue; // skip Blank template devices
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
            device.setId(new VmDeviceId(id, dstId));
            device.setSpecParams(appendDeviceIdToSpecParams(id, specParams));
            dao.save(device);
        }
        // if destination is a VM , update devices boot order
        if (isVm) {
            updateBootOrderInVmDevice(vmBase);
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
        return managedDevice;
    }

    /**
     * adds imported VM or Template devices
     * @param entity
     * @param id
     */
    public static <T extends VmBase> void addImportedDevices(T entity, Guid id, List<VmDevice> vmDeviceToAdd, List<VmDevice> vmDeviceToUpdate) {
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
    private static void updateCdInVmDevice(VmBase oldVmBase,
            VmBase newVmBase) {
        String newIsoPath = newVmBase.getiso_path();
        String oldIsoPath = oldVmBase.getiso_path();

        if (StringUtils.isEmpty(oldIsoPath) && StringUtils.isNotEmpty(newIsoPath)) {
            // new CD was added
            VmDevice cd = new VmDevice(new VmDeviceId(Guid.NewGuid(),
                    newVmBase.getId()),
                    VmDeviceType.DISK.getName(),
                    VmDeviceType.CDROM.getName(), "", 0,
                    newIsoPath, true, null, false);
            dao.save(cd);
        } else {
            if (StringUtils.isNotEmpty(oldIsoPath) && StringUtils.isEmpty(newIsoPath)) {
                // existing CD was removed
                List<VmDevice> list = DbFacade
                        .getInstance()
                        .getVmDeviceDAO()
                        .getVmDeviceByVmIdTypeAndDevice(newVmBase.getId(),
                                VmDeviceType.DISK.getName(),
                                VmDeviceType.CDROM.getName());
                dao.remove(list.get(0).getId());
            } else if (StringUtils.isNotEmpty(oldIsoPath) && StringUtils.isNotEmpty(newIsoPath)
                    && !oldIsoPath.equals(newIsoPath)) {
                // CD was changed
                List<VmDevice> list = DbFacade
                        .getInstance()
                        .getVmDeviceDAO()
                        .getVmDeviceByVmIdTypeAndDevice(newVmBase.getId(),
                                VmDeviceType.DISK.getName(),
                                VmDeviceType.CDROM.getName());
                VmDevice cd = list.get(0);
                cd.setSpecParams(newIsoPath);
                dao.update(cd);
            }
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
            vmBaseInstance = vmBase;
            List<VmDevice> devices = dao.getVmDeviceByVmId(vmBase.getId());
            int bootOrder = 1;
            switch (vmBase.getdefault_boot_sequence()) {
            case C:
                bootOrder = setDiskBootOrder(devices, bootOrder);
                break;
            case CD:
                bootOrder = setDiskBootOrder(devices, bootOrder);
                bootOrder = setCDBootOrder(devices, bootOrder);
                break;
            case CDN:
                bootOrder = setDiskBootOrder(devices, bootOrder);
                bootOrder = setCDBootOrder(devices, bootOrder);
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                break;
            case CN:
                bootOrder = setDiskBootOrder(devices, bootOrder);
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                break;
            case CND:
                bootOrder = setDiskBootOrder(devices, bootOrder);
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                bootOrder = setCDBootOrder(devices, bootOrder);
                break;
            case D:
                bootOrder = setCDBootOrder(devices, bootOrder);
                break;
            case DC:
                bootOrder = setCDBootOrder(devices, bootOrder);
                bootOrder = setDiskBootOrder(devices, bootOrder);
                break;
            case DCN:
                bootOrder = setCDBootOrder(devices, bootOrder);
                bootOrder = setDiskBootOrder(devices, bootOrder);
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                break;
            case DN:
                bootOrder = setCDBootOrder(devices, bootOrder);
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                break;
            case DNC:
                bootOrder = setCDBootOrder(devices, bootOrder);
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                bootOrder = setDiskBootOrder(devices, bootOrder);
                break;
            case N:
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                break;
            case NC:
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                bootOrder = setDiskBootOrder(devices, bootOrder);
                break;
            case NCD:
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                bootOrder = setDiskBootOrder(devices, bootOrder);
                bootOrder = setCDBootOrder(devices, bootOrder);
                break;
            case ND:
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                bootOrder = setCDBootOrder(devices, bootOrder);
                break;
            case NDC:
                bootOrder = setNetworkBootOrder(devices, bootOrder);
                bootOrder = setCDBootOrder(devices, bootOrder);
                bootOrder = setDiskBootOrder(devices, bootOrder);
                break;
            }
            // update boot order in vm device
            for (VmDevice device : devices) {
                dao.update(device);
            }
        }
    }

    /**
     * updates network devices boot order
     * @param devices
     * @param bootOrder
     * @return
     */
    private static int setNetworkBootOrder(List<VmDevice> devices, int bootOrder) {
        for (VmDevice device : devices) {
            if (device.getType().equals(
                    VmDeviceType.INTERFACE.getName())
                    && device.getDevice().equals(
                            VmDeviceType.BRIDGE.getName())) {
                device.setBootOrder(bootOrder++);
            }
        }
        return bootOrder;
    }

    /**
     * updates CD boot order
     * @param devices
     * @param bootOrder
     * @return
     */
    private static int setCDBootOrder(List<VmDevice> devices, int bootOrder) {
        for (VmDevice device : devices) {
            if (device.getType()
                    .equals(VmDeviceType.DISK.getName())
                    && device.getDevice().equals(
                            VmDeviceType.CDROM.getName())) {
                device.setBootOrder(bootOrder++);
                break; // only one CD is currently supported.
            }
        }
        return bootOrder;
    }

    /**
     * updates disk boot order
     * @param devices
     * @param bootOrder
     * @return
     */
    private static int setDiskBootOrder(List<VmDevice> devices, int bootOrder) {
        VM vm = DbFacade.getInstance().getVmDAO().get(vmBaseInstance.getId());
        boolean isOldCluster = VmDeviceCommonUtils.isOldClusterVersion(vm.getvds_group_compatibility_version());
        for (VmDevice device : devices) {
            if (device.getType()
                    .equals(VmDeviceType.DISK.getName())
                    && device.getDevice().equals(
                            VmDeviceType.DISK.getName())) {
                Guid id = device.getDeviceId();
                Disk disk = DbFacade.getInstance().getDiskDao().get(id);
                if (id != null && !id.equals(Guid.Empty)) {
                    if (isOldCluster) { // Only one system disk can be bootable in
                                        // old version.
                        if (disk != null && disk.getDiskType().equals(DiskType.System)) {
                            device.setBootOrder(bootOrder++);
                            break;
                        }
                    } else { // supporting more than 1 bootable disk in 3.1 and up.
                        device.setBootOrder(bootOrder++);
                    }
                }
            }
        }
        return bootOrder;
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
        for (DiskImage disk : entity.getImages()) {
            Guid deviceId = disk.getDisk().getId();
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
            vmDeviceToAdd.add(vmDevice);
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
        String mem = (numOfMonitors > 2 ? LOW_VIDEO_MEM : HIGH_VIDEO_MEM);
        StringBuilder sb = new StringBuilder();
        sb.append(VRAM);
        sb.append("=");
        sb.append(mem);
        return sb.toString();
    }
}
