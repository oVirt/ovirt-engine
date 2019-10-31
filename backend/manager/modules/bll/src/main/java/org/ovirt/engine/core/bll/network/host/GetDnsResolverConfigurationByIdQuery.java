package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;


public class GetDnsResolverConfigurationByIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;

    public GetDnsResolverConfigurationByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(dnsResolverConfigurationDao.get(getDnsResolverConfigurationId()));
    }

    private Guid getDnsResolverConfigurationId() {
        return getParameters().getId();
    }

    @Override protected boolean validateInputs() {
        if (!super.validateInputs()) {
            return false;
        }

        if (getDnsResolverConfigurationId() == null) {
            getQueryReturnValue().setExceptionString(EngineMessage.DNS_RESOLVER_CONFIGURATION_ID_IS_NULL.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        return true;
    }
}
