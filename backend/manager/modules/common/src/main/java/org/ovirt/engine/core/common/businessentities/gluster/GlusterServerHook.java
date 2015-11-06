package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class GlusterServerHook implements Serializable {

    private static final long serialVersionUID = -8502820314459844040L;

    private Guid hookId;

    private Guid serverId;

    private String serverName;

    private GlusterHookStatus status;

    private GlusterHookContentType contentType;

    private String checksum;

    public Guid getHookId() {
        return hookId;
    }

    public void setHookId(Guid hookId) {
        this.hookId = hookId;
    }

    public Guid getServerId() {
        return serverId;
    }

    public void setServerId(Guid serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public GlusterHookStatus getStatus() {
        return status;
    }

    public void setStatus(GlusterHookStatus status) {
        this.status = status;
    }

    public void setStatus(String status) {
       if (status != null) {
           this.status = GlusterHookStatus.valueOf(status);
       } else {
           this.status = null;
       }
    }

    public GlusterHookContentType getContentType() {
        return contentType;
    }

    public void setContentType(GlusterHookContentType contentType) {
        this.contentType = contentType;
    }

    public void setContentType(String contentType) {
        if (contentType != null) {
            this.contentType = GlusterHookContentType.valueOf(contentType);
        } else {
            this.contentType = null;
        }
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hookId.hashCode();
        result = prime * result + ((serverId == null) ? 0 : serverId.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
        result = prime * result + ((checksum == null) ? 0 : checksum.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlusterServerHook)) {
            return false;
        }
        GlusterServerHook serverHook = (GlusterServerHook) obj;

        return Objects.equals(getHookId(), serverHook.getHookId())
                && Objects.equals(getServerId(), serverHook.getServerId())
                && Objects.equals(getStatus(), serverHook.getStatus())
                && Objects.equals(getContentType(), serverHook.getContentType())
                && Objects.equals(getChecksum(), serverHook.getChecksum());
    }

}
