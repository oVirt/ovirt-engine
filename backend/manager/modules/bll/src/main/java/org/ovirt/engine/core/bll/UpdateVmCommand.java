package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsGroupConsumptionParameter;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.HotSetNumerOfCpusParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.UpdateVm;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class UpdateVmCommand<T extends VmManagementParametersBase> extends VmManagementCommandBase<T>
        implements QuotaVdsDependent, RenamedEntityInfoProvider{
    private VM oldVm;
    private boolean quotaSanityOnly = false;
    private VmStatic newVmStatic;
    private VdcReturnValueBase setNumberOfCpusResult;

    public UpdateVmCommand(T parameters) {
        this(parameters, null);
    }

    public UpdateVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        if (getVdsGroup() != null) {
            setStoragePoolId(getVdsGroup().getStoragePoolId());
        }

        if (isVmExist()) {
            Version clusterVersion = getVdsGroup().getcompatibility_version();
            getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(clusterVersion, parameters.getVmStaticData());
            getVmPropertiesUtils().separateCustomPropertiesToUserAndPredefined(clusterVersion, getVm().getStaticData());
        }
        VmHandler.updateDefaultTimeZone(parameters.getVmStaticData());

        updateParametersVmFromInstanceType();
    }


    private VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return isInternalExecution() ?
                getSucceeded() ? AuditLogType.SYSTEM_UPDATE_VM : AuditLogType.SYSTEM_FAILED_UPDATE_VM
                : getSucceeded() ? AuditLogType.USER_UPDATE_VM : AuditLogType.USER_FAILED_UPDATE_VM;
    }

    @Override
    protected void executeVmCommand() {
        if (isRunningConfigurationNeeded()) {
            createNextRunSnapshot();
        }

        oldVm = getVm();
        VmHandler.warnMemorySizeLegal(getParameters().getVm().getStaticData(), getVdsGroup().getcompatibility_version());
        getVmStaticDAO().incrementDbGeneration(getVm().getId());
        newVmStatic = getParameters().getVmStaticData();
        newVmStatic.setCreationDate(oldVm.getStaticData().getCreationDate());

        // save user selected value for hotplug before overriding with db values (when updating running vm)
        int cpuPerSocket = newVmStatic.getCpuPerSocket();
        int numOfSockets = newVmStatic.getNumOfSockets();

        if (newVmStatic.getCreationDate().equals(DateTime.getMinValue())) {
            newVmStatic.setCreationDate(new Date());
        }

        if (getVm().isRunningOrPaused()) {
            if (!VmHandler.copyNonEditableFieldsToDestination(oldVm.getStaticData(), newVmStatic)) {
                // fail update vm if some fields could not be copied
                throw new VdcBLLException(VdcBllErrors.FAILED_UPDATE_RUNNING_VM);
            }

        }

        UpdateVmNetworks();
        if (!getParameters().isApplyChangesLater()) {
            hotSetCpus(cpuPerSocket, numOfSockets);
        }
        getVmStaticDAO().update(newVmStatic);
        if (getVm().isNotRunning()) {
            updateVmPayload();
            VmDeviceUtils.updateVmDevices(getParameters(), oldVm);
            updateWatchdog();
            updateRngDevice();
        }
        VmHandler.updateVmInitToDB(getParameters().getVmStaticData());

        checkTrustedService();
        setSucceeded(true);
    }

    private boolean updateRngDevice() {
        // do not update if this flag is not set
        if (getParameters().isUpdateRngDevice()) {
            VdcQueryReturnValue query =
                    runInternalQuery(VdcQueryType.GetRngDevice, new IdQueryParameters(getParameters().getVmId()));

            @SuppressWarnings("unchecked")
            List<VmRngDevice> rngDevs = query.getReturnValue();

            VdcReturnValueBase rngCommandResult = null;
            if (rngDevs.isEmpty()) {
                if (getParameters().getRngDevice() != null) {
                    RngDeviceParameters params = new RngDeviceParameters(getParameters().getRngDevice(), true);
                    rngCommandResult = runInternalAction(VdcActionType.AddRngDevice, params, cloneContextAndDetachFromParent());
                }
            } else {
                if (getParameters().getRngDevice() == null) {
                    RngDeviceParameters params = new RngDeviceParameters(rngDevs.get(0), true);
                    rngCommandResult = runInternalAction(VdcActionType.RemoveRngDevice, params, cloneContextAndDetachFromParent());
                } else {
                    RngDeviceParameters params = new RngDeviceParameters(getParameters().getRngDevice(), true);
                    params.getRngDevice().setDeviceId(rngDevs.get(0).getDeviceId());
                    rngCommandResult = runInternalAction(VdcActionType.UpdateRngDevice, params, cloneContextAndDetachFromParent());
                }
            }

            if (rngCommandResult != null && !rngCommandResult.getSucceeded()) {
                return false;
            }
        }

        return true;
    }

    private void createNextRunSnapshot() {
        // first remove existing snapshot
        Snapshot runSnap = getSnapshotDao().get(getVmId(), Snapshot.SnapshotType.NEXT_RUN);
        if (runSnap != null) {
            getSnapshotDao().remove(runSnap.getId());
        }

        VM vm = new VM();
        vm.setStaticData(getParameters().getVmStaticData());

        // create new snapshot with new configuration
        new SnapshotsManager().addSnapshot(Guid.newGuid(),
                "Next Run configuration snapshot",
                Snapshot.SnapshotStatus.OK,
                Snapshot.SnapshotType.NEXT_RUN,
                vm,
                true,
                StringUtils.EMPTY,
                Collections.EMPTY_LIST,
                VmDeviceUtils.getVmDevicesForNextRun(getVm(), getParameters()),
                getCompensationContext());
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    private void hotSetCpus(int cpuPerSocket, int newNumOfSockets) {
        int currentSockets = getVm().getNumOfSockets();
        int currentCpuPerSocket = getVm().getCpuPerSocket();

        // try hotplug only if topology (cpuPerSocket) hasn't changed
        if (getVm().getStatus() == VMStatus.Up && currentSockets != newNumOfSockets
                && currentCpuPerSocket == cpuPerSocket) {
            HotSetNumerOfCpusParameters params =
                    new HotSetNumerOfCpusParameters(
                            newVmStatic,
                            currentSockets < newNumOfSockets ? PlugAction.PLUG : PlugAction.UNPLUG);
            setNumberOfCpusResult =
                    runInternalAction(
                            VdcActionType.HotSetNumberOfCpus,
                            params, cloneContextAndDetachFromParent());
            newVmStatic.setNumOfSockets(setNumberOfCpusResult.getSucceeded() ? newNumOfSockets : currentSockets);
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
                    runInternalQuery(VdcQueryType.GetWatchdog, new IdQueryParameters(getParameters().getVmId()));
            List<VmWatchdog> watchdogs = query.getReturnValue();
            if (watchdogs.isEmpty()) {
                if (getParameters().getWatchdog() == null) {
                    // nothing to do, no watchdog and no watchdog to create
                } else {
                    WatchdogParameters parameters = new WatchdogParameters();
                    parameters.setId(getParameters().getVmId());
                    parameters.setAction(getParameters().getWatchdog().getAction());
                    parameters.setModel(getParameters().getWatchdog().getModel());
                    runInternalAction(VdcActionType.AddWatchdog, parameters, cloneContextAndDetachFromParent());
                }
            } else {
                WatchdogParameters watchdogParameters = new WatchdogParameters();
                watchdogParameters.setId(getParameters().getVmId());
                if (getParameters().getWatchdog() == null) {
                    // there is a watchdog in the vm, there should not be any, so let's delete
                    runInternalAction(VdcActionType.RemoveWatchdog, watchdogParameters, cloneContextAndDetachFromParent());
                } else {
                    // there is a watchdog in the vm, we have to update.
                    watchdogParameters.setAction(getParameters().getWatchdog().getAction());
                    watchdogParameters.setModel(getParameters().getWatchdog().getModel());
                    runInternalAction(VdcActionType.UpdateWatchdog, watchdogParameters, cloneContextAndDetachFromParent());
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
        if (!Objects.equals(getVm().getVdsGroupId(), getParameters().getVmStaticData().getVdsGroupId())) {
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
        if (!super.canDoAction()) {
            return false;
        }

        VM vmFromDB = getVm();
        VM vmFromParams = getParameters().getVm();

        if (getVdsGroup() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
        }

        if (vmFromDB.getVdsGroupId() == null) {
            failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
        }

        if (!isVmExist()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (StringUtils.isEmpty(vmFromParams.getName())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
        }

        // check that VM name is not too long
        boolean vmNameValidLength = isVmNameValidLength(vmFromParams);
        if (!vmNameValidLength) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
        }

        // Checking if a desktop with same name already exists
        if (!StringUtils.equals(vmFromDB.getName(), vmFromParams.getName())) {
            boolean exists = isVmWithSameNameExists(vmFromParams.getName());

            if (exists) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
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
            return failCanDoAction(VdcBllMessages.VM_CANNOT_UPDATE_ILLEGAL_FIELD);
        }

        if (!vmFromDB.getVdsGroupId().equals(vmFromParams.getVdsGroupId())) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_UPDATE_CLUSTER);
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
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_FROM_POOL_CANNOT_BE_STATELESS);
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

        if (!FeatureSupported.isMigrationSupported(getVdsGroup().getArchitecture(), getVdsGroup().getcompatibility_version())
                && vmFromParams.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
            return failCanDoAction(VdcBllMessages.VM_MIGRATION_IS_NOT_SUPPORTED);
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

        VmValidator vmValidator = createVmValidator(vmFromParams);
        if (Boolean.FALSE.equals(getParameters().isVirtioScsiEnabled()) && !validate(vmValidator.canDisableVirtioScsi(null))) {
            return false;
        }

        if (vmFromParams.getMinAllocatedMem() > vmFromParams.getMemSizeMb()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_EXCEED_MEMORY_SIZE);
        }

        if (!setAndValidateCpuProfile()) {
            return false;
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
                isBalloonEnabled(),
                isSoundDeviceEnabled(),
                getReturnValue().getCanDoActionMessages());
    }

    private boolean isVmExist() {
        return getParameters().getVmStaticData() != null && getVm() != null;
    }

    protected boolean areUpdatedFieldsLegal() {
        return VmHandler.isUpdateValid(getVm().getStaticData(),
                getParameters().getVmStaticData(),
                VMStatus.Down);
    }

    /**
     * check if we need to use running-configuration
     * @return true if vm is running and we change field that has @EditableOnVmStatusField annotation
     *          or runningConfiguration already exist
     */
    private boolean isRunningConfigurationNeeded() {
        return getVm().isNextRunConfigurationExists() ||
                !VmHandler.isUpdateValid(getVm().getStaticData(),
                        getParameters().getVmStaticData(),
                        getVm().getStatus(),
                        !getParameters().isApplyChangesLater()) ||
                !VmHandler.isUpdateValidForVmDevices(getVmId(), getVm().getStatus(), getParameters());
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
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_NAME, VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_BEING_UPDATED));
        }
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(
                getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.VM,
                        VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_BEING_UPDATED));
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

    private boolean isBalloonEnabled() {
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

    public VmValidator createVmValidator(VM vm) {
        return new VmValidator(vm);
    }
}
