package org.ovirt.engine.core.common.businessentities.gluster;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

/**
 * Represents a service on a gluster server
 */
public class GlusterServerService extends IVdcQueryable implements BusinessEntity<Guid> {

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
        if (!(obj instanceof GlusterServerService)) {
            return false;
        }

        GlusterServerService other = (GlusterServerService) obj;
        if (!(ObjectUtils.objectsEqual(id, other.getId())
                && ObjectUtils.objectsEqual(serverId, other.getServerId())
                && ObjectUtils.objectsEqual(serviceId, other.getServiceId())
                && status == other.getStatus()
                && ObjectUtils.objectsEqual(message, other.getMessage())
                && ObjectUtils.objectsEqual(pid, other.getPid())
                && serviceType == other.getServiceType()
                && ObjectUtils.objectsEqual(hostName, other.getHostName())
                && ObjectUtils.objectsEqual(port, other.getPort())
                && ObjectUtils.objectsEqual(rdmaPort, other.getRdmaPort()))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((serverId == null) ? 0 : serverId.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((pid == null) ? 0 : pid.hashCode());
        result = prime * result + ((serviceType == null) ? 0 : serviceType.hashCode());
        result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        result = prime * result + ((rdmaPort == null) ? 0 : rdmaPort.hashCode());
        return result;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }
}
