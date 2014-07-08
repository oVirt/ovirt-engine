package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

public class VM extends IVdcQueryable implements Serializable, BusinessEntityWithStatus<Guid, VMStatus>, HasStoragePool<Guid>, Nameable, Commented, Reasoned {
    private static final long serialVersionUID = -4078140531074414263L;
    @Valid
    private VmStatic vmStatic;

    private VmDynamic vmDynamic;
    private VmStatistics vmStatistics;
    @EditableField
    private VmPayload vmPayload;
    @EditableField
    private boolean balloonEnabled;

    @Valid
    private List<Snapshot> snapshots;
    private boolean runOnce;

    private String vdsGroupSpiceProxy;

    private String vmPoolSpiceProxy;

    private InitializationType initializationType;

    private Map<VmDeviceId, Map<String, String>> runtimeDeviceCustomProperties;

    private ArchitectureType clusterArch;

    private boolean nextRunConfigurationExists;

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

    public Map<VmDeviceId, Map<String, String>> getRuntimeDeviceCustomProperties() {
        return runtimeDeviceCustomProperties;
    }

    public void setRuntimeDeviceCustomProperties(Map<VmDeviceId, Map<String, String>> runtimeDeviceCustomProperties) {
        this.runtimeDeviceCustomProperties = runtimeDeviceCustomProperties;
    }

    public VM() {
        this(new VmStatic(), new VmDynamic(), new VmStatistics());
    }

