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
        return Objects.hash(
                hookId,
                serverId,
                status,
                contentType,
                checksum
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterServerHook)) {
            return false;
        }
        GlusterServerHook serverHook = (GlusterServerHook) obj;
        return Objects.equals(hookId, serverHook.hookId)
                && Objects.equals(serverId, serverHook.serverId)
                && Objects.equals(status, serverHook.status)
                && Objects.equals(contentType, serverHook.contentType)
                && Objects.equals(checksum, serverHook.checksum);
    }

}
