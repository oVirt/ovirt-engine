package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImportCandidateInfoBase")
public abstract class ImportCandidateInfoBase {
    @XmlElement(name = "CandidateSource")
    private ImportCandidateSourceEnum privateCandidateSource = ImportCandidateSourceEnum.forValue(0);

    public ImportCandidateSourceEnum getCandidateSource() {
        return privateCandidateSource;
    }

    public void setCandidateSource(ImportCandidateSourceEnum value) {
        privateCandidateSource = value;
    }

    // @XmlElement
    private java.util.HashMap<String, List<DiskImage>> privateImagesData;

    public Map<String, List<DiskImage>> getImagesData() {
        return privateImagesData;
    }

    public void setImagesData(java.util.HashMap<String, List<DiskImage>> value) {
        privateImagesData = value;
    }

    public ImportCandidateInfoBase(ImportCandidateSourceEnum candidateSource,
            java.util.HashMap<String, List<DiskImage>> imagesData) {
        setImagesData(imagesData);
        setCandidateSource(candidateSource);
    }

    public abstract String getCandidateDisplayName();

    public ImportCandidateInfoBase() {
    }
}
