package org.ovirt.engine.core.bll.aaa;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

public class DirectoryEntryKey {

    private String authz;
    private String externalId;

    public DirectoryEntryKey(String authz, String externalId) {
        this.authz = authz;
        this.externalId = externalId;
    }

    public DirectoryEntryKey(DbUser dbUser) {
        this(dbUser.getDomain(), dbUser.getExternalId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                authz,
                externalId
        );
    }

    public String getAuthz() {
        return authz;
    }

    public String getExternalId() {
        return externalId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DirectoryEntryKey)) {
            return false;
        }
        DirectoryEntryKey other = (DirectoryEntryKey) obj;
        return Objects.equals(authz, other.authz)
                && Objects.equals(externalId, other.externalId);
    }

}

