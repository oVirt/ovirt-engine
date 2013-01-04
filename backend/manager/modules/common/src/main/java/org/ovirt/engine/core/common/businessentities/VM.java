package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

public class VM extends IVdcQueryable implements Serializable, BusinessEntity<Guid>, HasStoragePool<Guid>, Nameable {
    private static final long serialVersionUID = -4078140531074414263L;
    @Valid
    private VmStatic vmStatic;

    private VmDynamic vmDynamic;
    private VmStatistics vmStatistics;
    private VmPayload vmPayload;
    private boolean balloonEnabled = true;

    @Valid
    private List<Snapshot> snapshots = new ArrayList<Snapshot>();
    private boolean runOnce = false;

    public String getUserDefinedProperties() {
        return vmStatic.getUserDefinedProperties();
    }

    public void setUserDefinedProperties(String userDefinedProperties) {
        vmStatic.setUserDefinedProperties(userDefinedProperties);
    }

    public String getPredefinedProperties() {
        return vmStatic.getPredefinedProperties();
    }

    public String getCustomProperties() {
        return vmStatic.getCustomProperties();
    }

    public void setCustomProperties(String customProperties) {
        vmStatic.setCustomProperties(customProperties);
    }

    public void setPredefinedProperties(String predefinedProperties) {
        vmStatic.setPredefinedProperties(predefinedProperties);
    }

    public VM() {
        vmStatic = new VmStatic();
        vmDynamic = new VmDynamic();
        vmStatistics = new VmStatistics();
        setImages(new ArrayList<DiskImage>());
        setInterfaces(new ArrayList<VmNetworkInterface>());
        diskMap = new HashMap<Guid, Disk>();
        cdPath = "";
        floppyPath = "";
        runAndPause = false;
        diskSize = 0;
    }

    public VM(VmStatic vmStatic, VmDynamic vmDynamic, VmStatistics vmStatistics) {
        diskMap = new HashMap<Guid, Disk>();
        cdPath = "";
        floppyPath = "";
        runAndPause = false;
        diskSize = 0;

        this.vmStatic = (vmStatic == null) ? new VmStatic() : vmStatic;
        this.vmDynamic = vmDynamic;
        this.vmStatistics = vmStatistics;
        setImages(new ArrayList<DiskImage>());
        setInterfaces(new ArrayList<VmNetworkInterface>());
    }

    public VM(Guid vm_guid, String vm_name, int vm_mem_size_mb, Guid vmt_guid, VmOsType vm_os, String vm_description,
            Guid vds_group_id, String vds_group_name, String vds_group_description, String vmt_name,
            int vmt_mem_size_mb, VmOsType vmt_os, Date vmt_creation_date, int vmt_child_count,
            int vmt_num_of_cpus, int vmt_num_of_sockets, int vmt_cpu_per_socket, String vmt_description, int status,
            String vm_ip, String vm_host, Integer vm_pid, Date vm_last_up_time,
            Date vm_last_boot_time, String guest_cur_user_name, Date guest_last_login_time,
            Date guest_last_logout_time, String guest_os, Double cpu_user, Double cpu_sys, Integer vm_if_id_1, String vm_if_name_1,
            Integer vm_line_rate_1, java.math.BigDecimal rx_dropped_1, java.math.BigDecimal rx_rate_1,
            java.math.BigDecimal tx_dropped_1, java.math.BigDecimal tx_rate_1, Integer vm_if_id_2, String vm_if_name_2,
            Integer vm_line_rate_2, java.math.BigDecimal rx_dropped_2, java.math.BigDecimal rx_rate_2,
            java.math.BigDecimal tx_dropped_2, java.math.BigDecimal tx_rate_2, Double elapsed_time,
            Integer usage_network_percent, Integer usage_mem_percent, Integer usage_cpu_percent, Guid run_on_vds,
            Guid migrating_to_vds, String app_list, Integer display, String vm_domain, Date vm_creation_date,
            String run_on_vds_name, String time_zone, Boolean acpi_enable, Integer session, String display_ip,
            Integer display_type, Boolean kvm_enable, Integer boot_sequence, String vmt_time_zone,
            Integer display_secure_port, Integer utc_diff, boolean is_stateless, String vds_cpu_name,
            boolean fail_back, BootSequence default_boot_sequence, VmType vm_type,
            int minAllocatedMem) {
        vmStatic = new VmStatic();
        vmDynamic = new VmDynamic();
        vmStatistics = new VmStatistics();
        setImages(new ArrayList<DiskImage>());
        setInterfaces(new ArrayList<VmNetworkInterface>());
        diskMap = new HashMap<Guid, Disk>();
        cdPath = "";
        floppyPath = "";
        runAndPause = false;
        diskSize = 0;

        this.setId(vm_guid);
        this.setVmName(vm_name);
        this.setVmMemSizeMb(vm_mem_size_mb);
        this.setVmtGuid(vmt_guid);
        this.setVmDomain(vm_domain);
        this.setVmOs(vm_os);
        this.setVmCreationDate(vm_creation_date);
        this.setVmDescription(vm_description);
        this.setVdsGroupId(vds_group_id);
        this.vdsGroupName = vds_group_name;
        this.vdsGroupDescription = vds_group_description;
        this.vmtName = vmt_name;
        this.vmtMemSizeMb = vmt_mem_size_mb;
        this.vmtOs = vmt_os;
        this.vmtCreationDate = vmt_creation_date;
        this.vmtchildCount = vmt_child_count;
        this.vmtNumOfCpus = vmt_num_of_cpus;
        this.vmtDescription = vmt_description;
        this.vmtTimeZone = vmt_time_zone;
        this.setStatus(VMStatus.forValue(status));
        this.setVmIp(vm_ip);
        this.setVmHost(vm_host);
        this.setVmPid(vm_pid);
        this.setLastStartTime(vm_last_boot_time);
        this.setGuestCurrentUserName(guest_cur_user_name);
        this.setGuestLastLoginTime(guest_last_login_time);
        this.setGuestLastLogoutTime(guest_last_logout_time);
        this.setGuestOs(guest_os);
        this.setCpuUser(cpu_user);
        this.setCpuSys(cpu_sys);
        this.setElapsedTime(elapsed_time);
        this.setUsageNetworkPercent(usage_network_percent);
        this.setUsageMemPercent(usage_mem_percent);
        this.setUsageCpuPercent(usage_cpu_percent);
        this.setRunOnVds(run_on_vds);
        this.setMigratingToVds(migrating_to_vds);
        this.setAppList(app_list);
        this.setDisplay(display);
        this.runOnVdsName = run_on_vds_name;
        this.setTimeZone(time_zone);
        this.setAcpiEnable(acpi_enable);
        this.setSession(SessionState.forValue(session));
        this.setDisplayIp(display_ip);
        this.setDisplayType(DisplayType.forValue(display_type));
        this.setKvmEnable(kvm_enable);
        this.setBootSequence(BootSequence.forValue(boot_sequence));
        this.setVmtTimeZone(vmt_time_zone);
        this.setDisplaySecurePort(display_secure_port);
        this.setUtcDiff(utc_diff);
        this.setStateless(is_stateless);
        this.setVdsGroupCpuName(vds_cpu_name);
        this.setFailBack(fail_back);
        this.setDefaultBootSequence(default_boot_sequence);
        this.setVmType(vm_type);
        this.setMinAllocatedMem(minAllocatedMem);
    }

