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
        return Objects.hash(
                hostName,
                trustLevel
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AttestationValue)) {
            return false;
        }
        AttestationValue other = (AttestationValue) obj;
        return Objects.equals(hostName, other.hostName)
                && Objects.equals(trustLevel, other.trustLevel);
    }

}
