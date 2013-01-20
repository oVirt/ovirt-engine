package org.ovirt.engine.core.bll.lsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.utils.MultiValueMapUtils;

@LockIdNameAttribute
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class LiveMigrateVmDisksCommand<T extends LiveMigrateVmDisksParameters> extends CommandBase<T>
        implements TaskHandlerCommand<LiveMigrateVmDisksParameters>, QuotaStorageDependent {
    private static final long serialVersionUID = -6216729539906812205L;

    private Map<Guid, DiskImage> diskImagesMap = new HashMap<Guid, DiskImage>();
    private Map<Guid, storage_domains> storageDomainsMap = new HashMap<Guid, storage_domains>();

    public LiveMigrateVmDisksCommand(T parameters) {
        super(parameters);

        getParameters().setCommandType(getActionType());
        setVmId(getParameters().getVmId());
    }

    @Override
    protected List<SPMAsyncTaskHandler> initTaskHandlers() {
        return Arrays.<SPMAsyncTaskHandler> asList(
                new LiveSnapshotTaskHandler(this),
                new LiveMigrateDisksTaskHandler(this)
                );
    }

    /* Overridden stubs declared as public in order to implement ITaskHandlerCommand */

    @Override
    public T getParameters() {
        return super.getParameters();
    }

    @Override
    public Guid createTask(AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTask(asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    @Override
    public VdcActionType getActionType() {
        return super.getActionType();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            permissionList.add(new PermissionSubject(parameters.getImageId(),
                    VdcObjectType.Disk,
                    ActionGroup.CONFIGURE_DISK_STORAGE));

            setStoragePoolId(getVm().getStoragePoolId());

            addStoragePermissionByQuotaMode(permissionList,
                    getStoragePoolId().getValue(),
                    parameters.getStorageDomainId());
        }

        return permissionList;
    }

    @Override
    protected void executeCommand() {
        setSucceeded(true);
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(), LockingGroup.VM.name());
    }

    @Override
    public VM getVm() {
        VM vm = super.getVm();
        if (vm != null) {
            setVm(vm);
        }

        return vm;
    }

    protected DiskImageDAO getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }

    protected StorageDomainDAO getStorageDomainDao() {
        return getDbFacade().getStorageDomainDao();
    }

    private DiskImage getDiskImageById(Guid imageId) {
        if (diskImagesMap.containsKey(imageId)) {
            return diskImagesMap.get(imageId);
        }

        DiskImage diskImage = getDiskImageDao().get(imageId);
        diskImagesMap.put(imageId, diskImage);

        return diskImage;
    }

    private storage_domains getStorageDomainById(Guid storageDomainId, Guid storagePoolId) {
        if (storageDomainsMap.containsKey(storageDomainId)) {
            return storageDomainsMap.get(storageDomainId);
        }

        storage_domains storageDomain = getStorageDomainDao().getForStoragePool(storageDomainId, storagePoolId);
        storageDomainsMap.put(storageDomainId, storageDomain);

        return storageDomain;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            // If source and destination are in the same quota - return empty list
            DiskImage diskImage = getDiskImageById(parameters.getImageId());
            if (diskImage.getQuotaId() != null && diskImage.getQuotaId().equals(parameters.getQuotaId())) {
                return list;
            }

            list.add(new QuotaStorageConsumptionParameter(
                    parameters.getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    parameters.getStorageDomainId(),
                    (double) diskImage.getSizeInGigabytes()));

            if (diskImage.getQuotaId() != null && !Guid.Empty.equals(diskImage.getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(
                        diskImage.getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        parameters.getSourceDomainId().getValue(),
                        (double) diskImage.getSizeInGigabytes()));
            }
        }
        return list;
    }

    @Override
    protected boolean canDoAction() {
        if (!isValidParametersList() || !isLiveMigrationEnabled() || !checkImagesStatus()
                || !isValidSpaceRequirements()) {
            return false;
        }

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            getReturnValue().setCanDoAction(isDiskNotShareable(parameters.getImageId())
                    && isTemplateInDestStorageDomain(parameters.getImageId(), parameters.getStorageDomainId())
                    && validateSourceStorageDomain(parameters.getImageId(), parameters.getSourceStorageDomainId())
                    && validateDestStorage(parameters.getImageId(), parameters.getStorageDomainId()));

            if (!getReturnValue().getCanDoAction()) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidParametersList() {
        if (getParameters().getParametersList().isEmpty()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED);
        }

        return true;
    }

    protected boolean checkImagesStatus() {
        return ImagesHandler.checkImagesLocked(getVm(), getReturnValue().getCanDoActionMessages());
    }

    private boolean isLiveMigrationEnabled() {
        if (!Config.<Boolean> GetValue(
                ConfigValues.LiveStorageMigrationEnabled,
                getStoragePool().getcompatibility_version().toString())) {
            return failCanDoAction(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
        }

        return true;
    }

    private boolean isDiskNotShareable(Guid imageId) {
        DiskImage diskImage = getDiskImageById(imageId);

        if (diskImage.isShareable()) {
            addCanDoActionMessage(String.format("$%1$s %2$s", "diskAliases", diskImage.getDiskAlias()));
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_SHAREABLE_DISK_NOT_SUPPORTED);
        }

        return true;
    }

    private boolean isTemplateInDestStorageDomain(Guid imageId, Guid sourceDomainId) {
        Guid templateId = getDiskImageById(imageId).getit_guid();

        if (!Guid.Empty.equals(templateId)) {
            DiskImage templateImage = getDiskImageDao().get(templateId);
            if (!templateImage.getstorage_ids().contains(sourceDomainId)) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }

        return true;
    }

    private boolean validateSourceStorageDomain(Guid imageId, Guid sourceDomainId) {
        StorageDomainValidator validator = getValidator(imageId, sourceDomainId);

        return validator.isDomainExistAndActive(getReturnValue().getCanDoActionMessages());
    }

    private boolean validateDestStorage(Guid imageId, Guid destDomainId) {
        StorageDomainValidator validator = getValidator(imageId, destDomainId);

        return validateSourceStorageDomain(imageId, destDomainId)
                && validator.domainIsValidDestination(getReturnValue().getCanDoActionMessages());
    }

    private StorageDomainValidator getValidator(Guid imageId, Guid domainId) {
        DiskImage diskImage = getDiskImageById(imageId);

        return new StorageDomainValidator(
                getStorageDomainById(domainId, diskImage.getstorage_pool_id().getValue()));
    }

    protected boolean isValidSpaceRequirements() {
        Map<Guid, List<DiskImage>> storageDomainsImagesMap = new HashMap<Guid, List<DiskImage>>();

        for (LiveMigrateDiskParameters parameters : getParameters().getParametersList()) {
            MultiValueMapUtils.addToMap(parameters.getStorageDomainId(),
                    getDiskImageById(parameters.getImageId()),
                    storageDomainsImagesMap);
        }

        for (Map.Entry<Guid, List<DiskImage>> entry : storageDomainsImagesMap.entrySet()) {
            Guid destDomainId = entry.getKey();
            List<DiskImage> disksList = entry.getValue();
            Guid storagePoolId = disksList.get(0).getstorage_pool_id().getValue();
            storage_domains destDomain = getStorageDomainById(destDomainId, storagePoolId);

            if (!StorageDomainSpaceChecker.isBelowThresholds(destDomain)) {
                addCanDoActionMessage(String.format("$%1$s %2$s", "storageName", destDomain.getstorage_name()));
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_TARGET_STORAGE_DOMAIN);
            }

            long totalImagesSize = 0;
            for (DiskImage diskImage : disksList) {
                Guid templateId = diskImage.getit_guid();
                List<DiskImage> allImageSnapshots =
                        ImagesHandler.getAllImageSnapshots(diskImage.getImageId(), templateId);

                diskImage.getSnapshots().addAll(allImageSnapshots);
                totalImagesSize += Math.round(diskImage.getActualDiskWithSnapshotsSize());
            }

            if (!StorageDomainSpaceChecker.hasSpaceForRequest(destDomain, totalImagesSize)) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
            }
        }

        return true;
    }

}
