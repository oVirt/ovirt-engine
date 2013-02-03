package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class DbUser extends IVdcQueryable {
    private static final long serialVersionUID = 7052102138405696755L;

    private Guid id = new Guid();

    @Size(max = BusinessEntitiesDefinitions.USER_NAME_SIZE)
    private String name = "";

    @Size(max = BusinessEntitiesDefinitions.USER_SURENAME_SIZE)
    private String surname = "";

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_DOMAIN_SIZE)
    private String domain;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_USER_NAME_SIZE)
    private String username = "";

    @Size(min = 1, max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String groups;

    @Size(max = BusinessEntitiesDefinitions.USER_DEPARTMENT_SIZE)
    private String department = "";

    @Size(max = BusinessEntitiesDefinitions.USER_ROLE_SIZE)
    private String role = "";

    @Size(max = BusinessEntitiesDefinitions.USER_ICON_PATH_SIZE)
    private String userIconPath = "";

    @Size(max = BusinessEntitiesDefinitions.USER_DESKTOP_DEVICE_SIZE)
    private String desktopDevice = "";

    @Size(max = BusinessEntitiesDefinitions.USER_EMAIL_SIZE)
    private String email;

    @Size(max = BusinessEntitiesDefinitions.USER_NOTE_SIZE)
    private String note = "";

    private int status;

    private int sessionCount;

    private boolean isLoggedIn;

    /**
     * GUI flag only. Do not use for internal logic. The sole purpose of calculating this field is for the GUI user to
     * understand who is admin in a snap on the user-grid
     */
    private boolean lastAdminCheckStatus;

    /**
     * comma delimited list of group guids
     */
    @Size(max = BusinessEntitiesDefinitions.USER_GROUP_IDS_SIZE)
    private String groupIds;

    public DbUser() {
    }

    public DbUser(String department, String desktop_device, String domain, String email, String groups, String name,
            String note, String role, int status, String surname, String user_icon_path, Guid user_id, String username,
            int sessionCount, String groupIds) {
        this.department = department;
        this.desktopDevice = desktop_device;
        this.domain = domain;
        this.email = email;
        this.groups = groups;
        this.name = name;
        this.note = note;
        this.role = role;
        this.status = status;
        this.surname = surname;
        this.userIconPath = user_icon_path;
        this.id = user_id;
        this.username = username;
        this.sessionCount = sessionCount;
        this.setGroupIds(groupIds);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((department == null) ? 0 : department.hashCode());
        result = prime
                * result
                + ((desktopDevice == null) ? 0 : desktopDevice
                        .hashCode());
        result = prime * result
                + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result
                + ((email == null) ? 0 : email.hashCode());
        result = prime * result
                + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + (isLoggedIn ? 1231 : 1237);
        result = prime * result + (lastAdminCheckStatus ? 1231 : 1237);
        result = prime * result
                + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((note == null) ? 0 : note.hashCode());
        result = prime * result
                + ((role == null) ? 0 : role.hashCode());
        result = prime * result + sessionCount;
        result = prime * result + status;
        result = prime * result
                + ((surname == null) ? 0 : surname.hashCode());
        result = prime
                * result
                + ((userIconPath == null) ? 0 : userIconPath
                        .hashCode());
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((username == null) ? 0 : username.hashCode());
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
        DbUser other = (DbUser) obj;
        if (department == null) {
            if (other.department != null)
                return false;
        } else if (!department.equals(other.department))
            return false;
        if (desktopDevice == null) {
            if (other.desktopDevice != null)
                return false;
        } else if (!desktopDevice.equals(other.desktopDevice))
            return false;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (groups == null) {
            if (other.groups != null)
                return false;
        } else if (!groups.equals(other.groups))
            return false;
        if (isLoggedIn != other.isLoggedIn)
            return false;
        if (lastAdminCheckStatus != other.lastAdminCheckStatus)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (note == null) {
            if (other.note != null)
                return false;
        } else if (!note.equals(other.note))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        if (sessionCount != other.sessionCount)
            return false;
        if (status != other.status)
            return false;
        if (surname == null) {
            if (other.surname != null)
                return false;
        } else if (!surname.equals(other.surname))
            return false;
        if (userIconPath == null) {
            if (other.userIconPath != null)
                return false;
        } else if (!userIconPath.equals(other.userIconPath))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    public String getdepartment() {
        return this.department;
    }

    public void setdepartment(String value) {
        this.department = value;
    }

    public String getdesktop_device() {
        return this.desktopDevice;
    }

    public void setdesktop_device(String value) {
        this.desktopDevice = value;
    }

    public String getdomain() {
        return this.domain;
    }

    public void setdomain(String value) {
        this.domain = value;
    }

    public String getemail() {
        return this.email;
    }

    public void setemail(String value) {
        this.email = value;
    }

    public String getgroups() {
        return this.groups;
    }

    public void setgroups(String value) {
        this.groups = value;
    }

    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        this.name = value;
    }

    public String getnote() {
        return this.note;
    }

    public void setnote(String value) {
        this.note = value;
    }

    public String getrole() {
        return this.role;
    }

    public void setrole(String value) {
        this.role = value;
    }

    public int getstatus() {
        return this.status;
    }

    public void setstatus(int value) {
        this.status = value;
    }

    public String getsurname() {
        return this.surname;
    }

    public void setsurname(String value) {
        this.surname = value;
    }

    public String getuser_icon_path() {
        return this.userIconPath;
    }

    public void setuser_icon_path(String value) {
        this.userIconPath = value;
    }

    public Guid getuser_id() {
        return this.id;
    }

    public void setuser_id(Guid value) {
        this.id = value;
    }

    public String getusername() {
        return this.username;
    }

    public void setusername(String value) {
        this.username = value;
    }

    public int getsession_count() {
        return sessionCount;
    }

    public void setsession_count(int value) {
        sessionCount = value;
        setIsLogedin((sessionCount > 0));
    }

    public boolean getIsLogedin() {
        return isLoggedIn;
    }

    public void setIsLogedin(boolean value) {
        isLoggedIn = value;
    }

    public DbUser(LdapUser ldapUser) {
        setuser_id(ldapUser.getUserId());
        setusername(getFullUserName(ldapUser));
        setname(ldapUser.getName());
        setsurname(ldapUser.getSurName());
        setdepartment(ldapUser.getDepartment());
        setdomain(ldapUser.getDomainControler());
        setemail(ldapUser.getEmail());
        setgroups(ldapUser.getGroup());
        setstatus(LdapRefStatus.Active.getValue());
        setGroupIds(ldapUser.getGroupIds());
    }

    private String getFullUserName(LdapUser ldapUser) {
        String userName = ldapUser.getUserName();
        if (userName.indexOf("@") == -1) {
            userName = userName +"@"+ ldapUser.getDomainControler();
        }
        return userName;
    }

    public LdapRefStatus getAdStatus() {
        if (getstatus() == 0) {
            return LdapRefStatus.Inactive;
        }
        return LdapRefStatus.Active;
    }

    public boolean getIsGroup() {
        return StringHelper.isNullOrEmpty(getusername());
    }

    public void setIsGroup(boolean value) {
        // do nothing for nothing
    }

    @Override
    public Object getQueryableId() {
        return getuser_id();
    }

    public void setLastAdminCheckStatus(boolean val) {
        this.lastAdminCheckStatus = val;
    }

    public boolean getLastAdminCheckStatus() {
        return lastAdminCheckStatus;
    }

    /**
     * Returns the user's given and family name and username in a standard format.
     *
     * @return the coalesced name
     */
    public String getCoalescedName() {
        return name + " " + surname + " (" + username + ")";
    }

    /**
     * Returns the set of group names as an array.
     *
     * @return the group names
     */
    public String[] getGroupsAsArray() {
        return groups.split(",");
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public String getGroupIds() {
        return groupIds;
    }
}
