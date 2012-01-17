package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsVersion")
public class VdsVersion implements Serializable {
    private static final long serialVersionUID = -3138828435468456070L;
    private String softwareVersion;
    private String softwareRevision;
    private String buildName;
    private String versionName;
    private Version mFullVersion;

    @XmlElement(name = "SoftwareVersion")
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String value) {
        softwareVersion = value;
    }

    @XmlElement(name = "SoftwareRevision")
    public String getSoftwareRevision() {
        return softwareRevision;
    }

    public void setSoftwareRevision(String value) {
        softwareRevision = value;
    }

    @XmlElement(name = "BuildName")
    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String value) {
        buildName = value;
    }

    @XmlElement(name = "VersionName")
    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String value) {
        versionName = value;
    }

    /**
     * Version in .Net style: a.b.c.d when a: major version, b: minor version , c: major revision, d: minor revision
     * assumption: Vds version in format: x.y where x = major version, y= minor version. there is no major revision
     * recieved from vds - always 0
     */
    public Version getFullVersion() {
        if (mFullVersion == null) {
            // defencive code for prevent incorrect versioning
            try {
                if (getSoftwareVersion() != null && getSoftwareRevision() != null) {
                    String stringVersion;
                    String[] revision = getSoftwareRevision().split("[.]", -1);

                    if (revision.length > 1) {
                        stringVersion = StringFormat.format("%s.%s", getSoftwareVersion(), getSoftwareRevision());
                    } else {
                        stringVersion = StringFormat.format("%s.0.%s", getSoftwareVersion(), getSoftwareRevision());
                    }

                    mFullVersion = new Version(stringVersion);
                }
            } catch (java.lang.Exception e) {
                log.warnFormat("Couldn't parse vds version: {0} , {1}", getSoftwareVersion(), getSoftwareRevision());
            }
        }

        return mFullVersion;
    }

    public Version getPartialVersion() {
        return getSoftwareVersion() == null ? null : new Version(getSoftwareVersion());
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        VdsVersion other = (VdsVersion) ((obj instanceof VdsVersion) ? obj : null);
        if (other == null) {
            return false;
        }
        return (getFullVersion() == null && other.getFullVersion() == null)
                || (getFullVersion() != null && getFullVersion().equals(other.getFullVersion()));
    }

    @Override
    public int hashCode() {
        return getFullVersion() != null ? getFullVersion().hashCode() : -1;
    }

    private static LogCompat log = LogFactoryCompat.getLog(VdsVersion.class);

    public VdsVersion() {
    }
}
