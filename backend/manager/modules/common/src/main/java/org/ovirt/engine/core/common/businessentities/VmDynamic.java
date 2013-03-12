package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VmDynamic implements BusinessEntity<Guid>, Comparable<VmDynamic> {
    private static final long serialVersionUID = 7789482445091432555L;

    private Guid id = new Guid();
    private VMStatus status = VMStatus.Down;
    private String vmIp;
    private String vmHost;
    private Integer vmPid;
    private Date lastStartTime;
    private String guestCurUserName;
    private String consoleCurUserName;
    private NGuid consoleUserId;
    private Date guestLastLoginTime;
    private Date guestLastLogoutTime;
    private String guestOs;
    private NGuid migratingToVds;
    private NGuid runOnVds;
    private String appList;
    private Integer display;
    private Boolean acpiEnabled;
    private SessionState session = SessionState.Unknown;
    private String displayIp;
    private DisplayType displayType = DisplayType.vnc;
    private Boolean kvmEnable;
    private Integer displaySecurePort;
    private Integer utcDiff;
    private NGuid lastVdsRunOn;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((acpiEnabled == null) ? 0 : acpiEnabled.hashCode());
        result = prime * result
                + ((appList == null) ? 0 : appList.hashCode());
        result = prime
                * result
                + bootSequence.hashCode() * prime;
        result = prime * result
                + ((clientIp == null) ? 0 : clientIp.hashCode());
        result = prime * result
                + ((display == null) ? 0 : display.hashCode());
        result = prime * result
                + ((displayIp == null) ? 0 : displayIp.hashCode());
        result = prime
                * result
                + ((displaySecurePort == null) ? 0
                        : displaySecurePort.hashCode());
        result = prime
                * result
                + displayType.hashCode() * prime;
        result = prime
                * result
                + ((consoleCurUserName == null) ? 0
                        : consoleCurUserName.hashCode());
        result = prime
                * result
                + ((guestCurUserName == null) ? 0
                        : guestCurUserName.hashCode());
        result = prime
                * result
                + ((consoleUserId == null) ? 0
                       : consoleUserId.hashCode());
        result = prime
                * result
                + ((guestLastLoginTime == null) ? 0
                        : guestLastLoginTime.hashCode());
        result = prime
                * result
                + ((guestLastLogoutTime == null) ? 0
                        : guestLastLogoutTime.hashCode());
        result = prime * result
                + ((guestOs == null) ? 0 : guestOs.hashCode());
        result = prime
                * result
                + ((guestRequestedMemory == null) ? 0
                        : guestRequestedMemory.hashCode());
        result = prime
                * result
                + ((hibernationVolHandle == null) ? 0
                        : hibernationVolHandle.hashCode());
        result = prime * result
                + ((kvmEnable == null) ? 0 : kvmEnable.hashCode());
        result = prime
                * result
                + ((lastVdsRunOn == null) ? 0 : lastVdsRunOn
                        .hashCode());
        result = prime * result + ((disks == null) ? 0 : disks.hashCode());
        result = prime * result
                + ((mExitMessage == null) ? 0 : mExitMessage.hashCode());
        result = prime * result
                + exitStatus.hashCode() * prime;
        result = prime * result + (win2kHackEnabled ? 1231 : 1237);
        result = prime
                * result
                + ((migratingToVds == null) ? 0 : migratingToVds
                        .hashCode());
        result = prime * result
                + ((pauseStatus == null) ? 0 : pauseStatus.hashCode());
        result = prime * result
                + ((runOnVds == null) ? 0 : runOnVds.hashCode());
        result = prime * result
                + session.hashCode() * prime;
        result = prime * result
                + status.hashCode() * prime;
        result = prime * result
                + ((utcDiff == null) ? 0 : utcDiff.hashCode());
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((vmHost == null) ? 0 : vmHost.hashCode());
        result = prime * result
                + ((vmIp == null) ? 0 : vmIp.hashCode());
        result = prime
                * result
                + ((lastStartTime == null) ? 0
                        : lastStartTime.hashCode());
        result = prime * result
                + ((vmPid == null) ? 0 : vmPid.hashCode());
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
                && ObjectUtils.objectsEqual(vmPid, other.vmPid));
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

    public String getapp_list() {
        return this.appList;
    }

    public void setapp_list(String value) {
        this.appList = value;
    }

    public String getConsole_current_user_name() {
        return consoleCurUserName;
    }

    public void setConsole_current_user_name(String console_cur_user_name) {
        this.consoleCurUserName = console_cur_user_name;
    }

    public String getguest_cur_user_name() {
        return this.guestCurUserName;
    }

    public void setguest_cur_user_name(String value) {
        this.guestCurUserName = value;
    }

    public NGuid getConsoleUserId() {
        return this.consoleUserId;
    }

    public void setConsoleUserId(NGuid value) {
        this.consoleUserId = value;
    }

    public String getguest_os() {
        return this.guestOs;
    }

    public void setguest_os(String value) {
        this.guestOs = value;
    }

    public Date getguest_last_login_time() {
        return this.guestLastLoginTime;
    }

    public void setguest_last_login_time(Date value) {
        this.guestLastLoginTime = value;
    }

    public Date getguest_last_logout_time() {
        return this.guestLastLogoutTime;
    }

    public void setguest_last_logout_time(Date value) {
        this.guestLastLogoutTime = value;
    }

    public NGuid getmigrating_to_vds() {
        return this.migratingToVds;
    }

    public void setmigrating_to_vds(NGuid value) {
        this.migratingToVds = value;
    }

    public NGuid getrun_on_vds() {
        return this.runOnVds;
    }

    public void setrun_on_vds(NGuid value) {
        this.runOnVds = value;
    }

    public VMStatus getstatus() {
        return this.status;
    }

    public void setstatus(VMStatus value) {
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

    public String getvm_host() {
        return this.vmHost;
    }

    public void setvm_host(String value) {
        this.vmHost = value;
    }

    public String getvm_ip() {
        return this.vmIp;
    }

    public void setvm_ip(String value) {
        this.vmIp = value;
    }

    public Date getLastStartTime() {
        return this.lastStartTime;
    }

    public void setLastStartTime(Date value) {
        this.lastStartTime = value;
    }

    public Integer getvm_pid() {
        return this.vmPid;
    }

    public void setvm_pid(Integer value) {
        this.vmPid = value;
    }

    public Integer getdisplay() {
        return this.display;
    }

    public void setdisplay(Integer value) {
        this.display = value;
    }

    public Boolean getacpi_enable() {
        return this.acpiEnabled;
    }

    public void setacpi_enable(Boolean value) {
        this.acpiEnabled = value;
    }

    public String getdisplay_ip() {
        return this.displayIp;
    }

    public void setdisplay_ip(String value) {
        this.displayIp = value;
    }

    public DisplayType getdisplay_type() {
        return displayType;
    }

    public void setdisplay_type(DisplayType value) {
        this.displayType = value;
    }

    public Boolean getkvm_enable() {
        return this.kvmEnable;
    }

    public void setkvm_enable(Boolean value) {
        this.kvmEnable = value;
    }

    public SessionState getsession() {
        return this.session;
    }

    public void setsession(SessionState value) {
        this.session = value;
    }

    public BootSequence getboot_sequence() {
        return this.bootSequence;
    }

    public void setboot_sequence(BootSequence value) {
        this.bootSequence = value;
    }

    public Integer getdisplay_secure_port() {
        return this.displaySecurePort;
    }

    public void setdisplay_secure_port(Integer value) {
        this.displaySecurePort = value;
    }

    public Integer getutc_diff() {
        return this.utcDiff;
    }

    public void setutc_diff(Integer value) {
        this.utcDiff = value;
    }

    public NGuid getlast_vds_run_on() {
        return this.lastVdsRunOn;
    }

    public void setlast_vds_run_on(NGuid value) {
        this.lastVdsRunOn = value;
    }

    public String getclient_ip() {
        return this.clientIp;
    }

    public void setclient_ip(String value) {
        this.clientIp = value;
    }

    public Integer getguest_requested_memory() {
        return this.guestRequestedMemory;
    }

    public void setguest_requested_memory(Integer value) {
        this.guestRequestedMemory = value;
    }

    public String gethibernation_vol_handle() {
        return this.hibernationVolHandle;
    }

    public void sethibernation_vol_handle(String value) {
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
}
