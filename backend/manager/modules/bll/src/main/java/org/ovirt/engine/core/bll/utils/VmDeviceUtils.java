package org.ovirt.engine.core.bll.utils;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class VmDeviceUtils {
    private final static String LOW_VIDEO_MEM = "32768";
    private final static String HIGH_VIDEO_MEM = "65536";
    private static VM vm;

    /**
     * Update the vm devices according to changes made in vm static for existing VM
     */
    public static void updateVmDevices(VmStatic oldVmStatic) {
        VmStatic newVmStatic = DbFacade.getInstance().getVmDAO()
                .get(oldVmStatic.getId()).getStaticData();
        if (!oldVmStatic.getiso_path().equals(newVmStatic.getiso_path())) {
            updateCdInVmDevice(oldVmStatic, newVmStatic);
        }
        if (oldVmStatic.getdefault_boot_sequence() != newVmStatic
                .getdefault_boot_sequence()) {
            updateBootOrderInVmDevice(newVmStatic);
        }
        if (oldVmStatic.getnum_of_monitors() != newVmStatic
                .getnum_of_monitors()) {
            updateNumOfMonitorsInVmDevice(oldVmStatic, newVmStatic);
        }
    }

    /**
     * Update the vm devices according to changes made in vm static for new VM
     */

    public static void updateVmDevices(Guid newVmId) {
        vm = DbFacade.getInstance().getVmDAO().get(newVmId);
        VmStatic newVmStatic = vm.getStaticData();
        updateCdInVmDevice(newVmStatic);
        updateBootOrderInVmDevice(newVmStatic);
        updateNumOfMonitorsInVmDevice(newVmStatic);
    }

    /**
     * updates existing VM CD ROM in vm_device
     * @param oldVmStatic
     * @param newVmStatic
     *            NOTE : Only one CD is currently supported.
     */
    private static void updateCdInVmDevice(VmStatic oldVmStatic,
            VmStatic newVmStatic) {
        if (oldVmStatic.getiso_path().isEmpty()
                && !newVmStatic.getiso_path().isEmpty()) {
            // new CD was added
            VmDevice cd = new VmDevice(new VmDeviceId(Guid.NewGuid(),
                    newVmStatic.getId()),
                    VmDeviceType.getName(VmDeviceType.DISK),
                    VmDeviceType.getName(VmDeviceType.CDROM), "", 0,
                    newVmStatic.getiso_path(), true, false, false, false);
            DbFacade.getInstance().getVmDeviceDAO().save(cd);
        } else if (!oldVmStatic.getiso_path().isEmpty()
                && newVmStatic.getiso_path().isEmpty()) {
            // existing CD was removed
            List<VmDevice> list = DbFacade
                    .getInstance()
                    .getVmDeviceDAO()
                    .getVmDeviceByVmIdTypeAndDevice(newVmStatic.getId(),
                            VmDeviceType.getName(VmDeviceType.DISK),
                            VmDeviceType.getName(VmDeviceType.CDROM));
            DbFacade.getInstance().getVmDeviceDAO().remove(list.get(0).getId());
        } else {
            // CD was changed
            List<VmDevice> list = DbFacade
                    .getInstance()
                    .getVmDeviceDAO()
                    .getVmDeviceByVmIdTypeAndDevice(newVmStatic.getId(),
                            VmDeviceType.getName(VmDeviceType.DISK),
                            VmDeviceType.getName(VmDeviceType.CDROM));
            VmDevice cd = list.get(0);
            cd.setSpecParams(newVmStatic.getiso_path());
            DbFacade.getInstance().getVmDeviceDAO().update(cd);
        }
    }

    /**
     * updates new VM CD ROM in vm_device
     * @param newVmStatic
     */

    private static void updateCdInVmDevice(VmStatic newVmStatic) {
        // new CD was added
        VmDevice cd = new VmDevice(new VmDeviceId(Guid.NewGuid(),
                newVmStatic.getId()), VmDeviceType.getName(VmDeviceType.DISK),
                VmDeviceType.getName(VmDeviceType.CDROM), "", 0,
                newVmStatic.getiso_path(), true, false, false, false);
        DbFacade.getInstance().getVmDeviceDAO().save(cd);
    }

    /**
     * Updates VM boot order in vm device according to the BootSequence enum value.
     * @param newStatic
     */
    private static void updateBootOrderInVmDevice(VmStatic newStatic) {
        List<VmDevice> devices = DbFacade.getInstance().getVmDeviceDAO()
                .getVmDeviceByVmId(newStatic.getId());
        int bootOrder = 1;
        switch (newStatic.getdefault_boot_sequence()) {
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
            DbFacade.getInstance().getVmDeviceDAO().update(device);
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
                    VmDeviceType.getName(VmDeviceType.INTERFACE))
                    && device.getDevice().equals(
                            VmDeviceType.getName(VmDeviceType.BRIDGE))) {
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
                    .equals(VmDeviceType.getName(VmDeviceType.DISK))
                    && device.getDevice().equals(
                            VmDeviceType.getName(VmDeviceType.CDROM))) {
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
        boolean isOldCluster = VmDeviceCommonUtils.isOldClusterVersion(vm);
        for (VmDevice device : devices) {
            if (device.getType()
                    .equals(VmDeviceType.getName(VmDeviceType.DISK))
                    && device.getDevice().equals(
                            VmDeviceType.getName(VmDeviceType.DISK))) {
                if (isOldCluster) { // Only one system disk can be bootable in
                                    // old version.
                    if (DbFacade.getInstance().getDiskDao()
                            .get(device.getDeviceId()).getDiskType()
                            .equals(DiskType.System)) {
                        device.setBootOrder(bootOrder++);
                        break;
                    }
                } else { // supporting more than 1 bootable disk in 3.1 and up.
                    device.setBootOrder(bootOrder++);
                }
            }
        }
        return bootOrder;
    }

    /**
     * updates existing VM video cards in vm device
     * @param oldVmStatic
     * @param newStatic
     */
    private static void updateNumOfMonitorsInVmDevice(VmStatic oldVmStatic,
            VmStatic newStatic) {
        if (newStatic.getnum_of_monitors() > oldVmStatic.getnum_of_monitors()) {
            String mem = (newStatic.getnum_of_monitors() > 2 ? LOW_VIDEO_MEM
                    : HIGH_VIDEO_MEM);
            // monitors were added
            for (int i = oldVmStatic.getnum_of_monitors(); i <= newStatic
                    .getnum_of_monitors(); i++) {
                VmDevice cd = new VmDevice(new VmDeviceId(Guid.NewGuid(),
                        newStatic.getId()),
                        VmDeviceType.getName(VmDeviceType.VIDEO),
                        DisplayType.qxl.name(), "", 0, mem, true, false, false,
                        false);
                DbFacade.getInstance().getVmDeviceDAO().save(cd);
            }
        } else { // delete video cards
            List<VmDevice> list = DbFacade
                    .getInstance()
                    .getVmDeviceDAO()
                    .getVmDeviceByVmIdAndType(newStatic.getId(),
                            VmDeviceType.getName(VmDeviceType.VIDEO));
            for (int i = 1; i <= (oldVmStatic.getnum_of_monitors() - newStatic
                    .getnum_of_monitors()); i++) {
                DbFacade.getInstance().getVmDeviceDAO()
                        .remove(list.get(i).getId());
            }
        }
    }

    /**
     * updates new VM video cards in vm device
     * @param newStatic
     */
    private static void updateNumOfMonitorsInVmDevice(VmStatic newStatic) {
        if (newStatic.getnum_of_monitors() > 0) {
            String mem = (newStatic.getnum_of_monitors() > 2 ? LOW_VIDEO_MEM
                    : HIGH_VIDEO_MEM);
            // monitors were added
            for (int i = 1; i <= newStatic.getnum_of_monitors(); i++) {
                VmDevice cd = new VmDevice(new VmDeviceId(Guid.NewGuid(),
                        newStatic.getId()),
                        VmDeviceType.getName(VmDeviceType.VIDEO),
                        DisplayType.qxl.name(), "", 0, mem, true, false, false,
                        false);
                DbFacade.getInstance().getVmDeviceDAO().save(cd);
            }
        }
    }
}
