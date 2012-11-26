package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.MultiValueMapUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmTemplateCommand<T extends AddVmTemplateParameters> extends VmTemplateCommand<T>
        implements QuotaStorageDependent {

    private final List<DiskImage> mImages = new ArrayList<DiskImage>();
    private List<PermissionSubject> permissionCheckSubject;
    protected Map<Guid, DiskImage> diskInfoDestinationMap;

    /**
     * A list of the new disk images which were saved for the Template.
     */
    private final List<DiskImage> newDiskImages = new ArrayList<DiskImage>();

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmTemplateCommand(T parameters) {
        super(parameters);
        super.setVmTemplateName(parameters.getName());
        VmStatic parameterMasterVm = parameters.getMasterVm();
        if (parameterMasterVm != null) {
            super.setVmId(parameterMasterVm.getId());
            setVdsGroupId(parameterMasterVm.getvds_group_id());
        }
        if (getVm() != null) {
            VmHandler.updateDisksFromDb(getVm());
            setStoragePoolId(getVm().getStoragePoolId());
        }
        diskInfoDestinationMap = parameters.getDiskInfoDestinationMap();
        if (diskInfoDestinationMap == null) {
            diskInfoDestinationMap = new HashMap<Guid, DiskImage>();
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE : AuditLogType.USER_FAILED_ADD_VM_TEMPLATE;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_SUCCESS
                    : AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_FAILURE;
        }
    }

    @Override
    protected void executeCommand() {
        // get vm status from db to check its really down before locking
        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(getVmId());
        if (vmDynamic.getstatus() != VMStatus.Down) {
            throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
        }

        VmHandler.LockVm(vmDynamic, getCompensationContext());
        setActionReturnValue(Guid.Empty);
        setVmTemplateId(Guid.NewGuid());
        getParameters().setVmTemplateId(getVmTemplateId());
        getParameters().setEntityId(getVmTemplateId());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                AddVmTemplateToDb();
                getCompensationContext().stateChanged();
                return null;
            }
        });
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                addPermission();
                AddVmTemplateImages();
                List<VmNetworkInterface> vmInterfaces = addVmInterfaces();
                VmDeviceUtils.copyVmDevices(getVmId(), getVmTemplateId(), newDiskImages, vmInterfaces);
                setSucceeded(true);
                return null;
            }
        });

        // means that there are no asynchronous tasks to execute and that we can
        // end the command synchronously
        boolean pendingAsyncTasks = !getReturnValue().getTaskIdList().isEmpty();
        if (!pendingAsyncTasks) {
            endSuccessfullySynchronous();
        }
    }

    @Override
    protected boolean canDoAction() {
        if (getVdsGroup() == null || !getVm().getStoragePoolId().equals(getVdsGroup().getStoragePoolId())) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }
        for (DiskImage diskImage : getVm().getDiskList()) {
            mImages.add(diskImage);
        }
        if (!VmHandler.isMemorySizeLegal(getParameters().getMasterVm().getos(),
                getParameters().getMasterVm().getmem_size_mb(),
                getReturnValue().getCanDoActionMessages(), getVdsGroup().getcompatibility_version().toString())) {
            return false;
        }
        if (!IsVmPriorityValueLegal(getParameters().getMasterVm().getpriority(), getReturnValue()
                .getCanDoActionMessages())) {
            return false;
        }

        if (!validate(new SnapshotsValidator().vmNotDuringSnapshot(getVmId()))) {
            return false;
        }

        if (getVm().getStatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM.toString());
            return false;
        }

        if (isVmTemlateWithSameNameExist(getVmTemplateName())) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_CREATE_DUPLICATE_NAME);
            return false;
        }

        // Check that the USB policy is legal
        if (!VmHandler.isUsbPolicyLegal(getParameters().getVm().getUsbPolicy(), getParameters().getVm().getOs(), getVdsGroup(), getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        Map<Guid, List<DiskImage>> sourceImageDomainsImageMap = new HashMap<Guid, List<DiskImage>>();
        for (DiskImage image : mImages) {
            MultiValueMapUtils.addToMap(image.getstorage_ids().get(0), image, sourceImageDomainsImageMap);
            if (!diskInfoDestinationMap.containsKey(image.getId())) {
                Guid destStorageId =
                        getParameters().getDestinationStorageDomainId() != null ? getParameters().getDestinationStorageDomainId()
                                : image.getstorage_ids().get(0);
                ArrayList<Guid> storageIds = new ArrayList<Guid>();
                storageIds.add(destStorageId);
                image.setstorage_ids(storageIds);
                diskInfoDestinationMap.put(image.getId(), image);
            }
        }

        for (Guid srcStorageDomainId : sourceImageDomainsImageMap.keySet()) {
            boolean checkIsValid = true;
            if (!ImagesHandler.PerformImagesChecks(getVm(),
                    getReturnValue().getCanDoActionMessages(),
                    getVm().getStoragePoolId(),
                    srcStorageDomainId,
                    false,
                    true,
                    true,
                    true,
                    true,
                    false,
                    true, checkIsValid, sourceImageDomainsImageMap.get(srcStorageDomainId))) {
                return false;
            }
            checkIsValid = false;
        }

        Map<Guid, storage_domains> storageDomains = new HashMap<Guid, storage_domains>();
        Set<Guid> destImageDomains = getStorageGuidSet();
        destImageDomains.removeAll(sourceImageDomainsImageMap.keySet());
        for (Guid destImageDomain : destImageDomains) {
            storage_domains storage = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
                    destImageDomain, getVm().getStoragePoolId());
            if (storage == null) {
                // if storage is null then we need to check if it doesn't exist or
                // domain is not in the same storage pool as the vm
                if (DbFacade.getInstance().getStorageDomainStaticDao().get(destImageDomain) == null) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST.toString());
                } else {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_IN_STORAGE_POOL);
                }
                return false;
            }
            if (storage.getstatus() == null || storage.getstatus() != StorageDomainStatus.Active) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL.toString());
                return false;
            }

            if (storage.getstorage_domain_type() == StorageDomainType.ImportExport
                    || storage.getstorage_domain_type() == StorageDomainType.ISO) {

                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                return false;
            }
            storageDomains.put(destImageDomain, storage);
        }
        // update vm snapshots for storage free space check
        ImagesHandler.fillImagesBySnapshots(getVm());

        Map<storage_domains, Integer> domainMap =
                StorageDomainValidator.getSpaceRequirementsForStorageDomains(
                        ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), true, false),
                        storageDomains,
                        diskInfoDestinationMap);
        for (Map.Entry<storage_domains, Integer> entry : domainMap.entrySet()) {
            if (!StorageDomainSpaceChecker.hasSpaceForRequest(entry.getKey(), entry.getValue())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
                return false;
            }
        }
        return AddVmCommand.CheckCpuSockets(getParameters().getMasterVm().getnum_of_sockets(),
                getParameters().getMasterVm().getcpu_per_socket(), getVdsGroup()
                        .getcompatibility_version().toString(), getReturnValue().getCanDoActionMessages());
    }

    private Set<Guid> getStorageGuidSet() {
        Set<Guid> destImageDomains = new HashSet<Guid>();
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            destImageDomains.add(diskImage.getstorage_ids().get(0));
        }
        return destImageDomains;
    }

    protected void AddVmTemplateToDb() {
        // TODO: add timezone handling
        setVmTemplate(new VmTemplate(0, new Date(), getParameters().getDescription(),
                getParameters().getMasterVm().getmem_size_mb(), getVmTemplateName(),
                getParameters().getMasterVm().getnum_of_sockets(), getParameters().getMasterVm()
                        .getcpu_per_socket(), getParameters().getMasterVm().getos(),
                getParameters().getMasterVm().getvds_group_id(), getVmTemplateId(),
                getParameters().getMasterVm().getdomain(), getParameters().getMasterVm()
                        .getnum_of_monitors(), (VmTemplateStatus.Locked.getValue()), (getParameters().getMasterVm()
                        .getusb_policy().getValue()), getParameters().getMasterVm().gettime_zone(),
                getParameters().getMasterVm().getis_auto_suspend(), getParameters().getMasterVm()
                        .getnice_level(), getParameters().getMasterVm().getfail_back(),
                getParameters().getMasterVm().getdefault_boot_sequence(), getParameters()
                        .getMasterVm().getvm_type(),
                getParameters().getMasterVm().isSmartcardEnabled(),
                getParameters().getMasterVm().isDeleteProtected()));
        getVmTemplate().setauto_startup(getParameters().getMasterVm().getauto_startup());
        getVmTemplate().setpriority(getParameters().getMasterVm().getpriority());
        getVmTemplate().setdefault_display_type(getParameters().getMasterVm().getdefault_display_type());
        getVmTemplate().setinitrd_url(getParameters().getMasterVm().getinitrd_url());
        getVmTemplate().setkernel_url(getParameters().getMasterVm().getkernel_url());
        getVmTemplate().setkernel_params(getParameters().getMasterVm().getkernel_params());
        getVmTemplate().setis_stateless(getParameters().getMasterVm().getis_stateless());
        getVmTemplate().setQuotaId(getParameters().getMasterVm().getQuotaId());
        getVmTemplate().setdedicated_vm_for_vds(getParameters().getMasterVm().getdedicated_vm_for_vds());
        getVmTemplate().setMigrationSupport(getParameters().getMasterVm().getMigrationSupport());
        DbFacade.getInstance().getVmTemplateDao().save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());
        setActionReturnValue(getVmTemplate().getId());
    }

    protected List<VmNetworkInterface> addVmInterfaces() {
        List<VmNetworkInterface> templateInterfaces = new ArrayList<VmNetworkInterface>();
        List<VmNetworkInterface> interfaces = DbFacade
                .getInstance()
                .getVmNetworkInterfaceDao()
                .getAllForVm(
                        getParameters().getMasterVm().getId());
        for (VmNetworkInterface iface : interfaces) {
            VmNetworkInterface iDynamic = new VmNetworkInterface();
            iDynamic.setId(Guid.NewGuid());
            iDynamic.setVmTemplateId(getVmTemplateId());
            iDynamic.setName(iface.getName());
            iDynamic.setNetworkName(iface.getNetworkName());
            iDynamic.setSpeed(VmInterfaceType.forValue(iface.getType()).getSpeed());
            iDynamic.setType(iface.getType());
            templateInterfaces.add(iDynamic);
            DbFacade.getInstance().getVmNetworkInterfaceDao().save(iDynamic);
        }
        return templateInterfaces;
    }

    protected void AddVmTemplateImages() {
        Guid vmSnapshotId = Guid.NewGuid();

        for (DiskImage diskImage : mImages) {
            CreateImageTemplateParameters createParams = new CreateImageTemplateParameters(diskImage.getImageId(),
                    getVmTemplateId(), getVmTemplateName(), getVmId());
            createParams.setStorageDomainId(diskImage.getstorage_ids().get(0));
            createParams.setVmSnapshotId(vmSnapshotId);
            createParams.setEntityId(getParameters().getEntityId());
            createParams.setDestinationStorageDomainId(diskInfoDestinationMap.get(diskImage.getId())
                    .getstorage_ids()
                    .get(0));
            createParams.setDiskAlias(diskInfoDestinationMap.get(diskImage.getId()).getDiskAlias());
            createParams.setParentParameters(getParameters());
            if (getParameters().getDiskInfoDestinationMap() != null
                    && getParameters().getDiskInfoDestinationMap().get(diskImage.getId()) != null) {
            createParams.setQuotaId(getParameters().getDiskInfoDestinationMap().get(diskImage.getId()).getQuotaId() != null
                    ? getParameters().getDiskInfoDestinationMap().get(diskImage.getId()).getQuotaId() : null);
            }
            getParameters().getImagesParameters().add(createParams);
            // The return value of this action is the 'copyImage' task GUID:
            VdcReturnValueBase retValue = Backend.getInstance().runInternalAction(
                    VdcActionType.CreateImageTemplate,
                    createParams,
                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

            if (!retValue.getSucceeded()) {
                throw new VdcBLLException(retValue.getFault().getError(), retValue.getFault().getMessage());
            }

            getReturnValue().getTaskIdList().addAll(retValue.getInternalTaskIdList());
            newDiskImages.add((DiskImage) retValue.getActionReturnValue());
        }
    }


    private Guid getVmIdFromImageParameters(){
        return ((CreateImageTemplateParameters)getParameters().getImagesParameters().get(0)).getVmId();
    }

    @Override
    protected void endSuccessfully() {
        setVmTemplateId(getParameters().getVmTemplateId());
        setVmId(getVmIdFromImageParameters());
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Backend.getInstance().EndAction(VdcActionType.CreateImageTemplate, p);
        }
        if (reloadVmTemplateFromDB() != null) {
            endDefaultOperations();
        }
        setSucceeded(true);
    }

    private void endSuccessfullySynchronous() {
        if (reloadVmTemplateFromDB() != null) {
            endDefaultOperations();
        }
        setSucceeded(true);
    }

    private void endDefaultOperations() {
        endTemplateRelatedOperations();
        endUnlockOps();
    }

    private void endTemplateRelatedOperations() {
        VmTemplate template = this.reloadVmTemplateFromDB();
        UpdateTemplateInSpm(template.getstorage_pool_id().getValue(), new java.util.ArrayList<VmTemplate>(
                java.util.Arrays.asList(new VmTemplate[] { template })));
    }

    private void endUnlockOps() {
        VmHandler.UnLockVm(getVm());
        VmTemplateHandler.UnLockVmTemplate(getVmTemplateId());
    }

    private VmTemplate reloadVmTemplateFromDB() {
        // set it to null to reload the template from the db
        setVmTemplate(null);
        return getVmTemplate();
    }

    @Override
    protected void endWithFailure() {
        // We evaluate 'VmTemplate' so it won't be null in the last 'if'
        // statement.
        // (a template without images doesn't exist in the 'vm_template_view').
        setVmTemplateId(getParameters().getVmTemplateId());
        setVmId(getVmIdFromImageParameters());

        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Backend.getInstance().EndAction(VdcActionType.CreateImageTemplate, p);
        }

        // if template exist in db remove it
        if (getVmTemplate() != null) {
            RemoveTemplateInSpm(getVmTemplate().getstorage_pool_id().getValue(), getVmTemplateId());
            DbFacade.getInstance().getVmTemplateDao().remove(getVmTemplateId());
            RemoveNetwork();
        }

        if (!getVmId().equals(Guid.Empty) && getVm() != null) {
            VmHandler.UnLockVm(getVm());
        }

        setSucceeded(true);
    }

    /**
     * in case of non-existing cluster the backend query will return a null
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permissionCheckSubject == null) {
            permissionCheckSubject = new ArrayList<PermissionSubject>();
            Guid storagePoolId = getVdsGroup() == null || getVdsGroup().getStoragePoolId() == null ? null
                    : getVdsGroup().getStoragePoolId().getValue();
            permissionCheckSubject.add(new PermissionSubject(storagePoolId,
                    VdcObjectType.StoragePool,
                    getActionType().getActionGroup()));
        }
        return permissionCheckSubject;
    }

    private void addPermission() {
        addPermissionForTempalte(getCurrentUser().getUserId(), PredefinedRoles.TEMPLATE_OWNER);
        // if the template is for public use, set EVERYONE as a TEMPLATE_USER.
        if (getParameters().isPublicUse()) {
            addPermissionForTempalte(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID, PredefinedRoles.TEMPLATE_USER);
        }
    }

    private void addPermissionForTempalte(Guid userId, PredefinedRoles role) {
        permissions perms = new permissions();
        perms.setad_element_id(userId);
        perms.setObjectType(VdcObjectType.VmTemplate);
        perms.setObjectId(getParameters().getVmTemplateId());
        perms.setrole_id(role.getId());
        MultiLevelAdministrationHandler.addPermission(perms);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        for (DiskImage disk : diskInfoDestinationMap.values()) {
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    null,
                    QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
                    disk.getstorage_ids().get(0),
                    (double)disk.getSizeInGigabytes()));
        }
        return list;
    }
}
