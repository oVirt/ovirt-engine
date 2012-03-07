package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This abstract class holds helper methods for concrete command classes that require to add a VM and clone an image in
 * the process
 *
 * @param <T>
 */
public abstract class AddVmAndCloneImageCommand<T extends VmManagementParametersBase> extends AddVmCommand<T> {

    protected AddVmAndCloneImageCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmAndCloneImageCommand(T parameters) {
        super(parameters);
    }

    protected void copyDiskImage(Guid destVmId,
            DiskImage diskImage,
            Guid srcStorageDomainId,
            Guid destStorageDomainId,
            VdcActionType parentCommandType) {
        DiskImage newDiskImage = cloneDiskImage(getVmId(),
                destStorageDomainId,
                Guid.NewGuid(),
                Guid.NewGuid(),
                diskImage);
        MoveOrCopyImageGroupParameters parameters = createCopyParameters(newDiskImage,
                srcStorageDomainId,
                diskImage.getimage_group_id(),
                diskImage.getId(), parentCommandType);
        VdcReturnValueBase result = executeChildCopyingCommand(parameters);
        handleCopyResult(newDiskImage, parameters, result);
    }

    protected MoveOrCopyImageGroupParameters createCopyParameters(DiskImage diskImage,
            Guid srcStorageDomainId,
            Guid srcImageGroupId,
            Guid srcImageId, VdcActionType parentCommandType) {
        MoveOrCopyImageGroupParameters params =
                new MoveOrCopyImageGroupParameters(diskImage.getvm_guid(),
                        srcImageGroupId,
                        srcImageId,
                        diskImage.getimage_group_id(),
                        diskImage.getId(),
                        ImagesHandler.getStorageDomainId(diskImage),
                        ImageOperation.Copy);
        params.setAddImageDomainMapping(false);
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setVolumeFormat(diskImage.getvolume_format());
        params.setVolumeType(diskImage.getvolume_type());
        params.setUseCopyCollapse(true);
        params.setSourceDomainId(srcStorageDomainId);
        params.setWipeAfterDelete(diskImage.getwipe_after_delete());
        params.setParentParemeters(getParameters());
        params.setParentCommand(parentCommandType);
        return params;
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = false;
        if (super.canDoAction()) {
            for (DiskImage diskImage : getDiskMap().values()) {
                retValue = checkImageConfiguration(diskImage);
                if (!retValue) {
                    break;
                }
            }
            retValue = true;
        }
        return retValue;
    }

    protected abstract boolean checkImageConfiguration(DiskImage diskImage);

    protected DiskImage cloneDiskImage(Guid newVmId,
            Guid storageDomainId,
            Guid newImageGroupId,
            Guid newImageGuid,
            DiskImage srcDiskImage) {
        DiskImage retDiskImage = DiskImage.copyOf(srcDiskImage);
        retDiskImage.setId(newImageGuid);
        retDiskImage.setdescription(ImagesHandler.calculateImageDescription(getParameters().getVmStaticData()
                .getvm_name()));
        retDiskImage.setParentId(Guid.Empty);
        retDiskImage.setvm_snapshot_id(Guid.Empty);
        retDiskImage.setvm_guid(newVmId);
        retDiskImage.setimage_group_id(newImageGroupId);
        retDiskImage.setlast_modified_date(new Date());
        retDiskImage.setvolume_format(srcDiskImage.getvolume_format());
        retDiskImage.setvolume_type(srcDiskImage.getvolume_type());
        ImagesHandler.setStorageDomainId(retDiskImage, storageDomainId);
        return retDiskImage;
    }

    /**
     * Handle the result of copying the image
     *
     * @param copiedDiskImage
     *            disk image that represents the copied image at VDSM
     * @param parameters
     *            parameters for the child command that executes the copy at VDSM
     * @param result
     *            result of execution of child command
     */
    protected void handleCopyResult(DiskImage copiedDiskImage,
            VdcActionParametersBase parameters,
            VdcReturnValueBase result) {
        getParameters().getImagesParameters().add(parameters);
        /**
         * if couldnt create copy, abort
         */
        if (!result.getSucceeded()) {
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError);
        } else {
            ImagesHandler.addDiskImageWithNoVmDevice(copiedDiskImage);
            getTaskIdList().addAll(result.getInternalTaskIdList());
        }
    }

    /**
     * Executes the child command responsible for the image copying
     * @param parameters
     *            parameters for copy
     * @return
     */
    protected VdcReturnValueBase executeChildCopyingCommand(VdcActionParametersBase parameters) {
        VdcReturnValueBase result = Backend.getInstance().runInternalAction(
                getChildActionType(),
                        parameters,
                        ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        return result;
    }

    @Override
    protected boolean buildAndCheckDestStorageDomains() {
        if (imageToDestinationDomainMap.isEmpty()) {
            List<storage_domains> domains =
                    DbFacade.getInstance()
                            .getStorageDomainDAO()
                            .getAllForStoragePool(getVmTemplate().getstorage_pool_id().getValue());
            Map<Guid, storage_domains> storageDomainsMap = new HashMap<Guid, storage_domains>();
            for (storage_domains storageDomain : domains) {
                StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
                ArrayList<String> messages = new ArrayList<String>();
                if (validator.isDomainExistAndActive(messages) && validator.domainIsValidDestination(messages)) {
                    storageDomainsMap.put(storageDomain.getId(), storageDomain);
                }
            }
            for (DiskImage image : getDiskMap().values()) {
                for (Guid storageId : image.getstorage_ids()) {
                    if (storageDomainsMap.containsKey(storageId)) {
                        imageToDestinationDomainMap.put(image.getId(), storageId);
                        break;
                    }
                }
            }
            if (getDiskMap().values().size() != imageToDestinationDomainMap.size()) {
                logErrorOneOrMoreActiveDomainsAreMissing();
                return false;
            }
            for (Guid storageDomainId : new HashSet<Guid>(imageToDestinationDomainMap.values())) {
                destStorages.put(storageDomainId, storageDomainsMap.get(storageDomainId));
            }
            return true;
        }
        return super.buildAndCheckDestStorageDomains();
    }

    /**
     * Logs error if one or more active domains are missing for disk images
     */
    protected abstract void logErrorOneOrMoreActiveDomainsAreMissing();

    /**
     * Returns collection of DiskImage objects to use for construction of the imageTODestionationDomainMap
     *
     * @return
     */
    protected abstract Map<String, DiskImage> getDiskMap();

    @Override
    protected DiskImage getDiskImageToRemoveByParam(VdcActionParametersBase param) {
        MoveOrCopyImageGroupParameters moveOrCopyImageGroupParams = (MoveOrCopyImageGroupParameters) param;
        Guid imageGroupId = moveOrCopyImageGroupParams.getDestImageGroupId();
        Guid imageId = moveOrCopyImageGroupParams.getDestinationImageId();
        Guid vmId = moveOrCopyImageGroupParams.getContainerId();
        DiskImage diskImage = new DiskImage();
        diskImage.setvm_guid(vmId);
        diskImage.setimage_group_id(imageGroupId);
        diskImage.setId(imageId);
        return diskImage;
    }

}
