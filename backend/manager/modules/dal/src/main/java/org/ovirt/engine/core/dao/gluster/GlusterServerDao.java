package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.dao.GenericDao;

public interface GlusterServerDao extends Dao, GenericDao<GlusterServer, Guid> {

    public GlusterServer getByServerId(Guid serverId);

    public GlusterServer getByGlusterServerUuid(Guid glusterServerUuid);

    public void removeByGlusterServerUuid(Guid glusterServerUuid);

    public void addKnownAddress(Guid serverId, String address);

    public void updateKnownAddresses(Guid serverId, List<String> addresses);

    public void updatePeerStatus(Guid serverId, PeerStatus peerStatus);
}
