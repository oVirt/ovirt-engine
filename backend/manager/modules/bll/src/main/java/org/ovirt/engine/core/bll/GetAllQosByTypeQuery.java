package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;

public class GetAllQosByTypeQuery extends QosQueryBase {

    public GetAllQosByTypeQuery(QosQueryParameterBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getQosDao()
                .getAll(getUserID(), getParameters().isFiltered()));
    }

}
