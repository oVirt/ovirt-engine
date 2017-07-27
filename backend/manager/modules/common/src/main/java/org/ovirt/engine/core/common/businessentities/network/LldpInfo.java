package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * The entity `LldpInfo` has a list of all `Tlv`s of an interface and shows if the
 * gathering of the LLDP information is `enabled` on the interface. If it is
 * not `enabled`, there is a problem gathering the LLDP information on the interface.
 * Problems on gathering LLDP information might be that the interface is down or
 * LLDP is not enabled on the interface.
 */
public class LldpInfo implements Serializable {
    private static final long serialVersionUID = 6324656959415566547L;
    private boolean enabled = false;
    private List<Tlv> tlvs = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Tlv> getTlvs() {
        return tlvs;
    }

    public void setTlvs(List<Tlv> tlvs) {
        this.tlvs = Objects.requireNonNull(tlvs);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("enabled", enabled)
                .append("tlvs", tlvs)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LldpInfo)) {
            return false;
        }
        LldpInfo lldpInfo = (LldpInfo) o;
        return enabled == lldpInfo.enabled &&
                Objects.equals(tlvs, lldpInfo.tlvs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, tlvs);
    }
}