    public VmPauseStatus getVmPauseStatus() {
        return this.vmDynamic.getPauseStatus();
    }

    public void setVmPauseStatus(VmPauseStatus aPauseStatus) {
        this.vmDynamic.setPauseStatus(aPauseStatus);
    }

    @Override
    public Guid getId() {
        return this.vmStatic.getId();
    }

    @Override
    public void setId(Guid value) {
        this.vmStatic.setId(value);
        this.vmDynamic.setId(value);
        this.vmStatistics.setId(value);
    }

    public String getVmName() {
        return this.vmStatic.getVmName();
    }

    public void setVmName(String value) {
        this.vmStatic.setVmName(value);
    }

    public int getMemSizeMb() {
        return this.getVmMemSizeMb();
    }

    public int getVmMemSizeMb() {
        return this.vmStatic.getMemSizeMb();
    }

    public void setVmMemSizeMb(int value) {
        this.vmStatic.setMemSizeMb(value);
    }

    public String getVmDomain() {
        return this.vmStatic.getDomain();
    }

    public void setVmDomain(String value) {
        this.vmStatic.setDomain(value);
    }

    public VmOsType getOs() {
        return this.getVmOs();
    }

    public VmOsType getVmOs() {
        return this.vmStatic.getOs();
    }

    public void setVmOs(VmOsType value) {
        this.vmStatic.setOs(value);
    }

    public Date getVmCreationDate() {
        return this.vmStatic.getCreationDate();
    }

    public void setVmCreationDate(Date value) {
        this.vmStatic.setCreationDate(value);
    }

    public Guid getQuotaId() {
        return this.vmStatic.getQuotaId();
    }

    public void setQuotaId(Guid value) {
        this.vmStatic.setQuotaId(value);
    }

    public String getQuotaName() {
        return this.vmStatic.getQuotaName();
    }

    public void setQuotaName(String value) {
        this.vmStatic.setQuotaName(value);
    }

    public boolean isQuotaDefault() {
        return this.vmStatic.isQuotaDefault();
    }

    public void setIsQuotaDefault(boolean isQuotaDefault) {
        this.vmStatic.setQuotaDefault(isQuotaDefault);
    }

    public QuotaEnforcementTypeEnum getQuotaEnforcementType() {
        return this.vmStatic.getQuotaEnforcementType();
    }

    public void setQuotaEnforcementType(QuotaEnforcementTypeEnum quotaEnforcementType) {
        this.vmStatic.setQuotaEnforcementType(quotaEnforcementType);
    }

