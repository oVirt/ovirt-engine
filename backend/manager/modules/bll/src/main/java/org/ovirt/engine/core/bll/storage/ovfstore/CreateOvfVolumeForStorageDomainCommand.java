package org.ovirt.engine.core.bll.storage.ovfstore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.CreateOvfVolumeForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.OvfInfoFileConstants;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateOvfVolumeForStorageDomainCommand<T extends CreateOvfVolumeForStorageDomainCommandParameters> extends StorageDomainCommandBase<T> {

    public CreateOvfVolumeForStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStorageDomainId(getParameters().getStorageDomainId());
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    public CreateOvfVolumeForStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override

    protected void executeCommand() {
        AddDiskParameters diskParameters = new AddDiskParameters(null, createDisk(getStorageDomainId()));
        diskParameters.setStorageDomainId(getStorageDomainId());
        diskParameters.setParentCommand(getParameters().getParentCommand());
        diskParameters.setParentParameters(getParameters().getParentParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        diskParameters.setSkipDomainCheck(getParameters().isSkipDomainChecks());
        VdcReturnValueBase vdcReturnValueBase =
                runInternalActionWithTasksContext(VdcActionType.AddDisk, diskParameters);
        Guid createdId = (Guid)vdcReturnValueBase.getActionReturnValue();

        if (createdId != null) {
            addStorageDomainOvfInfoToDb(createdId);
        }

        if (!vdcReturnValueBase.getSucceeded()) {
            if (createdId != null) {
                addCustomValue("DiskId", createdId.toString());
                auditLogDirector.log(this, AuditLogType.CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_FAILED);
            } else {
                auditLogDirector.log(this, AuditLogType.CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_INITIATE_FAILED);
            }
            setSucceeded(false);
        }

        getReturnValue().getInternalVdsmTaskIdList().addAll(vdcReturnValueBase.getInternalVdsmTaskIdList());
        setSucceeded(true);
    }

    private boolean shouldOvfStoreBeShareable() {
        // we don't create shareable disks on gluster domains to avoid
        // split brain - see BZ 1024654
        return getStorageDomain().getStorageType() != StorageType.GLUSTERFS;
    }

    public DiskImage createDisk(Guid domainId) {
        DiskImage createdDiskImage = new DiskImage();
        createdDiskImage.setDiskInterface(DiskInterface.IDE);
        createdDiskImage.setWipeAfterDelete(false);
        createdDiskImage.setDiskAlias(OvfInfoFileConstants.OvfStoreDescriptionLabel);
        createdDiskImage.setDiskDescription(OvfInfoFileConstants.OvfStoreDescriptionLabel);
        createdDiskImage.setShareable(shouldOvfStoreBeShareable());
        createdDiskImage.setStorageIds(new ArrayList<>(Arrays.asList(domainId)));
        createdDiskImage.setSize(SizeConverter.BYTES_IN_MB * 128);
        createdDiskImage.setvolumeFormat(VolumeFormat.RAW);
        createdDiskImage.setVolumeType(VolumeType.Preallocated);
        createdDiskImage.setDescription("OVF store for domain " + domainId);
        Date creationDate = new Date();
        createdDiskImage.setCreationDate(creationDate);
        createdDiskImage.setLastModified(creationDate);
        return createdDiskImage;
    }

    private void addStorageDomainOvfInfoToDb(Guid diskId) {
        StorageDomainOvfInfo storageDomainOvfInfo =
                new StorageDomainOvfInfo(getStorageDomainId(), null, diskId, StorageDomainOvfInfoStatus.DISABLED, null);
        getStorageDomainOvfInfoDao().save(storageDomainOvfInfo);
    }
}
