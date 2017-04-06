package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.ChangeQuotaCommand;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;

public class ChangeQuotaForDiskCommand extends ChangeQuotaCommand {

    @Inject
    private DiskDao diskDao;

    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;

    private DiskImage disk;

    public ChangeQuotaForDiskCommand(ChangeQuotaParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        Disk disk = diskDao.get(getParameters().getObjectId());
        if (disk == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
            return false;
        }
        if (!disk.getDiskStorageType().isInternal()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_DISK_OPERATION);
            return false;
        }
        this.disk = (DiskImage) disk;

        return super.validate();
    }

    @Override
    protected void executeCommand() {
        imageStorageDomainMapDao.updateQuotaForImageAndSnapshots(getParameters().getObjectId(),
                getParameters().getContainerId(),
                getParameters().getQuotaId());
        setSucceeded(true);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        if (!Objects.equals(getQuotaId(), disk.getQuotaId())) {
            if (disk.getQuotaId() != null && !Guid.Empty.equals(disk.getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(
                        disk.getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        getParameters().getContainerId(),
                        getDiskSize()));
            }
            list.add(new QuotaStorageConsumptionParameter(
                    getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    getParameters().getContainerId(),
                    getDiskSize()));
        }

        return list;
    }

    private double getDiskSize() {
        return disk.getSizeInGigabytes();
    }

}
