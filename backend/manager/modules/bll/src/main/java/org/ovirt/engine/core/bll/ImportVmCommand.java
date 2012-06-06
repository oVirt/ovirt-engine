package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
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
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.utils.MultiValueMapUtils;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.OvfLogEventHandler;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmCommand extends MoveOrCopyTemplateCommand<ImportVmParameters> {
    private static final long serialVersionUID = -5500615685812075744L;

    private static VmStatic vmStaticForDefaultValues = new VmStatic();
    private List<DiskImage> imageList;
    private List<Guid> diskGuidList = new ArrayList<Guid>();
    private List<Guid> imageGuidList = new ArrayList<Guid>();

    public ImportVmCommand(ImportVmParameters parameters) {
        super(parameters);
        setVmId(parameters.getContainerId());
        setVm(parameters.getVm());
        setVdsGroupId(parameters.getVdsGroupId());
        if (parameters.getVm() != null && getVm().getDiskMap() != null) {
            imageList = new ArrayList<DiskImage>();
            for (Disk disk : getVm().getDiskMap().values()) {
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    imageList.add((DiskImage) disk);
                }
            }
        }
        ensureDomainMap(imageList, getParameters().getDestDomainId());
    }

    protected ImportVmCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = true;
        List<String> canDoActionMessages = getReturnValue().getCanDoActionMessages();
        Map<Guid, storage_domains> domainsMap = new HashMap<Guid, storage_domains>();
        retVal = canDoAction_beforeCloneVm(retVal, canDoActionMessages, domainsMap);

        if (retVal && getParameters().isImportAsNewEntity()) {
            initImportClonedVm();
        }

        return retVal && canDoAction_afterCloneVm(retVal, canDoActionMessages, domainsMap);
    }


    private void initImportClonedVm() {
        Guid guid = Guid.NewGuid();
        getVm().setId(guid);
        setVmId(guid);
        getVm().setvm_name(getParameters().getVm().getvm_name());
        getVm().setstorage_pool_id(getParameters().getStoragePoolId());
        getParameters().setVm(getVm());
        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            iface.setId(Guid.NewGuid());
            String mac = MacPoolManager.getInstance().allocateNewMac();
            iface.setMacAddress(mac);
        }
    }

    private boolean canDoAction_beforeCloneVm(boolean retVal,
            List<String> canDoActionMessages,
            Map<Guid, storage_domains> domainsMap) {
        if (getVm() != null) {
            setDescription(getVmName());
        }
        retVal = checkStoragePool();

        if (retVal) {
            Set<Guid> destGuids = new HashSet<Guid>(imageToDestinationDomainMap.values());
            for (Guid destGuid : destGuids) {
                storage_domains storageDomain = getStorageDomain(destGuid);
                StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
                if (!validator.isDomainExistAndActive(canDoActionMessages)
                        || !validator.domainIsValidDestination(canDoActionMessages)) {
                    retVal = false;
                    break;
                } else {
                    domainsMap.put(destGuid, storageDomain);
                }
            }
        }

        if (retVal && getParameters().isImportAsNewEntity() && !getParameters().getCopyCollapse()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMPORT_CLONE_NOT_COLLAPSED);
            retVal = false;
        }

        if (retVal) {
            SetSourceDomainId(getParameters().getSourceDomainId());
            StorageDomainValidator validator = new StorageDomainValidator(getSourceDomain());
            retVal =
                    validator.isDomainExistAndActive(canDoActionMessages);
            if (retVal && getSourceDomain().getstorage_domain_type() != StorageDomainType.ImportExport) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                retVal = false;
            }
        }

        if (retVal) {
            // Load images from Import/Export domain
            GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(getParameters()
                    .getStoragePoolId(), getParameters().getSourceDomainId());
            tempVar.setGetAll(true);
            VdcQueryReturnValue qretVal = getBackend().runInternalQuery(VdcQueryType.GetVmsFromExportDomain,
                    tempVar);

            retVal = qretVal.getSucceeded();
            if (retVal) {
                List<VM> vms = (List<VM>) qretVal.getReturnValue();
                VM vm = LinqUtils.firstOrNull(vms, new Predicate<VM>() {
                    @Override
                    public boolean eval(VM vm) {
                        return vm.getId().equals(getParameters().getVm().getId());
                    }
                });

                if (vm != null) {
                    // At this point we should work with the VM that was read from
                    // the OVF
                    setVm(vm);
                    for (DiskImage image : getVm().getImages()) {
                        for (DiskImage p : imageList) {
                            // copy the new disk volume format/type if provided,
                            // only if requested by the user
                            if (getParameters().getCopyCollapse()) {
                                if (p.getImageId().equals(image.getImageId())) {
                                    if (p.getvolume_format() != null) {
                                        image.setvolume_format(p.getvolume_format());
                                    }
                                    if (p.getvolume_type() != null) {
                                        image.setvolume_type(p.getvolume_type());
                                    }
                                }
                            }
                            if (image.getId().equals(p.getId())
                                    && !imageToDestinationDomainMap.containsKey(image.getId())) {
                                imageToDestinationDomainMap.put(image.getId(),
                                        imageToDestinationDomainMap.get(p.getId()));
                            }
                        }
                        retVal =
                                ImagesHandler.CheckImageConfiguration(domainsMap.get(imageToDestinationDomainMap.get(image.getId()))
                                        .getStorageStaticData(),
                                        image,
                                        canDoActionMessages);
                        if (!retVal) {
                            break;
                        } else {
                            image.setstorage_pool_id(getParameters().getStoragePoolId());
                            // we put the source domain id in order that copy will
                            // work
                            // ok
                            // we fix it to DestDomainId in
                            // MoveOrCopyAllImageGroups();
                            image.setstorage_ids(new ArrayList<Guid>(Arrays.asList(getParameters().getSourceDomainId())));
                        }
                    }
                    if (retVal) {
                        Map<Guid, List<DiskImage>> images = GetImagesLeaf(getVm().getImages());
                        for (Guid id : images.keySet()) {
                            List<DiskImage> list = images.get(id);
                            getVm().getDiskMap().put(id, list.get(list.size() - 1));
                        }
                    }
                } else {
                    retVal = false;
                }
            }
        }

        return retVal;
    }

    private boolean canDoAction_afterCloneVm(boolean retVal,
            List<String> canDoActionMessages,
            Map<Guid, storage_domains> domainsMap) {
        // check that the imported vm guid is not in engine
        if (retVal) {
            VmStatic duplicateVm = getVmStaticDAO().get(getVm().getId());
            if (duplicateVm != null) {
                addCanDoActionMessage(VdcBllMessages.VM_CANNOT_IMPORT_VM_EXISTS);
                addCanDoActionMessage(String.format("$VmName %1$s", duplicateVm.getvm_name()));
                retVal = false;
            }
        }

        setVmTemplateId(getVm().getvmt_guid());
        if (retVal) {
            if (!TemplateExists() || !CheckTemplateInStorageDomain() || !CheckImagesGUIDsLegal() || !CanAddVm()) {
                retVal = false;
            }
        }
        if (retVal && !VmTemplateHandler.BlankVmTemplateId.equals(getVm().getvmt_guid()) && getVmTemplate() != null
                && getVmTemplate().getstatus() == VmTemplateStatus.Locked) {
            addCanDoActionMessage(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED);
            retVal = false;
        }
        if (retVal && getParameters().getCopyCollapse() && getParameters().getDiskInfoList() != null) {
            for (DiskImageBase imageBase : getParameters().getDiskInfoList().values()) {
                DiskImage key = (DiskImage) getVm().getDiskMap()
                        .get(imageBase.getId());
                if (key != null) {
                    retVal =
                            ImagesHandler.CheckImageConfiguration(domainsMap.get(imageToDestinationDomainMap.get(key.getId()))
                                    .getStorageStaticData(),
                                    imageBase,
                                    canDoActionMessages);
                    if (!retVal) {
                        break;
                    }
                }
            }
        }
        // if collapse true we check that we have the template on source
        // (backup) domain
        if (retVal && getParameters().getCopyCollapse() && !TemplateExistsOnExportDomain()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMPORTED_TEMPLATE_IS_MISSING);
            addCanDoActionMessage(String.format("$DomainName %1$s",
                    getStorageDomainStaticDAO().get(getParameters().getSourceDomainId())
                            .getstorage_name()));
            retVal = false;
        }

        if (retVal) {
            boolean inCluster = false;
            List<VDSGroup> groups = getVdsGroupDAO().getAllForStoragePool(
                    getParameters().getStoragePoolId());
            for (VDSGroup group : groups) {
                if (group.getId().equals(getParameters().getVdsGroupId())) {
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
            Map<storage_domains, Integer> domainMap = getSpaceRequirementsForStorageDomains(imageList);

            for (Map.Entry<storage_domains, Integer> entry : domainMap.entrySet()) {
                retVal = StorageDomainSpaceChecker.hasSpaceForRequest(entry.getKey(), entry.getValue());
                if (!retVal) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
                    break;
                }
            }
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

        // Check that the USB policy is legal
        if (retVal) {
            retVal = VmHandler.isUsbPolicyLegal(getParameters().getVm().getusb_policy(), getParameters().getVm().getos(), getVdsGroup(), getReturnValue().getCanDoActionMessages());
        }

        if (!retVal) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        }
        return retVal;
    }

    @Override
    protected boolean validateQuota() {
        // Set default quota id if storage pool enforcement is disabled.
        getParameters().setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(getVm().getStaticData().getQuotaId(),
                getStoragePool()));
        for (Disk dit : getVm().getDiskMap().values()) {
            ((DiskImage) dit).setQuotaId(QuotaHelper.getInstance()
                    .getQuotaIdToConsume(getVm().getStaticData().getQuotaId(),
                            getStoragePool()));
        }
        // TODO: Validate quota for import VM.
        return true;
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
        boolean retValue = getParameters().isImportAsNewEntity() || checkIfDisksExist(imageList);
        if (retValue && !VmTemplateHandler.BlankVmTemplateId.equals(getVm().getvmt_guid())
                && !getParameters().getCopyCollapse()) {
            List<storage_domains> domains = (List<storage_domains>) Backend
                    .getInstance()
                    .runInternalQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                            new GetStorageDomainsByVmTemplateIdQueryParameters(getVm().getvmt_guid())).getReturnValue();
            List<Guid> domainsId = LinqUtils.foreach(domains, new Function<storage_domains, Guid>() {
                @Override
                public Guid eval(storage_domains storageDomainStatic) {
                    return storageDomainStatic.getId();
                }
            });

            if (Collections.disjoint(domainsId, imageToDestinationDomainMap.values())) {
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
            Guid imageGUID = image.getImageId();
            Guid storagePoolId = image.getstorage_pool_id() != null ? image.getstorage_pool_id().getValue()
                    : Guid.Empty;
            Guid storageDomainId = getParameters().getSourceDomainId();
            Guid imageGroupId = image.getId() != null ? image.getId() : Guid.Empty;

            VDSReturnValue retValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DoesImageExist,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    imageGUID));

            if (Boolean.FALSE.equals(retValue.getReturnValue())) {
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
        addVmToDb();
        processImages();
        setSucceeded(true);
    }

    private void addVmToDb() {
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
    }

    private void processImages() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                addVmImagesAndSnapshots();
                updateSnapshotsFromExport();
                MoveOrCopyAllImageGroups();
                VmDeviceUtils.addImportedDevices(getVm().getStaticData());
                VmHandler.LockVm(getVm().getId());
                if (getParameters().isImportAsNewEntity()) {
                    getParameters().setVm(getVm());
                    setVmId(getVm().getId());
                }
                return null;

            }
        });
    }

    @Override
    protected void MoveOrCopyAllImageGroups() {
        MoveOrCopyAllImageGroups(getVm().getId(),
                ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), false, false));
    }

    @Override
    protected void MoveOrCopyAllImageGroups(Guid containerID, Iterable<DiskImage> disks) {
        int i = 0;
        for (DiskImage disk : disks) {
            Guid destinationDomain = imageToDestinationDomainMap.get(diskGuidList.get(i));
            MoveOrCopyImageGroupParameters p = new MoveOrCopyImageGroupParameters(containerID,
                    diskGuidList.get(i),
                    imageGuidList.get(i),
                    disk.getId(),
                    disk.getImageId(),
                    destinationDomain, getMoveOrCopyImageOperation());
            p.setParentCommand(getActionType());
            p.setUseCopyCollapse(getParameters().getCopyCollapse());
            p.setCopyVolumeType(CopyVolumeType.LeafVol);
            p.setPostZero(disk.isWipeAfterDelete());
            p.setForceOverride(true);
            p.setSourceDomainId(getParameters().getSourceDomainId());
            p.setStoragePoolId(getParameters().getStoragePoolId());
            if (getParameters().getDiskInfoList() != null
                    && getParameters().getDiskInfoList().containsKey(diskGuidList.get(i))) {
                p.setVolumeType(getParameters().getDiskInfoList().get(diskGuidList.get(i))
                        .getvolume_type());
                p.setVolumeFormat(getParameters().getDiskInfoList().get(diskGuidList.get(i))
                        .getvolume_format());
            }
            p.setParentParemeters(getParameters());
            p.setAddImageDomainMapping(getParameters().isImportAsNewEntity());
            VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                    VdcActionType.MoveOrCopyImageGroup,
                    p,
                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
            if (!vdcRetValue.getSucceeded()) {
                throw new VdcBLLException(vdcRetValue.getFault().getError(),
                        "ImportVmCommand::MoveOrCopyAllImageGroups: Failed to copy disk!");
            }
            getParameters().getImagesParameters().add(p);

            getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
            i++;
        }
    }

    private void addVmImagesAndSnapshots() {
        Map<Guid, List<DiskImage>> images = GetImagesLeaf(getVm().getImages());

        if (getParameters().getCopyCollapse()) {
            Guid snapshotId = Guid.NewGuid();
            for (Guid id : images.keySet()) {
                List<DiskImage> list = images.get(id);
                DiskImage disk = list.get(list.size() - 1);

                disk.setParentId(VmTemplateHandler.BlankVmTemplateId);
                disk.setit_guid(VmTemplateHandler.BlankVmTemplateId);
                disk.setvm_snapshot_id(snapshotId);
                disk.setactive(true);

                if (getParameters().getDiskInfoList() != null
                        && getParameters().getDiskInfoList().containsKey(disk.getId())) {
                    disk.setvolume_format(getParameters().getDiskInfoList()
                            .get(disk.getId())
                            .getvolume_format());
                    disk.setvolume_type(getParameters().getDiskInfoList().get(disk.getId())
                            .getvolume_type());
                }
                diskGuidList.add(disk.getId());
                imageGuidList.add(disk.getImageId());
                if (getParameters().isImportAsNewEntity()) {
                    disk.setId(Guid.NewGuid());
                    disk.setImageId(Guid.NewGuid());
                }
                disk.setvm_guid(getVmId());
                disk.setcreation_date(new Date());
                BaseImagesCommand.saveImage(disk);
                ImagesHandler.setDiskAlias(disk, getVm());
                DbFacade.getInstance().getBaseDiskDao().save(disk);
                DiskImageDynamic diskDynamic = new DiskImageDynamic();
                diskDynamic.setId(disk.getImageId());
                diskDynamic.setactual_size(disk.getactual_size());
                DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
            }

            Snapshot snapshot = new SnapshotsManager().addActiveSnapshot(snapshotId, getVm(), getCompensationContext());
            getVm().getSnapshots().clear();
            getVm().getSnapshots().add(snapshot);
        } else {
            Guid snapshotId = null;
            for (DiskImage disk : getVm().getImages()) {
                diskGuidList.add(disk.getId());
                imageGuidList.add(disk.getImageId());
                disk.setactive(false);
                BaseImagesCommand.saveImage(disk);
                snapshotId = disk.getvm_snapshot_id().getValue();
                if (!getSnapshotDao().exists(getVm().getId(), snapshotId)) {
                    getSnapshotDao().save(
                            new Snapshot(snapshotId,
                                    SnapshotStatus.OK,
                                    getVm().getId(),
                                    null,
                                    SnapshotType.REGULAR,
                                    disk.getdescription(),
                                    disk.getlast_modified_date(),
                                    disk.getappList()));
                }

                DiskImageDynamic diskDynamic = new DiskImageDynamic();
                diskDynamic.setId(disk.getImageId());
                diskDynamic.setactual_size(disk.getactual_size());
                DbFacade.getInstance().getDiskImageDynamicDAO().save(diskDynamic);
            }

            for (Guid id : images.keySet()) {
                List<DiskImage> list = images.get(id);
                DiskImage disk = list.get(list.size() - 1);
                snapshotId = disk.getvm_snapshot_id().getValue();
                disk.setactive(true);
                DbFacade.getInstance().getImageDao().update(disk.getImage());
                DbFacade.getInstance().getBaseDiskDao().save(disk);
            }

            // Update active snapshot's data, since it was inserted as a regular snapshot.
            getSnapshotDao().update(
                    new Snapshot(snapshotId,
                            SnapshotStatus.OK,
                            getVm().getId(),
                            null,
                            SnapshotType.ACTIVE,
                            "Active VM snapshot",
                            new Date(),
                            null));
        }
    }

    /**
     * Go over the snapshots that were read from the export data. If the snapshot exists (since it was added for the
     * images), it will be updated. If it doesn't exist, it will be saved.
     */
    private void updateSnapshotsFromExport() {
        if (getVm().getSnapshots() != null) {
            for (Snapshot snapshot : getVm().getSnapshots()) {
                if (getSnapshotDao().exists(getVm().getId(), snapshot.getId())) {
                    getSnapshotDao().update(snapshot);
                } else {
                    getSnapshotDao().save(snapshot);
                }
            }
        }
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    // the last image in each list is the leaf
    public static Map<Guid, List<DiskImage>> GetImagesLeaf(List<DiskImage> images) {
        Map<Guid, List<DiskImage>> retVal = new HashMap<Guid, List<DiskImage>>();
        for (DiskImage image : images) {
            MultiValueMapUtils.addToMap(image.getId(), image, retVal);
        }

        for (Guid key : retVal.keySet()) {
            SortImageList(retVal.get(key));
        }
        return retVal;
    }

    private static void SortImageList(List<DiskImage> images) {
        List<DiskImage> hold = new ArrayList<DiskImage>();
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

    // function return the index of the image that has no parent
    private static int GetFirstImage(List<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (curr.getParentId().equals(images.get(i).getImageId())) {
                return i;
            }
        }
        return -1;
    }

    // function return the index of image that is it's chiled
    private static int GetNextImage(List<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getParentId().equals(curr.getImageId())) {
                return i;
            }
        }
        return -1;
    }

    protected void AddVmStatic() {

        logImportEvents();
        getVm().getStaticData().setId(getVmId());
        getVm().getStaticData().setcreation_date(new Date());
        getVm().getStaticData().setvds_group_id(getParameters().getVdsGroupId());
        getVm().getStaticData().setMinAllocatedMem(ComputeMinAllocatedMem());
        getVm().getStaticData().setQuotaId(getParameters().getQuotaId());
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
        addInterfacesFromTemplate();
        auditInvalidInterfaces();
    }

    private void addInterfacesFromTemplate() {
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager();

        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            if (iface.getId() == null) {
                iface.setId(Guid.NewGuid());
            }
            iface.setVmTemplateId(null);
            iface.setVmId(getVm().getStaticData().getId());
            iface.setVmName(getVm().getvm_name());

            if (getParameters().isImportAsNewEntity()) {
                macAdded = true;
            } else {
                macAdded = vmInterfaceManager.add(iface, getCompensationContext());
            }
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
        setVm(null); // Going to try and refresh the VM by re-loading
        // it form DB
        VM vmFromParams = getParameters().getVm();
        if (getVm() != null) {
            VmHandler.UnLockVm(getVm().getId());
            for (DiskImage disk : imageList) {
                DbFacade.getInstance().getDiskImageDynamicDAO().remove(disk.getImageId());
                DbFacade.getInstance().getImageDao().remove(disk.getImageId());

                List<DiskImage> imagesForDisk =
                        DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForImageGroup(disk.getId());
                if (imagesForDisk == null || imagesForDisk.isEmpty()) {
                    DbFacade.getInstance().getBaseDiskDao().remove(disk.getId());
                }
            }
            RemoveVmNetwork();
            new SnapshotsManager().removeSnapshots(getVm().getId());
            DbFacade.getInstance().getVmDynamicDAO().remove(getVmId());
            DbFacade.getInstance().getVmStatisticsDAO().remove(getVmId());
            new SnapshotsManager().removeSnapshots(getVmId());
            DbFacade.getInstance().getVmStaticDAO().remove(getVmId());
            setSucceeded(true);
        } else {
            setVm(vmFromParams); // Setting VM from params, for logging purposes
            // No point in trying to end action again, as the imported VM does not exist in the DB.
            getReturnValue().setEndActionTryAgain(false);
        }
    }

    protected void RemoveVmNetwork() {
        new VmInterfaceManager().removeAll(macAdded, getVmId());
    }

    protected void EndImportCommand() {
        if (!getParameters().isImportAsNewEntity()) {
            setVm(null);
        }

        EndActionOnAllImageGroups();
        if (getVm() != null) {
            VmHandler.UnLockVm(getVm().getId());

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
                new ArrayList<VM>(Arrays.asList(new VM[] { getVm() })));
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

    /**
     * log to audit-log if VmInterfaces are attached on non VmNetworks
     */
    private void auditInvalidInterfaces() {
        List<VmNetworkInterface> interfaces = getVm().getInterfaces();
        StringBuilder networks = new StringBuilder();
        StringBuilder ifaces = new StringBuilder();
        for (VmNetworkInterface iface : interfaces) {
            if (!VmInterfaceManager.isValidVmNetwork(iface, getVm().getvds_group_id())) {
                networks.append(iface.getNetworkName()).append(",");
                ifaces.append(iface.getName()).append(",");
            }
        }

        if (networks.length() > 0) {
            networks.deleteCharAt(networks.length()); // remove the last comma
            AuditLogableBase logable = new AuditLogableBase();
            logable.AddCustomValue("Newtorks", networks.toString());
            logable.AddCustomValue("Interfaces", ifaces.toString());
            AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_IMPORT_VM_INTERFACES_ON_NON_VM_NETWORKS);
        }
    }

    private static Log log = LogFactory.getLog(ImportVmCommand.class);
}
