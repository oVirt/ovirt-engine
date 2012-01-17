package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetCandidateInfoVDSCommandParameters")
public class GetCandidateInfoVDSCommandParameters extends GetImportCandidatesVDSCommandParameters {
    @XmlElement(name = "CandidateID")
    private String privateCandidateID;

    public String getCandidateID() {
        return privateCandidateID;
    }

    public void setCandidateID(String value) {
        privateCandidateID = value;
    }

    public GetCandidateInfoVDSCommandParameters(Guid storagePoolId, String candidateID, String path,
            ImportCandidateSourceEnum candidateSource, ImportCandidateTypeEnum candidateType) {
        super(storagePoolId, path, candidateSource, candidateType);
        setCandidateID(candidateID);
    }

    public GetCandidateInfoVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, candidateID = %s", super.toString(), getCandidateID());
    }
}
