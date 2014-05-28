package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.QosQueryParameterBase;

public class GetAllQosByStoragePoolIdAndTypeQuery extends QosQueryBase {

    public GetAllQosByStoragePoolIdAndTypeQuery(QosQueryParameterBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getQosDao().getAllForStoragePoolId(getParameters().getId()));
    }

}
