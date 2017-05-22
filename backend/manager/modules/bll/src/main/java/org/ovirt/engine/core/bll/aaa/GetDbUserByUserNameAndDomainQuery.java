package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetDbUserByUserNameAndDomainQueryParameters;
import org.ovirt.engine.core.dao.DbUserDao;

public class GetDbUserByUserNameAndDomainQuery<P extends GetDbUserByUserNameAndDomainQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private DbUserDao dbUserDao;

    public GetDbUserByUserNameAndDomainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                dbUserDao.getByUsernameAndDomain(getParameters().getUserName(), getParameters().getDomainName()));
    }
}
