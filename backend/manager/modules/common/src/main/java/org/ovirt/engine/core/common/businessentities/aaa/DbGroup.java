package org.ovirt.engine.core.common.businessentities.aaa;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Guid;

public class DbGroup implements IVdcQueryable {
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

    public DbGroup() {
        memberOf = new HashSet<>();
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
        return Objects.hash(
                id,
                externalId,
                namespace,
                domain,
                name,
                distinguishedName,
                memberOf
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DbGroup)) {
            return false;
        }
        DbGroup other = (DbGroup) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(externalId, other.externalId)
                && Objects.equals(domain, other.domain)
                && Objects.equals(name, other.name)
                && Objects.equals(distinguishedName, other.distinguishedName)
                && Objects.equals(memberOf, other.memberOf);
    }
}
