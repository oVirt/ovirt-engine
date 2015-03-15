package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class ImportVmTemplateFromConfigurationCommand<T extends ImportVmTemplateParameters> extends ImportVmTemplateCommand {

    private static final Logger log = LoggerFactory.getLogger(ImportVmFromConfigurationCommand.class);
    private OvfEntityData ovfEntityData;
    VmTemplate vmTemplateFromConfiguration;

    protected ImportVmTemplateFromConfigurationCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmTemplateFromConfigurationCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    public Guid getVmTemplateId() {
        if (isImagesAlreadyOnTarget()) {
            return getParameters().getContainerId();
        }
        return super.getVmTemplateId();
    }

    @Override
    protected boolean canDoAction() {
        initVmTemplate();
        ArrayList<DiskImage> disks = new ArrayList(getVmTemplate().getDiskTemplateMap().values());
        setImagesWithStoragePoolId(getStorageDomain().getStoragePoolId(), disks);
        getVmTemplate().setImages(disks);
        if (isImagesAlreadyOnTarget() && !validateUnregisteredEntity(vmTemplateFromConfiguration, ovfEntityData)) {
            return false;
        }
        return super.canDoAction();
    }

    private void initVmTemplate() {
        OvfHelper ovfHelper = new OvfHelper();
        List<OvfEntityData> ovfEntityList =
                getUnregisteredOVFDataDao().getByEntityIdAndStorageDomain(getParameters().getContainerId(),
                        getParameters().getStorageDomainId());
        if (!ovfEntityList.isEmpty()) {
            try {
                // We should get only one entity, since we fetched the entity with a specific Storage Domain
                ovfEntityData = ovfEntityList.get(0);
                vmTemplateFromConfiguration = ovfHelper.readVmTemplateFromOvf(ovfEntityData.getOvfData());
                vmTemplateFromConfiguration.setVdsGroupId(getParameters().getVdsGroupId());
                setVmTemplate(vmTemplateFromConfiguration);
                getParameters().setVmTemplate(vmTemplateFromConfiguration);
                getParameters().setDestDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setSourceDomainId(ovfEntityData.getStorageDomainId());

                // For quota, update disks when required
                if (getParameters().getDiskTemplateMap() != null) {
                    ArrayList imageList = new ArrayList<>(getParameters().getDiskTemplateMap().values());
                    vmTemplateFromConfiguration.setDiskList(imageList);
                    ensureDomainMap(imageList, getParameters().getDestDomainId());
                }
            } catch (OvfReaderException e) {
                log.error("Failed to parse a given ovf configuration: {}:\n{}",
                        e.getMessage(),
                        ovfEntityData.getOvfData());
                log.debug("Exception", e);
            }
        }
        setVdsGroupId(getParameters().getVdsGroupId());
        setStoragePoolId(getVdsGroup().getStoragePoolId());
    }

    @Override
    protected ArrayList<DiskImage> getImages() {
        return new ArrayList<>(getParameters().getDiskTemplateMap() != null ?
                getParameters().getDiskTemplateMap().values() : getVmTemplate().getDiskTemplateMap().values());
    }

    @Override
    public void executeCommand() {
        super.executeCommand();
        if (isImagesAlreadyOnTarget()) {
            if (!getImages().isEmpty()) {
                findAndSaveDiskCopies();
            }
            getUnregisteredOVFDataDao().removeEntity(ovfEntityData.getEntityId(), null);
        }
        setActionReturnValue(getVmTemplate().getId());
        setSucceeded(true);
    }

    private void findAndSaveDiskCopies() {
        List<OvfEntityData> ovfEntityDataList =
                getUnregisteredOVFDataDao().getByEntityIdAndStorageDomain(ovfEntityData.getEntityId(), null);
        List<ImageStorageDomainMap> copiedTemplateDisks = new LinkedList<>();
        for (OvfEntityData ovfEntityDataFetched : ovfEntityDataList) {
            populateDisksCopies(copiedTemplateDisks,
                    getImages(),
                    ovfEntityDataFetched.getStorageDomainId());
        }
        saveImageStorageDomainMapList(copiedTemplateDisks);
    }

    private void populateDisksCopies(List<ImageStorageDomainMap> copiedTemplateDisks,
            List<DiskImage> originalTemplateImages,
            Guid storageDomainId) {
        List<Guid> imagesContainedInStorageDomain = getImagesGuidFromStorage(storageDomainId, getStoragePoolId());
        for (DiskImage templateDiskImage : originalTemplateImages) {
            if (storageDomainId.equals(templateDiskImage.getStorageIds().get(0))) {
                // The original Storage Domain was already saved. skipping it.
                continue;
            }
            if (imagesContainedInStorageDomain.contains(templateDiskImage.getId())) {
                log.info("Found a copied image of '{}' on Storage Domain id '{}'",
                        templateDiskImage.getId(),
                        storageDomainId);
                ImageStorageDomainMap imageStorageDomainMap =
                        new ImageStorageDomainMap(templateDiskImage.getImageId(),
                                storageDomainId,
                                templateDiskImage.getQuotaId(),
                                templateDiskImage.getDiskProfileId());
                copiedTemplateDisks.add(imageStorageDomainMap);
            }
        }
    }

    private List<Guid> getImagesGuidFromStorage(Guid storageDomainId, Guid storagePoolId) {
        List<Guid> imagesList = Collections.emptyList();
        try {
            VDSReturnValue imagesListResult = runVdsCommand(VDSCommandType.GetImagesList,
                    new GetImagesListVDSCommandParameters(storageDomainId, storagePoolId));
            if (imagesListResult.getSucceeded()) {
                imagesList = (List<Guid>) imagesListResult.getReturnValue();
            } else {
                log.error("Unable to get images list for storage domain, can not update copied template disks related to Storage Domain id '{}'",
                        storageDomainId);
            }
        } catch (Exception e) {
            log.error("Unable to get images list for storage domain, can not update copied template disks related to Storage Domain id '{}'. error is: {}",
                    storageDomainId,
                    e);
        }
        return imagesList;
    }

    private void saveImageStorageDomainMapList(final List<ImageStorageDomainMap> copiedTemplateDisks) {
        if (!copiedTemplateDisks.isEmpty()) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    for (ImageStorageDomainMap imageStorageDomainMap : copiedTemplateDisks) {
                        getImageStorageDomainMapDao().save(imageStorageDomainMap);
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.TEMPLATE_IMPORT_FROM_CONFIGURATION_SUCCESS :
                AuditLogType.TEMPLATE_IMPORT_FROM_CONFIGURATION_FAILED;
    }
}
