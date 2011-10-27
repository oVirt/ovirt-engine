package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.vdscommands.*;

/**
 * Gets a dictionary of the import candidates of a specified type from a
 * specified source, where the keys are the candidates' IDs and the values are
 * the ImportCandidateInfoBase instances of the candidates.
 */
public class GetImportCandidatesInfoQuery<P extends GetImportCandidatesQueryParameters>
        extends GetImportCandidatesBase<P> {
    public GetImportCandidatesInfoQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // todo - omer handle this
        java.util.HashMap<String, ImportCandidateInfoBase> retFromIrs =
                (java.util.HashMap<String, ImportCandidateInfoBase>) Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.GetImportCandidatesInfo,
                                new GetImportCandidatesVDSCommandParameters(Guid.Empty, getParameters().getPath(),
                                        getParameters().getCandidateSource(), getParameters().getCandidateType()))
                        .getReturnValue();

        getQueryReturnValue().setReturnValue(retFromIrs);
    }
}
