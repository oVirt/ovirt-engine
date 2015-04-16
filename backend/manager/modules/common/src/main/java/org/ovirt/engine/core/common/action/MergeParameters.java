package org.ovirt.engine.core.common.action;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class MergeParameters extends StorageDomainParametersBase {
    private static final long serialVersionUID = -4684653037326443549L;
    private Guid vmId;
    private DiskImage activeImage;
    private DiskImage baseImage;
    private DiskImage topImage;
    private long bandwidth;

    // Members for internal command state persistence
    private Guid vmJobId;
    private Set<Guid> vmVolumeChain;

    private MergeParameters() {}

    public MergeParameters(
            Guid vdsId,
            Guid vmId,
            DiskImage activeImage,
            DiskImage baseImage, // diskImage in some Parameter classes
            DiskImage topImage, // destinationDiskImage in some Parameter classes
            long bandwidth) {
        super(activeImage.getStoragePoolId(), activeImage.getStorageIds().get(0));
        setVdsId(vdsId);
        this.vmId = vmId;
        this.baseImage = baseImage;
        this.topImage = topImage;
        this.activeImage = activeImage;
        this.bandwidth = bandwidth;
    }

    public Guid getVmId() {
        return vmId;
    }

    public Guid getImageGroupId() {
        return activeImage.getId();
    }

    public Guid getImageId() {
        return activeImage.getImageId();
    }

    public DiskImage getActiveImage() {
        return activeImage;
    }

    public DiskImage getBaseImage() {
        return baseImage;
    }

    public DiskImage getTopImage() {
        return topImage;
    }

    public long getBandwidth() {
        return bandwidth;
    }

    public Guid getVmJobId() {
        return vmJobId;
    }

    public void setVmJobId(Guid vmJobId) {
        this.vmJobId = vmJobId;
    }

    public Set<Guid> getVmVolumeChain() {
        return vmVolumeChain;
    }

    public void setVmVolumeChain(Set<Guid> vmVolumeChain) {
        this.vmVolumeChain = vmVolumeChain;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vmId", vmId)
                .append("activeImage", activeImage)
                .append("baseImage", baseImage)
                .append("topImage", topImage)
                .append("bandwidth", bandwidth);
    }
}
