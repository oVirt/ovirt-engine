package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

/**
 * The Volume status service info.
 *
 * This will store the general information about nfsServices and shdServices
 *
 */
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = 108478798053613345L;

    private ServiceType serviceType;
    private String hostName;
    private int port;
    private int pid;
    private GlusterStatus status;

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public GlusterStatus getStatus() {
        return status;
    }

    public void setStatus(GlusterStatus status) {
        this.status = status;
    }
}
