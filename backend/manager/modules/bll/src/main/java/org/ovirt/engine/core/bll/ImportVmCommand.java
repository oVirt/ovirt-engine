package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.ovf.OvfLogEventHandler;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmCommand<T extends ImportVmParameters> extends MoveOrCopyTemplateCommand<T> {
    private static VmStatic vmStaticForDefaultValues;

    static {
        vmStaticForDefaultValues = new VmStatic();
    }

    public ImportVmCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getContainerId());
        parameters.setEntityId(getVmId());
        setVm(parameters.getVm());
        parameters.setEntityId(getVm().getvm_guid());
        // we save the images for the EndAction
        getParameters().setImages(new ArrayList<DiskImage>(getVm().getDiskMap().values()));
        setStoragePoolId(parameters.getStoragePoolId());
        setVdsGroupId(parameters.getVdsGroupId());
    }

    protected ImportVmCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() != null) {
            setDescription(getVmName());
        }
        boolean retVal = false;

        // Load images from Import/Export domain
        GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(getParameters()
                .getStoragePoolId(), getParameters().getSourceDomainId());
        tempVar.setGetAll(true);
        VdcQueryReturnValue qretVal = getBackend().runInternalQuery(VdcQueryType.GetVmsFromExportDomain,
                tempVar);

        retVal = qretVal.getSucceeded();
        if (retVal) {
            List<VM> vms = (List) qretVal.getReturnValue();
            // VM vm = null; //LINQ vms.FirstOrDefault(v => v.getvm_guid() ==
            // ImportVmParameters.Vm.vm_guid);
            VM vm = LinqUtils.firstOrNull(vms, new Predicate<VM>() {
                @Override
                public boolean eval(VM vm) {
                    return vm.getvm_guid().equals(getParameters().getVm().getvm_guid());
                }
            });
            if (vm != null) {
                storage_domain_static storageDomain =
                        getStorageDomainStaticDAO().get(getParameters().getDestDomainId());
                // At this point we should work with the VM that was read from
                // the OVF
                setVm(vm);
                for (DiskImage image : getVm().getImages()) {
                    // copy the new disk volume format/type if provided,
                    // only if requested by the user
                    if (getParameters().getCopyCollapse()) {
                        for (DiskImage p : getParameters().getImages()) {
                            if (p.getId().equals(image.getId())) {
                                if (p.getvolume_format() != null) {
                                    image.setvolume_format(p.getvolume_format());
                                }
                                if (p.getvolume_type() != null) {
                                    image.setvolume_type(p.getvolume_type());
                                }
                            }
                        }
                    }
                    retVal = ImagesHandler.CheckImageConfiguration(storageDomain, image,
                            getReturnValue().getCanDoActionMessages());
                    if (!retVal) {
                        break;
                    } else {
                        image.setstorage_pool_id(getParameters().getStoragePoolId());
                        // we put the source domain id in order that copy will
                        // work
                        // ok
                        // we fix it to DestDomainId in
                        // MoveOrCopyAllImageGroups();
                        image.setstorage_id(getParameters().getSourceDomainId());
                    }
                }
                if (retVal) {
                    java.util.HashMap<String, java.util.ArrayList<DiskImage>> images =
                            GetImagesLeaf(getVm().getImages());
                    for (String drive : images.keySet()) {
                        java.util.ArrayList<DiskImage> list = images.get(drive);
                        getVm().addDriveToImageMap(drive, list.get(list.size() - 1));
                    }
                }
            } else {
                retVal = false;
            }
        }

        if (retVal) {
            retVal = ImportExportCommon.CheckStorageDomain(getParameters().getSourceDomainId(), getReturnValue()
                    .getCanDoActionMessages());
        }
        if (retVal) {
            retVal = ImportExportCommon.CheckStorageDomain(getParameters().getDestDomainId(), getReturnValue()
                    .getCanDoActionMessages());
        }
        if (retVal) {
            retVal = ImportExportCommon.CheckStoragePool(getParameters().getStoragePoolId(), getReturnValue()
                    .getCanDoActionMessages());
        }

        // check that the imported vm guid is not in engine
        if (retVal) {
            VmStatic duplicateVm = getVmStaticDAO().get(getParameters().getVm().getvm_guid());
            if (duplicateVm != null) {
                addCanDoActionMessage(VdcBllMessages.VM_CANNOT_IMPORT_VM_EXISTS);
                getReturnValue().getCanDoActionMessages().add(String.format("$VmName %1$s", duplicateVm.getvm_name()));
                retVal = false;
            }
        }

        // check that the imported vm name is not in engine
        if (retVal) {
            List<VmStatic> dupVmNmaes = getVmStaticDAO().getAllByName(getParameters().getVm().getvm_name());
            if (dupVmNmaes.size() >= 1) {
                addCanDoActionMessage(VdcBllMessages.VM_CANNOT_IMPORT_VM_EXISTS);
                getReturnValue().getCanDoActionMessages().add(String.format("$VmName %1$s", getVm().getvm_name()));
                retVal = false;
            }
        }

        setVmTemplateId(getVm().getvmt_guid());
        if (retVal) {
            if (!IsDomainActive(getParameters().getSourceDomainId(), getParameters().getStoragePoolId())
                    || !IsDomainActive(getParameters().getDestDomainId(), getParameters().getStoragePoolId())
                    || !TemplateExists() || !CheckTemplateInStorageDomain() || !CheckImagesGUIDsLegal() || !CanAddVm()) {
                retVal = false;
            }
        }
        if (retVal && !VmTemplateHandler.BlankVmTemplateId.equals(getVm().getvmt_guid()) && getVmTemplate() != null
                && getVmTemplate().getstatus() == VmTemplateStatus.Locked) {
            addCanDoActionMessage(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED);
            retVal = false;
        }
        if (retVal && getParameters().getCopyCollapse() && getParameters().getDiskInfoList() != null) {
            retVal = ImagesHandler.CheckImagesConfiguration(getParameters().getStorageDomainId(),
                    new java.util.ArrayList<DiskImageBase>(getParameters().getDiskInfoList().values()),
                    getReturnValue().getCanDoActionMessages());
        }
        // if collapse true we check that we have the template on source
        // (backup) domain
        if (retVal && getParameters().getCopyCollapse() && !TemplateExistsOnExportDomain()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMPORTED_TEMPLATE_IS_MISSING);
            getReturnValue().getCanDoActionMessages().add(
                    String.format("$DomainName %1$s",
                            getStorageDomainStaticDAO().get(getParameters().getSourceDomainId())
                                    .getstorage_name()));
            retVal = false;
        }
        if (retVal) {
            if (getStorageDomain().getstorage_domain_type() == StorageDomainType.ISO
                    || getStorageDomain().getstorage_domain_type() == StorageDomainType.ImportExport) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                retVal = false;
            }
        }
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
            boolean inCluster = false;
            List<VDSGroup> groups = getVdsGroupDAO().getAllForStoragePool(
                    getParameters().getStoragePoolId());
            for (VDSGroup group : groups) {
                if (group.getID().equals(getParameters().getVdsGroupId())) {
                    inCluster = true;
                    break;
                }
            }
            if (!inCluster) {
                addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
                retVal = false;
            }
        }
        if (retVal) {
            retVal = StorageDomainSpaceChecker.hasSpaceForRequest(getStorageDomain(), (int) getVm().getDiskSize());
            if (!retVal)
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
        }

        if (retVal && Config.<Boolean> GetValue(ConfigValues.LimitNumberOfNetworkInterfaces,
                getVdsGroup().getcompatibility_version().toString())) {
            // check that we have no more then 8 interfaces (kvm limitation in
            // version 2.x)
            if (!VmCommand.validateNumberOfNics(getParameters().getVm().getInterfaces(), null)) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_EXITED_MAX_INTERFACES);
                retVal = false;
            }
        }

        if (!retVal) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        }
        return retVal;
    }

    protected StorageDomainStaticDAO getStorageDomainStaticDAO() {
        return DbFacade.getInstance().getStorageDomainStaticDAO();
    }

    private boolean TemplateExistsOnExportDomain() {
        boolean retVal = false;
        if (!VmTemplateHandler.BlankVmTemplateId.equals(getParameters().getVm().getvmt_guid())) {
            GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(getParameters()
                    .getStoragePoolId(), getParameters().getSourceDomainId());
            tempVar.setGetAll(true);
            VdcQueryReturnValue qretVal = Backend.getInstance().runInternalQuery(
                    VdcQueryType.GetTemplatesFromExportDomain, tempVar);

            if (qretVal.getSucceeded()) {
                // (java.util.HashMap<VmTemplate,
                // java.util.ArrayList<DiskImage>>)qretVal.getReturnValue();
                Map templates = (Map) qretVal.getReturnValue();

                for (Object template : templates.keySet()) {
                    if (getParameters().getVm().getvmt_guid().equals(((VmTemplate) template).getId())) {
                        retVal = true;
                        break;
                    }
                }
            }
        } else {
            retVal = true;
        }
        return retVal;

    }

    protected boolean CheckTemplateInStorageDomain() {
        // LINQ && CheckIfDisksExist(Vm.DiskMap.Values.ToList());
        boolean retValue = CheckStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active)
                && CheckIfDisksExist(new ArrayList(getVm().getDiskMap().values()));
        if (retValue && !VmTemplateHandler.BlankVmTemplateId.equals(getVm().getvmt_guid())
                && !getParameters().getCopyCollapse()) {
            // Query returns an ArrayList of storage domains
            List<storage_domains> domains = (List<storage_domains>) Backend
                    .getInstance()
                    .runInternalQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                            new GetStorageDomainsByVmTemplateIdQueryParameters(getVm().getvmt_guid())).getReturnValue();

            // LINQ !domains.Select(a =>
            // a.id).Contains(MoveParameters.StorageDomainId))
            List<Guid> domainsId = LinqUtils.foreach(domains, new Function<storage_domains, Guid>() {
                @Override
                public Guid eval(storage_domains storageDomainStatic) {
                    return storageDomainStatic.getid();
                }
            });

            if (!domainsId.contains(getParameters().getStorageDomainId())) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }
        return retValue;
    }

    private boolean TemplateExists() {
        if (getVmTemplate() == null && !getParameters().getCopyCollapse()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            return false;
        }
        return true;
    }

    private boolean CheckImagesGUIDsLegal() {
        for (DiskImage image : getVm().getImages()) {
            Guid imageGUID = image.getId();
            Guid storagePoolId = image.getstorage_pool_id() != null ? image.getstorage_pool_id().getValue()
                    : Guid.Empty;
            Guid storageDomainId = getParameters().getSourceDomainId();
            Guid imageGroupId = image.getimage_group_id() != null ? image.getimage_group_id().getValue() : Guid.Empty;

            VDSReturnValue retValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DoesImageExist,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    imageGUID));

            if (retValue == null || retValue.getReturnValue() == null
                    || !(retValue.getReturnValue() instanceof Boolean)
                    || ((Boolean) (retValue.getReturnValue()) == false)) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
                return false;
            }
        }
        return true;
    }

    private boolean CanAddVm() {
        // Checking if a desktop with same name already exists
        boolean exists = (Boolean) getBackend()
                .runInternalQuery(VdcQueryType.IsVmWithSameNameExist,
                        new IsVmWithSameNameExistParameters(getVm().getvm_name())).getReturnValue();

        if (exists) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_IMPORT_VM_NAME_EXISTS);
        }
        return !exists;
    }

    @Override
    protected void executeCommand() {
        // Add Vm to Db
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                AddVmStatic();
                AddVmDynamic();
                AddVmNetwork();
                AddVmStatistics();
                getCompensationContext().stateChanged();
                return null;
            }
        });
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                AddVmImages();
                MoveOrCopyAllImageGroups();
                VmHandler.LockVm(getVm().getvm_guid());
                return null;

            }
        });
        setSucceeded(true);
    }

    @Override
    protected void MoveOrCopyAllImageGroups() {
        MoveOrCopyAllImageGroups(getVm().getvm_guid(), getVm().getDiskMap().values());
    }

    @Override
    protected void MoveOrCopyAllImageGroups(Guid containerID, Iterable<DiskImage> disks) {
        for (DiskImage disk : disks) {
            MoveOrCopyImageGroupParameters tempVar = new MoveOrCopyImageGroupParameters(containerID, disk
                    .getimage_group_id().getValue(), disk.getId(), getParameters().getStorageDomainId(),
                    getMoveOrCopyImageOperation());
            tempVar.setParentCommand(getActionType());
            tempVar.setEntityId(getParameters().getEntityId());
            tempVar.setUseCopyCollapse(getParameters().getCopyCollapse());
            tempVar.setCopyVolumeType(CopyVolumeType.LeafVol);
            tempVar.setPostZero(disk.getwipe_after_delete());
            tempVar.setForceOverride(true);
            MoveOrCopyImageGroupParameters p = tempVar;
            if (getParameters().getDiskInfoList() != null
                    && getParameters().getDiskInfoList().containsKey(disk.getinternal_drive_mapping())) {
                p.setVolumeType(getParameters().getDiskInfoList().get(disk.getinternal_drive_mapping())
                        .getvolume_type());
                p.setVolumeFormat(getParameters().getDiskInfoList().get(disk.getinternal_drive_mapping())
                        .getvolume_format());
            }
            p.setParentParemeters(getParameters());
            VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                    VdcActionType.MoveOrCopyImageGroup, p);
            getParameters().getImagesParameters().add(p);

            getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
        }
    }

    private void AddVmImages() {
        java.util.HashMap<String, java.util.ArrayList<DiskImage>> images = GetImagesLeaf(getVm().getImages());

        if (getParameters().getCopyCollapse()) {
            for (String drive : images.keySet()) {
                java.util.ArrayList<DiskImage> list = images.get(drive);
                DiskImage disk = list.get(list.size() - 1);

                disk.setParentId(VmTemplateHandler.BlankVmTemplateId);
                disk.setit_guid(VmTemplateHandler.BlankVmTemplateId);

                if (getParameters().getDiskInfoList() != null
                        && getParameters().getDiskInfoList().containsKey(disk.getinternal_drive_mapping())) {
                    disk.setvolume_format(getParameters().getDiskInfoList()
                            .get(disk.getinternal_drive_mapping())
                            .getvolume_format());
                    disk.setvolume_type(getParameters().getDiskInfoList().get(disk.getinternal_drive_mapping())
                            .getvolume_type());
                }

                DbFacade.getInstance().getDiskImageDAO().save(disk);
                DbFacade.getInstance().getDiskDao().save(disk.getDisk());

                DiskImageDynamic diskDynamic = new DiskImageDynamic();
                diskDynamic.setId(disk.getId());
                diskDynamic.setactual_size(disk.getactual_size());
                DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
                DbFacade.getInstance()
                        .getImageVmMapDAO()
                        .save(new image_vm_map(true, disk.getId(), getVm().getvm_guid()));
            }
        } else {
            for (DiskImage disk : getVm().getImages()) {
                DbFacade.getInstance().getDiskImageDAO().save(disk);

                DiskImageDynamic diskDynamic = new DiskImageDynamic();
                diskDynamic.setId(disk.getId());
                diskDynamic.setactual_size(disk.getactual_size());
                DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
            }

            for (String drive : images.keySet()) {
                java.util.ArrayList<DiskImage> list = images.get(drive);
                DiskImage disk = list.get(list.size() - 1);
                DbFacade.getInstance().getImageVmMapDAO().save(
                        new image_vm_map(true, disk.getId(), getVm().getvm_guid()));
                DbFacade.getInstance().getDiskDao().save(disk.getDisk());
            }
        }
    }

    // the last image in each list is the leaf
    public static java.util.HashMap<String, java.util.ArrayList<DiskImage>> GetImagesLeaf(
                                                                                          java.util.ArrayList<DiskImage> images) {
        java.util.HashMap<String, java.util.ArrayList<DiskImage>> retVal =
                new java.util.HashMap<String, java.util.ArrayList<DiskImage>>();

        for (DiskImage image : images) {
            // if (true) // !retVal.Keys.Contains(image.internal_drive_mapping))
            if (!retVal.keySet().contains(image.getinternal_drive_mapping())) {
                retVal.put(image.getinternal_drive_mapping(),
                        new java.util.ArrayList<DiskImage>(java.util.Arrays.asList(new DiskImage[] { image })));
            } else {
                retVal.get(image.getinternal_drive_mapping()).add(image);
            }
        }
        for (String key : retVal.keySet()) {
            SortImageList(retVal.get(key));
        }
        return retVal;
    }

    private static void SortImageList(java.util.ArrayList<DiskImage> images) {
        java.util.ArrayList<DiskImage> hold = new java.util.ArrayList<DiskImage>();
        DiskImage curr = null;

        // find the first image
        for (int i = 0; i < images.size(); i++) {
            int pos = GetFirstImage(images, images.get(i));
            if (pos == -1) {
                curr = images.get(i);
                hold.add(images.get(i));
                images.remove(images.get(i));
                break;
            }
        }

        while (images.size() > 0) {
            int pos = GetNextImage(images, curr);
            if (pos == -1) {
                // TODO: ERROR !!!
                log.error("Image list error in SortImageList");
                break;
            }
            curr = images.get(pos);
            hold.add(images.get(pos));
            images.remove(images.get(pos));
        }

        for (DiskImage image : hold) {
            images.add(image);
        }
    }

    // function retun the index of the image that has no parent
    private static int GetFirstImage(java.util.ArrayList<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (curr.getParentId().equals(images.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    // function return the index of image that is it's chiled
    private static int GetNextImage(java.util.ArrayList<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getParentId().equals(curr.getId())) {
                return i;
            }
        }
        return -1;
    }

    protected void AddVmStatic() {

        logImportEvents();
        getVm().getStaticData().setId(getVmId());
        getVm().getStaticData().setcreation_date(getNow());
        getVm().getStaticData().setvds_group_id(getParameters().getVdsGroupId());
        getVm().getStaticData().setMinAllocatedMem(ComputeMinAllocatedMem());
        if (getParameters().getCopyCollapse()) {
            getVm().setvmt_guid(VmTemplateHandler.BlankVmTemplateId);
        }
        DbFacade.getInstance().getVmStaticDAO().save(getVm().getStaticData());
        getCompensationContext().snapshotNewEntity(getVm().getStaticData());
    }

    private int ComputeMinAllocatedMem() {
        int vmMem = getVm().getmem_size_mb();
        int minAllocatedMem = vmMem;
        if (getVm().getMinAllocatedMem() > 0) {
            minAllocatedMem = getVm().getMinAllocatedMem();
        } else {
            // first get cluster memory over commit value
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDAO().get(getVm().getvds_group_id());
            if (vdsGroup != null && vdsGroup.getmax_vds_memory_over_commit() > 0) {
                minAllocatedMem = (vmMem * 100) / vdsGroup.getmax_vds_memory_over_commit();
            }
        }
        return minAllocatedMem;
    }

    private void logImportEvents() {
        // Some values at the OVF file are used for creating events at the GUI
        // for the sake of providing information on the content of the VM that
        // was exported,
        // but not setting it in the imported VM
        VmStatic vmStaticFromOvf = getVm().getStaticData();

        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(vmStaticFromOvf);
        Map<String, String> aliasesValuesMap = handler.getAliasesValuesMap();

        for (Map.Entry<String, String> entry : aliasesValuesMap.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            logField(vmStaticFromOvf, fieldName, fieldValue);
        }

        handler.resetDefaults(vmStaticForDefaultValues);

    }

    private void logField(VmStatic vmStaticFromOvf, String fieldName, String fieldValue) {
        String vmName = vmStaticFromOvf.getvm_name();
        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("FieldName", fieldName);
        logable.AddCustomValue("VmName", vmName);
        logable.AddCustomValue("FieldValue", fieldValue);
        AuditLogDirector.log(logable, AuditLogType.VM_IMPORT_INFO);
    }

    protected boolean macAdded = false;
    protected void AddVmNetwork() {
        // Add interfaces from template
        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            if (MacPoolManager.getInstance().IsMacInUse(iface.getMacAddress())) {
                AuditLogableBase logable = new AuditLogableBase();
                logable.AddCustomValue("MACAddr", iface.getMacAddress());
                logable.AddCustomValue("VmName", getVm().getvm_name());
                AuditLogDirector.log(logable, AuditLogType.MAC_ADDRESS_IS_IN_USE);
            }
            else {
                macAdded = MacPoolManager.getInstance().AddMac(iface.getMacAddress());
            }
            iface.setId(Guid.NewGuid());
            iface.setVmTemplateId(null);
            iface.setVmId(getVm().getStaticData().getId());
            DbFacade.getInstance().getVmNetworkInterfaceDAO().save(iface);
            DbFacade.getInstance().getVmNetworkStatisticsDAO().save(iface.getStatistics());
            getCompensationContext().snapshotNewEntity(iface);
            getCompensationContext().snapshotNewEntity(iface.getStatistics());
        }
    }

    private void AddVmDynamic() {
        VmDynamic tempVar = new VmDynamic();
        tempVar.setId(getVmId());
        tempVar.setstatus(VMStatus.ImageLocked);
        tempVar.setvm_host("");
        tempVar.setvm_ip("");
        tempVar.setapp_list(getParameters().getVm().getDynamicData().getapp_list());
        DbFacade.getInstance().getVmDynamicDAO().save(tempVar);
        getCompensationContext().snapshotNewEntity(tempVar);
    }

    private void AddVmStatistics() {
        VmStatistics stats = new VmStatistics();
        stats.setId(getVmId());
        DbFacade.getInstance().getVmStatisticsDAO().save(stats);
        getCompensationContext().snapshotNewEntity(stats);
        getCompensationContext().stateChanged();
    }

    @Override
    protected void EndSuccessfully() {
        EndImportCommand();
    }

    @Override
    protected void EndWithFailure() {
        setVm(null); //Going to try and refresh the VM by re-loading
        //it form DB
        VM vmFromParams = getParameters().getVm();
        if (getVm() != null) {
            VmHandler.UnLockVm(getVm().getvm_guid());
            for (DiskImage disk : getParameters().getImages()) {
                DbFacade.getInstance().getDiskImageDynamicDAO().remove(disk.getId());
                DbFacade.getInstance().getDiskImageDAO().remove(disk.getId());

                List<DiskImage> imagesForDisk =
                        DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForImageGroup(disk.getimage_group_id());
                if (imagesForDisk == null || imagesForDisk.isEmpty()) {
                    DbFacade.getInstance().getDiskDao().remove(disk.getimage_group_id());
                }
            }
            RemoveVmNetwork();
            DbFacade.getInstance().getVmDynamicDAO().remove(getVmId());
            DbFacade.getInstance().getVmStatisticsDAO().remove(getVmId());
            DbFacade.getInstance().getVmStaticDAO().remove(getVmId());
            setSucceeded(true);
        } else {
            setVm(vmFromParams); //Setting VM from params, for logging purposes
            //No point in trying to end action again, as the imported VM does not exist in the DB.
            getReturnValue().setEndActionTryAgain(false);
        }
    }

    protected void RemoveVmNetwork() {
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDAO()
                .getAllForVm(getVmId());
        if (interfaces != null) {
            for (VmNetworkInterface iface : interfaces) {
                if (macAdded) {
                    MacPoolManager.getInstance().freeMac(iface.getMacAddress());
                }
                DbFacade.getInstance().getVmNetworkInterfaceDAO().remove(iface.getId());
                DbFacade.getInstance().getVmNetworkStatisticsDAO().remove(iface.getId());
            }
        }
    }

    protected void EndImportCommand() {
        setVm(null);

        if (getVm() != null) {
            VmHandler.UnLockVm(getVm().getvm_guid());

            UpdateVmImSpm();
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("ImportVmCommand::EndImportCommand: Vm is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    protected boolean UpdateVmImSpm() {
        return VmCommand.UpdateVmInSpm(getVm().getstorage_pool_id(),
                new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { getVm() })));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_STARTING_IMPORT_VM
                    : AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_IMPORT_VM : AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        case END_FAILURE:
            return AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        }
        return super.getAuditLogTypeValue();
    }

    private static LogCompat log = LogFactoryCompat.getLog(ImportVmCommand.class);
}
