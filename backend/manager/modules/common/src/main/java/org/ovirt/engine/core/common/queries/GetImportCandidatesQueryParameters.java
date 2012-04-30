package org.ovirt.engine.core.common.queries;

public class GetImportCandidatesQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 6808376571843897755L;
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

    public GetImportCandidatesQueryParameters(String path, ImportCandidateSourceEnum candidateSource,
            ImportCandidateTypeEnum candidateType) {
        setPath(path);
        setCandidateSource(candidateSource);
        setCandidateType(candidateType);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetImportCandidatesQueryParameters() {
    }
}
