package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmTemplateCommand<T extends ImportVmTemplateParameters> extends ImportVmTemplateCommandBase<T> {

    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    protected ImageDao imageDao;

    private Map<Guid, QemuImageInfo> diskImageInfoMap = new HashMap<>();

    public ImportVmTemplateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public ImportVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validateSourceStorageDomain() {
        Guid sourceDomainId = getParameters().getSourceDomainId();
        StorageDomain sourceDomain = !Guid.isNullOrEmpty(sourceDomainId) ?
                storageDomainDao.getForStoragePool(sourceDomainId, getStoragePool().getId()) :
                null;

        if (!validate(new StorageDomainValidator(sourceDomain).isDomainExistAndActive())) {
            return false;
        }

        if ((sourceDomain.getStorageDomainType() != StorageDomainType.ImportExport)
                && !getParameters().isImagesExistOnTargetStorageDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        if (!getParameters().isImagesExistOnTargetStorageDomain()) {
            // Set the template images from the Export domain and change each image id storage is to the import domain
            GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(getParameters()
                    .getStoragePoolId(), getParameters().getSourceDomainId());
            QueryReturnValue qretVal = runInternalQuery(
                    QueryType.GetTemplatesFromExportDomain, tempVar);
            if (!qretVal.getSucceeded()) {
                return false;
            }

            Map<VmTemplate, List<DiskImage>> templates = qretVal.getReturnValue();
            ArrayList<DiskImage> images = new ArrayList<>();
            for (Map.Entry<VmTemplate, List<DiskImage>> entry : templates.entrySet()) {
                if (entry.getKey().getId().equals(getVmTemplate().getId())) {
                    images = new ArrayList<>(entry.getValue());
                    getVmTemplate().setInterfaces(entry.getKey().getInterfaces());
                    getVmTemplate().setOvfVersion(entry.getKey().getOvfVersion());
                    break;
                }
            }
            getParameters().setImages(images);
            getVmTemplate().setImages(images);
            ensureDomainMap(getImages(), getParameters().getDestDomainId());
            Map<Guid, DiskImage> imageMap = new HashMap<>();
            for (DiskImage image : images) {
                if (Guid.Empty.equals(image.getVmSnapshotId())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
                }

                StorageDomain storageDomain = storageDomainDao.getForStoragePool(
                        imageToDestinationDomainMap.get(image.getId()),
                        getStoragePool().getId());

                StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
                if (!validate(validator.isDomainExistAndActive()) ||
                        !validate(validator.domainIsValidDestination())) {
                    return false;
                }

                StorageDomainStatic targetDomain = storageDomain.getStorageStaticData();
                changeRawToCowIfSparseOnBlockDevice(targetDomain.getStorageType(), image);
                if (!ImagesHandler.checkImageConfiguration(targetDomain, image,
                        getReturnValue().getValidationMessages())) {
                    return false;
                }

                image.setStoragePoolId(getParameters().getStoragePoolId());
                image.setStorageIds(new ArrayList<>(Collections.singletonList(storageDomain.getId())));
                imageMap.put(image.getImageId(), image);
            }
            getVmTemplate().setDiskImageMap(imageMap);
        }

        return true;
    }

    @Override
    protected void initImportClonedTemplateDisks() {
        for (DiskImage image : getImages()) {
            // Update the virtual size with value queried from 'qemu-img info'
            // If the image is import from an export domain, we have to query it on
            // the export domain and not on the target domain.
            // If we import from a Storage Domain, we'll need to use the image's Storage
            // Domain ID and not the target domain's.
            if (!getParameters().isImagesExistOnTargetStorageDomain()) {
                updateDiskSizeByQcowImageInfo(image);
            } else {
                updateDiskSizeByQcowImageInfo(image, image.getStorageIds().get(0));
            }

            if (getParameters().isImportAsNewEntity()) {
                generateNewDiskId(image);
                updateManagedDeviceMap(image, getVmTemplate().getManagedDeviceMap());
            } else {
                originalDiskIdMap.put(image.getId(), image.getId());
                originalDiskImageIdMap.put(image.getId(), image.getImageId());
            }
        }
    }

    /**
     * Change the image format to {@link VolumeFormat#COW} in case the SD is a block device and the image format is
     * {@link VolumeFormat#RAW} and the type is {@link VolumeType#Sparse}.
     *
     * @param storageType
     *            The domain type.
     * @param image
     *            The image to check and change if needed.
     */
    private void changeRawToCowIfSparseOnBlockDevice(StorageType storageType, DiskImage image) {
        if (storageType.isBlockDomain()
                && image.getVolumeFormat() == VolumeFormat.RAW
                && image.getVolumeType() == VolumeType.Sparse) {
            image.setVolumeFormat(VolumeFormat.COW);
        }
    }

    @Override
    protected void copyImagesToTargetDomain() {
        TransactionSupport.executeInNewTransaction(() -> {
            for (DiskImage disk : getImages()) {
                Guid originalDiskId = originalDiskIdMap.get(disk.getId());
                Guid destinationDomain = imageToDestinationDomainMap.get(originalDiskId);

                ActionReturnValue vdcRetValue = runInternalActionWithTasksContext(
                        ActionType.CopyImageGroup,
                        buildMoveOrCopyImageGroupParameters(getVmTemplateId(), disk, originalDiskId, destinationDomain));

                if (!vdcRetValue.getSucceeded()) {
                    throw vdcRetValue.getFault() != null ? new EngineException(vdcRetValue.getFault().getError())
                            : new EngineException(EngineError.ENGINE);
                }

                getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
            }
            return null;
        });
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParameters(final Guid templateId,
            DiskImage disk,
            Guid originalDiskId,
            Guid destinationDomain) {
        MoveOrCopyImageGroupParameters p =
                new MoveOrCopyImageGroupParameters(templateId,
                        originalDiskId,
                        originalDiskImageIdMap.get(disk.getId()),
                        disk.getId(),
                        disk.getImageId(),
                        destinationDomain,
                        ImageOperation.Copy);

        p.setParentCommand(getActionType());
        p.setUseCopyCollapse(true);
        p.setVolumeType(disk.getVolumeType());
        p.setVolumeFormat(disk.getVolumeFormat());
        p.setCopyVolumeType(CopyVolumeType.SharedVol);
        p.setSourceDomainId(getParameters().getSourceDomainId());
        p.setForceOverride(getParameters().getForceOverride());
        p.setImportEntity(true);
        p.setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, templateId));
        p.setRevertDbOperationScope(ImageDbOperationScope.IMAGE);
        for (DiskImage diskImage : getParameters().getVmTemplate().getDiskList()) {
            if (originalDiskId.equals(diskImage.getId())) {
                p.setQuotaId(diskImage.getQuotaId());
                p.setDiskProfileId(diskImage.getDiskProfileId());
                break;
            }
        }

        p.setParentParameters(getParameters());
        return p;
    }

    @Override
    protected void addDisksToDb() {
        int count = 1;
        for (DiskImage image : getImages()) {
            image.setActive(true);
            ImageStorageDomainMap map = imagesHandler.saveImage(image);
            getCompensationContext().snapshotNewEntity(image.getImage());
            getCompensationContext().snapshotNewEntity(map);
            if (!baseDiskDao.exists(image.getId())) {
                image.setDiskAlias(ImagesHandler.getSuggestedDiskAlias(image, getVmTemplateName(), count));
                count++;
                baseDiskDao.save(image);
                getCompensationContext().snapshotNewEntity(image);
            }

            DiskImageDynamic diskDynamic = new DiskImageDynamic();
            diskDynamic.setId(image.getImageId());
            diskDynamic.setActualSize(image.getActualSizeInBytes());
            diskImageDynamicDao.save(diskDynamic);

            DiskVmElement dve = DiskVmElement.copyOf(image.getDiskVmElementForVm(getSourceTemplateId()),
                    image.getId(), getVmTemplateId());
            diskVmElementDao.save(dve);

            getCompensationContext().snapshotNewEntity(diskDynamic);
        }
    }

    private void updateDiskSizeByQcowImageInfo(DiskImage diskImage) {
        updateDiskSizeByQcowImageInfo(diskImage, getParameters().getSourceDomainId());
    }

    protected void updateDiskSizeByQcowImageInfo(DiskImage diskImage, Guid storageId) {
        QemuImageInfo qemuImageInfo = getQemuImageInfo(diskImage, storageId);
        if (qemuImageInfo != null) {
            diskImage.setSize(qemuImageInfo.getSize());
        }
        imageDao.update(diskImage.getImage());
    }

    protected QemuImageInfo getQemuImageInfo(DiskImage diskImage, Guid storageId) {
        if (!diskImageInfoMap.containsKey(diskImage.getId())) {
            diskImageInfoMap.put(diskImage.getId(),
                    imagesHandler.getQemuImageInfoFromVdsm(diskImage.getStoragePoolId(),
                            storageId,
                            diskImage.getId(),
                            diskImage.getImageId(),
                            null,
                            true));
        }
        return diskImageInfoMap.get(diskImage.getId());
    }
}
