package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityGuidComparator;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class VmDynamic implements BusinessEntity<Guid>, Comparable<VmDynamic> {
    private static final long serialVersionUID = 7789482445091432555L;

    private Guid id = Guid.Empty;
    private VMStatus status = VMStatus.Down;
    private String vmIp;
    private String vmHost;
    private Integer vmPid;
    private Date lastStartTime;
    private String guestCurUserName;
    private String consoleCurUserName;
    private Guid consoleUserId;
    private Date guestLastLoginTime;
    private Date guestLastLogoutTime;
    private String guestOs;
    private Guid migratingToVds;
    private Guid runOnVds;
    private String appList;
    private Integer display;
    private Boolean acpiEnabled;
    private SessionState session = SessionState.Unknown;
    private String displayIp;
    private DisplayType displayType = DisplayType.vnc;
    private Boolean kvmEnable;
    private Integer displaySecurePort;
    private Integer utcDiff;
    private Guid lastVdsRunOn;
    private String clientIp;
    private Integer guestRequestedMemory;
    private String hibernationVolHandle;
    private BootSequence bootSequence = BootSequence.C;
    private VmExitStatus exitStatus = VmExitStatus.Normal;
    private VmPauseStatus pauseStatus = VmPauseStatus.NONE;
    private String hash;
    private int guestAgentNicsHash;
    private String mExitMessage;
    private ArrayList<DiskImageDynamic> disks;
    private boolean win2kHackEnabled = false;
    private Long lastWatchdogEvent = null;
    private String lastWatchdogAction = null;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((acpiEnabled == null) ? 0 : acpiEnabled.hashCode());
        result = prime * result + ((appList == null) ? 0 : appList.hashCode());
        result = prime * result + bootSequence.hashCode();
        result = prime * result + ((clientIp == null) ? 0 : clientIp.hashCode());
        result = prime * result + ((display == null) ? 0 : display.hashCode());
        result = prime * result + ((displayIp == null) ? 0 : displayIp.hashCode());
        result = prime * result + ((displaySecurePort == null) ? 0 : displaySecurePort.hashCode());
        result = prime * result + displayType.hashCode();
        result = prime * result + ((consoleCurUserName == null) ? 0 : consoleCurUserName.hashCode());
        result = prime * result + ((guestCurUserName == null) ? 0 : guestCurUserName.hashCode());
        result = prime * result + ((consoleUserId == null) ? 0 : consoleUserId.hashCode());
        result = prime * result + ((guestLastLoginTime == null) ? 0 : guestLastLoginTime.hashCode());
        result = prime * result + ((guestLastLogoutTime == null) ? 0 : guestLastLogoutTime.hashCode());
        result = prime * result + ((guestOs == null) ? 0 : guestOs.hashCode());
        result = prime * result + ((guestRequestedMemory == null) ? 0 : guestRequestedMemory.hashCode());
        result = prime * result + ((hibernationVolHandle == null) ? 0 : hibernationVolHandle.hashCode());
        result = prime * result + ((kvmEnable == null) ? 0 : kvmEnable.hashCode());
        result = prime * result + ((lastVdsRunOn == null) ? 0 : lastVdsRunOn.hashCode());
        result = prime * result + ((disks == null) ? 0 : disks.hashCode());
        result = prime * result + ((mExitMessage == null) ? 0 : mExitMessage.hashCode());
        result = prime * result + exitStatus.hashCode();
        result = prime * result + (win2kHackEnabled ? 1231 : 1237);
        result = prime * result + ((migratingToVds == null) ? 0 : migratingToVds.hashCode());
        result = prime * result + ((pauseStatus == null) ? 0 : pauseStatus.hashCode());
        result = prime * result + ((runOnVds == null) ? 0 : runOnVds.hashCode());
        result = prime * result + session.hashCode();
        result = prime * result + status.hashCode();
        result = prime * result + ((utcDiff == null) ? 0 : utcDiff.hashCode());
        result = prime * result + ((vmHost == null) ? 0 : vmHost.hashCode());
        result = prime * result + ((vmIp == null) ? 0 : vmIp.hashCode());
        result = prime * result + ((lastStartTime == null) ? 0 : lastStartTime.hashCode());
        result = prime * result + ((vmPid == null) ? 0 : vmPid.hashCode());
        result = prime * result + (lastWatchdogEvent == null ? 0 : lastWatchdogEvent.hashCode());
        result = prime * result + (lastWatchdogAction == null ? 0 : lastWatchdogAction.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VmDynamic other = (VmDynamic) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(acpiEnabled, other.acpiEnabled)
                && ObjectUtils.objectsEqual(appList, other.appList)
                && bootSequence == other.bootSequence
                && ObjectUtils.objectsEqual(clientIp, other.clientIp)
                && ObjectUtils.objectsEqual(display, other.display)
                && ObjectUtils.objectsEqual(displayIp, other.displayIp)
                && ObjectUtils.objectsEqual(displaySecurePort, other.displaySecurePort)
                && displayType == other.displayType
                && ObjectUtils.objectsEqual(consoleCurUserName, other.consoleCurUserName)
                && ObjectUtils.objectsEqual(guestCurUserName, other.guestCurUserName)
                && ObjectUtils.objectsEqual(consoleUserId, other.consoleUserId)
                && ObjectUtils.objectsEqual(guestLastLoginTime, other.guestLastLoginTime)
                && ObjectUtils.objectsEqual(guestLastLogoutTime, other.guestLastLogoutTime)
                && ObjectUtils.objectsEqual(guestOs, other.guestOs)
                && ObjectUtils.objectsEqual(guestRequestedMemory, other.guestRequestedMemory)
                && ObjectUtils.objectsEqual(hibernationVolHandle, other.hibernationVolHandle)
                && ObjectUtils.objectsEqual(kvmEnable, other.kvmEnable)
                && ObjectUtils.objectsEqual(lastVdsRunOn, other.lastVdsRunOn)
                && ObjectUtils.objectsEqual(disks, other.disks)
                && ObjectUtils.objectsEqual(mExitMessage, other.mExitMessage)
                && exitStatus == other.exitStatus
                && win2kHackEnabled == other.win2kHackEnabled
                && ObjectUtils.objectsEqual(migratingToVds, other.migratingToVds)
                && pauseStatus == other.pauseStatus
                && ObjectUtils.objectsEqual(runOnVds, other.runOnVds)
                && session == other.session
                && status == other.status
                && ObjectUtils.objectsEqual(utcDiff, other.utcDiff)
                && ObjectUtils.objectsEqual(vmHost, other.vmHost)
                && ObjectUtils.objectsEqual(vmIp, other.vmIp)
                && ObjectUtils.objectsEqual(lastStartTime, other.lastStartTime)
                && ObjectUtils.objectsEqual(vmPid, other.vmPid)
                && ObjectUtils.objectsEqual(lastWatchdogEvent, other.lastWatchdogEvent)
                && ObjectUtils.objectsEqual(lastWatchdogAction, other.lastWatchdogAction));
    }

    public String getExitMessage() {
        return mExitMessage;
    }

    public void setExitMessage(String value) {
        mExitMessage = value;
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
        exitStatus = VmExitStatus.Normal;
        win2kHackEnabled = false;
        acpiEnabled = true;
        kvmEnable = true;
        session = SessionState.Unknown;
        bootSequence = BootSequence.C;
    }

    public String getAppList() {
        return this.appList;
    }

    public void setAppList(String value) {
        this.appList = value;
    }

    public String getConsoleCurrentUserName() {
        return consoleCurUserName;
    }

    public void setConsoleCurrentUserName(String consoleCurUserName) {
        this.consoleCurUserName = consoleCurUserName;
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

    public Date getGuestLastLoginTime() {
        return this.guestLastLoginTime;
    }

    public void setGuestLastLoginTime(Date value) {
        this.guestLastLoginTime = value;
    }

    public Date getGuestLastLogoutTime() {
        return this.guestLastLogoutTime;
    }

    public void setGuestLastLogoutTime(Date value) {
        this.guestLastLogoutTime = value;
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

    public VMStatus getStatus() {
        return this.status;
    }

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

    public Integer getVmPid() {
        return this.vmPid;
    }

    public void setVmPid(Integer value) {
        this.vmPid = value;
    }

    public Integer getDisplay() {
        return this.display;
    }

    public void setDisplay(Integer value) {
        this.display = value;
    }

    public Boolean getAcpiEnable() {
        return this.acpiEnabled;
    }

    public void setAcpiEnable(Boolean value) {
        this.acpiEnabled = value;
    }

    public String getDisplayIp() {
        return this.displayIp;
    }

    public void setDisplayIp(String value) {
        this.displayIp = value;
    }

    public DisplayType getDisplayType() {
        return displayType;
    }

    public void setDisplayType(DisplayType value) {
        this.displayType = value;
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

    public Integer getDisplaySecurePort() {
        return this.displaySecurePort;
    }

    public void setDisplaySecurePort(Integer value) {
        this.displaySecurePort = value;
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

    public String getHibernationVolHandle() {
        return this.hibernationVolHandle;
    }

    public void setHibernationVolHandle(String value) {
        this.hibernationVolHandle = value;
    }

    public void setPauseStatus(VmPauseStatus pauseStatus) {
        this.pauseStatus = pauseStatus;

    }

    public VmPauseStatus getPauseStatus() {
        return this.pauseStatus;
    }

    @Override
    public int compareTo(VmDynamic o) {
        return BusinessEntityGuidComparator.<VmDynamic>newInstance().compare(this,o);
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

}
