package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class GlusterServerInfo {

    private Guid uuid;

    private String hostnameOrIp;

    private PeerStatus status;

    public GlusterServerInfo() {
    }

    public GlusterServerInfo(Guid uuid, String hostnameOrIp, PeerStatus status) {
        setUuid(uuid);
        setHostnameOrIp(hostnameOrIp);
        setStatus(status);
    }

    public Guid getUuid() {
        return uuid;
    }

    public void setUuid(Guid uuid) {
        this.uuid = uuid;
    }

    public String getHostnameOrIp() {
        return hostnameOrIp;
    }

    public void setHostnameOrIp(String hostnameOrIp) {
        this.hostnameOrIp = hostnameOrIp;
    }

    public PeerStatus getStatus() {
        return status;
    }

    public void setStatus(PeerStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                uuid,
                hostnameOrIp,
                status
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterServerInfo)) {
            return false;
        }

        GlusterServerInfo host = (GlusterServerInfo) obj;
        return Objects.equals(uuid, host.uuid)
                && Objects.equals(hostnameOrIp, host.hostnameOrIp)
                && Objects.equals(status, host.status);
    }

    @Override
    public String toString() {
        return getHostnameOrIp() + ":" + getStatus().name();
    }

}
