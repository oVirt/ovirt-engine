package org.ovirt.engine.core.common.businessentities.aaa;

import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

public class DbGroup implements Queryable {
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
    @NotNull
    private String domain;
    @NotNull
    private String name;
    @NotNull
    private String distinguishedName;
    private Set<String> memberOf;

    public DbGroup() {
        domain = "";
        name = "";
        distinguishedName = "";

    }

    public DbGroup(DirectoryGroup directoryGroup) {
        externalId = directoryGroup.getId();
        setId(new Guid(directoryGroup.getId().getBytes()));
        namespace = directoryGroup.getNamespace();
        setDomain(directoryGroup.getDirectoryName());
        setName(directoryGroup.getName());
        distinguishedName = "";
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
        domain = value == null ? "" : value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value == null ? "" : value;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName == null ? "" : distinguishedName;
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
