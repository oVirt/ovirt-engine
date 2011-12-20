package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImprotVmTemplateParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
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
public class ImportVmTemplateCommand<T extends ImprotVmTemplateParameters> extends MoveOrCopyTemplateCommand<T> {
    final int BYTE_TO_GB_FACTOR = 1073741824;

    public ImportVmTemplateCommand(T parameters) {
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

        // Set the template images from the Export domain and change each image
        // id storage is to the import domain
        if (retVal) {
            GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(getParameters()
                    .getStoragePoolId(), getParameters().getSourceDomainId());
            tempVar.setGetAll(true);
            VdcQueryReturnValue qretVal = getBackend().runInternalQuery(
                    VdcQueryType.GetTemplatesFromExportDomain, tempVar);

            if (qretVal.getSucceeded()) {
                Map<VmTemplate, DiskImageList> templates = (Map) qretVal.getReturnValue();
                // java.util.ArrayList<DiskImage> images = null; //LINQ
                // templates.FirstOrDefault(t => t.Key.vmt_guid ==
                // ImprotVmTemplateParameters.VmTemplate.vmt_guid).Value;
                DiskImageList images = templates.get(LinqUtils.firstOrNull(templates.keySet(),
                        new Predicate<VmTemplate>() {
                            @Override
                            public boolean eval(VmTemplate t) {
                                return t.getId().equals(getParameters().getVmTemplate().getId());
                            }
                        }));
                List<DiskImage> list = Arrays.asList(images.getDiskImages());
                getParameters().setImages(list);
                storage_domain_static storageDomain =
                        getStorageDomainStaticDAO().get(getParameters().getDestDomainId());
                Map<String, DiskImage> imageMap = new HashMap<String, DiskImage>();
                for (DiskImage image : list) {
                    changeRawToCowIfSparseOnBlockDevice(storageDomain.getstorage_type(), image);
                    retVal = ImagesHandler.CheckImageConfiguration(storageDomain, image,
                             getReturnValue().getCanDoActionMessages());
                    if (!retVal) {
                        break;
                    } else {
                        image.setstorage_pool_id(getParameters().getStoragePoolId());
                        image.setstorage_id(getParameters().getSourceDomainId());
                        imageMap.put(image.getId().toString(), image);
                    }
                }
                getVmTemplate().setDiskImageMap(imageMap);
            }
        }

        if (getVmTemplate() == null) {
            retVal = false;
        } else {
            setDescription(getVmTemplateName());
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

        // check that the source domain is valid
        if (retVal) {
            retVal = ImportExportCommon.CheckStorageDomain(getParameters().getSourceDomainId(), getReturnValue()
                    .getCanDoActionMessages());
        }
        // check that the destination domain is valid
        if (retVal) {
            retVal = ImportExportCommon.CheckStorageDomain(getParameters().getDestDomainId(), getReturnValue()
                    .getCanDoActionMessages());
        }
        // check that the storage pool is valid
        if (retVal) {
            retVal = ImportExportCommon.CheckStoragePool(getParameters().getStoragePoolId(), getReturnValue()
                    .getCanDoActionMessages());
        }

        // check that template has images
        if (retVal) {
            if (getParameters().getImages() == null || getParameters().getImages().size() <= 0) {
                retVal = false;
                addCanDoActionMessage(VdcBllMessages.TEMPLATE_IMAGE_NOT_EXIST);
            }
        }

        // check if domains are active
        if (retVal) {
            retVal =
                    (IsDomainActive(getParameters().getSourceDomainId(), getParameters().getStoragePoolId()) && IsDomainActive(
                            getParameters().getDestDomainId(), getParameters().getStoragePoolId()));
        }
        // check that the destination domain is not ISO and not Export domain
        if (retVal) {
            if (getStorageDomain().getstorage_domain_type() == StorageDomainType.ISO
                    || getStorageDomain().getstorage_domain_type() == StorageDomainType.ImportExport) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                retVal = false;
            }
        }
        // set the source domain and check that it is ImportExport type
        if (retVal) {
            SetSourceDomainId(getParameters().getSourceDomainId());
            if (getSourceDomain() == null) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
                retVal = false;
            }
            if (getSourceDomain().getstorage_domain_type() != StorageDomainType.ImportExport) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                retVal = false;
            }
        }
        if (retVal) {
            int sz = 0;
            if (getVmTemplate().getDiskImageMap() != null) {
                for (DiskImage image : getVmTemplate().getDiskImageMap().values()) {
                    sz += image.getsize();
                }
            }
            int sizeInGB = sz / BYTE_TO_GB_FACTOR;
            retVal = StorageDomainSpaceChecker.hasSpaceForRequest(getStorageDomain(), sizeInGB);
            if (!retVal) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
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

        setSucceeded(true);
    }

    @Override
    protected void MoveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (DiskImage disk : disks) {
                    MoveOrCopyImageGroupParameters tempVar = new MoveOrCopyImageGroupParameters(containerID, disk
                            .getimage_group_id().getValue(), disk.getId(), getParameters().getStorageDomainId(),
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
                            VdcActionType.MoveOrCopyImageGroup, p);

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
        DbFacade.getInstance().getVmTemplateDAO().save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());

        for (DiskImage image : getParameters().getImages()) {
            DiskImageTemplate dt = new DiskImageTemplate(image.getId(), getParameters().getVmTemplate()
                    .getId(), image.getinternal_drive_mapping(), image.getId(), "", "", getNow(),
                    image.getsize(), image.getdescription(), null);

            DbFacade.getInstance().getDiskImageDAO().save(image);
            DbFacade.getInstance().getDiskImageTemplateDAO().save(dt);
            getCompensationContext().snapshotNewEntity(image);
            getCompensationContext().snapshotNewEntity(dt);

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
            VmNetworkInterface iDynamic = new VmNetworkInterface();
            VmNetworkStatistics iStat = new VmNetworkStatistics();
            iDynamic.setStatistics(iStat);
            iDynamic.setId(Guid.NewGuid());
            iStat.setId(iDynamic.getId());
            iDynamic.setVmTemplateId(getVmTemplateId());
            // TODO why does a VM interface get VDS details?
            // iDynamic.setAddress(iface.getInterfaceDynamic().getAddress());
            // iDynamic.setBondName(iface.getInterfaceDynamic().getBondName());
            // iDynamic.setBondType(iface.getInterfaceDynamic().getBondType());
            // iDynamic.setGateway(iface.getInterfaceDynamic().getGateway());
            iDynamic.setName(iface.getName());
            iDynamic.setNetworkName(iface.getNetworkName());
            iDynamic.setSpeed(iface.getSpeed());
            // iDynamic.setSubnet(iface.getInterfaceDynamic().getSubnet());
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
            DbFacade.getInstance().getDiskImageTemplateDAO().remove(image.getId());
            DbFacade.getInstance().getDiskImageDAO().remove(image.getId());
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
