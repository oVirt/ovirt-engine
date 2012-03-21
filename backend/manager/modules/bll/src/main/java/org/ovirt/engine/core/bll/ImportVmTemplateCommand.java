package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImprotVmTemplateParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmTemplateCommand extends MoveOrCopyTemplateCommand<ImprotVmTemplateParameters> {

    public ImportVmTemplateCommand(ImprotVmTemplateParameters parameters) {
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
            GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(getParameters()
                    .getStoragePoolId(), getParameters().getSourceDomainId());
            tempVar.setGetAll(true);
            VdcQueryReturnValue qretVal = getBackend().runInternalQuery(
                    VdcQueryType.GetTemplatesFromExportDomain, tempVar);
            retVal = qretVal.getSucceeded();
            if (retVal) {
                Map<VmTemplate, DiskImageList> templates = (Map) qretVal.getReturnValue();
                DiskImageList images = templates.get(LinqUtils.firstOrNull(templates.keySet(),
                        new Predicate<VmTemplate>() {
                            @Override
                            public boolean eval(VmTemplate t) {
                                return t.getId().equals(getParameters().getVmTemplate().getId());
                            }
                        }));
                ArrayList<DiskImage> list = new ArrayList<DiskImage>(Arrays.asList(images.getDiskImages()));
                getParameters().setImages(list);
                getVmTemplate().setImages(list);
                ensureDomainMap(getParameters().getImages(), getParameters().getDestDomainId());
                Map<String, DiskImage> imageMap = new HashMap<String, DiskImage>();
                for (DiskImage image : list) {
                    storage_domains storageDomain = getStorageDomain(imageToDestinationDomainMap.get(image.getId()));
                    StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
                    retVal = validator.isDomainExistAndActive(getReturnValue().getCanDoActionMessages()) &&
                            validator.domainIsValidDestination(getReturnValue().getCanDoActionMessages());
                    if(!retVal) {
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
                        imageMap.put(image.getId().toString(), image);
                    }
                }
                getVmTemplate().setDiskImageMap(imageMap);
            }
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
            } else if (VmTemplateCommand.isVmTemlateWithSameNameExist(getParameters().getVmTemplate().getname())) {
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
        if (!retVal) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
        }
        return retVal;
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

    protected StorageDomainStaticDAO getStorageDomainStaticDAO() {
        return DbFacade.getInstance().getStorageDomainStaticDAO();
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                AddVmTemplateToDb();
                AddVmInterfaces();
                getCompensationContext().stateChanged();
                return null;
            }
        });

        MoveOrCopyAllImageGroups(getVmTemplateId(), getParameters().getImages());
        VmDeviceUtils.addImportedDevices(getVmTemplate(), getVmTemplate().getId(),new ArrayList<VmDevice>(), new ArrayList<VmDevice>());
        setSucceeded(true);
    }

    @Override
    protected void MoveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (DiskImage disk : disks) {
                    Guid targetId = (imageToDestinationDomainMap.get(disk.getId()) == null) ? getParameters().getStorageDomainId() :
                            imageToDestinationDomainMap.get(disk.getId());
                    MoveOrCopyImageGroupParameters tempVar = new MoveOrCopyImageGroupParameters(containerID, disk
                            .getimage_group_id().getValue(), disk.getId(), targetId,
                            getMoveOrCopyImageOperation());
                    tempVar.setParentCommand(getActionType());
                    tempVar.setEntityId(getParameters().getEntityId());
                    tempVar.setUseCopyCollapse(true);
                    tempVar.setVolumeType(disk.getvolume_type());
                    tempVar.setVolumeFormat(disk.getvolume_format());
                    tempVar.setCopyVolumeType(CopyVolumeType.SharedVol);
                    tempVar.setPostZero(disk.getwipe_after_delete());
                    tempVar.setForceOverride(true);
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
        for (DiskImage image : getParameters().getImages()) {
            image.setactive(true);
            BaseImagesCommand.saveDiskImage(image);
            getCompensationContext().snapshotNewEntity(image);
            if (!DbFacade.getInstance().getDiskDao().exists(image.getimage_group_id())) {
                Disk disk = image.getDisk();
                DbFacade.getInstance().getDiskDao().save(disk);
                getCompensationContext().snapshotNewEntity(disk);
            }

            DiskImageDynamic diskDynamic = new DiskImageDynamic();
            diskDynamic.setId(image.getId());
            diskDynamic.setactual_size(image.getactual_size());
            DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
            getCompensationContext().snapshotNewEntity(diskDynamic);
        }
    }

    protected void AddVmInterfaces() {
        List<VmNetworkInterface> interfaces = getVmTemplate().getInterfaces();
        for (VmNetworkInterface iface : interfaces) {
            if (iface.getId() == null) {
                iface.setId(Guid.NewGuid());
            }
            iface.setVmId(getVmTemplateId());
            VmNetworkInterface iDynamic = new VmNetworkInterface();
            VmNetworkStatistics iStat = new VmNetworkStatistics();
            iDynamic.setStatistics(iStat);
            iDynamic.setId(iface.getId());
            iStat.setId(iface.getId());
            iDynamic.setVmTemplateId(getVmTemplateId());
            iDynamic.setName(iface.getName());
            iDynamic.setNetworkName(iface.getNetworkName());
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
        List<VmNetworkInterface> list = DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForTemplate(getVmTemplateId());
        for (VmNetworkInterface iface : list) {
            DbFacade.getInstance().getVmNetworkInterfaceDAO().remove(iface.getId());
        }
    }

    protected void RemoveImages() {
        for (DiskImage image : getParameters().getImages()) {
            DbFacade.getInstance().getDiskImageDynamicDAO().remove(image.getId());
            DbFacade.getInstance().getDiskImageDAO().remove(image.getId());
            DbFacade.getInstance().getVmDeviceDAO().remove(new VmDeviceId(image.getDisk().getId(),image.getvm_guid()));
            DbFacade.getInstance().getDiskDao().remove(image.getimage_group_id());
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
}
