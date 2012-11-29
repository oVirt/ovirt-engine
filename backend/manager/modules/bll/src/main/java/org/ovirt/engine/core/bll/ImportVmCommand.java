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

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.Network;
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
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
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

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmCommand extends MoveOrCopyTemplateCommand<ImportVmParameters>
        implements QuotaStorageDependent {
    private static final long serialVersionUID = -5500615685812075744L;
    private static final Log log = LogFactory.getLog(ImportVmCommand.class);

    private static VmStatic vmStaticForDefaultValues = new VmStatic();
    private List<DiskImage> imageList;
    private List<Guid> diskGuidList = new ArrayList<Guid>();
    private List<Guid> imageGuidList = new ArrayList<Guid>();
    private List<String> macsAdded = new ArrayList<String>();

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
    public Guid getVmId() {
        if (getParameters().isImportAsNewEntity()) {
            return getParameters().getVm().getId();
        }
        return super.getVmId();
    }
    @Override
    public VM getVm() {
        if (getParameters().isImportAsNewEntity()) {
            return getParameters().getVm();
        }
        return super.getVm();
    }

    @Override
    protected boolean canDoAction() {
        Map<Guid, storage_domains> domainsMap = new HashMap<Guid, storage_domains>();

        if (!canDoActionBeforeCloneVm(domainsMap)) {
            return false;
        }

        if (getParameters().isImportAsNewEntity()) {
            initImportClonedVm();
        }

        return canDoActionAfterCloneVm(domainsMap);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    private void initImportClonedVm() {
        Guid guid = Guid.NewGuid();
        getVm().setId(guid);
        setVmId(guid);
        getVm().setVmName(getParameters().getVm().getVmName());
        getVm().setStoragePoolId(getParameters().getStoragePoolId());
        getParameters().setVm(getVm());
        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            iface.setId(Guid.NewGuid());
        }
    }

    private boolean canDoActionBeforeCloneVm(Map<Guid, storage_domains> domainsMap) {
        List<String> canDoActionMessages = getReturnValue().getCanDoActionMessages();

        if (getVm() != null) {
            setDescription(getVmName());
        }

        if (!checkStoragePool()) {
            return false;
        }

        Set<Guid> destGuids = new HashSet<Guid>(imageToDestinationDomainMap.values());
        for (Guid destGuid : destGuids) {
            storage_domains storageDomain = getStorageDomain(destGuid);
            StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
            if (!validator.isDomainExistAndActive(canDoActionMessages)
                    || !validator.domainIsValidDestination(canDoActionMessages)) {
                return false;
            }

            domainsMap.put(destGuid, storageDomain);
        }

        if (getParameters().isImportAsNewEntity() && !getParameters().getCopyCollapse()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_IMPORT_CLONE_NOT_COLLAPSED);
        }

        setSourceDomainId(getParameters().getSourceDomainId());
        StorageDomainValidator validator = new StorageDomainValidator(getSourceDomain());
        if (validator.isDomainExistAndActive(canDoActionMessages) &&
                getSourceDomain().getstorage_domain_type() != StorageDomainType.ImportExport) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        List<VM> vms = getVmsFromExportDomain();
        if (vms == null) {
            return false;
        }

        VM vm = LinqUtils.firstOrNull(vms, new Predicate<VM>() {
            @Override
            public boolean eval(VM evalVm) {
                return evalVm.getId().equals(getParameters().getVm().getId());
            }
        });

        if (vm != null) {
            // At this point we should work with the VM that was read from
            // the OVF
            setVm(vm);
            // Iterate over all the VM images (active image and snapshots)
            for (DiskImage image : getVm().getImages()) {
                if (getParameters().getCopyCollapse()) {
                    // If copy collapse sent then iterate over the images got from the parameters, until we got
                    // a match with the image from the VM.
                    for (DiskImage p : imageList) {
                        // copy the new disk volume format/type if provided,
                        // only if requested by the user
                        if (p.getImageId().equals(image.getImageId())) {
                            if (p.getvolume_format() != null) {
                                image.setvolume_format(p.getvolume_format());
                            }
                            if (p.getvolume_type() != null) {
                                image.setvolume_type(p.getvolume_type());
                            }
                            // Validate the configuration of the image got from the parameters.
                            if (!validateImageConfig(canDoActionMessages, domainsMap, image)) {
                                return false;
                            }
                            break;
                        }
                    }
                } else {
                    // If no copy collapse sent, validate each image configuration (snapshot or active image).
                    if (!validateImageConfig(canDoActionMessages, domainsMap, image)) {
                        return false;
                    }
                }

                image.setstorage_pool_id(getParameters().getStoragePoolId());
                // we put the source domain id in order that copy will
                // work properly.
                // we fix it to DestDomainId in
                // MoveOrCopyAllImageGroups();
                image.setstorage_ids(new ArrayList<Guid>(Arrays.asList(getParameters().getSourceDomainId())));
            }

            Map<Guid, List<DiskImage>> images = getImagesLeaf(getVm().getImages());
            for (Map.Entry<Guid, List<DiskImage>> entry : images.entrySet()) {
                Guid id = entry.getKey();
                List<DiskImage> diskList = entry.getValue();
                getVm().getDiskMap().put(id, diskList.get(diskList.size() - 1));
            }
        }

        return true;
    }

    /**
     * Load images from Import/Export domain.
     * @return A {@link List} of {@link VM}s, or <code>null</code> if the query to the export domain failed.
     */
    protected List<VM> getVmsFromExportDomain() {
        GetAllFromExportDomainQueryParameters p =
                new GetAllFromExportDomainQueryParameters
                (getParameters().getStoragePoolId(), getParameters().getSourceDomainId());
        VdcQueryReturnValue qRetVal = getBackend().runInternalQuery(VdcQueryType.GetVmsFromExportDomain, p);

        if (!qRetVal.getSucceeded()) {
            return null;
        }

        return (List<VM>) qRetVal.getReturnValue();
    }

    private boolean validateImageConfig(List<String> canDoActionMessages,
            Map<Guid, storage_domains> domainsMap,
            DiskImage image) {
        return ImagesHandler.CheckImageConfiguration(domainsMap.get(imageToDestinationDomainMap.get(image.getId()))
                        .getStorageStaticData(),
                        image,
                        canDoActionMessages);
    }

    private boolean canDoActionAfterCloneVm(Map<Guid, storage_domains> domainsMap) {
        VM vm = getParameters().getVm();

        // check that the imported vm guid is not in engine
        if (!validateNoDuplicateVm()) {
            return false;
        }

        setVmTemplateId(getVm().getVmtGuid());
        if (!templateExists() || !checkTemplateInStorageDomain() || !checkImagesGUIDsLegal() || !canAddVm()) {
            return false;
        }

        if (!VmTemplateHandler.BlankVmTemplateId.equals(getVm().getVmtGuid())
                && getVmTemplate() != null
                && getVmTemplate().getstatus() == VmTemplateStatus.Locked) {
            return failCanDoAction(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED);
        }

        if (getParameters().getCopyCollapse() && vm.getDiskMap() != null) {
            for (Disk disk : vm.getDiskMap().values()) {
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage key = (DiskImage) getVm().getDiskMap().get(disk.getId());

                    if (key != null) {
                        if (!ImagesHandler.CheckImageConfiguration(domainsMap.get(imageToDestinationDomainMap.get(key.getId()))
                                .getStorageStaticData(),
                                (DiskImageBase) disk,
                                getReturnValue().getCanDoActionMessages())) {
                            return false;
                        }
                    }
                }
            }
        }

        // if collapse true we check that we have the template on source
        // (backup) domain
        if (getParameters().getCopyCollapse() && !templateExistsOnExportDomain()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMPORTED_TEMPLATE_IS_MISSING);
            addCanDoActionMessage(String.format("$DomainName %1$s",
                    getStorageDomainStaticDAO().get(getParameters().getSourceDomainId()).getstorage_name()));
            return false;
        }

        if (!validateVdsCluster()) {
            return false;
        }

        Map<storage_domains, Integer> domainMap = getSpaceRequirementsForStorageDomains(imageList);

        for (Map.Entry<storage_domains, Integer> entry : domainMap.entrySet()) {
            if (!StorageDomainSpaceChecker.hasSpaceForRequest(entry.getKey(), entry.getValue())) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
            }
        }

        if (!validateUsbPolicy()) {
            return false;
        }

        if (!validateMacAddress(getVm().getInterfaces())) {
            return false;
        }

        return true;
    }

    /**
     * Validates that there is no duplicate VM.
     * @return <code>true</code> if the validation passes, <code>false</code> otherwise.
     */
    protected boolean validateNoDuplicateVm() {
        VmStatic duplicateVm = getVmStaticDAO().get(getVm().getId());
        if (duplicateVm != null) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_IMPORT_VM_EXISTS);
            addCanDoActionMessage(String.format("$VmName %1$s", duplicateVm.getvm_name()));
            return false;
        }
        return true;
    }

    /**
     * Validates that that the required cluster exists.
     * @return <code>true</code> if the validation passes, <code>false</code> otherwise.
     */
    protected boolean validateVdsCluster() {
        List<VDSGroup> groups = getVdsGroupDAO().getAllForStoragePool(getParameters().getStoragePoolId());
        for (VDSGroup group : groups) {
            if (group.getId().equals(getParameters().getVdsGroupId())) {
                return true;
            }
        }
        return failCanDoAction(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
    }

    /**
     * Validates the USB policy.
     * @return <code>true</code> if the validation passes, <code>false</code> otherwise.
     */
    protected boolean validateUsbPolicy() {
        VM vm = getParameters().getVm();
        VmHandler.updateImportedVmUsbPolicy(vm.getStaticData());
        return VmHandler.isUsbPolicyLegal(vm.getUsbPolicy(),
                vm.getOs(),
                getVdsGroup(),
                getReturnValue().getCanDoActionMessages());
    }

    private boolean templateExistsOnExportDomain() {
        boolean retVal = false;
        if (!VmTemplateHandler.BlankVmTemplateId.equals(getParameters().getVm().getVmtGuid())) {
            GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(getParameters()
                    .getStoragePoolId(), getParameters().getSourceDomainId());
            VdcQueryReturnValue qretVal = Backend.getInstance().runInternalQuery(
                    VdcQueryType.GetTemplatesFromExportDomain, tempVar);

            if (qretVal.getSucceeded()) {
                Map templates = (Map) qretVal.getReturnValue();

                for (Object template : templates.keySet()) {
                    if (getParameters().getVm().getVmtGuid().equals(((VmTemplate) template).getId())) {
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

    protected boolean checkTemplateInStorageDomain() {
        boolean retValue = getParameters().isImportAsNewEntity() || checkIfDisksExist(imageList);
        if (retValue && !VmTemplateHandler.BlankVmTemplateId.equals(getVm().getVmtGuid())
                && !getParameters().getCopyCollapse()) {
            List<storage_domains> domains = (List<storage_domains>) Backend
                    .getInstance()
                    .runInternalQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                            new GetStorageDomainsByVmTemplateIdQueryParameters(getVm().getVmtGuid())).getReturnValue();
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

    private boolean templateExists() {
        if (getVmTemplate() == null && !getParameters().getCopyCollapse()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            return false;
        }
        return true;
    }

    protected boolean checkImagesGUIDsLegal() {
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

    protected boolean canAddVm() {
        // Checking if a desktop with same name already exists
        boolean exists = (Boolean) getBackend()
                .runInternalQuery(VdcQueryType.IsVmWithSameNameExist,
                        new IsVmWithSameNameExistParameters(getVm().getVmName())).getReturnValue();

        if (exists) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_IMPORT_VM_NAME_EXISTS);
        }
        return !exists;
    }

    @Override
    protected void executeCommand() {
        try {
            addVmToDb();
            VM vm = getVm();
            // if there aren't any images- we can just perform the end
            // vm related ops
            if (vm.getImages().isEmpty()) {
                endVmRelatedOps();
            } else {
                processImages();
            }
        } catch (RuntimeException e) {
            MacPoolManager.getInstance().freeMacs(macsAdded);
            throw e;
        }

        setSucceeded(true);
    }

    private void addVmToDb() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                addVmStatic();
                addVmDynamic();
                addVmInterfaces();
                addVmStatistics();
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
                moveOrCopyAllImageGroups();
                VmDeviceUtils.addImportedDevices(getVm().getStaticData(), getParameters().isImportAsNewEntity());
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
    protected void moveOrCopyAllImageGroups() {
        moveOrCopyAllImageGroups(getVm().getId(),
                ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), false, false));
    }

    @Override
    protected void moveOrCopyAllImageGroups(Guid containerID, Iterable<DiskImage> disks) {
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
            p.setForceOverride(getParameters().getForceOverride());
            p.setSourceDomainId(getParameters().getSourceDomainId());
            p.setStoragePoolId(getParameters().getStoragePoolId());
            p.setImportEntity(true);
            p.setEntityId(disk.getImageId());
            p.setQuotaId(disk.getQuotaId() != null ? disk.getQuotaId() : getParameters().getQuotaId());
            if (getParameters().getVm().getDiskMap() != null
                    && getParameters().getVm().getDiskMap().containsKey(diskGuidList.get(i))) {
                DiskImageBase diskImageBase =
                        (DiskImageBase) getParameters().getVm().getDiskMap().get(diskGuidList.get(i));
                p.setVolumeType(diskImageBase.getvolume_type());
                p.setVolumeFormat(diskImageBase.getvolume_format());
            }
            p.setParentParameters(getParameters());
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
        Map<Guid, List<DiskImage>> images = getImagesLeaf(getVm().getImages());

        if (getParameters().getCopyCollapse()) {
            Guid snapshotId = Guid.NewGuid();
            for (List<DiskImage> diskList : images.values()) {
                DiskImage disk = diskList.get(diskList.size() - 1);

                disk.setParentId(VmTemplateHandler.BlankVmTemplateId);
                disk.setit_guid(VmTemplateHandler.BlankVmTemplateId);
                disk.setvm_snapshot_id(snapshotId);
                disk.setactive(true);

                if (getParameters().getVm().getDiskMap() != null
                        && getParameters().getVm().getDiskMap().containsKey(disk.getId())) {
                    DiskImageBase diskImageBase =
                            (DiskImageBase) getParameters().getVm().getDiskMap().get(disk.getId());
                    disk.setvolume_format(diskImageBase.getvolume_format());
                    disk.setvolume_type(diskImageBase.getvolume_type());
                }
                diskGuidList.add(disk.getId());
                imageGuidList.add(disk.getImageId());
                if (getParameters().isImportAsNewEntity()) {
                    disk.setId(Guid.NewGuid());
                    disk.setImageId(Guid.NewGuid());
                    for (int i = 0; i < diskList.size() - 1; i++) {
                         diskList.get(i).setId(disk.getId());
                    }
                }
                disk.setcreation_date(new Date());
                BaseImagesCommand.saveImage(disk);
                ImagesHandler.setDiskAlias(disk, getVm());
                getBaseDiskDao().save(disk);
                saveDiskImageDynamic(disk);
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

                saveDiskImageDynamic(disk);
            }

            for (List<DiskImage> diskList : images.values()) {
                DiskImage disk = diskList.get(diskList.size() - 1);
                snapshotId = disk.getvm_snapshot_id().getValue();
                disk.setactive(true);
                getImageDao().update(disk.getImage());
                getBaseDiskDao().save(disk);
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
     * Generates and saves a {@link DiskImageDynamic} for the given {@link #disk}.
     * @param disk The imported disk
     **/
    private void saveDiskImageDynamic(DiskImage disk) {
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(disk.getImageId());
        diskDynamic.setactual_size(disk.getactual_size());
        getDiskImageDynamicDAO().save(diskDynamic);
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

    // the last image in each list is the leaf
    public static Map<Guid, List<DiskImage>> getImagesLeaf(List<DiskImage> images) {
        Map<Guid, List<DiskImage>> retVal = new HashMap<Guid, List<DiskImage>>();
        for (DiskImage image : images) {
            MultiValueMapUtils.addToMap(image.getId(), image, retVal);
        }

        for (Guid key : retVal.keySet()) {
            sortImageList(retVal.get(key));
        }
        return retVal;
    }

    private static void sortImageList(List<DiskImage> images) {
        List<DiskImage> hold = new ArrayList<DiskImage>();
        DiskImage curr = null;

        // find the first image
        for (int i = 0; i < images.size(); i++) {
            int pos = getFirstImage(images, images.get(i));
            if (pos == -1) {
                curr = images.get(i);
                hold.add(images.get(i));
                images.remove(images.get(i));
                break;
            }
        }

        while (images.size() > 0) {
            int pos = getNextImage(images, curr);
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
    private static int getFirstImage(List<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (curr.getParentId().equals(images.get(i).getImageId())) {
                return i;
            }
        }
        return -1;
    }

    // function return the index of image that is it's chiled
    private static int getNextImage(List<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getParentId().equals(curr.getImageId())) {
                return i;
            }
        }
        return -1;
    }

    protected void addVmStatic() {

        logImportEvents();
        getVm().getStaticData().setId(getVmId());
        getVm().getStaticData().setcreation_date(new Date());
        getVm().getStaticData().setvds_group_id(getParameters().getVdsGroupId());
        getVm().getStaticData().setMinAllocatedMem(computeMinAllocatedMem());
        getVm().getStaticData().setQuotaId(getParameters().getQuotaId());
        if (getParameters().getCopyCollapse()) {
            getVm().setVmtGuid(VmTemplateHandler.BlankVmTemplateId);
        }
        getVmStaticDAO().save(getVm().getStaticData());
        getCompensationContext().snapshotNewEntity(getVm().getStaticData());
    }

    private int computeMinAllocatedMem() {
        int vmMem = getVm().getMemSizeMb();
        int minAllocatedMem = vmMem;
        if (getVm().getMinAllocatedMem() > 0) {
            minAllocatedMem = getVm().getMinAllocatedMem();
        } else {
            // first get cluster memory over commit value
            VDSGroup vdsGroup = getVdsGroupDAO().get(getVm().getVdsGroupId());
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

    private static void logField(VmStatic vmStaticFromOvf, String fieldName, String fieldValue) {
        String vmName = vmStaticFromOvf.getvm_name();
        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("FieldName", fieldName);
        logable.AddCustomValue("VmName", vmName);
        logable.AddCustomValue("FieldValue", fieldValue);
        AuditLogDirector.log(logable, AuditLogType.VM_IMPORT_INFO);
    }

    protected void addVmInterfaces() {
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager();
        List<String> invalidNetworkNames = new ArrayList<String>();
        List<String> invalidIfaceNames = new ArrayList<String>();
        Map<String, Network> networksInVdsByName =
                Entities.entitiesByName(getNetworkDAO().getAllForCluster(getVm().getVdsGroupId()));

        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            initInterface(iface);
            if (!vmInterfaceManager.isValidVmNetwork(iface, networksInVdsByName)) {
                invalidNetworkNames.add(iface.getNetworkName());
                invalidIfaceNames.add(iface.getName());
                iface.setNetworkName(StringUtils.EMPTY);
            }

            vmInterfaceManager.add(iface, getCompensationContext(), getParameters().isImportAsNewEntity());
            macsAdded.add(iface.getMacAddress());
        }

        auditInvalidInterfaces(invalidNetworkNames, invalidIfaceNames);
    }

    private void initInterface(VmNetworkInterface iface) {
        if (iface.getId() == null) {
            iface.setId(Guid.NewGuid());
        }
        fillMacAddressIfMissing(iface);
        iface.setVmTemplateId(null);
        iface.setVmId(getVmId());
        iface.setVmName(getVm().getVmName());
    }

    private void addVmDynamic() {
        VmDynamic tempVar = new VmDynamic();
        tempVar.setId(getVmId());
        tempVar.setstatus(VMStatus.ImageLocked);
        tempVar.setvm_host("");
        tempVar.setvm_ip("");
        tempVar.setapp_list(getParameters().getVm().getDynamicData().getapp_list());
        getVmDynamicDAO().save(tempVar);
        getCompensationContext().snapshotNewEntity(tempVar);
    }

    private void addVmStatistics() {
        VmStatistics stats = new VmStatistics();
        stats.setId(getVmId());
        getVmStatisticsDAO().save(stats);
        getCompensationContext().snapshotNewEntity(stats);
        getCompensationContext().stateChanged();
    }

    @Override
    protected void endSuccessfully() {
        endImportCommand();
    }

    @Override
    protected void endWithFailure() {
        setVm(null); // Going to try and refresh the VM by re-loading
        // it form DB
        VM vmFromParams = getParameters().getVm();
        if (getVm() != null) {
            VmHandler.UnLockVm(getVm());
            for (DiskImage disk : imageList) {
                getDiskImageDynamicDAO().remove(disk.getImageId());
                getImageDao().remove(disk.getImageId());

                List<DiskImage> imagesForDisk = getDiskImageDao().getAllSnapshotsForImageGroup(disk.getId());
                if (imagesForDisk == null || imagesForDisk.isEmpty()) {
                    getBaseDiskDao().remove(disk.getId());
                }
            }
            removeVmNetworkInterfaces();
            new SnapshotsManager().removeSnapshots(getVm().getId());
            getVmDynamicDAO().remove(getVmId());
            getVmStatisticsDAO().remove(getVmId());
            new SnapshotsManager().removeSnapshots(getVmId());
            getVmStaticDAO().remove(getVmId());
            setSucceeded(true);
        } else {
            setVm(vmFromParams); // Setting VM from params, for logging purposes
            // No point in trying to end action again, as the imported VM does not exist in the DB.
            getReturnValue().setEndActionTryAgain(false);
        }
    }

    protected void removeVmNetworkInterfaces() {
        new VmInterfaceManager().removeAll(getVmId());
    }

    protected void endImportCommand() {
        endActionOnAllImageGroups();
        endVmRelatedOps();
        setSucceeded(true);
    }

    private void endVmRelatedOps() {
        setVm(null);
        if (getVm() != null) {
            VmHandler.UnLockVm(getVm());

            updateVmInSpm();
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("ImportVmCommand::EndImportCommand: Vm is null - not performing full EndAction");
        }
    }

    protected boolean updateVmInSpm() {
        return VmCommand.updateVmInSpm(getVm().getStoragePoolId(),
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

    @Override
    protected List<Class<?>> getValidationGroups() {
        if (getParameters().isImportAsNewEntity()) {
            return addValidationGroup(ImportClonedEntity.class);
        }
        return addValidationGroup(ImportEntity.class);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        // special permission is needed to use custom properties
        if (getVm() != null && !StringUtils.isEmpty(getVm().getCustomProperties())) {
            permissionList.add(new PermissionSubject(getVm().getVdsGroupId(),
                    VdcObjectType.VdsGroups,
                    ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
        }
        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
            jobProperties.put(VdcObjectType.VdsGroups.name().toLowerCase(), getVdsGroupName());
        }
        return jobProperties;
    }

    @Override
    protected AuditLogType getAuditLogTypeForInvalidInterfaces() {
        return AuditLogType.IMPORTEXPORT_IMPORT_VM_INVALID_INTERFACES;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        for (Disk disk : getParameters().getVm().getDiskMap().values()) {
            //TODO: handle import more than once;
            if(disk instanceof DiskImage){
                DiskImage diskImage = (DiskImage)disk;
                list.add(new QuotaStorageConsumptionParameter(
                        diskImage.getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.CONSUME,
                        imageToDestinationDomainMap.get(diskImage.getId()),
                        (double)diskImage.getSizeInGigabytes()));
            }
        }
        return list;
    }
}
