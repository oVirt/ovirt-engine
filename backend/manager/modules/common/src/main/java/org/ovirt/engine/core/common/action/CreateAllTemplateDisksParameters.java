package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class CreateAllTemplateDisksParameters extends ActionParametersBase {

    private static final long serialVersionUID = -8161353466219476690L;

    private Guid vmId;

    private Map<Guid, DiskImage> diskInfoDestinationMap = new HashMap<>();
    private Guid[] targetDiskIds;

    private Guid vmTemplateId;
    private String vmTemplateName;

    private CopyVolumeType copyVolumeType = CopyVolumeType.SharedVol;

    public CreateAllTemplateDisksParameters() {
    }

    public CreateAllTemplateDisksParameters(Guid vmId) {
        setVmId(vmId);
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public Map<Guid, DiskImage> getDiskInfoDestinationMap() {
        return diskInfoDestinationMap;
    }

    public void setDiskInfoDestinationMap(Map<Guid, DiskImage> diskInfoDestinationMap) {
        this.diskInfoDestinationMap = diskInfoDestinationMap;
    }

    public Guid[] getTargetDiskIds() {
        return targetDiskIds;
    }

    public void setTargetDiskIds(Guid[] targetDiskIds) {
        this.targetDiskIds = targetDiskIds;
    }

    public Guid getVmTemplateId() {
        return vmTemplateId;
    }

    public void setVmTemplateId(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

    public String getVmTemplateName() {
        return vmTemplateName;
    }

    public void setVmTemplateName(String vmTemplateName) {
        this.vmTemplateName = vmTemplateName;
    }

    public CopyVolumeType getCopyVolumeType() {
        return copyVolumeType;
    }

    public void setCopyVolumeType(CopyVolumeType copyVolumeType) {
        this.copyVolumeType = copyVolumeType;
    }

}
