package org.ovirt.engine.ui.uicommonweb.models.vms.register;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.compat.Guid;

/**
 * The class wraps an <code>ExternalVnicProfileMapping</code> and adds the indication of whether it was changed or not.
 * Using <code>ExternalVnicProfileMapping.vnicProfileId == null</code> isn't an option as null is one of the valid
 * values for the field.
 */
public class VnicProfileMappingEntity {
    private boolean changed;
    private final ExternalVnicProfileMapping externalVnicProfileMapping;

    public VnicProfileMappingEntity(String externalNetworkName,
            String externalNetworkProfileName,
            Guid vnicProfileId) {
        this.externalVnicProfileMapping =
                new ExternalVnicProfileMapping(externalNetworkName, externalNetworkProfileName, vnicProfileId);
    }

    public VnicProfileMappingEntity(VnicProfileMappingEntity that) {
        this(that.getExternalNetworkName(), that.getExternalNetworkProfileName(), that.getVnicProfileId());
        this.changed = that.changed;
    }

    public void setVnicProfileId(Guid vnicProfileId) {
        externalVnicProfileMapping.setVnicProfileId(vnicProfileId);
        setChanged();
    }

    public String getExternalNetworkName() {
        return externalVnicProfileMapping.getExternalNetworkName();
    }

    public String getExternalNetworkProfileName() {
        return externalVnicProfileMapping.getExternalNetworkProfileName();
    }

    public Guid getVnicProfileId() {
        return externalVnicProfileMapping.getVnicProfileId();
    }

    public boolean isChanged() {
        return changed;
    }

    private void setChanged() {
        this.changed = true;
    }

    public ExternalVnicProfileMapping getExternalVnicProfileMapping() {
        return externalVnicProfileMapping;
    }

    public boolean isSameSourceProfile(VnicProfileMappingEntity other) {
        return externalVnicProfileMapping.isSameSourceProfile(other.externalVnicProfileMapping);
    }

    /**
     * warning: this equals only compares the source profile
     * @return true if the source profile of o is the same as that of this
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VnicProfileMappingEntity)) {
            return false;
        }
        final VnicProfileMappingEntity that = (VnicProfileMappingEntity) o;
        return isSameSourceProfile(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalVnicProfileMapping);
    }
}
