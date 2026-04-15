package org.ovirt.engine.core.common.action;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Guid;

public class ConvertOvaParameters extends ConvertVmParameters {

    private String ovaPath;
    private VmEntityType vmEntityType = VmEntityType.VM;
    private Map<Guid, Guid> imageMappings;
    private Map<Guid, Map<String, Object>> preAttachedManagedBlockDevicesByDiskId;
    private Map<Guid, Guid> ovaSourceImageIdByDiskId;
    private List<Guid> templateDiskIdsForExtract;

    public ConvertOvaParameters() {
    }

    public ConvertOvaParameters(Guid vmId) {
        super(vmId);
    }

    public String getOvaPath() {
        return ovaPath;
    }

    public void setOvaPath(String ovaPath) {
        this.ovaPath = ovaPath;
    }

    public VmEntityType getVmEntityType() {
        return vmEntityType;
    }

    public void setVmEntityType(VmEntityType vmEntityType) {
        this.vmEntityType = vmEntityType;
    }

    public Map<Guid, Guid> getImageMappings() {
        return imageMappings;
    }

    public void setImageMappings(Map<Guid, Guid> diskMappings) {
        this.imageMappings = diskMappings;
    }

    public Map<Guid, Map<String, Object>> getPreAttachedManagedBlockDevicesByDiskId() {
        return preAttachedManagedBlockDevicesByDiskId;
    }

    public void setPreAttachedManagedBlockDevicesByDiskId(
            Map<Guid, Map<String, Object>> preAttachedManagedBlockDevicesByDiskId) {
        this.preAttachedManagedBlockDevicesByDiskId = preAttachedManagedBlockDevicesByDiskId;
    }

    public Map<Guid, Guid> getOvaSourceImageIdByDiskId() {
        return ovaSourceImageIdByDiskId;
    }

    public void setOvaSourceImageIdByDiskId(Map<Guid, Guid> ovaSourceImageIdByDiskId) {
        this.ovaSourceImageIdByDiskId = ovaSourceImageIdByDiskId;
    }

    public List<Guid> getTemplateDiskIdsForExtract() {
        return templateDiskIdsForExtract;
    }

    public void setTemplateDiskIdsForExtract(List<Guid> templateDiskIdsForExtract) {
        this.templateDiskIdsForExtract = templateDiskIdsForExtract;
    }
}
