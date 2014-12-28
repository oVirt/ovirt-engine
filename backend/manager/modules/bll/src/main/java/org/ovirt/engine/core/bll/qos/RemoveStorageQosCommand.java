package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageQosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.dao.qos.QosDao;

public class RemoveStorageQosCommand extends RemoveQosCommandBase<StorageQos, QosValidator<StorageQos>> {

    public RemoveStorageQosCommand(QosParametersBase<StorageQos> parameters) {
        super(parameters);
    }

    @Override
    protected QosDao<StorageQos> getQosDao() {
        return getDbFacade().getStorageQosDao();
    }

    @Override
    protected QosValidator<StorageQos> getQosValidator(StorageQos qos) {
        return new StorageQosValidator(qos);
    }

}
