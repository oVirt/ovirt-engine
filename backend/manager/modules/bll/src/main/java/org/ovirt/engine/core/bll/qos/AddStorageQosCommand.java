package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageQosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.dao.qos.QosDao;

public class AddStorageQosCommand extends AddQosCommand<StorageQos, QosValidator<StorageQos>> {

    public AddStorageQosCommand(QosParametersBase<StorageQos> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected QosDao<StorageQos> getQosDao() {
        return storageQosDao;
    }

    @Override
    protected QosValidator<StorageQos> getQosValidator(StorageQos qos) {
        return new StorageQosValidator(qos);
    }

}
