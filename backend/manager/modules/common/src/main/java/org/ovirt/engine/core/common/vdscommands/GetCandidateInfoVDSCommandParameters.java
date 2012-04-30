package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

import org.ovirt.engine.core.common.queries.*;

public class GetCandidateInfoVDSCommandParameters extends GetImportCandidatesVDSCommandParameters {
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
