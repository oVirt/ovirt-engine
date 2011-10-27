package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public abstract class GetImportCandidatesBase<P extends GetImportCandidatesQueryParameters>
        extends QueriesCommandBase<P> {
    public GetImportCandidatesBase(P parameters) {
        super(parameters);
    }
}
