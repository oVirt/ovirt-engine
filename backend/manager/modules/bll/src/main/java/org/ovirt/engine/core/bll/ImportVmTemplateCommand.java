package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmTemplateCommand extends MoveOrCopyTemplateCommand<ImportVmTemplateParameters>
        implements QuotaStorageDependent {

    private final List<Guid> diskGuidList = new ArrayList<Guid>();
    private final List<Guid> imageGuidList = new ArrayList<Guid>();

    public ImportVmTemplateCommand(ImportVmTemplateParameters parameters) {
        super(parameters);
        setVmTemplate(parameters.getVmTemplate());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplate().getId()));
        setStoragePoolId(parameters.getStoragePoolId());
        setVdsGroupId(parameters.getVdsGroupId());
        setStorageDomainId(parameters.getStorageDomainId());
    }

    protected ImportVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = true;
        if (getVmTemplate() == null) {
            retVal = false;
        } else {
            setDescription(getVmTemplateName());
        }
        // check that the storage pool is valid
        retVal = retVal && checkStoragePool();

        if (retVal) {
            // set the source domain and check that it is ImportExport type and active
            setSourceDomainId(getParameters().getSourceDomainId());
            StorageDomainValidator sourceDomainValidator = new StorageDomainValidator(getSourceDomain());
            retVal = validate(sourceDomainValidator.isDomainExistAndActive());
        }

        if (retVal && getSourceDomain().getStorageDomainType() != StorageDomainType.ImportExport) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            retVal = false;
        }

        if (retVal) {
            // Set the template images from the Export domain and change each image id storage is to the import domain
            GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(getParameters()
                    .getStoragePoolId(), getParameters().getSourceDomainId());
            VdcQueryReturnValue qretVal = getBackend().runInternalQuery(
                    VdcQueryType.GetTemplatesFromExportDomain, tempVar);
            retVal = qretVal.getSucceeded();
            if (retVal) {
                Map<VmTemplate, List<DiskImage>> templates = (Map) qretVal.getReturnValue();
                ArrayList<DiskImage> images = new ArrayList<DiskImage>();
                for (Map.Entry<VmTemplate, List<DiskImage>> entry : templates.entrySet()) {
                    if (entry.getKey().getId().equals(getVmTemplate().getId())) {
                        images = new ArrayList<DiskImage>(entry.getValue());
                        getVmTemplate().setInterfaces(entry.getKey().getInterfaces());
                        getVmTemplate().setOvfVersion(entry.getKey().getOvfVersion());
                        break;
                    }
                }
                getParameters().setImages(images);
                getVmTemplate().setImages(images);
                ensureDomainMap(getParameters().getImages(), getParameters().getDestDomainId());
                Map<Guid, DiskImage> imageMap = new HashMap<Guid, DiskImage>();
                for (DiskImage image : images) {
                    if (Guid.Empty.equals(image.getVmSnapshotId())) {
                        retVal = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
                        break;
                    }

                    StorageDomain storageDomain =
                            getStorageDomain(imageToDestinationDomainMap.get(image.getId()));
                    StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
                    retVal = validate(validator.isDomainExistAndActive()) &&
                            validate(validator.domainIsValidDestination());
                    if (!retVal) {
                        break;
                    }
                    StorageDomainStatic targetDomain = storageDomain.getStorageStaticData();
                    changeRawToCowIfSparseOnBlockDevice(targetDomain.getStorageType(), image);
                    retVal = ImagesHandler.CheckImageConfiguration(targetDomain, image,
                            getReturnValue().getCanDoActionMessages());
                    if (!retVal) {
                        break;
                    } else {
                        image.setStoragePoolId(getParameters().getStoragePoolId());
                        image.setStorageIds(new ArrayList<Guid>(Arrays.asList(storageDomain.getId())));
                        imageMap.put(image.getImageId(), image);
                    }
                }
                getVmTemplate().setDiskImageMap(imageMap);
            }
        }

        if (retVal && getParameters().isImportAsNewEntity()) {
            initImportClonedTemplate();
        }

        if (retVal) {
            VmTemplate duplicateTemplate = getVmTemplateDAO()
                    .get(getParameters().getVmTemplate().getId());
            // check that the template does not exists in the target domain
            if (duplicateTemplate != null) {
                addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_IMPORT_TEMPLATE_EXISTS);
                getReturnValue().getCanDoActionMessages().add(
                        String.format("$TemplateName %1$s", duplicateTemplate.getName()));
                retVal = false;
            } else if (isVmTemplateWithSameNameExist()) {
                addCanDoActionMessage(VdcBllMessages.VM_CANNOT_IMPORT_TEMPLATE_NAME_EXISTS);
                retVal = false;
            }
        }

        if (retVal) {
            retVal = validateNoDuplicateDiskImages(getParameters().getImages());
        }

        if (retVal && getParameters().getImages() != null && !getParameters().getImages().isEmpty()) {
            Map<StorageDomain, Integer> domainMap = getSpaceRequirementsForStorageDomains(
                    new ArrayList<DiskImage>(getVmTemplate().getDiskImageMap().values()));
            if (domainMap.isEmpty()) {
                int sz = 0;
                if (getVmTemplate().getDiskImageMap() != null) {
                    for (DiskImage image : getVmTemplate().getDiskImageMap().values()) {
                        sz += image.getSize();
                    }
                }
                domainMap.put(getStorageDomain(), sz);
            }
            for (Map.Entry<StorageDomain, Integer> entry : domainMap.entrySet()) {
                if (!doesStorageDomainhaveSpaceForRequest(entry.getKey(), entry.getValue())) {
                    return false;
                }
            }
        }
        if (retVal) {
            retVal = validateMacAddress( getVmTemplate().getInterfaces());
        }
        if (!retVal) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
        }
        return retVal;
    }

    protected boolean isVmTemplateWithSameNameExist() {
        return VmTemplateCommand.isVmTemlateWithSameNameExist(getParameters().getVmTemplate().getName());
    }

    private void initImportClonedTemplate() {
        getParameters().getVmTemplate().setId(Guid.newGuid());
        for (VmNetworkInterface iface : getParameters().getVmTemplate().getInterfaces()) {
            iface.setId(Guid.newGuid());
        }
    }

    private void initImportClonedTemplateDisks() {
        for (DiskImage image : getParameters().getImages()) {
            diskGuidList.add(image.getId());
            imageGuidList.add(image.getImageId());
            if (getParameters().isImportAsNewEntity()) {
                image.setId(Guid.newGuid());
                image.setImageId(Guid.newGuid());
            }
        }
    }

    protected boolean validateNoDuplicateDiskImages(Iterable<DiskImage> images) {
        if (!getParameters().isImportAsNewEntity()) {
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(images);
            return validate(diskImagesValidator.diskImagesAlreadyExist());
        }

        return true;
    }

    @Override
    protected StorageDomainStaticDAO getStorageDomainStaticDAO() {
        return DbFacade.getInstance().getStorageDomainStaticDao();
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
            image.setvolumeFormat(VolumeFormat.COW);
        }
    }

    @Override
    protected void executeCommand() {
        boolean success = true;
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                initImportClonedTemplateDisks();
                addVmTemplateToDb();
                addVmInterfaces();
                getCompensationContext().stateChanged();
                return null;
            }
        });

        boolean doesVmTemplateContainImages = !getParameters().getImages().isEmpty();
        if (doesVmTemplateContainImages) {
            moveOrCopyAllImageGroups(getVmTemplateId(), getParameters().getImages());
        }

        VmDeviceUtils.addImportedDevices(getVmTemplate(), getParameters().isImportAsNewEntity());

        if (!doesVmTemplateContainImages) {
            endMoveOrCopyCommand();
        }
        checkTrustedService();
        setSucceeded(success);
    }

    private void checkTrustedService() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmTemplateName", getVmTemplateName());
        if (getVmTemplate().isTrustedService() && !getVdsGroup().supportsTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVmTemplate().isTrustedService() && getVdsGroup().supportsTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    @Override
    protected void moveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                int i = 0;
                for (DiskImage disk : disks) {
                    Guid destinationDomain = imageToDestinationDomainMap.get(diskGuidList.get(i));
                    MoveOrCopyImageGroupParameters tempVar =
                            new MoveOrCopyImageGroupParameters(containerID,
                                    diskGuidList.get(i),
                                    imageGuidList.get(i),
                                    disk.getId(),
                                    disk.getImageId(),
                                    destinationDomain,
                                    getMoveOrCopyImageOperation());

                    tempVar.setParentCommand(getActionType());
                    tempVar.setUseCopyCollapse(true);
                    tempVar.setVolumeType(disk.getVolumeType());
                    tempVar.setVolumeFormat(disk.getVolumeFormat());
                    tempVar.setCopyVolumeType(CopyVolumeType.SharedVol);
                    tempVar.setSourceDomainId(getParameters().getSourceDomainId());
                    tempVar.setForceOverride(getParameters().getForceOverride());
                    tempVar.setImportEntity(true);
                    tempVar.setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, containerID));
                    for (DiskImage diskImage : getParameters().getVmTemplate().getDiskList()) {
                        if (diskGuidList.get(i).equals(diskImage.getId())) {
                            tempVar.setQuotaId(diskImage.getQuotaId());
                            break;
                        }
                    }

                    MoveOrCopyImageGroupParameters p = tempVar;
                    p.setParentParameters(getParameters());
                    VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                            VdcActionType.CopyImageGroup,
                            p,
                            ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

                    if (!vdcRetValue.getSucceeded()) {
                        throw ((vdcRetValue.getFault() != null) ? new VdcBLLException(vdcRetValue.getFault().getError())
                                : new VdcBLLException(VdcBllErrors.ENGINE));
                    }

                    getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
                    i++;
                }
                return null;
            }
        });
    }

    protected void addVmTemplateToDb() {
        getVmTemplate().setVdsGroupId(getParameters().getVdsGroupId());
        getVmTemplate().setStatus(VmTemplateStatus.Locked);
        getVmTemplate().setQuotaId(getParameters().getQuotaId());
        VmHandler.updateImportedVmUsbPolicy(getVmTemplate());
        DbFacade.getInstance().getVmTemplateDao().save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());
        int count = 1;
        for (DiskImage image : getParameters().getImages()) {
            image.setActive(true);
            image_storage_domain_map map = BaseImagesCommand.saveImage(image);
            getCompensationContext().snapshotNewEntity(image.getImage());
            getCompensationContext().snapshotNewEntity(map);
            if (!DbFacade.getInstance().getBaseDiskDao().exists(image.getId())) {
                image.setDiskAlias(ImagesHandler.getSuggestedDiskAlias(image, getVmTemplateName(), count));
                count++;
                DbFacade.getInstance().getBaseDiskDao().save(image);
                getCompensationContext().snapshotNewEntity(image);
            }

            DiskImageDynamic diskDynamic = new DiskImageDynamic();
            diskDynamic.setId(image.getImageId());
            diskDynamic.setactual_size(image.getActualSizeInBytes());
            DbFacade.getInstance().getDiskImageDynamicDao().save(diskDynamic);
            getCompensationContext().snapshotNewEntity(diskDynamic);
        }
    }

    protected void addVmInterfaces() {
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager();
        List<VmNetworkInterface> interfaces = getVmTemplate().getInterfaces();
        List<String> invalidNetworkNames = new ArrayList<String>();
        List<String> invalidIfaceNames = new ArrayList<String>();
        Map<String, Network> networksInVdsByName =
                Entities.entitiesByName(getNetworkDAO().getAllForCluster(getVmTemplate().getVdsGroupId()));
        String networkName;
        for (VmNetworkInterface iface : interfaces) {
            if (iface.getId() == null) {
                iface.setId(Guid.newGuid());
            }
            networkName = iface.getNetworkName();
            iface.setVmId(getVmTemplateId());
            VmNetworkInterface iDynamic = new VmNetworkInterface();
            VmNetworkStatistics iStat = new VmNetworkStatistics();
            iDynamic.setStatistics(iStat);
            iDynamic.setId(iface.getId());
            iStat.setId(iface.getId());
            iStat.setVmId(getVmTemplateId());
            iDynamic.setVmTemplateId(getVmTemplateId());
            iDynamic.setName(iface.getName());
            if (vmInterfaceManager.isValidVmNetwork(iface, networksInVdsByName)) {
                iDynamic.setNetworkName(networkName);
            } else {
                invalidNetworkNames.add(iface.getNetworkName());
                invalidIfaceNames.add(iface.getName());
                iDynamic.setNetworkName(null);
            }
            iDynamic.setLinked(iface.isLinked());
            iDynamic.setSpeed(iface.getSpeed());
            iDynamic.setType(iface.getType());

            getVmNicDao().save(iDynamic);
            getCompensationContext().snapshotNewEntity(iDynamic);
            DbFacade.getInstance().getVmNetworkStatisticsDao().save(iStat);
            getCompensationContext().snapshotNewEntity(iStat);
        }

        auditInvalidInterfaces(invalidNetworkNames, invalidIfaceNames);
    }

    @Override
    protected void endMoveOrCopyCommand() {
        VmTemplateHandler.UnLockVmTemplate(getVmTemplateId());

        endActionOnAllImageGroups();

        setSucceeded(true);
    }

    protected void removeNetwork() {
        List<VmNic> list = getVmNicDao().getAllForTemplate(getVmTemplateId());
        for (VmNic iface : list) {
            getVmNicDao().remove(iface.getId());
        }
    }

    @Override
    protected void endActionOnAllImageGroups() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            getBackend().EndAction(getImagesActionType(), p);
        }
    }

    @Override
    protected void endWithFailure() {
        removeNetwork();
        endActionOnAllImageGroups();
        DbFacade.getInstance().getVmTemplateDao().remove(getVmTemplateId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_STARTING_IMPORT_TEMPLATE
                    : AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE
                    : AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FAILED;
        }
        return super.getAuditLogTypeValue();
    }

    @Override
    public Guid getVmTemplateId() {
        if (getParameters().isImportAsNewEntity()) {
            return getParameters().getVmTemplate().getId();
        } else {
            return super.getVmTemplateId();
        }
    }

    @Override
    public VmTemplate getVmTemplate() {
        if (getParameters().isImportAsNewEntity()) {
            return getParameters().getVmTemplate();
        } else {
            return super.getVmTemplate();
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        if(getParameters().isImportAsNewEntity()){
            return addValidationGroup(ImportClonedEntity.class);
        }
        return addValidationGroup(ImportEntity.class);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<String, String>();
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(),
                    (getVmTemplateName() == null) ? "" : getVmTemplateName());
            jobProperties.put(VdcObjectType.StoragePool.name().toLowerCase(), getStoragePoolName());
        }
        return jobProperties;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        for (DiskImage disk : getParameters().getVmTemplate().getDiskList()) {
            //TODO: handle import more than once;
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    imageToDestinationDomainMap.get(disk.getId()),
                    (double)disk.getSizeInGigabytes()));
        }
        return list;
    }

    @Override
    protected AuditLogType getAuditLogTypeForInvalidInterfaces() {
        return AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_INVALID_INTERFACES;
    }
}
