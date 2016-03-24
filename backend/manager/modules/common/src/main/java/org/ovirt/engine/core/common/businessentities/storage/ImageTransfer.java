package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class ImageTransfer implements BusinessEntity<Guid> {

    private static final long serialVersionUID = 3761304077670003457L;
    private Guid commandId;
    private VdcActionType commandType;
    private ImageTransferPhase phase;
    private Date lastUpdated;
    private String message;

    private Guid vdsId;
    private Guid diskId;
    private Guid imagedTicketId;
    private String proxyUri;
    private String signedTicket;

    private Long bytesSent;
    private Long bytesTotal;

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

    public VdcActionType getCommandType() {
        return commandType;
    }

    public void setCommandType(VdcActionType commandType) {
        this.commandType = commandType;
    }

    public ImageTransferPhase getPhase() {
        return phase;
    }

    public void setPhase(ImageTransferPhase phase) {
        this.phase = phase;
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

    public String getSignedTicket() {
        return signedTicket;
    }

    public void setSignedTicket(String signedTicket) {
        this.signedTicket = signedTicket;
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
                && Objects.equals(lastUpdated, other.lastUpdated)
                && Objects.equals(message, other.message)
                && Objects.equals(vdsId, other.vdsId)
                && Objects.equals(diskId, other.diskId)
                && Objects.equals(imagedTicketId, other.imagedTicketId)
                && Objects.equals(proxyUri, other.proxyUri)
                && Objects.equals(signedTicket, other.signedTicket)
                && Objects.equals(bytesSent, other.bytesSent)
                && Objects.equals(bytesTotal, other.bytesTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                commandId,
                phase,
                lastUpdated,
                message,
                vdsId,
                diskId,
                imagedTicketId,
                proxyUri,
                signedTicket,
                bytesSent,
                bytesTotal
        );
    }
}
