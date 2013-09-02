package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;

public class GetDomainListQuery<P extends GetDomainListParameters> extends QueriesCommandBase<P> {
    public GetDomainListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<String> domains = LdapBrokerUtils.getDomainsList(getParameters().getFilterInternalDomain());
        String internalDomainName = Config.<String>GetValue(ConfigValues.AdminDomain);
        Collections.sort(domains, new LoginDomainComparator(internalDomainName));
        getQueryReturnValue().setReturnValue(domains);
    }
}
