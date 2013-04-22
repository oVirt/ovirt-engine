package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

/**
 * The Volume status service info.
 *
 * This will store the general information about nfsServices and shdServices
 *
 */
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = 108478798053613345L;

    private ServiceType serviceType;
    private String serviceName;
    private Guid serverId;
    private String hostName;
    private int port;
    private int pid;
    private GlusterServiceStatus status;
    private String message;

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

    public GlusterServiceStatus getStatus() {
        return status;
    }

    public void setStatus(GlusterServiceStatus status) {
        this.status = status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Guid getServerId() {
        return serverId;
    }

    public void setServerId(Guid hostId) {
        this.serverId = hostId;
    }
}
