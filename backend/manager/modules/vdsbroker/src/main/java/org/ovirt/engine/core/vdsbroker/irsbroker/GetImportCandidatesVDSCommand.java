package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

/**
 * Gets a list of all import candidates' IDs.
 */
public class GetImportCandidatesVDSCommand<P extends GetImportCandidatesVDSCommandParameters>
        extends GetImportCandidateBase<P> {
    private IrsVMListReturnForXmlRpc mVmListReturn;

    public GetImportCandidatesVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        mVmListReturn = getIrsProxy().getImportCandidates(StringHelper.trimEnd(getParameters().getPath(), '/'),
                ImportEnumsManager.CandidateSourceString(getParameters().getCandidateSource()),
                ImportEnumsManager.CandidateTypeString(getParameters().getCandidateType()));
        ProceedProxyReturnValue();
        setReturnValue(new java.util.ArrayList<String>(java.util.Arrays.asList(mVmListReturn.mVMList)));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return mVmListReturn.mStatus;
    }
}
