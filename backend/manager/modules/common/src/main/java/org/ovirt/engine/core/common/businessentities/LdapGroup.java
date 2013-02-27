package org.ovirt.engine.core.common.businessentities;

import java.util.List;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class LdapGroup extends IVdcQueryable {
    private static final long serialVersionUID = 6717840754119287059L;

    private Guid id = new Guid();

    private String name;

    private LdapRefStatus status = LdapRefStatus.Inactive;

    private String domain;

    private List<String> memberOf;

    private String distinguishedName;

    public LdapGroup() {
        this.status = LdapRefStatus.Active;
        id = Guid.Empty;
        name = "";
        distinguishedName = "";
    }

    public LdapGroup(Guid id, String name, int status) {
        this.id = id;
        this.name = name;
        this.status = LdapRefStatus.forValue(status);
    }

    public Guid getid() {
        return this.id;
    }

    public void setid(Guid value) {
        this.id = value;
    }

    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        this.name = value;
    }

    public LdapRefStatus getstatus() {
        return status;
    }

    public void setstatus(LdapRefStatus value) {
        status = value;
    }

    public LdapGroup(Guid id, String name) {
        this.id = id;
        this.name = name;
    }

    public LdapGroup(Guid id, String name, String domain) {
        this(id, name);
        this.domain = domain;
    }

    public LdapGroup(Guid id, String name, String domain, String distinguishedName) {
        this(id, name, domain);
        this.distinguishedName = distinguishedName;
    }

    public LdapGroup(Guid id, String name, String domain, String distinguishedName, List<String> memberOf) {
        this(id, name, domain, distinguishedName);
        this.setMemberOf(memberOf);
    }

    /**
     * This constructor used only for Inactive groups
     *
     * @param id
     */
    public LdapGroup(Guid id) {
        this.id = id;
        status = LdapRefStatus.Inactive;
    }

    public String getdomain() {
        return domain;
    }

    public void setdomain(String value) {
        domain = value;
    }

    @Override
    public Object getQueryableId() {
        return getid();
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setMemberOf(List<String> memberOf) {
        this.memberOf = memberOf;
    }

    public List<String> getMemberOf() {
        return memberOf;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((distinguishedName == null) ? 0 : distinguishedName.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((memberOf == null) ? 0 : memberOf.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
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
        LdapGroup other = (LdapGroup) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(distinguishedName, other.distinguishedName)
                && ObjectUtils.objectsEqual(domain, other.domain)
                && ObjectUtils.objectsEqual(memberOf, other.memberOf)
                && ObjectUtils.objectsEqual(name, other.name)
                && status == other.status);
    }
}
