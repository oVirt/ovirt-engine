package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Collection;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class ImageTicketInformation implements BusinessEntity<Guid> {

    private static final long serialVersionUID = -4124807995133307564L;

    private Guid id;

    private long size;

    private String url;

    private int timeout;

    private int expires;

    private Collection<TransferType> transferTypes;

    private String fileName;

    private boolean active;

    private Long transferred;

    private Integer idleTime;

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public Collection<TransferType> getTransferTypes() {
        return transferTypes;
    }

    public void setTransferTypes(Collection<TransferType> transferTypes) {
        this.transferTypes = transferTypes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getTransferred() {
        return transferred;
    }

    public void setTransferred(Long transferred) {
        this.transferred = transferred;
    }

    public Integer getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(Integer idleTime) {
        this.idleTime = idleTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImageTicketInformation other = (ImageTicketInformation) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(size, other.size)
                && Objects.equals(url, other.url)
                && Objects.equals(timeout, other.timeout)
                && Objects.equals(expires, other.expires)
                && Objects.equals(transferTypes, other.transferTypes)
                && Objects.equals(fileName, other.fileName)
                && Objects.equals(active, other.active)
                && Objects.equals(transferred, other.transferred)
                && Objects.equals(idleTime, other.idleTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                size,
                url,
                timeout,
                expires,
                transferTypes,
                fileName,
                active,
                transferred,
                idleTime);
    }
}
