package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;

public class VmDynamic implements BusinessEntityWithStatus<Guid, VMStatus>, Comparable<VmDynamic> {
    private static final long serialVersionUID = 7789482445091432555L;

    private Guid id;
    private VMStatus status;
    private String vmIp;
    private String vmFQDN;
    @UnchangeableByVdsm
    private String vmHost;
    private Integer vmPid;
    @UnchangeableByVdsm
    private Date lastStartTime;
    @UnchangeableByVdsm
    private Date lastStopTime;
    private String guestCurUserName;
    /** Last connected user name */
    @UnchangeableByVdsm
    private String consoleCurrentUserName;
    /** Last connected user id */
    @UnchangeableByVdsm
    private Guid consoleUserId;
    private String guestOs;
    @UnchangeableByVdsm
    private Guid migratingToVds;
    @UnchangeableByVdsm
    private Guid runOnVds;
    private String appList;
    private Boolean acpiEnabled;
    private SessionState session;
    private String vncKeyboardLayout;
    private Boolean kvmEnable;
    private Integer utcDiff;
    @UnchangeableByVdsm
    private Guid lastVdsRunOn;
    private String clientIp;
    private Integer guestRequestedMemory;
    @UnchangeableByVdsm
    private BootSequence bootSequence;
    private VmExitStatus exitStatus;
    private VmPauseStatus pauseStatus;
    private String hash;
    private int guestAgentNicsHash;
    @UnchangeableByVdsm
    private String exitMessage;
    @UnchangeableByVdsm
    private ArrayList<DiskImageDynamic> disks;
    private boolean win2kHackEnabled;
    private Long lastWatchdogEvent;
    private String lastWatchdogAction;
    @UnchangeableByVdsm
    private boolean runOnce;
    @UnchangeableByVdsm
    private String cpuName;
    @UnchangeableByVdsm
    private GuestAgentStatus guestAgentStatus;
    @UnchangeableByVdsm
    private String emulatedMachine;
    private String currentCd;
    @UnchangeableByVdsm
    private String stopReason;
    private VmExitReason exitReason;
    private int guestCpuCount;
    private Map<GraphicsType, GraphicsInfo> graphicsInfos;
    private Long guestMemoryCached;
    private Long guestMemoryBuffered;
    private Long guestMemoryFree;
    private String guestOsVersion;
    private String guestOsDistribution;
    private String guestOsCodename;
    private ArchitectureType guestOsArch;
    private OsType guestOsType;
    private String guestOsKernelVersion;
    private String guestOsTimezoneName;
    private int guestOsTimezoneOffset;
    private List<GuestContainer> guestContainers;

