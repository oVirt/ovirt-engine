package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.utils.PKIResources;

public class GetCACertificateQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    public GetCACertificateQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        try {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setReturnValue(
                PKIResources.getCaCertificate().toString(PKIResources.Format.X509_PEM)
            );
            getQueryReturnValue().setSucceeded(true);
        } catch (Exception e) {
            getQueryReturnValue().setExceptionString(e.getMessage());
        }
    }
}
