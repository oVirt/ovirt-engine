package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.dao.qos.QosBaseDao;

public class GetAllQosByStoragePoolIdQuery extends QosQueryBase {
    @Inject
    private QosBaseDao qosBaseDao;

    public GetAllQosByStoragePoolIdQuery(QosQueryParameterBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(qosBaseDao.getAllForStoragePoolId(getParameters().getId()));
    }

}
