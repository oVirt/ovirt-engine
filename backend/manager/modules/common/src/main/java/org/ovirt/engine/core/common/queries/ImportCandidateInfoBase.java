package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;
import java.util.List;
import java.util.Map;

public abstract class ImportCandidateInfoBase {
    private ImportCandidateSourceEnum privateCandidateSource = ImportCandidateSourceEnum.forValue(0);

    public ImportCandidateSourceEnum getCandidateSource() {
        return privateCandidateSource;
    }

    public void setCandidateSource(ImportCandidateSourceEnum value) {
        privateCandidateSource = value;
    }

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
