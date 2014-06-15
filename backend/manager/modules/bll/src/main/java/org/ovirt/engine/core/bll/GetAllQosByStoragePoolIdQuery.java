package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.QosQueryParameterBase;

public class GetAllQosByStoragePoolIdQuery extends QosQueryBase {

    public GetAllQosByStoragePoolIdQuery(QosQueryParameterBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getQosBaseDao()
                .getAllForStoragePoolId(getParameters().getId()));
    }

}
