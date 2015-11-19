package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

/**
 * Status of kdump flow for specific host
 */
public class VdsKdumpStatus {
    /**
     * VDS UUID
     */
    private Guid vdsId;

    /**
     * VDS kdump flow status
     */
    private KdumpFlowStatus status;

    /**
     * Address and port the message came from
     */
    private String address;

    public VdsKdumpStatus() {
        vdsId = Guid.Empty;
        status = null;
        address = null;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public KdumpFlowStatus getStatus() {
        return status;
    }

    public void setStatus(KdumpFlowStatus status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VdsKdumpStatus)) {
            return false;
        }
        VdsKdumpStatus other = (VdsKdumpStatus) obj;

        return Objects.equals(vdsId, other.getVdsId())
                && status == other.getStatus()
                && Objects.equals(address, other.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                vdsId,
                status,
                address
        );
    }
}
