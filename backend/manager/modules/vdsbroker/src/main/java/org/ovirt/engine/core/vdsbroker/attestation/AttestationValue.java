package org.ovirt.engine.core.vdsbroker.attestation;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.AttestationResultEnum;

public class AttestationValue {

    private String hostName;
    private AttestationResultEnum trustLevel;

    public AttestationValue() {
    }

    public AttestationValue(String hostName, AttestationResultEnum trustLevel) {
        super();
        this.hostName = hostName;
        this.trustLevel = trustLevel;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public AttestationResultEnum getTrustLevel() {
        return trustLevel;
    }

    public void setTrustLevel(AttestationResultEnum trustLevel) {
        this.trustLevel = trustLevel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((hostName == null) ? 0 : hostName.hashCode());
        result = prime * result
                + ((trustLevel == null) ? 0 : trustLevel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || (obj.getClass() != this.getClass()))
            return false;
        AttestationValue other = (AttestationValue) obj;
        return Objects.equals(hostName, other.hostName)
                && Objects.equals(trustLevel, other.trustLevel);
    }

}
