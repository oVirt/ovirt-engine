package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TemplateCandidateInfo")
public class TemplateCandidateInfo extends ImportCandidateInfoBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VmTemplateData")
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
        return getVmTemplateData().getname();
    }

    public TemplateCandidateInfo() {
    }
}
