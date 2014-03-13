package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateOvfVolumeForStorageDomainCommand<T extends StorageDomainParametersBase> extends StorageDomainCommandBase<T> {
    public CreateOvfVolumeForStorageDomainCommand(T parameters) {
        super(parameters);
        setStorageDomainId(getParameters().getStorageDomainId());
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    public CreateOvfVolumeForStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override

    protected void executeCommand() {
        DiskImage createdDisk = createDisk(getStorageDomainId());
        AddDiskParameters diskParameters = new AddDiskParameters(null, createDisk(getStorageDomainId()));
        diskParameters.setStorageDomainId(getStorageDomainId());
        diskParameters.setParentCommand(VdcActionType.CreateOvfVolumeForStorageDomain);
        diskParameters.setParentParameters(getParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        VdcReturnValueBase vdcReturnValueBase = Backend.getInstance().runInternalAction(VdcActionType.AddDisk, diskParameters,
                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        Guid createdId = (Guid)vdcReturnValueBase.getActionReturnValue();

        if (createdId != null) {
            addStorageDomainOvfInfoToDb(createdId);
        }

        if (!vdcReturnValueBase.getSucceeded()) {
            addCustomValue("DiskAlias", createdDisk.getDiskAlias());
            if (createdId != null) {
                AuditLogDirector.log(this, AuditLogType.CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_FAILED);
            } else {
                AuditLogDirector.log(this, AuditLogType.CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_INITIATE_FAILED);
            }
            setSucceeded(false);
        }

        getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValueBase.getInternalVdsmTaskIdList());
        setSucceeded(true);
    }

    public DiskImage createDisk(Guid domainId) {
        DiskImage mNewCreatedDiskImage = new DiskImage();
        mNewCreatedDiskImage.setDiskInterface(DiskInterface.IDE);
        mNewCreatedDiskImage.setWipeAfterDelete(false);
        mNewCreatedDiskImage.setDiskAlias("OVF_STORE");
        mNewCreatedDiskImage.setDiskDescription("OVF_STORE");
        mNewCreatedDiskImage.setShareable(true);
        mNewCreatedDiskImage.setStorageIds(new ArrayList<>(Arrays.asList(domainId)));
        mNewCreatedDiskImage.setSize(SizeConverter.BYTES_IN_MB * 128);
        mNewCreatedDiskImage.setvolumeFormat(VolumeFormat.RAW);
        mNewCreatedDiskImage.setVolumeType(VolumeType.Preallocated);
        mNewCreatedDiskImage.setDescription("OVF store for domain " + domainId);
        Date creationDate = new Date();
        mNewCreatedDiskImage.setCreationDate(creationDate);
        mNewCreatedDiskImage.setLastModified(creationDate);
        return mNewCreatedDiskImage;
    }

    private void addStorageDomainOvfInfoToDb(Guid diskId) {
        StorageDomainOvfInfo storageDomainOvfInfo =
                new StorageDomainOvfInfo(getStorageDomainId(), null, diskId, StorageDomainOvfInfoStatus.DISABLED, null);
        getStorageDomainOvfInfoDao().save(storageDomainOvfInfo);
    }

    @Override
    protected void endSuccessfully() {
        endChildCommands();
        Guid diskId = ((AddImageFromScratchParameters) getParameters().getImagesParameters()
                .get(0)).getDiskInfo().getId();
        StorageDomainOvfInfo storageDomainOvfInfoDb =
                getStorageDomainOvfInfoDao()
                        .get(diskId);
        storageDomainOvfInfoDb.setStatus(StorageDomainOvfInfoStatus.OUTDATED);
        getStorageDomainOvfInfoDao().update(storageDomainOvfInfoDb);
        getBackend().runInternalAction(VdcActionType.ProcessOvfUpdateForStorageDomain, getParameters());
        setSucceeded(true);
    }

    protected StorageDomainOvfInfoDao getStorageDomainOvfInfoDao() {
        return getDbFacade().getStorageDomainOvfInfoDao();
    }

    private void endChildCommands() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            getBackend().endAction(p.getCommandType(), p);
        }
    }

    @Override
    protected void endWithFailure() {
        endChildCommands();
        AuditLogDirector.log(this, AuditLogType.CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_FAILED);
        setSucceeded(true);
    }
}
