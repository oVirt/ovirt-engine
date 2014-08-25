package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.QosQueryParameterBase;

public class GetAllQosByTypeQuery extends QosQueryBase {

    public GetAllQosByTypeQuery(QosQueryParameterBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getQosDao()
                .getAll());
    }

}
