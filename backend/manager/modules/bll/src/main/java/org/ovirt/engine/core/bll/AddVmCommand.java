package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateSnapshotFromTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.GetImageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.utils.linq.All;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.VMCustomProperties;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.ValidationError;

public class AddVmCommand<T extends VmManagementParametersBase> extends VmManagementCommandBase<T> {

    protected Map<Guid, Guid> imageToDestinationDomainMap;
    protected Map<Guid, storage_domains> destStorages = new HashMap<Guid, storage_domains>();
    protected Map<Guid, List<DiskImage>> storageToDisksMap;
    protected String newMac = "";

    /**
     * A list of the new disk images which were saved for the VM.
     */
    protected List<DiskImage> newDiskImages = new ArrayList<DiskImage>();

    public AddVmCommand(T parameters) {
        super(parameters);
        // if we came from EndAction the VmId is not null
        setVmId((parameters.getVmId().equals(Guid.Empty)) ? Guid.NewGuid() : parameters.getVmId());
        parameters.setVmId(getVmId());
        setVmTemplateId(parameters.getVmStaticData().getvmt_guid());

        if (getVmTemplate() != null) {
            VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
        }
        parameters.setEntityId(getVmId());
        if (getVdsGroup() != null) {
            setStoragePoolId(getVdsGroup().getstorage_pool_id() != null ? getVdsGroup().getstorage_pool_id()
                        .getValue() : Guid.Empty);
        }
        imageToDestinationDomainMap = getParameters().getImageToDestinationDomainMap();
        if (imageToDestinationDomainMap == null) {
            imageToDestinationDomainMap = new HashMap<Guid, Guid>();
        }
    }

    protected AddVmCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public NGuid getStorageDomainId() {
        if (getParameters() != null) {
            if (getParameters().getStorageDomainId().equals(Guid.Empty)
                    && getVmTemplate() != null
                    && getVmTemplate().getDiskMap().size() > 0
                    && !LinqUtils.firstOrNull(getVmTemplate().getDiskMap().values(), new All<DiskImage>())
                            .getId().equals(VmTemplateHandler.BlankVmTemplateId)) {
                getParameters().setStorageDomainId(SelectStorageDomain(getVmTemplate()));
            }
            return getParameters().getStorageDomainId();
        } else {
            return null;
        }
    }

