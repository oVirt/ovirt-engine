package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

/**
 * An object representing the mapping between the current profile on a vnic of an OVF of a VM
 * (source) and the one desired by the user (target).
 * Used to relay the user mappings from the REST-API and the web-admin layers to the backend
 * for processing.
 */
public class ExternalVnicProfileMapping implements Serializable {

    private String sourceProfileName;
    private String sourceNetworkName;
    private String targetProfileName;
    private String targetNetworkName;
    private Guid targetProfileId;

    private ExternalVnicProfileMapping() {
    }

    public ExternalVnicProfileMapping(String sourceNetworkName, String sourceProfileName) {
        setSourceNetworkName(sourceNetworkName);
        setSourceProfileName(sourceProfileName);
    }

    public ExternalVnicProfileMapping(String sourceNetworkName,
            String sourceNetworkProfileName,
            Guid targetVnicProfileId) {
        setSourceNetworkName(sourceNetworkName);
        setSourceProfileName(sourceNetworkProfileName);
        setTargetProfileId(targetVnicProfileId);
    }

    public ExternalVnicProfileMapping(String sourceNetworkName,
            String sourceProfileName, String targetNetworkName, String targetProfileName) {
        setSourceNetworkName(sourceNetworkName);
        setSourceProfileName(sourceProfileName);
        setTargetProfileName(targetProfileName);
        setTargetNetworkName(targetNetworkName);
    }

    public void setSourceNetworkName(String name) {
        sourceNetworkName = name;
    }

    public void setTargetNetworkName(String name) {
        targetNetworkName = name;
    }

    public String getSourceNetworkName() {
        return sourceNetworkName;
    }

    public String getTargetNetworkName() {
        return targetNetworkName;
    }

    public String getSourceProfileName() {
        return sourceProfileName;
    }

    public String getTargetProfileName() {
        return targetProfileName;
    }

    public void setSourceProfileName(String name) {
        sourceProfileName = name;
    }

    public void setTargetProfileName(String name) {
        targetProfileName = name;
    }

    public Guid getTargetProfileId() {
        return targetProfileId;
    }

    public void setTargetProfileId(Guid id) {
        targetProfileId = id;
    }

    public boolean hasTarget() {
        return hasTargetId() || hasTargetNames();
    }

    public boolean hasTargetId() {
        return getTargetProfileId() != null;
    }

    public boolean hasTargetNames() {
        return getTargetProfileName() != null && getTargetNetworkName() != null;
    }

    public boolean targetNamesAreEmptyString() {
        return "".equals(getTargetProfileName()) && "".equals(getTargetNetworkName());
    }

    /**
     * Comparison used by the webadmin ui code to compare mappings based on similar source
     * network name and profile name.
     * @return true if this has same source network name and profile name as other
     */
    public boolean isSameSourceProfile(ExternalVnicProfileMapping other) {
        return Objects.equals(getSourceNetworkName(), other.getSourceNetworkName()) &&
                Objects.equals(getSourceProfileName(), other.getSourceProfileName());
    }

    /**
     * Comparison used by the backend to compare mappings.
     * External mapping supplied by the user may include empty string to denote 'no profile',
     * whereas in {@link VmNetworkInterface} 'no profile' is denoted with {@code null}s.
     * So apart from a standard equals(), compare the empty string mapping to null on the vnic.
     */
    public boolean isSameSourceProfile(VmNetworkInterface vnic) {
        return (Objects.equals(getSourceNetworkName(), vnic.getNetworkName()) ||
                ("".equals(getSourceNetworkName()) && vnic.getNetworkName() == null)) &&
                (Objects.equals(getSourceProfileName(), vnic.getVnicProfileName()) ||
                        ("".equals(getSourceProfileName()) && vnic.getVnicProfileName() == null));
    }

     /**
     * warning: this equals only compares the source profile. it is used implicitly
      * in the UI when adding and removing from a set.
     * @return true if the source profile of o is the same as that of this
     */
   @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalVnicProfileMapping)) {
            return false;
        }
        final ExternalVnicProfileMapping that = (ExternalVnicProfileMapping) o;
        return isSameSourceProfile(that);
    }

    public boolean equalsEntire(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalVnicProfileMapping)) {
            return false;
        }
        ExternalVnicProfileMapping that = (ExternalVnicProfileMapping) o;
        return Objects.equals(getSourceProfileName(), that.getSourceProfileName()) &&
                Objects.equals(getSourceNetworkName(), that.getSourceNetworkName()) &&
                Objects.equals(getTargetProfileName(), that.getTargetProfileName()) &&
                Objects.equals(getTargetNetworkName(), that.getTargetNetworkName()) &&
                Objects.equals(getTargetProfileId(), that.getTargetProfileId());
    }

    public int hashCodeEntire() {
        return Objects.hash(getSourceProfileName(), getSourceNetworkName(), getTargetProfileName(), getTargetNetworkName(), getTargetProfileId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceNetworkName(), getSourceProfileName());
    }
}
