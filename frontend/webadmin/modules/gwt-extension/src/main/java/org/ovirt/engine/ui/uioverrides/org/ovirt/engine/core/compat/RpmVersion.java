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
        if (rpmRevision == null) {
            if (other.rpmRevision != null)
                return false;
        } else if (!rpmRevision.equals(other.rpmRevision))
            return false;
        return true;
    }

    private String rpmName;
    private String rpmRevision;

    public String getRpmName() {
        return this.rpmName;
    }

    public void setRpmName(String rpmName) {
        this.rpmName = rpmName;
    }

    public String getRpmRevision() {
        return this.rpmRevision;
    }

    public void setRpmRevision(String rpmRevision) {
        this.rpmRevision = rpmRevision;
    }


}
