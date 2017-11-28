package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class ExternalVnicProfileMapping implements Serializable {

    private String externalNetworkName;
    private String externalNetworkProfileName;
    private Guid vnicProfileId;

    private ExternalVnicProfileMapping() {}

    public ExternalVnicProfileMapping(String externalNetworkName,
            String externalNetworkProfileName,
            Guid vnicProfileId) {
        /**
         * In the body of a REST request (e.g. register vm\template requests) an '<Empty>' target vNic profile
         * has "" for network and profile name while in {@link VmNetworkInterface} these fields are denoted with null.
         * So map the empty string to null so that comparisons of the two will succeed.
         * @see ExternalVnicProfileMappingFinder#findMappingEntry
         */
        this.externalNetworkName = "".equals(externalNetworkName) ? null : externalNetworkName;
        this.externalNetworkProfileName = "".equals(externalNetworkProfileName) ? null : externalNetworkProfileName;
        this.vnicProfileId = vnicProfileId;
    }

    public ExternalVnicProfileMapping(ExternalVnicProfileMapping that) {
        this(that.getExternalNetworkName(), that.getExternalNetworkProfileName(), that.getVnicProfileId());
    }

    public String getExternalNetworkName() {
        return externalNetworkName;
    }

    public String getExternalNetworkProfileName() {
        return externalNetworkProfileName;
    }

    public Guid getVnicProfileId() {
        return vnicProfileId;
    }

    public void setVnicProfileId(Guid vnicProfileId) {
        this.vnicProfileId = vnicProfileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalVnicProfileMapping)) {
            return false;
        }
        final ExternalVnicProfileMapping that = (ExternalVnicProfileMapping) o;
        return Objects.equals(externalNetworkName, that.externalNetworkName) &&
                Objects.equals(externalNetworkProfileName, that.externalNetworkProfileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalNetworkName, externalNetworkProfileName);
    }
}
