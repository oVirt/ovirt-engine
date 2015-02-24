package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class GlusterServer implements BusinessEntity<Guid> {

    private static final long serialVersionUID = -1425566208615075937L;

    private Guid serverId;

    private ArrayList<String> knownAddresses;

    private Guid glusterServerUuid;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getId().hashCode();
        result = prime * result + ((getGlusterServerUuid() == null) ? 0 : getGlusterServerUuid().hashCode());
        result = prime * result + ((getKnownAddresses() == null) ? 0 : getKnownAddresses().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlusterServer)) {
            return false;
        }

        GlusterServer entity = (GlusterServer) obj;
        return ObjectUtils.objectsEqual(getId(), entity.getId())
                && ObjectUtils.objectsEqual(getGlusterServerUuid(), entity.getGlusterServerUuid())
                && ObjectUtils.objectsEqual(getKnownAddresses(), entity.getKnownAddresses());
    }
}
