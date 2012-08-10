package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.compat.Guid;

public final class GlusterServersListReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String GLUSTER_HOSTS = "hosts";

    private static final String HOST_NAME = "hostname";

    private static final String UUID = "uuid";

    private static final String PEER_STATUS = "status";

    private Set<GlusterServerInfo> servers;

    public Set<GlusterServerInfo> getServers() {
        return servers;
    }

    public void setServer(Set<GlusterServerInfo> servers) {
        this.servers = servers;
    }

    public GlusterServersListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Set<GlusterServerInfo> glusterServers = new HashSet<GlusterServerInfo>();
        Object[] serversArr = (Object[]) innerMap.get(GLUSTER_HOSTS);

        if (serversArr != null) {
            for (int i = 0; i < serversArr.length; i++) {
                glusterServers.add(prepareServerInfo((Map<String, Object>) serversArr[i]));
            }
        }
        setServer(glusterServers);
    }

    private GlusterServerInfo prepareServerInfo(Map<String, Object> map) {
        GlusterServerInfo entity = new GlusterServerInfo();
        entity.setHostnameOrIp(map.get(HOST_NAME).toString());
        entity.setUuid(new Guid(map.get(UUID).toString()));

        String status = map.get(PEER_STATUS).toString();
        entity.setStatus(PeerStatus.valueOf(status));
        return entity;
    }

}
