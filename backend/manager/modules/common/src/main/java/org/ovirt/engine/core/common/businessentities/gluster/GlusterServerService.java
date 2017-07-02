package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

/**
 * Represents a service on a gluster server
 */
public class GlusterServerService implements Queryable, BusinessEntity<Guid> {

    private static final long serialVersionUID = 108478798053613345L;

    private Guid id;
    private Guid serviceId;
    private ServiceType serviceType;
    private String serviceName;
    private Guid glusterHostUuid;
    private Guid serverId;
    private String hostName;
    private Integer port;
    private Integer pid;
    private GlusterServiceStatus status;
    private String message;
    private Integer rdmaPort;

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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
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

    public Guid getGlusterHostUuid() {
        return glusterHostUuid;
    }

    public void setGlusterHostUuid(Guid glusterHostUuid) {
        this.glusterHostUuid = glusterHostUuid;
    }

    public Guid getServerId() {
        return serverId;
    }

    public void setServerId(Guid hostId) {
        this.serverId = hostId;
    }

    public Integer getRdmaPort() {
        return rdmaPort;
    }

    public void setRdmaPort(Integer rdmaPort) {
        this.rdmaPort = rdmaPort;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getServiceId() {
        return serviceId;
    }

    public void setServiceId(Guid serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterServerService)) {
            return false;
        }

        GlusterServerService other = (GlusterServerService) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(serverId, other.serverId)
                && Objects.equals(serviceId, other.serviceId)
                && status == other.status
                && Objects.equals(message, other.message)
                && Objects.equals(pid, other.pid)
                && serviceType == other.serviceType
                && Objects.equals(hostName, other.hostName)
                && Objects.equals(port, other.port)
                && Objects.equals(rdmaPort, other.rdmaPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                serverId,
                serviceId,
                status,
                message,
                pid,
                serviceType,
                hostName,
                port,
                rdmaPort
        );
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }
}
