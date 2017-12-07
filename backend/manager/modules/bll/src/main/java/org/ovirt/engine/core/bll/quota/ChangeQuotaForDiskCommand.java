package org.ovirt.engine.core.bll.quota;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.QuotaDao;

public class ChangeQuotaForDiskCommand extends CommandBase<ChangeQuotaParameters> implements QuotaStorageDependent {

    @Inject
    private DiskDao diskDao;

    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;

    @Inject
    private QuotaDao quotaDao;

    private DiskImage disk;

    public ChangeQuotaForDiskCommand(ChangeQuotaParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
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

        // check if SP exist
        StoragePoolValidator spValidator = new StoragePoolValidator(getStoragePool());
        if (!validate(spValidator.exists())) {
            return false;
        }
        // Check if quota exist:
        if (getQuotaId() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
            return false;
        }
        if (quotaDao.getById(getQuotaId()) == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }
        return true;
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
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        getParameters().getContainerId(),
                        getDiskSize()));
            }
            list.add(new QuotaStorageConsumptionParameter(
                    getQuotaId(),
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    getParameters().getContainerId(),
                    getDiskSize()));
        }

        return list;
    }

    private double getDiskSize() {
        return disk.getSizeInGigabytes();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getQuotaId(),
                VdcObjectType.Quota,
                getActionType().getActionGroup()));
        return permissionList;
    }

    protected Guid getQuotaId() {
        return getParameters().getQuotaId();
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        // no implementation here already checked in getPermissionCheckSubjects
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ASSIGN);
        addValidationMessage(EngineMessage.VAR__TYPE__QUOTA);
    }
}
