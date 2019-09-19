package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class GlusterServersListReturn extends StatusReturn {

    private static final String GLUSTER_HOSTS = "hosts";

    private static final String HOST_NAME = "hostname";

    private static final String UUID = "uuid";

    private static final String PEER_STATUS = "status";

    private List<GlusterServerInfo> servers = new ArrayList<>();

    public List<GlusterServerInfo> getServers() {
        return servers;
    }

    @SuppressWarnings("unchecked")
    public GlusterServersListReturn(Map<String, Object> innerMap) {
        super(innerMap);

        if (getStatus().code != 0) {
            return;
        }

        Object[] serversArr = (Object[]) innerMap.get(GLUSTER_HOSTS);

        if (serversArr != null) {
            for (Object server : serversArr) {
                this.servers.add(prepareServerInfo((Map<String, Object>) server));
            }
        }
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
