package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SecretValue;
import org.ovirt.engine.core.dao.VmDao;

public class HasNvramDataQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDao vmDao;

    public HasNvramDataQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Pair<SecretValue<String>, String> result = vmDao.getNvramData(getParameters().getId());
        getQueryReturnValue().setReturnValue(!SecretValue.isNull(result.getFirst()));
    }
}
