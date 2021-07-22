package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.validation.Valid;

import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.locks.LockInfo;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VM implements Queryable, BusinessEntityWithStatus<Guid, VMStatus>, HasStoragePool, HasErrata, Nameable, Commented {
    private static final long serialVersionUID = -4078140531074414263L;

    @Valid
    private VmStatic vmStatic;
    private VmDynamic vmDynamic;
    private VmStatistics vmStatistics;
    @Valid
    private List<Snapshot> snapshots;
    private String clusterSpiceProxy;
    private String vmPoolSpiceProxy;
    private Map<VmDeviceId, Map<String, String>> runtimeDeviceCustomProperties;
    private ArchitectureType clusterArch;
    private boolean nextRunConfigurationExists;
    private Set<String> nextRunChangedFields;
    private boolean previewSnapshot;
    private LockInfo lockInfo;
    private int backgroundOperationProgress;
    private String backgroundOperationDescription;
    private Guid vmPoolId;
    private String vmPoolName;
    private String vmtName;
    private Version clusterCompatibilityVersion;
    private String clusterName;
    private String clusterCpuName;
    private String clusterCpuFlags;
    private String clusterCpuVerb;
    private String configuredCpuVerb;
    private Map<Guid, Disk> diskMap;
    // even this field has no setter, it can not have the final modifier because the GWT serialization mechanism
    // ignores the final fields
    private String cdPath;
    private String wgtCdPath;
    private String floppyPath;
    private double _actualDiskWithSnapthotsSize;
    private double diskSize;
    private Version privateGuestAgentVersion;
    private Version spiceDriverVersion;
    private boolean transparentHugePages;
    private boolean trustedService;
    private boolean hasIllegalImages;
    private BiosType clusterBiosType;
    @TransientField
    private boolean differentTimeZone;
    private Map<VmExternalDataKind, String> vmExternalData;

    @TransientField
    private boolean vnicsOutOfSync;

    public VM() {
        this(new VmStatic(), new VmDynamic(), new VmStatistics());
    }

    public VM(VmStatic vmStatic, VmDynamic vmDynamic, VmStatistics vmStatistics) {
        this.setStaticData(vmStatic);
        this.setDynamicData(vmDynamic);
        this.setStatisticsData(vmStatistics);
        this.setvNumaNodeList(new ArrayList<VmNumaNode>());
        this.setDiskMap(new HashMap<Guid, Disk>());
        this.setCdPath("");
        this.setWgtCdPath("");
        this.setFloppyPath("");
        this.setDiskSize(0);
        snapshots = new ArrayList<>();
        runtimeDeviceCustomProperties = new HashMap<>();
        storagePoolId = Guid.Empty;
    }

    public VM(VmStatic vmStatic,
            VmDynamic vmDynamic,
            VmStatistics vmStatistics,
            ArchitectureType clusterArch,
            Version clusterCompatibilityVersion,
            BiosType clusterBiosType) {
        this(vmStatic, vmDynamic, vmStatistics);
        this.clusterArch = clusterArch;
        this.clusterCompatibilityVersion = clusterCompatibilityVersion;
        this.clusterBiosType = clusterBiosType;
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

    public int getMaxMemorySizeMb() {
        return vmStatic.getMaxMemorySizeMb();
    }

    public void setMaxMemorySizeMb(int maxMemorySizeMb) {
        vmStatic.setMaxMemorySizeMb(maxMemorySizeMb);
    }

    public void setVmMemSizeMb(int value) {
        this.vmStatic.setMemSizeMb(value);
    }

    public int getNumOfIoThreads() {
        return this.vmStatic.getNumOfIoThreads();
    }

    public void setNumOfIoThreads(int numOfIoThreads) {
        this.vmStatic.setNumOfIoThreads(numOfIoThreads);
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

    public String getEmulatedMachine() {
        return this.vmDynamic.getEmulatedMachine();
    }

    public void setEmulatedMachine(String value) {
        this.vmDynamic.setEmulatedMachine(value);
    }

    public String getCustomEmulatedMachine() {
        return this.vmStatic.getCustomEmulatedMachine();
    }

    public void setCustomEmulatedMachine(String value) {
        this.vmStatic.setCustomEmulatedMachine(value);
    }

    public String getStopReason() {
        return this.vmDynamic.getStopReason();
    }

    public void setStopReason(String value) {
        this.vmDynamic.setStopReason(value);
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

    public int getNumOfCpus() {
        return this.vmStatic.getNumOfCpus();
    }

    public int getNumOfCpus(boolean countThreadsAsCPU) {
        return this.vmStatic.getNumOfCpus(countThreadsAsCPU);
    }

    public int getCurrentNumOfCpus() {
        return getCurrentNumOfCpus(true);
    }

    public int getCurrentNumOfCpus(boolean countThreadsAsCPU) {
        return this.getCurrentCoresPerSocket() * this.getCurrentSockets()
                * (countThreadsAsCPU ? this.getCurrentThreadsPerCore() : 1);
    }

    /**
     * This method is created for SOAP serialization of primitives that are readonly but sent by the client. The setter
     * implementation is empty and the field is not being changed.
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

    public int getThreadsPerCpu() {
        return this.vmStatic.getThreadsPerCpu();
    }

    public void setThreadsPerCpu(int value) {
        this.vmStatic.setThreadsPerCpu(value);
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

    public List<Guid> getDedicatedVmForVdsList() {
        return vmStatic.getDedicatedVmForVdsList();
    }

    @JsonIgnore
    public void setDedicatedVmForVdsList(List<Guid> value) {
        vmStatic.setDedicatedVmForVdsList(value);
    }

    public Guid getClusterId() {
        return this.vmStatic.getClusterId();
    }

    public void setClusterId(Guid value) {
        this.vmStatic.setClusterId(value);
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

    public Map<GraphicsType, GraphicsInfo> getGraphicsInfos() {
        return vmDynamic.getGraphicsInfos();
    }

    /*
     * DON'T use this setter. It's here only for serizalization.
     */
    public void setGraphicsInfos(Map<GraphicsType, GraphicsInfo> graphicsInfos) {
        vmDynamic.setGraphicsInfos(graphicsInfos);
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

    @Override
    public VMStatus getStatus() {
        return this.vmDynamic.getStatus();
    }

    @Override
    public void setStatus(VMStatus value) {
        this.vmDynamic.setStatus(value);
    }

    public String getIp() {
        return this.vmDynamic.getIp();
    }

    public void setIp(String value) {
        this.vmDynamic.setIp(value);
    }

    public String getFqdn() {
        return this.vmDynamic.getFqdn();
    }

    public void setFqdn(String fqdn) {
        this.vmDynamic.setFqdn(fqdn);
    }

    public String getVmHost() {
        String vmDomain = (getVmInit() != null) ? getVmInit().getDomain() : null;
        String vmHost = this.vmDynamic.getVmHost();
        if (!StringHelper.isNullOrEmpty(this.getIp())) {
            this.vmDynamic.setVmHost(getIp());
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

    public Date getLastStartTime() {
        return this.vmDynamic.getLastStartTime();
    }

    public void setLastStartTime(Date value) {
        this.vmDynamic.setLastStartTime(value);
    }

    public Date getBootTime() {
        return this.vmDynamic.getBootTime();
    }

    public void setBootTime(Date value) {
        this.vmDynamic.setBootTime(value);
    }

    public long getDowntime() {
        return this.vmDynamic.getDowntime();
    }

    public void setDowntime(long value) {
        this.vmDynamic.setDowntime(value);
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

    public Guid getConsoleUserId() {
        return this.vmDynamic.getConsoleUserId();
    }

    public void setConsoleUserId(Guid value) {
        this.vmDynamic.setConsoleUserId(value);
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

    public Boolean getAcpiEnable() {
        return this.vmDynamic.getAcpiEnable();
    }

    public void setAcpiEnable(Boolean value) {
        this.vmDynamic.setAcpiEnable(value);
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

    public int getGuestAgentNicsHash() {
        return vmDynamic.getGuestAgentNicsHash();
    }

    public void setGuestAgentNicsHash(int guestAgentNicsHash) {
        vmDynamic.setGuestAgentNicsHash(guestAgentNicsHash);
    }

    public GuestAgentStatus getOvirtGuestAgentStatus() {
        return vmDynamic.getOvirtGuestAgentStatus();
    }

    public void setOvirtGuestAgentStatus(GuestAgentStatus status) {
        vmDynamic.setOvirtGuestAgentStatus(status);
    }

    public GuestAgentStatus getQemuGuestAgentStatus() {
        return vmDynamic.getQemuGuestAgentStatus();
    }

    public void setQemuGuestAgentStatus(GuestAgentStatus status) {
        vmDynamic.setQemuGuestAgentStatus(status);
    }

    public int getGuestOsTimezoneOffset() {
        return vmDynamic.getGuestOsTimezoneOffset();
    }

    public void setGuestOsTimezoneOffset(int timezoneOffset) {
        vmDynamic.setGuestOsTimezoneOffset(timezoneOffset);
    }

    public String getGuestOsTimezoneName() {
        return vmDynamic.getGuestOsTimezoneName();
    }

    public void setGuestOsTimezoneName(String timezoneName) {
        vmDynamic.setGuestOsTimezoneName(timezoneName);
    }

    public String getGuestOsVersion() {
        return vmDynamic.getGuestOsVersion();
    }

    public void setGuestOsVersion(String guestOsVersion) {
        vmDynamic.setGuestOsVersion(guestOsVersion);
    }

    public String getGuestOsDistribution() {
        return vmDynamic.getGuestOsDistribution();
    }

    public void setGuestOsDistribution(String guestOsDistribution) {
        vmDynamic.setGuestOsDistribution(guestOsDistribution);
    }

    public String getGuestOsCodename() {
        return vmDynamic.getGuestOsCodename();
    }

    public void setGuestOsCodename(String guestOsCodename) {
        vmDynamic.setGuestOsCodename(guestOsCodename);
    }

    public ArchitectureType getGuestOsArch() {
        return vmDynamic.getGuestOsArch();
    }

    public void setGuestOsArch(ArchitectureType guestOsArch) {
        vmDynamic.setGuestOsArch(guestOsArch);
    }

    @JsonIgnore
    public void setGuestOsArch(Integer arch) {
        vmDynamic.setGuestOsArch(arch);
    }

    @JsonIgnore
    public void setGuestOsArch(String arch) {
        vmDynamic.setGuestOsArch(arch);
    }

    public OsType getGuestOsType() {
        return vmDynamic.getGuestOsType();
    }

    public void setGuestOsType(OsType guestOsType) {
        vmDynamic.setGuestOsType(guestOsType);
    }

    public String getGuestOsKernelVersion() {
        return vmDynamic.getGuestOsKernelVersion();
    }

    public void setGuestOsKernelVersion(String guestOsKernelVersion) {
        vmDynamic.setGuestOsKernelVersion(guestOsKernelVersion);
    }

    public Double getCpuUser() {
        return this.vmStatistics.getCpuUser();
    }

    public void setCpuUser(Double value) {
        this.vmStatistics.setCpuUser(value);
    }

    public Double getCpuSys() {
        return this.vmStatistics.getCpuSys();
    }

    public void setCpuSys(Double value) {
        this.vmStatistics.setCpuSys(value);
    }

    public Double getElapsedTime() {
        return this.vmStatistics.getElapsedTime();
    }

    public void setElapsedTime(Double value) {
        this.vmStatistics.setElapsedTime(value);
    }

    public Integer getUsageNetworkPercent() {
        return this.vmStatistics.getUsageNetworkPercent();
    }

    public void setUsageNetworkPercent(Integer value) {
        this.vmStatistics.setUsageNetworkPercent(value);
    }

    public Integer getUsageMemPercent() {
        return this.vmStatistics.getUsageMemPercent();
    }

    public void setUsageMemPercent(Integer value) {
        this.vmStatistics.setUsageMemPercent(value);
    }

    public List<Integer> getMemoryUsageHistory() {
        return this.vmStatistics.getMemoryUsageHistory();
    }

    public List<Integer> getCpuUsageHistory() {
        return this.vmStatistics.getCpuUsageHistory();
    }

    public List<Integer> getNetworkUsageHistory() {
        return this.vmStatistics.getNetworkUsageHistory();
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
        return this.vmStatistics.getUsageCpuPercent();
    }

    public void setUsageCpuPercent(Integer value) {
        this.vmStatistics.setUsageCpuPercent(value);
    }

    public Guid getVmtGuid() {
        return this.vmStatic.getVmtGuid();
    }

    public void setVmtGuid(Guid value) {
        this.vmStatic.setVmtGuid(value);
    }

    public String getVmtName() {
        return this.vmtName;
    }

    public void setVmtName(String value) {
        this.vmtName = value;
    }

    public Version getClusterCompatibilityVersion() {
        return this.clusterCompatibilityVersion;
    }

    public void setClusterCompatibilityVersion(Version value) {
        this.clusterCompatibilityVersion = value;
    }

    /**
     * Get custom compatibility version, if set for this VM or null otherwise.
     *
     * <b>Note:</b> In most cases {@link #getCompatibilityVersion()} must be used instead.
     * Use this method only if you're interested in the custom compatibility version
     * set for this particular VM.
     *
     * @return the custom compatibility version
     */
    public Version getCustomCompatibilityVersion() {
        return this.vmStatic.getCustomCompatibilityVersion();
    }

    public void setCustomCompatibilityVersion(Version value) {
        this.vmStatic.setCustomCompatibilityVersion(value);
    }

    /**
     * Get compatibility version for this VM.
     *
     * This method returns the custom compatibility version, if set for this VM or
     * cluster's compatibility version otherwise.
     *
     * @return the compatibility version
     */
    public Version getCompatibilityVersion() {
        return getCustomCompatibilityVersion() != null ? getCustomCompatibilityVersion() : getClusterCompatibilityVersion();
    }

    public String getClusterName() {
        return this.clusterName;
    }

    public void setClusterName(String value) {
        this.clusterName = value;
    }

    public String getClusterCpuFlags() {
        return clusterCpuFlags;
    }

    public void setClusterCpuFlags(String clusterCpuFlags) {
        this.clusterCpuFlags = clusterCpuFlags;
    }

    public String getClusterCpuVerb() {
        return clusterCpuVerb;
    }

    public void setClusterCpuVerb(String clusterCpuVerb) {
        this.clusterCpuVerb = clusterCpuVerb;
    }

    public String getConfiguredCpuVerb() {
        return configuredCpuVerb;
    }

    public void setConfiguredCpuVerb(String configuredCpuVerb) {
        this.configuredCpuVerb = configuredCpuVerb;
    }

    public String getClusterCpuName() {
        return this.clusterCpuName;
    }

    public void setClusterCpuName(String value) {
        this.clusterCpuName = value;
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

    public String getCustomCpuName() {
        return this.vmStatic.getCustomCpuName();
    }

    public void setCustomCpuName(String value) {
        this.vmStatic.setCustomCpuName(value);
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

    public boolean isFirstRun() {
        return vmStatic.isFirstRun();
    }

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
     */
    @Deprecated
    public void setActualDiskWithSnapshotsSize(double value) {
        // Purposely empty
    }

    public double getDiskSize() {
        if (diskSize == 0) {
            for (Disk disk : getDiskMap().values()) {
                if (DiskStorageType.IMAGE == disk.getDiskStorageType()) {
                    diskSize += disk.getSize() / Double.valueOf(1024 * 1024 * 1024);
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

    public String getWgtCdPath() {
        return wgtCdPath;
    }

    public void setWgtCdPath(String value) {
        wgtCdPath = value;
    }

    public String getFloppyPath() {
        return floppyPath;
    }

    public void setFloppyPath(String value) {
        floppyPath = value;
    }

    public String getCurrentCpuPinning() {
        return this.vmDynamic.getCurrentCpuPinning();
    }

    public void setCurrentCpuPinning(String cpuPinning) {
        this.vmDynamic.setCurrentCpuPinning(cpuPinning);
    }

    public int getCurrentSockets() {
        return this.vmDynamic.getCurrentSockets();
    }

    public void setCurrentSockets(int sockets) {
        this.vmDynamic.setCurrentSockets(sockets);
    }

    public int getCurrentCoresPerSocket() {
        return this.vmDynamic.getCurrentCoresPerSocket();
    }

    public void setCurrentCoresPerSocket(int cores) {
        this.vmDynamic.setCurrentCoresPerSocket(cores);
    }

    public int getCurrentThreadsPerCore() {
        return this.vmDynamic.getCurrentThreadsPerCore();
    }

    public void setCurrentThreadsPerCore(int threads) {
        this.vmDynamic.setCurrentThreadsPerCore(threads);
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

    /**
     * Check if two Vms are Equal. Current equality rule is: Two Vms are equal when them points to same object or have
     * same vm_guid property
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
        return Objects.hash(_actualDiskWithSnapthotsSize,
                cdPath,
                diskMap,
                diskSize,
                floppyPath,
                privateGuestAgentVersion,
                runOnVdsName,
                snapshots,
                spiceDriverVersion,
                storagePoolId,
                storagePoolName,
                transparentHugePages,
                clusterCompatibilityVersion,
                clusterCpuName,
                clusterCpuFlags,
                clusterCpuVerb,
                clusterName,
                vmDynamic,
                vmPoolId,
                vmPoolName,
                vmStatic,
                vmStatistics,
                vmtName,
                wgtCdPath);
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

    /**
     * assumption: Qumranet Agent version stored in app_list by "Qumranet Agent" name. Qumranet Agent version,
     * received from vds in format : a.b.d there is no major revision received from vds - always 0
     * @see Version
     */
    public Version getGuestAgentVersion() {
        return privateGuestAgentVersion;
    }

    public void setGuestAgentVersion(Version value) {
        privateGuestAgentVersion = value;
    }

    public boolean getHasAgent() {
        return getGuestAgentVersion() != null;
    }

    public Version getSpiceDriverVersion() {
        return spiceDriverVersion;
    }

    public void setSpiceDriverVersion(Version value) {
        spiceDriverVersion = value;
    }

    public boolean getHasSpiceDriver() {
        return getSpiceDriverVersion() != null;
    }

    public boolean isTransparentHugePages() {
        return this.transparentHugePages;
    }

    public void setTransparentHugePages(boolean value) {
        this.transparentHugePages = value;
    }

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

    public ArrayList<DiskImage> getDiskList() {
        return vmStatic.getDiskList();
    }

    public Map<Guid, VmDevice> getManagedVmDeviceMap() {
        return vmStatic.getManagedDeviceMap();
    }

    public void setManagedDeviceMap(Map<Guid, VmDevice> map) {
        vmStatic.setManagedDeviceMap(map);
    }

    public List<VmDevice> getUnmanagedDeviceList() {
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

    public boolean isVolatileRun() {
        return vmDynamic.isVolatileRun();
    }

    public void setVolatileRun(boolean useWarmReboot) {
        vmDynamic.setVolatileRun(useWarmReboot);
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

    public String getOvfVersion() {
        return vmStatic.getOvfVersion();
    }

    public void setOvfVersion(String ovfVersion) {
        vmStatic.setOvfVersion(ovfVersion);
    }

    public Version getClusterCompatibilityVersionOrigin() {
        return vmStatic.getClusterCompatibilityVersionOrigin();
    }

    public void setClusterCompatibilityVersionOrigin(Version value) {
        vmStatic.setClusterCompatibilityVersionOrigin(value);
    }

    @Override
    public String getName() {
        return this.vmStatic.getName();
    }

    @Override
    public String toString() {
        return "VM [" + getName() + "]";
    }

    public boolean isDown() {
        return getStatus() == VMStatus.Down;
    }

    public boolean isRunning() {
        return getStatus().isRunning();
    }

    public boolean isRunningOrPaused() {
        return getStatus().isRunningOrPaused();
    }

    public boolean isSuspended() {
        return getStatus().isSuspended();
    }

    public boolean isQualifyToMigrate() {
        return getStatus().isQualifyToMigrate();
    }

    public boolean isQualifiedForSnapshotMerge() {
        return getStatus().isQualifiedForSnapshotMerge();
    }

    public boolean isQualifiedForLiveSnapshotMerge() {
        return getStatus().isQualifiedForLiveSnapshotMerge();
    }

    public boolean isQualifiedForConsoleConnect() {
        return getStatus().isQualifiedForConsoleConnect();
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
        return vmStatic.isHostedEngine();
    }

    public boolean isManagedHostedEngine() {
        return vmStatic.isManagedHostedEngine();
    }

    public boolean isExternalVm() {
        return OriginType.EXTERNAL.equals(getOrigin());
    }

    public boolean isManagedVm() {
        return !(OriginType.HOSTED_ENGINE == getOrigin()) && !isExternalVm();
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

    public String getClusterSpiceProxy() {
        return clusterSpiceProxy;
    }

    public void setClusterSpiceProxy(String clusterSpiceProxy) {
        this.clusterSpiceProxy = clusterSpiceProxy;
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

    public void setNextRunChangedFields(Set<String> nextRunChangedFields) {
        this.nextRunChangedFields = nextRunChangedFields;
    }

    public Set<String> getNextRunChangedFields() {
        return nextRunChangedFields;
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

    public Guid getCpuProfileId() {
        return vmStatic.getCpuProfileId();
    }

    public void setCpuProfileId(Guid cpuProfileId) {
        vmStatic.setCpuProfileId(cpuProfileId);
    }

    public Boolean getAutoConverge() {
        return vmStatic.getAutoConverge();
    }

    public void setAutoConverge(Boolean autoConverge) {
        vmStatic.setAutoConverge(autoConverge);
    }

    public Boolean getMigrateCompressed() {
        return vmStatic.getMigrateCompressed();
    }

    public Guid getMigrationPolicyId() {
        return vmStatic.getMigrationPolicyId();
    }

    public void setMigrationPolicyId(Guid migrationPolicyId) {
        vmStatic.setMigrationPolicyId(migrationPolicyId);
    }

    public void setMigrateCompressed(Boolean migrateCompressed) {
        vmStatic.setMigrateCompressed(migrateCompressed);
    }

    public void setMigrateEncrypted(Boolean migrateEncrypted) {
        vmStatic.setMigrateEncrypted(migrateEncrypted);
    }

    public Boolean getMigrateEncrypted() {
        return vmStatic.getMigrateEncrypted();
    }

    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public void setLockInfo(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    public Long getGuestMemoryCached() {
        return vmStatistics.getGuestMemoryCached();
    }

    public void setGuestMemoryCached(Long guestMemoryCached) {
        vmStatistics.setGuestMemoryCached(guestMemoryCached);
    }

    public Long getGuestMemoryBuffered() {
        return vmStatistics.getGuestMemoryBuffered();
    }

    public void setGuestMemoryBuffered(Long guestMemoryBuffered) {
        vmStatistics.setGuestMemoryBuffered(guestMemoryBuffered);
    }

    public Long getGuestMemoryFree() {
        return vmStatistics.getGuestMemoryFree();
    }

    public void setGuestMemoryFree(Long guestMemoryFree) {
        vmStatistics.setGuestMemoryFree(guestMemoryFree);
    }

    public Long getGuestMemoryUnused() {
        return vmStatistics.getGuestMemoryUnused();
    }

    public void setGuestMemoryUnused(Long guestMemoryUnused) {
        vmStatistics.setGuestMemoryUnused(guestMemoryUnused);
    }

    public Guid getProviderId() {
        return vmStatic.getProviderId();
    }

    public void setProviderId(Guid providerId) {
        vmStatic.setProviderId(providerId);
    }
    public ConsoleDisconnectAction getConsoleDisconnectAction() {
        return vmStatic.getConsoleDisconnectAction();
    }

    public void setConsoleDisconnectAction(ConsoleDisconnectAction consoleDisconnectAction) {
        vmStatic.setConsoleDisconnectAction(consoleDisconnectAction);
    }

    public int getConsoleDisconnectActionDelay() {
        return vmStatic.getConsoleDisconnectActionDelay();
    }

    public void setConsoleDisconnectActionDelay(int consoleDisconnectActionDelay) {
        vmStatic.setConsoleDisconnectActionDelay(consoleDisconnectActionDelay);
    }

    public int getBackgroundOperationProgress() {
        return backgroundOperationProgress;
    }

    public void setBackgroundOperationProgress(int progress) {
        this.backgroundOperationProgress = progress;
    }

    public String getBackgroundOperationDescription() {
        return backgroundOperationDescription;
    }

    public void setBackgroundOperationDescription(String description) {
        this.backgroundOperationDescription = description;
    }

    public boolean isPreviewSnapshot() {
        return previewSnapshot;
    }

    public void setPreviewSnapshot(boolean previewSnapshot) {
        this.previewSnapshot = previewSnapshot;
    }

    public void setGuestContainers(List<GuestContainer> containers) {
        vmDynamic.setGuestContainers(containers);
    }

    public List<GuestContainer> getGuestContainers() {
        return vmDynamic.getGuestContainers();
    }

    public Guid getLeaseStorageDomainId() {
        return vmStatic.getLeaseStorageDomainId();
    }

    public void setLeaseStorageDomainId(Guid leaseStorageDomainId) {
        vmStatic.setLeaseStorageDomainId(leaseStorageDomainId);
    }

    public Map<String, String> getLeaseInfo() {
        return vmDynamic.getLeaseInfo();
    }

    public void setLeaseInfo(Map<String, String> leaseInfo) {
        vmDynamic.setLeaseInfo(leaseInfo);
    }

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

    public boolean hasIllegalImages() {
        return hasIllegalImages;
    }

    public void setHasIllegalImages(boolean hasIllegalImages) {
        this.hasIllegalImages = hasIllegalImages;
    }

    public VmResumeBehavior getResumeBehavior() {
        return vmStatic.getResumeBehavior();
    }

    public void setResumeBehavior(VmResumeBehavior resumeBehavior) {
        vmStatic.setResumeBehavior(resumeBehavior);
    }

    public boolean isMultiQueuesEnabled() {
        return vmStatic.isMultiQueuesEnabled();
    }

    public int getVirtioScsiMultiQueues() {
        return vmStatic.getVirtioScsiMultiQueues();
    }

    public void setMultiQueuesEnabled(boolean multiQueuesEnabled) {
        vmStatic.setMultiQueuesEnabled(multiQueuesEnabled);
    }

    public void setVirtioScsiMultiQueues(int virtioScsiMultiQueues) {
        vmStatic.setVirtioScsiMultiQueues(virtioScsiMultiQueues);
    }

    public String getRuntimeName() {
        return vmDynamic.getRuntimeName();
    }

    public void setRuntimeName(String runtimeName) {
        vmDynamic.setRuntimeName(runtimeName);
    }

    public boolean isUsingCpuPassthrough() {
        return isUseHostCpuFlags() || Objects.equals(getCustomCpuName(), "hostPassthrough");
    }

    public BiosType getClusterBiosType() {
        return clusterBiosType;
    }

    public void setClusterBiosType(BiosType clusterBiosType) {
        this.clusterBiosType = clusterBiosType;
    }

    public void setBiosType(BiosType type) {
        vmStatic.setBiosType(type);
    }

    public BiosType getBiosType() {
        return vmStatic.getBiosType();
    }

    public boolean getUseTscFrequency() {
        return vmStatic.getUseTscFrequency();
    }

    public void setUseTscFrequency(boolean value) {
        vmStatic.setUseTscFrequency(value);
    }

    public String getNamespace() {
        return vmStatic.getNamespace();
    }

    public void setNamespace(String namespace) {
        vmStatic.setNamespace(namespace);
    }

    @Override
    public boolean isManaged() {
        // TODO: think of a better way to distinguish that from #isManagedVm
        return vmStatic.isManaged();
    }

    public boolean isDifferentTimeZone() {
        return differentTimeZone;
    }

    public void setDifferentTimeZone(boolean differentTimeZone) {
        this.differentTimeZone = differentTimeZone;
    }

    public Guid getSmallIconId() {
        return vmStatic.getSmallIconId();
    }

    public void setSmallIconId(Guid smallIconId) {
        vmStatic.setSmallIconId(smallIconId);
    }

    public Guid getLargeIconId() {
        return vmStatic.getLargeIconId();
    }

    public void setLargeIconId(Guid largeIconId) {
        vmStatic.setLargeIconId(largeIconId);
    }

    public Map<VmExternalDataKind, String> getVmExternalData() {
        return vmExternalData;
    }

    public void setVmExternalData(Map<VmExternalDataKind, String> vmExternalData) {
        this.vmExternalData = vmExternalData;
    }

    public boolean isVnicsOutOfSync() {
        return vnicsOutOfSync;
    }

    public void setVnicsOutOfSync(boolean vnicsOutOfSync) {
        this.vnicsOutOfSync = vnicsOutOfSync;
    }

    public boolean isBalloonEnabled() {
        return vmStatic.isBalloonEnabled();
    }

    public void setBalloonEnabled(boolean balloonEnabled) {
        vmStatic.setBalloonEnabled(balloonEnabled);
    }

    public CpuPinningPolicy getCpuPinningPolicy() {
        return vmStatic.getCpuPinningPolicy();
    }

    public void setCpuPinningPolicy(CpuPinningPolicy cpuPinningPolicy) {
        vmStatic.setCpuPinningPolicy(cpuPinningPolicy);
    }
}
