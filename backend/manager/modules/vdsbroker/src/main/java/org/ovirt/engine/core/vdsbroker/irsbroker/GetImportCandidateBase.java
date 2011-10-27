package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public abstract class GetImportCandidateBase<P extends GetImportCandidatesVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public GetImportCandidateBase(P parameters) {
        super(parameters);
    }
}
