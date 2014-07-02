package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetDbUserByUserNameAndDomainQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetDbUserByUserNameAndDomainQuery<P extends GetDbUserByUserNameAndDomainQueryParameters> extends QueriesCommandBase<P> {

    public GetDbUserByUserNameAndDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getDbUserDao()
                .getByUsernameAndDomain(getParameters().getUserName(), getParameters().getDomainName()));
    }
}
