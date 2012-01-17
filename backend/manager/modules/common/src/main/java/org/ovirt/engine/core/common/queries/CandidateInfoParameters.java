package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CandidateInfoParameters")
public class CandidateInfoParameters extends GetImportCandidatesQueryParameters {
    private static final long serialVersionUID = 2908006384750137275L;
    @XmlElement(name = "CandidateIdOrName")
    private String privateCandidateIdOrName;

    public String getCandidateIdOrName() {
        return privateCandidateIdOrName;
    }

    public void setCandidateIdOrName(String value) {
        privateCandidateIdOrName = value;
    }

    @XmlElement(name = "IsName")
    private boolean privateIsName;

    public boolean getIsName() {
        return privateIsName;
    }

    public void setIsName(boolean value) {
        privateIsName = value;
    }

    public CandidateInfoParameters(String candidateIdOrName, boolean isName, String path,
            ImportCandidateSourceEnum candidateSource, ImportCandidateTypeEnum candidateType) {
        super(path, candidateSource, candidateType);
        setCandidateIdOrName(candidateIdOrName);
        setIsName(isName);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public CandidateInfoParameters() {
    }
}
