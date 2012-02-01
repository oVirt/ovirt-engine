package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.Version;

public class VdsVersion implements Serializable {
    private static final long serialVersionUID = -3138828435468456070L;
    private static final LogCompat log = LogFactoryCompat.getLog(VdsVersion.class);
    private String softwareVersion;
    private String softwareRevision;
    private String buildName;
    private String versionName;
    private Version mFullVersion;

    public VdsVersion() {
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String value) {
        softwareVersion = value;
    }

    public String getSoftwareRevision() {
        return softwareRevision;
    }

    public void setSoftwareRevision(String value) {
        softwareRevision = value;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String value) {
        buildName = value;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String value) {
        versionName = value;
    }

    /**
     * Version in .Net style a.b.c.d when:
     * <ul>
     * <li>a: major version</li>
     * <li>b: minor version</li>
     * <li>c: major revision</li>
     * <li>d: minor revision</li>
     * <ul>
     * <p>
     * Assumption: VDS version in format: x.y where x = major version, y= minor version. There is no major revision
     * received from VDS - always 0
     */
    public Version getFullVersion() {
        if (mFullVersion == null) {
            // defensive code for prevent incorrect versioning
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
            } catch (Exception e) {
                log.info(StringFormat.format("Couldn't parse vds version: %s , %s",
                        getSoftwareVersion(),
                        getSoftwareRevision()));
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

}
