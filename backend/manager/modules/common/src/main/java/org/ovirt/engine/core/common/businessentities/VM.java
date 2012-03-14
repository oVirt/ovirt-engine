package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VM")
public class VM extends IVdcQueryable implements INotifyPropertyChanged, Serializable, BusinessEntity<Guid>, HasStoragePool<Guid> {
    private static final long serialVersionUID = -4078140531074414263L;
    @Valid
    private VmStatic mVmStatic;
    private VmDynamic mVmDynamic;
    private VmStatistics mVmStatistics;

    public String getUserDefinedProperties() {
        return mVmStatic.getUserDefinedProperties();
    }

    public void setUserDefinedProperties(String userDefinedProperties) {
        mVmStatic.setUserDefinedProperties(userDefinedProperties);
    }

    public String getPredefinedProperties() {
        return mVmStatic.getPredefinedProperties();
    }

    @XmlElement(name = "CustomProperties")
    public String getCustomProperties() {
        return mVmStatic.getCustomProperties();
    }

    public void setCustomProperties(String customProperties) {
        mVmStatic.setCustomProperties(customProperties);
    }

    public void setPredefinedProperties(String predefinedProperties) {
        mVmStatic.setPredefinedProperties(predefinedProperties);
    }

    public VM() {
        mVmStatic = new VmStatic();
        mVmDynamic = new VmDynamic();
        mVmStatistics = new VmStatistics();
        setImages(new ArrayList<DiskImage>());
        setInterfaces(new ArrayList<VmNetworkInterface>());
        mDiskMap = new java.util.HashMap<String, DiskImage>();
        mCdPath = "";
        mFloppyPath = "";
        mRunAndPause = false;
        _diskSize = 0;
    }

    public VM(VmStatic vmStatic, VmDynamic vmDynamic, VmStatistics vmStatistics) {
        mDiskMap = new java.util.HashMap<String, DiskImage>();
        mCdPath = "";
        mFloppyPath = "";
        mRunAndPause = false;
        _diskSize = 0;

        mVmStatic = (vmStatic == null) ? new VmStatic() : vmStatic;
        mVmDynamic = vmDynamic;
        mVmStatistics = vmStatistics;
        setImages(new ArrayList<DiskImage>());
        setInterfaces(new ArrayList<VmNetworkInterface>());
    }

    public VM(Guid vm_guid, String vm_name, int vm_mem_size_mb, Guid vmt_guid, VmOsType vm_os, String vm_description,
            Guid vds_group_id, String vds_group_name, String vds_group_description, String vmt_name,
            int vmt_mem_size_mb, VmOsType vmt_os, java.util.Date vmt_creation_date, int vmt_child_count,
            int vmt_num_of_cpus, int vmt_num_of_sockets, int vmt_cpu_per_socket, String vmt_description, int status,
            String vm_ip, String vm_host, Integer vm_pid, java.util.Date vm_last_up_time,
            java.util.Date vm_last_boot_time, String guest_cur_user_name, java.util.Date guest_last_login_time,
            NGuid guest_cur_user_id, java.util.Date guest_last_logout_time, String guest_os,
            Double cpu_user, Double cpu_sys, Integer vm_if_id_1, String vm_if_name_1,
            Integer vm_line_rate_1, java.math.BigDecimal rx_dropped_1, java.math.BigDecimal rx_rate_1,
            java.math.BigDecimal tx_dropped_1, java.math.BigDecimal tx_rate_1, Integer vm_if_id_2, String vm_if_name_2,
            Integer vm_line_rate_2, java.math.BigDecimal rx_dropped_2, java.math.BigDecimal rx_rate_2,
            java.math.BigDecimal tx_dropped_2, java.math.BigDecimal tx_rate_2, Double elapsed_time,
            Integer usage_network_percent, Integer usage_mem_percent, Integer usage_cpu_percent, Guid run_on_vds,
            Guid migrating_to_vds, String app_list, Integer display, String vm_domain, java.util.Date vm_creation_date,
            String run_on_vds_name, String time_zone, Boolean acpi_enable, Integer session, String display_ip,
            Integer display_type, Boolean kvm_enable, Integer boot_sequence, String vmt_time_zone,
            Integer display_secure_port, Integer utc_diff, boolean is_stateless, String vds_cpu_name,
            boolean fail_back, BootSequence default_boot_sequence, VmType vm_type, HypervisorType hypervisor_type,
            OperationMode operation_mode, int minAllocatedMem) {
        mVmStatic = new VmStatic();
        mVmDynamic = new VmDynamic();
        mVmStatistics = new VmStatistics();
        setImages(new ArrayList<DiskImage>());
        setInterfaces(new ArrayList<VmNetworkInterface>());
        mDiskMap = new java.util.HashMap<String, DiskImage>();
        mCdPath = "";
        mFloppyPath = "";
        mRunAndPause = false;
        _diskSize = 0;

        this.setId(vm_guid);
        this.setvm_name(vm_name);
        this.setvm_mem_size_mb(vm_mem_size_mb);
        this.setvmt_guid(vmt_guid);
        this.setvm_domain(vm_domain);
        this.setvm_os(vm_os);
        this.setvm_creation_date(vm_creation_date);
        this.setvm_description(vm_description);
        this.setvds_group_id(vds_group_id);
        this.vds_group_nameField = vds_group_name;
        this.vds_group_descriptionField = vds_group_description;
        this.vmt_nameField = vmt_name;
        this.vmt_mem_size_mbField = vmt_mem_size_mb;
        this.vmt_osField = vmt_os;
        this.vmt_creation_dateField = vmt_creation_date;
        this.vmt_child_countField = vmt_child_count;
        this.vmt_num_of_cpusField = vmt_num_of_cpus;
        this.vmt_descriptionField = vmt_description;
        this.vmt_time_zoneField = vmt_time_zone;
        this.setstatus(VMStatus.forValue(status));
        this.setvm_ip(vm_ip);
        this.setvm_host(vm_host);
        this.setvm_pid(vm_pid);
        this.setvm_last_up_time(vm_last_up_time);
        this.setvm_last_boot_time(vm_last_boot_time);
        this.setguest_cur_user_name(guest_cur_user_name);
        this.setguest_last_login_time(guest_last_login_time);
        this.setguest_cur_user_id(guest_cur_user_id);
        this.setguest_last_logout_time(guest_last_logout_time);
        this.setguest_os(guest_os);
        this.setcpu_user(cpu_user);
        this.setcpu_sys(cpu_sys);
        this.setelapsed_time(elapsed_time);
        this.setusage_network_percent(usage_network_percent);
        this.setusage_mem_percent(usage_mem_percent);
        this.setusage_cpu_percent(usage_cpu_percent);
        this.setrun_on_vds(run_on_vds);
        this.setmigrating_to_vds(migrating_to_vds);
        this.setapp_list(app_list);
        this.setdisplay(display);
        this.run_on_vds_nameField = run_on_vds_name;
        this.settime_zone(time_zone);
        this.setacpi_enable(acpi_enable);
        this.setsession(SessionState.forValue(session));
        this.setdisplay_ip(display_ip);
        this.setdisplay_type(DisplayType.forValue(display_type));
        this.setkvm_enable(kvm_enable);
        this.setboot_sequence(BootSequence.forValue(boot_sequence));
        this.setvmt_time_zone(vmt_time_zone);
        this.setdisplay_secure_port(display_secure_port);
        this.setutc_diff(utc_diff);
        this.setis_stateless(is_stateless);
        this.setvds_group_cpu_name(vds_cpu_name);
        this.setfail_back(fail_back);
        this.setdefault_boot_sequence(default_boot_sequence);
        this.setvm_type(vm_type);
        this.sethypervisor_type(hypervisor_type);
        this.setoperation_mode(operation_mode);
        this.setMinAllocatedMem(minAllocatedMem);
    }

