package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.utils.ovf.OvfVmIconDefaultsProvider;

/**
 * It provides mapping of operating systems to their default icons.
 */
public class GetVmIconDefaultsQuery extends QueriesCommandBase<QueryParametersBase> {

    @Inject
    private OvfVmIconDefaultsProvider iconDefaultsProvider;

    public GetVmIconDefaultsQuery(QueryParametersBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    /**
     * query return type {@code Map<Integer, Guid>} osId -> iconId
     */
    @Override
    protected void executeQueryCommand() {
        setReturnValue(iconDefaultsProvider.getVmIconDefaults());
    }
}
