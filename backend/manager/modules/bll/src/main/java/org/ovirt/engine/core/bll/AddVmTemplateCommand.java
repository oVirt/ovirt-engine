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
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
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
import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmTemplateCommand<T extends AddVmTemplateParameters> extends VmTemplateCommand<T> {

    private final List<DiskImage> mImages = new ArrayList<DiskImage>();
    private Map<Pair<Guid, Guid>, Double> quotaForStorageConsumption;
    private List<PermissionSubject> permissionCheckSubject;
    protected Map<Guid, Guid> imageToDestinationDomainMap;

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
            setQuotaId(parameterMasterVm.getQuotaId());
        }
        if (getVm() != null) {
            VmHandler.updateDisksFromDb(getVm());
            setStoragePoolId(getVm().getstorage_pool_id());
        }
        imageToDestinationDomainMap = parameters.getImageToDestinationDomainMap();
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
        VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDAO().get(getVmId());
        if (vmDynamic.getstatus() != VMStatus.Down) {
            throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
        }

        VmHandler.LockVm(vmDynamic, getCompensationContext());
        setActionReturnValue(Guid.Empty);
        setVmTemplateId(Guid.NewGuid());
        getParameters().setVmTemplateID(getVmTemplateId());
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
                AddVmInterfaces();
                VmDeviceUtils.copyVmDevices(getVmId(), getVmTemplateId(), newDiskImages);
                setSucceeded(true);
                return null;
            }
        });

    }

    @Override
    protected boolean validateQuota() {
        // Set default quota id if storage pool enforcement is disabled.
        getParameters().setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(getQuotaId(),
                getStoragePool()));
        for (DiskImage diskImage : mImages) {
            diskImage.setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(getQuotaId(),
                    getStoragePool()));
        }

        if (!isInternalExecution()) {
            return QuotaManager.validateMultiStorageQuota(getStoragePool().getQuotaEnforcementType(),
                    getQuotaConsumeMap(),
                    getCommandId(),
                    getReturnValue().getCanDoActionMessages());
        }
        return true;
    }

    private Map<Pair<Guid, Guid>, Double> getQuotaConsumeMap() {
        if (quotaForStorageConsumption == null) {
            quotaForStorageConsumption = QuotaHelper.getInstance().getQuotaConsumeMap(mImages);
        }
        return quotaForStorageConsumption;
    }

    @Override
    protected void removeQuotaCommandLeftOver() {
        if (!isInternalExecution()) {
            QuotaManager.removeMultiStorageDeltaQuotaCommand(getQuotaConsumeMap(),
                    getStoragePool().getQuotaEnforcementType(),
                    getCommandId());
        }
    }

    @Override
    protected boolean canDoAction() {
        if (getVdsGroup() == null || !getVm().getstorage_pool_id().equals(getVdsGroup().getstorage_pool_id())) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }
        for (DiskImage diskImage : getVm().getDiskList()) {
            mImages.add(diskImage);
        }
        if (mImages.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_NO_DISKS);
            return false;
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

        if (getVm().getstatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM.toString());
            return false;
        }

        if (isVmTemlateWithSameNameExist(getVmTemplateName())) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_CREATE_DUPLICATE_NAME);
            return false;
        }

        Map<Guid, List<DiskImage>> sourceImageDomainsImageMap = new HashMap<Guid, List<DiskImage>>();
        if (imageToDestinationDomainMap == null) {
            imageToDestinationDomainMap = new HashMap<Guid, Guid>();
        }
        for (DiskImage image : mImages) {
            MultiValueMapUtils.addToMap(image.getstorage_ids().get(0), image, sourceImageDomainsImageMap);
            if (!imageToDestinationDomainMap.containsKey(image.getId())) {
                Guid destImageId =
                        getParameters().getDestinationStorageDomainId() != null ? getParameters().getDestinationStorageDomainId()
                                : image.getstorage_ids().get(0);
                imageToDestinationDomainMap.put(image.getId(), destImageId);
            }
        }

        for (Guid srcStorageDomainId : sourceImageDomainsImageMap.keySet()) {
            boolean checkIsValid = true;
            if (!ImagesHandler.PerformImagesChecks(getVm(),
                    getReturnValue().getCanDoActionMessages(),
                    getVm().getstorage_pool_id(),
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
        Set<Guid> destImageDomains = new HashSet<Guid>(imageToDestinationDomainMap.values());
        destImageDomains.removeAll(sourceImageDomainsImageMap.keySet());
        for (Guid destImageDomain : destImageDomains) {
            storage_domains storage = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(
                    destImageDomain, getVm().getstorage_pool_id());
            if (storage == null) {
                // if storage is null then we need to check if it doesn't exist or
                // domain is not in the same storage pool as the vm
                if (DbFacade.getInstance().getStorageDomainStaticDAO().get(destImageDomain) == null) {
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
        for (DiskImage diskImage : getVm().getDiskMap().values()) {
            diskImage.getSnapshots().addAll(
                    ImagesHandler.getAllImageSnapshots(diskImage.getId(),
                            diskImage.getit_guid()));
        }

        Map<storage_domains, Integer> domainMap =
                StorageDomainValidator.getSpaceRequirementsForStorageDomains(
                        getVmTemplate().getDiskImageMap().values(),
                        storageDomains,
                        imageToDestinationDomainMap);
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
                        .getMasterVm().getvm_type(), getParameters().getMasterVm().gethypervisor_type(),
                getParameters().getMasterVm().getoperation_mode()));
        getVmTemplate().setauto_startup(getParameters().getMasterVm().getauto_startup());
        getVmTemplate().setpriority(getParameters().getMasterVm().getpriority());
        getVmTemplate().setdefault_display_type(getParameters().getMasterVm().getdefault_display_type());
        getVmTemplate().setinitrd_url(getParameters().getMasterVm().getinitrd_url());
        getVmTemplate().setkernel_url(getParameters().getMasterVm().getkernel_url());
        getVmTemplate().setkernel_params(getParameters().getMasterVm().getkernel_params());
        getVmTemplate().setis_stateless(getParameters().getMasterVm().getis_stateless());
        getVmTemplate().setQuotaId(getParameters().getQuotaId());
        DbFacade.getInstance().getVmTemplateDAO().save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());
        setActionReturnValue(getVmTemplate().getId());
    }

    protected void AddVmInterfaces() {
        List<VmNetworkInterface> interfaces = DbFacade
                .getInstance()
                .getVmNetworkInterfaceDAO()
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

            DbFacade.getInstance().getVmNetworkInterfaceDAO().save(iDynamic);
        }
    }

    protected void AddVmTemplateImages() {
        Guid vmSnapshotId = Guid.NewGuid();

        for (DiskImage diskImage : mImages) {
            CreateImageTemplateParameters createParams = new CreateImageTemplateParameters(diskImage.getId(),
                    getVmTemplateId(), getVmTemplateName(), getVmId());
            createParams.setStorageDomainId(diskImage.getstorage_ids().get(0));
            createParams.setVmSnapshotId(vmSnapshotId);
            createParams.setEntityId(getParameters().getEntityId());
            createParams.setDestinationStorageDomainId(imageToDestinationDomainMap.get(diskImage.getId()));
            createParams.setParentParemeters(getParameters());
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

    @Override
    protected void EndSuccessfully() {
        setVmTemplateId(getParameters().getVmTemplateId());
        // set it to null to reload from db
        setVmTemplate(null);

        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Backend.getInstance().EndAction(VdcActionType.CreateImageTemplate, p);
        }

        if (getVmTemplate() != null) {
            UpdateTemplateInSpm(getVmTemplate().getstorage_pool_id().getValue(), new java.util.ArrayList<VmTemplate>(
                    java.util.Arrays.asList(new VmTemplate[] { getVmTemplate() })));
            if (getVm() != null) {
                VmHandler.UnLockVm(getVm().getId());
            } else {
                log.warn("AddVmTemplateCommand::EndSuccessfully: Vm is null, cannot unlock Vm");
            }
            VmTemplateHandler.UnLockVmTemplate(getVmTemplateId());
        } else {
            setCommandShouldBeLogged(false);
            log.warn("AddVmTemplateCommand::EndSuccessfully: VmTemplate is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    @Override
    protected void EndWithFailure() {
        // We evaluate 'VmTemplate' so it won't be null in the last 'if'
        // statement.
        // (a template without images doesn't exist in the 'vm_template_view').
        setVmTemplateId(getParameters().getVmTemplateId());

        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Backend.getInstance().EndAction(VdcActionType.CreateImageTemplate, p);
        }

        // if template exist in db remove it
        if (getVmTemplate() != null) {
            RemoveTemplateInSpm(getVmTemplate().getstorage_pool_id().getValue(), getVmTemplateId());
            DbFacade.getInstance().getVmTemplateDAO().remove(getVmTemplateId());
            RemoveNetwork();
        }

        if (!getVmId().equals(Guid.Empty) && getVm() != null) {
            VmHandler.UnLockVm(getVmId());
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
            Guid storagePoolId = getVdsGroup() == null || getVdsGroup().getstorage_pool_id() == null ? null
                    : getVdsGroup().getstorage_pool_id().getValue();
            permissionCheckSubject.add(new PermissionSubject(storagePoolId,
                    VdcObjectType.StoragePool,
                    getActionType().getActionGroup()));
            permissionCheckSubject = QuotaHelper.getInstance().addQuotaPermissionSubject(permissionCheckSubject,
                    getStoragePool(),
                    getQuotaId());
            permissionCheckSubject = setPermissionListForDiskImage(permissionCheckSubject);
        }
        return permissionCheckSubject;
    }

    private List<PermissionSubject> setPermissionListForDiskImage(List<PermissionSubject> list) {
        Map<Guid, Object> quotaMap = new HashMap<Guid, Object>();
        // Distinct the quotas for images.
        for (DiskImage diskImage : mImages) {
            if (quotaMap.get(diskImage.getQuotaId()) == null) {
                quotaMap.put(diskImage.getQuotaId(), diskImage.getQuotaId());
                list = QuotaHelper.getInstance().addQuotaPermissionSubject(list,
                        getStoragePool(),
                        diskImage.getQuotaId());
            }
        }
        return list;
    }

    private void addPermission() {
        // if the template is for public use, set EVERYONE as a TEMPLATE_USER.
        if (getParameters().isPublicUse()) {
            permissions perms = new permissions();
            perms.setad_element_id(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID);
            perms.setObjectType(VdcObjectType.VmTemplate);
            perms.setObjectId(getParameters().getVmTemplateId());
            perms.setrole_id(PredefinedRoles.TEMPLATE_USER.getId());
            MultiLevelAdministrationHandler.addPermission(perms);
        }
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
}
