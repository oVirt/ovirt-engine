package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;

/**
 * This class encapsulates advanced details of a brick, fetched using the 'gluster volume status' command
 *
 * @see BrickProperties
 * @see GlusterClientInfo
 * @see MemoryStatus
 */
public class BrickDetails implements Serializable {

    private static final long serialVersionUID = -1134758927239004412L;

    private BrickProperties brickProperties;
    private List<GlusterClientInfo> clients;
    private MemoryStatus memoryStatus;

    public BrickProperties getBrickProperties() {
        return brickProperties;
    }

    public void setBrickProperties(BrickProperties brickProperties) {
        this.brickProperties = brickProperties;
    }

    public List<GlusterClientInfo> getClients() {
        return clients;
    }

    public void setClients(List<GlusterClientInfo> clients) {
        this.clients = clients;
    }

    public MemoryStatus getMemoryStatus() {
        return memoryStatus;
    }

    public void setMemoryStatus(MemoryStatus memoryStatus) {
        this.memoryStatus = memoryStatus;
    }
}
