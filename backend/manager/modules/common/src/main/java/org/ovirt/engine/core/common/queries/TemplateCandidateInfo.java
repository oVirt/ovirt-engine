package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;
import java.util.List;

public class TemplateCandidateInfo extends ImportCandidateInfoBase {
    private VmTemplate privateVmTemplateData;

    public VmTemplate getVmTemplateData() {
        return privateVmTemplateData;
    }

    public void setVmTemplateData(VmTemplate value) {
        privateVmTemplateData = value;
    }

    public TemplateCandidateInfo(VmTemplate vmTemplateData, ImportCandidateSourceEnum candidateSource,
            java.util.HashMap<String, List<DiskImage>> imagesData) {
        super(candidateSource, imagesData);
        setVmTemplateData(vmTemplateData);
    }

    @Override
    public String getCandidateDisplayName() {
        return getVmTemplateData().getName();
    }

    public TemplateCandidateInfo() {
    }
}
