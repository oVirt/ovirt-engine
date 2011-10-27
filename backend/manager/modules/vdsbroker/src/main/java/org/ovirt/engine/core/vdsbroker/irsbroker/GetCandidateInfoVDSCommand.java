package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

/**
 * Gets an ImportCandidateInfoBase instance that contains the data regarding the specified candidate ID.
 */
public class GetCandidateInfoVDSCommand<P extends GetCandidateInfoVDSCommandParameters>
        extends GetImportCandidatesInfoVDSCommand<P> {
    private ImportCandidateInfoReturnForXmlRpc _candidateVMInfoRetVal;

    public GetCandidateInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        _candidateVMInfoRetVal = getIrsProxy().getCandidateInfo(getParameters().getCandidateID(),
                StringHelper.trimEnd(getParameters().getPath(), '/'),
                ImportEnumsManager.CandidateSourceString(getParameters().getCandidateSource()));

        ProceedProxyReturnValue();
        setReturnValue(GetCandidateInfoByIrsInfoList(_candidateVMInfoRetVal.mInfoList));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _candidateVMInfoRetVal.mStatus;
    }
}
