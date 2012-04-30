package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

import org.ovirt.engine.core.common.queries.*;

public class GetImportCandidatesVDSCommandParameters extends IrsBaseVDSCommandParameters {
    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    public void setPath(String value) {
        privatePath = value;
    }

    private ImportCandidateSourceEnum privateCandidateSource = ImportCandidateSourceEnum.forValue(0);

    public ImportCandidateSourceEnum getCandidateSource() {
        return privateCandidateSource;
    }

    public void setCandidateSource(ImportCandidateSourceEnum value) {
        privateCandidateSource = value;
    }

    private ImportCandidateTypeEnum privateCandidateType = ImportCandidateTypeEnum.forValue(0);

    public ImportCandidateTypeEnum getCandidateType() {
        return privateCandidateType;
    }

    public void setCandidateType(ImportCandidateTypeEnum value) {
        privateCandidateType = value;
    }

    public GetImportCandidatesVDSCommandParameters(Guid storagePoolId, String path,
            ImportCandidateSourceEnum candidateSource, ImportCandidateTypeEnum candidateType) {
        super(storagePoolId);
        setPath(path);
        setCandidateSource(candidateSource);
        setCandidateType(candidateType);
    }

    public GetImportCandidatesVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, path = %s, candidateSource = %s, candidateType = %s",
                super.toString(),
                getPath(),
                getCandidateSource(),
                getCandidateType());
    }
}
