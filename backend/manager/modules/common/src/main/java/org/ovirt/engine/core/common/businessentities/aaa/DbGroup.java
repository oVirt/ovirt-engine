package org.ovirt.engine.core.common.businessentities.aaa;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class DbGroup extends IVdcQueryable {
    private static final long serialVersionUID = 6717840754119287059L;

    /**
     * This is the identifier assigned by the engine to this group for internal use only.
     */
    private Guid id;

    /**
     * This is the identifier assigned by the external directory to this group.
     */
    private String externalId;
    private String namespace;
    private String domain;
    private String name;
    private String distinguishedName;
    private Set<String> memberOf;

    /**
     * This flag indicates if the user was available in the directory the last time that it was checked, so {@code true}
     * means it was available and {@code false} means it wasn't.
     */
    private boolean active;

    public DbGroup() {
        memberOf = new HashSet<String>();
    }

    public DbGroup(DirectoryGroup directoryGroup) {
        externalId = directoryGroup.getId();
        setId(new Guid(directoryGroup.getId().getBytes(), true));
        namespace = directoryGroup.getNamespace();
        domain = directoryGroup.getDirectoryName();
        name = directoryGroup.getName();
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid value) {
        id = value;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String value) {
        externalId = value;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String value) {
        namespace = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        domain = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean value) {
        active = value;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setMemberOf(Set<String> memberOf) {
        this.memberOf = memberOf;
    }

    public Set<String> getMemberOf() {
        return memberOf;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((distinguishedName == null) ? 0 : distinguishedName.hashCode());
        result = prime * result + ((memberOf == null) ? 0 : memberOf.hashCode());
        result = prime * result + (active ? 1231 : 1237);
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
        DbGroup other = (DbGroup) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(externalId, other.externalId)
                && ObjectUtils.objectsEqual(domain, other.domain)
                && ObjectUtils.objectsEqual(name, other.name)
                && ObjectUtils.objectsEqual(distinguishedName, other.distinguishedName)
                && ObjectUtils.objectsEqual(memberOf, other.memberOf)
                && active == other.active);
    }
}
