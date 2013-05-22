package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateSnapshotFromTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.customprop.ValidationError;
import org.ovirt.engine.core.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.utils.customprop.VmPropertiesUtils.VMCustomProperties;
import org.ovirt.engine.core.utils.linq.All;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;


@DisableInPrepareMode
@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class AddVmCommand<T extends VmManagementParametersBase> extends VmManagementCommandBase<T>
        implements QuotaStorageDependent, QuotaVdsDependent {

    protected HashMap<Guid, DiskImage> diskInfoDestinationMap;
    protected Map<Guid, StorageDomain> destStorages = new HashMap<Guid, StorageDomain>();
    protected Map<Guid, List<DiskImage>> storageToDisksMap;

    /**
     * A list of the new disk images which were saved for the VM.
     */
    protected List<DiskImage> newDiskImages = new ArrayList<DiskImage>();

    public AddVmCommand(T parameters) {
        super(parameters);
        // if we came from EndAction the VmId is not null
        setVmId((parameters.getVmId().equals(Guid.Empty)) ? Guid.NewGuid() : parameters.getVmId());
        setVmName(parameters.getVm().getName());
        parameters.setVmId(getVmId());
        setStorageDomainId(getParameters().getStorageDomainId());
        if (parameters.getVmStaticData() != null) {
            setVmTemplateId(parameters.getVmStaticData().getVmtGuid());
        }

        parameters.setEntityId(getVmId());
        initTemplateDisks();
        initStoragePoolId();
        diskInfoDestinationMap = getParameters().getDiskInfoDestinationMap();
        if (diskInfoDestinationMap == null) {
            diskInfoDestinationMap = new HashMap<Guid, DiskImage>();
        }
    }

    protected AddVmCommand(Guid commandId) {
        super(commandId);
    }

    protected void initStoragePoolId() {
        if (getVdsGroup() != null) {
            setStoragePoolId(getVdsGroup().getStoragePoolId() != null ? getVdsGroup().getStoragePoolId().getValue()
                    : Guid.Empty);
        }
    }

    protected void initTemplateDisks() {
        if (getVmTemplate() != null) {
            VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
        }
    }

    private Guid _vmSnapshotId = Guid.Empty;

    protected Guid getVmSnapshotId() {
        return _vmSnapshotId;
    }

    protected List<VmNetworkInterface> _vmInterfaces;

    protected List<VmNetworkInterface> getVmInterfaces() {
        if (_vmInterfaces == null) {
            List<VmNetworkInterface> vmNetworkInterfaces =
                    DbFacade.getInstance().getVmNetworkInterfaceDao().getAllForTemplate(getVmTemplate().getId());
            _vmInterfaces =
                    (vmNetworkInterfaces != null) ? vmNetworkInterfaces
                            : new ArrayList<VmNetworkInterface>();
        }
        return _vmInterfaces;
    }

    protected List<? extends Disk> _vmDisks;

    protected List<? extends Disk> getVmDisks() {
        if (_vmDisks == null) {
            _vmDisks =
                    DbFacade.getInstance()
                            .getDiskDao()
                            .getAllForVm(getVmTemplateId());
        }

        return _vmDisks;
    }

    protected boolean canAddVm(ArrayList<String> reasons, Collection<StorageDomain> destStorages) {
        VmStatic vmStaticFromParams = getParameters().getVmStaticData();
        boolean returnValue = canAddVm(reasons, vmStaticFromParams.getName(), getStoragePoolId()
                .getValue(), vmStaticFromParams.getPriority());

        if (returnValue) {
            List<ValidationError> validationErrors = validateCustomProperties(vmStaticFromParams);
            if (!validationErrors.isEmpty()) {
                VmPropertiesUtils.getInstance().handleCustomPropertiesError(validationErrors, reasons);
                returnValue = false;
            }
        }

        // check that template image and vm are on the same storage pool
        if (returnValue
                && shouldCheckSpaceInStorageDomains()) {
            if (!getStoragePoolId().equals(getStoragePoolIdFromSourceImageContainer())) {
                reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH.toString());
                returnValue = false;
            } else {
                for (StorageDomain domain : destStorages) {
                    StorageDomainValidator storageDomainValidator = new StorageDomainValidator(domain);
                    if (!validate(storageDomainValidator.isDomainWithinThresholds())
                            || !validate(storageDomainValidator.isDomainHasSpaceForRequest(getNeededDiskSize(domain.getId())))) {
                        return false;
                    }
                }
            }
        }
        if (returnValue) {
            returnValue = isDedicatedVdsOnSameCluster(vmStaticFromParams);
        }
        return returnValue;
    }

    protected boolean shouldCheckSpaceInStorageDomains() {
        return !getImagesToCheckDestinationStorageDomains().isEmpty()
                && !LinqUtils.firstOrNull(getImagesToCheckDestinationStorageDomains(), new All<DiskImage>())
                        .getImageId().equals(VmTemplateHandler.BlankVmTemplateId);
    }

    protected Guid getStoragePoolIdFromSourceImageContainer() {
        return getVmTemplate().getStoragePoolId().getValue();
    }

    protected int getNeededDiskSize(Guid domainId) {
        return getBlockSparseInitSizeInGb() * storageToDisksMap.get(domainId).size();
    }

    protected boolean canDoAddVmCommand() {
        boolean returnValue = false;
        returnValue = areParametersLegal(getReturnValue().getCanDoActionMessages());
        // Check if number of monitors passed is legal
        returnValue =
                returnValue
                        && checkNumberOfMonitors();

        returnValue =
                returnValue
                        && checkPciAndIdeLimit(getParameters().getVmStaticData().getNumOfMonitors(),
                                getVmInterfaces(),
                                getVmDisks(), getReturnValue().getCanDoActionMessages())
                        && canAddVm(getReturnValue().getCanDoActionMessages(), destStorages.values())
                        && hostToRunExist();
        return returnValue;
    }

    protected boolean checkNumberOfMonitors() {
        return VmHandler.isNumOfMonitorsLegal(getParameters().getVmStaticData().getDefaultDisplayType(),
                getParameters().getVmStaticData().getNumOfMonitors(),
                getReturnValue().getCanDoActionMessages());
    }

    protected boolean hostToRunExist() {
        if (getParameters().getVmStaticData().getDedicatedVmForVds() != null) {
            if (DbFacade.getInstance().getVdsDao().get(getParameters().getVmStaticData().getDedicatedVmForVds()) == null) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
                return false;
            }
        }
        return true;
    }

    public static boolean CheckCpuSockets(int num_of_sockets, int cpu_per_socket, String compatibility_version,
            List<String> CanDoActionMessages) {
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
        if (getVmTemplate().isDisabled()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_IS_DISABLED);
        }
        returnValue = buildAndCheckDestStorageDomains();
        if (returnValue) {
            storageToDisksMap =
                    ImagesHandler.buildStorageToDiskMap(getImagesToCheckDestinationStorageDomains(),
                            diskInfoDestinationMap);
            returnValue = canDoAddVmCommand();
        }

        String vmName = getParameters().getVm().getName();
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

        // check for Vm Payload
        if (returnValue && getParameters().getVmPayload() != null) {
            returnValue = checkPayload(getParameters().getVmPayload(),
                    getParameters().getVmStaticData().getIsoPath());
            if (returnValue) {
                // we save the content in base64 string
                getParameters().getVmPayload().setContent(Base64.encodeBase64String(
                        getParameters().getVmPayload().getContent().getBytes()));
            }
        }

        // Check that the USB policy is legal
        if (!VmHandler.isUsbPolicyLegal(getParameters().getVm().getUsbPolicy(),
                getParameters().getVm().getOs(),
                getVdsGroup(),
                getReturnValue().getCanDoActionMessages())) {
            returnValue = false;
        }

        // check cpuPinning if the check haven't failed yet
        if (returnValue) {
            VM vmFromParams = getParameters().getVm();
            returnValue = isCpuPinningValid(vmFromParams.getCpuPinning(), vmFromParams.getStaticData());
        }

        if (getParameters().getVm().isUseHostCpuFlags()
                && getParameters().getVm().getMigrationSupport() == MigrationSupport.MIGRATABLE) {
            return failCanDoAction(VdcBllMessages.VM_HOSTCPU_MUST_BE_PINNED_TO_HOST);
        }

        return returnValue && checkCpuSockets();
    }

    protected boolean checkCpuSockets() {
        return AddVmCommand.CheckCpuSockets(getParameters().getVmStaticData().getNumOfSockets(),
                getParameters().getVmStaticData().getCpuPerSocket(), getVdsGroup().getcompatibility_version()
                        .toString(), getReturnValue().getCanDoActionMessages());
    }

    protected boolean buildAndCheckDestStorageDomains() {
        boolean retValue = true;
        if (diskInfoDestinationMap.isEmpty()) {
            retValue = fillDestMap();
        } else {
            retValue = validateProvidedDestinations();
        }
        if (retValue && getImagesToCheckDestinationStorageDomains().size() != diskInfoDestinationMap.size()) {
            log.errorFormat("Can not found any default active domain for one of the disks of template with id : {0}",
                    getVmTemplate().getId());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
            retValue = false;
        }

        return retValue && validateIsImagesOnDomains();
    }

    protected Collection<DiskImage> getImagesToCheckDestinationStorageDomains() {
        return getVmTemplate().getDiskMap().values();
    }

    private boolean validateProvidedDestinations() {
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            if (diskImage.getStorageIds() == null || diskImage.getStorageIds().isEmpty()) {
                diskImage.setStorageIds(new ArrayList<Guid>());
                diskImage.getStorageIds().add(getParameters().getStorageDomainId());
            }
            Guid storageDomainId = diskImage.getStorageIds().get(0);
            if (destStorages.get(storageDomainId) == null) {
                StorageDomain storage = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
                        storageDomainId, getStoragePoolId());
                StorageDomainValidator validator =
                        new StorageDomainValidator(storage);
                if (!validate(validator.isDomainExistAndActive()) || !validate(validator.domainIsValidDestination())) {
                    return false;
                }
                destStorages.put(storage.getId(), storage);
            }
        }
        return true;
    }

    private boolean fillDestMap() {
        if (getParameters().getStorageDomainId() != null
                && !Guid.Empty.equals(getParameters().getStorageDomainId())) {
            Guid storageId = getParameters().getStorageDomainId();
            for (DiskImage image : getImagesToCheckDestinationStorageDomains()) {
                diskInfoDestinationMap.put(image.getId(), makeNewImage(storageId, image));
            }
            return validateProvidedDestinations();
        }
        fillImagesMapBasedOnTemplate();
        return true;
    }

    protected void fillImagesMapBasedOnTemplate() {
        ImagesHandler.fillImagesMapBasedOnTemplate(getVmTemplate(),
                getStorageDomainDAO().getAllForStoragePool(getVmTemplate().getStoragePoolId().getValue()),
                diskInfoDestinationMap,
                destStorages, false);
    }

    protected boolean validateIsImagesOnDomains() {
        for (DiskImage image : getImagesToCheckDestinationStorageDomains()) {
            if (!image.getStorageIds().containsAll(diskInfoDestinationMap.get(image.getId()).getStorageIds())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
                return false;
            }
        }
        return true;
    }

    private DiskImage makeNewImage(Guid storageId, DiskImage image) {
        DiskImage newImage = new DiskImage();
        newImage.setImageId(image.getImageId());
        newImage.setDiskAlias(image.getDiskAlias());
        newImage.setvolumeFormat(image.getVolumeFormat());
        newImage.setVolumeType(image.getVolumeType());
        ArrayList<Guid> storageIds = new ArrayList<Guid>();
        storageIds.add(storageId);
        newImage.setStorageIds(storageIds);
        newImage.setQuotaId(image.getQuotaId());
        return newImage;
    }

    protected boolean canAddVm(List<String> reasons, String name, Guid storagePoolId,
            int vmPriority) {
        boolean returnValue;
        // Checking if a desktop with same name already exists
        boolean exists = isVmWithSameNameExists(name);

        if (exists) {
            if (reasons != null) {
                reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED.toString());
            }

            return false;
        }

        boolean checkTemplateLock = getParameters().getParentCommand() == VdcActionType.AddVmPoolWithVms ? false : true;

        returnValue = verifyAddVM(reasons, vmPriority);

        if (returnValue && !getParameters().getDontCheckTemplateImages()) {
            for (StorageDomain storage : destStorages.values()) {
                if (!VmTemplateCommand.isVmTemplateImagesReady(getVmTemplate(), storage.getId(),
                        reasons, false, checkTemplateLock, true, true, storageToDisksMap.get(storage.getId()))) {
                    return false;
                }
            }
        }

        return returnValue;
    }

    protected boolean verifyAddVM(List<String> reasons, int vmPriority) {
        return VmHandler.VerifyAddVm(reasons,
                getVmInterfaces().size(),
                vmPriority);
    }

    @Override
    protected void executeVmCommand() {
        ArrayList<String> errorMessages = new ArrayList<String>();
        if (canAddVm(errorMessages, destStorages.values())) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    addVmStatic();
                    addVmDynamic();
                    addVmNetwork();
                    addVmStatistics();
                    addActiveSnapshot();
                    addVmPermission();
                    getCompensationContext().stateChanged();
                    return null;
                }
            });

            if (addVmImages()) {
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                    @Override
                    public Void runInTransaction() {
                        copyVmDevices();
                        addDiskPermissions(newDiskImages);
                        addVmPayload();
                        updateSmartCardDevices();
                        addVmWatchdog();
                        setActionReturnValue(getVm().getId());
                        setSucceeded(true);
                        return null;
                    }
                });
            }
        } else {
            log.errorFormat("Failed to add vm . The reasons are: {0}", StringUtils.join(errorMessages, ','));
        }
    }

    private void updateSmartCardDevices() {
        // if vm smartcard settings is different from template's
        // add or remove the smartcard according to user request
        if (getVm().isSmartcardEnabled() != getVmTemplate().isSmartcardEnabled()) {
            VmDeviceUtils.updateSmartcardDevice(getVm().getId(), getVm().isSmartcardEnabled());
        }
    }

    protected void addVmWatchdog() {
        VmWatchdog vmWatchdog = getParameters().getWatchdog();
        if (vmWatchdog != null) {
            WatchdogParameters parameters = new WatchdogParameters();
            parameters.setId(getParameters().getVmId());
            parameters.setAction(vmWatchdog.getAction());
            parameters.setModel(vmWatchdog.getModel());
            getBackend().runInternalAction(VdcActionType.AddWatchdog, parameters);
        }
    }

    protected void addVmPayload() {
        VmPayload payload = getParameters().getVmPayload();

        if (payload != null) {
            VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.NewGuid(), getParameters().getVmId()),
                    VmDeviceGeneralType.DISK,
                    payload.getType(),
                    payload.getSpecParams(),
                    true,
                    true,
                    null);
        }
    }

    protected void copyVmDevices() {
        VmDeviceUtils.copyVmDevices(getVmTemplateId(),
                getVmId(),
                newDiskImages,
                _vmInterfaces);
    }

    protected static boolean IsLegalClusterId(Guid clusterId, List<String> reasons) {
        // check given cluster id
        VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(clusterId);
        boolean legalClusterId = (vdsGroup != null);
        if (!legalClusterId) {
            reasons.add(VdcBllErrors.VM_INVALID_SERVER_CLUSTER_ID.toString());
        }
        return legalClusterId;
    }

    protected boolean areParametersLegal(List<String> reasons) {
        boolean returnValue = false;
        final VmStatic vmStaticData = getParameters().getVmStaticData();

        if (vmStaticData != null) {

            returnValue = vmStaticData.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST
                    || !vmStaticData.isAutoStartup();

            if (!returnValue) {
                reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_PINNED_TO_HOST
                        .toString());
            }

            if (!returnValue) {
                returnValue = returnValue && IsLegalClusterId(vmStaticData.getVdsGroupId(), reasons);
            }

            if (!validatePinningAndMigration(reasons, vmStaticData, getParameters().getVm().getCpuPinning())) {
                returnValue = false;
            }

            returnValue = returnValue
                    && VmHandler.isMemorySizeLegal(vmStaticData.getOsId(), vmStaticData.getMemSizeMb(),
                            reasons, getVdsGroup().getcompatibility_version());

        }
        return returnValue;
    }

    protected void addVmNetwork() {
        // Add interfaces from template
        for (VmNetworkInterface iface : getVmInterfaces()) {
            iface.setId(Guid.NewGuid());
            iface.setMacAddress(MacPoolManager.getInstance().allocateNewMac());
            iface.setSpeed(VmInterfaceType.forValue(iface.getType()).getSpeed());
            iface.setVmTemplateId(null);
            iface.setVmId(getParameters().getVmStaticData().getId());
            DbFacade.getInstance().getVmNetworkInterfaceDao().save(iface);
            getCompensationContext().snapshotNewEntity(iface);
            DbFacade.getInstance().getVmNetworkStatisticsDao().save(iface.getStatistics());
            getCompensationContext().snapshotNewEntity(iface.getStatistics());
        }
    }

    protected void addVmStatic() {
        VmStatic vmStatic = getParameters().getVmStaticData();
        if (vmStatic.getOrigin() == null) {
            vmStatic.setOrigin(OriginType.valueOf(Config.<String> GetValue(ConfigValues.OriginType)));
        }
        vmStatic.setId(getVmId());
        vmStatic.setQuotaId(getQuotaId());
        vmStatic.setCreationDate(new Date());
        // Parses the custom properties field that was filled by frontend to
        // predefined and user defined fields
        if (vmStatic.getCustomProperties() != null) {
            VMCustomProperties properties =
                    VmPropertiesUtils.getInstance().parseProperties(getVdsGroupDAO()
                            .get(getParameters().getVm().getVdsGroupId())
                            .getcompatibility_version(),
                            vmStatic.getCustomProperties());
            String predefinedProperties = properties.getPredefinedProperties();
            String userDefinedProperties = properties.getUseDefinedProperties();
            vmStatic.setPredefinedProperties(predefinedProperties);
            vmStatic.setUserDefinedProperties(userDefinedProperties);
        }
        getVmStaticDao().save(vmStatic);
        getCompensationContext().snapshotNewEntity(vmStatic);
    }

    void addVmDynamic() {
        VmDynamic tempVar = new VmDynamic();
        tempVar.setId(getVmId());
        tempVar.setStatus(VMStatus.Down);
        tempVar.setVmHost("");
        tempVar.setVmIp("");
        tempVar.setDisplayType(getParameters().getVmStaticData().getDefaultDisplayType());
        VmDynamic vmDynamic = tempVar;
        DbFacade.getInstance().getVmDynamicDao().save(vmDynamic);
        getCompensationContext().snapshotNewEntity(vmDynamic);
    }

    void addVmStatistics() {
        VmStatistics stats = new VmStatistics();
        stats.setId(getVmId());
        DbFacade.getInstance().getVmStatisticsDao().save(stats);
        getCompensationContext().snapshotNewEntity(stats);
    }

    protected boolean addVmImages() {
        if (getVmTemplate().getDiskMap().size() > 0) {
            if (getVm().getStatus() != VMStatus.Down) {
                log.error("Cannot add images. VM is not Down");
                throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
            }
            VmHandler.LockVm(getVmId());
            for (DiskImage dit : getImagesToCheckDestinationStorageDomains()) {
                CreateSnapshotFromTemplateParameters tempVar = new CreateSnapshotFromTemplateParameters(
                        dit.getImageId(), getParameters().getVmStaticData().getId());
                tempVar.setDestStorageDomainId(diskInfoDestinationMap.get(dit.getId()).getStorageIds().get(0));
                tempVar.setDiskAlias(diskInfoDestinationMap.get(dit.getId()).getDiskAlias());
                tempVar.setStorageDomainId(dit.getStorageIds().get(0));
                tempVar.setVmSnapshotId(getVmSnapshotId());
                tempVar.setParentCommand(VdcActionType.AddVm);
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setParentParameters(getParameters());
                tempVar.setQuotaId(diskInfoDestinationMap.get(dit.getId()).getQuotaId());
                VdcReturnValueBase result =
                        getBackend().runInternalAction(VdcActionType.CreateSnapshotFromTemplate,
                                tempVar,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

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
    protected void endWithFailure() {
        super.endActionOnDisks();
        removeVmRelatedEntitiesFromDb();
        setSucceeded(true);
    }

    protected void removeVmRelatedEntitiesFromDb() {
        removeVmUsers();
        removeVmNetwork();
        removeVmSnapshots();
        removeVmStatic();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getVdsGroupId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getVmTemplateId(),
                VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        if (getVmTemplate() != null && !getVmTemplate().getDiskList().isEmpty()) {
            for (DiskImage disk : getParameters().getDiskInfoDestinationMap().values()) {
                if (disk.getStorageIds() != null && !disk.getStorageIds().isEmpty()) {
                    permissionList.add(new PermissionSubject(GuidUtils.getGuidValue(disk.getStorageIds().get(0)),
                            VdcObjectType.Storage, ActionGroup.CREATE_DISK));
                }
            }
        }
        addPermissionSubjectForAdminLevelProperties(permissionList);
        return permissionList;
    }

    protected void addPermissionSubjectForAdminLevelProperties(List<PermissionSubject> permissionList) {
        final VmStatic vmFromParams = getParameters().getVmStaticData();

        if (vmFromParams != null) {
            // user needs specific permission to change custom properties
            if (!StringUtils.isEmpty(vmFromParams.getCustomProperties())) {
                permissionList.add(new PermissionSubject(getVdsGroupId(),
                        VdcObjectType.VdsGroups, ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
            }

            // host-specific parameters can be changed by administration role only
            if (vmFromParams.getDedicatedVmForVds() != null || !StringUtils.isEmpty(vmFromParams.getCpuPinning())) {
                permissionList.add(new PermissionSubject(getVdsGroupId(),
                        VdcObjectType.VdsGroups, ActionGroup.EDIT_ADMIN_VM_PROPERTIES));
            }
        }
    }

    protected void addVmPermission() {
        if ((getParameters()).isMakeCreatorExplicitOwner()) {
            permissions perms = new permissions(getCurrentUser().getUserId(), PredefinedRoles.VM_OPERATOR.getId(),
                    getVmId(), VdcObjectType.VM);
            MultiLevelAdministrationHandler.addPermission(perms);
            getCompensationContext().snapshotNewEntity(perms);
        }
    }

    protected void addDiskPermissions(List<DiskImage> newDiskImages) {
        permissions[] permsArray = new permissions[newDiskImages.size()];
        for (int i = 0; i < newDiskImages.size(); i++) {
            permsArray[i] =
                    new permissions(getCurrentUser().getUserId(),
                            PredefinedRoles.DISK_OPERATOR.getId(),
                            newDiskImages.get(i).getId(),
                            VdcObjectType.Disk);
        }
        MultiLevelAdministrationHandler.addPermission(permsArray);
    }

    protected void addActiveSnapshot() {
        _vmSnapshotId = Guid.NewGuid();
        new SnapshotsManager().addActiveSnapshot(_vmSnapshotId, getVm(), getCompensationContext());
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!StringUtils.isBlank(getParameters().getVm().getName())) {
            return Collections.singletonMap(getParameters().getVm().getName(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_NAME, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }

    @Override
    protected VmDynamicDAO getVmDynamicDao() {
        return DbFacade.getInstance().getVmDynamicDao();
    }

    protected VmStaticDAO getVmStaticDao() {
        return DbFacade.getInstance().getVmStaticDao();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
        }
        return jobProperties;
    }

    private Guid getQuotaId() {
        return getParameters().getVmStaticData().getQuotaId();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        for (DiskImage disk : diskInfoDestinationMap.values()) {
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    null,
                    QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
                    disk.getStorageIds().get(0),
                    (double)disk.getSizeInGigabytes()));
        }
        return list;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        list.add(new QuotaSanityParameter(getQuotaId(), null));
        return list;
    }
}
