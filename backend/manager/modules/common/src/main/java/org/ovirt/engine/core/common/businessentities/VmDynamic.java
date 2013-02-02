package org.ovirt.engine.core.common.businessentities;

import java.util.Date;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VmDynamic implements BusinessEntity<Guid>, Comparable<VmDynamic> {
    private static final long serialVersionUID = 7789482445091432555L;

    private Guid id = new Guid();
    private VMStatus status = VMStatus.Down;
    private String vm_ip;
    private String vm_host;
    private Integer vm_pid;
    private Date lastStartTime;
    private String guest_cur_user_name;
    private String console_cur_user_name;
    private NGuid consoleUserId;
    private java.util.Date guest_last_login_time;
    private java.util.Date guest_last_logout_time;
    private String guest_os;
    private NGuid migrating_to_vds;
    private NGuid run_on_vds;
    private String appList;
    private Integer display;
    private Boolean acpi_enable;
    private SessionState session = SessionState.Unknown;
    private String display_ip;
    private DisplayType display_type = DisplayType.vnc;
    private Boolean kvm_enable;
    private Integer display_secure_port;
    private Integer utc_diff;
    private NGuid last_vds_run_on;
    private String client_ip;
    private Integer guest_requested_memory;
    private String hibernation_vol_handle;
    private BootSequence boot_sequence = BootSequence.C;
    private VmExitStatus mExitStatus = VmExitStatus.Normal;
    private VmPauseStatus pauseStatus = VmPauseStatus.NONE;
    private String hash;
    private int guestAgentNicsHash;
    private String mExitMessage;
    private java.util.ArrayList<DiskImageDynamic> mDisks;
    private boolean mWin2kHackEnable = false;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((acpi_enable == null) ? 0 : acpi_enable.hashCode());
        result = prime * result
                + ((appList == null) ? 0 : appList.hashCode());
        result = prime
                * result
                + boot_sequence.hashCode() * prime;
        result = prime * result
                + ((client_ip == null) ? 0 : client_ip.hashCode());
        result = prime * result
                + ((display == null) ? 0 : display.hashCode());
        result = prime * result
                + ((display_ip == null) ? 0 : display_ip.hashCode());
        result = prime
                * result
                + ((display_secure_port == null) ? 0
                        : display_secure_port.hashCode());
        result = prime
                * result
                + display_type.hashCode() * prime;
        result = prime
                * result
                + ((console_cur_user_name == null) ? 0
                        : console_cur_user_name.hashCode());
        result = prime
                * result
                + ((guest_cur_user_name == null) ? 0
                        : guest_cur_user_name.hashCode());
        result = prime
                * result
                + ((consoleUserId == null) ? 0
                       : consoleUserId.hashCode());
        result = prime
                * result
                + ((guest_last_login_time == null) ? 0
                        : guest_last_login_time.hashCode());
        result = prime
                * result
                + ((guest_last_logout_time == null) ? 0
                        : guest_last_logout_time.hashCode());
        result = prime * result
                + ((guest_os == null) ? 0 : guest_os.hashCode());
        result = prime
                * result
                + ((guest_requested_memory == null) ? 0
                        : guest_requested_memory.hashCode());
        result = prime
                * result
                + ((hibernation_vol_handle == null) ? 0
                        : hibernation_vol_handle.hashCode());
        result = prime * result
                + ((kvm_enable == null) ? 0 : kvm_enable.hashCode());
        result = prime
                * result
                + ((last_vds_run_on == null) ? 0 : last_vds_run_on
                        .hashCode());
        result = prime * result + ((mDisks == null) ? 0 : mDisks.hashCode());
        result = prime * result
                + ((mExitMessage == null) ? 0 : mExitMessage.hashCode());
        result = prime * result
                + mExitStatus.hashCode() * prime;
        result = prime * result + (mWin2kHackEnable ? 1231 : 1237);
        result = prime
                * result
                + ((migrating_to_vds == null) ? 0 : migrating_to_vds
                        .hashCode());
        result = prime * result
                + ((pauseStatus == null) ? 0 : pauseStatus.hashCode());
        result = prime * result
                + ((run_on_vds == null) ? 0 : run_on_vds.hashCode());
        result = prime * result
                + session.hashCode() * prime;
        result = prime * result
                + status.hashCode() * prime;
        result = prime * result
                + ((utc_diff == null) ? 0 : utc_diff.hashCode());
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((vm_host == null) ? 0 : vm_host.hashCode());
        result = prime * result
                + ((vm_ip == null) ? 0 : vm_ip.hashCode());
        result = prime
                * result
                + ((lastStartTime == null) ? 0
                        : lastStartTime.hashCode());
        result = prime * result
                + ((vm_pid == null) ? 0 : vm_pid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VmDynamic other = (VmDynamic) obj;
        if (acpi_enable == null) {
            if (other.acpi_enable != null)
                return false;
        } else if (!acpi_enable.equals(other.acpi_enable))
            return false;
        if (appList == null) {
            if (other.appList != null)
                return false;
        } else if (!appList.equals(other.appList))
            return false;
        if (boot_sequence != other.boot_sequence)
            return false;
        if (client_ip == null) {
            if (other.client_ip != null)
                return false;
        } else if (!client_ip.equals(other.client_ip))
            return false;
        if (display == null) {
            if (other.display != null)
                return false;
        } else if (!display.equals(other.display))
            return false;
        if (display_ip == null) {
            if (other.display_ip != null)
                return false;
        } else if (!display_ip.equals(other.display_ip))
            return false;
        if (display_secure_port == null) {
            if (other.display_secure_port != null)
                return false;
        } else if (!display_secure_port
                .equals(other.display_secure_port))
            return false;
        if (display_type != other.display_type)
            return false;
        if (console_cur_user_name == null) {
            if (other.console_cur_user_name != null)
                return false;
        } else if (!console_cur_user_name.equals(other.console_cur_user_name))
            return false;
        if (guest_cur_user_name == null) {
            if (other.guest_cur_user_name != null)
                return false;
        } else if (!guest_cur_user_name
                .equals(other.guest_cur_user_name))
            return false;
        if (consoleUserId == null) {
            if (other.consoleUserId != null)
                return false;
        } else if (!consoleUserId.equals(other.consoleUserId))
            return false;
        if (guest_last_login_time == null) {
            if (other.guest_last_login_time != null)
                return false;
        } else if (!guest_last_login_time
                .equals(other.guest_last_login_time))
            return false;
        if (guest_last_logout_time == null) {
            if (other.guest_last_logout_time != null)
                return false;
        } else if (!guest_last_logout_time
                .equals(other.guest_last_logout_time))
            return false;
        if (guest_os == null) {
            if (other.guest_os != null)
                return false;
        } else if (!guest_os.equals(other.guest_os))
            return false;
        if (guest_requested_memory == null) {
            if (other.guest_requested_memory != null)
                return false;
        } else if (!guest_requested_memory
                .equals(other.guest_requested_memory))
            return false;
        if (hibernation_vol_handle == null) {
            if (other.hibernation_vol_handle != null)
                return false;
        } else if (!hibernation_vol_handle
                .equals(other.hibernation_vol_handle))
            return false;
        if (kvm_enable == null) {
            if (other.kvm_enable != null)
                return false;
        } else if (!kvm_enable.equals(other.kvm_enable))
            return false;
        if (last_vds_run_on == null) {
            if (other.last_vds_run_on != null)
                return false;
        } else if (!last_vds_run_on.equals(other.last_vds_run_on))
            return false;
        if (mDisks == null) {
            if (other.mDisks != null)
                return false;
        } else if (!mDisks.equals(other.mDisks))
            return false;
        if (mExitMessage == null) {
            if (other.mExitMessage != null)
                return false;
        } else if (!mExitMessage.equals(other.mExitMessage))
            return false;
        if (mExitStatus != other.mExitStatus)
            return false;
        if (mWin2kHackEnable != other.mWin2kHackEnable)
            return false;
        if (migrating_to_vds == null) {
            if (other.migrating_to_vds != null)
                return false;
        } else if (!migrating_to_vds.equals(other.migrating_to_vds))
            return false;
        if (pauseStatus != other.pauseStatus)
            return false;
        if (run_on_vds == null) {
            if (other.run_on_vds != null)
                return false;
        } else if (!run_on_vds.equals(other.run_on_vds))
            return false;
        if (session != other.session)
            return false;
        if (status != other.status)
            return false;
        if (utc_diff == null) {
            if (other.utc_diff != null)
                return false;
        } else if (!utc_diff.equals(other.utc_diff))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (vm_host == null) {
            if (other.vm_host != null)
                return false;
        } else if (!vm_host.equals(other.vm_host))
            return false;
        if (vm_ip == null) {
            if (other.vm_ip != null)
                return false;
        } else if (!vm_ip.equals(other.vm_ip))
            return false;
        if (lastStartTime == null) {
            if (other.lastStartTime != null)
                return false;
        } else if (!lastStartTime.equals(other.lastStartTime))
            return false;
        if (vm_pid == null) {
            if (other.vm_pid != null)
                return false;
        } else if (!vm_pid.equals(other.vm_pid))
            return false;
        return true;
    }

    public String getExitMessage() {
        return mExitMessage;
    }

    public void setExitMessage(String value) {
        mExitMessage = value;
    }

    public VmExitStatus getExitStatus() {
        return this.mExitStatus;
    }

    public void setExitStatus(VmExitStatus value) {
        mExitStatus = value;
    }

    public java.util.ArrayList<DiskImageDynamic> getDisks() {
        return mDisks;
    }

    public void setDisks(java.util.ArrayList<DiskImageDynamic> value) {
        mDisks = value;
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
        return mWin2kHackEnable;
    }

    public void setWin2kHackEnable(boolean value) {
        mWin2kHackEnable = value;
    }

    public VmDynamic() {
        mExitStatus = VmExitStatus.Normal;
        mWin2kHackEnable = false;
        acpi_enable = true;
        kvm_enable = true;
        session = SessionState.Unknown;
        boot_sequence = BootSequence.C;
    }

    public String getapp_list() {
        return this.appList;
    }

    public void setapp_list(String value) {
        this.appList = value;
    }

    public String getConsole_current_user_name() {
        return console_cur_user_name;
    }

    public void setConsole_current_user_name(String console_cur_user_name) {
        this.console_cur_user_name = console_cur_user_name;
    }

    public String getguest_cur_user_name() {
        return this.guest_cur_user_name;
    }

    public void setguest_cur_user_name(String value) {
        this.guest_cur_user_name = value;
    }

    public NGuid getConsoleUserId() {
        return this.consoleUserId;
    }

    public void setConsoleUserId(NGuid value) {
        this.consoleUserId = value;
    }

    public String getguest_os() {
        return this.guest_os;
    }

    public void setguest_os(String value) {
        this.guest_os = value;
    }

    public java.util.Date getguest_last_login_time() {
        return this.guest_last_login_time;
    }

    public void setguest_last_login_time(java.util.Date value) {
        this.guest_last_login_time = value;
    }

    public java.util.Date getguest_last_logout_time() {
        return this.guest_last_logout_time;
    }

    public void setguest_last_logout_time(java.util.Date value) {
        this.guest_last_logout_time = value;
    }

    public NGuid getmigrating_to_vds() {
        return this.migrating_to_vds;
    }

    public void setmigrating_to_vds(NGuid value) {
        this.migrating_to_vds = value;
    }

    public NGuid getrun_on_vds() {
        return this.run_on_vds;
    }

    public void setrun_on_vds(NGuid value) {
        this.run_on_vds = value;
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
        return this.vm_host;
    }

    public void setvm_host(String value) {
        this.vm_host = value;
    }

    public String getvm_ip() {
        return this.vm_ip;
    }

    public void setvm_ip(String value) {
        this.vm_ip = value;
    }

    public Date getLastStartTime() {
        return this.lastStartTime;
    }

    public void setLastStartTime(Date value) {
        this.lastStartTime = value;
    }

    public Integer getvm_pid() {
        return this.vm_pid;
    }

    public void setvm_pid(Integer value) {
        this.vm_pid = value;
    }

    public Integer getdisplay() {
        return this.display;
    }

    public void setdisplay(Integer value) {
        this.display = value;
    }

    public Boolean getacpi_enable() {
        return this.acpi_enable;
    }

    public void setacpi_enable(Boolean value) {
        this.acpi_enable = value;
    }

    public String getdisplay_ip() {
        return this.display_ip;
    }

    public void setdisplay_ip(String value) {
        this.display_ip = value;
    }

    public DisplayType getdisplay_type() {
        return display_type;
    }

    public void setdisplay_type(DisplayType value) {
        this.display_type = value;
    }

    public Boolean getkvm_enable() {
        return this.kvm_enable;
    }

    public void setkvm_enable(Boolean value) {
        this.kvm_enable = value;
    }

    public SessionState getsession() {
        return this.session;
    }

    public void setsession(SessionState value) {
        this.session = value;
    }

    public BootSequence getboot_sequence() {
        return this.boot_sequence;
    }

    public void setboot_sequence(BootSequence value) {
        this.boot_sequence = value;
    }

    public Integer getdisplay_secure_port() {
        return this.display_secure_port;
    }

    public void setdisplay_secure_port(Integer value) {
        this.display_secure_port = value;
    }

    public Integer getutc_diff() {
        return this.utc_diff;
    }

    public void setutc_diff(Integer value) {
        this.utc_diff = value;
    }

    public NGuid getlast_vds_run_on() {
        return this.last_vds_run_on;
    }

    public void setlast_vds_run_on(NGuid value) {
        this.last_vds_run_on = value;
    }

    public String getclient_ip() {
        return this.client_ip;
    }

    public void setclient_ip(String value) {
        this.client_ip = value;
    }

    public Integer getguest_requested_memory() {
        return this.guest_requested_memory;
    }

    public void setguest_requested_memory(Integer value) {
        this.guest_requested_memory = value;
    }

    public String gethibernation_vol_handle() {
        return this.hibernation_vol_handle;
    }

    public void sethibernation_vol_handle(String value) {
        this.hibernation_vol_handle = value;
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
