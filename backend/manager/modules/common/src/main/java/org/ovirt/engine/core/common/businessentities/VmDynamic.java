package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VmDynamic implements BusinessEntityWithStatus<Guid, VMStatus>, Comparable<VmDynamic> {
    private static final long serialVersionUID = 7789482445091432555L;

    private Guid id;
    private VMStatus status;
    @UnchangeableByVdsm
    private String ip;
    private String fqdn;
    @UnchangeableByVdsm
    private String vmHost;
    @UnchangeableByVdsm
    private Date bootTime;
    @UnchangeableByVdsm
    private long downtime;
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
    private Integer utcDiff;
    private String clientIp;
    private Integer guestRequestedMemory;
    @UnchangeableByVdsm
    private BootSequence bootSequence;
    private VmExitStatus exitStatus;
    private VmPauseStatus pauseStatus;
    private int guestAgentNicsHash;
    @UnchangeableByVdsm
    private String exitMessage;
    private Long lastWatchdogEvent;
    private String lastWatchdogAction;
    @UnchangeableByVdsm
    private boolean runOnce;
    @UnchangeableByVdsm
    private boolean volatileRun;

    /**
     * cpuName contains one of two possible values:
     *
     * - CpuId VDSM verb for standard VMs (model + extra flags)
     * - List of host CPU flags present on the host where
     *   the VM was started for VMs with CPU flag passthrough
     */
    @UnchangeableByVdsm
    private String cpuName;

    @UnchangeableByVdsm
    private GuestAgentStatus ovirtGuestAgentStatus;
    @UnchangeableByVdsm
    private GuestAgentStatus qemuGuestAgentStatus;
    @UnchangeableByVdsm
    private String emulatedMachine;
    private String currentCd;
    @UnchangeableByVdsm
    private String stopReason;
    private VmExitReason exitReason;
    private int guestCpuCount;
    private Map<GraphicsType, GraphicsInfo> graphicsInfos;
    private String guestOsVersion;
    private String guestOsDistribution;
    private String guestOsCodename;
    private ArchitectureType guestOsArch;
    private OsType guestOsType;
    private String guestOsKernelVersion;
    private String guestOsTimezoneName;
    private int guestOsTimezoneOffset;
    private List<GuestContainer> guestContainers;
    @UnchangeableByVdsm
    private Map<String, String> leaseInfo;
    @UnchangeableByVdsm
    private String runtimeName;
    @UnchangeableByVdsm
    private String currentCpuPinning;
    @UnchangeableByVdsm
    private int currentSockets;
    @UnchangeableByVdsm
    private int currentCoresPerSocket;
    @UnchangeableByVdsm
    private int currentThreadsPerCore;

    public static final String APPLICATIONS_LIST_FIELD_NAME = "appList";

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
                exitMessage,
                exitStatus,
                migratingToVds,
                pauseStatus,
                runOnVds,
                session,
                status,
                utcDiff,
                vmHost,
                ip,
                fqdn,
                lastStartTime,
                bootTime,
                downtime,
                lastStopTime,
                lastWatchdogEvent,
                lastWatchdogAction,
                runOnce,
                volatileRun,
                cpuName,
                ovirtGuestAgentStatus,
                qemuGuestAgentStatus,
                currentCd,
                stopReason,
                exitReason,
                emulatedMachine,
                graphicsInfos,
                guestOsTimezoneName,
                guestOsTimezoneOffset,
                guestOsArch,
                guestOsCodename,
                guestOsDistribution,
                guestOsKernelVersion,
                guestOsVersion,
                guestOsType,
                guestContainers,
                leaseInfo,
                currentCpuPinning,
                currentSockets,
                currentCoresPerSocket,
                currentThreadsPerCore
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
                && Objects.equals(exitMessage, other.exitMessage)
                && exitStatus == other.exitStatus
                && Objects.equals(migratingToVds, other.migratingToVds)
                && pauseStatus == other.pauseStatus
                && Objects.equals(runOnVds, other.runOnVds)
                && session == other.session
                && status == other.status
                && Objects.equals(utcDiff, other.utcDiff)
                && Objects.equals(vmHost, other.vmHost)
                && Objects.equals(ip, other.ip)
                && Objects.equals(fqdn, other.fqdn)
                && Objects.equals(lastStartTime, other.lastStartTime)
                && Objects.equals(bootTime, other.bootTime)
                && Objects.equals(downtime, other.downtime)
                && Objects.equals(lastStopTime, other.lastStopTime)
                && Objects.equals(lastWatchdogEvent, other.lastWatchdogEvent)
                && Objects.equals(lastWatchdogAction, other.lastWatchdogAction)
                && runOnce == other.runOnce
                && volatileRun == other.volatileRun
                && Objects.equals(cpuName, other.cpuName)
                && Objects.equals(ovirtGuestAgentStatus, other.ovirtGuestAgentStatus)
                && Objects.equals(qemuGuestAgentStatus, other.qemuGuestAgentStatus)
                && Objects.equals(currentCd, other.currentCd)
                && Objects.equals(stopReason, other.stopReason)
                && exitReason == other.exitReason
                && Objects.equals(emulatedMachine, other.emulatedMachine)
                && Objects.equals(graphicsInfos, other.getGraphicsInfos())
                && Objects.equals(guestOsTimezoneName, other.guestOsTimezoneName)
                && guestOsTimezoneOffset == other.guestOsTimezoneOffset
                && Objects.equals(guestOsVersion, other.guestOsVersion)
                && Objects.equals(guestOsDistribution, other.guestOsDistribution)
                && Objects.equals(guestOsCodename, other.guestOsCodename)
                && Objects.equals(guestOsKernelVersion, other.guestOsKernelVersion)
                && Objects.equals(guestOsArch, other.guestOsArch)
                && Objects.equals(guestOsType, other.guestOsType)
                && Objects.equals(guestContainers, other.guestContainers)
                && Objects.equals(leaseInfo, other.leaseInfo)
                && Objects.equals(currentCpuPinning, other.currentCpuPinning)
                && Objects.equals(currentSockets, other.currentSockets)
                && Objects.equals(currentCoresPerSocket, other.currentCoresPerSocket)
                && Objects.equals(currentThreadsPerCore, other.currentThreadsPerCore);
    }

    public Date getBootTime() {
        return bootTime;
    }

    public void setBootTime(Date bootTime) {
        this.bootTime = bootTime;
    }

    public long getDowntime() {
        return downtime;
    }

    public void setDowntime(long downtime) {
        this.downtime = downtime;
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

    public int getGuestAgentNicsHash() {
        return guestAgentNicsHash;
    }

    public void setGuestAgentNicsHash(int guestAgentNicsHash) {
        this.guestAgentNicsHash = guestAgentNicsHash;
    }

    public VmDynamic() {
        id = Guid.Empty;
        status = VMStatus.Down;
        pauseStatus = VmPauseStatus.NONE;
        exitStatus = VmExitStatus.Normal;
        acpiEnabled = true;
        session = SessionState.Unknown;
        bootSequence = BootSequence.C;
        exitReason = VmExitReason.Unknown;
        graphicsInfos = new HashMap<>();
        ovirtGuestAgentStatus = GuestAgentStatus.DoesntExist;
        qemuGuestAgentStatus = GuestAgentStatus.DoesntExist;
        guestOsTimezoneName = "";
        guestOsTimezoneOffset = 0;
        guestOsVersion = "";
        guestOsDistribution = "";
        guestOsCodename = "";
        guestOsKernelVersion = "";
        guestOsArch = ArchitectureType.undefined;
        guestOsType = OsType.Other;
        guestContainers = new ArrayList<>();
    }

    public VmDynamic(VmDynamic template) {
        id = template.getId();
        status = template.getStatus();
        ip = template.getIp();
        fqdn = template.getFqdn();
        vmHost = template.getVmHost();
        lastStartTime = template.getLastStartTime();
        bootTime = template.getBootTime();
        downtime = template.getDowntime();
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
        utcDiff = template.getUtcDiff();
        clientIp = template.getClientIp();
        guestRequestedMemory = template.getGuestRequestedMemory();
        bootSequence = template.getBootSequence();
        exitStatus = template.getExitStatus();
        pauseStatus = template.getPauseStatus();
        guestAgentNicsHash = template.getGuestAgentNicsHash();
        exitMessage = template.getExitMessage();
        lastWatchdogEvent = template.getLastWatchdogEvent();
        lastWatchdogAction = template.getLastWatchdogAction();
        runOnce = template.isRunOnce();
        cpuName = template.getCpuName();
        ovirtGuestAgentStatus = template.getOvirtGuestAgentStatus();
        qemuGuestAgentStatus = template.getQemuGuestAgentStatus();
        emulatedMachine = template.getEmulatedMachine();
        currentCd = template.getCurrentCd();
        stopReason = template.getStopReason();
        exitReason = template.getExitReason();
        guestCpuCount = template.getGuestCpuCount();
        graphicsInfos = new HashMap<>(template.getGraphicsInfos());
        guestOsVersion = template.getGuestOsVersion();
        guestOsDistribution = template.getGuestOsDistribution();
        guestOsCodename = template.getGuestOsCodename();
        guestOsArch = template.getGuestOsArch();
        guestOsType = template.getGuestOsType();
        guestOsKernelVersion = template.getGuestOsKernelVersion();
        guestOsTimezoneName = template.getGuestOsTimezoneName();
        guestOsTimezoneOffset = template.getGuestOsTimezoneOffset();
        guestContainers = template.getGuestContainers();
        volatileRun = template.isVolatileRun();
        leaseInfo = template.leaseInfo;
        currentCpuPinning = template.getCurrentCpuPinning();
        currentSockets = template.getCurrentSockets();
        currentCoresPerSocket = template.getCurrentCoresPerSocket();
        currentThreadsPerCore = template.getCurrentThreadsPerCore();
    }

    public String getAppList() {
        return this.appList;
    }

    public void setAppList(String value) {
        if (value != null) {
            this.appList = value.intern();
        } else {
            this.appList = null;
        }
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

    public String getFqdn() {
        return this.fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String value) {
        this.ip = value;
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

    public boolean isVolatileRun() {
        return volatileRun;
    }

    public void setVolatileRun(boolean volatileRun) {
        this.volatileRun = volatileRun;
    }

    public String getCpuName() {
        return cpuName;
    }

    public void setCpuName(String cpuName) {
        this.cpuName = cpuName;
    }
    public GuestAgentStatus getOvirtGuestAgentStatus() {
        return ovirtGuestAgentStatus;
    }

    public GuestAgentStatus getQemuGuestAgentStatus() {
        return qemuGuestAgentStatus;
    }

    public void setOvirtGuestAgentStatus(GuestAgentStatus ovirtGuestAgentStatus) {
        this.ovirtGuestAgentStatus = ovirtGuestAgentStatus;
    }

    public void setQemuGuestAgentStatus(GuestAgentStatus qemuGuestAgentStatus) {
        this.qemuGuestAgentStatus = qemuGuestAgentStatus;
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

    public Map<String, String> getLeaseInfo() {
        return leaseInfo;
    }

    public void setLeaseInfo(Map<String, String> leaseInfo) {
        this.leaseInfo = leaseInfo;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public void setRuntimeName(String runtimeName) {
        this.runtimeName = runtimeName;
    }

    public String getCurrentCpuPinning() {
        return currentCpuPinning;
    }

    public void setCurrentCpuPinning(String cpuPinning) {
        this.currentCpuPinning = cpuPinning;
    }

    public int getCurrentSockets() {
        return currentSockets;
    }

    public void setCurrentSockets(int sockets) {
        this.currentSockets = sockets;
    }

    public int getCurrentCoresPerSocket() {
        return currentCoresPerSocket;
    }

    public void setCurrentCoresPerSocket(int cores) {
        currentCoresPerSocket = cores;
    }

    public int getCurrentThreadsPerCore() {
        return currentThreadsPerCore;
    }

    public void setCurrentThreadsPerCore(int threads) {
        this.currentThreadsPerCore = threads;
    }

    /**
     * Update data that was received from VDSM
     * @param vm - the reported VM from VDSM
     * @param vdsId - the host that it was reported from
     */
    public void updateRuntimeData(VmDynamic vm, Guid vdsId) {
        setStatus(vm.getStatus());
        if (vm.getStatus().isUpOrPaused()) {
            // migratingToVds is usually cleared by the migrate command or by ResourceManager#resetVmAttributes, this is just a
            // safety net in case those are missed, e.g., when the engine restarts and the migration ends before the engine is up.
            setMigratingToVds(null);
        }
        setRunOnVds(vdsId);
        setVmHost(vm.getVmHost());
        setFqdn(vm.getFqdn());
        // update only if vdsm actually provides some value, otherwise engine has more information
        if (vm.getCurrentCd() != null) {
            setCurrentCd(vm.getCurrentCd());
        }

        setAppList(vm.getAppList());
        setGuestOs(vm.getGuestOs());
        setVncKeyboardLayout(vm.getVncKeyboardLayout());
        setAcpiEnable(vm.getAcpiEnable());
        setGuestCurrentUserName(vm.getGuestCurrentUserName());
        setUtcDiff(vm.getUtcDiff());
        setExitStatus(vm.getExitStatus());
        setExitMessage(vm.getExitMessage());
        setExitReason(vm.getExitReason());
        setClientIp(vm.getClientIp());
        setPauseStatus(vm.getPauseStatus());
        setLastWatchdogEvent(vm.getLastWatchdogEvent());
        setGuestCpuCount(vm.getGuestCpuCount());
        setGraphicsInfos(new HashMap<>(vm.getGraphicsInfos()));
        setGuestOsArch(vm.getGuestOsArch());
        setGuestOsCodename(vm.getGuestOsCodename());
        setGuestOsDistribution(vm.getGuestOsDistribution());
        setGuestOsKernelVersion(vm.getGuestOsKernelVersion());
        setGuestOsType(vm.getGuestOsType());
        setGuestOsVersion(vm.getGuestOsVersion());
        setGuestOsTimezoneName(vm.getGuestOsTimezoneName());
        setGuestOsTimezoneOffset(vm.getGuestOsTimezoneOffset());
        setGuestContainers(vm.getGuestContainers());
        setGuestAgentNicsHash(vm.getGuestAgentNicsHash());
        setQemuGuestAgentStatus(vm.getQemuGuestAgentStatus());
        setSession(vm.getSession());
    }
}
