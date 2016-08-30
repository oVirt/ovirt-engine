package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.network.vm.VnicProfileHelper;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmTemplateCommand extends MoveOrCopyTemplateCommand<ImportVmTemplateParameters>
        implements QuotaStorageDependent {

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private DiskProfileHelper diskProfileHelper;

    @Inject
    private CpuProfileHelper cpuProfileHelper;

    public ImportVmTemplateCommand(ImportVmTemplateParameters parameters) {
        super(parameters);
        setVmTemplate(parameters.getVmTemplate());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
        setStoragePoolId(parameters.getStoragePoolId());
        setVdsGroupId(parameters.getVdsGroupId());
        setStorageDomainId(parameters.getStorageDomainId());

        Version clusterVersion = getVdsGroup() == null
                ? null
                : getVdsGroup().getCompatibilityVersion();
        ImportUtils.updateGraphicsDevices(getVmTemplate(), clusterVersion);
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

        if(retVal) {
            retVal = validateTemplateArchitecture();
        }

        if (retVal) {
            retVal = isVDSGroupCompatible();
        }

        if (retVal) {
            // set the source domain and check that it is ImportExport type and active
            setSourceDomainId(getParameters().getSourceDomainId());
            StorageDomainValidator sourceDomainValidator = new StorageDomainValidator(getSourceDomain());
            retVal = validate(sourceDomainValidator.isDomainExistAndActive());
        }

        if (retVal && (getSourceDomain().getStorageDomainType() != StorageDomainType.ImportExport)
                && !isImagesAlreadyOnTarget()) {
            addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            retVal = false;
        }

        if (retVal && !isImagesAlreadyOnTarget()) {
            // Set the template images from the Export domain and change each image id storage is to the import domain
            GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(getParameters()
                    .getStoragePoolId(), getParameters().getSourceDomainId());
            VdcQueryReturnValue qretVal = runInternalQuery(
                    VdcQueryType.GetTemplatesFromExportDomain, tempVar);
            retVal = qretVal.getSucceeded();
            if (retVal) {
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
                HashMap<Guid, DiskImage> imageMap = new HashMap<>();
                for (DiskImage image : images) {
                    if (Guid.Empty.equals(image.getVmSnapshotId())) {
                        retVal = failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
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
                    retVal = ImagesHandler.checkImageConfiguration(targetDomain, image,
                            getReturnValue().getCanDoActionMessages());
                    if (!retVal) {
                        break;
                    } else {
                        image.setStoragePoolId(getParameters().getStoragePoolId());
                        image.setStorageIds(new ArrayList<>(Arrays.asList(storageDomain.getId())));
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
            VmTemplate duplicateTemplate = getVmTemplateDao()
                    .get(getParameters().getVmTemplate().getId());
            // check that the template does not exists in the target domain
            if (duplicateTemplate != null) {
                addCanDoActionMessage(EngineMessage.VMT_CANNOT_IMPORT_TEMPLATE_EXISTS);
                getReturnValue().getCanDoActionMessages().add(
                        String.format("$TemplateName %1$s", duplicateTemplate.getName()));
                retVal = false;
            } else if (getVmTemplate().isBaseTemplate() && isVmTemplateWithSameNameExist()) {
                addCanDoActionMessage(EngineMessage.VM_CANNOT_IMPORT_TEMPLATE_NAME_EXISTS);
                retVal = false;
            }
        }

        if (retVal) {
            retVal = validateNoDuplicateDiskImages(getImages());
        }

        if (retVal && getImages() != null && !getImages().isEmpty() && !isImagesAlreadyOnTarget()) {
            if (!validateSpaceRequirements(getImages())) {
                return false;
            }
        }

        if (retVal) {
            retVal = validateMacAddress(Entities.<VmNic, VmNetworkInterface> upcast(getVmTemplate().getInterfaces()));
        }

        // if this is a template version, check base template exist
        if (retVal && !getVmTemplate().isBaseTemplate()) {
            VmTemplate baseTemplate = getVmTemplateDao().get(getVmTemplate().getBaseTemplateId());
            if (baseTemplate == null) {
                retVal = false;
                addCanDoActionMessage(EngineMessage.VMT_CANNOT_IMPORT_TEMPLATE_VERSION_MISSING_BASE);
            }
        }

        if (retVal && !setAndValidateDiskProfiles()) {
            return false;
        }

        if(retVal && !setAndValidateCpuProfile()) {
            return false;
        }

        if (!retVal) {
            addCanDoActionMessage(EngineMessage.VAR__ACTION__IMPORT);
            addCanDoActionMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
        }
        return retVal;
    }

    protected boolean isVDSGroupCompatible () {
        if (getVdsGroup().getArchitecture() != getVmTemplate().getClusterArch()) {
            addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_TEMPLATE_ARCHITECTURE_NOT_SUPPORTED_BY_CLUSTER);
            return false;
        }
        return true;
    }

    protected boolean validateTemplateArchitecture () {
        if (getVmTemplate().getClusterArch() == ArchitectureType.undefined) {
            addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_TEMPLATE_WITH_NOT_SUPPORTED_ARCHITECTURE);
            return false;
        }
        return true;
    }

    protected boolean isVmTemplateWithSameNameExist() {
        return vmTemplateDao.getByName(getParameters().getVmTemplate().getName(),
                getParameters().getStoragePoolId(),
                null,
                false) != null;
    }

    private void initImportClonedTemplate() {
        Guid newTemplateId = Guid.newGuid();
        getParameters().getVmTemplate().setId(newTemplateId);
        for (VmNetworkInterface iface : getParameters().getVmTemplate().getInterfaces()) {
            iface.setId(Guid.newGuid());
        }
        // cloned template is always base template, as its a new entity
        getParameters().getVmTemplate().setBaseTemplateId(newTemplateId);
    }

    private void initImportClonedTemplateDisks() {
        for (DiskImage image : getImages()) {
            if (getParameters().isImportAsNewEntity()) {
                generateNewDiskId(image);
                updateManagedDeviceMap(image, getVmTemplate().getManagedDeviceMap());
            } else {
                newDiskIdForDisk.put(image.getId(), image);
            }
        }
    }

    protected boolean validateNoDuplicateDiskImages(Iterable<DiskImage> images) {
        if (!getParameters().isImportAsNewEntity() && !isImagesAlreadyOnTarget()) {
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(images);
            return validate(diskImagesValidator.diskImagesAlreadyExist());
        }

        return true;
    }

    @Override
    protected List<DiskImage> getImages() {
        return getParameters().getImages();
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
                updateOriginalTemplateNameOnDerivedVms();
                addVmInterfaces();
                getCompensationContext().stateChanged();
                VmHandler.addVmInitToDB(getVmTemplate());
                return null;
            }
        });

        boolean doesVmTemplateContainImages = !getImages().isEmpty();
        if (doesVmTemplateContainImages && !isImagesAlreadyOnTarget()) {
            moveOrCopyAllImageGroups(getVmTemplateId(), getImages());
        }

        VmDeviceUtils.addImportedDevices(getVmTemplate(), getParameters().isImportAsNewEntity());

        if (!doesVmTemplateContainImages || isImagesAlreadyOnTarget()) {
            endMoveOrCopyCommand();
        }
        checkTrustedService();
        setSucceeded(success);
    }

    private void updateOriginalTemplateNameOnDerivedVms() {
        if (!getParameters().isImportAsNewEntity()) {
            // in case it has been renamed
            getVmDao().updateOriginalTemplateName(getVmTemplate().getId(), getVmTemplate().getName());
        }
    }

    private void checkTrustedService() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmTemplateName", getVmTemplateName());
        if (getVmTemplate().isTrustedService() && !getVdsGroup().supportsTrustedService()) {
            auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVmTemplate().isTrustedService() && getVdsGroup().supportsTrustedService()) {
            auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    @Override
    protected void moveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (DiskImage disk : disks) {
                    Guid originalDiskId = newDiskIdForDisk.get(disk.getId()).getId();
                    Guid destinationDomain = imageToDestinationDomainMap.get(originalDiskId);
                    MoveOrCopyImageGroupParameters tempVar =
                            new MoveOrCopyImageGroupParameters(containerID,
                                    originalDiskId,
                                    newDiskIdForDisk.get(disk.getId()).getImageId(),
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
                    tempVar.setRevertDbOperationScope(ImageDbOperationScope.IMAGE);
                    for (DiskImage diskImage : getParameters().getVmTemplate().getDiskList()) {
                        if (originalDiskId.equals(diskImage.getId())) {
                            tempVar.setQuotaId(diskImage.getQuotaId());
                            tempVar.setDiskProfileId(diskImage.getDiskProfileId());
                            break;
                        }
                    }

                    MoveOrCopyImageGroupParameters p = tempVar;
                    p.setParentParameters(getParameters());
                    VdcReturnValueBase vdcRetValue = runInternalActionWithTasksContext(
                            VdcActionType.CopyImageGroup,
                            p);

                    if (!vdcRetValue.getSucceeded()) {
                        throw ((vdcRetValue.getFault() != null) ? new EngineException(vdcRetValue.getFault().getError())
                                : new EngineException(EngineError.ENGINE));
                    }

                    getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
                }
                return null;
            }
        });
    }

    protected void addVmTemplateToDb() {
        getVmTemplate().setVdsGroupId(getParameters().getVdsGroupId());

        // if "run on host" field points to a non existent vds (in the current cluster) -> remove field and continue
        if(!VmHandler.validateDedicatedVdsExistOnSameCluster(getVmTemplate(), null)){
            getVmTemplate().setDedicatedVmForVdsList(Collections.<Guid>emptyList());
        }

        getVmTemplate().setStatus(VmTemplateStatus.Locked);
        getVmTemplate().setQuotaId(getParameters().getQuotaId());
        VmHandler.updateImportedVmUsbPolicy(getVmTemplate());
        DbFacade.getInstance().getVmTemplateDao().save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());
        int count = 1;
        for (DiskImage image : getImages()) {
            image.setActive(true);
            ImageStorageDomainMap map = BaseImagesCommand.saveImage(image);
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
        VnicProfileHelper vnicProfileHelper =
                new VnicProfileHelper(getVmTemplate().getVdsGroupId(),
                        getStoragePoolId(),
                        getVdsGroup().getCompatibilityVersion(),
                        AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_INVALID_INTERFACES);

        for (VmNetworkInterface iface : getVmTemplate().getInterfaces()) {
            if (iface.getId() == null) {
                iface.setId(Guid.newGuid());
            }

            iface.setVmId(getVmTemplateId());
            VmNic nic = new VmNic();
            nic.setId(iface.getId());
            nic.setVmTemplateId(getVmTemplateId());
            nic.setName(iface.getName());
            nic.setLinked(iface.isLinked());
            nic.setSpeed(iface.getSpeed());
            nic.setType(iface.getType());

            vnicProfileHelper.updateNicWithVnicProfileForUser(iface, getCurrentUser());
            nic.setVnicProfileId(iface.getVnicProfileId());
            getVmNicDao().save(nic);
            getCompensationContext().snapshotNewEntity(nic);

            VmNetworkStatistics iStat = new VmNetworkStatistics();
            nic.setStatistics(iStat);
            iStat.setId(iface.getId());
            iStat.setVmId(getVmTemplateId());
            getDbFacade().getVmNetworkStatisticsDao().save(iStat);
            getCompensationContext().snapshotNewEntity(iStat);
        }

        vnicProfileHelper.auditInvalidInterfaces(getVmTemplateName());
    }

    @Override
    protected void endMoveOrCopyCommand() {
        VmTemplateHandler.unlockVmTemplate(getVmTemplateId());

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
            getBackend().endAction(getImagesActionType(),
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
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
            jobProperties = new HashMap<>();
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(),
                    (getVmTemplateName() == null) ? "" : getVmTemplateName());
            jobProperties.put(VdcObjectType.StoragePool.name().toLowerCase(), getStoragePoolName());
        }
        return jobProperties;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (getParameters().getVmTemplate().getDiskList() != null) {
            Map<DiskImage, Guid> map = new HashMap<>();
            for (DiskImage diskImage : getParameters().getVmTemplate().getDiskList()) {
                map.put(diskImage, imageToDestinationDomainMap.get(diskImage.getId()));
            }
            return validate(diskProfileHelper.setAndValidateDiskProfiles(map,
                    getStoragePool().getCompatibilityVersion(), getCurrentUser()));
        }
        return true;
    }

    protected boolean setAndValidateCpuProfile() {
        getVmTemplate().setVdsGroupId(getVdsGroupId());
        getVmTemplate().setCpuProfileId(getParameters().getCpuProfileId());
        return validate(cpuProfileHelper.setAndValidateCpuProfile(getVmTemplate(),
                getVdsGroup().getCompatibilityVersion(), getUserId()));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

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
}
