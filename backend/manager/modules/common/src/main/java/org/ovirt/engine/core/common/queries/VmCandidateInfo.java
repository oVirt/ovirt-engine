package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmCandidateInfo")
public class VmCandidateInfo extends ImportCandidateInfoBase {
    @XmlElement(name = "VmData")
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
        return getVmData().getvm_name();
    }

    public VmCandidateInfo() {
    }
}
