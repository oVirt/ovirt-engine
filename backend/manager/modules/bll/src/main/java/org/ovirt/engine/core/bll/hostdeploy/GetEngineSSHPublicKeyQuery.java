package org.ovirt.engine.core.bll.hostdeploy;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;

/**
 * Query to fetch public key of the given server name
 */
public class GetEngineSSHPublicKeyQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetEngineSSHPublicKeyQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(EngineEncryptionUtils.getEngineSSHPublicKey());
    }
}
