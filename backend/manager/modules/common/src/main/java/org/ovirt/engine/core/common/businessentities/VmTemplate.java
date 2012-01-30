package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmTemplate")
@Entity
@Table(name = "vm_templates")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class VmTemplate extends VmBase {
    private static final long serialVersionUID = -522552511046744989L;

    private static final ArrayList<String> _vmProperties = new ArrayList<String>(
            Arrays.asList(new String[] { "name", "domain", "child_count", "description",
                    "default_display_type", "mem_size_mb", "vds_group_name", "status", "time_zone", "num_of_monitors",
                    "vds_group_id", "usb_policy", "num_of_sockets", "cpu_per_socket", "os", "is_auto_suspend",
                    "auto_startup", "priority", "default_boot_sequence", "is_stateless", "iso_path", "initrd_url",
                    "kernel_url", "kernel_params" }));

    @Transient
    private List<VmNetworkInterface> _Interfaces = new ArrayList<VmNetworkInterface>();

    @Column(name = "child_count", nullable = false)
    private int childCount;

    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE,
            message = "VALIDATION.VM_TEMPLATE.NAME.MAX",
            groups = { CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
            UpdateEntity.class })
    @Column(name = "name", length = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE, nullable = false)
    private String name;

    @Column(name = "num_of_monitors", nullable = false)
    private int numOfMonitors;

    @Column(name = "status", nullable = false)
    @Enumerated
    private VmTemplateStatus status = VmTemplateStatus.OK;

    private String vdsGroupName;

    @XmlElement(name = "storage_pool_id")
    @Column(name = "storage_pool_id")
    @Type(type = "guid")
    private NGuid storagePoolId;

    @XmlElement(name = "storage_pool_name")
    @Transient
    private String storagePoolName;

    @XmlElement(name = "default_display_type")
    @Column(name = "default_display_type", nullable = false)
    @Enumerated
    private DisplayType defaultDisplayType = DisplayType.vnc;

    @Transient
    private Map<String, DiskImage> diskMap = new HashMap<String, DiskImage>();

    @Transient
    private ArrayList<DiskImage> diskList = new ArrayList<DiskImage>();

    @Transient
    private HashMap<String, DiskImage> diskTemplateMap = new HashMap<String, DiskImage>();

    @XmlElement(name = "SizeGB")
    @Transient
    private double bootDiskSizeGB;

    @Transient
    private double actualDiskSize = 0;

    public VmTemplate() {
        setis_auto_suspend(false);
        setnice_level(0);
        diskTemplateMap = new HashMap<String, DiskImage>();
    }

    public VmTemplate(int child_count, Date creation_date, String description, int mem_size_mb, String name,
            int num_of_sockets, int cpu_per_socket, VmOsType os, Guid vds_group_id, Guid vmt_guid, String domain,
            int num_of_monitors, int status, int usb_policy, String time_zone, boolean is_auto_suspend, int nice_level,
            boolean fail_back, BootSequence default_boot_sequence, VmType vm_type, HypervisorType hypervisor_type,
            OperationMode operation_mode) {
        super(vmt_guid,
                vds_group_id,
                os,
                creation_date,
                description,
                mem_size_mb,
                num_of_sockets,
                cpu_per_socket,
                domain,
                time_zone,
                vm_type,
                UsbPolicy.forValue(usb_policy),
                fail_back,
                default_boot_sequence,
                hypervisor_type,
                operation_mode,
                nice_level,
                is_auto_suspend,
                0,
                false,
                false,
                null,
                OriginType.valueOf(Config.<String>GetValue(ConfigValues.OriginType)),
                null,
                null,
                null);

        diskTemplateMap = new HashMap<String, DiskImage>();

        this.childCount = child_count;
        this.name = name;
        this.setnum_of_monitors(num_of_monitors);
        this.setstatus(VmTemplateStatus.forValue(status));
    }

    @XmlElement
    public int getchild_count() {
        return this.childCount;
    }

    public void setchild_count(int value) {
        this.childCount = value;
    }

    @XmlElement
    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        if (!StringHelper.EqOp(this.name, value)) {
            this.name = value;
        }
    }

    // no need fo DataMember, it's setter and calculated from 2 other fields
    @XmlElement
    public int getnum_of_cpus() {
        return this.getcpu_per_socket() * this.getnum_of_sockets();
    }

    /**
     * empty setters to fix CXF issue
     */
    public void setnum_of_cpus(int val) {
    }

    @XmlElement
    public int getnum_of_monitors() {
        return numOfMonitors;
    }

    public void setnum_of_monitors(int value) {
        numOfMonitors = value;
    }

    @XmlElement
    public VmTemplateStatus getstatus() {
        return status;
    }

    public void setstatus(VmTemplateStatus value) {
        if (status != value) {
            status = value;
        }
    }

    @XmlElement
    public String getvds_group_name() {
        return vdsGroupName;
    }

    public void setvds_group_name(String value) {
        vdsGroupName = value;
    }

    @XmlElement(name = "Interfaces")
    public List<VmNetworkInterface> getInterfaces() {
        return _Interfaces;
    }

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

    public DisplayType getdefault_display_type() {
        return defaultDisplayType;
    }

    public void setdefault_display_type(DisplayType value) {
        defaultDisplayType = value;
    }

    public double getSizeGB() {
        return bootDiskSizeGB;
    }

    public void setSizeGB(double value) {
        bootDiskSizeGB = value;
    }

    public HashMap<String, DiskImage> getDiskMap() {
        return diskTemplateMap;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return _vmProperties;
    }

    @XmlElement(name = "ActualDiskSize")
    public double getActualDiskSize() {
        if (actualDiskSize == 0 && getDiskImageMap() != null) {
            for (DiskImage disk : getDiskImageMap().values()) {
                actualDiskSize += disk.getActualSize();
            }
        }
        return actualDiskSize;
    }

    /**
     * empty setters to fix CXF issue
     */
    public void setActualDiskSize(double actualDiskSize) {
    }

    public Map<String, DiskImage> getDiskImageMap() {
        return diskMap;
    }

    public void setDiskImageMap(Map<String, DiskImage> value) {
        diskMap = value;
    }

    @XmlElement(name = "DiskImageMap")
    public ValueObjectMap getSerializedDiskImageMap() {
        return new ValueObjectMap(diskMap, false);
    }

    public void setSerializedDiskImageMap(ValueObjectMap serializedDiskImageMap) {
        diskMap = (serializedDiskImageMap == null) ? null : serializedDiskImageMap.asMap();
    }

    public ArrayList<DiskImage> getDiskList() {
        return diskList;
    }

    public void setDiskList(ArrayList<DiskImage> disks) {
        diskList = disks;
    }

    public boolean addDiskImage(DiskImage di) {
        boolean retval = false;
        if (!getDiskMap().containsKey(di.getinternal_drive_mapping())) {
            getDiskMap().put(di.getinternal_drive_mapping(), di);
            retval = true;
        }
        return retval;
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
}
