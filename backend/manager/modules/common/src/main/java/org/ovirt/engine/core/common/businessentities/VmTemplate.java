package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VmTemplate extends VmBase {
    private static final long serialVersionUID = -522552511046744989L;

    private List<VmNetworkInterface> _Interfaces = new ArrayList<VmNetworkInterface>();

    private int childCount;

    private VmTemplateStatus status = VmTemplateStatus.OK;

    private String vdsGroupName;

    private NGuid storagePoolId;

    private String storagePoolName;

    private Map<Guid, DiskImage> diskMap = new HashMap<Guid, DiskImage>();

    private ArrayList<DiskImage> diskList = new ArrayList<DiskImage>();

    private HashMap<Guid, DiskImage> diskTemplateMap = new HashMap<Guid, DiskImage>();

    private double bootDiskSizeGB;

    private double actualDiskSize = 0;

    public VmTemplate() {
        setAutoSuspend(false);
        setNiceLevel(0);
        diskTemplateMap = new HashMap<Guid, DiskImage>();
    }

    private boolean disabled;

    public VmTemplate(int child_count, Date creation_date, String description, int mem_size_mb, String name,
            int num_of_sockets, int cpu_per_socket, VmOsType os, Guid vds_group_id, Guid vmt_guid, String domain,
            int num_of_monitors, int status, int usb_policy, String time_zone, boolean is_auto_suspend, int nice_level,
            boolean fail_back, BootSequence default_boot_sequence, VmType vm_type,
            boolean smartcardEnabled, boolean deleteProtected, Boolean tunnelMigration, String vncKeyboardLayout,
            int minAllocatedMem) {
        super(vmt_guid,
                vds_group_id,
                os,
                creation_date,
                description,
                mem_size_mb,
                num_of_sockets,
                cpu_per_socket,
                num_of_monitors,
                domain,
                time_zone,
                vm_type,
                UsbPolicy.forValue(usb_policy),
                fail_back,
                default_boot_sequence,
                nice_level,
                is_auto_suspend,
                0,
                false,
                false,
                null,
                OriginType.valueOf(Config.<String> GetValue(ConfigValues.OriginType)),
                null,
                null,
                null,
                null,
                smartcardEnabled,
                deleteProtected,
                tunnelMigration,
                vncKeyboardLayout,
                minAllocatedMem);

        diskTemplateMap = new HashMap<Guid, DiskImage>();

        this.childCount = child_count;
        setName(name);
        this.setNumOfMonitors(num_of_monitors);
        this.setstatus(VmTemplateStatus.forValue(status));
    }

    public int getchild_count() {
        return this.childCount;
    }

    public void setchild_count(int value) {
        this.childCount = value;
    }

    public VmTemplateStatus getstatus() {
        return status;
    }

    public void setstatus(VmTemplateStatus value) {
        if (status != value) {
            status = value;
        }
    }

    public String getvds_group_name() {
        return vdsGroupName;
    }

    public void setvds_group_name(String value) {
        vdsGroupName = value;
    }

    @Override
    public List<VmNetworkInterface> getInterfaces() {
        return _Interfaces;
    }

    @Override
    public void setInterfaces(List<VmNetworkInterface> value) {
        _Interfaces = value;
    }

    public NGuid getstorage_pool_id() {
        return storagePoolId;
    }

    public void setstorage_pool_id(NGuid value) {
        storagePoolId = value;
    }

    public String getstorage_pool_name() {
        return storagePoolName;
    }

    public void setstorage_pool_name(String value) {
        storagePoolName = value;
    }

    public double getSizeGB() {
        return bootDiskSizeGB;
    }

    public void setSizeGB(double value) {
        bootDiskSizeGB = value;
    }

    @JsonIgnore
    public HashMap<Guid, DiskImage> getDiskMap() {
        return diskTemplateMap;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public double getActualDiskSize() {
        if (actualDiskSize == 0 && getDiskImageMap() != null) {
            for (Disk disk : getDiskImageMap().values()) {
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    actualDiskSize += ((DiskImage) disk).getActualSize();
                }
            }
        }
        return actualDiskSize;
    }

    /**
     * empty setters to fix CXF issue
     */
    public void setActualDiskSize(double actualDiskSize) {
        // Purposely empty
    }

    @JsonIgnore
    public Map<Guid, DiskImage> getDiskImageMap() {
        return diskMap;
    }

    public void setDiskImageMap(Map<Guid, DiskImage> value) {
        diskMap = value;
    }

    @Override
    @JsonIgnore
    public ArrayList<DiskImage> getDiskList() {
        return diskList;
    }

    public void setDiskList(ArrayList<DiskImage> disks) {
        diskList = disks;
    }

    @Override
    public boolean equals(Object obj) {
        boolean returnValue = super.equals(obj);
        if (!returnValue && obj != null && obj instanceof VmTemplate) {
            returnValue = getId()
                    .equals(((VmTemplate) obj).getId());
        }
        return returnValue;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE,
            message = "VALIDATION.VM_TEMPLATE.NAME.MAX",
            groups = { Default.class, ImportClonedEntity.class })
    @ValidI18NName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
            UpdateEntity.class, ImportClonedEntity.class })
    public String getName() {
        return super.getName();
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

}
