package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.utils.ObjectUtils;

public class LdapGroup extends IVdcQueryable {
    private static final long serialVersionUID = 6717840754119287059L;

    private String id;

    private String name;

    private String domain;

    private List<String> memberOf;

    private String distinguishedName;

    public LdapGroup() {
        name = "";
        distinguishedName = "";
    }

    public LdapGroup(DbGroup dbGroup) {
        id = dbGroup.getExternalId();
        name = dbGroup.getName();
        domain = dbGroup.getDomain();
        distinguishedName = dbGroup.getDistinguishedName();
        memberOf = dbGroup.getMemberOf() != null ? new ArrayList<String>(dbGroup.getMemberOf()) : null;
    }

    public String getid() {
        return this.id;
    }

    public void setid(String value) {
        this.id = value;
    }

    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        this.name = value;
    }

    public LdapGroup(String id, String name, String domain) {
        this (id);
        this.name = name;
        this.domain = domain;
    }

    public LdapGroup(String id, String name, String domain, String distinguishedName, List<String> memberOf) {
        this(id, name, domain);
        this.distinguishedName = distinguishedName;
        this.setMemberOf(memberOf);
    }

    public LdapGroup(String id) {
        this.id = id;
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
                && ObjectUtils.objectsEqual(name, other.name));
    }
}