    public String getDescription() {
        return this.getVmDescription();
    }

    public String getVmDescription() {
        return this.vmStatic.getDescription();
    }

    public void setVmDescription(String value) {
        this.vmStatic.setDescription(value);
    }

    public int getNumOfMonitors() {
        return this.vmStatic.getNumOfMonitors();
    }

    public void setNumOfMonitors(int value) {
        this.vmStatic.setNumOfMonitors(value);
    }

    public boolean getAllowConsoleReconnect() {
        return this.vmStatic.isAllowConsoleReconnect();
    }

    public void setAllowConsoleReconnect(boolean value) {
        this.vmStatic.setAllowConsoleReconnect(value);
    }

    public boolean isInitialized() {
        return this.vmStatic.isInitialized();
    }

    public void setInitialized(boolean value) {
        this.vmStatic.setInitialized(value);
    }

    public boolean isAutoSuspend() {
        return this.vmStatic.isAutoSuspend();
    }

    public void setAutoSuspend(boolean value) {
        this.vmStatic.setAutoSuspend(value);
    }

    public int getNumOfCpus() {
        return this.vmStatic.getNumOfCpus();
    }

    /**
     * This method is created for SOAP serialization of primitives that are readonly but sent by the client. The setter
     * implementation is empty and the field is not being changed.
     *
     * @param value
     */
    @Deprecated
    public void setNumOfCpus(int value) {
        // Purposely empty
    }

    public int getNumOfSockets() {
        return this.vmStatic.getNumOfSockets();
    }

    public void setNumOfSockets(int value) {
        this.vmStatic.setNumOfSockets(value);
    }

    public int getCpuPerSocket() {
        return this.vmStatic.getCpuPerSocket();
    }

    public void setCpuPerSocket(int value) {
        this.vmStatic.setCpuPerSocket(value);
    }

    public UsbPolicy getUsbPolicy() {
        return vmStatic.getUsbPolicy();
    }

    public void setUsbPolicy(UsbPolicy value) {
        vmStatic.setUsbPolicy(value);
    }

    public boolean isAutoStartup() {
        return vmStatic.isAutoStartup();
    }

    public void setAutoStartup(boolean value) {
        vmStatic.setAutoStartup(value);
    }

    public NGuid getDedicatedVmForVds() {
        return vmStatic.getDedicatedVmForVds();
    }

    public void setDedicatedVmForVds(NGuid value) {
        vmStatic.setDedicatedVmForVds(value);
    }

    public Guid getVdsGroupId() {
        return this.vmStatic.getVdsGroupId();
    }

    public void setVdsGroupId(Guid value) {
        this.vmStatic.setVdsGroupId(value);
    }

    public String getTimeZone() {
        return vmStatic.getTimeZone();
    }

    public void setTimeZone(String value) {
        vmStatic.setTimeZone(value);
    }

    public boolean isStateless() {
        return vmStatic.isStateless();
    }

    public boolean isSmartcardEnabled() {
        return vmStatic.isSmartcardEnabled();
    }

    public void setSmartcardEnabled(boolean isSmartcardEnabled) {
        vmStatic.setSmartcardEnabled(isSmartcardEnabled);
    }

    public void setStateless(boolean value) {
        vmStatic.setStateless(value);
    }

    public void setDeleteProtected(boolean deleteProtected) {
        vmStatic.setDeleteProtected(deleteProtected);
    }

    public boolean isDeleteProtected() {
        return vmStatic.isDeleteProtected();
    }

    public DisplayType getDefaultDisplayType() {
        return vmStatic.getDefaultDisplayType();
    }

    public void setDefaultDisplayType(DisplayType value) {
        vmStatic.setDefaultDisplayType(value);
    }

    public int getPriority() {
        return vmStatic.getPriority();
    }

    public void setPriority(int value) {
        vmStatic.setPriority(value);
    }

    public String getIsoPath() {
        return vmStatic.getIsoPath();
    }

    public void setIsoPath(String value) {
        vmStatic.setIsoPath(value);
    }

    public OriginType getOrigin() {
        return vmStatic.getOrigin();
    }

    public void setOrigin(OriginType value) {
        vmStatic.setOrigin(value);
    }

    public String getInitrdUrl() {
        return vmStatic.getInitrdUrl();
    }

    public void setInitrdUrl(String value) {
        vmStatic.setInitrdUrl(value);
    }

    public String getKernelUrl() {
        return vmStatic.getKernelUrl();
    }

    public void setKernelUrl(String value) {
        vmStatic.setKernelUrl(value);
    }

    public String getKernelParams() {
        return vmStatic.getKernelParams();
    }

    public void setKernelParams(String value) {
        vmStatic.setKernelParams(value);
    }

    private NGuid vmPoolId;
    private String vmPoolName;

    public VMStatus getStatus() {
        return this.vmDynamic.getstatus();
    }

    public void setStatus(VMStatus value) {
        this.vmDynamic.setstatus(value);
    }

