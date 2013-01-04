package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;
import java.util.List;

public class VmCandidateInfo extends ImportCandidateInfoBase {
    private VmStatic privateVmData;

    public VmStatic getVmData() {
        return privateVmData;
    }

    public void setVmData(VmStatic value) {
        privateVmData = value;
    }

    public VmCandidateInfo(VmStatic vmData, ImportCandidateSourceEnum candidateSource,
            java.util.HashMap<String, List<DiskImage>> imagesData) {
        super(candidateSource, imagesData);
        setVmData(vmData);
    }

    @Override
    public String getCandidateDisplayName() {
        return getVmData().getVmName();
    }

    public VmCandidateInfo() {
    }
}