    public VM(VmStatic vmStatic, VmDynamic vmDynamic, VmStatistics vmStatistics) {
        this.setStaticData(vmStatic);
        this.setDynamicData(vmDynamic);
        this.setStatisticsData(vmStatistics);
        this.setImages(new ArrayList<DiskImage>());
        this.setInterfaces(new ArrayList<VmNetworkInterface>());
        this.setvNumaNodeList(new ArrayList<VmNumaNode>());
        this.setDiskMap(new HashMap<Guid, Disk>());
        this.setCdPath("");
        this.setFloppyPath("");
        this.setDiskSize(0);
        snapshots = new ArrayList<Snapshot>();
        initializationType = InitializationType.None;
        runtimeDeviceCustomProperties = new HashMap<VmDeviceId, Map<String, String>>();
        vmtCreationDate = new Date(0);
        storagePoolId = Guid.Empty;
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

    public void setName(String value) {
        this.vmStatic.setName(value);
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

    public int getOs() {
        return this.getVmOsId();
    }

    public ArchitectureType getClusterArch() {
        return this.clusterArch;
    }

    public void setClusterArch(ArchitectureType clusterArch) {
        this.clusterArch = clusterArch;
    }

    public int getVmOsId() {
        return this.vmStatic.getOsId();
    }

    public void setVmOs(int value) {
        this.vmStatic.setOsId(value);
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

    @Override
    public String getComment() {
        return this.vmStatic.getComment();
    }

    @Override
    public void setComment(String value) {
        this.vmStatic.setComment(value);
    }

    @Override
    public String getStopReason() {
        return this.vmDynamic.getStopReason();
    }

    @Override
    public void setStopReason(String value) {
        this.vmDynamic.setStopReason(value);
    }

    public int getNumOfMonitors() {
        return this.vmStatic.getNumOfMonitors();
    }

    public void setNumOfMonitors(int value) {
        this.vmStatic.setNumOfMonitors(value);
    }

    public boolean getSingleQxlPci() {
        return this.vmStatic.getSingleQxlPci();
    }

    public void setSingleQxlPci(boolean value) {
        this.vmStatic.setSingleQxlPci(value);
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

    public Guid getDedicatedVmForVds() {
        return vmStatic.getDedicatedVmForVds();
    }

    public void setDedicatedVmForVds(Guid value) {
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

    public void setSsoMethod(SsoMethod ssoMethod) {
        vmStatic.setSsoMethod(ssoMethod);
    }

    public SsoMethod getSsoMethod() {
        return vmStatic.getSsoMethod();
    }

    public String getDefaultVncKeyboardLayout() {
        return vmStatic.getVncKeyboardLayout();
    }

    public void setDefaultVncKeyboardLayout(String vncKeyboardLayout) {
        vmStatic.setVncKeyboardLayout(vncKeyboardLayout);
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

    public boolean isUseLatestVersion() {
        return vmStatic.isUseLatestVersion();
    }

    public void setUseLatestVersion(boolean useLatestVersion) {
        vmStatic.setUseLatestVersion(useLatestVersion);
    }

    public void setKernelParams(String value) {
        vmStatic.setKernelParams(value);
    }

    private Guid vmPoolId;
    private String vmPoolName;

    @Override
    public VMStatus getStatus() {
        return this.vmDynamic.getStatus();
    }

    @Override
    public void setStatus(VMStatus value) {
        this.vmDynamic.setStatus(value);
    }

    public String getVmIp() {
        return this.vmDynamic.getVmIp();
    }

    public void setVmIp(String value) {
        this.vmDynamic.setVmIp(value);
    }

    public String getVmFQDN() {
        return this.vmDynamic.getVmFQDN();
    }

    public void setVmFQDN(String fqdn) {
        this.vmDynamic.setVmFQDN(fqdn);
    }

    public String getVmHost() {
        String vmDomain = (getVmInit() != null) ? getVmInit().getDomain() : null;
        String vmHost = this.vmDynamic.getVmHost();
        if (!StringHelper.isNullOrEmpty(this.getVmIp())) {
            this.vmDynamic.setVmHost(getVmIp());
        } else {
            // If VM's host name isn't available - set as VM's name
            // If no IP address is available - assure that 'vm_host' is FQN by concatenating
            // vmHost and vmDomain.
            if (StringHelper.isNullOrEmpty(vmHost)) {
                vmHost = StringHelper.isNullOrEmpty(vmDomain) ? getName() : getName() + "." + vmDomain;
                this.vmDynamic.setVmHost(vmHost);
            } else if (!StringHelper.isNullOrEmpty(vmDomain) && !vmHost.endsWith(vmDomain)) {
                this.vmDynamic.setVmHost(vmHost + "." + vmDomain);
            }
        }

        return this.vmDynamic.getVmHost();
    }

    public void setVmHost(String value) {
        this.vmDynamic.setVmHost(value);
    }

    public Integer getVmPid() {
        return this.vmDynamic.getVmPid();
    }

    public void setVmPid(Integer value) {
        this.vmDynamic.setVmPid(value);
    }

    public Date getLastStartTime() {
        return this.vmDynamic.getLastStartTime();
    }

    public void setLastStartTime(Date value) {
        this.vmDynamic.setLastStartTime(value);
    }

    public Date getLastStopTime() {
        return this.vmDynamic.getLastStopTime();
    }

    public void setLastStopTime(Date value) {
        this.vmDynamic.setLastStopTime(value);
    }

    public String getConsoleCurentUserName() {
        return this.vmDynamic.getConsoleCurrentUserName();
    }

    public void setConsoleCurrentUserName(String value) {
        this.vmDynamic.setConsoleCurrentUserName(value);
    }

    public String getGuestCurentUserName() {
        return this.vmDynamic.getGuestCurrentUserName();
    }

    public void setGuestCurrentUserName(String value) {
        this.vmDynamic.setGuestCurrentUserName(value);
    }

    public Date getGuestLastLoginTime() {
        return this.vmDynamic.getGuestLastLoginTime();
    }

    public void setGuestLastLoginTime(Date value) {
        this.vmDynamic.setGuestLastLoginTime(value);
    }

    public Guid getConsoleUserId() {
        return this.vmDynamic.getConsoleUserId();
    }

    public void setConsoleUserId(Guid value) {
        this.vmDynamic.setConsoleUserId(value);
    }

    public Date getGuestLastLogoutTime() {
        return this.vmDynamic.getGuestLastLogoutTime();
    }

    public void setGuestLastLogoutTime(Date value) {
        this.vmDynamic.setGuestLastLogoutTime(value);
    }

    public String getGuestOs() {
        return this.vmDynamic.getGuestOs();
    }

    public void setGuestOs(String value) {
        this.vmDynamic.setGuestOs(value);
    }

    public Guid getRunOnVds() {
        return this.vmDynamic.getRunOnVds();
    }

    public void setRunOnVds(Guid value) {
        this.vmDynamic.setRunOnVds(value);
    }

    public Guid getMigratingToVds() {
        return this.vmDynamic.getMigratingToVds();
    }

    public void setMigratingToVds(Guid value) {
        this.vmDynamic.setMigratingToVds(value);
    }

    public String getAppList() {
        return this.vmDynamic.getAppList();
    }

    public void setAppList(String value) {
        this.vmDynamic.setAppList(value);
    }

    public Integer getDisplay() {
        return this.vmDynamic.getDisplay();
    }

    public void setDisplay(Integer value) {
        this.vmDynamic.setDisplay(value);
    }

    public Boolean getAcpiEnable() {
        return this.vmDynamic.getAcpiEnable();
    }

    public void setAcpiEnable(Boolean value) {
        this.vmDynamic.setAcpiEnable(value);
    }

    public String getDisplayIp() {
        return this.vmDynamic.getDisplayIp();
    }

    public void setDisplayIp(String value) {
        this.vmDynamic.setDisplayIp(value);
    }

    public DisplayType getDisplayType() {
        return this.vmDynamic.getDisplayType();
    }

    public void setDisplayType(DisplayType value) {
        this.vmDynamic.setDisplayType(value);
    }

    public Boolean getKvmEnable() {
        return this.vmDynamic.getKvmEnable();
    }

    public void setKvmEnable(Boolean value) {
        this.vmDynamic.setKvmEnable(value);
    }

    public SessionState getSession() {
        return this.vmDynamic.getSession();
    }

    public void setSession(SessionState value) {
        this.vmDynamic.setSession(value);
    }

    public BootSequence getBootSequence() {
        return this.vmDynamic.getBootSequence();
    }

    public void setBootSequence(BootSequence value) {
        this.vmDynamic.setBootSequence(value);
    }

    public Integer getDisplaySecurePort() {
        return this.vmDynamic.getDisplaySecurePort();
    }

    public void setDisplaySecurePort(Integer value) {
        this.vmDynamic.setDisplaySecurePort(value);
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

    public VmExitReason getExitReason() {
        return this.vmDynamic.getExitReason();
    }

    public void setExitReason(VmExitReason value) {
        this.vmDynamic.setExitReason(value);
    }

    /**
     * Tracking value of VM's UTC offset. Useful for long running VMs when there
     * can be significant drift over initial value computed from timeZone.
     * Note that this value is no longer being used when
     * starting VMs (The timeZone field is used to calculate that offset) and is kept
     * in sync with value reported by VDSM only for debugging purposes.
     * {@see VmInfoBuilderBase#buildVmTimeZone()}
     */
    public Integer getUtcDiff() {
        return this.vmDynamic.getUtcDiff();
    }

    public void setUtcDiff(Integer value) {
        this.vmDynamic.setUtcDiff(value);
    }

    public Guid getLastVdsRunOn() {
        return this.vmDynamic.getLastVdsRunOn();
    }

    public void setLastVdsRunOn(Guid value) {
        this.vmDynamic.setLastVdsRunOn(value);
    }

    public String getClientIp() {
        return this.vmDynamic.getClientIp();
    }

    public void setClientIp(String value) {
        this.vmDynamic.setClientIp(value);
    }

    public Integer getGuestRequestedMemory() {
        return this.vmDynamic.getGuestRequestedMemory();
    }

    public void setGuestRequestedMemory(Integer value) {
        this.vmDynamic.setGuestRequestedMemory(value);
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

    public List<Integer> getMemoryUsageHistory() {
        return this.vmStatistics.getMemoryUsageHistory();
    }

    public void addMemoryUsageHistory(Integer memoryUsageHistory, int limit) {
        this.vmStatistics.addMemoryUsageHistory(memoryUsageHistory, limit);
    }

    public List<Integer> getCpuUsageHistory() {
        return this.vmStatistics.getCpuUsageHistory();
    }

    public void addCpuUsageHistory(Integer cpuUsageHistory, int limit) {
        this.vmStatistics.addCpuUsageHistory(cpuUsageHistory, limit);
    }

    public List<Integer> getNetworkUsageHistory() {
        return this.vmStatistics.getNetworkUsageHistory();
    }

    public void addNetworkUsageHistory(Integer networkUsageHistory, int limit) {
        this.vmStatistics.addNetworkUsageHistory(networkUsageHistory, limit);
    }

    public Integer getMigrationProgressPercent() {
        return this.vmStatistics.getMigrationProgressPercent();
    }

    public void setMigrationProgressPercent(Integer value) {
        this.vmStatistics.setMigrationProgressPercent(value);
    }

    public void setMemoryUsageHistory(List<Integer> memoryUsageHistory) {
        this.vmStatistics.setMemoryUsageHistory(memoryUsageHistory);
    }

    public void setCpuUsageHistory(List<Integer> cpuUsageHistory) {
        this.vmStatistics.setCpuUsageHistory(cpuUsageHistory);
    }

    public void setNetworkUsageHistory(List<Integer> networkUsageHistory) {
        this.vmStatistics.setNetworkUsageHistory(networkUsageHistory);
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

    private int vmtOsId;

    public int getVmtOsId() {
        return this.vmtOsId;
    }

    public void setVmtOsId(int value) {
        this.vmtOsId = value;
    }

    private Date vmtCreationDate;

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
        this.vdsGroupCompatibilityVersion = value;
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

    public int getCpuShares() {
        return this.vmStatic.getCpuShares();
    }

    public void setCpuShares(int value) {
        this.vmStatic.setCpuShares(value);
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
        return this.vmDynamic.getHibernationVolHandle();
    }

    public void setHibernationVolHandle(String value) {
        this.vmDynamic.setHibernationVolHandle(value);
    }

    public String getCpuName() {
        return this.vmDynamic.getCpuName();
    }

    public void setCpuName(String value) {
        this.vmDynamic.setCpuName(value);
    }

    public String getCurrentCd() {
        return this.vmDynamic.getCurrentCd();
    }

    public void setCurrentCd(String value) {
        this.vmDynamic.setCurrentCd(value);
    }

    public void setExportDate(Date value) {
        this.vmStatic.setExportDate(value);
    }

    public Date getExportDate() {
        return this.vmStatic.getExportDate();
    }

    private Guid storagePoolId;

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

    private Map<Guid, Disk> diskMap;

    // even this field has no setter, it can not have the final modifier because the GWT serialization mechanism
    // ignores the final fields
    private String cdPath;
    private String floppyPath;

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

    public InitializationType getInitializationType() {
        return initializationType;
    }

    public void setInitializationType(InitializationType value) {
        initializationType = value;
    }

    public boolean isFirstRun() {
        return vmStatic.isFirstRun();
    }

    public boolean isSysprepUsed() {
        return getInitializationType() == InitializationType.Sysprep
                && SimpleDependecyInjector.getInstance().get(OsRepository.class).isWindows(getVmOsId())
                && (getFloppyPath() == null || "".equals(getFloppyPath()));
    }

    public boolean isCloudInitUsed() {
        return getInitializationType() == InitializationType.CloudInit
                && !SimpleDependecyInjector.getInstance().get(OsRepository.class).isWindows(getVmOsId());
    }

    private double _actualDiskWithSnapthotsSize;

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
                    diskSize += ((DiskImage) disk).getSize() / Double.valueOf(1024 * 1024 * 1024);
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
        vmStatic = value == null ? new VmStatic() : value;
    }

    public VmStatistics getStatisticsData() {
        return vmStatistics;
    }

    public void setStatisticsData(VmStatistics value) {
        vmStatistics = value;
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

    public Boolean isRunAndPause() {
        return vmStatic.isRunAndPause();
    }

    public void setRunAndPause(Boolean value) {
        vmStatic.setRunAndPause(value);
    }

    public Guid getCreatedByUserId() {
        return vmStatic.getCreatedByUserId();
    }

    public void setCreatedByUserId(Guid value) {
        vmStatic.setCreatedByUserId(value);
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
        setStatus(vm.getStatus());
        setRunOnVds(vdsId);
        setRunOnVdsName(vdsName);
        setDisplay(vm.getDisplay());
        setDisplaySecurePort(vm.getDisplaySecurePort());
        setVmHost(vm.getVmHost());
        setVmIp(vm.getVmIp());
        setVmFQDN(vm.getVmFQDN());
        // update only if vdsm actually provides some value, otherwise engine has more information
        if (vm.getCurrentCd() != null) {
            setCurrentCd(vm.getCurrentCd());
        }

        // if (!string.IsNullOrEmpty(vm.app_list))
        // {
        setAppList(vm.getAppList());
        // }
        setGuestOs(vm.getGuestOs());
        setDisplayType(vm.getDisplayType());
        getDynamicData().setVncKeyboardLayout(vm.getVncKeyboardLayout());
        setDisplayIp(vm.getDisplayIp());
        setKvmEnable(vm.getKvmEnable());
        setAcpiEnable(vm.getAcpiEnable());
        setGuestCurrentUserName(vm.getGuestCurrentUserName());
        setWin2kHackEnable(vm.getWin2kHackEnable());
        setUtcDiff(vm.getUtcDiff());
        setExitStatus(vm.getExitStatus());
        setExitMessage(vm.getExitMessage());
        setExitReason(vm.getExitReason());
        setClientIp(vm.getClientIp());
        setVmPauseStatus(vm.getPauseStatus());
        setLastWatchdogEvent(vm.getLastWatchdogEvent());
        setGuestCpuCount(vm.getGuestCpuCount());
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
        Integer usageHistoryLimit = Config.getValue(ConfigValues.UsageHistoryLimit);

        setElapsedTime(vmStatistics.getelapsed_time());
        setUsageNetworkPercent(vmStatistics.getusage_network_percent());
        addNetworkUsageHistory(getUsageNetworkPercent(), usageHistoryLimit);

        vm.getStatisticsData().setDisksUsage(vmStatistics.getDisksUsage());

        // -------- cpu --------------
        setCpuSys(vmStatistics.getcpu_sys());
        setCpuUser(vmStatistics.getcpu_user());
        if ((getCpuSys() != null) && (getCpuUser() != null)) {
            Double percent = (getCpuSys() + getCpuUser()) / vm.getNumOfCpus();
            setUsageCpuPercent(percent.intValue());
            if (getUsageCpuPercent() != null && getUsageCpuPercent() > 100) {
                setUsageCpuPercent(100);
            }
        }
        addCpuUsageHistory(getUsageCpuPercent(), usageHistoryLimit);

        // -------- memory --------------
        setUsageMemPercent(vmStatistics.getusage_mem_percent());
        addMemoryUsageHistory(getUsageMemPercent(), usageHistoryLimit);

        // -------- migration --------------
        setMigrationProgressPercent(vmStatistics.getMigrationProgressPercent());
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
        final int prime = 31;
        int result = 1;
        long temp;
        temp = (long)_actualDiskWithSnapthotsSize;
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (balloonEnabled ? 1231 : 1237);
        result = prime * result + ((cdPath == null) ? 0 : cdPath.hashCode());
        result = prime * result + ((configured == null) ? 0 : configured.hashCode());
        result = prime * result + ((diskMap == null) ? 0 : diskMap.hashCode());
        temp = (long)diskSize;
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((floppyPath == null) ? 0 : floppyPath.hashCode());
        result = prime * result + ((initializationType == null) ? 0 : initializationType.hashCode());
        result = prime * result + ((privateGuestAgentVersion == null) ? 0 : privateGuestAgentVersion.hashCode());
        result = prime * result + ((runOnVdsName == null) ? 0 : runOnVdsName.hashCode());
        result = prime * result + (runOnce ? 1231 : 1237);
        result = prime * result + ((snapshots == null) ? 0 : snapshots.hashCode());
        result = prime * result + ((spiceDriverVersion == null) ? 0 : spiceDriverVersion.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + ((storagePoolName == null) ? 0 : storagePoolName.hashCode());
        result = prime * result + (transparentHugePages ? 1231 : 1237);
        result =
                prime * result + ((vdsGroupCompatibilityVersion == null) ? 0 : vdsGroupCompatibilityVersion.hashCode());
        result = prime * result + ((vdsGroupCpuFlagsData == null) ? 0 : vdsGroupCpuFlagsData.hashCode());
        result = prime * result + ((vdsGroupCpuName == null) ? 0 : vdsGroupCpuName.hashCode());
        result = prime * result + ((vdsGroupDescription == null) ? 0 : vdsGroupDescription.hashCode());
        result = prime * result + ((vdsGroupName == null) ? 0 : vdsGroupName.hashCode());
        result = prime * result + ((vmDynamic == null) ? 0 : vmDynamic.hashCode());
        result = prime * result + ((vmPayload == null) ? 0 : vmPayload.hashCode());
        result = prime * result + ((vmPoolId == null) ? 0 : vmPoolId.hashCode());
        result = prime * result + ((vmPoolName == null) ? 0 : vmPoolName.hashCode());
        result = prime * result + ((vmStatic == null) ? 0 : vmStatic.hashCode());
        result = prime * result + ((vmStatistics == null) ? 0 : vmStatistics.hashCode());
        result = prime * result + vmtCpuPerSocket;
        result = prime * result + ((vmtCreationDate == null) ? 0 : vmtCreationDate.hashCode());
        result = prime * result + ((vmtDescription == null) ? 0 : vmtDescription.hashCode());
        result = prime * result + vmtMemSizeMb;
        result = prime * result + ((vmtName == null) ? 0 : vmtName.hashCode());
        result = prime * result + vmtNumOfCpus;
        result = prime * result + vmtNumOfSockets;
        result = prime * result + vmtOsId;
        result = prime * result + ((vmtTimeZone == null) ? 0 : vmtTimeZone.hashCode());
        result = prime * result + vmtchildCount;
        return result;
    }

    public String getVmPoolName() {
        return vmPoolName;
    }

    public void setVmPoolName(String value) {
        vmPoolName = value;
    }

    public Guid getVmPoolId() {
        return vmPoolId;
    }

    public void setVmPoolId(Guid value) {
        vmPoolId = value;
    }

    private Version privateGuestAgentVersion;

    /**
     * assumption: Qumranet Agent version stored in app_list by "Qumranet Agent" name. Qumranet Agent version,
     * received from vds in format : a.b.d there is no major revision received from vds - always 0
     * @see {@link Version}
     */
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

    private boolean trustedService;

    public void setTrustedService(boolean trustedService) {
        this.trustedService = trustedService;
    }

    public boolean isTrustedService() {
        return trustedService;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    private Boolean configured;

    /**
     * @return true if vm has at least one Disk and one Interface
     */
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
        vmDynamic.setRunOnce(value);
    }

    public boolean isRunOnce() {
        return vmDynamic.isRunOnce();
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
        return this.vmStatic.getName();
    }

    @Override
    public String toString() {
        return "VM [" + getName() + "]";
    }

    /**
     * Returns the required size for saving all the memory used by this VM.
     * it is useful for determining the size to be allocated in the storage when hibernating
     * VM or taking a snapshot with memory.
     *
     * @return - Memory size for allocation in bytes.
     */
    @JsonIgnore
    public long getTotalMemorySizeInBytes() {
        return (long) (getVmMemSizeMb() + 200 + (64 * getNumOfMonitors())) * 1024 * 1024;
    }

    ///////////////////////////////////////////////
    /// Utility methods that check the VM state ///
    ///////////////////////////////////////////////

    public boolean isDown() {
        return getStatus() == VMStatus.Down;
    }

    public boolean isRunning() {
        return getStatus().isRunning();
    }

    public boolean isRunningOrPaused() {
        return getStatus().isRunningOrPaused();
    }

    public boolean isQualifyToMigrate() {
        return getStatus().isQualifyToMigrate();
    }

    public boolean isQualifiedForSnapshotMerge() {
        return getStatus().isQualifiedForSnapshotMerge();
    }

    public boolean isRunningAndQualifyForDisksMigration() {
        return getStatus().isUpOrPaused() && getRunOnVds() != null && !getRunOnVds().equals(Guid.Empty);
    }

    public boolean isNotRunning() {
        return getStatus().isNotRunning();
    }

    public boolean isStartingOrUp() {
        return getStatus().isStartingOrUp();
    }

    public Boolean getTunnelMigration() {
        return vmStatic.getTunnelMigration();
    }

    public void setTunnelMigration(Boolean value) {
        vmStatic.setTunnelMigration(value);
    }

    public Long getLastWatchdogEvent() {
        return vmDynamic.getLastWatchdogEvent();
    }

    public void setLastWatchdogEvent(Long lastWatchdogEvent) {
        vmDynamic.setLastWatchdogEvent(lastWatchdogEvent);
    }

    public boolean isHostedEngine() {
        return OriginType.HOSTED_ENGINE.equals(getOrigin());
    }

    public boolean isExternalVm() {
        return OriginType.EXTERNAL.equals(getOrigin());
    }

    public boolean isManagedVm() {
        return !isHostedEngine() && !isExternalVm();
    }

    public Guid getInstanceTypeId() {
        return vmStatic.getInstanceTypeId();
    }

    public void setInstanceTypeId(Guid instanceTypeId) {
        vmStatic.setInstanceTypeId(instanceTypeId);
    }

    public Guid getImageTypeId() {
        return vmStatic.getImageTypeId();
    }

    public void setImageTypeId(Guid ImageTypeId) {
        vmStatic.setImageTypeId(ImageTypeId);
    }

    public String getOriginalTemplateName() {
        return vmStatic.getOriginalTemplateName();
    }

    public void setOriginalTemplateName(String originalTemplateName) {
        vmStatic.setOriginalTemplateName(originalTemplateName);
    }

    public Guid getOriginalTemplateGuid() {
        return vmStatic.getOriginalTemplateGuid();
    }

    public void setOriginalTemplateGuid(Guid originalTemplateGuid) {
        vmStatic.setOriginalTemplateGuid(originalTemplateGuid);
    }

    public String getVmPoolSpiceProxy() {
        return vmPoolSpiceProxy;
    }

    public void setVmPoolSpiceProxy(String vmPoolSpiceProxy) {
        this.vmPoolSpiceProxy = vmPoolSpiceProxy;
    }

    public String getVdsGroupSpiceProxy() {
        return vdsGroupSpiceProxy;
    }

    public void setVdsGroupSpiceProxy(String vdsGroupSpiceProxy) {
        this.vdsGroupSpiceProxy = vdsGroupSpiceProxy;
    }

    public void clearDisks() {
        getDiskList().clear();
        getDiskMap().clear();
    }

    public void setMigrationDowntime(Integer migrationDowntime) {
        vmStatic.setMigrationDowntime(migrationDowntime);
    }

    public Integer getMigrationDowntime() {
        return vmStatic.getMigrationDowntime();
    }

    public VmInit getVmInit() {
        return this.vmStatic.getVmInit();
    }

    public void setVmInit(VmInit vmInit) {
        this.vmStatic.setVmInit(vmInit);
    }

    public SerialNumberPolicy getSerialNumberPolicy() {
        return vmStatic.getSerialNumberPolicy();
    }

    public void setSerialNumberPolicy(SerialNumberPolicy serialNumberPolicy) {
        vmStatic.setSerialNumberPolicy(serialNumberPolicy);
    }

    public String getCustomSerialNumber() {
        return vmStatic.getCustomSerialNumber();
    }

    public void setCustomSerialNumber(String customSerialNumber) {
        vmStatic.setCustomSerialNumber(customSerialNumber);
    }

    public boolean isBootMenuEnabled() {
        return vmStatic.isBootMenuEnabled();
    }

    public void setBootMenuEnabled(boolean enabled) {
        vmStatic.setBootMenuEnabled(enabled);
    }

    public void setGuestCpuCount(int guestCpuCount) {
        getDynamicData().setGuestCpuCount(guestCpuCount);
    }

    public int getGuestCpuCount() {
        return getDynamicData().getGuestCpuCount();
    }

    public void setNextRunConfigurationExists(boolean nextRunConfigurationExists) {
        this.nextRunConfigurationExists = nextRunConfigurationExists;
    }

    public boolean isNextRunConfigurationExists() {
        return nextRunConfigurationExists;
    }

    public NumaTuneMode getNumaTuneMode() {
        return vmStatic.getNumaTuneMode();
    }

    public void setNumaTuneMode(NumaTuneMode numaTuneMode) {
        vmStatic.setNumaTuneMode(numaTuneMode);
    }

    public void setvNumaNodeList(List<VmNumaNode> vNumaNodeList) {
        vmStatic.setvNumaNodeList(vNumaNodeList);
    }

    public List<VmNumaNode> getvNumaNodeList() {
        return vmStatic.getvNumaNodeList();
    }

    public boolean isSpiceFileTransferEnabled() {
        return vmStatic.isSpiceFileTransferEnabled();
    }

    public void setSpiceFileTransferEnabled(boolean enabled) {
        vmStatic.setSpiceFileTransferEnabled(enabled);
    }

    public boolean isSpiceCopyPasteEnabled() {
        return vmStatic.isSpiceCopyPasteEnabled();
    }

    public void setSpiceCopyPasteEnabled(boolean enabled) {
        vmStatic.setSpiceCopyPasteEnabled(enabled);
    }
}
