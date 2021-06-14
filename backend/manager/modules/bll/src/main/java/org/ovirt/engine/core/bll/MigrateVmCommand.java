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
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.kubevirt.KubevirtMonitoring;
import org.ovirt.engine.core.bll.migration.ConvergenceConfigProvider;
import org.ovirt.engine.core.bll.migration.ConvergenceSchedule;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.MultipleVmsValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.businessentities.MigrationMethod;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.migration.ConvergenceConfig;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.vdscommands.MigrateStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MigrateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.MigrateStatusReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class MigrateVmCommand<T extends MigrateVmParameters> extends RunVmCommandBase<T> {

    private Logger log = LoggerFactory.getLogger(MigrateVmCommand.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    ConvergenceConfigProvider convergenceConfigProvider;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private HostNetworkQosDao hostNetworkQosDao;
    @Inject
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;
    @Inject
    private KubevirtMonitoring kubevirt;

    /** The VDS that the VM is going to migrate to */
    private VDS destinationVds;

    /** Used to log the migration error. */
    private EngineError migrationErrorCode;
    private String migrationErrorMessage;

    private Integer actualDowntime;
    private Object actualDowntimeLock = new Object();

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
    @SuppressWarnings("unused") // used by AuditLogger via reflection
    public String getDueToMigrationError() {
        if (migrationErrorCode != null) {
            return " due to an Error: " +
                    backend.getVdsErrorsTranslator().translateErrorTextSingle(migrationErrorCode.name(), true);
        } else if (migrationErrorMessage != null) {
            return " due to an Error: " + migrationErrorMessage;
        }
        return " ";
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

    private boolean initVdss() {
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

    protected Optional<Guid> getVdsToRunOn() {
        List<String> messages = new ArrayList<>();
        Optional<Guid> vdsToRunOn = schedulingManager.prepareCall(getCluster())
                .hostBlackList(getVdsBlackList())
                .hostWhiteList(getVdsWhiteList())
                .destHostIdList(getDestinationHostList())
                .ignoreHardVmToVmAffinity(getParameters().isIgnoreHardVmToVmAffinity())
                .delay(true)
                .correlationId(getCorrelationId())
                .outputMessages(messages)
                .schedule(getVm());
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
        if (getVm().getOrigin() == OriginType.KUBEVIRT) {
            kubevirt.migrate(getVm());
            freeLock();
            setSucceeded(true);
            return;
        }
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
            getParameters().setTotalMigrationTime(new Date());
            getParameters().resetStartTime();

            BooleanSupplier attachMBSDisks =  () -> managedBlockStorageCommandUtil
                    .attachManagedBlockStorageDisks(getVm(), vmHandler, getDestinationVds(), true);
            if (unplugPassthroughNics() && connectLunDisks(getDestinationVdsId()) &&
                    attachOrDetachMBSFromDest(attachMBSDisks) &&
                    migrateVm()) {
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
                ActionType.ActivateDeactivateVmNic,
                Arrays.toString(parametersList.toArray()));

        for (ActivateDeactivateVmNicParameters parameter : parametersList) {
            ActionReturnValue returnValue = runInternalAction(ActionType.ActivateDeactivateVmNic, parameter);
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

            List<VmNic> notRepluggedNics = replugNics(parametersList);
            if (!notRepluggedNics.isEmpty()) {
                Map<Guid, String> vnicToVfMap = getVnicToVfMap(getDestinationVdsId());
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

    private List<VmNic> replugNics(List<ActivateDeactivateVmNicParameters> parametersList) {
        log.debug("About to call {} with parameters: {}",
                ActionType.ActivateDeactivateVmNic,
                Arrays.toString(parametersList.toArray()));

        List<VmNic> notRepluggedNics = new ArrayList<>();

        for (ActivateDeactivateVmNicParameters parameter : parametersList) {
            ActionReturnValue returnValue = runInternalAction(ActionType.ActivateDeactivateVmNic, parameter);

            boolean nicPlugSucceeded = returnValue.getSucceeded();
            if (!nicPlugSucceeded) {
                notRepluggedNics.add(parameter.getNic());
            }
        }
        return notRepluggedNics;
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
        parameters.setWithFailover(false);
        return parameters;
    }

    private boolean migrateVm() {
        getVmManager().setLastStatusBeforeMigration(getVm().getStatus());
        setActionReturnValue(vdsBroker
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
        Boolean migrateEncrypted = vmHandler.getMigrateEncrypted(getVm(), getCluster());
        Boolean enableGuestEvents = null;
        Integer maxIncomingMigrations = null;
        Integer maxOutgoingMigrations = null;

        MigrationPolicy clusterMigrationPolicy = convergenceConfigProvider.getMigrationPolicy(
                getCluster().getMigrationPolicyId(),
                getCluster().getCompatibilityVersion());
        MigrationPolicy effectiveMigrationPolicy = findEffectiveConvergenceConfig(clusterMigrationPolicy);
        ConvergenceConfig convergenceConfig = getVm().getStatus() == VMStatus.Paused
                ? filterOutPostcopy(effectiveMigrationPolicy.getConfig())
                : effectiveMigrationPolicy.getConfig();
        convergenceSchedule = ConvergenceSchedule.from(convergenceConfig).asMap();

        maxBandwidth = getMaxBandwidth(clusterMigrationPolicy);
        if (!NoMigrationPolicy.ID.equals(effectiveMigrationPolicy.getId())) {
            autoConverge = effectiveMigrationPolicy.isAutoConvergence();
            migrateCompressed = effectiveMigrationPolicy.isMigrationCompression();
        }
        enableGuestEvents = effectiveMigrationPolicy.isEnableGuestEvents();

        maxIncomingMigrations = maxOutgoingMigrations = effectiveMigrationPolicy.getMaxMigrations();

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
                migrateEncrypted,
                getDestinationVds().getConsoleAddress(),
                maxBandwidth,
                convergenceSchedule,
                enableGuestEvents,
                maxIncomingMigrations,
                maxOutgoingMigrations);
    }

    private ConvergenceConfig filterOutPostcopy(ConvergenceConfig config) {
        ConvergenceConfig filteredConfig = new ConvergenceConfig();
        filteredConfig.setInitialItems(config.getInitialItems());
        if (config.getConvergenceItems() != null) {
            filteredConfig.setConvergenceItems(config.getConvergenceItems()
                    .stream()
                    .filter(item -> !item.getConvergenceItem().getAction().equals("postcopy"))
                    .collect(Collectors.toList()));
        }
        if (config.getLastItems() != null) {
            filteredConfig.setLastItems(config.getLastItems()
                    .stream()
                    .filter(item -> !item.getAction().equals("postcopy"))
                    .collect(Collectors.toList()));
        }
        return filteredConfig;
    }

    /**
     * @return Maximum bandwidth of each migration in cluster in MiB/s, `null` indicates that value in VDSM configuration
     * on source host should be used.
     *
     * @see org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType
     */
    private Integer getMaxBandwidth(MigrationPolicy migrationPolicy) {
        switch (getCluster().getMigrationBandwidthLimitType()) {
            case AUTO:
                return Optional.ofNullable(getAutoMaxBandwidth())
                        .map(bandwidth -> bandwidth / migrationPolicy.getMaxMigrations() / 8)
                        .orElse(null);
            case VDSM_CONFIG:
                return null;
            case CUSTOM:
                return getCluster().getCustomMigrationNetworkBandwidth() / migrationPolicy.getMaxMigrations() / 8;
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
     * {@link HostNetworkQos#getOutAverageRealtime()} nor {@link HostNetworkQos#getOutAverageUpperlimit()} property.
     * @return `null` if it can't be obtained, value in Mbps otherwise
     */
    private Integer getQosBandwidth(Guid clusterId) {
        final HostNetworkQos migrationHostNetworkQos = hostNetworkQosDao
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
            queryDowntime();
            vmDynamicDao.clearMigratingToVds(getVmId());
            updateVmAfterMigrationToDifferentCluster();
            plugPassthroughNics();
            initParametersForExternalNetworks(destinationVds, true);
        } finally {
            super.runningSucceded();
        }
    }

    @Override
    protected void runningFailed() {
        try {
            //this will clean all VF reservations made in {@link #initVdss}.
            cleanupPassthroughVnics(getDestinationVdsId());
            BooleanSupplier detachMBSDisk = () -> managedBlockStorageCommandUtil
                    .disconnectManagedBlockStorageDisks(getVm(), vmHandler, true);
            if (!attachOrDetachMBSFromDest(detachMBSDisk)) {
                log.error("Failed to detach managed block disks from destination host");
            }
        } finally {
            super.runningFailed();
        }
    }

    private void queryDowntime() {
        if (actualDowntime != null) {
            return;
        }

        try {
            VDSReturnValue retVal = runVdsCommand(VDSCommandType.MigrateStatus,
                    new MigrateStatusVDSCommandParameters(getDestinationVdsId(), getVmId()));
            if (retVal != null && retVal.getReturnValue() != null) {
                Integer downtime = ((MigrateStatusReturn) retVal.getReturnValue()).getDowntime();
                if (downtime != null) {
                    setActualDowntime(downtime);
                }
            }
        } catch (EngineException e) {
            migrationErrorCode = e.getErrorCode();
        }
    }

    private void updateVmAfterMigrationToDifferentCluster() {
        if (getParameters().getTargetClusterId() == null
                || getVm().getClusterId().equals(getParameters().getTargetClusterId())) {
            return;
        }

        ChangeVMClusterParameters params = new ChangeVMClusterParameters(
                getParameters().getTargetClusterId(),
                getVmId(),
                getVm().getCustomCompatibilityVersion());
        setSucceeded(backend.runInternalAction(ActionType.ChangeVMCluster, params).getSucceeded());
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
                    findValidMigrationIpAddress(migrationNetwork, VdsNetworkInterface::getIpv4Address, "v4");
            if (migrationDestinationIpv4Address != null) {
                return migrationDestinationIpv4Address;
            }
            return findValidMigrationIpAddress(migrationNetwork, VdsNetworkInterface::getIpv6Address, "v6");
        }
        return null;
    }

    private String findValidMigrationIpAddress(Network migrationNetwork,
            Function<VdsNetworkInterface, String> ipAddressGetter, String ipVersion) {

        // assure migration network is active on source host
        final String migrationSourceIpAddress = getMigrationNetworkAddress(getVds().getId(),
                migrationNetwork.getName(),
                ipAddressGetter,
                ipVersion);
        if (StringUtils.isNotEmpty(migrationSourceIpAddress)) {
            // find migration IP address on destination host
            final String migrationDestinationIpAddress = getMigrationNetworkAddress(getDestinationVds().getId(),
                    migrationNetwork.getName(),
                    ipAddressGetter,
                    ipVersion);
            if (StringUtils.isNotEmpty(migrationDestinationIpAddress)) {
                return migrationDestinationIpAddress;
            }
        }
        return null;
    }

    private String getMigrationNetworkAddress(Guid hostId,
            String migrationNetworkName,
            Function<VdsNetworkInterface, String> ipAddressGetter,
            String ipVersion) {
        final List<VdsNetworkInterface> nics = interfaceDao.getAllInterfacesForVds(hostId);

        String errorMsg = String.format("Couldn't find ip %s migration address", ipVersion);
        VdsNetworkInterface migrationNic =
                nics.stream().filter(nic -> migrationNetworkName.equals(nic.getNetworkName())).findFirst().orElse(null);
        if (migrationNic == null) {
            log.warn("{} : migration network {} doesn't exist on host {}.", errorMsg, migrationNetworkName, hostId);
            return null;
        }

        if (migrationInterfaceUp(migrationNic, nics)) {
            String ip = ipAddressGetter.apply(migrationNic);
            if (StringUtils.isEmpty(ip)) {
                log.warn("{} : the IP address of migration network {} (host {}/nic {}) is empty.",
                        errorMsg,
                        migrationNetworkName,
                        hostId,
                        NetworkCommonUtils.stripVlan(migrationNic));
            }
            return ip;
        } else {
            log.warn("{} : Nic {} with migration network {} is not up on host {}.",
                    errorMsg,
                    NetworkCommonUtils.stripVlan(migrationNic),
                    migrationNetworkName,
                    hostId);
            return null;
        }
    }

    private boolean migrationInterfaceUp(VdsNetworkInterface nic, List<VdsNetworkInterface> nics) {
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

    private boolean attachOrDetachMBSFromDest(BooleanSupplier supplier) {
        vmHandler.updateDisksFromDb(getVm());
        if (DisksFilter.filterManagedBlockStorageDisks(getVm().getDiskList()).isEmpty()) {
            return true;
        }

        return supplier.getAsBoolean();
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
        if (getVm().getOrigin() == OriginType.KUBEVIRT) {
            return AuditLogType.VM_MIGRATION_FAILED;
        }

        if (getDestinationVds() == null) {
            auditLogDirector.log(this, AuditLogType.VM_MIGRATION_NO_VDS_TO_MIGRATE_TO);
        }

        if (getVds().getStatus() == VDSStatus.PreparingForMaintenance) {
            return getDestinationVds() != null ?
                    AuditLogType.VM_MIGRATION_FAILED_DURING_MOVE_TO_MAINTENANCE
                    : AuditLogType.VM_MIGRATION_FAILED_DURING_MOVE_TO_MAINTENANCE_NO_DESTINATION_VDS;
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
    protected boolean validateImpl() {
        final VM vm = getVm();

        if (vm == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (isVmDuringBackup()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_BACKUP);
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

        if (!validate(vmValidator.canMigrate(getParameters().isForceMigrationForNonMigratableVm()))) {
            return false;
        }

        if (!validate(new MultipleVmsValidator(vm).vmNotHavingPluggedDiskSnapshots(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_PLUGGED_DISK_SNAPSHOT))
                || !validate(vmValidator.allPassthroughVnicsMigratable())) {
            return false;
        }

        if (getParameters().getTargetClusterId() != null) {
            ChangeVmClusterValidator changeVmClusterValidator = ChangeVmClusterValidator.create(
                    getVm(),
                    getParameters().getTargetClusterId(),
                    getVm().getCustomCompatibilityVersion(),
                    getUserId());
            if (!validate(changeVmClusterValidator.validate())) {
                return false;
            }
        }

        return validate(snapshotsValidator.vmNotDuringSnapshot(vm.getId()))
                // This check was added to prevent migration of VM while its disks are being migrated
                // TODO: replace it with a better solution
                && validate(new DiskImagesValidator(callFilterImageDisks(vm)).diskImagesNotLocked())
                && canScheduleVm();
    }

    protected boolean canScheduleVm() {
        boolean result = !schedulingManager.prepareCall(getCluster())
                .hostBlackList(getVdsBlackList())
                .hostWhiteList(getVdsWhiteList())
                .outputMessages(getReturnValue().getValidationMessages())
                .canSchedule(getVm()).isEmpty();

        if (result) {
            // If it is possible to migrate VM without breaking affinity, do not ignore it.
            getParameters().setIgnoreHardVmToVmAffinity(false);
            return true;
        }

        // If the migration is caused by moving a host to maintenance,
        // it is possible to ignore VM affinity groups.
        if (getParameters().isIgnoreHardVmToVmAffinity()) {
            return !schedulingManager.prepareCall(getCluster())
                    .hostBlackList(getVdsBlackList())
                    .hostWhiteList(getVdsWhiteList())
                    .outputMessages(getReturnValue().getValidationMessages())
                    .ignoreHardVmToVmAffinity(getParameters().isIgnoreHardVmToVmAffinity())
                    .canSchedule(getVm()).isEmpty();
        }

        return false;
    }

    @Override
    protected void logValidationFailed() {
        addCustomValue("DueToMigrationError",
                " due to a failed validation: " +
                        backend.getErrorsTranslator().translateErrorText(getReturnValue().getValidationMessages()));
        auditLogDirector.log(this, AuditLogType.VM_MIGRATION_FAILED);
    }

    @SuppressWarnings("unchecked")
    private List<DiskImage> callFilterImageDisks(VM vm) {
        return DisksFilter.filterImageDisks(diskDao.getAllForVm(vm.getId(), true),
                ONLY_NOT_SHAREABLE,
                ONLY_ACTIVE);
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
                VDSReturnValue retVal = runVdsCommand(VDSCommandType.MigrateStatus,
                        new MigrateStatusVDSCommandParameters(getVdsId(), getVmId()));
                if (retVal != null && retVal.getReturnValue() != null) {
                    migrationErrorMessage = ((MigrateStatusReturn) retVal.getReturnValue()).getMessage();
                }
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

    @SuppressWarnings("unused") // used by AuditLogger via reflection
    // Duration: time that took for the actual migration
    public String getDuration() {
        Date start = getParameters().getStartTime() != null
                ? getParameters().getStartTime()
                : getParameters().getTotalMigrationTime();
        return DurationFormatUtils.formatDurationWords(new Date().getTime() - start.getTime(), true, true);
    }

    @SuppressWarnings("unused") // used by AuditLogger via reflection
    // TotalDuration: time that took migration including retries (can be identical to Duration)
    public String getTotalDuration() {
        return DurationFormatUtils.formatDurationWords(new Date().getTime() - getParameters().getTotalMigrationTime().getTime(), true, true);
    }

    @SuppressWarnings("unused") // used by AuditLogger via reflection
    // ActualDowntime: returns the actual time that the vm was offline (not available for access)
    public String getActualDowntime() {
        return (actualDowntime == null) ? "(N/A)" : actualDowntime + "ms";
    }

    private void setActualDowntime(int actualDowntime) {
        synchronized (actualDowntimeLock) {
            if (this.actualDowntime == null) {
                this.actualDowntime = actualDowntime;
            }
        }
    }

    @Override
    protected String getLockMessage() {
        return String.format("%1$s$VmName %2$s",
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_MIGRATED.name(),
                getVmName());
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

    @Override
    public void migrationProgressReported(int progress) {
        getParameters().setStartTime(new Date());
    }

    @Override
    public void actualDowntimeReported(int actualDowntime) {
        setActualDowntime(actualDowntime);
    }

    protected VmValidator getVmValidator() {
        return new VmValidator(getVm());
    }

    @Override
    public void reportCompleted() {
        try {
            vmDynamicDao.clearMigratingToVds(getVmId());
            managedBlockStorageCommandUtil.disconnectManagedBlockStorageDisks(getVm(), vmHandler);
        } finally {
            super.reportCompleted();
        }
    }
}
