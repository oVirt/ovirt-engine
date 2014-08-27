package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsGroupConsumptionParameter;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.HotSetNumerOfCpusParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.UpdateVm;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.customprop.ValidationError;
import org.ovirt.engine.core.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.utils.customprop.VmPropertiesUtils.VMCustomProperties;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@LockIdNameAttribute
public class UpdateVmCommand<T extends VmManagementParametersBase> extends VmManagementCommandBase<T>
        implements QuotaVdsDependent, RenamedEntityInfoProvider{
    private VM oldVm;
    private boolean quotaSanityOnly = false;
    private VmStatic newVmStatic;
    private VdcReturnValueBase setNumberOfCpusResult;

    public UpdateVmCommand(T parameters) {
        super(parameters);
        if (getVdsGroup() != null) {
            setStoragePoolId(getVdsGroup().getStoragePoolId());
        }

        if (isVmExist()) {
            setCustomDefinedProperties(parameters.getVmStaticData());
            setCustomDefinedProperties(getVm().getStaticData());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM : AuditLogType.USER_FAILED_UPDATE_VM;
    }

    @Override
    protected void executeVmCommand() {
        oldVm = getVm();
        VmHandler.warnMemorySizeLegal(getParameters().getVm().getStaticData(), getVdsGroup().getcompatibility_version());
        getVmStaticDAO().incrementDbGeneration(getVm().getId());
        newVmStatic = getParameters().getVmStaticData();
        newVmStatic.setCreationDate(oldVm.getStaticData().getCreationDate());
        if (newVmStatic.getCreationDate().equals(DateTime.getMinValue())) {
            newVmStatic.setCreationDate(new Date());
        }
        UpdateVmNetworks();
        hotSetCpus();
        getVmStaticDAO().update(newVmStatic);
        updateVmPayload();
        VmDeviceUtils.updateVmDevices(getParameters(), oldVm);
        updateWatchdog();
        checkTrustedService();
        VmHandler.updateVmInitToDB(getParameters().getVmStaticData());
        setSucceeded(true);
    }

    private void hotSetCpus() {
        int currentSockets = getVm().getNumOfSockets();
        int newSockets = newVmStatic.getNumOfSockets();

        if (getVm().getStatus() == VMStatus.Up && currentSockets != newSockets) {
            HotSetNumerOfCpusParameters params =
                    new HotSetNumerOfCpusParameters(
                            newVmStatic,
                            currentSockets < newSockets ? PlugAction.PLUG : PlugAction.UNPLUG);
            setNumberOfCpusResult =
                    getBackend().runInternalAction(
                            VdcActionType.HotSetNumberOfCpus,
                            params);
            newVmStatic.setNumOfSockets(setNumberOfCpusResult.getSucceeded() ? newSockets : currentSockets);
            auditLogHotSetCpusCandos(params);
        }
    }

    private void auditLogHotSetCpusCandos(HotSetNumerOfCpusParameters params) {
        if (!setNumberOfCpusResult.getCanDoAction()) {
            AuditLogableBase logable = new HotSetNumberOfCpusCommand<>(params);
            List<String> canDos = getBackend().getErrorsTranslator().
                    TranslateErrorText(setNumberOfCpusResult.getCanDoActionMessages());
            logable.addCustomValue(HotSetNumberOfCpusCommand.LOGABLE_FIELD_ERROR_MESSAGE, StringUtils.join(canDos, ","));
            AuditLogDirector.log(logable, AuditLogType.FAILED_HOT_SET_NUMBER_OF_CPUS);
        }
    }

    private void checkTrustedService() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmName", getVmName());
        if (getParameters().getVm().isTrustedService() && !getVdsGroup().supportsTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getParameters().getVm().isTrustedService() && getVdsGroup().supportsTrustedService()) {
            AuditLogDirector.log(logable, AuditLogType.USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    private void updateWatchdog() {
        // do not update if this flag is not set
        if (getParameters().isUpdateWatchdog()) {
            VdcQueryReturnValue query =
                    getBackend().runInternalQuery(VdcQueryType.GetWatchdog, new IdQueryParameters(getParameters().getVmId()));
            List<VmWatchdog> watchdogs = query.getReturnValue();
            if (watchdogs.isEmpty()) {
                if (getParameters().getWatchdog() == null) {
                    // nothing to do, no watchdog and no watchdog to create
                } else {
                    WatchdogParameters parameters = new WatchdogParameters();
                    parameters.setId(getParameters().getVmId());
                    parameters.setAction(getParameters().getWatchdog().getAction());
                    parameters.setModel(getParameters().getWatchdog().getModel());
                    getBackend().runInternalAction(VdcActionType.AddWatchdog, parameters);
                }
            } else {
                WatchdogParameters watchdogParameters = new WatchdogParameters();
                watchdogParameters.setId(getParameters().getVmId());
                if (getParameters().getWatchdog() == null) {
                    // there is a watchdog in the vm, there should not be any, so let's delete
                    getBackend().runInternalAction(VdcActionType.RemoveWatchdog, watchdogParameters);
                } else {
                    // there is a watchdog in the vm, we have to update.
                    watchdogParameters.setAction(getParameters().getWatchdog().getAction());
                    watchdogParameters.setModel(getParameters().getWatchdog().getModel());
                    getBackend().runInternalAction(VdcActionType.UpdateWatchdog, watchdogParameters);
                }
            }

        }
    }

    protected void updateVmPayload() {
        VmDeviceDAO dao = getVmDeviceDao();
        VmPayload payload = getParameters().getVmPayload();

        if (payload != null || getParameters().isClearPayload()) {
            List<VmDevice> disks = dao.getVmDeviceByVmIdAndType(getVmId(), VmDeviceGeneralType.DISK);
            VmDevice oldPayload = null;
            for (VmDevice disk : disks) {
                if (VmPayload.isPayload(disk.getSpecParams())) {
                    oldPayload = disk;
                    break;
                }
            }

            if (oldPayload != null) {
                List<VmDeviceId> devs = new ArrayList<VmDeviceId>();
                devs.add(oldPayload.getId());
                dao.removeAll(devs);
            }

            if (!getParameters().isClearPayload()) {
                VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.newGuid(), getVmId()),
                        VmDeviceGeneralType.DISK,
                        payload.getType(),
                        payload.getSpecParams(),
                        true,
                        true,
                        null);
            }
        }
    }

    private void UpdateVmNetworks() {
        // check if the cluster has changed
        if (!getVm().getVdsGroupId().equals(getParameters().getVmStaticData().getVdsGroupId())) {
            List<Network> networks =
                    getNetworkDAO().getAllForCluster(getParameters().getVmStaticData().getVdsGroupId());
            List<VmNic> interfaces = getVmNicDao().getAllForVm(getParameters().getVmStaticData().getId());

            for (final VmNic iface : interfaces) {
                final Network network = NetworkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
                Network net = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                    @Override
                    public boolean eval(Network n) {
                        return ObjectUtils.equals(n.getId(), network.getId());
                    }
                });

                // if network not exists in cluster we remove the network from the interface
                if (net == null) {
                    iface.setVnicProfileId(null);
                    getVmNicDao().update(iface);
                }

            }
        }
    }


    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateVm.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected boolean canDoAction() {
        VM vmFromDB = getVm();
        VM vmFromParams = getParameters().getVm();

        if (!isVmExist()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (StringUtils.isEmpty(vmFromParams.getName())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
            return false;
        }

        // check that VM name is not too long
        boolean vmNameValidLength = isVmNameValidLength(vmFromParams);
        if (!vmNameValidLength) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
            return false;
        }

        // Checking if a desktop with same name already exists
        if (!StringUtils.equals(vmFromDB.getName(), vmFromParams.getName())) {
            boolean exists = isVmWithSameNameExists(vmFromParams.getName());

            if (exists) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                return false;
            }
        }

        List<ValidationError> validationErrors = validateCustomProperties(vmFromParams.getStaticData());
        if (!validationErrors.isEmpty()) {
            VmPropertiesUtils.getInstance().handleCustomPropertiesError(validationErrors,
                    getReturnValue().getCanDoActionMessages());
            return false;
        }

        if (!VmHandler.isOsTypeSupported(vmFromParams.getOs(),
                getVdsGroup().getArchitecture(), getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        if (vmFromParams.getSingleQxlPci() &&
                !VmHandler.isSingleQxlDeviceLegal(vmFromParams.getDefaultDisplayType(),
                        vmFromParams.getOs(),
                        getReturnValue().getCanDoActionMessages(),
                        getVdsGroup().getcompatibility_version())) {
            return false;
        }

        if (!areUpdatedFieldsLegal()) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_UPDATE_ILLEGAL_FIELD);
            return false;
        }

        if (!vmFromDB.getVdsGroupId().equals(vmFromParams.getVdsGroupId())) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_UPDATE_CLUSTER);
            return false;
        }

        if (!isDedicatedVdsOnSameCluster(vmFromParams.getStaticData())) {
            return false;
        }

        // Check if number of monitors passed is legal
        if (!VmHandler.isNumOfMonitorsLegal(vmFromParams.getDefaultDisplayType(),
                vmFromParams.getNumOfMonitors(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        // Check PCI and IDE limits are ok
        if (!isValidPciAndIdeLimit(vmFromParams)) {
            return false;
        }

        if (!VmTemplateCommand.isVmPriorityValueLegal(vmFromParams.getPriority(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        if (vmFromDB.getVmPoolId() != null && vmFromParams.isStateless()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_FROM_POOL_CANNOT_BE_STATELESS);
            return false;
        }

        if (!AddVmCommand.checkCpuSockets(vmFromParams.getNumOfSockets(),
                vmFromParams.getCpuPerSocket(), getVdsGroup().getcompatibility_version()
                .toString(), getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        // check for Vm Payload
        if (getParameters().getVmPayload() != null) {
            if (!checkPayload(getParameters().getVmPayload(), vmFromParams.getIsoPath())) {
                return false;
            }
            // we save the content in base64 string
            for (Map.Entry<String, String> entry : getParameters().getVmPayload().getFiles().entrySet()) {
                entry.setValue(Base64.encodeBase64String(entry.getValue().getBytes()));
            }
        }

        // check for Vm Watchdog Model
        if (getParameters().getWatchdog() != null) {
            if (!validate((new VmWatchdogValidator(vmFromParams.getOs(),
                    getParameters().getWatchdog(),
                    getVdsGroup().getcompatibility_version())).isModelCompatibleWithOs())) {
                return false;
            }
        }

        // Check that the USB policy is legal
        if (!VmHandler.isUsbPolicyLegal(vmFromParams.getUsbPolicy(),
                vmFromParams.getOs(),
                getVdsGroup(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        }

        // Check if the display type is supported
        if (!VmHandler.isDisplayTypeSupported(vmFromParams.getOs(),
                vmFromParams.getDefaultDisplayType(),
                getReturnValue().getCanDoActionMessages(),
                getVdsGroup().getcompatibility_version())) {
            return false;
        }

        // check cpuPinning
        if (!isCpuPinningValid(vmFromParams.getCpuPinning(), vmFromParams.getStaticData())) {
            return false;
        }

        if (!validatePinningAndMigration(getReturnValue().getCanDoActionMessages(),
                getParameters().getVm().getStaticData(), getParameters().getVm().getCpuPinning())) {
            return false;
        }

        if (vmFromParams.isUseHostCpuFlags()
                && vmFromParams.getMigrationSupport() == MigrationSupport.MIGRATABLE) {
            return failCanDoAction(VdcBllMessages.VM_HOSTCPU_MUST_BE_PINNED_TO_HOST);
        }

        if (getParameters().isConsoleEnabled() != null && !getVm().isDown()
                && vmDeviceChanged(VmDeviceGeneralType.CONSOLE, getParameters().isConsoleEnabled())) {
            addCanDoActionMessage("$device console");
            return failCanDoAction(VdcBllMessages.VM_CANNOT_UPDATE_DEVICE_VM_NOT_DOWN);
        }

        if (getParameters().isSoundDeviceEnabled() != null && !getVm().isDown()
                && vmDeviceChanged(VmDeviceGeneralType.SOUND, getParameters().isSoundDeviceEnabled())) {
            addCanDoActionMessage("$device sound");
            return failCanDoAction(VdcBllMessages.VM_CANNOT_UPDATE_DEVICE_VM_NOT_DOWN);
        }

        if (!isCpuSharesValid(vmFromParams)) {
            return failCanDoAction(VdcBllMessages.QOS_CPU_SHARES_OUT_OF_RANGE);
        }

        if (Boolean.TRUE.equals(getParameters().isVirtioScsiEnabled()) || isVirtioScsiEnabledForVm(getVmId())) {
            // Verify cluster compatibility
            if (!FeatureSupported.virtIoScsi(getVdsGroup().getcompatibility_version())) {
                return failCanDoAction(VdcBllMessages.VIRTIO_SCSI_INTERFACE_IS_NOT_AVAILABLE_FOR_CLUSTER_LEVEL);
            }

            // Verify OS compatibility
            if (!VmHandler.isOsTypeSupportedForVirtioScsi(vmFromParams.getOs(), getVdsGroup().getcompatibility_version(),
                    getReturnValue().getCanDoActionMessages())) {
                return false;
            }
        }

        if (Boolean.FALSE.equals(getParameters().isVirtioScsiEnabled())) {
            List<Disk> allDisks = getDiskDao().getAllForVm(getVmId(), true);
            for (Disk disk : allDisks) {
                if (disk.getDiskInterface() == DiskInterface.VirtIO_SCSI) {
                    return failCanDoAction(VdcBllMessages.CANNOT_DISABLE_VIRTIO_SCSI_PLUGGED_DISKS);
                }
            }
        }

        if (getParameters().isVirtioScsiEnabled() != null
                && !getVm().isDown()
                && vmDeviceChanged(VmDeviceGeneralType.CONTROLLER,
                        VmDeviceType.VIRTIOSCSI.getName(),
                        getParameters().isVirtioScsiEnabled())) {
            addCanDoActionMessage("$device VirtIO-SCSI");
            return failCanDoAction(VdcBllMessages.VM_CANNOT_UPDATE_DEVICE_VM_NOT_DOWN);
        }

        if (vmFromParams.getMinAllocatedMem() > vmFromParams.getMemSizeMb()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_EXCEED_MEMORY_SIZE);
        }

        if (isBalloonEnabled() && !getVdsGroup().isBalloonSupported()) {
            return failCanDoAction(VdcBllMessages.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH,
                    String.format("$clusterArch %1$s", getVdsGroup().getArchitecture()));
        }

        return true;
    }

    protected boolean isValidPciAndIdeLimit(VM vmFromParams) {
        List<Disk> allDisks = getDbFacade().getDiskDao().getAllForVm(getVmId());
        List<VmNic> interfaces = getVmNicDao().getAllForVm(getVmId());

        return checkPciAndIdeLimit(
                vmFromParams.getOs(),
                getVdsGroup().getcompatibility_version(),
                vmFromParams.getNumOfMonitors(),
                interfaces,
                allDisks,
                isVirtioScsiEnabled(),
                hasWatchdog(),
                getParameters().isBalloonEnabled(),
                isSoundDeviceEnabled(),
                getReturnValue().getCanDoActionMessages());
    }

    private boolean vmDeviceChanged(VmDeviceGeneralType deviceType, boolean deviceEnabled) {
        List<VmDevice> vmDevices = getVmDeviceDao().getVmDeviceByVmIdAndType(getParameters().getVmId(),
                deviceType);

        return deviceEnabled == vmDevices.isEmpty();
    }

    /**
     * Determines whether a VM device change has been request by the user.
     * @param deviceType VmDeviceGeneralType.
     * @param device VmDeviceType name.
     * @param deviceEnabled indicates whether the user asked to enable the device.
     * @return true if a change has been requested; otherwise, false
     */
    private boolean vmDeviceChanged(VmDeviceGeneralType deviceType, String device, boolean deviceEnabled) {
        List<VmDevice> vmDevices = getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(getParameters().getVmId(),
                deviceType, device);

        return deviceEnabled == vmDevices.isEmpty();
    }

    private boolean isVmExist() {
        return getParameters().getVmStaticData() != null && getVm() != null;
    }

    protected boolean areUpdatedFieldsLegal() {
        return VmHandler.isUpdateValid(getVm().getStaticData(),
                getParameters().getVmStaticData(),
                getVm().getStatus());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        if (isVmExist()) {
            // user need specific permission to change custom properties
            if (!StringUtils.equals(
                    getVm().getPredefinedProperties(),
                    getParameters().getVmStaticData().getPredefinedProperties())
                    || !StringUtils.equals(
                            getVm().getUserDefinedProperties(),
                            getParameters().getVmStaticData().getUserDefinedProperties())) {
                permissionList.add(new PermissionSubject(getParameters().getVmId(),
                        VdcObjectType.VM,
                        ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
            }

            // host-specific parameters can be changed by administration role only
            final boolean isDedicatedVmForVdsChanged =
                    !(getVm().getDedicatedVmForVds() == null ?
                            getParameters().getVmStaticData().getDedicatedVmForVds() == null :
                            getVm().getDedicatedVmForVds().equals(getParameters().getVmStaticData().getDedicatedVmForVds()));

            final boolean isCpuPinningChanged =
                    !(getVm().getCpuPinning() == null ?
                            getParameters().getVmStaticData().getCpuPinning() == null :
                            getVm().getCpuPinning().equals(getParameters().getVmStaticData().getCpuPinning()));

            if (isDedicatedVmForVdsChanged || isCpuPinningChanged) {
                permissionList.add(
                        new PermissionSubject(getParameters().getVmId(),
                                VdcObjectType.VM,
                                ActionGroup.EDIT_ADMIN_VM_PROPERTIES));
            }
        }

        return permissionList;
    }

    private void setCustomDefinedProperties(VmStatic vmStaticDataFromParams) {
        VMCustomProperties properties =
                VmPropertiesUtils.getInstance().parseProperties(getVdsGroup()
                        .getcompatibility_version(),
                        vmStaticDataFromParams.getCustomProperties());

        vmStaticDataFromParams.setPredefinedProperties(properties.getPredefinedProperties());
        vmStaticDataFromParams.setUserDefinedProperties(properties.getUseDefinedProperties());
    }

    @Override
    public Guid getVmId() {
        if (super.getVmId().equals(Guid.Empty)) {
            super.setVmId(getParameters().getVmStaticData().getId());
        }
        return super.getVmId();
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
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        // The cases must be persistent with the create_functions_sp
        if (!getQuotaManager().isVmStatusQuotaCountable(getVm().getStatus())) {
            list.add(new QuotaSanityParameter(getParameters().getVmStaticData().getQuotaId(), null));
            quotaSanityOnly = true;
        } else {
            if (getParameters().getVmStaticData().getQuotaId() == null
                    || getParameters().getVmStaticData().getQuotaId().equals(Guid.Empty)
                    || !getParameters().getVmStaticData().getQuotaId().equals(getVm().getQuotaId())) {
                list.add(new QuotaVdsGroupConsumptionParameter(getVm().getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        getVdsGroupId(),
                        getVm().getVmtCpuPerSocket() * getVm().getNumOfSockets(),
                        getVm().getMemSizeMb()));
                list.add(new QuotaVdsGroupConsumptionParameter(getParameters().getVmStaticData().getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.CONSUME,
                        getParameters().getVmStaticData().getVdsGroupId(),
                        getParameters().getVmStaticData().getCpuPerSocket()
                                * getParameters().getVmStaticData().getNumOfSockets(),
                        getParameters().getVmStaticData().getMemSizeMb()));
            }

        }
        return list;
    }
    @Override
    public String getEntityType() {
        return VdcObjectType.VM.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return oldVm.getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getVmStaticData().getName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {
       logable.setVmId(oldVm.getId());
    }
    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        // if only quota sanity is checked the user may use a quota he cannot consume
        // (it will be consumed only when the vm will run)
        if (!quotaSanityOnly) {
            super.addQuotaPermissionSubject(quotaPermissionList);
        }
    }

    protected boolean isVirtioScsiEnabled() {
        Boolean virtioScsiEnabled = getParameters().isVirtioScsiEnabled();
        return virtioScsiEnabled != null ? virtioScsiEnabled : isVirtioScsiEnabledForVm(getVmId());
    }

    public boolean isVirtioScsiEnabledForVm(Guid vmId) {
        return VmDeviceUtils.isVirtioScsiControllerAttached(vmId);
    }

    boolean isBalloonEnabled() {
        Boolean balloonEnabled = getParameters().isBalloonEnabled();
        return balloonEnabled != null ? balloonEnabled : VmDeviceUtils.isBalloonEnabled(getVmId());
    }

    protected boolean isSoundDeviceEnabled() {
        Boolean soundDeviceEnabled = getParameters().isSoundDeviceEnabled();
        return soundDeviceEnabled != null ? soundDeviceEnabled :
                VmDeviceUtils.isSoundDeviceEnabled(getVmId());
    }

    protected boolean hasWatchdog() {
        return getParameters().getWatchdog() != null;
    }

}
