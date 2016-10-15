package org.ovirt.engine.core.compat;

import java.util.Objects;

public class RpmVersion extends Version {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RpmVersion other = (RpmVersion) obj;
        if (rpmName == null) {
            if (other.rpmName != null) {
                return false;
            }
        } else if (!rpmName.equals(other.rpmName)) {
            return false;
        }
        if (rpmRelease == null) {
            if (other.rpmRelease != null) {
                return false;
            }
        } else if (!rpmRelease.equals(other.rpmRelease)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rpmName, rpmRelease);
    }

    private String rpmName;
    private String rpmRelease;

    public String getRpmName() {
        return this.rpmName;
    }

    public void setRpmName(String rpmName) {
        this.rpmName = rpmName;
    }

    public String getRpmRelease() {
        return this.rpmRelease;
    }

    public void setRpmRelease(String rpmRelease) {
        this.rpmRelease = rpmRelease;
    }


}
