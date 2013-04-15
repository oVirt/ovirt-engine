package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ObjectUtils;
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

    @Size(max = BusinessEntitiesDefinitions.USER_EMAIL_SIZE)
    private String email;

    @Size(max = BusinessEntitiesDefinitions.USER_NOTE_SIZE)
    private String note = "";

    private int status;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((department == null) ? 0 : department.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + (lastAdminCheckStatus ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((note == null) ? 0 : note.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + status;
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
        result = prime * result + ((userIconPath == null) ? 0 : userIconPath.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
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
        DbUser other = (DbUser) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(department, other.department)
                && ObjectUtils.objectsEqual(domain, other.domain)
                && ObjectUtils.objectsEqual(email, other.email)
                && ObjectUtils.objectsEqual(groups, other.groups)
                && lastAdminCheckStatus == other.lastAdminCheckStatus
                && ObjectUtils.objectsEqual(name, other.name)
                && ObjectUtils.objectsEqual(note, other.note)
                && ObjectUtils.objectsEqual(role, other.role)
                && status == other.status
                && ObjectUtils.objectsEqual(surname, other.surname)
                && ObjectUtils.objectsEqual(userIconPath, other.userIconPath)
                && ObjectUtils.objectsEqual(username, other.username));
    }

    public String getdepartment() {
        return this.department;
    }

    public void setdepartment(String value) {
        this.department = value;
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
