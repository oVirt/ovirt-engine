package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.migration.ConvergenceConfigProvider;
import org.ovirt.engine.core.bll.migration.ConvergenceSchedule;
import org.ovirt.engine.core.bll.scheduling.VdsFreeMemoryChecker;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.MultipleVmsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.MigrationMethod;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.vdscommands.MigrateStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class MigrateVmCommand<T extends MigrateVmParameters> extends RunVmCommandBase<T> {

    private Logger log = LoggerFactory.getLogger(MigrateVmCommand.class);

    @Inject
    ConvergenceConfigProvider convergenceConfigProvider;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    /** The VDS that the VM is going to migrate to */
    private VDS destinationVds;

    /** Used to log the migration error. */
    private EngineError migrationErrorCode;

    private Integer actualDowntime;

    private List<VmNetworkInterface> cachedVmPassthroughNics;
    private boolean passthroughNicsUnplugged;

    public MigrateVmCommand(T migrateVmParameters, CommandContext cmdContext) {
        super(migrateVmParameters, cmdContext);

        if (migrateVmParameters.getTargetClusterId() != null) {
            setClusterId(migrateVmParameters.getTargetClusterId());
            // force reload
            setCluster(null);
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    /**
     * this property is used for audit log events
     */
    public final String getDestinationVdsName() {
        VDS destinationVds = getDestinationVds();
        return destinationVds != null ? destinationVds.getName() : null;
    }

    /**
     * @return Migration error text which is used in audit log message, if the migration status was queried from VDSM.
     */
    public String getDueToMigrationError() {
        if (migrationErrorCode == null) {
            return " ";
        }

        return " due to Error: " + Backend.getInstance()
                .getVdsErrorsTranslator()
                .translateErrorTextSingle(migrationErrorCode.name(), true);
    }

    /**
     * Returns the VDS that the VM is about to migrate to
     */
    protected VDS getDestinationVds() {
        return destinationVds;
    }

    @Override
    protected void processVmOnDown() {
        // In case the migration failed and the VM turned back to Up in the
        // source, we don't need to handle it as a VM that failed to run
        if (getVm().getStatus() != VMStatus.Up) {
            super.processVmOnDown();
        }
    }

    protected boolean initVdss() {
        try {
            setVdsIdRef(getVm().getRunOnVds());
            Optional<Guid> vdsToRunOn = getVdsToRunOn();
            setDestinationVdsId(vdsToRunOn.orElse(null));

            if (vdsToRunOn.isPresent()) {
                getRunVdssList().add(vdsToRunOn.get());
            }

            vmHandler.updateVmGuestAgentVersion(getVm());

            if (!vdsToRunOn.isPresent()) {
                return false;
            }

            if (getDestinationVds() == null || getVds() == null) {
                return false;
            }

            return true;
        } catch (Exception e) {
            cleanupPassthroughVnics(getDestinationVdsId());
            throw e;
        }
    }

    private Optional<Guid> getVdsToRunOn() {
        List<String> messages = new ArrayList<>();
        Optional<Guid> vdsToRunOn =
                schedulingManager.schedule(getCluster(),
                        getVm(),
                        getVdsBlackList(),
                        getVdsWhiteList(),
                        getDestinationHostList(),
                        messages,
                        new VdsFreeMemoryChecker(this),
                        getCorrelationId());
        messages.forEach(this::addValidationMessage);
        return vdsToRunOn;
    }

    private List<Guid> getDestinationHostList() {
        List<Guid> destinationHostGuidList = new LinkedList<>();
        if (getDestinationVdsId() != null){
            destinationHostGuidList.add(getDestinationVdsId());
        }
        return destinationHostGuidList;
    }

    @Override
    protected void executeVmCommand() {
        getVmManager().getStatistics().setMigrationProgressPercent(0);
        setSucceeded(initVdss() && perform());
    }

    /**
     * all found passhthrough nics related to VM being migrated. We need to store them, so we can unplug them before
     * migration and re-plug them back after migration.
     */
    private List<VmNetworkInterface> getAllVmPassthroughNics() {
        if (cachedVmPassthroughNics == null) {
            cachedVmPassthroughNics = vmNetworkInterfaceDao.getAllForVm(getVmId()).stream()
                    .filter(vnic -> vnic.isPassthrough() && vnic.isPlugged())
                    .collect(Collectors.toList());
            log.debug("Performing migration with following passthrough nics: {}", cachedVmPassthroughNics);
        }
        return cachedVmPassthroughNics;
    }

    protected boolean perform() {
        try {
            getParameters().setStartTime(new Date());

            if (unplugPassthroughNics() && connectLunDisks(getDestinationVdsId()) && migrateVm()) {
                ExecutionHandler.setAsyncJob(getExecutionContext(), true);
                return true;
            }

            // otherwise
            runningFailed();
            return false;
        } catch (Exception e) {
            runningFailed();
            throw e;
        }
    }

    /**
     * When unplugging fails for any nic, we stop immediately. In that case we won't proceed with migration, and thus
     * it makes sense to stop asap to create minimum problems which needs to be fixed manually.
     *
     * @return false if unplugging failed.
     */
    private boolean unplugPassthroughNics() {
        if (passthroughNicsUnplugged) {
            // no need to unplug more than once
            return true;
        }
        List<ActivateDeactivateVmNicParameters> parametersList = createActivateDeactivateVmNicParameters(
                getAllVmPassthroughNics(),
                PlugAction.UNPLUG);

        log.debug("About to call {} with parameters: {}",
                VdcActionType.ActivateDeactivateVmNic,
                Arrays.toString(parametersList.toArray()));

        for (ActivateDeactivateVmNicParameters parameter : parametersList) {
            VdcReturnValueBase returnValue = runInternalAction(VdcActionType.ActivateDeactivateVmNic, parameter);
            if (!returnValue.getSucceeded()) {
                returnValue.getValidationMessages().forEach(this::addValidationMessage);
                return false;
            }
        }
        passthroughNicsUnplugged = true;
        return true;
    }

    /**
     * This method is called when migration succeeded, and we're plugging vmnics back. If there some error with plugging
     * any of them back, we will create least problem, if we try to plug back each of them. Also for each such failure,
     * we must release preallocated VF.
     */
    private void plugPassthroughNics() {
        try {
            List<ActivateDeactivateVmNicParameters> parametersList = createActivateDeactivateVmNicParameters(
                    getAllVmPassthroughNics(),
                    PlugAction.PLUG);

            boolean plugOfAllMacsSucceeded = true;
            log.debug("About to call {} with parameters: {}",
                    VdcActionType.ActivateDeactivateVmNic,
                    Arrays.toString(parametersList.toArray()));
            Map<Guid, String> vnicToVfMap = getVnicToVfMap(getDestinationVdsId());
            List<VmNic> notRepluggedNics = new ArrayList<>();

            for (ActivateDeactivateVmNicParameters parameter : parametersList) {
                VdcReturnValueBase returnValue = runInternalAction(VdcActionType.ActivateDeactivateVmNic, parameter);

                boolean nicPlugSucceeded = returnValue.getSucceeded();
                plugOfAllMacsSucceeded &= nicPlugSucceeded;
                if (!nicPlugSucceeded) {
                    notRepluggedNics.add(parameter.getNic());
                }
            }

            if (!plugOfAllMacsSucceeded) {
                Set<String> vfsToUnregister = notRepluggedNics.stream()
                        .map(VmNic::getId)
                        .map(vnicToVfMap::get)
                        .collect(Collectors.toSet());
                networkDeviceHelper.setVmIdOnVfs(getDestinationVdsId(), null, vfsToUnregister);

                addCustomValue("NamesOfNotRepluggedNics",
                        notRepluggedNics.stream().map(VmNic::getName).collect(Collectors.joining(",")));
                auditLogDirector.log(this, AuditLogType.VM_MIGRATION_NOT_ALL_VM_NICS_WERE_PLUGGED_BACK);
            }

        } catch(Exception e) {
            auditLogDirector.log(this, AuditLogType.VM_MIGRATION_PLUGGING_VM_NICS_FAILED);
            log.error("Failed to plug nics back after migration of vm {}: {}", getVmName(), e.getMessage());
            log.debug("Exception: ", e);
        }
    }

    private List<ActivateDeactivateVmNicParameters> createActivateDeactivateVmNicParameters(List<VmNetworkInterface> vmNics,
            PlugAction plugAction) {
        return vmNics
                .stream()
                .map(vmNic -> createActivateDeactivateVmNicParameters(vmNic, plugAction))
                .collect(Collectors.toList());
    }

    private ActivateDeactivateVmNicParameters createActivateDeactivateVmNicParameters(VmNic nic,
            PlugAction plugAction) {
        ActivateDeactivateVmNicParameters parameters = new ActivateDeactivateVmNicParameters(nic, plugAction, false);
        parameters.setVmId(getParameters().getVmId());
        return parameters;
    }

    private boolean migrateVm() {
        setActionReturnValue(getVdsBroker()
                .runAsyncVdsCommand(
                        VDSCommandType.Migrate,
                        createMigrateVDSCommandParameters(),
                        this)
                .getReturnValue());

        return getActionReturnValue() == VMStatus.MigratingFrom;
    }

    private MigrateVDSCommandParameters createMigrateVDSCommandParameters() {
        String srcVdsHost = getVds().getHostName();
        String dstVdsHost = String.format("%1$s:%2$s",
                getDestinationVds().getHostName(),
                getDestinationVds().getPort());
        Map<String, Object> convergenceSchedule = null;
        Integer maxBandwidth = null;

        Boolean autoConverge = getAutoConverge();
        Boolean migrateCompressed = getMigrateCompressed();
        Boolean enableGuestEvents = null;
        Integer maxIncomingMigrations = null;
        Integer maxOutgoingMigrations = null;

        if (FeatureSupported.migrationPoliciesSupported(getVm().getCompatibilityVersion())) {
            MigrationPolicy clusterMigrationPolicy = convergenceConfigProvider.getMigrationPolicy(
                    getCluster().getMigrationPolicyId(), getCluster().getCompatibilityVersion());
            MigrationPolicy effectiveMigrationPolicy = findEffectiveConvergenceConfig(clusterMigrationPolicy);
            convergenceSchedule = ConvergenceSchedule.from(effectiveMigrationPolicy.getConfig()).asMap();

            maxBandwidth = getMaxBandwidth(clusterMigrationPolicy);
            if (!NoMigrationPolicy.ID.equals(effectiveMigrationPolicy.getId())) {
                autoConverge = effectiveMigrationPolicy.isAutoConvergence();
                migrateCompressed = effectiveMigrationPolicy.isMigrationCompression();
            }
            enableGuestEvents = effectiveMigrationPolicy.isEnableGuestEvents();

            maxIncomingMigrations = maxOutgoingMigrations = effectiveMigrationPolicy.getMaxMigrations();
        }

        return new MigrateVDSCommandParameters(getVdsId(),
                getVmId(),
                srcVdsHost,
                getDestinationVdsId(),
                dstVdsHost,
                MigrationMethod.ONLINE,
                isTunnelMigrationUsed(),
                getLiteralMigrationNetworkIp(),
                getVds().getClusterCompatibilityVersion(),
                getMaximumMigrationDowntime(),
                autoConverge,
                migrateCompressed,
                getDestinationVds().getConsoleAddress(),
                maxBandwidth,
                convergenceSchedule,
                enableGuestEvents,
                maxIncomingMigrations,
                maxOutgoingMigrations);
    }

    /**
     * @return Maximum bandwidth of each migration in cluster in Mbps, `null` indicates that value in VDSM configuration
     * on source host should be used.
     *
     * @see org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType
     */
    private Integer getMaxBandwidth(MigrationPolicy migrationPolicy) {
        switch (getCluster().getMigrationBandwidthLimitType()) {
            case AUTO:
                return Optional.ofNullable(getAutoMaxBandwidth())
                        .map(bandwidth -> bandwidth / migrationPolicy.getMaxMigrations())
                        .orElse(null);
            case VDSM_CONFIG:
                return null;
            case CUSTOM:
                return getCluster().getCustomMigrationNetworkBandwidth() / migrationPolicy.getMaxMigrations();
            default:
                throw new IllegalStateException(
                        "Unexpected enum item: " + getCluster().getMigrationBandwidthLimitType());
        }
    }

    private Integer getAutoMaxBandwidth() {
        final Guid sourceClusterId = getVm().getClusterId();
        final Guid destinationClusterId = getClusterId();

        final Guid sourceHostId = getVm().getRunOnVds();
        final Guid destinationHostId = getDestinationVdsId();

        return ObjectUtils.minIfExists(
                getAutoMaxBandwidth(sourceClusterId, sourceHostId),
                getAutoMaxBandwidth(destinationClusterId, destinationHostId));
    }

    /**
     * @return `null` if it can't be computed, value in Mbps otherwise
     */
    private Integer getAutoMaxBandwidth(Guid clusterId, Guid hostId) {
        Integer qosBandwidth = getQosBandwidth(clusterId);
        if (qosBandwidth != null) {
            return qosBandwidth;
        }
        return getLinkSpeedBandwidth(hostId);
    }

    /**
     * Note: Even if there is a QoS associated with migrational network, it may not contain neither
     * {@link HostNetworkQos#outAverageRealtime} nor {@link HostNetworkQos#outAverageUpperlimit} property.
     * @return `null` if it can't be obtained, value in Mbps otherwise
     */
    private Integer getQosBandwidth(Guid clusterId) {
        final HostNetworkQos migrationHostNetworkQos = getDbFacade().getHostNetworkQosDao()
                .getHostNetworkQosOfMigrationNetworkByClusterId(clusterId);
        if (migrationHostNetworkQos == null) {
            return null;
        }
        if (migrationHostNetworkQos.getOutAverageRealtime() != null) {
            return migrationHostNetworkQos.getOutAverageRealtime();
        }
        return migrationHostNetworkQos.getOutAverageUpperlimit();
    }

    /**
     * Link speed of host network interface that is connected to migration network.
     * @return value in Mbps otherwise, `null` if it can't be computed (e.g. virtio NIC can't
     *         report its speed)
     */
    private Integer getLinkSpeedBandwidth(Guid hostId) {
        return interfaceDao.getActiveMigrationNetworkInterfaceForHost(hostId)
                .map(NetworkInterface::getSpeed)
                .map(speed -> speed > 0 ? speed : null)
                .orElse(null);
    }

    private MigrationPolicy findEffectiveConvergenceConfig(MigrationPolicy clusterMigrationPolicy) {
        Guid overriddenPolicyId = getVm().getMigrationPolicyId();
        if (overriddenPolicyId == null) {
            return clusterMigrationPolicy;
        }

        return convergenceConfigProvider.getMigrationPolicy(overriddenPolicyId, getCluster().getCompatibilityVersion());
    }

    @Override
    public void runningSucceded() {
        try {
            getDowntime();
            vmDynamicDao.clearMigratingToVds(getVmId());
            updateVmAfterMigrationToDifferentCluster();
            plugPassthroughNics();
        }
        finally {
            super.runningSucceded();
        }
    }

    @Override
    protected void runningFailed() {
        try {
            //this will clean all VF reservations made in {@link #initVdss}.
            cleanupPassthroughVnics(getDestinationVdsId());
        } finally {
            super.runningFailed();
        }
    }

    protected void getDowntime() {
        try {
            VDSReturnValue retVal = runVdsCommand(VDSCommandType.MigrateStatus,
                    new MigrateStatusVDSCommandParameters(getDestinationVdsId(), getVmId()));
            if (retVal != null) {
                actualDowntime = (Integer) retVal.getReturnValue();
            }
        } catch (EngineException e) {
            migrationErrorCode = e.getErrorCode();
        }
    }

    private void updateVmAfterMigrationToDifferentCluster() {
        if (getVm().getClusterId().equals(getParameters().getTargetClusterId())) {
            return;
        }

        ChangeVMClusterParameters params = new ChangeVMClusterParameters(
                getParameters().getTargetClusterId(),
                getVmId(),
                getVm().getCustomCompatibilityVersion());
        setSucceeded(getBackend().runInternalAction(VdcActionType.ChangeVMCluster, params).getSucceeded());
    }

    private Boolean getAutoConverge() {
        if (getVm().getAutoConverge() != null) {
            return getVm().getAutoConverge();
        }

        if (getCluster().getAutoConverge() != null) {
            return getCluster().getAutoConverge();
        }

        return Config.getValue(ConfigValues.DefaultAutoConvergence);
    }

    private Boolean getMigrateCompressed() {
        if (getVm().getMigrateCompressed() != null) {
            return getVm().getMigrateCompressed();
        }

        if (getCluster().getMigrateCompressed() != null) {
            return getCluster().getMigrateCompressed();
        }

        return Config.getValue(ConfigValues.DefaultMigrationCompression);
    }

    private int getMaximumMigrationDowntime() {
        if (getVm().getMigrationDowntime() != null) {
            return getVm().getMigrationDowntime();
        }

        return Config.getValue(ConfigValues.DefaultMaximumMigrationDowntime);
    }

    private boolean isTunnelMigrationUsed() {
        // if vm has no override for tunnel migration (its null),
        // use cluster's setting
        return getVm().getTunnelMigration() != null ?
                getVm().getTunnelMigration()
                : getCluster().isTunnelMigration();
    }

    private String getLiteralMigrationNetworkIp() {
        Network migrationNetwork = null;

        // Find migrationNetworkCluster
        List<Network> allNetworksInCluster = networkDao.getAllForCluster(getVm().getClusterId());

        for (Network tempNetwork : allNetworksInCluster) {
            if (tempNetwork.getCluster().isMigration()) {
                migrationNetwork = tempNetwork;
                break;
            }
        }

        if (migrationNetwork != null) {

            final String migrationDestinationIpv4Address =
                    findValidMigrationIpAddress(migrationNetwork, VdsNetworkInterface::getIpv4Address);
            if (migrationDestinationIpv4Address != null) {
                return migrationDestinationIpv4Address;
            }
            final String migrationDestinationIpv6Address =
                    findValidMigrationIpAddress(migrationNetwork, VdsNetworkInterface::getIpv6Address);
            if (migrationDestinationIpv6Address != null) {
                return formatIpv6AddressForUri(migrationDestinationIpv6Address);
            }
        }
        return null;
    }

    private String formatIpv6AddressForUri(String migrationDestinationIpv6Address) {
        if (FeatureSupported.isIpv6MigrationProperlyHandled(getCluster().getCompatibilityVersion())) {
            return migrationDestinationIpv6Address;
        } else {
            return String.format("[%s]", migrationDestinationIpv6Address);
        }
    }

    private String findValidMigrationIpAddress(Network migrationNetwork,
            Function<VdsNetworkInterface, String> ipAddressGetter) {

        // assure migration network is active on source host
        final String migrationSourceIpAddress = getMigrationNetworkAddress(getVds().getId(),
                migrationNetwork.getName(),
                ipAddressGetter);
        if (StringUtils.isNotEmpty(migrationSourceIpAddress)) {
            // find migration IP address on destination host
            final String migrationDestinationIpAddress = getMigrationNetworkAddress(getDestinationVds().getId(),
                    migrationNetwork.getName(),
                    ipAddressGetter);
            if (StringUtils.isNotEmpty(migrationDestinationIpAddress)) {
                return migrationDestinationIpAddress;
            }
        }
        return null;
    }

    private String getMigrationNetworkAddress(Guid hostId,
            String migrationNetworkName,
            Function<VdsNetworkInterface, String> ipAddressGetter) {
        final List<VdsNetworkInterface> nics = interfaceDao.getAllInterfacesForVds(hostId);

        for (VdsNetworkInterface nic : nics) {
            if (migrationNetworkName.equals(nic.getNetworkName()) && migrationInterfaceUp(nic, nics)) {
                return ipAddressGetter.apply(nic);
            }
        }

        return null;
    }

    protected boolean migrationInterfaceUp(VdsNetworkInterface nic, List<VdsNetworkInterface> nics) {
        if (NetworkCommonUtils.isVlan(nic)) {
            String physicalNic = nic.getBaseInterface();
            for (VdsNetworkInterface iface : nics) {
                if (iface.getName().equals(physicalNic)) {
                    return iface.getStatistics().getStatus() == InterfaceStatus.UP;
                }
            }
        }

        return nic.getStatistics().getStatus() == InterfaceStatus.UP;
    }

    /**
     * command succeeded and VM is up => migration done
     * command succeeded and VM is not up => migration started
     * command failed and rerun flag is set => rerun migration was initiated
     * command failed and rerun flag is not set => migration failed
     */
    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ?
                getActionReturnValue() == VMStatus.Up ?
                        AuditLogType.VM_MIGRATION_DONE
                        : getAuditLogForMigrationStarted()
                : _isRerun ?
                        AuditLogType.VM_MIGRATION_TRYING_RERUN
                        : getAuditLogForMigrationFailure();
    }

    private AuditLogType getAuditLogForMigrationStarted() {
        addCustomValue("OptionalReason", getParameters().getReason());
        return isInternalExecution() ?
                AuditLogType.VM_MIGRATION_START_SYSTEM_INITIATED
                : AuditLogType.VM_MIGRATION_START;
    }

    protected AuditLogType getAuditLogForMigrationFailure() {
        if (getVds().getStatus() == VDSStatus.PreparingForMaintenance) {
            return AuditLogType.VM_MIGRATION_FAILED_DURING_MOVE_TO_MAINTENANCE;
        }

        if (getDestinationVds() == null) {
            auditLogDirector.log(this, AuditLogType.VM_MIGRATION_NO_VDS_TO_MIGRATE_TO);
        }

        return AuditLogType.VM_MIGRATION_FAILED;
    }

    protected Guid getDestinationVdsId() {
        VDS destinationVds = getDestinationVds();
        return destinationVds != null ? destinationVds.getId() : null;
    }

    protected void setDestinationVdsId(Guid vdsId) {
        destinationVds = vdsId != null ? vdsDao.get(vdsId) : null;
    }

    @Override
    protected boolean validate() {
        final VM vm = getVm();

        if (vm == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        VmValidator vmValidator = getVmValidator();
        if (!validate(vmValidator.isVmPluggedDiskNotUsingScsiReservation())) {
            return false;
        }

        if (!FeatureSupported.isMigrationSupported(getCluster().getArchitecture(), getCluster().getCompatibilityVersion())) {
            return failValidation(EngineMessage.MIGRATION_IS_NOT_SUPPORTED);
        }

        // If VM is pinned to host, no migration can occur
        if (vm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_PINNED_TO_HOST);
        }

        if (vm.getMigrationSupport() == MigrationSupport.IMPLICITLY_NON_MIGRATABLE
                && !getParameters().isForceMigrationForNonMigratableVm()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NON_MIGRTABLE_AND_IS_NOT_FORCED_BY_USER_TO_MIGRATE);
        }

        switch (vm.getStatus()) {
        case MigratingFrom:
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS);

        case NotResponding:
            return failVmStatusIllegal();

        case Paused:
            if (vm.getVmPauseStatus() == VmPauseStatus.EIO) {
                return failValidation(EngineMessage.MIGRATE_PAUSED_EIO_VM_IS_NOT_SUPPORTED);
            }
            break;

        default:
        }

        if (!vm.isQualifyToMigrate()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING);
        }

        if (!validate(new MultipleVmsValidator(vm).vmNotHavingPluggedDiskSnapshots(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_PLUGGED_DISK_SNAPSHOT))
                || !validate(vmValidator.allPassthroughVnicsMigratable())) {
            return false;
        }

        if (getParameters().getTargetClusterId() != null) {
            ChangeVmClusterValidator changeVmClusterValidator = ChangeVmClusterValidator.create(
                    this,
                    getParameters().getTargetClusterId(),
                    getVm().getCustomCompatibilityVersion());
            if (!changeVmClusterValidator.validate()) {
                return false;
            }
        }

        if (isInternalExecution() && getVm().getMigrationSupport() != MigrationSupport.MIGRATABLE) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NON_MIGRTABLE);
        }

        return validate(snapshotsValidator.vmNotDuringSnapshot(vm.getId()))
                // This check was added to prevent migration of VM while its disks are being migrated
                // TODO: replace it with a better solution
                && validate(new DiskImagesValidator(
                        DisksFilter.filterImageDisks(diskDao.getAllForVm(vm.getId(), true),
                                ONLY_NOT_SHAREABLE,
                                ONLY_ACTIVE)).diskImagesNotLocked())
                && schedulingManager.canSchedule(getCluster(),
                        getVm(),
                        getVdsBlackList(),
                        getVdsWhiteList(),
                        getReturnValue().getValidationMessages());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__MIGRATE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    public void rerun() {
         // make Vm property to null in order to refresh it from db
        setVm(null);

        determineMigrationFailureForAuditLog();

        // if vm is up and rerun is called then it got up on the source, try to rerun
        if (getVm() != null && getVm().getStatus() == VMStatus.Up) {
            // this will clean all VF reservations made in {@link #initVdss}.
            cleanupPassthroughVnics(getDestinationVdsId());
            super.rerun();
        } else {
            // vm went down on the destination and source, migration failed.
            runningFailed();
            // signal the caller that a rerun was made so that it won't log
            // the failure message again
            _isRerun = true;
        }
    }

    @Override
    protected void reexecuteCommand() {
        setDestinationVdsId(null);
        super.reexecuteCommand();
    }

    /**
     * Log that the migration had failed with the error code that is in the VDS and needs to be retrieved.
     */
    protected void determineMigrationFailureForAuditLog() {
        if (getVm() != null && getVm().getStatus() == VMStatus.Up) {
            try {
                runVdsCommand(VDSCommandType.MigrateStatus, new MigrateStatusVDSCommandParameters(getVdsId(), getVmId()));
            } catch (EngineException e) {
                migrationErrorCode = e.getErrorCode();
            }
        }
    }

    @Override
    protected Guid getCurrentVdsId() {
        Guid destinationVdsId = getDestinationVdsId();
        return destinationVdsId != null ? destinationVdsId : super.getCurrentVdsId();
    }

    // Duration: time that took for the actual migration
    public String getDuration() {
        return DurationFormatUtils.formatDurationWords(new Date().getTime() - getParameters().getStartTime().getTime(), true, true);
    }

    // TotalDuration: time that took migration including retries (can be identical to Duration)
    public String getTotalDuration() {
        return DurationFormatUtils.formatDurationWords(new Date().getTime() - getParameters().getTotalMigrationTime().getTime(), true, true);
    }

    // ActualDowntime: returns the actual time that the vm was offline (not available for access)
    public String getActualDowntime() {
        return (actualDowntime == null) ? "(N/A)" : actualDowntime + "ms";
    }

    @Override
    protected String getLockMessage() {
        StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_MIGRATED.name());
        builder.append(String.format("$VmName %1$s", getVmName()));
        return builder.toString();
    }

    // hosts that cannot be selected for scheduling (failed hosts + VM source host)
    protected List<Guid> getVdsBlackList() {
        List<Guid> blackList = new ArrayList<>(getRunVdssList());
        if (getVdsId() != null) {
            blackList.add(getVdsId());
        }
        return blackList;
    }

    // initial hosts list picked for scheduling, currently
    // passed by load balancing process.
    protected List<Guid> getVdsWhiteList() {
        return getParameters().getInitialHosts() == null ?
                Collections.emptyList() : getParameters().getInitialHosts();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        if (getParameters().getTargetClusterId() != null &&
                getVm() != null &&
                !Objects.equals(getParameters().getTargetClusterId(), getVm().getClusterId())) {
            // additional permissions needed since changing the cluster
            permissionList.addAll(VmHandler.getPermissionsNeededToChangeCluster(getParameters().getVmId(), getParameters().getTargetClusterId()));
        }

        return permissionList;
    }

    @Override
    public void onPowerringUp() {
        // nothing to do
    }

    protected VmValidator getVmValidator() {
        return new VmValidator(getVm());
    }

}
