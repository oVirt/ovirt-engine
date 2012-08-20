package org.ovirt.engine.core.bll;

import java.util.Arrays;

import org.ovirt.engine.core.bll.quota.ChangeQuotaCommand;
import org.ovirt.engine.core.bll.quota.StorageQuotaValidationParameter;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class ChangeQuotaForDiskCommand extends ChangeQuotaCommand {

    private static final long serialVersionUID = 1165911489949172000L;
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
    public boolean validateAndSetQuota() {
        // no change to quota
        if (getQuotaId().equals(getDisk().getQuotaId())) {
            return true;
        }
        getQuotaManager().decreaseStorageQuota(getStoragePool(),
                Arrays.asList(new StorageQuotaValidationParameter(getDisk().getQuotaId(),
                        getParameters().getContainerId(),
                        getDiskSize())));
        if (getQuotaManager()
                .validateAndSetStorageQuota(getStoragePool(),
                        Arrays.asList(new StorageQuotaValidationParameter(getQuotaId(),
                                getParameters().getContainerId(),
                                getDiskSize())),
                        getReturnValue().getCanDoActionMessages())) {
            return true;
        }
        getQuotaManager().rollbackQuota(getStoragePool(),
                Arrays.asList(getQuotaId(), getDisk().getQuotaId()));
        return false;
    }

    @Override
    public void rollbackQuota() {
        getQuotaManager().rollbackQuota(getStoragePool(),
                Arrays.asList(getQuotaId(), getDisk().getQuotaId()));
    }

    private long getDiskSize() {
        return disk.getSizeInGigabytes();
    }

    private DiskImageBase getDisk() {
        return disk;
    }

}
