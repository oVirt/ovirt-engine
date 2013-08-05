package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmTemplateCommand<T extends AddVmTemplateParameters> extends VmTemplateCommand<T>
        implements QuotaStorageDependent, QuotaVdsDependent {

    private final List<DiskImage> mImages = new ArrayList<DiskImage>();
    private List<PermissionSubject> permissionCheckSubject;
    protected Map<Guid, DiskImage> diskInfoDestinationMap;
    protected Map<Guid, List<DiskImage>> sourceImageDomainsImageMap;
    private boolean isVmInDb;

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
            setVdsGroupId(parameterMasterVm.getVdsGroupId());

            // API backward compatibility
            if (parameters.isSoundDeviceEnabled() == null) {
                parameters.setSoundDeviceEnabled(parameterMasterVm.getVmType() == VmType.Desktop);
            }

            if (getParameters().isConsoleEnabled() == null) {
                parameters.setConsoleEnabled(false);
            }
        }
        if (getVm() != null) {
            updateVmDisks();
            setStoragePoolId(getVm().getStoragePoolId());
            isVmInDb = true;
        } else if (getVdsGroup() != null) {
            VM vm = new VM(parameterMasterVm, new VmDynamic(), null);
            vm.setDisplayType(parameterMasterVm.getDefaultDisplayType());
            vm.setVdsGroupCompatibilityVersion(getVdsGroup().getcompatibility_version());
            setVm(vm);
            setStoragePoolId(getVdsGroup().getStoragePoolId());
        }
        updateDiskInfoDestinationMap();
    }

    protected void updateDiskInfoDestinationMap() {
        diskInfoDestinationMap = getParameters().getDiskInfoDestinationMap();
        if (diskInfoDestinationMap == null) {
            diskInfoDestinationMap = new HashMap<Guid, DiskImage>();
        }
        sourceImageDomainsImageMap = new HashMap<Guid, List<DiskImage>>();
        for (DiskImage image : mImages) {
            MultiValueMapUtils.addToMap(image.getStorageIds().get(0), image, sourceImageDomainsImageMap);
            if (!diskInfoDestinationMap.containsKey(image.getId())) {
                Guid destStorageId =
                        getParameters().getDestinationStorageDomainId() != null ? getParameters().getDestinationStorageDomainId()
                                : image.getStorageIds().get(0);
                ArrayList<Guid> storageIds = new ArrayList<Guid>();
                storageIds.add(destStorageId);
                image.setStorageIds(storageIds);
                diskInfoDestinationMap.put(image.getId(), image);
            }
        }
    }

    protected void updateVmDisks() {
        VmHandler.updateDisksFromDb(getVm());
        for (DiskImage diskImage : getVm().getDiskList()) {
            mImages.add(diskImage);
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
    protected void buildChildCommandInfos() {
        Guid vmSnapshotId = Guid.newGuid();
        for (DiskImage diskImage : mImages) {
            addChildCommandInfo(diskImage.getImageId(), VdcActionType.CreateImageTemplate, buildChildCommandParameters(diskImage, vmSnapshotId));
        }
    }

    private CreateImageTemplateParameters buildChildCommandParameters(DiskImage diskImage, Guid vmSnapshotId) {
        CreateImageTemplateParameters createParams = new CreateImageTemplateParameters(diskImage.getImageId(),
                getVmTemplateId(), getVmTemplateName(), getVmId());
        createParams.setStorageDomainId(diskImage.getStorageIds().get(0));
        createParams.setVmSnapshotId(vmSnapshotId);
        createParams.setEntityInfo(getParameters().getEntityInfo());
        createParams.setDestinationStorageDomainId(diskInfoDestinationMap.get(diskImage.getId())
                .getStorageIds()
                .get(0));
        createParams.setDiskAlias(diskInfoDestinationMap.get(diskImage.getId()).getDiskAlias());
        createParams.setParentParameters(getParameters());
        createParams.setQuotaId(getQuotaIdForDisk(diskImage));
        return createParams;
    }

    @Override
    protected void executeCommand() {
        // get vm status from db to check its really down before locking
        // relevant only if template created from vm
        if (isVmInDb) {
            VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(getVmId());
            if (vmDynamic.getStatus() != VMStatus.Down) {
                throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
            }

            VmHandler.LockVm(vmDynamic, getCompensationContext());
        }
        setActionReturnValue(Guid.Empty);
        setVmTemplateId(Guid.newGuid());
        getParameters().setVmTemplateId(getVmTemplateId());
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));

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
                if (isVmInDb) {
                    VmDeviceUtils.copyVmDevices(getVmId(), getVmTemplateId(), newDiskImages, vmInterfaces, getParameters().isSoundDeviceEnabled(), getParameters().isConsoleEnabled());
                } else {
                    // sending true for isVm in order to create basic devices needed
                    VmDeviceUtils.copyVmDevices(getVmId(),
                            getVmTemplateId(),
                            getVm(),
                            getVmTemplate(),
                            true,
                            Collections.<VmDevice> emptyList(),
                            newDiskImages,
                            vmInterfaces,
                            getParameters().isSoundDeviceEnabled(),
                            getParameters().isConsoleEnabled());
                }

                setSucceeded(true);
                return null;
            }
        });

        // means that there are no asynchronous tasks to execute and that we can
        // end the command synchronously
        boolean pendingAsyncTasks = !getReturnValue().getVdsmTaskIdList().isEmpty();
        if (!pendingAsyncTasks) {
            endSuccessfullySynchronous();
        }
    }

    @Override
    protected boolean canDoAction() {
        if (getVdsGroup() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }
        if (!VmHandler.isMemorySizeLegal(getParameters().getMasterVm().getOsId(),
                getParameters().getMasterVm().getMemSizeMb(),
                getReturnValue().getCanDoActionMessages(), getVdsGroup().getcompatibility_version())) {
            return false;
        }
        if (!IsVmPriorityValueLegal(getParameters().getMasterVm().getPriority(), getReturnValue()
                .getCanDoActionMessages())) {
            return false;
        }

        if (isVmInDb && getVm().getStatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM.toString());
            return false;
        }

        if (isVmTemlateWithSameNameExist(getVmTemplateName())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            return false;
        }

        // Check that the USB policy is legal
        if (!VmHandler.isUsbPolicyLegal(getParameters().getVm().getUsbPolicy(),
                getParameters().getVm().getOs(),
                getVdsGroup(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        if (getParameters().getVm().getSingleQxlPci() &&
                !VmHandler.isSingleQxlDeviceLegal(getParameters().getVm().getDefaultDisplayType(),
                        getParameters().getVm().getOs(),
                        getReturnValue().getCanDoActionMessages(),
                        getVdsGroup().getcompatibility_version())) {
            return false;
        }

        return imagesRelatedChecks() && AddVmCommand.CheckCpuSockets(getParameters().getMasterVm().getNumOfSockets(),
                getParameters().getMasterVm().getCpuPerSocket(), getVdsGroup()
                        .getcompatibility_version().toString(), getReturnValue().getCanDoActionMessages());
    }

    private boolean imagesRelatedChecks() {
        // images related checks
        if (!mImages.isEmpty()) {
            if (!getVm().getStoragePoolId().equals(getVdsGroup().getStoragePoolId())) {
                addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
                return false;
            }
            if (!validateVmNotDuringSnapshot()) {
                return false;
            }

            if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
                return false;
            }

            List<DiskImage> diskImagesToCheck = ImagesHandler.filterImageDisks(mImages, true, false);
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(diskImagesToCheck);
            if (!validate(diskImagesValidator.diskImagesNotIllegal()) ||
                    !validate(diskImagesValidator.diskImagesNotLocked())) {
                return false;
            }

            MultipleStorageDomainsValidator storageDomainsValidator =
                    new MultipleStorageDomainsValidator(getStoragePoolId(), sourceImageDomainsImageMap.keySet());
            if (!validate(storageDomainsValidator.allDomainsExistAndActive())) {
                return false;
            }

            Map<Guid, StorageDomain> storageDomains = new HashMap<Guid, StorageDomain>();
            Set<Guid> destImageDomains = getStorageGuidSet();
            destImageDomains.removeAll(sourceImageDomainsImageMap.keySet());
            for (Guid destImageDomain : destImageDomains) {
                StorageDomain storage = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
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
                if (storage.getStatus() == null || storage.getStatus() != StorageDomainStatus.Active) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL.toString());
                    return false;
                }

                if (storage.getStorageDomainType() == StorageDomainType.ImportExport
                        || storage.getStorageDomainType() == StorageDomainType.ISO) {

                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                    return false;
                }
                storageDomains.put(destImageDomain, storage);
            }
            // update vm snapshots for storage free space check
            ImagesHandler.fillImagesBySnapshots(getVm());

            Map<StorageDomain, Integer> domainMap =
                    StorageDomainValidator.getSpaceRequirementsForStorageDomains(
                            ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), true, false),
                            storageDomains,
                            diskInfoDestinationMap);

            for (Map.Entry<StorageDomain, Integer> entry : domainMap.entrySet()) {
                if (!doesStorageDomainhaveSpaceForRequest(entry.getKey(), entry.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean doesStorageDomainhaveSpaceForRequest(StorageDomain storageDomain, long spaceForRequest) {
        return validate(new StorageDomainValidator(storageDomain).isDomainHasSpaceForRequest(spaceForRequest));
    }

    protected boolean validateVmNotDuringSnapshot() {
        return validate(new SnapshotsValidator().vmNotDuringSnapshot(getVmId()));
    }

    private Set<Guid> getStorageGuidSet() {
        Set<Guid> destImageDomains = new HashSet<Guid>();
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            destImageDomains.add(diskImage.getStorageIds().get(0));
        }
        return destImageDomains;
    }

    protected void AddVmTemplateToDb() {
        // TODO: add timezone handling
        setVmTemplate(
                new VmTemplate(
                        0,
                        new Date(),
                        getParameters().getDescription(),
                        getParameters().getMasterVm().getMemSizeMb(), getVmTemplateName(),
                        getParameters().getMasterVm().getNumOfSockets(),
                        getParameters().getMasterVm().getCpuPerSocket(),
                        getParameters().getMasterVm().getOsId(),
                        getParameters().getMasterVm().getVdsGroupId(),
                        getVmTemplateId(),
                        getParameters().getMasterVm().getDomain(),
                        getParameters().getMasterVm().getNumOfMonitors(),
                        getParameters().getMasterVm().getSingleQxlPci(),
                        VmTemplateStatus.Locked.getValue(),
                        getParameters().getMasterVm().getUsbPolicy().getValue(),
                        getParameters().getMasterVm().getTimeZone(),
                        getParameters().getMasterVm().getNiceLevel(),
                        getParameters().getMasterVm().getCpuShares(),
                        getParameters().getMasterVm().isFailBack(),
                        getParameters().getMasterVm().getDefaultBootSequence(),
                        getParameters().getMasterVm().getVmType(),
                        getParameters().getMasterVm().isSmartcardEnabled(),
                        getParameters().getMasterVm().isDeleteProtected(),
                        getParameters().getMasterVm().getTunnelMigration(),
                        getParameters().getMasterVm().getVncKeyboardLayout(),
                        getParameters().getMasterVm().getMinAllocatedMem(),
                        getParameters().getMasterVm().isStateless(),
                        getParameters().getMasterVm().isRunAndPause(),
                        getUserId()));
        getVmTemplate().setAutoStartup(getParameters().getMasterVm().isAutoStartup());
        getVmTemplate().setPriority(getParameters().getMasterVm().getPriority());
        getVmTemplate().setDefaultDisplayType(getParameters().getMasterVm().getDefaultDisplayType());
        getVmTemplate().setInitrdUrl(getParameters().getMasterVm().getInitrdUrl());
        getVmTemplate().setKernelUrl(getParameters().getMasterVm().getKernelUrl());
        getVmTemplate().setKernelParams(getParameters().getMasterVm().getKernelParams());
        getVmTemplate().setQuotaId(getParameters().getMasterVm().getQuotaId());
        getVmTemplate().setDedicatedVmForVds(getParameters().getMasterVm().getDedicatedVmForVds());
        getVmTemplate().setMigrationSupport(getParameters().getMasterVm().getMigrationSupport());
        getVmTemplate().setAllowConsoleReconnect(getParameters().getMasterVm().isAllowConsoleReconnect());
        getVmTemplate().setIsoPath(getParameters().getMasterVm().getIsoPath());
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
            iDynamic.setId(Guid.newGuid());
            iDynamic.setVmTemplateId(getVmTemplateId());
            iDynamic.setName(iface.getName());
            iDynamic.setNetworkName(iface.getNetworkName());
            iDynamic.setSpeed(VmInterfaceType.forValue(iface.getType()).getSpeed());
            iDynamic.setType(iface.getType());
            iDynamic.setLinked(iface.isLinked());
            templateInterfaces.add(iDynamic);
            DbFacade.getInstance().getVmNetworkInterfaceDao().save(iDynamic);
        }
        return templateInterfaces;
    }

    protected void AddVmTemplateImages() {
        Guid vmSnapshotId = Guid.newGuid();
        for (DiskImage diskImage : mImages) {
            // The return value of this action is the 'copyImage' task GUID:
            VdcReturnValueBase retValue = executeChildCommand(diskImage.getImageId());

            if (!retValue.getSucceeded()) {
                throw new VdcBLLException(retValue.getFault().getError(), retValue.getFault().getMessage());
            }

            getReturnValue().getVdsmTaskIdList().addAll(retValue.getInternalVdsmTaskIdList());
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
        getVmStaticDAO().incrementDbGeneration(getVmTemplateId());
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Backend.getInstance().EndAction(VdcActionType.CreateImageTemplate, p);
        }
        if (reloadVmTemplateFromDB() != null) {
            endDefaultOperations();
        }
        checkTrustedService();
        setSucceeded(true);
    }

    private void checkTrustedService() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmName", getVmName());
        logable.addCustomValue("VmTemplateName", getVmTemplateName());
        if (getVm().isTrustedService() && !getVmTemplate().isTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVm().isTrustedService() && getVmTemplate().isTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    private void endSuccessfullySynchronous() {
        if (reloadVmTemplateFromDB() != null) {
            endDefaultOperations();
        }
        setSucceeded(true);
    }

    private void endDefaultOperations() {
        endUnlockOps();
    }

    private void endUnlockOps() {
        if (isVmInDb) {
            VmHandler.UnLockVm(getVm());
        }
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
            p.setTaskGroupSuccess(false);
            Backend.getInstance().EndAction(VdcActionType.CreateImageTemplate, p);
        }

        // if template exist in db remove it
        if (getVmTemplate() != null) {
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
            Guid storagePoolId = getVdsGroup() == null ? null : getVdsGroup().getStoragePoolId();
            permissionCheckSubject.add(new PermissionSubject(storagePoolId,
                    VdcObjectType.StoragePool,
                    getActionType().getActionGroup()));

            // host-specific parameters can be changed by administration role only
            if (getParameters().getMasterVm().getDedicatedVmForVds() != null ||
                    !StringUtils.isEmpty(getParameters().getMasterVm().getCpuPinning())) {
                permissionCheckSubject.add(
                        new PermissionSubject(storagePoolId,
                                VdcObjectType.StoragePool,
                                ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES));
            }
        }

        return permissionCheckSubject;
    }

    private void addPermission() {
        addPermissionForTemplate(getCurrentUser().getUserId(), PredefinedRoles.TEMPLATE_OWNER);
        // if the template is for public use, set EVERYONE as a TEMPLATE_USER.
        if (getParameters().isPublicUse()) {
            addPermissionForTemplate(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID, PredefinedRoles.TEMPLATE_USER);
        }

        copyVmPermissions();
    }

    private void copyVmPermissions() {
        if (!isVmInDb || !getParameters().isCopyVmPermissions()) {
            return;
        }

        PermissionDAO dao = getDbFacade().getPermissionDao();

        List<permissions> vmPermissions = dao.getAllForEntity(getVmId(), getCurrentUser().getUserId(), false);

        List<permissions> templatePermissions = new ArrayList<permissions>();

        for (permissions vmPermission : vmPermissions) {
            templatePermissions.add(new permissions(getCurrentUser().getUserId(), vmPermission.getrole_id(),
                    getParameters().getVmTemplateId(), VdcObjectType.VmTemplate));
        }

        if (templatePermissions.size() > 0) {
            MultiLevelAdministrationHandler.addPermission(templatePermissions.toArray(new permissions[templatePermissions.size()]));
        }
    }

    private void addPermissionForTemplate(Guid userId, PredefinedRoles role) {
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

    private Guid getQuotaIdForDisk(DiskImage diskImage) {
        // If the DiskInfoDestinationMap is available and contains information about the disk
        if (getParameters().getDiskInfoDestinationMap() != null
                && getParameters().getDiskInfoDestinationMap().get(diskImage.getId()) != null) {
            return  getParameters().getDiskInfoDestinationMap().get(diskImage.getId()).getQuotaId();
        }
        return diskImage.getQuotaId();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        for (DiskImage disk : getVm().getDiskList()) {
            list.add(new QuotaStorageConsumptionParameter(
                    getQuotaIdForDisk(disk),
                    null,
                    QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
                    disk.getStorageIds().get(0),
                    (double)disk.getSizeInGigabytes()));
        }
        return list;
    }

    private Guid getQuotaId() {
        return getParameters().getMasterVm().getQuotaId();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        list.add(new QuotaSanityParameter(getQuotaId(), null));
        return list;
    }
}