    public String getVmIp() {
        return this.vmDynamic.getvm_ip();
    }

    public void setVmIp(String value) {
        this.vmDynamic.setvm_ip(value);
    }

    public String getVmHost() {
        String vmHost = this.vmDynamic.getvm_host();
        if (!StringHelper.isNullOrEmpty(this.getVmIp())) {
            this.vmDynamic.setvm_host(getVmIp());
        } else {
            String vmDomain = getVmDomain();

            // If VM's host name isn't available - set as VM's name
            // If no IP address is available - assure that 'vm_host' is FQN by concatenating
            // vmHost and vmDomain.
            if (StringHelper.isNullOrEmpty(vmHost)) {
                vmHost = StringHelper.isNullOrEmpty(vmDomain) ? getVmName() : getVmName() + "." + vmDomain;
                this.vmDynamic.setvm_host(vmHost);
            } else if (!StringHelper.isNullOrEmpty(vmDomain) && !vmHost.endsWith(vmDomain)) {
                this.vmDynamic.setvm_host(vmHost + "." + vmDomain);
            }
        }

        return this.vmDynamic.getvm_host();
    }

    public void setVmHost(String value) {
        this.vmDynamic.setvm_host(value);
    }

    public Integer getVmPid() {
        return this.vmDynamic.getvm_pid();
    }

    public void setVmPid(Integer value) {
        this.vmDynamic.setvm_pid(value);
    }

    public Date getLastStartTime() {
        return this.vmDynamic.getLastStartTime();
    }

    public void setLastStartTime(Date value) {
        this.vmDynamic.setLastStartTime(value);
    }

    public String getConsoleCurentUserName() {
        return this.vmDynamic.getConsole_current_user_name();
    }

    public void setConsoleCurrentUserName(String value) {
        this.vmDynamic.setConsole_current_user_name(value);
    }

    public String getGuestCurentUserName() {
        return this.vmDynamic.getguest_cur_user_name();
    }

    public void setGuestCurrentUserName(String value) {
        this.vmDynamic.setguest_cur_user_name(value);
    }

    public Date getGuestLastLoginTime() {
        return this.vmDynamic.getguest_last_login_time();
    }

    public void setGuestLastLoginTime(Date value) {
        this.vmDynamic.setguest_last_login_time(value);
    }

    public NGuid getConsoleUserId() {
        return this.vmDynamic.getConsoleUserId();
    }

    public void setConsoleUserId(NGuid value) {
        this.vmDynamic.setConsoleUserId(value);
    }

    public Date getGuestLastLogoutTime() {
        return this.vmDynamic.getguest_last_logout_time();
    }

    public void setGuestLastLogoutTime(Date value) {
        this.vmDynamic.setguest_last_logout_time(value);
    }

    public String getGuestOs() {
        return this.vmDynamic.getguest_os();
    }

    public void setGuestOs(String value) {
        this.vmDynamic.setguest_os(value);
    }

    public NGuid getRunOnVds() {
        return this.vmDynamic.getrun_on_vds();
    }

    public void setRunOnVds(NGuid value) {
        this.vmDynamic.setrun_on_vds(value);
    }

    public NGuid getmigrating_to_vds() {
        return this.vmDynamic.getmigrating_to_vds();
    }

    public void setMigratingToVds(NGuid value) {
        this.vmDynamic.setmigrating_to_vds(value);
    }

    public String getAppList() {
        return this.vmDynamic.getapp_list();
    }

    public void setAppList(String value) {
        this.vmDynamic.setapp_list(value);
    }

    public Integer getDisplay() {
        return this.vmDynamic.getdisplay();
    }

    public void setDisplay(Integer value) {
        this.vmDynamic.setdisplay(value);
    }

    public Boolean getAcpiEnable() {
        return this.vmDynamic.getacpi_enable();
    }

    public void setAcpiEnable(Boolean value) {
        this.vmDynamic.setacpi_enable(value);
    }

    public String getDisplayIp() {
        return this.vmDynamic.getdisplay_ip();
    }

    public void setDisplayIp(String value) {
        this.vmDynamic.setdisplay_ip(value);
    }

    public DisplayType getDisplayType() {
        return this.vmDynamic.getdisplay_type();
    }

    public void setDisplayType(DisplayType value) {
        this.vmDynamic.setdisplay_type(value);
    }

    public Boolean getKvmEnable() {
        return this.vmDynamic.getkvm_enable();
    }

    public void setKvmEnable(Boolean value) {
        this.vmDynamic.setkvm_enable(value);
    }

    public SessionState getSession() {
        return this.vmDynamic.getsession();
    }

    public void setSession(SessionState value) {
        this.vmDynamic.setsession(value);
    }

    public BootSequence getBootSequence() {
        return this.vmDynamic.getboot_sequence();
    }

    public void setBootSequence(BootSequence value) {
        this.vmDynamic.setboot_sequence(value);
    }

    public Integer getDisplaySecurePort() {
        return this.vmDynamic.getdisplay_secure_port();
    }

