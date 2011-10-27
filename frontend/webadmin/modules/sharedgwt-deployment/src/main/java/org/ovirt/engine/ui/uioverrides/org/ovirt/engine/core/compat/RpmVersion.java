package org.ovirt.engine.core.compat;

public class RpmVersion extends Version {

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RpmVersion other = (RpmVersion) obj;
        if (rpmName == null) {
            if (other.rpmName != null)
                return false;
        } else if (!rpmName.equals(other.rpmName))
            return false;
        return true;
    }

    private String rpmName;

    public String getRpmName() {
        return this.rpmName;
    }
}
