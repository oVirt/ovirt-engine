package org.ovirt.engine.core.bll.qos;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmSlaPolicyUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageQosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.qos.QosDao;

public class RemoveStorageQosCommand extends RemoveQosCommandBase<StorageQos, QosValidator<StorageQos>> {

    @Inject
    VmSlaPolicyUtils vmSlaPolicyUtils;

    public RemoveStorageQosCommand(QosParametersBase<StorageQos> parameters, CommandContext cmdContext) {
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
        Map<Guid, List<DiskImage>> vmDisksMap = vmSlaPolicyUtils.getRunningVmDiskImageMapWithQos(getQosId());

        super.executeCommand();

        // After successful command, set everything to unlimited
        if (getSucceeded()) {
            vmSlaPolicyUtils.refreshVmsStorageQos(vmDisksMap, new StorageQos());
        }
    }
}
