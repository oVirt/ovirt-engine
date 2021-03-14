package org.ovirt.engine.core.common.businessentities.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.VmEntityType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The disk is contains data from the {@link BaseDisk} and the storage specific details for the disk, which are
 * determined by {@link Disk#getDiskStorageType()}.<br>
 * The disk may be attached to a VM or Template, which is indicated by {@link Disk#getVmEntityType()}. If it is null,
 * then the disk is detached (floating).<br>
 * <br>
 * <b>Preferably, use this entity as the base reference wherever you don't need to hold a reference of a specific
 * storage implementation.</b>
 */
public abstract class Disk extends BaseDisk {

    private static final long serialVersionUID = 1380107681294904254L;

    /**
     * The VM Type is indicated by this field, or <code>null</code> if it is detached.
     */
    private VmEntityType vmEntityType;
    private int numberOfVms;
    private ArrayList<String> vmNames;
    private List<String> templateVersionNames;
    private boolean plugged;
    private String logicalName;

    /**
     * Image Transfer information is only for display purposes
     */
    private ImageTransferPhase imageTransferPhase;
    private Long imageTransferBytesSent;
    private Long imageTransferBytesTotal;
    private TransferType transferType;

    /**
     * @return Whether taking snapshots of this disk is allowed
     */
    public abstract boolean isAllowSnapshot();

    public VmEntityType getVmEntityType() {
        return vmEntityType;
    }

    public void setVmEntityType(VmEntityType vmEntityType) {
        this.vmEntityType = vmEntityType;
    }

    public boolean getPlugged() {
        return plugged;
    }

    public void setPlugged(boolean plugged) {
        this.plugged = plugged;
    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public abstract long getSize();

    public ImageTransferPhase getImageTransferPhase() {
        return imageTransferPhase;
    }

    public void setImageTransferPhase(ImageTransferPhase imageTransferPhase) {
        this.imageTransferPhase = imageTransferPhase;
    }

    public Long getImageTransferBytesSent() {
        return imageTransferBytesSent;
    }

    public void setImageTransferBytesSent(Long imageTransferBytesSent) {
        this.imageTransferBytesSent = imageTransferBytesSent;
    }

    public Long getImageTransferBytesTotal() {
        return imageTransferBytesTotal;
    }

    public void setImageTransferBytesTotal(Long imageTransferBytesTotal) {
        this.imageTransferBytesTotal = imageTransferBytesTotal;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                plugged,
                vmNames,
                templateVersionNames,
                vmEntityType,
                numberOfVms,
                logicalName,
                imageTransferPhase,
                imageTransferBytesSent,
                imageTransferBytesTotal,
                transferType
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Disk)) {
            return false;
        }
        Disk other = (Disk) obj;
        return super.equals(obj)
                && Objects.equals(plugged, other.plugged)
                && Objects.equals(vmNames, other.vmNames)
                && Objects.equals(templateVersionNames, other.templateVersionNames)
                && Objects.equals(logicalName, other.logicalName)
                && imageTransferPhase == other.imageTransferPhase
                && Objects.equals(imageTransferBytesSent, other.imageTransferBytesSent)
                && Objects.equals(imageTransferBytesTotal, other.imageTransferBytesTotal)
                && transferType == other.transferType
                && vmEntityType == other.vmEntityType
                && numberOfVms == other.numberOfVms;
    }

    public int getNumberOfVms() {
        return numberOfVms;
    }

    public void setNumberOfVms(int numberOfVms) {
        this.numberOfVms = numberOfVms;
    }

    public ArrayList<String> getVmNames() {
        return vmNames;
    }

    public void setVmNames(ArrayList<String> vmNames) {
        this.vmNames = vmNames;
    }

    public List<String> getTemplateVersionNames() {
        return this.templateVersionNames;
    }

    public void setTemplateVersionNames(List<String> templateVersionNames) {
        this.templateVersionNames = templateVersionNames;
    }

    @JsonIgnore
    public boolean isDiskSnapshot() {
        return false;
    }

    @JsonIgnore
    public boolean isOvfStore() {
        return getContentType() == DiskContentType.OVF_STORE;
    }
}
