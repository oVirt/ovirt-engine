package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;


/**
 * The gluster volume status clients info.
 */
public class GlusterClientInfo implements Serializable {

    private static final long serialVersionUID = 4426819375609665363L;

    private String hostname;

    private int clientPort;

    private int bytesRead;

    private int bytesWritten;

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

    public int getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }

    public int getBytesWritten() {
        return bytesWritten;
    }

    public void setBytesWritten(int bytesWritten) {
        this.bytesWritten = bytesWritten;
    }
}
