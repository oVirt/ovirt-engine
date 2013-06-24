package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;

/**
 * Query to fetch public key of the given server name
 */
public class GetServerSSHPublicKeyQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetServerSSHPublicKeyQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(EngineEncryptionUtils.getEngineSSHPublicKey());
    }
}
