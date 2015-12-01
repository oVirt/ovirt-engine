package org.ovirt.engine.core.common.vdscommands;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;


public class GetDiskLunAlignmentVDSCommandParameters extends GetDiskAlignmentVDSCommandParameters {
    private String lunId;

    public GetDiskLunAlignmentVDSCommandParameters(Guid vdsId, Guid vmId) {
        super(vdsId, vmId);
    }

    public GetDiskLunAlignmentVDSCommandParameters() {
    }

    public void setLunId(String lunId) {
        this.lunId = lunId;
    }

    public String getLunId() {
        return lunId;
    }

    @Override
    public Map<String, String> getDriveSpecs() {
        Map<String, String> drive = new HashMap<>();
        drive.put("device", "disk");
        drive.put("GUID", getLunId());
        return drive;
    }
}
