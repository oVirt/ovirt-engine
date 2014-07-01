package org.ovirt.engine.core.bll;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.naming.AuthenticationException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.uutils.ssh.ConstraintByteArrayOutputStream;
import org.ovirt.engine.core.uutils.ssh.SSHClient;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVdsCommand<T extends AddVdsActionParameters> extends VdsCommand<T> {

    private AuditLogType errorType = AuditLogType.USER_FAILED_ADD_VDS;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddVdsCommand(Guid commandId) {
        super(commandId);
    }

    public AddVdsCommand(T parameters) {
        super(parameters);
        setVdsGroupId(parameters.getvds().getVdsGroupId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessageVariable("server", getParameters().getvds().getHostName());
    }

    private Provider<?> getHostProvider() {
        return getProviderDao().get(getParameters().getProviderId());
    }

    @Override
    protected void executeCommand() {
        Guid oVirtId = getParameters().getVdsForUniqueId();
        if (oVirtId != null) {

            // if fails to remove deprecated entry, we might attempt to add new oVirt host with an existing unique-id.
            if (!removeDeprecatedOvirtEntry(oVirtId)) {
                log.errorFormat("Failed to remove duplicated oVirt entry with id {0}. Abort adding oVirt Host type",
                        oVirtId);
                throw new VdcBLLException(VdcBllErrors.HOST_ALREADY_EXISTS);
            }
        }

        if (getParameters().getAddProvisioned()) {
            HostProviderProxy proxy =
                    ((HostProviderProxy) ProviderProxyFactory.getInstance().create(getHostProvider()));
            getParameters().getvds().getStaticData().setHostProviderId(getParameters().getProviderId());
            proxy.provisionHost(
                    getParameters().getvds(),
                    getParameters().getHostGroup(),
                    getParameters().getComputeResource(),
                    getParameters().getHostMac(),
                    getParameters().getDiscoverName(),
                    getParameters().getPassword()
            );

            AuditLogableBase logable = new AuditLogableBase();
            logable.setVds(getParameters().getvds());
            logable.addCustomValue("HostGroupName", getParameters().getHostGroup().getName());
            AuditLogDirector.log(logable, AuditLogType.VDS_PROVISION);
        }

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                AddVdsStaticToDb();
                AddVdsDynamicToDb();
                AddVdsStatisticsToDb();
                getCompensationContext().stateChanged();
                return null;
            }
        });

        // set vds spm id
        if (getVdsGroup().getStoragePoolId() != null) {
            VdsActionParameters tempVar = new VdsActionParameters(getVdsIdRef());
            tempVar.setSessionId(getParameters().getSessionId());
            tempVar.setCompensationEnabled(true);
            VdcReturnValueBase addVdsSpmIdReturn =
                    runInternalAction(VdcActionType.AddVdsSpmId,
                            tempVar,
                            cloneContext().withoutLock().withoutExecutionContext());
            if (!addVdsSpmIdReturn.getSucceeded()) {
                setSucceeded(false);
                getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                return;
            }
        }
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                initializeVds(true);
                AlertIfPowerManagementNotConfigured(getParameters().getVdsStaticData());
                TestVdsPowerManagementStatus(getParameters().getVdsStaticData());
                setSucceeded(true);
                setActionReturnValue(getVdsIdRef());

                // If the installation failed, we don't want to compensate for the failure since it will remove the
                // host, but instead the host should be left in an "install failed" status.
                getCompensationContext().resetCompensation();
                return null;
            }
        });
        // do not install vds's which added in pending mode or for provisioning (currently power
        // clients). they are installed as part of the approve process or automatically after provision
        if (Config.<Boolean> getValue(ConfigValues.InstallVds) &&
            !getParameters().getAddPending() &&
            !getParameters().getAddProvisioned()) {
            final InstallVdsParameters installVdsParameters = new InstallVdsParameters(getVdsId(), getParameters().getPassword());
            installVdsParameters.setAuthMethod(getParameters().getAuthMethod());
            installVdsParameters.setOverrideFirewall(getParameters().getOverrideFirewall());
            installVdsParameters.setActivateHost(getParameters().getActivateHost());
            installVdsParameters.setRebootAfterInstallation(getParameters().isRebootAfterInstallation());
            installVdsParameters.setNetworkProviderId(getParameters().getNetworkProviderId());
            installVdsParameters.setNetworkMappings(getParameters().getNetworkMappings());
            Map<String, String> values = new HashMap<String, String>();
            values.put(VdcObjectType.VDS.name().toLowerCase(), getParameters().getvds().getName());
            Step installStep = ExecutionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    StepEnum.INSTALLING_HOST,
                    ExecutionMessageDirector.resolveStepMessage(StepEnum.INSTALLING_HOST, values));
            final ExecutionContext installCtx = new ExecutionContext();
            installCtx.setJob(getExecutionContext().getJob());
            installCtx.setStep(installStep);
            installCtx.setMonitored(true);
            installCtx.setShouldEndJob(true);
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    runInternalAction(VdcActionType.InstallVdsInternal,
                            installVdsParameters,
                            cloneContextAndDetachFromParent()
                            .withExecutionContext(installCtx));
                }
            });
            ExecutionHandler.setAsyncJob(getExecutionContext(), true);
        }
    }

    protected boolean isGlusterSupportEnabled() {
        return getVdsGroup() != null && getVdsGroup().supportsGlusterService() && getParameters().isGlusterPeerProbeNeeded();
    }

    /**
     * The scenario in which a host is already exists when adding new host after the canDoAction is when the existed
     * host type is oVirt and its status is 'Pending Approval'. In this case the old entry is removed from the DB, since
     * the oVirt node was added again, where the new host properties might be updated (e.g. cluster adjustment, data
     * center, host name, host address) and a new entry with updated properties is added.
     *
     * @param oVirtId
     *            the deprecated host entry to remove
     */
    private boolean removeDeprecatedOvirtEntry(final Guid oVirtId) {

        final VDS vds = DbFacade.getInstance().getVdsDao().get(oVirtId);
        if (vds == null || !VdsHandler.isPendingOvirt(vds)) {
            return false;
        }

        String vdsName = getParameters().getVdsStaticData().getName();
        log.infoFormat("Host {0}, id {1} of type {2} is being re-registered as Host {3}",
                vds.getName(),
                vds.getId(),
                vds.getVdsType().name(),
                vdsName);
        VdcReturnValueBase result =
                TransactionSupport.executeInNewTransaction(new TransactionMethod<VdcReturnValueBase>() {
                    @Override
                    public VdcReturnValueBase runInTransaction() {
                        return runInternalAction(VdcActionType.RemoveVds,
                                new RemoveVdsParameters(oVirtId));
                    }
                });

        if (!result.getSucceeded()) {
            String errors =
                    result.getCanDoAction() ? result.getFault().getError().name()
                            : StringUtils.join(result.getCanDoActionMessages(), ",");
            log.warnFormat("Failed to remove Host {0}, id {1}, re-registering it as Host {2} fails with errors {3}",
                    vds.getName(),
                    vds.getId(),
                    vdsName,
                    errors);
        } else {
            log.infoFormat("Host {0} is now known as Host {2}",
                    vds.getName(),
                    vdsName);
        }

        return result.getSucceeded();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_VDS : errorType;
    }

    private void AddVdsStaticToDb() {
        getParameters().getVdsStaticData().setServerSslEnabled(
                Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication));
        VdsStatic vdsStatic = getParameters().getVdsStaticData();
        if (vdsStatic.getProtocol() == null) {
            VDSGroup cluster = getVdsGroup();
            if (cluster != null && FeatureSupported.jsonProtocol(cluster.getcompatibility_version())) {
                vdsStatic.setProtocol(VdsProtocol.STOMP);
            } else {
                vdsStatic.setProtocol(VdsProtocol.XML);
            }
        }
        DbFacade.getInstance().getVdsStaticDao().save(getParameters().getVdsStaticData());
        getCompensationContext().snapshotNewEntity(getParameters().getVdsStaticData());
        setVdsIdRef(getParameters().getVdsStaticData().getId());
        setVds(null);
    }

    private void AddVdsDynamicToDb() {
        VdsDynamic vdsDynamic = new VdsDynamic();
        vdsDynamic.setId(getParameters().getVdsStaticData().getId());
        // TODO: oVirt type - here oVirt behaves like power client?
        if (getParameters().getAddPending()) {
            vdsDynamic.setStatus(VDSStatus.PendingApproval);
        }
        else if (getParameters().getAddProvisioned()) {
            vdsDynamic.setStatus(VDSStatus.InstallingOS);
        }
        else if (Config.<Boolean> getValue(ConfigValues.InstallVds)) {
            vdsDynamic.setStatus(VDSStatus.Installing);
        }
        DbFacade.getInstance().getVdsDynamicDao().save(vdsDynamic);
        getCompensationContext().snapshotNewEntity(vdsDynamic);
    }

    private void AddVdsStatisticsToDb() {
        VdsStatistics vdsStatistics = new VdsStatistics();
        vdsStatistics.setId(getParameters().getVdsStaticData().getId());
        DbFacade.getInstance().getVdsStatisticsDao().save(vdsStatistics);
        getCompensationContext().snapshotNewEntity(vdsStatistics);
    }

    protected boolean validateVdsGroup() {
        if (getVdsGroup() == null) {
            return failCanDoAction(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
        }
        return true;
    }

    @Override
    protected boolean canDoAction() {
        setVdsGroupId(getParameters().getVdsStaticData().getVdsGroupId());
        getParameters().setVdsForUniqueId(null);
        // Check if this is a valid cluster
        boolean returnValue = validateVdsGroup();
        if (returnValue) {
            VDS vds = getParameters().getvds();
            String vdsName = vds.getName();
            String hostName = vds.getHostName();
            int maxVdsNameLength = Config.<Integer> getValue(ConfigValues.MaxVdsNameLength);
            // check that vds name is not null or empty
            if (vdsName == null || vdsName.isEmpty()) {
                returnValue = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
                // check that VDS name is not too long
            } else if (vdsName.length() > maxVdsNameLength) {
                returnValue = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
                // check that VDS hostname does not contain special characters.
            } else if (!ValidationUtils.validHostname(hostName)) {
                returnValue = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_VDS_HOSTNAME);
            } else if (getVdsDAO().getByName(vdsName) != null) {
                returnValue = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            } else if (getVdsDAO().getAllForHostname(hostName).size() != 0) {
                returnValue = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST);
            } else if (!ValidationUtils.validatePort(vds.getSshPort())) {
                returnValue = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_PORT);
            } else if ((StringUtils.isBlank(vds.getSshUsername())) ||
                       (vds.getSshUsername().length() > BusinessEntitiesDefinitions.USER_LOGIN_NAME_SIZE)) {
                returnValue = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_USERNAME);
            } else {
                returnValue = returnValue && validateSingleHostAttachedToLocalStorage();

                if (Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication)
                        && !EngineEncryptionUtils.haveKey()) {
                    returnValue = failCanDoAction(VdcBllMessages.VDS_TRY_CREATE_SECURE_CERTIFICATE_NOT_FOUND);
                } else if (!getParameters().getAddPending()
                        && (getParameters().getAuthMethod() == AuthenticationMethod.Password)
                        && StringUtils.isEmpty(getParameters().getPassword())) {
                    // We block vds installations if it's not a RHEV-H and password is empty
                    // Note that this may override local host SSH policy. See BZ#688718.
                    returnValue = failCanDoAction(VdcBllMessages.VDS_CANNOT_INSTALL_EMPTY_PASSWORD);
                } else if (!isPowerManagementLegal()) {
                    returnValue = false;
                } else {
                    returnValue = returnValue && canConnect(vds);
                }
            }
        }

        if (returnValue && getParameters().getNetworkProviderId() != null) {
            returnValue = validateNetworkProviderProperties(getParameters().getNetworkProviderId(),
                    getParameters().getNetworkMappings());
        }

        if (returnValue && isGlusterSupportEnabled()) {
            if (clusterHasServers()) {
                VDS upServer = getClusterUtils().getUpServer(getVdsGroupId());
                if (upServer == null) {
                    returnValue = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_GLUSTER_HOST_TO_PEER_PROBE);
                }
            }
        }
        return returnValue;
    }

    protected boolean isPowerManagementLegal() {
        return IsPowerManagementLegal(getParameters().getVdsStaticData(), getVdsGroup()
                .getcompatibility_version().toString());
    }

    private boolean clusterHasServers() {
        return getClusterUtils().hasServers(getVdsGroupId());
    }

    protected ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
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
        try {
            ByteArrayOutputStream out = new ConstraintByteArrayOutputStream(256);
            client.executeCommand(Config.<String> getValue(ConfigValues.GetVdsmIdByVdsmToolCommand),
                                  null, out, null);
            return new String(out.toByteArray(), Charset.forName("UTF-8"));
        }
        catch (Exception e) {
            log.warnFormat(
                    "Failed to initiate vdsm-id request on host with message {0}",
                    e.getMessage()
                    );
            return null;
        }
    }

    protected boolean canConnect(VDS vds) {
        // execute the connectivity and id uniqueness validation for VDS type hosts
        if (
            !getParameters().getAddPending() && !getParameters().getAddProvisioned() &&
            Config.<Boolean> getValue(ConfigValues.InstallVds)
        ) {
            try (final EngineSSHClient sshclient = getSSHClient()) {
                sshclient.connect();
                sshclient.authenticate();

                String hostUUID = getInstalledVdsIdIfExists(sshclient);
                if (hostUUID != null && getVdsDAO().getAllWithUniqueId(hostUUID).size() != 0) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_SAME_UUID_EXIST);
                }

                return isValidGlusterPeer(sshclient, vds.getVdsGroupId());
            } catch (AuthenticationException e) {
                log.errorFormat(
                        "Failed to authenticate session with host {0}",
                        vds.getName(),
                        e
                        );

                return failCanDoAction(VdcBllMessages.VDS_CANNOT_AUTHENTICATE_TO_SERVER);
            } catch (SecurityException e) {
                log.errorFormat(
                        "Failed to connect to host {0}, fingerprint: {1}",
                        vds.getName(),
                        vds.getSshKeyFingerprint(),
                        e
                        );
                addCanDoActionMessage(VdcBllMessages.VDS_SECURITY_CONNECTION_ERROR);
                addCanDoActionMessageVariable("ErrorMessage", e.getMessage());
                return failCanDoAction(VdcBllMessages.VDS_CANNOT_AUTHENTICATE_TO_SERVER);
            } catch (Exception e) {
                log.errorFormat(
                        "Failed to establish session with host {0}",
                        vds.getName(),
                        e
                        );

                return failCanDoAction(VdcBllMessages.VDS_CANNOT_CONNECT_TO_SERVER);
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
                Set<String> peers = getGlusterUtil().getPeers(sshclient);
                if (peers.size() > 0) {
                    for(String peer : peers) {
                        if(getGlusterDBUtils().serverExists(clusterId, peer)) {
                            // peer present in cluster. so server being added is valid.
                            return true;
                        }
                    }

                    // none of the peers present in the cluster. fail with appropriate error.
                    return failCanDoAction(VdcBllMessages.SERVER_ALREADY_PART_OF_ANOTHER_CLUSTER);
                }
            } catch (Exception e) {
                // This can happen if glusterd is not running on the server. Ignore it and let the server get added.
                // Peer probe will anyway fail later and the server will then go to non-operational status.
                log.debugFormat("Could not check if server {0} is already part of another gluster cluster. Will allow adding it.",
                        sshclient.getHost(),
                        e);
            }
        }
        return true;
    }

    protected GlusterDBUtils getGlusterDBUtils() {
        return GlusterDBUtils.getInstance();
    }

    protected GlusterUtil getGlusterUtil() {
        return GlusterUtil.getInstance();
    }

    protected boolean validateSingleHostAttachedToLocalStorage() {
        boolean retrunValue = true;
        StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().getForVdsGroup(
                getParameters().getVdsStaticData().getVdsGroupId());

        if (storagePool != null && storagePool.isLocal()) {
            if (!DbFacade.getInstance()
                    .getVdsStaticDao()
                    .getAllForVdsGroup(getParameters().getVdsStaticData().getVdsGroupId())
                    .isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
                retrunValue = false;
            }
        }
        return retrunValue;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getVdsGroupId(), VdcObjectType.VdsGroups,
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
}
