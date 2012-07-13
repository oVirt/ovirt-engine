package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHostAddVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.CommandParametersInitializer;
import org.ovirt.engine.core.utils.FileUtil;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVdsCommand<T extends AddVdsActionParameters> extends VdsCommand<T> {

    static {
        CommandParametersInitializer initializer = new CommandParametersInitializer();
        initializer.AddParameter(VdsStatic.class, "mVds");
    }

    private VDS upServer;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddVdsCommand(Guid commandId) {
        super(commandId);
    }

    public AddVdsCommand(T parametars) {
        super(parametars);
        setVdsGroupId(parametars.getvds().getvds_group_id());
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
        if (getVdsGroup().getstorage_pool_id() != null) {
            VdsActionParameters tempVar = new VdsActionParameters(getVdsIdRef().getValue());
            tempVar.setSessionId(getParameters().getSessionId());
            tempVar.setCompensationEnabled(true);
            CompensationContext compensationContext = getCompensationContext();
            VdcReturnValueBase addVdsSpmIdReturn =
                    Backend.getInstance().runInternalAction(VdcActionType.AddVdsSpmId,
                            tempVar,
                            new CommandContext(compensationContext));
            if (!addVdsSpmIdReturn.getSucceeded()) {
                setSucceeded(false);
                getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                return;
            }
        }
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                InitializeVds();
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
        // do not install vds's which added in pending mode (currently power
        // clients). they are installed as part of the approve process
        if (Config.<Boolean> GetValue(ConfigValues.InstallVds) && !getParameters().getAddPending()) {
            final InstallVdsParameters installVdsParameters = new InstallVdsParameters(getVdsId(),
                    getParameters().getRootPassword());
            installVdsParameters.setOverrideFirewall(getParameters().getOverrideFirewall());
            installVdsParameters.setRebootAfterInstallation(getParameters().isRebootAfterInstallation());
            Map<String, String> values = new HashMap<String, String>();
            values.put(VdcObjectType.VDS.name().toLowerCase(), getParameters().getvds().getvds_name());
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
                    Backend.getInstance().runInternalAction(VdcActionType.InstallVds,
                            installVdsParameters,
                            new CommandContext(installCtx));
                }
            });
            ExecutionHandler.setAsyncJob(getExecutionContext(), true);
        } else {
            // If cluster supports gluster service do gluster peer probe
            // only on non vds installation mode.
            if (getVdsGroup().supportsGlusterService() && getAllVds(getVdsGroupId()).size() > 1) {
                String hostName =
                        (getParameters().getvds().gethost_name().isEmpty()) ? getParameters().getvds().getManagmentIp()
                                : getParameters().getvds().gethost_name();
                VDSReturnValue returnValue =
                        runVdsCommand(
                                VDSCommandType.GlusterHostAdd,
                                new GlusterHostAddVDSParameters(upServer.getId(),
                                        hostName));
                setSucceeded(returnValue.getSucceeded());
                if (!getSucceeded()) {
                    getReturnValue().getFault().setError(returnValue.getVdsError().getCode());
                    getReturnValue().getFault().setMessage(returnValue.getVdsError().getMessage());
                    return;
                }
            }
        }
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

        final VDS vds = DbFacade.getInstance().getVdsDAO().get(oVirtId);
        if (vds == null || !VdsHandler.isPendingOvirt(vds)) {
            return false;
        }

        String vdsName = getParameters().getVdsStaticData().getvds_name();
        log.infoFormat("Host {0}, id {1} of type {2} is being re-registered as Host {3}",
                vds.getvds_name(),
                vds.getId(),
                vds.getvds_type().name(),
                vdsName);
        VdcReturnValueBase result =
                TransactionSupport.executeInNewTransaction(new TransactionMethod<VdcReturnValueBase>() {
                    @Override
                    public VdcReturnValueBase runInTransaction() {
                        return Backend.getInstance().runInternalAction(VdcActionType.RemoveVds,
                                new VdsActionParameters(oVirtId));
                    }
                });

        if (!result.getSucceeded()) {
            String errors =
                    result.getCanDoAction() ? result.getFault().getError().name()
                            : StringUtils.join(result.getCanDoActionMessages(), ",");
            log.warnFormat("Failed to remove Host {0}, id {1}, re-registering it as Host {2} fails with errors {3}",
                    vds.getvds_name(),
                    vds.getId(),
                    vdsName,
                    errors);
        } else {
            log.infoFormat("Host {0} is now known as Host {2}",
                    vds.getvds_name(),
                    vdsName);
        }

        return result.getSucceeded();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_VDS : AuditLogType.USER_FAILED_ADD_VDS;
    }

    private void AddVdsStaticToDb() {
        getParameters().getVdsStaticData().setserver_SSL_enabled(
                Config.<Boolean> GetValue(ConfigValues.UseSecureConnectionWithServers));
        DbFacade.getInstance().getVdsStaticDAO().save(getParameters().getVdsStaticData());
        getCompensationContext().snapshotNewEntity(getParameters().getVdsStaticData());
        setVdsIdRef(getParameters().getVdsStaticData().getId());
        setVds(null);
    }

    private void AddVdsDynamicToDb() {
        VdsDynamic vdsDynamic = new VdsDynamic();
        vdsDynamic.setId(getParameters().getVdsStaticData().getId());
        // TODO: oVirt type - here oVirt behaves like power client?
        if (Config.<Boolean> GetValue(ConfigValues.InstallVds)
                && getParameters().getVdsStaticData().getvds_type() == VDSType.VDS) {
            vdsDynamic.setstatus(VDSStatus.Installing);
        } else if (getParameters().getAddPending()) {
            vdsDynamic.setstatus(VDSStatus.PendingApproval);
        }
        DbFacade.getInstance().getVdsDynamicDAO().save(vdsDynamic);
        getCompensationContext().snapshotNewEntity(vdsDynamic);
    }

    private void AddVdsStatisticsToDb() {
        VdsStatistics vdsStatistics = new VdsStatistics();
        vdsStatistics.setId(getParameters().getVdsStaticData().getId());
        DbFacade.getInstance().getVdsStatisticsDAO().save(vdsStatistics);
        getCompensationContext().snapshotNewEntity(vdsStatistics);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        setVdsGroupId(getParameters().getVdsStaticData().getvds_group_id());
        getParameters().setVdsForUniqueId(null);
        // Check if this is a valid cluster
        if (getVdsGroup() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            returnValue = false;
        } else {
            VDS vds = getParameters().getvds();
            String vdsName = vds.getvds_name();
            String hostName = vds.gethost_name();
            int maxVdsNameLength = Config.<Integer> GetValue(ConfigValues.MaxVdsNameLength);
            // check that vds name is not null or empty
            if (vdsName == null || vdsName.isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
                returnValue = false;
                // check that VDS name is not too long
            } else if (vdsName.length() > maxVdsNameLength) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
                returnValue = false;
                // check that VDS hostname does not contain special characters.
            } else if (!ValidationUtils.validHostname(hostName)) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_VDS_HOSTNAME);
                returnValue = false;
            } else {
                returnValue = returnValue && validateSingleHostAttachedToLocalStorage();

                if (Config.<Boolean> GetValue(ConfigValues.UseSecureConnectionWithServers)
                        && !FileUtil.fileExists(Config.resolveCertificatePath())) {
                    addCanDoActionMessage(VdcBllMessages.VDS_TRY_CREATE_SECURE_CERTIFICATE_NOT_FOUND);
                    returnValue = false;
                } else if (!getParameters().getAddPending()
                        && StringUtils.isEmpty(getParameters().getRootPassword())) {
                    // We block vds installations if it's not a RHEV-H and password is empty
                    // Note that this may override local host SSH policy. See BZ#688718.
                    addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_INSTALL_EMPTY_PASSWORD);
                    returnValue = false;
                } else if (!IsPowerManagementLegal(getParameters().getVdsStaticData(), getVdsGroup()
                            .getcompatibility_version().toString())) {
                    returnValue = false;
                } else if (getParameters().getVdsStaticData().getport() < 1
                            || getParameters().getVdsStaticData().getport() > 65536) {
                    addCanDoActionMessage(VdcBllMessages.VDS_PORT_IS_NOT_LEGAL);
                    returnValue = false;
                } else {
                    returnValue = returnValue && validateHostUniqueness(vds);
                }
            }
        }
        if (getVdsGroup().supportsGlusterService()) {
            if (clusterHasServers()) {
                upServer = ClusterUtils.getInstance().getUpServer(getVdsGroupId());
                if (upServer == null) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_GLUSTER_HOST_TO_PEER_PROBE);
                    returnValue = false;
                }
            }
        }

        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        }
        return returnValue;
    }

    private boolean clusterHasServers() {
        return ClusterUtils.getInstance().hasServers(getVdsGroupId());
    }

    public VdsInstallHelper getVdsInstallHelper() {
        return new VdsInstallHelper();
    }

    private boolean validateHostUniqueness(VDS vds) {
        boolean retrunValue = true;

        // execute the connectivity and id uniqueness validation for VDS type hosts
        if (vds.getvds_type() == VDSType.VDS) {
            VdsInstallHelper installHelper = getVdsInstallHelper();
            try {
                Long timeout =
                        TimeUnit.SECONDS.toMillis(Config.<Integer> GetValue(ConfigValues.ConnectToServerTimeoutInSeconds));
                if (!installHelper.connectToServer(vds.gethost_name(), getParameters().getRootPassword(), timeout)) {
                    addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_CONNECT_TO_SERVER);
                    retrunValue = false;
                } else {
                    String serverUniqueId = installHelper.getServerUniqueId();
                    if (serverUniqueId != null) {
                        serverUniqueId = serverUniqueId.trim();
                    }
                    retrunValue = retrunValue && validateHostUniqueId(vds, serverUniqueId);
                }
            } finally {
                try {
                    installHelper.wrapperShutdown();
                } catch (Exception e) {
                    log.errorFormat("Failed to terminate session with host {0} with message: {1}",
                            vds.getvds_name(),
                            ExceptionUtils.getMessage(e));
                    log.debug(e);
                }
            }
        }

        return retrunValue && validateHostUniqueNameAndAddress(getParameters().getVdsStaticData());
    }

    private boolean validateHostUniqueNameAndAddress(VdsStatic vdsStaticData) {
        // having oVirt in pending approval state allows having a host with same name and address
        Guid vdsForUniqueId = getParameters().getVdsForUniqueId();
        if (vdsForUniqueId == null) {
            return !VdsHandler.isVdsExist(vdsStaticData,
                    getReturnValue().getCanDoActionMessages());
        }
        return !VdsHandler.isVdsExistForPendingOvirt(vdsStaticData,
                getReturnValue().getCanDoActionMessages(), vdsForUniqueId);
    }

    /**
     * Validate if same unique-id associated with the given host exists in other hosts.<br>
     * There should be up to one host with the same unique-id besides the new host.<br>
     * If host typed oVirt has the same unique id, in status PendingApproval, set that host-id<br>
     * on the parameters member of the command.
     *
     * @param vds
     *            a new host to be added
     * @param serverUniqueId
     *            a new host unique-id
     * @return true - if no host is associated with the unique-id, or if there is a single oVirt node in PendingApproval
     *         status, else - false.
     */
    private boolean validateHostUniqueId(VDS vds, String serverUniqueId) {
        boolean retrunValue = true;
        List<VDS> vdssByUniqueId = VdsInstallHelper.getVdssByUniqueId(vds.getId(), serverUniqueId);

        if (!vdssByUniqueId.isEmpty()) {
            if (vdssByUniqueId.size() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append(vdssByUniqueId.get(0));
                for (int i = 1; i < vdssByUniqueId.size(); i++) {
                    sb.append(", ").append(vdssByUniqueId.get(i).getvds_name());
                }
                addCanDoActionMessage(VdcBllMessages.VDS_REGISTER_UNIQUE_ID_AMBIGUOUS);
                addCanDoActionMessage(String.format("$HostNameList %1$s", sb.toString()));
                retrunValue = false;
            } else {
                VDS existedVds = vdssByUniqueId.get(0);
                if (vds.getvds_type() == VDSType.VDS
                        && existedVds.getvds_type() == VDSType.oVirtNode
                        && (existedVds.getstatus() == VDSStatus.PendingApproval)) {
                    getParameters().setVdsForUniqueId(existedVds.getId());
                } else {
                    addCanDoActionMessage(VdcBllMessages.VDS_REGISTER_UNIQUE_ID_AMBIGUOUS);
                    addCanDoActionMessage(String.format("$HostNameList %1$s", existedVds.getvds_name()));
                    retrunValue = false;
                }
            }
        }
        return retrunValue;
    }

    private boolean validateSingleHostAttachedToLocalStorage() {
        boolean retrunValue = true;
        storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().getForVdsGroup(
                getParameters().getVdsStaticData().getvds_group_id());

        if (storagePool != null && storagePool.getstorage_pool_type() == StorageType.LOCALFS) {
            if (!DbFacade.getInstance()
                    .getVdsStaticDAO()
                    .getAllForVdsGroup(getParameters().getVdsStaticData().getvds_group_id())
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
        if (getParameters().getVdsStaticData().getpm_enabled()) {
            addValidationGroup(PowerManagementCheck.class);
        }
        return super.getValidationGroups();
    }

    private static Log log = LogFactory.getLog(AddVdsCommand.class);

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            VDS vds = getParameters().getvds();

            String vdsName = (vds != null && vds.getvds_name() != null) ? vds.getvds_name() : "";
            jobProperties.put(VdcObjectType.VDS.name().toLowerCase(), vdsName);
        }
        return jobProperties;
    }

    /**
     * This method will return the list of all vds from the cluster
     *
     * @param clusterId
     * @return List of Vds
     */
    private List<VDS> getAllVds(Guid clusterId) {
        return getVdsDAO().getAllForVdsGroup(clusterId);
    }
}
