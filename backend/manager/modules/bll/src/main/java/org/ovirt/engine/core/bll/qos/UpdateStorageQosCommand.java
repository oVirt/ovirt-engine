package org.ovirt.engine.core.bll.qos;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmSlaPolicyUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageQosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.dao.qos.QosDao;

public class UpdateStorageQosCommand extends UpdateQosCommandBase<StorageQos, QosValidator<StorageQos>> {

    @Inject
    VmSlaPolicyUtils vmSlaPolicyUtils;

    public UpdateStorageQosCommand(QosParametersBase<StorageQos> parameters, CommandContext cmdContext) {
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

    @Override
    protected void executeCommand() {
        super.executeCommand();
        if (getSucceeded()) {
            vmSlaPolicyUtils.refreshRunningVmsWithStorageQos(getQosId(), getQos());
        }
    }
}
