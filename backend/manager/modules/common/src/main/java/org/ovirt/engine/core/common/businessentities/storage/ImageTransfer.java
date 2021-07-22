package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ImageTransfer implements BusinessEntity<Guid>, Queryable {
    private static final long serialVersionUID = -2022195484408789358L;

    private Guid commandId;
    private ActionType commandType;
    private ImageTransferPhase phase;
    private TransferType type;
    private Boolean active;
    private Date lastUpdated;
    private String message;
    private TransferClientType transferClientType;

    private Guid vdsId;
    private Guid diskId;
    private Guid imagedTicketId;
    private String proxyUri;
    private String daemonUri;
    private Guid backupId;

    private Long bytesSent;
    private Long bytesTotal;
    private Integer clientInactivityTimeout;
    private TimeoutPolicyType timeoutPolicy;
    private VolumeFormat imageFormat;
    private ImageTransferBackend backend;

    // Transfer specified image data instead of data from entire image chain.
    private Boolean shallow;

    public ImageTransfer(Guid commandId) {
        this.commandId = commandId;
    }

    public ImageTransfer() {}

    public Guid getId() {
        return commandId;
    }

    public void setId(Guid commandId) {
        this.commandId = commandId;
    }

    public ActionType getCommandType() {
        return commandType;
    }

    public void setCommandType(ActionType commandType) {
        this.commandType = commandType;
    }

    public ImageTransferPhase getPhase() {
        return phase;
    }

    public void setPhase(ImageTransferPhase phase) {
        this.phase = phase;
    }

    public TransferType getType() {
        return type;
    }

    public void setType(TransferType type) {
        this.type = type;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }

    public Guid getImagedTicketId() {
        return imagedTicketId;
    }

    public void setImagedTicketId(Guid imagedTicketId) {
        this.imagedTicketId = imagedTicketId;
    }

    public String getProxyUri() {
        return proxyUri;
    }

    public void setProxyUri(String proxyUri) {
        this.proxyUri = proxyUri;
    }

    public String getDaemonUri() {
        return daemonUri;
    }

    public void setDaemonUri(String daemonUri) {
        this.daemonUri = daemonUri;
    }

    public Long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public Long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(Long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public String getProxyURLForTransfer() {
        return getProxyUri() + "/" + getImagedTicketId();
    }

    public String getDaemonURLForTransfer() {
        return getDaemonUri() + "/" + getImagedTicketId();
    }

    public Integer getClientInactivityTimeout() {
        return clientInactivityTimeout;
    }

    public void setClientInactivityTimeout(Integer clientInactivityTimeout) {
        this.clientInactivityTimeout = clientInactivityTimeout;
    }

    public TimeoutPolicyType getTimeoutPolicy() {
        return timeoutPolicy;
    }

    public void setTimeoutPolicy(TimeoutPolicyType timeoutPolicy) {
        this.timeoutPolicy = timeoutPolicy;
    }

    public VolumeFormat getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(VolumeFormat imageFormat) {
        this.imageFormat = imageFormat;
    }

    public ImageTransferBackend getBackend() {
        return backend;
    }

    public void setBackend(ImageTransferBackend backend) {
        this.backend = backend;
    }

    public Guid getBackupId() {
        return backupId;
    }

    public void setBackupId(Guid backupId) {
        this.backupId = backupId;
    }

    public TransferClientType getTransferClientType() {
        return transferClientType;
    }

    public void setTransferClientType(TransferClientType transferClientType) {
        this.transferClientType = transferClientType;
    }

    public Boolean isShallow() {
        return shallow;
    }

    public void setShallow(Boolean shallow) {
        this.shallow = shallow;
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
        ImageTransfer other = (ImageTransfer) obj;
        return Objects.equals(commandId, other.commandId)
                && Objects.equals(phase, other.phase)
                && Objects.equals(active, other.active)
                && Objects.equals(lastUpdated, other.lastUpdated)
                && Objects.equals(message, other.message)
                && Objects.equals(vdsId, other.vdsId)
                && Objects.equals(diskId, other.diskId)
                && Objects.equals(imagedTicketId, other.imagedTicketId)
                && Objects.equals(proxyUri, other.proxyUri)
                && Objects.equals(bytesSent, other.bytesSent)
                && Objects.equals(bytesTotal, other.bytesTotal)
                && Objects.equals(clientInactivityTimeout, other.clientInactivityTimeout)
                && Objects.equals(timeoutPolicy, other.timeoutPolicy)
                && Objects.equals(imageFormat, other.imageFormat)
                && Objects.equals(transferClientType, other.transferClientType)
                && Objects.equals(shallow, other.shallow)
                && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                commandId,
                phase,
                type,
                active,
                lastUpdated,
                message,
                vdsId,
                diskId,
                imagedTicketId,
                proxyUri,
                bytesSent,
                bytesTotal,
                clientInactivityTimeout,
                timeoutPolicy,
                imageFormat,
                transferClientType,
                shallow
        );
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("phase", getPhase())
                .append("type", getType())
                .append("active", getActive())
                .append("lastUpdated", getLastUpdated())
                .append("message", getMessage())
                .append("vdsId", getVdsId())
                .append("diskId", getDiskId())
                .append("imagedTicketId", getImagedTicketId())
                .append("proxyUri", getProxyUri())
                .append("bytesSent", getBytesSent())
                .append("bytesTotal", getBytesTotal())
                .append("clientInactivityTimeout", getClientInactivityTimeout())
                .append("timeoutPolicy", getTimeoutPolicy())
                .append("imageFormat", getImageFormat())
                .append("transferClientType", getTransferClientType())
                .append("shallow", isShallow())
                .build();
    }

    @Override
    public Guid getQueryableId() {
        return getId();
    }

    public boolean isPaused() {
        return phase == ImageTransferPhase.PAUSED_SYSTEM || phase == ImageTransferPhase.PAUSED_USER;
    }

    public boolean isPausedOrFinished() {
        return isPaused() || phase == ImageTransferPhase.FINISHED_SUCCESS || phase == ImageTransferPhase.FINISHED_FAILURE;
    }
}
