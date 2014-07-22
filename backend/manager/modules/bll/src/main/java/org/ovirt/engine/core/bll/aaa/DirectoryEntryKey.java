package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.common.businessentities.DbUser;


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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((authz == null) ? 0 : authz.hashCode());
        result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
        return result;
    }

    public String getAuthz() {
        return authz;
    }

    public String getExternalId() {
        return externalId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DirectoryEntryKey other = (DirectoryEntryKey) obj;
        if (authz == null) {
            if (other.authz != null)
                return false;
        } else if (!authz.equals(other.authz))
            return false;
        if (externalId == null) {
            if (other.externalId != null)
                return false;
        } else if (!externalId.equals(other.externalId))
            return false;
        return true;
    }

}

