package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllImportCandidatesQueryParameters")
public class GetAllImportCandidatesQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 5576238956288782372L;
    @XmlElement(name = "Path")
    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    public void setPath(String value) {
        privatePath = value;
    }

    @XmlElement(name = "CandidateType")
    private ImportCandidateTypeEnum privateCandidateType = ImportCandidateTypeEnum.forValue(0);

    public ImportCandidateTypeEnum getCandidateType() {
        return privateCandidateType;
    }

    public void setCandidateType(ImportCandidateTypeEnum value) {
        privateCandidateType = value;
    }

    public GetAllImportCandidatesQueryParameters(String path, ImportCandidateTypeEnum candidateType) {
        setPath(path);
        setCandidateType(candidateType);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetAllImportCandidatesQueryParameters() {
    }
}