    public void setDisplaySecurePort(Integer value) {
        this.vmDynamic.setdisplay_secure_port(value);
    }

    public VmExitStatus getExitStatus() {
        return this.vmDynamic.getExitStatus();
    }

    public void setExitStatus(VmExitStatus value) {
        this.vmDynamic.setExitStatus(value);
    }

    public String getExitMessage() {
        return this.vmDynamic.getExitMessage();
    }

    public void setExitMessage(String value) {
        this.vmDynamic.setExitMessage(value);
    }

    public Integer getUtcDiff() {
        return this.vmDynamic.getutc_diff();
    }

    public void setUtcDiff(Integer value) {
        this.vmDynamic.setutc_diff(value);
    }

    public NGuid getLastVdsRunOn() {
        return this.vmDynamic.getlast_vds_run_on();
    }

    public void setLastVdsRunOn(NGuid value) {
        this.vmDynamic.setlast_vds_run_on(value);
    }

    public String getClientIp() {
        return this.vmDynamic.getclient_ip();
    }

    public void setClientIp(String value) {
        this.vmDynamic.setclient_ip(value);
    }

    public Integer getGuestRequestedMemory() {
        return this.vmDynamic.getguest_requested_memory();
    }

    public void setGuestRequestedMemory(Integer value) {
        this.vmDynamic.setguest_requested_memory(value);
    }

    public String getHash() {
        return vmDynamic.getHash();
    }

    public void setHash(String hash) {
        vmDynamic.setHash(hash);
    }

    public int getGuestAgentNicsHash() {
        return vmDynamic.getGuestAgentNicsHash();
    }

    public void setGuestAgentNicsHash(int guestAgentNicsHash) {
        vmDynamic.setGuestAgentNicsHash(guestAgentNicsHash);
    }

    public Double getCpuUser() {
        return this.vmStatistics.getcpu_user();
    }

    public void setCpuUser(Double value) {
        this.vmStatistics.setcpu_user(value);
    }

    public Double getCpuSys() {
        return this.vmStatistics.getcpu_sys();
    }

    public void setCpuSys(Double value) {
        this.vmStatistics.setcpu_sys(value);
    }

    public Double getElapsedTime() {
        return this.vmStatistics.getelapsed_time();
    }

    public void setElapsedTime(Double value) {
        this.vmStatistics.setelapsed_time(value);
    }

    public Double getRoundedElapsedTime() {
        return this.vmStatistics.getRoundedElapsedTime();
    }

    public void setRoundedElapsedTime(Double value) {
        this.vmStatistics.setRoundedElapsedTime(value);
    }

    public Integer getUsageNetworkPercent() {
        return this.vmStatistics.getusage_network_percent();
    }

    public void setUsageNetworkPercent(Integer value) {
        this.vmStatistics.setusage_network_percent(value);
    }

    public Integer getUsageMemPercent() {
        return this.vmStatistics.getusage_mem_percent();
    }

    public void setUsageMemPercent(Integer value) {
        this.vmStatistics.setusage_mem_percent(value);
    }

    public Integer getUsageCpuPercent() {
        return this.vmStatistics.getusage_cpu_percent();
    }

    public void setUsageCpuPercent(Integer value) {
        this.vmStatistics.setusage_cpu_percent(value);
    }

    public Guid getVmtGuid() {
        return this.vmStatic.getVmtGuid();
    }

    public void setVmtGuid(Guid value) {
        this.vmStatic.setVmtGuid(value);
    }

    private String vmtName;

    public String getVmtName() {
        return this.vmtName;
    }

    public void setVmtName(String value) {
        this.vmtName = value;
    }

    private int vmtMemSizeMb;

    public int getVmtMemSizeMb() {
        return this.vmtMemSizeMb;
    }

    public void setVmtMemSizeMb(int value) {
        this.vmtMemSizeMb = value;
    }

    private VmOsType vmtOs = VmOsType.forValue(0);

    public VmOsType getVmtOs() {
        return this.vmtOs;
    }

    public void setVmtOs(VmOsType value) {
        this.vmtOs = value;
    }

    private Date vmtCreationDate = new Date(0);

    public Date getVmtCreationDate() {
        return this.vmtCreationDate;
    }

    public void setVmtCreationDate(Date value) {
        this.vmtCreationDate = value;
    }

    private int vmtchildCount;

    public int getVmtChildCount() {
        return this.vmtchildCount;
    }

    public void setVmtChildCount(int value) {
        this.vmtchildCount = value;
    }

    private int vmtNumOfCpus;

    public int getVmtNumOfCpus() {
        return this.vmtNumOfCpus;
    }

    public void setVmtNumOfCpus(int value) {
        this.vmtNumOfCpus = value;
    }

    private int vmtNumOfSockets;

    public int getVmtNumOfSockets() {
        return this.vmtNumOfSockets;
    }

    public void setVmtNumOfSockets(int value) {
        this.vmtNumOfSockets = value;
    }