    public static final String APPLICATIONS_LIST_FIELD_NAME = "appList";
    public static final String STATUS_FIELD_NAME = "status";

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                acpiEnabled,
                appList,
                bootSequence,
                clientIp,
                vncKeyboardLayout,
                consoleCurrentUserName,
                guestCurUserName,
                consoleUserId,
                guestOs,
                guestRequestedMemory,
                kvmEnable,
                lastVdsRunOn,
                disks,
                exitMessage,
                exitStatus,
                win2kHackEnabled,
                migratingToVds,
                pauseStatus,
                runOnVds,
                session,
                status,
                utcDiff,
                vmHost,
                vmIp,
                vmFQDN,
                lastStartTime,
                lastStopTime,
                vmPid,
                lastWatchdogEvent,
                lastWatchdogAction,
                runOnce,
                cpuName,
                guestAgentStatus,
                currentCd,
                stopReason,
                exitReason,
                emulatedMachine,
                graphicsInfos,
                guestMemoryFree,
                guestMemoryBuffered,
                guestMemoryCached,
                guestOsTimezoneName,
                guestOsTimezoneOffset,
                guestOsArch,
                guestOsCodename,
                guestOsDistribution,
                guestOsKernelVersion,
                guestOsVersion,
                guestOsType,
                guestContainers
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmDynamic)) {
            return false;
        }
        VmDynamic other = (VmDynamic) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(acpiEnabled, other.acpiEnabled)
                && Objects.equals(appList, other.appList)
                && bootSequence == other.bootSequence
                && Objects.equals(clientIp, other.clientIp)
                && Objects.equals(vncKeyboardLayout, other.vncKeyboardLayout)
                && Objects.equals(consoleCurrentUserName, other.consoleCurrentUserName)
                && Objects.equals(guestCurUserName, other.guestCurUserName)
                && Objects.equals(consoleUserId, other.consoleUserId)
                && Objects.equals(guestOs, other.guestOs)
                && Objects.equals(guestRequestedMemory, other.guestRequestedMemory)
                && Objects.equals(kvmEnable, other.kvmEnable)
                && Objects.equals(lastVdsRunOn, other.lastVdsRunOn)
                && Objects.equals(disks, other.disks)
                && Objects.equals(exitMessage, other.exitMessage)
                && exitStatus == other.exitStatus
                && win2kHackEnabled == other.win2kHackEnabled
                && Objects.equals(migratingToVds, other.migratingToVds)
                && pauseStatus == other.pauseStatus
                && Objects.equals(runOnVds, other.runOnVds)
                && session == other.session
                && status == other.status
                && Objects.equals(utcDiff, other.utcDiff)
                && Objects.equals(vmHost, other.vmHost)
                && Objects.equals(vmIp, other.vmIp)
                && Objects.equals(vmFQDN, other.vmFQDN)
                && Objects.equals(lastStartTime, other.lastStartTime)
                && Objects.equals(lastStopTime, other.lastStopTime)
                && Objects.equals(vmPid, other.vmPid)
                && Objects.equals(lastWatchdogEvent, other.lastWatchdogEvent)
                && Objects.equals(lastWatchdogAction, other.lastWatchdogAction)
                && runOnce == other.runOnce
                && Objects.equals(cpuName, other.cpuName)
                && Objects.equals(guestAgentStatus, other.guestAgentStatus)
                && Objects.equals(currentCd, other.currentCd)
                && Objects.equals(stopReason, other.stopReason)
                && exitReason == other.exitReason
                && Objects.equals(emulatedMachine, other.emulatedMachine)
                && Objects.equals(graphicsInfos, other.getGraphicsInfos())
                && Objects.equals(guestMemoryBuffered, other.guestMemoryBuffered)
                && Objects.equals(guestMemoryCached, other.guestMemoryCached)
                && Objects.equals(guestMemoryFree, other.guestMemoryFree)
                && Objects.equals(guestOsTimezoneName, other.guestOsTimezoneName)
                && guestOsTimezoneOffset == other.guestOsTimezoneOffset
                && Objects.equals(guestOsVersion, other.guestOsVersion)
                && Objects.equals(guestOsDistribution, other.guestOsDistribution)
                && Objects.equals(guestOsCodename, other.guestOsCodename)
                && Objects.equals(guestOsKernelVersion, other.guestOsKernelVersion)
                && Objects.equals(guestOsArch, other.guestOsArch)
                && Objects.equals(guestOsType, other.guestOsType)
                && Objects.equals(guestContainers, other.guestContainers);
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(String value) {
        exitMessage = value;
    }

    public VmExitStatus getExitStatus() {
        return this.exitStatus;
    }

    public void setExitStatus(VmExitStatus value) {
        exitStatus = value;
    }

    public ArrayList<DiskImageDynamic> getDisks() {
        return disks;
    }

    public void setDisks(ArrayList<DiskImageDynamic> value) {
        disks = value;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getGuestAgentNicsHash() {
        return guestAgentNicsHash;
    }

    public void setGuestAgentNicsHash(int guestAgentNicsHash) {
        this.guestAgentNicsHash = guestAgentNicsHash;
    }

    public boolean getWin2kHackEnable() {
        return win2kHackEnabled;
    }

    public void setWin2kHackEnable(boolean value) {
        win2kHackEnabled = value;
    }

    public VmDynamic() {
        id = Guid.Empty;
        status = VMStatus.Down;
        pauseStatus = VmPauseStatus.NONE;
        exitStatus = VmExitStatus.Normal;
        win2kHackEnabled = false;
        acpiEnabled = true;
        kvmEnable = true;
        session = SessionState.Unknown;
        bootSequence = BootSequence.C;
        exitReason = VmExitReason.Unknown;
        graphicsInfos = new HashMap<>();
        guestAgentStatus = GuestAgentStatus.DoesntExist;
        guestOsTimezoneName = "";
        guestOsTimezoneOffset = 0;
        guestOsVersion = "";
        guestOsDistribution = "";
        guestOsCodename = "";
        guestOsKernelVersion = "";
        guestOsArch = ArchitectureType.undefined;
        guestOsType = OsType.Other;
        disks = new ArrayList<>();
        guestContainers = new ArrayList<>();
    }

    public VmDynamic(VmDynamic template) {
        id = template.getId();
        status = template.getStatus();
        vmIp = template.getVmIp();
        vmFQDN = template.getVmFQDN();
        vmHost = template.getVmHost();
        vmPid = template.getVmPid();
        lastStartTime = template.getLastStartTime();
        lastStopTime = template.getLastStopTime();
        guestCurUserName = template.getGuestCurrentUserName();
        consoleCurrentUserName = template.getConsoleCurrentUserName();
        consoleUserId = template.getConsoleUserId();
        guestOs = template.getGuestOs();
        migratingToVds = template.getMigratingToVds();
        runOnVds = template.getRunOnVds();
        appList = template.getAppList();
        acpiEnabled = template.getAcpiEnable();
        session = template.getSession();
        vncKeyboardLayout = template.getVncKeyboardLayout();
        kvmEnable = template.getKvmEnable();
        utcDiff = template.getUtcDiff();
        lastVdsRunOn = template.getLastVdsRunOn();
        clientIp = template.getClientIp();
        guestRequestedMemory = template.getGuestRequestedMemory();
        bootSequence = template.getBootSequence();
        exitStatus = template.getExitStatus();
        pauseStatus = template.getPauseStatus();
        hash = template.getHash();
        guestAgentNicsHash = template.getGuestAgentNicsHash();
        exitMessage = template.getExitMessage();
        disks = new ArrayList<>(template.getDisks());
        win2kHackEnabled = template.getWin2kHackEnable();
        lastWatchdogEvent = template.getLastWatchdogEvent();
        lastWatchdogAction = template.getLastWatchdogAction();
        runOnce = template.isRunOnce();
        cpuName = template.getCpuName();
        guestAgentStatus = template.getGuestAgentStatus();
        emulatedMachine = template.getEmulatedMachine();
        currentCd = template.getCurrentCd();
        stopReason = template.getStopReason();
        exitReason = template.getExitReason();
        guestCpuCount = template.getGuestCpuCount();
        graphicsInfos = new HashMap<>(template.getGraphicsInfos());
        guestMemoryCached = template.getGuestMemoryCached();
        guestMemoryBuffered = template.getGuestMemoryBuffered();
        guestMemoryFree = template.getGuestMemoryFree();
        guestOsVersion = template.getGuestOsVersion();
        guestOsDistribution = template.getGuestOsDistribution();
        guestOsCodename = template.getGuestOsCodename();
        guestOsArch = template.getGuestOsArch();
        guestOsType = template.getGuestOsType();
        guestOsKernelVersion = template.getGuestOsKernelVersion();
        guestOsTimezoneName = template.getGuestOsTimezoneName();
        guestOsTimezoneOffset = template.getGuestOsTimezoneOffset();
        guestContainers = template.getGuestContainers();
    }

    public String getAppList() {
        return this.appList;
    }

    public void setAppList(String value) {
        this.appList = value;
    }

    public String getConsoleCurrentUserName() {
        return consoleCurrentUserName;
    }

    public void setConsoleCurrentUserName(String consoleCurUserName) {
        this.consoleCurrentUserName = consoleCurUserName;
    }

    public String getGuestCurrentUserName() {
        return this.guestCurUserName;
    }

    public void setGuestCurrentUserName(String value) {
        this.guestCurUserName = value;
    }

    public Guid getConsoleUserId() {
        return this.consoleUserId;
    }

    public void setConsoleUserId(Guid value) {
        this.consoleUserId = value;
    }

    public String getGuestOs() {
        return this.guestOs;
    }

    public void setGuestOs(String value) {
        this.guestOs = value;
    }

    public Guid getMigratingToVds() {
        return this.migratingToVds;
    }

    public void setMigratingToVds(Guid value) {
        this.migratingToVds = value;
    }

    public Guid getRunOnVds() {
        return this.runOnVds;
    }

    public void setRunOnVds(Guid value) {
        this.runOnVds = value;
    }

    @Override
    public VMStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(VMStatus value) {
        this.status = value;
    }

    @Override
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid value) {
        this.id = value;
    }

    public String getVmHost() {
        return this.vmHost;
    }

    public void setVmHost(String value) {
        this.vmHost = value;
    }

    public String getVmFQDN() {
        return this.vmFQDN;
    }

    public void setVmFQDN(String fqdn) {
        this.vmFQDN = fqdn;
    }

    public String getVmIp() {
        return this.vmIp;
    }

    public void setVmIp(String value) {
        this.vmIp = value;
    }

    public Date getLastStartTime() {
        return this.lastStartTime;
    }

    public void setLastStartTime(Date value) {
        this.lastStartTime = value;
    }

    public Date getLastStopTime() {
        return this.lastStopTime;
    }

    public void setLastStopTime(Date value) {
        this.lastStopTime = value;
    }

    public Integer getVmPid() {
        return this.vmPid;
    }

    public void setVmPid(Integer value) {
        this.vmPid = value;
    }

    public Map<GraphicsType, GraphicsInfo> getGraphicsInfos() {
        return graphicsInfos;
    }

    /*
     * DON'T use this setter. It's here only for serizalization.
     */
    public void setGraphicsInfos(Map<GraphicsType, GraphicsInfo> graphicsInfos) {
        this.graphicsInfos = graphicsInfos;
    }

    public Boolean getAcpiEnable() {
        return this.acpiEnabled;
    }

    public void setAcpiEnable(Boolean value) {
        this.acpiEnabled = value;
    }

    public String getVncKeyboardLayout() {
        return vncKeyboardLayout;
    }

    public void setVncKeyboardLayout(String vncKeyboardLayout) {
        this.vncKeyboardLayout = vncKeyboardLayout;
    }

    public Boolean getKvmEnable() {
        return this.kvmEnable;
    }

    public void setKvmEnable(Boolean value) {
        this.kvmEnable = value;
    }

    public SessionState getSession() {
        return this.session;
    }

    public void setSession(SessionState value) {
        this.session = value;
    }

    public BootSequence getBootSequence() {
        return this.bootSequence;
    }

    public void setBootSequence(BootSequence value) {
        this.bootSequence = value;
    }

    public Integer getUtcDiff() {
        return this.utcDiff;
    }

    public void setUtcDiff(Integer value) {
        this.utcDiff = value;
    }

    public Guid getLastVdsRunOn() {
        return this.lastVdsRunOn;
    }

    public void setLastVdsRunOn(Guid value) {
        this.lastVdsRunOn = value;
    }

    public String getClientIp() {
        return this.clientIp;
    }

    public void setClientIp(String value) {
        this.clientIp = value;
    }

    public Integer getGuestRequestedMemory() {
        return this.guestRequestedMemory;
    }

    public void setGuestRequestedMemory(Integer value) {
        this.guestRequestedMemory = value;
    }

    public void setPauseStatus(VmPauseStatus pauseStatus) {
        this.pauseStatus = pauseStatus;
    }

    public VmPauseStatus getPauseStatus() {
        return this.pauseStatus;
    }

    @Override
    public int compareTo(VmDynamic o) {
        return BusinessEntityComparator.<VmDynamic, Guid>newInstance().compare(this, o);
    }

    public Long getLastWatchdogEvent() {
        return lastWatchdogEvent;
    }

    public void setLastWatchdogEvent(Long lastWatchdogEvent) {
        this.lastWatchdogEvent = lastWatchdogEvent;
    }

    public String getLastWatchdogAction() {
        return lastWatchdogAction;
    }

    public void setLastWatchdogAction(String lastWatchdogAction) {
        this.lastWatchdogAction = lastWatchdogAction;
    }

    public boolean isRunOnce() {
        return runOnce;
    }

    public void setRunOnce(boolean runOnce) {
        this.runOnce = runOnce;
    }
    public String getCpuName() {
        return cpuName;
    }

    public void setCpuName(String cpuName) {
        this.cpuName = cpuName;
    }
    public GuestAgentStatus getGuestAgentStatus() {
        return guestAgentStatus;
    }

    public void setGuestAgentStatus(GuestAgentStatus guestAgentStatus) {
        this.guestAgentStatus = guestAgentStatus;
    }

    public String getCurrentCd() {
        return currentCd;
    }

    public void setCurrentCd(String currentCd) {
        this.currentCd = currentCd;
    }

    public String getStopReason() {
        return stopReason;
    }

    public void setStopReason(String stopReason) {
        this.stopReason = stopReason;
    }

    public VmExitReason getExitReason() {
        return exitReason;
    }

    public void setExitReason(VmExitReason value) {
        exitReason = value;
    }

    public void setGuestCpuCount(int guestCpuCount) {
        this.guestCpuCount = guestCpuCount;
    }

    public int getGuestCpuCount() {
        return guestCpuCount;
    }

    public String getEmulatedMachine() {
        return emulatedMachine;
    }

    public void setEmulatedMachine(String emulatedMachine) {
        this.emulatedMachine = emulatedMachine;
    }

    public Long getGuestMemoryCached() {
        return guestMemoryCached;
    }

    public void setGuestMemoryCached(Long guestMemoryCached) {
        this.guestMemoryCached = guestMemoryCached;
    }

    public Long getGuestMemoryBuffered() {
        return guestMemoryBuffered;
    }

    public void setGuestMemoryBuffered(Long guestMemoryBuffered) {
        this.guestMemoryBuffered = guestMemoryBuffered;
    }

    public Long getGuestMemoryFree() {
        return guestMemoryFree;
    }

    public void setGuestMemoryFree(Long guestMemoryFree) {
        this.guestMemoryFree = guestMemoryFree;
    }

    public int getGuestOsTimezoneOffset() {
        return guestOsTimezoneOffset;
    }

    public void setGuestOsTimezoneOffset(int guestOsTimezoneOffset) {
        this.guestOsTimezoneOffset = guestOsTimezoneOffset;
    }

    public String getGuestOsTimezoneName() {
        return guestOsTimezoneName;
    }

    public void setGuestOsTimezoneName(String guestOsTimezoneName) {
        this.guestOsTimezoneName = guestOsTimezoneName;
    }

    public String getGuestOsVersion() {
        return guestOsVersion;
    }

    public void setGuestOsVersion(String guestOsVersion) {
        this.guestOsVersion = guestOsVersion;
    }

    public String getGuestOsDistribution() {
        return guestOsDistribution;
    }

    public void setGuestOsDistribution(String guestOsDistribution) {
        this.guestOsDistribution = guestOsDistribution;
    }

    public String getGuestOsCodename() {
        return guestOsCodename;
    }

    public void setGuestOsCodename(String guestOsCodename) {
        this.guestOsCodename = guestOsCodename;
    }

    public ArchitectureType getGuestOsArch() {
        return guestOsArch;
    }

    public void setGuestOsArch(ArchitectureType guestOsArch) {
        this.guestOsArch = guestOsArch;
    }

    @JsonIgnore
    public void setGuestOsArch(Integer arch) {
        this.guestOsArch = ArchitectureType.forValue(arch);
    }

    @JsonIgnore
    public void setGuestOsArch(String arch) {
        this.guestOsArch = ArchitectureType.valueOf(arch);
    }

    public OsType getGuestOsType() {
        return guestOsType;
    }

    public void setGuestOsType(OsType guestOsType) {
        this.guestOsType = guestOsType;
    }

    @JsonIgnore
    public void setGuestOsType(String osType) {
        this.guestOsType = EnumUtils.valueOf(OsType.class, osType, true);
    }

    public String getGuestOsKernelVersion() {
        return guestOsKernelVersion;
    }

    public void setGuestOsKernelVersion(String guestOsKernelVersion) {
        this.guestOsKernelVersion = guestOsKernelVersion;
    }

    public List<GuestContainer> getGuestContainers() {
        return guestContainers;
    }

    public void setGuestContainers(List<GuestContainer> guestContainers) {
        this.guestContainers = guestContainers;
    }

    /**
     * Update data that was received from VDSM
     * @param vm - the reported VM from VDSM
     * @param vdsId - the host that it was reported from
     */
    public void updateRuntimeData(VmDynamic vm, Guid vdsId) {
        setStatus(vm.getStatus());
        setRunOnVds(vdsId);
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
        setVncKeyboardLayout(vm.getVncKeyboardLayout());
        setKvmEnable(vm.getKvmEnable());
        setAcpiEnable(vm.getAcpiEnable());
        setGuestCurrentUserName(vm.getGuestCurrentUserName());
        setWin2kHackEnable(vm.getWin2kHackEnable());
        setUtcDiff(vm.getUtcDiff());
        setExitStatus(vm.getExitStatus());
        setExitMessage(vm.getExitMessage());
        setExitReason(vm.getExitReason());
        setClientIp(vm.getClientIp());
        setPauseStatus(vm.getPauseStatus());
        setLastWatchdogEvent(vm.getLastWatchdogEvent());
        setGuestCpuCount(vm.getGuestCpuCount());
        setGraphicsInfos(new HashMap<>(vm.getGraphicsInfos()));
        setGuestMemoryBuffered(vm.getGuestMemoryBuffered());
        setGuestMemoryCached(vm.getGuestMemoryCached());
        setGuestMemoryFree(vm.getGuestMemoryFree());
        setGuestOsArch(vm.getGuestOsArch());
        setGuestOsCodename(vm.getGuestOsCodename());
        setGuestOsDistribution(vm.getGuestOsDistribution());
        setGuestOsKernelVersion(vm.getGuestOsKernelVersion());
        setGuestOsType(vm.getGuestOsType());
        setGuestOsVersion(vm.getGuestOsVersion());
        setGuestOsTimezoneName(vm.getGuestOsTimezoneName());
        setGuestOsTimezoneOffset(vm.getGuestOsTimezoneOffset());
        setGuestContainers(vm.getGuestContainers());
        // TODO: check what to do with update disk data
        // updateDisksData(vm);

        // updateSession(vm);
    }
}
