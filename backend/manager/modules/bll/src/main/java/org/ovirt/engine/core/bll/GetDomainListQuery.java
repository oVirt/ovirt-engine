package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetDomainListQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetDomainListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        boolean filterInternalDomain = false;
        // get concrete parameters object
        if (getParameters() instanceof GetDomainListParameters) {
            filterInternalDomain = ((GetDomainListParameters)getParameters()).getFilterInternalDomain();
        }
        List<String> domains = LdapBrokerUtils.getDomainsList(filterInternalDomain);
        Collections.sort(domains);
        getQueryReturnValue().setReturnValue(domains);
    }
}