    private int vmtCpuPerSocket;

    public int getVmtCpuPerSocket() {
        return this.vmtCpuPerSocket;
    }

    public void setVmtCpuPerSocket(int value) {
        this.vmtCpuPerSocket = value;
    }

    private String vmtDescription;

    public String getVmtDescription() {
        return this.vmtDescription;
    }

    public void setVmtDescription(String value) {
        this.vmtDescription = value;
    }

    private String vmtTimeZone;

    public String getVmtTimeZone() {
        return vmtTimeZone;
    }

    public void setVmtTimeZone(String value) {
        vmtTimeZone = value;
    }

    private Version vdsGroupCompatibilityVersion;

    public Version getVdsGroupCompatibilityVersion() {
        return this.vdsGroupCompatibilityVersion;
    }

    public void setVdsGroupCompatibilityVersion(Version value) {
        if (Version.OpInequality(getVdsGroupCompatibilityVersion(), value)) {
            this.vdsGroupCompatibilityVersion = value;
        }
    }

    private String vdsGroupName;

    public String getVdsGroupName() {
        return this.vdsGroupName;
    }

    public void setVdsGroupName(String value) {
        this.vdsGroupName = value;
    }

    private String vdsGroupDescription;

    public String getVdsGroupDescription() {
        return this.vdsGroupDescription;
    }

    public void setVdsGroupDescription(String value) {
        this.vdsGroupDescription = value;
    }

    private String vdsGroupCpuName;

    public String getVdsGroupCpuName() {
        return this.vdsGroupCpuName;
    }

    public void setVdsGroupCpuName(String value) {
        this.vdsGroupCpuName = value;
    }

    public boolean isFailBack() {
        return this.vmStatic.isFailBack();
    }

    public void setFailBack(boolean value) {
        this.vmStatic.setFailBack(value);
    }

    public BootSequence getDefaultBootSequence() {
        return this.vmStatic.getDefaultBootSequence();
    }

    public void setDefaultBootSequence(BootSequence value) {
        this.vmStatic.setDefaultBootSequence(value);
    }

    public int getNiceLevel() {
        return this.vmStatic.getNiceLevel();
    }

    public void setNiceLevel(int value) {
        this.vmStatic.setNiceLevel(value);
    }

    public void setDbGeneration(long value) {
        this.vmStatic.setDbGeneration(value);
    }

    public long getDbGeneration() {
        return vmStatic.getDbGeneration();
    }

    public MigrationSupport getMigrationSupport() {
        return this.vmStatic.getMigrationSupport();
    }

    public void setMigrationSupport(MigrationSupport migrationSupport) {
        this.vmStatic.setMigrationSupport(migrationSupport);
    }

    public VmType getVmType() {
        return this.vmStatic.getVmType();
    }

    public void setVmType(VmType value) {
        this.vmStatic.setVmType(value);
    }

    public String getHibernationVolHandle() {
        return this.vmDynamic.gethibernation_vol_handle();
    }

    public void setHibernationVolHandle(String value) {
        this.vmDynamic.sethibernation_vol_handle(value);
    }

    public void setExportDate(Date value) {
        this.vmStatic.setExportDate(value);
    }

    public Date getExportDate() {
        return this.vmStatic.getExportDate();
    }

    private Guid storagePoolId = new Guid();

