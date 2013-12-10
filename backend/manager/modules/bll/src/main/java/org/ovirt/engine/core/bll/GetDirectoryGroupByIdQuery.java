package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetDirectoryGroupByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final String domain = getParameters().getDomain();
        final Guid id = getParameters().getId();
        final LdapBroker broker = LdapFactory.getInstance(domain);
        final LdapGroup group = (LdapGroup) broker.runAdAction(
            AdActionType.GetAdGroupByGroupId,
            new LdapSearchByIdParameters(domain, id)
        ).getReturnValue();
        getQueryReturnValue().setReturnValue(group);
    }

}
