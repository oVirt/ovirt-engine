package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.dao.qos.QosBaseDao;

public class GetAllQosByStoragePoolIdQuery extends QosQueryBase {
    @Inject
    private QosBaseDao qosBaseDao;

    public GetAllQosByStoragePoolIdQuery(QosQueryParameterBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(qosBaseDao.getAllForStoragePoolId(getParameters().getId()));
    }

}
