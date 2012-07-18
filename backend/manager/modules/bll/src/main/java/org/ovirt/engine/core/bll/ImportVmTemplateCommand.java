package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmTemplateCommand extends MoveOrCopyTemplateCommand<ImportVmTemplateParameters> {

    private List<Guid> diskGuidList = new ArrayList<Guid>();
    private List<Guid> imageGuidList = new ArrayList<Guid>();

    public ImportVmTemplateCommand(ImportVmTemplateParameters parameters) {
        super(parameters);
        setVmTemplate(parameters.getVmTemplate());
        parameters.setEntityId(getVmTemplate().getId());
        setStoragePoolId(parameters.getStoragePoolId());
        setVdsGroupId(parameters.getVdsGroupId());
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
            SetSourceDomainId(getParameters().getSourceDomainId());
            StorageDomainValidator sourceDomainValidator = new StorageDomainValidator(getSourceDomain());
            retVal = sourceDomainValidator.isDomainExistAndActive(getReturnValue().getCanDoActionMessages());
        }
        if (retVal && getSourceDomain().getstorage_domain_type() != StorageDomainType.ImportExport) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            retVal = false;
        }

        if (retVal) {
            // Set the template images from the Export domain and change each image id storage is to the import domain
            GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(getParameters()
                    .getStoragePoolId(), getParameters().getSourceDomainId());
            tempVar.setGetAll(true);
            VdcQueryReturnValue qretVal = getBackend().runInternalQuery(
                    VdcQueryType.GetTemplatesFromExportDomain, tempVar);
            retVal = qretVal.getSucceeded();
            if (retVal) {
                Map<VmTemplate, DiskImageList> templates = (Map) qretVal.getReturnValue();
                DiskImageList images = new DiskImageList();
                for (VmTemplate t : templates.keySet()) {
                    if (t.getId().equals(getVmTemplate().getId())) {
                        images = templates.get(t);
                        getVmTemplate().setInterfaces(t.getInterfaces());
                        break;
                    }
                }
                ArrayList<DiskImage> list = new ArrayList<DiskImage>(Arrays.asList(images.getDiskImages()));
                getParameters().setImages(list);
                getVmTemplate().setImages(list);
                ensureDomainMap(getParameters().getImages(), getParameters().getDestDomainId());
                Map<Guid, DiskImage> imageMap = new HashMap<Guid, DiskImage>();
                for (DiskImage image : list) {
                    storage_domains storageDomain =
                            getStorageDomain(imageToDestinationDomainMap.get(image.getId()));
                    StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
                    retVal = validator.isDomainExistAndActive(getReturnValue().getCanDoActionMessages()) &&
                            validator.domainIsValidDestination(getReturnValue().getCanDoActionMessages());
                    if (!retVal) {
                        break;
                    }
                    storage_domain_static targetDomain = storageDomain.getStorageStaticData();
                    changeRawToCowIfSparseOnBlockDevice(targetDomain.getstorage_type(), image);
                    retVal = ImagesHandler.CheckImageConfiguration(targetDomain, image,
                            getReturnValue().getCanDoActionMessages());
                    if (!retVal) {
                        break;
                    } else {
                        image.setstorage_pool_id(getParameters().getStoragePoolId());
                        image.setstorage_ids(new ArrayList<Guid>(Arrays.asList(getParameters().getSourceDomainId())));
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
                        String.format("$TemplateName %1$s", duplicateTemplate.getname()));
                retVal = false;
            } else if (isVmTemplateWithSameNameExist()) {
                addCanDoActionMessage(VdcBllMessages.VM_CANNOT_IMPORT_TEMPLATE_NAME_EXISTS);
                retVal = false;
            }
        }

        // check that template has images
        if (retVal && (getParameters().getImages() == null || getParameters().getImages().size() == 0)) {
            retVal = false;
            addCanDoActionMessage(VdcBllMessages.TEMPLATE_IMAGE_NOT_EXIST);
        }

        if (retVal) {
            Map<storage_domains, Integer> domainMap = getSpaceRequirementsForStorageDomains(
                    new ArrayList<DiskImage>(getVmTemplate().getDiskImageMap().values()));
            if (domainMap.isEmpty()) {
                int sz = 0;
                if (getVmTemplate().getDiskImageMap() != null) {
                    for (DiskImage image : getVmTemplate().getDiskImageMap().values()) {
                        sz += image.getsize();
                    }
                }
                domainMap.put(getStorageDomain(), sz);
            }
            for (Map.Entry<storage_domains, Integer> entry : domainMap.entrySet()) {
                retVal = StorageDomainSpaceChecker.hasSpaceForRequest(entry.getKey(), entry.getValue());
                if (!retVal) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
                    break;
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
        return VmTemplateCommand.isVmTemlateWithSameNameExist(getParameters().getVmTemplate().getname());
    }

    private void initImportClonedTemplate() {
        getParameters().getVmTemplate().setId(Guid.NewGuid());
        for (VmNetworkInterface iface : getParameters().getVmTemplate().getInterfaces()) {
            iface.setId(Guid.NewGuid());
        }
    }

    private void initImportClonedTemplateDisks() {
        for (DiskImage image : getParameters().getImages()) {
            diskGuidList.add(image.getId());
            imageGuidList.add(image.getImageId());
            if (getParameters().isImportAsNewEntity()) {
                image.setId(Guid.NewGuid());
                image.setImageId(Guid.NewGuid());
            }
        }
    }

    protected StorageDomainStaticDAO getStorageDomainStaticDAO() {
        return DbFacade.getInstance().getStorageDomainStaticDAO();
    }

    /**
     * Change the image format to {@link VolumeFormat#COW} in case the SD is a block device and the image format is
     * {@link VolumeFormat#RAW} and the type is {@link VolumeType#Sparse}.
     *
     * @param storageType
     *            The domain type.
     * @param image
     *            The image to check and cheange if needed.
     */
    private void changeRawToCowIfSparseOnBlockDevice(StorageType storageType, DiskImage image) {
        if ((storageType == StorageType.FCP
                || storageType == StorageType.ISCSI)
                && image.getvolume_format() == VolumeFormat.RAW
                && image.getvolume_type() == VolumeType.Sparse) {
            image.setvolume_format(VolumeFormat.COW);
        }
    }

    @Override
    protected boolean validateQuota() {
        // Set default quota id if storage pool enforcement is disabled.
        getParameters().setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(getVmTemplate().getQuotaId(),
                getStoragePool()));
        for (DiskImage di : getParameters().getImages()) {
            di.setQuotaId(QuotaHelper.getInstance()
                    .getQuotaIdToConsume(getVmTemplate().getQuotaId(),
                            getStoragePool()));
        }
        // TODO: Validate quota for import VM.
        return true;
    }

    @Override
    protected void executeCommand() {
        boolean success = true;
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                initImportClonedTemplateDisks();
                AddVmTemplateToDb();
                AddVmInterfaces();
                getCompensationContext().stateChanged();
                return null;
            }
        });
        MoveOrCopyAllImageGroups(getVmTemplateId(), getParameters().getImages());
        VmDeviceUtils.addImportedDevices(getVmTemplate(), getParameters().isImportAsNewEntity());
        setSucceeded(success);
    }

    @Override
    protected void MoveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
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
                    tempVar.setVolumeType(disk.getvolume_type());
                    tempVar.setVolumeFormat(disk.getvolume_format());
                    tempVar.setCopyVolumeType(CopyVolumeType.SharedVol);
                    tempVar.setPostZero(disk.isWipeAfterDelete());
                    tempVar.setSourceDomainId(getParameters().getSourceDomainId());
                    tempVar.setForceOverride(true);
                    tempVar.setImportEntity(true);
                    tempVar.setEntityId(disk.getImageId());
                    MoveOrCopyImageGroupParameters p = tempVar;
                    p.setParentParemeters(getParameters());
                    VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                            VdcActionType.MoveOrCopyImageGroup,
                            p,
                            ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

                    if (!vdcRetValue.getSucceeded()) {
                        throw ((vdcRetValue.getFault() != null) ? new VdcBLLException(vdcRetValue.getFault().getError())
                                : new VdcBLLException(VdcBllErrors.ENGINE));
                    }

                    getParameters().getImagesParameters().add(p);
                    getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
                    i++;
                }
                return null;
            }
        });
    }

    protected void AddVmTemplateToDb() {
        getVmTemplate().setvds_group_id(getParameters().getVdsGroupId());
        getVmTemplate().setstatus(VmTemplateStatus.Locked);
        getVmTemplate().setQuotaId(getParameters().getQuotaId());
        DbFacade.getInstance().getVmTemplateDAO().save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());
        int count = 1;
        for (DiskImage image : getParameters().getImages()) {
            image.setvm_guid(getVmTemplateId());
            image.setactive(true);
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
            diskDynamic.setactual_size(image.getactual_size());
            DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
            getCompensationContext().snapshotNewEntity(diskDynamic);
        }
    }

    protected void AddVmInterfaces() {
        List<VmNetworkInterface> interfaces = getVmTemplate().getInterfaces();
        String networkName;
        for (VmNetworkInterface iface : interfaces) {
            if (iface.getId() == null) {
                iface.setId(Guid.NewGuid());
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
            if (VmInterfaceManager.isValidVmNetwork(iface, getVmTemplate().getvds_group_id())) {
                iDynamic.setNetworkName(networkName);
            }
            else {
                log.warnFormat("Imported template interface {0} has an invalid network name {1}, network name was set to empty string" , iface.getName(), networkName);
                iDynamic.setNetworkName(StringUtils.EMPTY);
            }
            iDynamic.setSpeed(iface.getSpeed());
            iDynamic.setType(iface.getType());

            DbFacade.getInstance().getVmNetworkInterfaceDAO().save(iDynamic);
            getCompensationContext().snapshotNewEntity(iDynamic);
            DbFacade.getInstance().getVmNetworkStatisticsDAO().save(iStat);
            getCompensationContext().snapshotNewEntity(iStat);
        }
    }

    @Override
    protected void EndMoveOrCopyCommand() {
        VmTemplateHandler.UnLockVmTemplate(getVmTemplateId());

        EndActionOnAllImageGroups();

        UpdateTemplateInSpm();

        setSucceeded(true);
    }

    protected void RemoveNetwork() {
        List<VmNetworkInterface> list =
                DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForTemplate(getVmTemplateId());
        for (VmNetworkInterface iface : list) {
            DbFacade.getInstance().getVmNetworkInterfaceDAO().remove(iface.getId());
        }
    }

    protected void RemoveImages() {
        for (DiskImage image : getParameters().getImages()) {
            DbFacade.getInstance().getDiskImageDynamicDAO().remove(image.getImageId());
            DbFacade.getInstance().getImageStorageDomainMapDao().remove(image.getImageId());
            DbFacade.getInstance().getImageDao().remove(image.getImageId());
            DbFacade.getInstance().getVmDeviceDAO().remove(new VmDeviceId(image.getId(),image.getvm_guid()));
            DbFacade.getInstance().getBaseDiskDao().remove(image.getId());
        }
    }

    @Override
    protected void EndWithFailure() {
        RemoveNetwork();
        RemoveImages();

        DbFacade.getInstance().getVmTemplateDAO().remove(getVmTemplateId());
        setSucceeded(true);
    }

    @Override
    protected void UpdateTemplateInSpm() {
        VmTemplateCommand.UpdateTemplateInSpm(getParameters().getStoragePoolId(), new java.util.ArrayList<VmTemplate>(
                java.util.Arrays.asList(new VmTemplate[] { getParameters().getVmTemplate() })), Guid.Empty,
                getParameters().getImages());
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
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(),
                    (getVmTemplateName() == null) ? "" : getVmTemplateName());
        }
        return jobProperties;
    }
}
