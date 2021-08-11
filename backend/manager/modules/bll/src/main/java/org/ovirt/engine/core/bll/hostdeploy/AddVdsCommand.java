package org.ovirt.engine.core.bll.hostdeploy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.naming.AuthenticationException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.uutils.ssh.ConstraintByteArrayOutputStream;
import org.ovirt.engine.core.uutils.ssh.SSHClient;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVdsCommand<T extends AddVdsActionParameters> extends VdsCommand<T> {

    private final AuditLogType errorType = AuditLogType.USER_FAILED_ADD_VDS;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ProviderDao providerDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private VdsStatisticsDao vdsStatisticsDao;
    @Inject
    private FenceAgentDao fenceAgentDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private LabelDao labelDao;
    @Inject
    private ClusterUtils clusterUtils;
    @Inject
    private AffinityValidator affinityValidator;

    private BiConsumer<AuditLogable, AuditLogDirector> affinityGroupLoggingMethod = (a, b) -> {};
    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AddVdsCommand(Guid commandId) {
        super(commandId);
    }

    public AddVdsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setClusterId(parameters.getvds().getClusterId());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
        addValidationMessageVariable("server", getParameters().getvds().getHostName());
    }

    private Provider<?> getHostProvider() {
        return providerDao.get(getParameters().getVdsStaticData().getHostProviderId());
    }

    @Override
    protected void executeCommand() {
        Guid oVirtId = getParameters().getVdsForUniqueId();
        if (oVirtId != null) {

            // if fails to remove deprecated entry, we might attempt to add new oVirt host with an existing unique-id.
            if (!removeDeprecatedOvirtEntry(oVirtId)) {
                log.error("Failed to remove duplicated oVirt entry with id '{}'. Abort adding oVirt Host type",
                        oVirtId);
                throw new EngineException(EngineError.HOST_ALREADY_EXISTS);
            }
        }

        TransactionSupport.executeInNewTransaction(() -> {
            addVdsStaticToDb();
            addVdsDynamicToDb();
            addVdsStatisticsToDb();
            addAffinityGroupsAndLabels();
            getCompensationContext().stateChanged();
            return null;
        });

        if (getParameters().isProvisioned() && getParameters().getVdsStaticData().isManaged()) {
            HostProviderProxy proxy = providerProxyFactory.create(getHostProvider());
            proxy.provisionHost(
                    getParameters().getvds(),
                    getParameters().getHostGroup(),
                    getParameters().getComputeResource(),
                    getParameters().getHostMac(),
                    getParameters().getDiscoverName(),
                    getParameters().getPassword(),
                    getParameters().getDiscoverIp()
            );

            addCustomValue("HostGroupName", getParameters().getHostGroup().getName());
            auditLogDirector.log(this, AuditLogType.VDS_PROVISION);
        }

        // set vds spm id
        if (getCluster().getStoragePoolId() != null && getParameters().getVdsStaticData().isManaged()) {
            VdsActionParameters tempVar = new VdsActionParameters(getVdsIdRef());
            tempVar.setSessionId(getParameters().getSessionId());
            tempVar.setCompensationEnabled(true);
            tempVar.setCorrelationId(getCorrelationId());
            ActionReturnValue addVdsSpmIdReturn =
                    runInternalAction(ActionType.AddVdsSpmId,
                            tempVar,
                            cloneContext().withoutLock().withoutExecutionContext());
            if (!addVdsSpmIdReturn.getSucceeded()) {
                setSucceeded(false);
                getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                return;
            }
        }
        TransactionSupport.executeInNewTransaction(() -> {
            initializeVds(true);
            alertIfPowerManagementNotConfigured(getParameters().getVdsStaticData());
            testVdsPowerManagementStatus(getParameters().getVdsStaticData());
            setSucceeded(true);
            setActionReturnValue(getVdsIdRef());

            // If the installation failed, we don't want to compensate for the failure since it will remove the
            // host, but instead the host should be left in an "install failed" status.
            getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();
            return null;
        });
        // do not install vds's which added in pending mode or for provisioning (currently power
        // clients). they are installed as part of the approve process or automatically after provision
        if (Config.<Boolean> getValue(ConfigValues.InstallVds) &&
            getParameters().getVdsStaticData().isManaged() &&
            !getParameters().isPending() &&
            !getParameters().isProvisioned()) {
            final InstallVdsParameters installVdsParameters = new InstallVdsParameters(getVdsId(), getParameters().getPassword());
            installVdsParameters.setAuthMethod(getParameters().getAuthMethod());
            installVdsParameters.setOverrideFirewall(getParameters().getOverrideFirewall());
            installVdsParameters.setActivateHost(getParameters().getActivateHost());
            installVdsParameters.setRebootHost(getParameters().getRebootHost());
            installVdsParameters
                    .setHostedEngineDeployConfiguration(getParameters().getHostedEngineDeployConfiguration());
            installVdsParameters.setCorrelationId(getCorrelationId());
            Map<String, String> values = new HashMap<>();
            values.put(VdcObjectType.VDS.name().toLowerCase(), getParameters().getvds().getName());
            Step installStep = executionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    StepEnum.INSTALLING_HOST,
                    ExecutionMessageDirector.resolveStepMessage(StepEnum.INSTALLING_HOST, values));
            final ExecutionContext installCtx = new ExecutionContext();
            installCtx.setJob(getExecutionContext().getJob());
            installCtx.setStep(installStep);
            installCtx.setMonitored(true);
            installCtx.setShouldEndJob(true);
            ThreadPoolUtil.execute(() -> runInternalAction(
                    ActionType.InstallVdsInternal,
                    installVdsParameters,
                    cloneContextAndDetachFromParent()
                    .withExecutionContext(installCtx)));
            ExecutionHandler.setAsyncJob(getExecutionContext(), true);
        }
    }

    protected boolean isGlusterSupportEnabled() {
        return getCluster() != null && getCluster().supportsGlusterService() && getParameters().isGlusterPeerProbeNeeded();
    }

    /**
     * The scenario in which a host is already exists when adding new host after the validate is when the existed
     * host type is oVirt and its status is 'Pending Approval'. In this case the old entry is removed from the DB, since
     * the oVirt node was added again, where the new host properties might be updated (e.g. cluster adjustment, data
     * center, host name, host address) and a new entry with updated properties is added.
     *
     * @param oVirtId
     *            the deprecated host entry to remove
     */
    private boolean removeDeprecatedOvirtEntry(final Guid oVirtId) {

        final VDS vds = vdsDao.get(oVirtId);
        if (vds == null) {
            return false;
        }

        String vdsName = getParameters().getVdsStaticData().getName();
        log.info("Host '{}', id '{}' of type '{}' is being re-registered as Host '{}'",
                vds.getName(),
                vds.getId(),
                vds.getVdsType().name(),
                vdsName);
        ActionReturnValue result =
                TransactionSupport.executeInNewTransaction(() -> runInternalAction(ActionType.RemoveVds,
                        new RemoveVdsParameters(oVirtId)));

        if (!result.getSucceeded()) {
            String errors =
                    result.isValid() ? result.getFault().getError().name()
                            : StringUtils.join(result.getValidationMessages(), ",");
            log.warn("Failed to remove Host '{}', id '{}', re-registering it as Host '{}' fails with errors {}",
                    vds.getName(),
                    vds.getId(),
                    vdsName,
                    errors);
        } else {
            log.info("Host '{}' is now known as Host '{}'",
                    vds.getName(),
                    vdsName);
        }

        return result.getSucceeded();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_VDS : errorType;
    }

    private void addVdsStaticToDb() {
        getParameters().getVdsStaticData().setServerSslEnabled(
                Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication));
        vdsStaticDao.save(getParameters().getVdsStaticData());
        getCompensationContext().snapshotNewEntity(getParameters().getVdsStaticData());
        setVdsIdRef(getParameters().getVdsStaticData().getId());
        addFenceAgents();
        setVds(null);
    }

    private void addVdsDynamicToDb() {
        VdsDynamic vdsDynamic = new VdsDynamic();
        vdsDynamic.setId(getParameters().getVdsStaticData().getId());
        // TODO: oVirt type - here oVirt behaves like power client?
        if (!getParameters().getVdsStaticData().isManaged()) {
            vdsDynamic.setStatus(VDSStatus.Unassigned);
        } else if (getParameters().isPending()) {
            vdsDynamic.setStatus(VDSStatus.PendingApproval);
        } else if (getParameters().isProvisioned()) {
            vdsDynamic.setStatus(VDSStatus.InstallingOS);
        } else if (Config.<Boolean> getValue(ConfigValues.InstallVds)) {
            vdsDynamic.setStatus(VDSStatus.Installing);
        }
        vdsDynamicDao.save(vdsDynamic);
        getCompensationContext().snapshotNewEntity(vdsDynamic);
    }

    private void addVdsStatisticsToDb() {
        VdsStatistics vdsStatistics = new VdsStatistics();
        vdsStatistics.setId(getParameters().getVdsStaticData().getId());
        vdsStatisticsDao.save(vdsStatistics);
        getCompensationContext().snapshotNewEntity(vdsStatistics);
    }

    protected boolean validateCluster() {
        if (getCluster() == null) {
            return failValidation(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
        }
        return true;
    }

    @Override
    protected boolean validate() {
        T params = getParameters();
        if (!params.getVdsStaticData().isManaged()) {
            return true;
        }
        setClusterId(params.getVdsStaticData().getClusterId());
        params.setVdsForUniqueId(null);
        // Check if this is a valid cluster
        boolean returnValue = validateCluster();
        if (returnValue) {
            HostValidator validator = getHostValidator();
            returnValue = validate(validator.nameNotEmpty())
                    && validate(validator.nameLengthIsLegal())
                    && validate(validator.hostNameIsValid())
                    && validate(validator.nameNotUsed())
                    && validate(validator.hostNameNotUsed())
                    && validate(validator.portIsValid())
                    && validate(validator.sshUserNameNotEmpty())
                    && validate(validator.validateSingleHostAttachedToLocalStorage())
                    && validate(validator.securityKeysExists())
                    && validate(validator.provisioningComputeResourceValid(params.isProvisioned(),
                            params.getComputeResource()))
                    && validate(validator.provisioningHostGroupValid(params.isProvisioned(),
                            params.getHostGroup()))
                    && validate(validator.passwordNotEmpty(params.isPending(),
                            params.getAuthMethod(),
                            params.getPassword()))
                    && validate(validator.supportsDeployingHostedEngine(params.getHostedEngineDeployConfiguration(),
                            getCluster(),
                            false));
        }

        if (!(returnValue
                && isPowerManagementLegal(params.getVdsStaticData().isPmEnabled(),
                        params.getFenceAgents(),
                        getCluster().getCompatibilityVersion().toString(),
                        true)
                && canConnect(params.getvds()))) {
            return false;
        }

        if (isGlusterSupportEnabled() && clusterHasNonInitializingServers()) {
            // allow simultaneous installation of hosts, but if a host has completed install, only
            // allow addition of another host if it can be peer probed to cluster.
            VDS upServer = glusterUtil.getUpServer(getClusterId());
            if (upServer == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_GLUSTER_HOST_TO_PEER_PROBE);
            }
        }

        if (!validate(validateAffinityGroups())) {
            return false;
        }

        return true;
    }

    protected HostValidator getHostValidator() {
        return HostValidator.createInstance(getParameters().getvds());
    }

    private boolean clusterHasServers() {
        return clusterUtils.hasServers(getClusterId());
    }

    private boolean clusterHasNonInitializingServers() {
        for (VDS vds : vdsDao.getAllForCluster(getClusterId())) {
            if (vds.getStatus() != VDSStatus.Installing &&
                    vds.getStatus() != VDSStatus.InstallingOS &&
                    vds.getStatus() != VDSStatus.PendingApproval &&
                    vds.getStatus() != VDSStatus.Initializing &&
                    vds.getStatus() != VDSStatus.InstallFailed) {
                return true;
            }
        }
        return false;
    }

    public EngineSSHClient getSSHClient() throws Exception {
        Long timeout =
                TimeUnit.SECONDS.toMillis(Config.<Integer> getValue(ConfigValues.ConnectToServerTimeoutInSeconds));

        EngineSSHClient sshclient = new EngineSSHClient();
        sshclient.setVds(getParameters().getvds());
        sshclient.setHardTimeout(timeout);
        sshclient.setSoftTimeout(timeout);
        sshclient.setPassword(getParameters().getPassword());
        switch (getParameters().getAuthMethod()) {
            case PublicKey:
                sshclient.useDefaultKeyPair();
                break;
            case Password:
                sshclient.setPassword(getParameters().getPassword());
                break;
            default:
                throw new Exception("Invalid authentication method value was sent to AddVdsCommand");
        }

        return sshclient;
    }

    /**
     * getInstalledVdsIdIfExists
     *
     * Communicate with host by SSH session and gather vdsm-id if exist
     *
     * @param client - already connected ssh client
     */
    private String getInstalledVdsIdIfExists(SSHClient client) {
        ByteArrayOutputStream err = new ConstraintByteArrayOutputStream(256);
        try {
            ByteArrayOutputStream out = new ConstraintByteArrayOutputStream(256);
            client.executeCommand(Config.getValue(ConfigValues.GetVdsmIdByVdsmToolCommand), null, out, err);
            return new String(out.toByteArray(), StandardCharsets.UTF_8).trim();
        } catch(Exception e) {
            log.warn(
                    "Failed to initiate vdsm-id request on host: {} with error: {}",
                    e.getMessage(),
                    new String(err.toByteArray(), StandardCharsets.UTF_8)
                    );
            log.debug("Exception", e);
            return null;
        }
    }

    protected boolean canConnect(VDS vds) {
        // execute the connectivity and id uniqueness validation for VDS type hosts
        if (
            !getParameters().isPending() && !getParameters().isProvisioned() &&
            Config.<Boolean> getValue(ConfigValues.InstallVds)
        ) {
            try (final EngineSSHClient sshclient = getSSHClient()) {
                sshclient.connect();
                sshclient.authenticate();

                String hostUUID = getInstalledVdsIdIfExists(sshclient);
                if (hostUUID != null && vdsDao.getAllWithUniqueId(hostUUID).size() != 0) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_VDS_WITH_SAME_UUID_EXIST);
                }

                return isValidGlusterPeer(sshclient, vds.getClusterId());
            } catch (AuthenticationException e) {
                log.error(
                        "Failed to authenticate session with host '{}': {}",
                        vds.getName(),
                        e.getMessage());
                log.debug("Exception", e);
                return failValidation(EngineMessage.VDS_CANNOT_AUTHENTICATE_TO_SERVER);
            } catch (SecurityException e) {
                log.error(
                        "Failed to connect to host '{}', fingerprint '{}': {}",
                        vds.getName(),
                        vds.getSshKeyFingerprint(),
                        e.getMessage());
                log.debug("Exception", e);
                addValidationMessage(EngineMessage.VDS_SECURITY_CONNECTION_ERROR);
                addValidationMessageVariable("ErrorMessage", e.getMessage());
                return failValidation(EngineMessage.VDS_CANNOT_AUTHENTICATE_TO_SERVER);
            } catch (Exception e) {
                log.error(
                        "Failed to establish session with host '{}': {}",
                        vds.getName(),
                        e.getMessage());
                log.debug("Exception", e);

                return failValidation(EngineMessage.VDS_CANNOT_CONNECT_TO_SERVER);
            }
        }
        return true;
    }

    /**
     * Checks if the server can be a valid gluster peer. Fails if it is already part of another cluster (when current
     * cluster is not empty). This is done by executing the 'gluster peer status' command on the server.<p>
     * In case glusterd is down or not installed on the server (which is a possibility on a new server), the command can
     * fail. In such cases, we just log it as a debug message and return true.<p>
     * Another interesting case is where one of the peers of the server is already present as part of this cluster in
     * the engine DB. This means that one ore more hosts were probably added to the gluster cluster using gluster CLI,
     * and hence this server should be allowed to be added.
     *
     * @param sshclient
     *            SSH client that can be used to execute 'gluster peer status' command on the server
     * @param clusterId
     *            ID of the cluster to which the server is being added.
     * @return true if the server is good to be added to a gluster cluster, else false.
     */
    private boolean isValidGlusterPeer(SSHClient sshclient, Guid clusterId) {
        if (isGlusterSupportEnabled() && clusterHasServers()) {
            try {
                // Must not allow adding a server that already is part of another gluster cluster
                Set<String> peers = glusterUtil.getPeers(sshclient);
                if (peers.size() > 0) {
                    for(String peer : peers) {
                        if(glusterDBUtils.serverExists(clusterId, peer)) {
                            // peer present in cluster. so server being added is valid.
                            return true;
                        }
                    }

                    // none of the peers present in the cluster. fail with appropriate error.
                    return failValidation(EngineMessage.SERVER_ALREADY_PART_OF_ANOTHER_CLUSTER);
                }
            } catch (Exception e) {
                // This can happen if glusterd is not running on the server. Ignore it and let the server get added.
                // Peer probe will anyway fail later and the server will then go to non-operational status.
                log.debug("Could not check if server '{}' is already part of another gluster cluster. Will"
                                + " allow adding it.",
                        sshclient.getHost());
                log.debug("Exception", e);
            }
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getClusterId(), VdcObjectType.Cluster,
                getActionType().getActionGroup()));
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        if (getParameters().getVdsStaticData().isPmEnabled()) {
            addValidationGroup(PowerManagementCheck.class);
        }
        return super.getValidationGroups();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            VDS vds = getParameters().getvds();

            String vdsName = (vds != null && vds.getName() != null) ? vds.getName() : "";
            jobProperties.put(VdcObjectType.VDS.name().toLowerCase(), vdsName);
        }
        return jobProperties;
    }

    private void addFenceAgents() {
        if (getParameters().getFenceAgents() != null) { // if == null, means no update. Empty list means
            for (FenceAgent agent : getParameters().getFenceAgents()) {
                agent.setHostId(getVdsId());
                fenceAgentDao.save(agent);
            }
        }
    }

    private ValidationResult validateAffinityGroups() {
        AffinityValidator.Result result = affinityValidator.validateAffinityUpdateForHost(getClusterId(),
                getVdsId(),
                getParameters().getAffinityGroups(),
                getParameters().getAffinityLabels());

        affinityGroupLoggingMethod = result.getLoggingMethod();
        return result.getValidationResult();
    }

    private void addAffinityGroupsAndLabels() {
        // TODO - check permissions to modify affinity groups
        List<AffinityGroup> affinityGroups = getParameters().getAffinityGroups();
        if (affinityGroups != null) {
            affinityGroupLoggingMethod.accept(this, auditLogDirector);
            affinityGroupDao.setAffinityGroupsForHost(getVdsId(),
                    affinityGroups.stream()
                            .map(AffinityGroup::getId)
                            .collect(Collectors.toList()));
        }

        // TODO - check permissions to modify labels
        List<Label> affinityLabels = getParameters().getAffinityLabels();
        if (affinityLabels != null) {
            List<Guid> labelIds = affinityLabels.stream()
                    .map(Label::getId)
                    .collect(Collectors.toList());
            labelDao.addHostToLabels(getVdsId(), labelIds);
        }
    }
}