    @XmlElement(name = "VmPauseStatus")
    public VmPauseStatus getVmPauseStatus() {
        return this.mVmDynamic.getPauseStatus();
    }

    public void setVmPauseStatus(VmPauseStatus aPauseStatus) {
        this.mVmDynamic.setPauseStatus(aPauseStatus);
    }

    @XmlElement(name = "vm_guid")
    @Override
    public Guid getId() {
        return this.mVmStatic.getId();
    }

    @Override
    public void setId(Guid value) {
        this.mVmStatic.setId(value);
        this.mVmDynamic.setId(value);
        this.mVmStatistics.setId(value);
    }

    @XmlElement(name = "vm_name")
    public String getvm_name() {
        return this.mVmStatic.getvm_name();
    }

    public void setvm_name(String value) {
        if (!StringHelper.EqOp(this.mVmStatic.getvm_name(), value)) {
            this.mVmStatic.setvm_name(value);
            OnPropertyChanged(new PropertyChangedEventArgs("vm_name"));
        }
    }

    public int getmem_size_mb() {
        return this.getvm_mem_size_mb();
    }

    @XmlElement(name = "vm_mem_size_mb")
    public int getvm_mem_size_mb() {
        return this.mVmStatic.getmem_size_mb();
    }

    public void setvm_mem_size_mb(int value) {
        this.mVmStatic.setmem_size_mb(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vm_mem_size_mb"));
    }

    @XmlElement(name = "vm_domain")
    public String getvm_domain() {
        return this.mVmStatic.getdomain();
    }

    public void setvm_domain(String value) {
        this.mVmStatic.setdomain(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vm_domain"));
    }

    public VmOsType getos() {
        return this.getvm_os();
    }

    @XmlElement(name = "vm_os")
    public VmOsType getvm_os() {
        return this.mVmStatic.getos();
    }

    public void setvm_os(VmOsType value) {
        this.mVmStatic.setos(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vm_os"));
    }

    @XmlElement(name = "vm_creation_date")
    public java.util.Date getvm_creation_date() {
        return this.mVmStatic.getcreation_date();
    }

    public void setvm_creation_date(java.util.Date value) {
        this.mVmStatic.setcreation_date(value);
    }

    public Guid getQuotaId() {
        return this.mVmStatic.getQuotaId();
    }

    public void setQuotaId(Guid value) {
        this.mVmStatic.setQuotaId(value);
    }

    public String getQuotaName() {
        return this.mVmStatic.getQuotaName();
    }

    public void setQuotaName(String value) {
        this.mVmStatic.setQuotaName(value);
    }

    public String getdescription() {
        return this.getvm_description();
    }

    @XmlElement(name = "vm_description")
    public String getvm_description() {
        return this.mVmStatic.getdescription();
    }

    public void setvm_description(String value) {
        this.mVmStatic.setdescription(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vm_description"));
    }

    @XmlElement(name = "num_of_monitors")
    public int getnum_of_monitors() {
        return this.mVmStatic.getnum_of_monitors();
    }

    public void setnum_of_monitors(int value) {
        this.mVmStatic.setnum_of_monitors(value);
        OnPropertyChanged(new PropertyChangedEventArgs("num_of_monitors"));
    }

    @XmlElement(name = "is_initialized")
    public boolean getis_initialized() {
        return this.mVmStatic.getis_initialized();
    }

    public void setis_initialized(boolean value) {
        this.mVmStatic.setis_initialized(value);
        OnPropertyChanged(new PropertyChangedEventArgs("is_initialized"));
    }

    @XmlElement(name = "is_auto_suspend")
    public boolean getis_auto_suspend() {
        return this.mVmStatic.getis_auto_suspend();
    }

    public void setis_auto_suspend(boolean value) {
        this.mVmStatic.setis_auto_suspend(value);
        OnPropertyChanged(new PropertyChangedEventArgs("is_auto_suspend"));
    }

    @XmlElement(name = "num_of_cpus")
    public int getnum_of_cpus() {
        return this.mVmStatic.getnum_of_cpus();
    }

    /**
     * This method is created for SOAP serialization of primitives that are readonly but sent by the client. The setter
     * implementation is empty and the field is not being changed.
     *
     * @param value
     */
    @Deprecated
    public void setnum_of_cpus(int value) {

    }

    @XmlElement(name = "num_of_sockets")
    public int getnum_of_sockets() {
        return this.mVmStatic.getnum_of_sockets();
    }

    public void setnum_of_sockets(int value) {
        this.mVmStatic.setnum_of_sockets(value);
        OnPropertyChanged(new PropertyChangedEventArgs("num_of_sockets"));
        OnPropertyChanged(new PropertyChangedEventArgs("num_of_cpus"));
    }

    @XmlElement(name = "cpu_per_socket")
    public int getcpu_per_socket() {
        return this.mVmStatic.getcpu_per_socket();
    }

    public void setcpu_per_socket(int value) {
        this.mVmStatic.setcpu_per_socket(value);
        OnPropertyChanged(new PropertyChangedEventArgs("cpu_per_socket"));
        OnPropertyChanged(new PropertyChangedEventArgs("num_of_cpus"));
    }

    @XmlElement(name = "usb_policy")
    public UsbPolicy getusb_policy() {
        return mVmStatic.getusb_policy();
    }

    public void setusb_policy(UsbPolicy value) {
        mVmStatic.setusb_policy(value);
        OnPropertyChanged(new PropertyChangedEventArgs("usb_policy"));
    }

    @XmlElement(name = "auto_startup")
    public boolean getauto_startup() {
        return mVmStatic.getauto_startup();
    }

    public void setauto_startup(boolean value) {
        mVmStatic.setauto_startup(value);
        OnPropertyChanged(new PropertyChangedEventArgs("auto_startup"));
    }

    @XmlElement(name = "dedicated_vm_for_vds")
    public NGuid getdedicated_vm_for_vds() {
        return mVmStatic.getdedicated_vm_for_vds();
    }

    public void setdedicated_vm_for_vds(NGuid value) {
        mVmStatic.setdedicated_vm_for_vds(value);
        OnPropertyChanged(new PropertyChangedEventArgs("dedicated_vm_for_vds"));
    }

    @XmlElement(name = "vds_group_id")
    public Guid getvds_group_id() {
        return this.mVmStatic.getvds_group_id();
    }

    public void setvds_group_id(Guid value) {
        this.mVmStatic.setvds_group_id(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vds_group_id"));
    }

    @XmlElement(name = "time_zone")
    public String gettime_zone() {
        return mVmStatic.gettime_zone();
    }

    public void settime_zone(String value) {
        mVmStatic.settime_zone(value);
    }

    @XmlElement(name = "is_stateless")
    public boolean getis_stateless() {
        return mVmStatic.getis_stateless();
    }

    public void setis_stateless(boolean value) {
        mVmStatic.setis_stateless(value);
        OnPropertyChanged(new PropertyChangedEventArgs("is_stateless"));
    }

    @XmlElement(name = "default_display_type")
    public DisplayType getdefault_display_type() {
        return mVmStatic.getdefault_display_type();
    }

    public void setdefault_display_type(DisplayType value) {
        mVmStatic.setdefault_display_type(value);
        OnPropertyChanged(new PropertyChangedEventArgs("default_display_type"));
    }

    @XmlElement(name = "priority")
    public int getpriority() {
        return mVmStatic.getpriority();
    }

    public void setpriority(int value) {
        mVmStatic.setpriority(value);
        OnPropertyChanged(new PropertyChangedEventArgs("priority"));

    }

    @XmlElement(name = "iso_path")
    public String getiso_path() {
        return mVmStatic.getiso_path();
    }

    public void setiso_path(String value) {
        if (!StringHelper.EqOp(mVmStatic.getiso_path(), value)) {
            mVmStatic.setiso_path(value);
            OnPropertyChanged(new PropertyChangedEventArgs("iso_path"));
        }
    }

    @XmlElement(name = "origin")
    public OriginType getorigin() {
        return mVmStatic.getorigin();
    }

    public void setorigin(OriginType value) {
        mVmStatic.setorigin(value);
    }

    @XmlElement(name = "initrd_url")
    public String getinitrd_url() {
        return mVmStatic.getinitrd_url();
    }

    public void setinitrd_url(String value) {
        mVmStatic.setinitrd_url(value);
    }

    @XmlElement(name = "kernel_url")
    public String getkernel_url() {
        return mVmStatic.getkernel_url();
    }

    public void setkernel_url(String value) {
        mVmStatic.setkernel_url(value);
    }

    @XmlElement(name = "kernel_params")
    public String getkernel_params() {
        return mVmStatic.getkernel_params();
    }

    public void setkernel_params(String value) {
        mVmStatic.setkernel_params(value);
    }

    private NGuid mVmPoolId;
    private String mVmPoolName;

    @XmlElement(name = "status")
    public VMStatus getstatus() {
        return this.mVmDynamic.getstatus();
    }

    public void setstatus(VMStatus value) {
        if (this.mVmDynamic.getstatus() != value) {
            this.mVmDynamic.setstatus(value);
            OnPropertyChanged(new PropertyChangedEventArgs("status"));
        }
    }

    @XmlElement(name = "vm_ip")
    public String getvm_ip() {
        return this.mVmDynamic.getvm_ip();
    }

    public void setvm_ip(String value) {
        this.mVmDynamic.setvm_ip(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vm_ip"));
    }

    @XmlElement(name = "vm_host")
    public String getvm_host() {
        String vmHost = this.mVmDynamic.getvm_host();
        if (!StringHelper.isNullOrEmpty(this.getvm_ip())) {
            this.mVmDynamic.setvm_host(getvm_ip());
        } else {
            String vmDomain = getvm_domain();

            // If VM's host name isn't available - set as VM's name
            // If no IP address is available - assure that 'vm_host' is FQN by concatenating
            // vmHost and vmDomain.
            if (StringHelper.isNullOrEmpty(vmHost)) {
                vmHost = StringHelper.isNullOrEmpty(vmDomain) ? getvm_name() : getvm_name() + "." + vmDomain;
                this.mVmDynamic.setvm_host(vmHost);
            } else if (!StringHelper.isNullOrEmpty(vmDomain) && !vmHost.endsWith(vmDomain)) {
                this.mVmDynamic.setvm_host(vmHost + "." + vmDomain);
            }
        }

        return this.mVmDynamic.getvm_host();
    }

    public void setvm_host(String value) {
        this.mVmDynamic.setvm_host(value);
    }

    @XmlElement(name = "vm_pid")
    public Integer getvm_pid() {
        return this.mVmDynamic.getvm_pid();
    }

    public void setvm_pid(Integer value) {
        this.mVmDynamic.setvm_pid(value);
    }

    @XmlElement(name = "vm_last_up_time")
    public java.util.Date getvm_last_up_time() {
        return this.mVmDynamic.getvm_last_up_time();
    }

    public void setvm_last_up_time(java.util.Date value) {
        this.mVmDynamic.setvm_last_up_time(value);
    }

    @XmlElement(name = "vm_last_boot_time")
    public java.util.Date getvm_last_boot_time() {
        return this.mVmDynamic.getvm_last_boot_time();
    }

    public void setvm_last_boot_time(java.util.Date value) {
        this.mVmDynamic.setvm_last_boot_time(value);
    }

    @XmlElement(name = "guest_cur_user_name")
    public String getguest_cur_user_name() {
        return this.mVmDynamic.getguest_cur_user_name();
    }

    public void setguest_cur_user_name(String value) {
        this.mVmDynamic.setguest_cur_user_name(value);
        OnPropertyChanged(new PropertyChangedEventArgs("guest_cur_user_name"));
    }

    @XmlElement(name = "guest_last_login_time")
    public java.util.Date getguest_last_login_time() {
        return this.mVmDynamic.getguest_last_login_time();
    }

    public void setguest_last_login_time(java.util.Date value) {
        this.mVmDynamic.setguest_last_login_time(value);
    }

    @XmlElement(name = "guest_cur_user_id")
    public NGuid getguest_cur_user_id() {
        return this.mVmDynamic.getguest_cur_user_id();
    }

    public void setguest_cur_user_id(NGuid value) {
        this.mVmDynamic.setguest_cur_user_id(value);
    }

    @XmlElement(name = "guest_last_logout_time")
    public java.util.Date getguest_last_logout_time() {
        return this.mVmDynamic.getguest_last_logout_time();
    }

    public void setguest_last_logout_time(java.util.Date value) {
        this.mVmDynamic.setguest_last_logout_time(value);
    }

    @XmlElement(name = "guest_os")
    public String getguest_os() {
        return this.mVmDynamic.getguest_os();
    }

    public void setguest_os(String value) {
        this.mVmDynamic.setguest_os(value);
    }

    @XmlElement(name = "run_on_vds")
    public NGuid getrun_on_vds() {
        return this.mVmDynamic.getrun_on_vds();
    }

    public void setrun_on_vds(NGuid value) {
        this.mVmDynamic.setrun_on_vds(value);
        OnPropertyChanged(new PropertyChangedEventArgs("run_on_vds"));
    }

    @XmlElement(name = "migrating_to_vds")
    public NGuid getmigrating_to_vds() {
        return this.mVmDynamic.getmigrating_to_vds();
    }

    public void setmigrating_to_vds(NGuid value) {
        this.mVmDynamic.setmigrating_to_vds(value);
    }

    @XmlElement(name = "app_list")
    public String getapp_list() {
        return this.mVmDynamic.getapp_list();
    }

    public void setapp_list(String value) {
        this.mVmDynamic.setapp_list(value);
        OnPropertyChanged(new PropertyChangedEventArgs("app_list"));
    }

    @XmlElement(name = "display", nillable = true)
    public Integer getdisplay() {
        return this.mVmDynamic.getdisplay();
    }

    public void setdisplay(Integer value) {
        this.mVmDynamic.setdisplay(value);
    }

    @XmlElement(name = "acpi_enable")
    public Boolean getacpi_enable() {
        return this.mVmDynamic.getacpi_enable();
    }

    public void setacpi_enable(Boolean value) {
        this.mVmDynamic.setacpi_enable(value);
    }

    @XmlElement(name = "display_ip")
    public String getdisplay_ip() {
        return this.mVmDynamic.getdisplay_ip();
    }

    public void setdisplay_ip(String value) {
        this.mVmDynamic.setdisplay_ip(value);
    }

    @XmlElement(name = "display_type")
    public DisplayType getdisplay_type() {
        return this.mVmDynamic.getdisplay_type();
    }

    public void setdisplay_type(DisplayType value) {
        this.mVmDynamic.setdisplay_type(value);
        OnPropertyChanged(new PropertyChangedEventArgs("display_type"));
    }

    @XmlElement(name = "kvm_enable")
    public Boolean getkvm_enable() {
        return this.mVmDynamic.getkvm_enable();
    }

    public void setkvm_enable(Boolean value) {
        this.mVmDynamic.setkvm_enable(value);
    }

    @XmlElement(name = "session")
    public SessionState getsession() {
        return this.mVmDynamic.getsession();
    }

    public void setsession(SessionState value) {
        this.mVmDynamic.setsession(value);
    }

    @XmlElement(name = "boot_sequence")
    public BootSequence getboot_sequence() {
        return this.mVmDynamic.getboot_sequence();
    }

    public void setboot_sequence(BootSequence value) {
        this.mVmDynamic.setboot_sequence(value);
    }

    @XmlElement(name = "display_secure_port", nillable = true)
    public Integer getdisplay_secure_port() {
        return this.mVmDynamic.getdisplay_secure_port();
    }

    public void setdisplay_secure_port(Integer value) {
        this.mVmDynamic.setdisplay_secure_port(value);
    }

    @XmlElement(name = "ExitStatus")
    public VmExitStatus getExitStatus() {
        return this.mVmDynamic.getExitStatus();
    }

    public void setExitStatus(VmExitStatus value) {
        this.mVmDynamic.setExitStatus(value);
    }

    @XmlElement(name = "ExitMessage")
    public String getExitMessage() {
        return this.mVmDynamic.getExitMessage();
    }

    public void setExitMessage(String value) {
        this.mVmDynamic.setExitMessage(value);
    }

    @XmlElement(name = "utc_diff")
    public Integer getutc_diff() {
        return this.mVmDynamic.getutc_diff();
    }

    public void setutc_diff(Integer value) {
        this.mVmDynamic.setutc_diff(value);
    }

    @XmlElement(name = "last_vds_run_on")
    public NGuid getlast_vds_run_on() {
        return this.mVmDynamic.getlast_vds_run_on();
    }

    public void setlast_vds_run_on(NGuid value) {
        this.mVmDynamic.setlast_vds_run_on(value);
    }

    @XmlElement(name = "client_ip")
    public String getclient_ip() {
        return this.mVmDynamic.getclient_ip();
    }

    public void setclient_ip(String value) {
        this.mVmDynamic.setclient_ip(value);
    }

    @XmlElement(name = "guest_requested_memory")
    public Integer getguest_requested_memory() {
        return this.mVmDynamic.getguest_requested_memory();
    }

    public void setguest_requested_memory(Integer value) {
        this.mVmDynamic.setguest_requested_memory(value);
        OnPropertyChanged(new PropertyChangedEventArgs("guest_requested_memory"));
    }

    @XmlElement(name = "hash")
    public String getHash() {
        return mVmDynamic.getHash();
    }

    public void setHash(String hash) {
        mVmDynamic.setHash(hash);
    }

    @XmlElement(name = "cpu_user")
    public Double getcpu_user() {
        return this.mVmStatistics.getcpu_user();
    }

    public void setcpu_user(Double value) {
        this.mVmStatistics.setcpu_user(value);
    }

    @XmlElement(name = "cpu_sys")
    public Double getcpu_sys() {
        return this.mVmStatistics.getcpu_sys();
    }

    public void setcpu_sys(Double value) {
        this.mVmStatistics.setcpu_sys(value);
    }

    @XmlElement(name = "elapsed_time", nillable = true)
    public Double getelapsed_time() {
        return this.mVmStatistics.getelapsed_time();
    }

    public void setelapsed_time(Double value) {
        this.mVmStatistics.setelapsed_time(value);
    }

    @XmlElement(name = "RoundedElapsedTime", nillable = true)
    public Double getRoundedElapsedTime() {
        return this.mVmStatistics.getRoundedElapsedTime();
    }

    public void setRoundedElapsedTime(Double value) {
        this.mVmStatistics.setRoundedElapsedTime(value);
        OnPropertyChanged(new PropertyChangedEventArgs("roundedElapsedTime"));
    }

    @XmlElement(name = "usage_network_percent", nillable = true)
    public Integer getusage_network_percent() {
        return this.mVmStatistics.getusage_network_percent();
    }

    public void setusage_network_percent(Integer value) {
        this.mVmStatistics.setusage_network_percent(value);
        OnPropertyChanged(new PropertyChangedEventArgs("usage_network_percent"));
    }

    @XmlElement(name = "usage_mem_percent", nillable = true)
    public Integer getusage_mem_percent() {
        return this.mVmStatistics.getusage_mem_percent();
    }

    public void setusage_mem_percent(Integer value) {
        this.mVmStatistics.setusage_mem_percent(value);
        OnPropertyChanged(new PropertyChangedEventArgs("usage_mem_percent"));
    }

    @XmlElement(name = "usage_cpu_percent", nillable = true)
    public Integer getusage_cpu_percent() {
        return this.mVmStatistics.getusage_cpu_percent();
    }

    public void setusage_cpu_percent(Integer value) {
        this.mVmStatistics.setusage_cpu_percent(value);
        OnPropertyChanged(new PropertyChangedEventArgs("usage_cpu_percent"));
    }

    @XmlElement(name = "vmt_guid")
    public Guid getvmt_guid() {
        return this.mVmStatic.getvmt_guid();
    }

    public void setvmt_guid(Guid value) {
        this.mVmStatic.setvmt_guid(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vmt_guid"));
    }

    private String vmt_nameField;

    @XmlElement(name = "vmt_name")
    public String getvmt_name() {
        return this.vmt_nameField;
    }

    public void setvmt_name(String value) {
        this.vmt_nameField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("vmt_name"));
    }

    private int vmt_mem_size_mbField;


    public int getvmt_mem_size_mb() {
        return this.vmt_mem_size_mbField;
    }

    public void setvmt_mem_size_mb(int value) {
        this.vmt_mem_size_mbField = value;
    }

    private VmOsType vmt_osField = VmOsType.forValue(0);


    public VmOsType getvmt_os() {
        return this.vmt_osField;
    }

    public void setvmt_os(VmOsType value) {
        this.vmt_osField = value;
    }

    private java.util.Date vmt_creation_dateField = new java.util.Date(0);


    public java.util.Date getvmt_creation_date() {
        return this.vmt_creation_dateField;
    }

    public void setvmt_creation_date(java.util.Date value) {
        this.vmt_creation_dateField = value;
    }

    private int vmt_child_countField;


    public int getvmt_child_count() {
        return this.vmt_child_countField;
    }

    public void setvmt_child_count(int value) {
        this.vmt_child_countField = value;
    }

    private int vmt_num_of_cpusField;


    public int getvmt_num_of_cpus() {
        return this.vmt_num_of_cpusField;
    }

    public void setvmt_num_of_cpus(int value) {
        this.vmt_num_of_cpusField = value;
    }

    private int vmt_num_of_socketsField;


    public int getvmt_num_of_sockets() {
        return this.vmt_num_of_socketsField;
    }

    public void setvmt_num_of_sockets(int value) {
        this.vmt_num_of_socketsField = value;
    }

    private int vmt_cpu_per_socketField;


    public int getvmt_cpu_per_socket() {
        return this.vmt_cpu_per_socketField;
    }

    public void setvmt_cpu_per_socket(int value) {
        this.vmt_cpu_per_socketField = value;
    }

    private String vmt_descriptionField;


    public String getvmt_description() {
        return this.vmt_descriptionField;
    }

    public void setvmt_description(String value) {
        this.vmt_descriptionField = value;
    }

    private String vmt_time_zoneField;

    public String getvmt_time_zone() {
        return vmt_time_zoneField;
    }

    public void setvmt_time_zone(String value) {
        vmt_time_zoneField = value;
    }

    private Version vds_group_compatibility_versionField;

    @XmlElement(name = "vds_group_compatibility_version")
    public Version getvds_group_compatibility_version() {
        return this.vds_group_compatibility_versionField;
    }

    public void setvds_group_compatibility_version(Version value) {
        if (Version.OpInequality(getvds_group_compatibility_version(), value)) {
            this.vds_group_compatibility_versionField = value;
            OnPropertyChanged(new PropertyChangedEventArgs("vds_group_compatibility_version"));
        }
    }

    @XmlElement(name = "vds_group_name")
    private String vds_group_nameField;


    public String getvds_group_name() {
        return this.vds_group_nameField;
    }

    public void setvds_group_name(String value) {
        this.vds_group_nameField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("vds_group_name"));
    }

    private String vds_group_descriptionField;


    public String getvds_group_description() {
        return this.vds_group_descriptionField;
    }

    public void setvds_group_description(String value) {
        this.vds_group_descriptionField = value;
    }

    private String vds_group_cpu_nameField;


    public String getvds_group_cpu_name() {
        return this.vds_group_cpu_nameField;
    }

    public void setvds_group_cpu_name(String value) {
        this.vds_group_cpu_nameField = value;
    }

    @XmlElement(name = "fail_back")
    public boolean getfail_back() {
        return this.mVmStatic.getfail_back();
    }

    public void setfail_back(boolean value) {
        this.mVmStatic.setfail_back(value);
    }

    @XmlElement(name = "default_boot_sequence")
    public BootSequence getdefault_boot_sequence() {
        return this.mVmStatic.getdefault_boot_sequence();
    }

    public void setdefault_boot_sequence(BootSequence value) {
        this.mVmStatic.setdefault_boot_sequence(value);
    }

    @XmlElement(name = "nice_level")
    public int getnice_level() {
        return this.mVmStatic.getnice_level();
    }

    public void setnice_level(int value) {
        this.mVmStatic.setnice_level(value);
    }

    @XmlElement(name = "MigrationSupport")
    public MigrationSupport getMigrationSupport() {
        return this.mVmStatic.getMigrationSupport();
    }

    public void setMigrationSupport(MigrationSupport migrationSupport) {
        this.mVmStatic.setMigrationSupport(migrationSupport);
    }

    @XmlElement(name = "vm_type")
    public VmType getvm_type() {
        return this.mVmStatic.getvm_type();
    }

    public void setvm_type(VmType value) {
        this.mVmStatic.setvm_type(value);
    }

    public HypervisorType gethypervisor_type() {
        return this.mVmStatic.gethypervisor_type();
    }

    public void sethypervisor_type(HypervisorType value) {
        this.mVmStatic.sethypervisor_type(value);
    }

    public OperationMode getoperation_mode() {
        return this.mVmStatic.getoperation_mode();
    }

    public void setoperation_mode(OperationMode value) {
        this.mVmStatic.setoperation_mode(value);
    }

    public String gethibernation_vol_handle() {
        return this.mVmDynamic.gethibernation_vol_handle();
    }

    public void sethibernation_vol_handle(String value) {
        this.mVmDynamic.sethibernation_vol_handle(value);
    }

    private Guid storage_pool_idField = new Guid();

    @Override
    @XmlElement(name = "storage_pool_id")
    public Guid getstorage_pool_id() {
        return storage_pool_idField;
    }

    @Override
    public void setstorage_pool_id(Guid value) {
        storage_pool_idField = value;
    }

    private String storage_pool_nameField;

    @XmlElement(name = "storage_pool_name")
    public String getstorage_pool_name() {
        return storage_pool_nameField;
    }

    public void setstorage_pool_name(String value) {
        storage_pool_nameField = value;
    }

    private VdsSelectionAlgorithm selection_algorithmField = VdsSelectionAlgorithm.forValue(0);


    public VdsSelectionAlgorithm getselection_algorithm() {
        return selection_algorithmField;
    }

    public void setselection_algorithm(VdsSelectionAlgorithm value) {
        selection_algorithmField = value;
    }

    @XmlElement(name = "Interfaces")
    public List<VmNetworkInterface> getInterfaces() {
        return mVmStatic.getInterfaces();
    }

    public void setInterfaces(List<VmNetworkInterface> value) {
        mVmStatic.setInterfaces(value);
    }

    public ArrayList<DiskImage> getImages() {
        return mVmStatic.getImages();
    }

    public void setImages(ArrayList<DiskImage> value) {
        mVmStatic.setImages(value);
    }

    // public event PropertyChangedEventHandler PropertyChanged;

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

    private java.util.Map<String, DiskImage> mDiskMap = new java.util.HashMap<String, DiskImage>();

    @XmlElement(name = "DiskList")
    // even this field has no setter, it can not have the final modifier because the GWT serialization mechanizm
    // ignores the final fields
    private String mCdPath = "";
    private String mFloppyPath = "";
    private boolean mRunAndPause = false;

    /**
     * Vitaly change. guest last logout time treatment. If vm stoped without logging out - set last logout time now
     */
    public void guestLogoutTimeTreatmentAfterDestroy() {
        if (getguest_last_login_time() != null
                && (getguest_last_logout_time() == null || getguest_last_login_time().compareTo(
                        getguest_last_logout_time()) > 0)) {
            setguest_last_logout_time(new java.util.Date());
        }
    }

    public boolean isStatusUp() {
        return isStatusUp(getstatus());
    }

    private boolean useSysPrep;

    public boolean useSysPrep() {
        return useSysPrep;
    }

    public void setUseSysPrep(boolean value) {
        useSysPrep = value;
    }

    public boolean getIsFirstRun() {
        return mVmStatic.getIsFirstRun();
    }

    public static boolean isStatusUp(VMStatus st) {
        return (st == VMStatus.Up || st == VMStatus.PoweredDown || st == VMStatus.PoweringDown
                || st == VMStatus.PoweringUp || st == VMStatus.MigratingFrom || st == VMStatus.WaitForLaunch || st == VMStatus.RebootInProgress);

    }

    public static boolean isStatusUpOrPaused(VMStatus st) {
        return (isStatusUp(st) || st == VMStatus.Paused || st == VMStatus.SavingState || st == VMStatus.RestoringState);

    }

    public static boolean isStatusQualifyToMigrate(VMStatus st) {
        // return (st == VMStatus.Up ||
        // st == VMStatus.PoweringUp ||
        // st == VMStatus.Down ||
        // st == VMStatus.Paused);
        return (st == VMStatus.Up || st == VMStatus.PoweringUp || st == VMStatus.Paused || st == VMStatus.RebootInProgress);
    }

    public static boolean isStatusUpOrPausedOrSuspended(VMStatus st) {
        return (isStatusUpOrPaused(st) || st == VMStatus.Suspended);
    }

    public static boolean isStatusDown(VMStatus st) {
        return (st == VMStatus.Down || st == VMStatus.Suspended || st == VMStatus.ImageLocked || st == VMStatus.ImageIllegal);
    }

    public static boolean isGuestUp(VMStatus st) {
        return (st == VMStatus.Up || st == VMStatus.PoweringDown || st == VMStatus.PoweredDown || st == VMStatus.PoweringUp);
    }

    private double _actualDiskWithSnapthotsSize = 0;

    @XmlElement(name = "ActualDiskWithSnapshotsSize")
    public double getActualDiskWithSnapshotsSize() {
        if (_actualDiskWithSnapthotsSize == 0 && getDiskMap() != null) {
            for (DiskImage disk : getDiskMap().values()) {
                _actualDiskWithSnapthotsSize += disk.getActualDiskWithSnapshotsSize();
            }
        }
        return _actualDiskWithSnapthotsSize;
    }

    /**
     * This method is created for SOAP serialization of primitives that are readonly but sent by the client. The setter
     * implementation is empty and the field is not being changed.
     *
     * @param value
     */
    @Deprecated
    public void setActualDiskWithSnapshotsSize(double value) {

    }

    private double _diskSize;

    @XmlElement(name = "DiskSize")
    public double getDiskSize() {
        if (_diskSize == 0) {
            for (DiskImage disk : getDiskMap().values()) {
                _diskSize += disk.getsize() / Double.valueOf(1024 * 1024 * 1024);
            }
        }
        return _diskSize;
    }

    public void setDiskSize(double value) {
        OnPropertyChanged(new PropertyChangedEventArgs("DiskSize"));
        _diskSize = value;
    }

    public VmDynamic getDynamicData() {
        return mVmDynamic;
    }

    public void setDynamicData(VmDynamic value) {
        mVmDynamic = value;
    }

    public VmStatic getStaticData() {
        return mVmStatic;
    }

    public void setStaticData(VmStatic value) {
        if (mVmStatic == null) {
            mVmStatic = new VmStatic();
        }
        mVmStatic = value;
    }

    public VmStatistics getStatisticsData() {
        return mVmStatistics;
    }

    public void setStatisticsData(VmStatistics value) {
        mVmStatistics = value;
    }

    private int mMigreatingToPort;

    public int getMigreatingToPort() {
        return mMigreatingToPort;
    }

    public void setMigreatingToPort(int value) {
        mMigreatingToPort = value;
    }

    private int mMigreatingFromPort;

    public int getMigreatingFromPort() {
        return mMigreatingFromPort;
    }

    public void setMigreatingFromPort(int value) {
        mMigreatingFromPort = value;
    }

    private String run_on_vds_nameField;

    @XmlElement
    public String getrun_on_vds_name() {
        return run_on_vds_nameField;
    }

    public void setrun_on_vds_name(String value) {
        run_on_vds_nameField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("run_on_vds_name"));
    }

    public DiskImage getDriveToImageMap(String drive) {
        DiskImage image = null;
        if (mDiskMap.containsKey(drive)) {
            image = mDiskMap.get(drive);
        }
        return image;
    }

    @XmlElement(name = "DiskValueObjectMap")
    public ValueObjectMap getDiskValueObjectMap() {
        return new ValueObjectMap(mDiskMap, false);
    }

    public void setDiskValueObjectMap(ValueObjectMap serializedDiskMap) {
        if (serializedDiskMap != null) {
            mDiskMap = serializedDiskMap.asMap();
        }
    }

    public Map<String, DiskImage> getDiskMap() {
        return mDiskMap;
    }

    public void setDiskMap(Map<String,DiskImage> diskMap) {
        mDiskMap = diskMap;
    }

    public int getDiskMapCount() {
        return mDiskMap.size();
    }

    @XmlElement(name = "MinAllocatedMem")
    public int getMinAllocatedMem() {
        return mVmStatic.getMinAllocatedMem();
    }

    public void setMinAllocatedMem(int value) {
        mVmStatic.setMinAllocatedMem(value);
    }
    // This function is left only to leave the option of creating a VM without
    // having all the data in the DB
    // Currently it is used mainly by tests and VdcClient (for direct acccess to
    // the VDS)
    // TO CONSIDER removing this function
    public void addDriveToImageMap(String drive, DiskImage image) {
        mDiskMap.put(drive, image);
        getDiskList().add(image);
    }

    public String getCdPath() {
        return mCdPath;
    }

    public void setCdPath(String value) {
        mCdPath = value;
    }

    public String getFloppyPath() {
        return mFloppyPath;
    }

    public void setFloppyPath(String value) {
        mFloppyPath = value;
    }

    public boolean getRunAndPause() {
        return mRunAndPause;
    }

    public void setRunAndPause(boolean value) {
        mRunAndPause = value;
    }

    public boolean getWin2kHackEnable() {
        return getDynamicData().getWin2kHackEnable();
    }

    public void setWin2kHackEnable(boolean value) {
        getDynamicData().setWin2kHackEnable(value);
    }

    /**
     * update vm dynamic data
     *
     * @param vm
     * @param vdsId
     * @param vdsName
     */
    public void updateRunTimeDynamicData(VmDynamic vm, Guid vdsId, String vdsName) {
        setstatus(vm.getstatus());
        setrun_on_vds(vdsId);
        setrun_on_vds_name(vdsName);
        setdisplay(vm.getdisplay());
        setdisplay_secure_port(vm.getdisplay_secure_port());
        setvm_host(vm.getvm_host());
        setvm_ip(vm.getvm_ip());

        // if (!string.IsNullOrEmpty(vm.app_list))
        // {
        setapp_list(vm.getapp_list());
        // }
        setguest_os(vm.getguest_os());
        setdisplay_type(vm.getdisplay_type());
        setdisplay_ip(vm.getdisplay_ip());
        setkvm_enable(vm.getkvm_enable());
        setacpi_enable(vm.getacpi_enable());
        setWin2kHackEnable(vm.getWin2kHackEnable());
        setutc_diff(vm.getutc_diff());
        setExitStatus(vm.getExitStatus());
        setExitMessage(vm.getExitMessage());
        setclient_ip(vm.getclient_ip());
        setVmPauseStatus(vm.getPauseStatus());

        // TODO: check what to do with update disk data
        // updateDisksData(vm);

        // updateSession(vm);
    }

    // public string qemuAudioDrv;
    /**
     * update vm statistics data
     *
     * @param vm
     */
    public void updateRunTimeStatisticsData(VmStatistics vmStatistics, VM vm) {
        setelapsed_time(vmStatistics.getelapsed_time());
        setusage_network_percent(vmStatistics.getusage_network_percent());
        vm.getStatisticsData().setDisksUsage(vmStatistics.getDisksUsage());
        // -------- cpu --------------
        setcpu_sys(vmStatistics.getcpu_sys());
        setcpu_user(vmStatistics.getcpu_user());
        if ((getcpu_sys() != null) && (getcpu_user() != null)) {
            Double percent = (getcpu_sys() + getcpu_user()) / new Double(vm.getnum_of_cpus());
            setusage_cpu_percent(percent.intValue());
            if (getusage_cpu_percent() != null && getusage_cpu_percent() > 100) {
                setusage_cpu_percent(100);
            }
        }
        // -------- memory --------------
        setusage_mem_percent(vmStatistics.getusage_mem_percent());
    }

    /**
     * Check if two Vms are Equal. Current equality rule is: Two Vms are equal when them points to same object or have
     * same vm_guid property
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (super.equals(obj)) {
            return true;
        }
        VM eq = (VM) ((obj instanceof VM) ? obj : null);
        if (eq != null) {
            if (eq.getId().equals(this.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @XmlElement(name = "VmPoolName")
    public String getVmPoolName() {
        return mVmPoolName;
        // string returnValue = null;
        // vm_pool_map map = DbFacade.Instance.GetVmPoolsMapByVmGuid(vm_guid);
        // if (map != null)
        // {
        // vm_pools pool =
        // DbFacade.Instance.GetVmPoolByVmPoolId(map.vm_pool_id.Value);
        // if (pool != null)
        // {
        // returnValue = pool.vm_pool_name;
        // }
        // }
        // return returnValue;
    }

    public void setVmPoolName(String value) {
        mVmPoolName = value;
    }

    @XmlElement(name = "VmPoolId", nillable = true)
    public NGuid getVmPoolId() {
        return mVmPoolId;
        // int? returnValue = null;
        // vm_pool_map map = DbFacade.Instance.GetVmPoolsMapByVmGuid(vm_guid);
        // if (map != null)
        // {
        // vm_pools pool =
        // DbFacade.Instance.GetVmPoolByVmPoolId(map.vm_pool_id.Value);
        // if (pool != null)
        // {
        // returnValue = pool.vm_pool_id;
        // }
        // }
        // return returnValue;
    }

    public void setVmPoolId(NGuid value) {
        mVmPoolId = value;
        OnPropertyChanged(new PropertyChangedEventArgs("VmPoolId"));
    }

    /**
     * Version in .Net style: a.b.c.d when a: major version, b: minor version , c: major revision, d: minor revision
     * assumption: Qumranet Agent version stored in app_list by "Qumranet Agent" name. Qumranet Agent version, recieved
     * from vds in format : a.b.d there is no major revision recieved from vds - always 0
     */
    @XmlElement(name = "GuestAgentVersion")
    private Version privateGuestAgentVersion;

    public Version getGuestAgentVersion() {
        return privateGuestAgentVersion;
    }

    public void setGuestAgentVersion(Version value) {
        privateGuestAgentVersion = value;
    }

    public Version getPartialVersion() {
        Version initial = getGuestAgentVersion();
        return initial == null ? null : new Version(initial.getMajor(), initial.getMinor());
    }

    public boolean getHasAgent() {
        return getGuestAgentVersion() != null;
    }

    @XmlElement(name = "SpiceDriverVersion")
    private Version privateSpiceDriverVersion;

    public Version getSpiceDriverVersion() {
        return privateSpiceDriverVersion;
    }

    public void setSpiceDriverVersion(Version value) {
        privateSpiceDriverVersion = value;
    }

    public boolean getHasSpiceDriver() {
        return getSpiceDriverVersion() != null;
    }

    // @XmlElement(name = "vds_group_cpu_flags_data")
    private String privatevds_group_cpu_flags_data;

    public String getvds_group_cpu_flags_data() {
        return privatevds_group_cpu_flags_data;
    }

    public void setvds_group_cpu_flags_data(String value) {
        privatevds_group_cpu_flags_data = value;
    }

    private boolean transparentHugePages;

    @XmlElement(name = "TransparentHugePages")
    public boolean getTransparentHugePages() {
        return this.transparentHugePages;
    }

    public void setTransparentHugePages(boolean value) {
        this.transparentHugePages = value;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    private static final java.util.ArrayList<String> _vmProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "vm_name", "status", "usage_cpu_percent",
                    "usage_mem_percent", "usage_network_percent", "run_on_vds", "run_on_vds_name",
                    "vm_description", "vds_group_id", "vds_group_name", "vm_ip",
                    "guest_cur_user_name", "DiskSize", "vm_os", "num_of_monitors", "roundedElapsedTime",
                    "vm_mem_size_mb", "vm_domain", "dedicated_vm_for_vds",
                    "guest_requested_memory", "is_stateless", "is_initialized", "display",
                    "display_type", "default_display_type", "run_on_vds", "app_list", "time_zone",
                    "display_secure_port", "IsConfigured", "is_auto_suspend", "auto_startup",
                    "display_ip", "priority", "default_boot_sequence", "iso_path", "VmPoolId",
                    "num_of_sockets", "cpu_per_socket", "vds_group_compatibility_version",
                    "usb_policy", "vmt_guid", "vmt_name", "initrd_url", "kernel_url",
                    "kernel_params", "VmPauseStatus", "CustomProperties", "MigrationSupport","num_of_cpus","MinAllocatedMem"}));

    // ,"DiskSize"
    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _vmProperties;
    }

    /**
     * Return true if vm has at least one Disk and one Interface
     */
    private Boolean _IsConfigured;

    public boolean getIsConfigured() {
        if (_IsConfigured == null) {
            _IsConfigured =
                    (getInterfaces() != null && getDiskMap() != null && getInterfaces().size() > 0 && getDiskMap()
                            .size() > 0);
        }
        return _IsConfigured;
    }

    public void setIsConfigured(boolean value) {
        _IsConfigured = value;
        OnPropertyChanged(new PropertyChangedEventArgs("IsConfigured"));
    }

    public ArrayList<DiskImage> getDiskList() {
        return mVmStatic.getDiskList();
    }

    public Map<Guid, VmDevice> getManagedVmDeviceMap() {
        return mVmStatic.getManagedVmDeviceMap();
    }

    public void setManagedDeviceMap(Map<Guid, VmDevice> map) {
        mVmStatic.setManagedDeviceMap(map);
    }

    public List<VmDevice> getVmUnamagedDeviceList() {
        return mVmStatic.getUnmanagedDeviceList();
    }

    public void setUnmanagedDeviceList(List<VmDevice> list) {
        mVmStatic.setUnmanagedDeviceList(list);
    }

}
