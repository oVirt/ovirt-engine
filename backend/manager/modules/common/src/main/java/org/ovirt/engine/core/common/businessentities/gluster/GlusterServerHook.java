package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class GlusterServerHook implements Serializable {

    private static final long serialVersionUID = -8502820314459844040L;

    private Guid hookId;

    private Guid serverId;

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

}