    /**
     * Select storage domain according to the given template - with most
     * available disk size. must initialize template disks before using this
     * method
     *
     * @param vmTemplate
     * @return
     */
    public static Guid SelectStorageDomain(VmTemplate vmTemplate) {
        Guid selectedDomainId = Guid.Empty;
        if (vmTemplate != null && vmTemplate.getDiskMap().size() > 0) {
            int size = -1;
            DiskImage disk = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(
                    LinqUtils.firstOrNull(vmTemplate.getDiskMap().values(), new All<DiskImage>())
                            .getId());
            ArrayList<Guid> domainsList = (ArrayList<Guid>) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageDomainsList,
                            new GetImageDomainsListVDSCommandParameters(vmTemplate.getstorage_pool_id().getValue(),
                                    disk.getimage_group_id().getValue())).getReturnValue();
            for (Guid domainId : domainsList) {
                storage_domains domain = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(domainId,
                        vmTemplate.getstorage_pool_id());
                if (domain != null
                        && domain.getstorage_domain_type() != StorageDomainType.ImportExport
                        && domain.getstatus() == StorageDomainStatus.Active
                        && StorageDomainSpaceChecker.isBelowThresholds(domain)) {
                    if (domain.getavailable_disk_size() != null && domain.getavailable_disk_size() > size) {
                        selectedDomainId = domainId;
                        size = domain.getavailable_disk_size();
                    }
                }
            }
        }
        return selectedDomainId;
    }

    private Guid _vmSnapshotId = Guid.Empty;

    protected VmDynamicDAO getVmDynamicDao() {
        return DbFacade.getInstance().getVmDynamicDAO();
    }

    protected VmStaticDAO getVmStaticDao() {
        return DbFacade.getInstance().getVmStaticDAO();
    }

    protected Guid getVmSnapshotId() {
        return _vmSnapshotId;
    }

    protected List<VmNetworkInterface> _vmInterfaces;

    protected List<VmNetworkInterface> getVmInterfaces() {
        if (_vmInterfaces == null) {
            _vmInterfaces =
                    ((DbFacade.getInstance().getVmNetworkInterfaceDAO().getAllForTemplate(getVmTemplate().getId())) != null) ? DbFacade
                            .getInstance().getVmNetworkInterfaceDAO().getAllForTemplate(getVmTemplate().getId())
                            : new ArrayList<VmNetworkInterface>();
        }
        return _vmInterfaces;
    }

    protected List<DiskImageBase> _vmDisks;

    protected List<DiskImageBase> getVmDisks() {
        if (_vmDisks == null) {
            _vmDisks =
                    LinqUtils.foreach(DbFacade.getInstance()
                            .getDiskImageDAO()
                            .getAllForVm(getVmTemplateId()),
                            new Function<DiskImage, DiskImageBase>() {
                                @Override
                                public DiskImageBase eval(DiskImage diskImageTemplate) {
                                    return DbFacade.getInstance()
                                            .getDiskImageDAO()
                                            .getSnapshotById(diskImageTemplate.getId());
                                }
                            });
        }

        return _vmDisks;
    }

    protected boolean CanAddVm(ArrayList<String> reasons, Collection<storage_domains> destStorages) {
        VmStatic vmStaticFromParams = getParameters().getVmStaticData();
        boolean returnValue = canAddVm(reasons, 1, vmStaticFromParams.getvm_name(), getStoragePoolId()
                .getValue(), vmStaticFromParams.getpriority());
        // check that template image and vm are on the same storage pool

        if (returnValue) {
            List<ValidationError> validationErrors = VmPropertiesUtils.validateVMProperties(vmStaticFromParams);
            if (!validationErrors.isEmpty()) {
                handleCustomPropertiesError(validationErrors, reasons);
                returnValue = false;
            }
        }
        if (returnValue
                && getVmTemplate().getDiskMap().size() > 0
                && !LinqUtils.firstOrNull(getVmTemplate().getDiskMap().values(), new All<DiskImage>())
                        .getId().equals(VmTemplateHandler.BlankVmTemplateId)) {
            if (!getStoragePoolId().equals(getVmTemplate().getstorage_pool_id().getValue())) {
                reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH.toString());
                returnValue = false;
            } else {
                for (storage_domains domain : destStorages) {
                    if (!StorageDomainSpaceChecker.isBelowThresholds(domain)) {
                        returnValue = false;
                        reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString());
                        break;
                    } else if (!StorageDomainSpaceChecker.hasSpaceForRequest(domain,
                            getNeededDiskSize(domain.getId()))) {
                        returnValue = false;
                        reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString());
                        break;
                    }
                }
            }
        }
        if (returnValue) {
            returnValue = isDedicatedVdsOnSameCluster(vmStaticFromParams);
        }
        return returnValue;
    }

    protected int getNeededDiskSize(Guid domainId) {
        return getBlockSparseInitSizeInGB() * storageToDisksMap.get(domainId).size();
    }

    protected boolean CanDoAddVmCommand() {
        boolean returnValue = false;
        returnValue = areParametersLegal(getReturnValue().getCanDoActionMessages());
        returnValue =
                returnValue
                        && CheckPCIAndIDELimit(getParameters().getVmStaticData().getnum_of_monitors(),
                                getVmInterfaces(),
                                getVmDisks(), getReturnValue().getCanDoActionMessages())
                        && CanAddVm(getReturnValue().getCanDoActionMessages(), destStorages.values())
                        && hostToRunExist();
        return returnValue;
    }

    protected boolean hostToRunExist() {
        if (getParameters().getVmStaticData().getdedicated_vm_for_vds() != null) {
            if (DbFacade.getInstance().getVdsDAO().get(getParameters().getVmStaticData().getdedicated_vm_for_vds()) == null) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
                return false;
            }
        }
        return true;
    }

    public static boolean CheckCpuSockets(int num_of_sockets, int cpu_per_socket, String compatibility_version,
                                          java.util.ArrayList<String> CanDoActionMessages) {
        boolean retValue = true;
        if (retValue
                && (num_of_sockets * cpu_per_socket) > Config.<Integer> GetValue(ConfigValues.MaxNumOfVmCpus,
                        compatibility_version)) {
            CanDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_MAX_NUM_CPU.toString());
            retValue = false;
        }
        if (retValue
                && num_of_sockets > Config.<Integer> GetValue(ConfigValues.MaxNumOfVmSockets, compatibility_version)) {
            CanDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_MAX_NUM_SOCKETS.toString());
            retValue = false;
        }
        if (retValue
                && cpu_per_socket > Config.<Integer> GetValue(ConfigValues.MaxNumOfCpuPerSocket, compatibility_version)) {
            CanDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_MAX_CPU_PER_SOCKET.toString());
            retValue = false;
        }
        if (retValue && cpu_per_socket < 1) {
            CanDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_MIN_CPU_PER_SOCKET.toString());
            retValue = false;
        }
        if (retValue && num_of_sockets < 1) {
            CanDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_MIN_NUM_SOCKETS.toString());
            retValue = false;
        }
        return retValue;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getVmTemplate() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            return false;
        }
        returnValue = buildAndCheckDestStorageDomains();
        if (returnValue) {
            storageToDisksMap =
                    ImagesHandler.buildStorageToDiskMap(getVmTemplate().getDiskMap().values(),
                            imageToDestinationDomainMap);
            returnValue = CanDoAddVmCommand();
        }

        String vmName = getParameters().getVm().getvm_name();
        if (vmName == null || vmName.isEmpty()) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
        } else {
            // check that VM name is not too long
            boolean vmNameValidLength = isVmNameValidLength(getParameters().getVm());
            if (!vmNameValidLength) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
            }
        }

        if (returnValue && Config.<Boolean> GetValue(ConfigValues.LimitNumberOfNetworkInterfaces,
                                        getVdsGroup().getcompatibility_version().toString())) {
            // check that we have no more then 8 interfaces (kvm limitation in version 2.x)
            if (!validateNumberOfNics(getVmInterfaces(), null)) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_EXITED_MAX_INTERFACES);
                returnValue = false;
            }
        }

        return returnValue && checkCpuSockets();
    }

    protected boolean checkCpuSockets() {
        return AddVmCommand.CheckCpuSockets(getParameters().getVmStaticData().getnum_of_sockets(),
                getParameters().getVmStaticData().getcpu_per_socket(), getVdsGroup().getcompatibility_version()
                        .toString(), getReturnValue().getCanDoActionMessages());
    }

    protected boolean buildAndCheckDestStorageDomains() {
        boolean returnValue = true;
        ensureDomainMap();
        Set<Guid> destStorageDomains = new HashSet<Guid>(imageToDestinationDomainMap.values());
        for (Guid destStorageDomain : destStorageDomains) {
            storage_domains storage = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(
                    destStorageDomain, getStoragePoolId());
            StorageDomainValidator validator =
                    new StorageDomainValidator(storage);
            if (!validator.isDomainExistAndActive(getReturnValue().getCanDoActionMessages())
                    || !validator.domainIsValidDestination(getReturnValue().getCanDoActionMessages())) {
                returnValue = false;
                break;
            }
            destStorages.put(storage.getId(), storage);
        }
        return returnValue;
    }

    private void ensureDomainMap() {
        if (imageToDestinationDomainMap.isEmpty()) {
            for (DiskImage image : getVmTemplate().getDiskMap().values()) {
                imageToDestinationDomainMap.put(image.getId(), getStorageDomainId().getValue());
            }
        }

    }

    @Override
    protected boolean validateQuota() {
        // Set default quota id if storage pool enforcement is disabled.
        getParameters().setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(getParameters().getVmStaticData()
                .getQuotaId(),
                getStoragePool()));
        for (DiskImage dit : getVmTemplate().getDiskMap().values()) {
            dit.setQuotaId(QuotaHelper.getInstance()
                    .getQuotaIdToConsume(getParameters().getVmStaticData().getQuotaId(),
                            getStoragePool()));
        }
        if (!isInternalExecution()) {
            // TODO: Should be changed when multiple storage domain will be implemented and the desired quotas will be transferred.
            return QuotaManager.validateMultiStorageQuota(getStoragePool().getQuotaEnforcementType(),
                        QuotaHelper.getInstance().getQuotaConsumeMap(getVmTemplate().getDiskList()),
                        getCommandId(),
                        getReturnValue().getCanDoActionMessages());
        }
        return true;
    }

    protected boolean canAddVm(ArrayList<String> reasons, int vmsCount, String name,
                            Guid storagePoolId, int vmPriority) {
        boolean returnValue;
        // Checking if a desktop with same name already exists
        boolean exists = (Boolean) Backend.getInstance()
                .runInternalQuery(VdcQueryType.IsVmWithSameNameExist, new IsVmWithSameNameExistParameters(name))
                .getReturnValue();

        if (exists) {
            if (reasons != null) {
                reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_ALREADY_EXIST.toString());
            }

            return false;
        }

        boolean checkTemplateLock = getParameters().getParentCommand() == VdcActionType.AddVmPoolWithVms ? false : true;

        returnValue = VmHandler.VerifyAddVm(reasons, vmsCount, getVmTemplate(), storagePoolId, vmPriority);

        if (returnValue && !getParameters().getDontCheckTemplateImages()) {
            for (storage_domains storage : destStorages.values()) {
                if (!VmTemplateCommand.isVmTemplateImagesReady(getVmTemplate(), storage.getId(),
                        reasons, false, checkTemplateLock, true, true, storageToDisksMap.get(storage.getId()))) {
                    return false;
                }
            }
        }

        return returnValue;
    }

    @Override
    protected void ExecuteVmCommand() {
        ArrayList<String> errorMessages = new ArrayList<String>();
        if (CanAddVm(errorMessages, destStorages.values())) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    AddVmStatic();
                    AddVmDynamic();
                    AddVmNetwork();
                    AddVmStatistics();
                    addActiveSnapshot();
                    getCompensationContext().stateChanged();
                    return null;
                }
            });

            addVmPermission();
            if (AddVmImages()) {
                copyVmDevices();
                setActionReturnValue(getVm().getId());
                setSucceeded(true);
            }
        } else {
            log.errorFormat("Failed to add vm . The reasons are: {0}", StringUtils.join(errorMessages, ','));
        }
    }

    protected void copyVmDevices() {
        VmDeviceUtils.copyVmDevices(getVmTemplateId(), getVmId(), newDiskImages);
    }

    protected static boolean IsLegalClusterId(Guid clusterId, java.util.ArrayList<String> reasons) {
        // check given cluster id
        VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDAO().get(clusterId);
        boolean legalClusterId = (vdsGroup != null);
        if (!legalClusterId) {
            reasons.add(VdcBllErrors.VM_INVALID_SERVER_CLUSTER_ID.toString());
        }
        return legalClusterId;
    }

    protected boolean areParametersLegal(java.util.ArrayList<String> reasons) {
        boolean returnValue = false;
        VmStatic vmStaticData = getParameters().getVmStaticData();

        if (vmStaticData != null) {

            returnValue = vmStaticData.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST
                    || !vmStaticData.getauto_startup();
            if (!returnValue) {
                reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_PINNED_TO_HOST
                        .toString());
            }
            returnValue = returnValue && IsLegalClusterId(vmStaticData.getvds_group_id(), reasons);
            returnValue = returnValue
                    && VmHandler.isMemorySizeLegal(vmStaticData.getos(), vmStaticData.getmem_size_mb(),
                            reasons, getVdsGroup().getcompatibility_version().toString());

        }
        return returnValue;
    }

    protected void AddVmNetwork() {
        // Add interfaces from template
        for (VmNetworkInterface iface : getVmInterfaces()) {
            String mac = null;
            RefObject<String> tempRefObject = new RefObject<String>(mac);
            MacPoolManager.getInstance().allocateNewMac(tempRefObject);
            mac = tempRefObject.argvalue;
            iface.setId(Guid.NewGuid());
            iface.setMacAddress(mac);
            iface.setSpeed(VmInterfaceType.forValue(iface.getType()).getSpeed());
            iface.setVmTemplateId(null);
            iface.setVmId(getParameters().getVmStaticData().getId());
            DbFacade.getInstance().getVmNetworkInterfaceDAO().save(iface);
            getCompensationContext().snapshotNewEntity(iface);
            DbFacade.getInstance().getVmNetworkStatisticsDAO().save(iface.getStatistics());
            getCompensationContext().snapshotNewEntity(iface.getStatistics());
        }
    }

    protected void AddVmStatic() {
        VmStatic vmStatic = getParameters().getVmStaticData();
        if (vmStatic.getorigin() == null) {
            vmStatic.setorigin(OriginType.valueOf(Config.<String>GetValue(ConfigValues.OriginType)));
        }
        vmStatic.setId(getVmId());
        vmStatic.setQuotaId(getParameters().getQuotaId());
        vmStatic.setcreation_date(new Date());
        // Parses the custom properties field that was filled by frontend to
        // predefined and user defined fields
        if (vmStatic.getCustomProperties() != null) {
            VMCustomProperties properties = VmPropertiesUtils.parseProperties(vmStatic.getCustomProperties());
            String predefinedProperties = properties.getPredefinedProperties();
            String userDefinedProperties = properties.getUseDefinedProperties();
            vmStatic.setPredefinedProperties(predefinedProperties);
            vmStatic.setUserDefinedProperties(userDefinedProperties);
        }
        getVmStaticDao().save(vmStatic);
        getCompensationContext().snapshotNewEntity(vmStatic);
    }

    private void AddVmDynamic() {
        VmDynamic tempVar = new VmDynamic();
        tempVar.setId(getVmId());
        tempVar.setstatus(VMStatus.Down);
        tempVar.setvm_host("");
        tempVar.setvm_ip("");
        tempVar.setdisplay_type(getParameters().getVmStaticData().getdefault_display_type());
        VmDynamic vmDynamic = tempVar;
        DbFacade.getInstance().getVmDynamicDAO().save(vmDynamic);
        getCompensationContext().snapshotNewEntity(vmDynamic);
    }

    private void AddVmStatistics() {
        VmStatistics stats = new VmStatistics();
        stats.setId(getVmId());
        DbFacade.getInstance().getVmStatisticsDAO().save(stats);
        getCompensationContext().snapshotNewEntity(stats);
    }

    protected boolean AddVmImages() {
        if (getVmTemplate().getDiskMap().size() > 0) {
            if (getVm().getstatus() != VMStatus.Down) {
                log.error("Cannot add images. VM is not Down");
                throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
            }
            VmHandler.LockVm(getVmId());
            for (DiskImage dit : getVmTemplate().getDiskMap().values()) {
                CreateSnapshotFromTemplateParameters tempVar = new CreateSnapshotFromTemplateParameters(
                        dit.getId(), getParameters().getVmStaticData().getId());
                tempVar.setDestStorageDomainId(imageToDestinationDomainMap.get(dit.getId()));
                tempVar.setStorageDomainId(dit.getstorage_ids().get(0));
                tempVar.setVmSnapshotId(getVmSnapshotId());
                tempVar.setParentCommand(VdcActionType.AddVm);
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setParentParemeters(getParameters());
                tempVar.setQuotaId(dit.getQuotaId());
                VdcReturnValueBase result =
                        Backend.getInstance().runInternalAction(VdcActionType.CreateSnapshotFromTemplate,
                                tempVar,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
                getParameters().getImagesParameters().add(tempVar);

                /**
                 * if couldn't create snapshot then stop the transaction and the command
                 */
                if (!result.getSucceeded()) {
                    throw new VdcBLLException(result.getFault().getError());
                } else {
                    getTaskIdList().addAll(result.getInternalTaskIdList());
                    newDiskImages.add((DiskImage) result.getActionReturnValue());
                }
            }
        }
        return true;
    }

    protected void removeQuotaCommandLeftOver() {
        if (!isInternalExecution()) {
            QuotaManager.removeMultiStorageDeltaQuotaCommand(QuotaHelper.getInstance()
                    .getQuotaConsumeMap(getVmTemplate().getDiskList()),
                    getStoragePool().getQuotaEnforcementType(),
                    getCommandId());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? (getReturnValue().getTaskIdList().size() > 0 ? AuditLogType.USER_ADD_VM_STARTED
                    : AuditLogType.USER_ADD_VM) : AuditLogType.USER_FAILED_ADD_VM;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ADD_VM_FINISHED_SUCCESS
                    : AuditLogType.USER_ADD_VM_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ADD_VM_FINISHED_FAILURE;
        }
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.CreateSnapshotFromTemplate;
    }

    @Override
    protected void EndWithFailure() {
        super.EndActionOnDisks();

        if (getVm() != null) {
            RemoveVmInSpm(getVm().getstorage_pool_id(), getVmId());
        }
        removeVmRelatedEntitiesFromDb();
        setSucceeded(true);
    }

    protected void removeVmRelatedEntitiesFromDb() {
        RemoveVmUsers();
        RemoveVmNetwork();
        new SnapshotsManager().removeSnapshots(getVmId());
        RemoveVmStatic();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getVdsGroupId(), VdcObjectType.VdsGroups, getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getVmTemplateId(), VdcObjectType.VmTemplate, getActionType().getActionGroup()));
        permissionList = QuotaHelper.getInstance().addQuotaPermissionSubject(permissionList, getStoragePool(), getQuotaId());
        return permissionList;
    }

    protected void addVmPermission() {
        if ((getParameters()).isMakeCreatorExplicitOwner()) {
            permissions perms = new permissions(getCurrentUser().getUserId(), PredefinedRoles.VM_OPERATOR.getId(),
                    getVmId(), VdcObjectType.VM);
            MultiLevelAdministrationHandler.addPermission(perms);
        }
    }

    protected void addActiveSnapshot() {
        _vmSnapshotId = Guid.NewGuid();
        new SnapshotsManager().addActiveSnapshot(_vmSnapshotId, getVm(), getCompensationContext());
    }
}
