package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.quota.ChangeQuotaCommand;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class ChangeQuotaForDiskCommand extends ChangeQuotaCommand {

    private DiskImageBase disk;

    public ChangeQuotaForDiskCommand(ChangeQuotaParameters params) {
        super(params);
    }

    @Override
    protected boolean canDoAction() {
        Disk disk = getDbFacade().getDiskDao().get(getParameters().getObjectId());
        if (disk == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
            return false;
        }
        if (disk.getDiskStorageType() != DiskStorageType.IMAGE) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_DISK_OPERATION);
            return false;
        }
        this.disk = (DiskImageBase) disk;

        return super.canDoAction();
    }

    @Override
    protected void executeCommand() {
        getDbFacade().getImageDao().updateQuotaForImageAndSnapshots(getParameters().getObjectId(),
                getParameters().getQuotaId());
        setSucceeded(true);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        if (!getQuotaId().equals(getDisk().getQuotaId())) {
            if (getDisk().getQuotaId() != null && !Guid.Empty.equals(getDisk().getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(
                        getDisk().getQuotaId(),
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

    private DiskImageBase getDisk() {
        return disk;
    }

}
