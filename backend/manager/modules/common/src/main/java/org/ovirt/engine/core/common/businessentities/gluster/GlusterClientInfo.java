package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;


/**
 * The gluster volume status clients info.
 */
public class GlusterClientInfo implements Serializable {

    private static final long serialVersionUID = 4426819375609665363L;

    private String hostname;

    private int clientPort;

    private long bytesRead;

    private long bytesWritten;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }
}
