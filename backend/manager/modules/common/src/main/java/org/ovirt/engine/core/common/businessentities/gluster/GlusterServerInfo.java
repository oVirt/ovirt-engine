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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUuid() == null) ? 0 : getUuid().hashCode());
        result = prime * result + ((getHostnameOrIp() == null) ? 0 : getHostnameOrIp().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlusterServerInfo)) {
            return false;
        }

        GlusterServerInfo host = (GlusterServerInfo) obj;
        return (Objects.equals(getUuid(), host.getUuid())
                && (Objects.equals(getHostnameOrIp(), host.getHostnameOrIp()))
                && (Objects.equals(getStatus(), host.getStatus())));
    }

    @Override
    public String toString() {
        return getHostnameOrIp() + ":" + getStatus().name();
    }

}
