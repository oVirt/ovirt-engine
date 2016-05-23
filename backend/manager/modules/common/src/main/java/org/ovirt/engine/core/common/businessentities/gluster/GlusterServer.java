package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.ArrayList;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterServer implements BusinessEntity<Guid> {

    private static final long serialVersionUID = -1425566208615075937L;

    private Guid serverId;

    private ArrayList<String> knownAddresses;

    private Guid glusterServerUuid;

    private PeerStatus peerStatus;

    public GlusterServer() {
        knownAddresses = new ArrayList<>();
    }

    public GlusterServer(Guid serverId, Guid glusterServerUuid) {
        this();
        setId(serverId);
        setGlusterServerUuid(glusterServerUuid);
    }

    @Override
    public Guid getId() {
        return serverId;
    }

    @Override
    public void setId(Guid id) {
        this.serverId = id;
    }

    public Guid getGlusterServerUuid() {
        return glusterServerUuid;
    }

    public void setGlusterServerUuid(Guid serverUuid) {
        this.glusterServerUuid = serverUuid;
    }

    public ArrayList<String> getKnownAddresses() {
        return knownAddresses;
    }

    public void setKnownAddresses(ArrayList<String> knownAddresses) {
        this.knownAddresses = knownAddresses;
    }

    public PeerStatus getPeerStatus() {
        return peerStatus;
    }

    public void setPeerStatus(PeerStatus peerStatus) {
        this.peerStatus = peerStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                serverId,
                glusterServerUuid,
                knownAddresses,
                peerStatus
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterServer)) {
            return false;
        }

        GlusterServer entity = (GlusterServer) obj;
        return Objects.equals(serverId, entity.serverId)
                && Objects.equals(glusterServerUuid, entity.glusterServerUuid)
                && Objects.equals(knownAddresses, entity.knownAddresses)
                && Objects.equals(peerStatus, entity.peerStatus);
    }
}