    @Override
    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    @Override
    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
    }

    private String storagePoolName;

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public void setStoragePoolName(String value) {
        storagePoolName = value;
    }

    private VdsSelectionAlgorithm selectionAlgorithm = VdsSelectionAlgorithm.forValue(0);

    public VdsSelectionAlgorithm getSelectionAlgorithm() {
        return selectionAlgorithm;
    }

    public void setSelectionAlgorithm(VdsSelectionAlgorithm value) {
        selectionAlgorithm = value;
    }

    public List<VmNetworkInterface> getInterfaces() {
        return vmStatic.getInterfaces();
    }

    public void setInterfaces(List<VmNetworkInterface> value) {
        vmStatic.setInterfaces(value);
    }

    public ArrayList<DiskImage> getImages() {
        return vmStatic.getImages();
    }

    public void setImages(ArrayList<DiskImage> value) {
        vmStatic.setImages(value);
    }

    private Map<Guid, Disk> diskMap = new HashMap<Guid, Disk>();

    // even this field has no setter, it can not have the final modifier because the GWT serialization mechanizm
    // ignores the final fields
    private String cdPath = "";
    private String floppyPath = "";
    private boolean runAndPause = false;

    /**
     * Vitaly change. guest last logout time treatment. If vm stoped without logging out - set last logout time now
     */
    public void guestLogoutTimeTreatmentAfterDestroy() {
        if (getGuestLastLoginTime() != null
                && (getGuestLastLogoutTime() == null || getGuestLastLoginTime().compareTo(
                        getGuestLastLogoutTime()) > 0)) {
            setGuestLastLogoutTime(new Date());
        }
    }

    public boolean isStatusUp() {
        return isStatusUp(getStatus());
    }

    private boolean useSysPrep;

    public boolean useSysPrep() {
        return useSysPrep;
    }

    public void setUseSysPrep(boolean value) {
        useSysPrep = value;
    }

    public boolean isFirstRun() {
        return vmStatic.isFirstRun();
    }

    public static boolean isStatusUp(VMStatus st) {
        return (st == VMStatus.Up || st == VMStatus.PoweredDown || st == VMStatus.PoweringDown
                || st == VMStatus.PoweringUp || st == VMStatus.MigratingFrom || st == VMStatus.WaitForLaunch || st == VMStatus.RebootInProgress);

    }

    public static boolean isStatusUpOrPaused(VMStatus st) {
        return (isStatusUp(st) || st == VMStatus.Paused || st == VMStatus.SavingState || st == VMStatus.RestoringState);

    }

    public static boolean isStatusQualifyToMigrate(VMStatus st) {
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

    public double getActualDiskWithSnapshotsSize() {
        if (_actualDiskWithSnapthotsSize == 0 && getDiskMap() != null) {
            for (Disk disk : getDiskMap().values()) {
                if (DiskStorageType.IMAGE == disk.getDiskStorageType()) {
                    _actualDiskWithSnapthotsSize += ((DiskImage) disk).getActualDiskWithSnapshotsSize();
                }
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
        // Purposely empty
    }

    private double diskSize;

    public double getDiskSize() {
        if (diskSize == 0) {
            for (Disk disk : getDiskMap().values()) {
                if (DiskStorageType.IMAGE == disk.getDiskStorageType()) {
                    diskSize += ((DiskImage) disk).getsize() / Double.valueOf(1024 * 1024 * 1024);
                }
            }
        }
        return diskSize;
    }

    public void setDiskSize(double value) {
        diskSize = value;
    }

    public VmDynamic getDynamicData() {
        return vmDynamic;
    }

    public void setDynamicData(VmDynamic value) {
        vmDynamic = value;
    }

    public VmStatic getStaticData() {
        return vmStatic;
    }

    public void setStaticData(final VmStatic value) {
        // TODO this null protection is here for historical reasons, it may not be needed anymore
        if (value == null) {
            vmStatic = new VmStatic();
        } else {
            vmStatic = value;
        }
    }

    public VmStatistics getStatisticsData() {
        return vmStatistics;
    }

    public void setStatisticsData(VmStatistics value) {
        vmStatistics = value;
    }

    private int migreatingToPort;

    public int getMigreatingToPort() {
        return migreatingToPort;
    }

    public void setMigreatingToPort(int value) {
        migreatingToPort = value;
    }

    private int migreatingFromPort;

    public int getMigreatingFromPort() {
        return migreatingFromPort;
    }

    public void setMigreatingFromPort(int value) {
        migreatingFromPort = value;
    }

    private String runOnVdsName;

    public String getRunOnVdsName() {
        return runOnVdsName;
    }

    public void setRunOnVdsName(String value) {
        runOnVdsName = value;
    }

    public Map<Guid, Disk> getDiskMap() {
        return diskMap;
    }

    public void setDiskMap(Map<Guid, Disk> diskMap) {
        this.diskMap = diskMap;
    }

    public int getDiskMapCount() {
        return diskMap.size();
    }

    public int getMinAllocatedMem() {
        return vmStatic.getMinAllocatedMem();
    }

    public void setMinAllocatedMem(int value) {
        vmStatic.setMinAllocatedMem(value);
    }

    public String getCdPath() {
        return cdPath;
    }

    public void setCdPath(String value) {
        cdPath = value;
    }

    public String getFloppyPath() {
        return floppyPath;
    }

    public void setFloppyPath(String value) {
        floppyPath = value;
    }

    public boolean isRunAndPause() {
        return runAndPause;
    }

    public void setRunAndPause(boolean value) {
        runAndPause = value;
    }

    public boolean isWin2kHackEnable() {
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
        setStatus(vm.getstatus());
        setRunOnVds(vdsId);
        setRunOnVdsName(vdsName);
        setDisplay(vm.getdisplay());
        setDisplaySecurePort(vm.getdisplay_secure_port());
        setVmHost(vm.getvm_host());
        setVmIp(vm.getvm_ip());

        // if (!string.IsNullOrEmpty(vm.app_list))
        // {
        setAppList(vm.getapp_list());
        // }
        setGuestOs(vm.getguest_os());
        setDisplayType(vm.getdisplay_type());
        setDisplayIp(vm.getdisplay_ip());
        setKvmEnable(vm.getkvm_enable());
        setAcpiEnable(vm.getacpi_enable());
        setGuestCurrentUserName(vm.getguest_cur_user_name());
        setWin2kHackEnable(vm.getWin2kHackEnable());
        setUtcDiff(vm.getutc_diff());
        setExitStatus(vm.getExitStatus());
        setExitMessage(vm.getExitMessage());
        setClientIp(vm.getclient_ip());
        setVmPauseStatus(vm.getPauseStatus());

        // TODO: check what to do with update disk data
        // updateDisksData(vm);

        // updateSession(vm);
    }

    /**
     * update vm statistics data
     *
     * @param vm
     */
    public void updateRunTimeStatisticsData(VmStatistics vmStatistics, VM vm) {
        setElapsedTime(vmStatistics.getelapsed_time());
        setUsageNetworkPercent(vmStatistics.getusage_network_percent());
        vm.getStatisticsData().setDisksUsage(vmStatistics.getDisksUsage());
        // -------- cpu --------------
        setCpuSys(vmStatistics.getcpu_sys());
        setCpuUser(vmStatistics.getcpu_user());
        if ((getCpuSys() != null) && (getCpuUser() != null)) {
            Double percent = (getCpuSys() + getCpuUser()) / new Double(vm.getNumOfCpus());
            setUsageCpuPercent(percent.intValue());
            if (getUsageCpuPercent() != null && getUsageCpuPercent() > 100) {
                setUsageCpuPercent(100);
            }
        }
        // -------- memory --------------
        setUsageMemPercent(vmStatistics.getusage_mem_percent());
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

    public String getVmPoolName() {
        return vmPoolName;
    }

    public void setVmPoolName(String value) {
        vmPoolName = value;
    }

    public NGuid getVmPoolId() {
        return vmPoolId;
    }

    public void setVmPoolId(NGuid value) {
        vmPoolId = value;
    }

    /**
     * Version in .Net style: a.b.c.d when a: major version, b: minor version , c: major revision, d: minor revision
     * assumption: Qumranet Agent version stored in app_list by "Qumranet Agent" name. Qumranet Agent version, received
     * from vds in format : a.b.d there is no major revision received from vds - always 0
     */
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

    private Version spiceDriverVersion;

    public Version getSpiceDriverVersion() {
        return spiceDriverVersion;
    }

    public void setSpiceDriverVersion(Version value) {
        spiceDriverVersion = value;
    }

    public boolean getHasSpiceDriver() {
        return getSpiceDriverVersion() != null;
    }

    private String vdsGroupCpuFlagsData;

    public String getVdsGroupCpuFlagsData() {
        return vdsGroupCpuFlagsData;
    }

    public void setVdsGroupCpuFlagsData(String value) {
        vdsGroupCpuFlagsData = value;
    }

    private boolean transparentHugePages;

    public boolean isTransparentHugePages() {
        return this.transparentHugePages;
    }

    public void setTransparentHugePages(boolean value) {
        this.transparentHugePages = value;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    /**
     * Return true if vm has at least one Disk and one Interface
     */
    private Boolean configured;

    public boolean isConfigured() {
        if (configured == null) {
            configured =
                    (getInterfaces() != null && getDiskMap() != null && getInterfaces().size() > 0 && getDiskMap()
                            .size() > 0);
        }
        return configured;
    }

    public void setConfigured(boolean value) {
        configured = value;
    }

    public ArrayList<DiskImage> getDiskList() {
        return vmStatic.getDiskList();
    }

    public Map<Guid, VmDevice> getManagedVmDeviceMap() {
        return vmStatic.getManagedDeviceMap();
    }

    public void setManagedDeviceMap(Map<Guid, VmDevice> map) {
        vmStatic.setManagedDeviceMap(map);
    }

    public List<VmDevice> getVmUnamagedDeviceList() {
        return vmStatic.getUnmanagedDeviceList();
    }

    public void setUnmanagedDeviceList(List<VmDevice> list) {
        vmStatic.setUnmanagedDeviceList(list);
    }

    public List<Snapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<Snapshot> snapshots) {
        this.snapshots = snapshots;
    }

    public void setRunOnce(boolean value) {
        runOnce = value;
    }

    public boolean isRunOnce() {
        return runOnce;
    }

    public void setVmPayload(VmPayload vmPayload) {
        this.vmPayload = vmPayload;
    }

    public VmPayload getVmPayload() {
        return vmPayload;
    }

    public boolean isUseHostCpuFlags() {
        return vmStatic.isUseHostCpuFlags();
    }

    public void setUseHostCpuFlags(boolean useHostCpuFlags) {
        vmStatic.setUseHostCpuFlags(useHostCpuFlags);
    }

    public String getCpuPinning() {
        return vmStatic.getCpuPinning();
    }

    public void setCpuPinning(String cpuPinning) {
        vmStatic.setCpuPinning(cpuPinning);
    }

    public boolean isBalloonEnabled() {
        return balloonEnabled;
    }

    public void setBalloonEnabled(boolean isBallonEnabled) {
        balloonEnabled = isBallonEnabled;
    }

    public String getOvfVersion() {
        return vmStatic.getOvfVersion();
    }

    public void setOvfVersion(String ovfVersion) {
        vmStatic.setOvfVersion(ovfVersion);
    }

    @Override
    public String getName() {
        return getVmName();
    }
}
