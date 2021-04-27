package org.ovirt.engine.core.bll.hostdeploy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.bll.validator.UpdateHostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVdsCommand<T extends UpdateVdsActionParameters>  extends VdsCommand<T>  implements RenamedEntityInfoProvider{

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private HostedEngineHelper hostedEngineHelper;

    @Inject
    private VdsHandler vdsHandler;

    @Inject
    private NetworkClusterHelper networkClusterHelper;

    @Inject
    private AffinityValidator affinityValidator;

    private VDS oldHost;
    private static final List<String> UPDATE_FIELDS_VDS_BROKER = Arrays.asList(
            "host_name",
            "ip",
            "vds_unique_id",
            "port",
            "cluster_id",
            "protocol");
    private ActionType actionType;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private FenceAgentDao fenceAgentDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private LabelDao labelDao;

    private BiConsumer<AuditLogable, AuditLogDirector> affinityGroupLoggingMethod = (a, b) -> {};

    public UpdateVdsCommand(T parameters, CommandContext cmdContext) {
        this(parameters, cmdContext, ActionType.InstallVdsInternal);
    }

    public UpdateVdsCommand(T parameters, CommandContext commandContext, ActionType actionType) {
        super(parameters, commandContext);
        this.actionType = actionType;
    }

    public UpdateVdsCommand(Guid commandId) {
        this(commandId, ActionType.InstallVdsInternal);
    }

    protected UpdateVdsCommand(Guid commandId, ActionType actionType) {
        super(commandId);
        this.actionType = actionType;
    }

    @Override
    protected boolean validate() {
        oldHost = vdsDao.get(getVdsId());
        // if fence agents list is null, try to fill it from database
        // so we can enable changing other host attributes from API
        // without failing validation or removing the persisted fence agents
        boolean validateAgents=true;
        if (getParameters().getFenceAgents() == null) {
            getParameters().setFenceAgents(fenceAgentDao.getFenceAgentsForHost(getVdsId()));
            validateAgents = false;
        }
        UpdateHostValidator validator =
                getUpdateHostValidator(oldHost, getParameters().getvds(), getParameters().isInstallHost());

        return validate(validator.hostExists())
                && validate(validator.hostStatusValid())
                && validate(validator.nameNotEmpty())
                && validate(validator.nameLengthIsLegal())
                && validate(validator.updateHostAddressAllowed())
                && validate(validator.nameNotUsed())
                && validate(validator.hostNameNotUsed())
                && validate(validator.statusSupportedForHostInstallation())
                && validate(validator.passwordProvidedForHostInstallation(getParameters().getAuthMethod(),
                        getParameters().getPassword()))
                && validate(validator.updatePortAllowed())
                && validate(validator.clusterNotChanged())
                && validate(validator.hostProviderExists())
                && validate(validator.hostProviderTypeMatches())
                && isPowerManagementLegal(getParameters().getVdsStaticData().isPmEnabled(),
                        getParameters().getFenceAgents(),
                        oldHost.getClusterCompatibilityVersion().toString(),
                        validateAgents)
                && validate(
                        validator.supportsDeployingHostedEngine(getParameters().getHostedEngineDeployConfiguration(),
                                getCluster(),
                                oldHost.isHostedEngineDeployed()))
                && validate(validateAffinityGroups());
    }

    UpdateHostValidator getUpdateHostValidator(VDS oldHost, VDS updatedHost, boolean installHost) {
        return UpdateHostValidator.createInstance(
                oldHost,
                updatedHost,
                installHost);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    @Override
    protected void executeCommand() {
        getParameters().getVdsStaticData().setReinstallRequired(shouldVdsBeReinstalled());
        updateVdsData();
        if (needToUpdateVdsBroker()) {
            initializeVds();
        }

        if (getParameters().isInstallHost()) {
            InstallVdsParameters tempVar = new InstallVdsParameters(getVdsId(), getParameters().getPassword());
            tempVar.setIsReinstallOrUpgrade(getParameters().isReinstallOrUpgrade());
            tempVar.setoVirtIsoFile(getParameters().getoVirtIsoFile());
            if (vdsDynamicDao.get(getVdsId()).getStatus() ==  VDSStatus.InstallingOS) {
                // TODO: remove hack when reinstall api will provider override-firewall parameter.
                // https://bugzilla.redhat.com/show_bug.cgi?id=1177126 - for now we override firewall
                // configurations on each deploy for provisioned host to avoid wrong deployment.
                tempVar.setOverrideFirewall(true);
            } else {
                tempVar.setOverrideFirewall(getParameters().getOverrideFirewall());
            }
            tempVar.setOverrideFirewall(getParameters().getOverrideFirewall());
            tempVar.setActivateHost(getParameters().getActivateHost());
            tempVar.setRebootHost(getParameters().getRebootHost());
            tempVar.setAuthMethod(getParameters().getAuthMethod());
            tempVar.setHostedEngineDeployConfiguration(getParameters().getHostedEngineDeployConfiguration());
            tempVar.setReplaceHostConfiguration(getParameters().getReplaceHostConfiguration());
            tempVar.setFqdnBox(null);
            tempVar.setReconfigureGluster(false);

            List<ActionReturnValue> resultList = runInternalMultipleActions(
                    actionType,
                    new ArrayList<>(Arrays.asList(tempVar)),
                    ExecutionHandler.createInternalJobContext().getExecutionContext());

            // Since Host status is set to "Installing", failure of InstallVdsCommand will hang the Host to in that
            // status, therefore needed to fail the command to revert the status.
            if (!resultList.isEmpty()) {
                ActionReturnValue actionReturnValue = resultList.get(0);
                if (actionReturnValue != null && !actionReturnValue.isValid()) {
                    List<String> validationMessages = actionReturnValue.getValidationMessages();
                    if (!validationMessages.isEmpty()) {
                        // add can do action messages to return value so error messages
                        // are returned back to the client
                        getReturnValue().getValidationMessages().addAll(validationMessages);
                        log.error("Installation/upgrade of Host '{}', '{}' failed: {}",
                                getVdsId(),
                                getVdsName(),
                                StringUtils.join
                                        (backend.getErrorsTranslator().translateErrorText(validationMessages), ","));
                    }
                    // set can do action to false so can do action messages are
                    // returned back to client
                    getReturnValue().setValid(false);
                    setSucceeded(false);
                    // add old vds dynamic data to compensation context. This
                    // way the status will revert back to what it was before
                    // starting installation process
                    getCompensationContext().snapshotEntityStatus(oldHost.getDynamicData());
                    getCompensationContext().stateChanged();
                    return;
                }
            }
        }

        // set clusters network to be operational (if needed)
        if (oldHost.getStatus() == VDSStatus.Up) {
            List<Network> networks = networkDao.getAllForCluster(oldHost.getClusterId());
            networkClusterHelper.setStatus(oldHost.getClusterId(), networks);
        }
        alertIfPowerManagementNotConfigured(getParameters().getVdsStaticData());
        testVdsPowerManagementStatus(getParameters().getVdsStaticData());
        checkKdumpIntegrationStatus();
        addAffinityGroupsAndLabels();
        setSucceeded(true);
    }

    private boolean shouldVdsBeReinstalled() {
        VdsStatic vdsStatic = getParameters().getVdsStaticData();
        VdsStatic oldVdsStatic = oldHost.getStaticData();
        return vdsStatic.isReinstallRequired() ||
                !Objects.equals(vdsStatic.isPmKdumpDetection(), oldVdsStatic.isPmKdumpDetection()) ||
                !Objects.equals(vdsStatic.getCurrentKernelCmdline(), oldVdsStatic.getCurrentKernelCmdline());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VDS : AuditLogType.USER_FAILED_UPDATE_VDS;
    }

    private void updateVdsData() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(getVds().getStaticData());
                vdsStaticDao.update(getParameters().getVdsStaticData());
                updateFenceAgents();// TODO: what compensation needed for fencing?
                getCompensationContext().stateChanged();
                return null;
            }

            private void updateFenceAgents() {
                if (getParameters().getFenceAgents() != null) { // if == null, means no update. Empty list means
                                                                  // delete agents.
                    fenceAgentDao.removeByVdsId(getVdsId());
                    for (FenceAgent agent : getParameters().getFenceAgents()) {
                        agent.setHostId(getVdsId());
                        fenceAgentDao.save(agent);
                    }
                }
            }
        });

    }

    private boolean needToUpdateVdsBroker() {
        return vdsHandler.isFieldsUpdated(getParameters().getVdsStaticData(), oldHost.getStaticData(),
                UPDATE_FIELDS_VDS_BROKER);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        if (getParameters().getVdsStaticData().isPmEnabled()) {
            addValidationGroup(PowerManagementCheck.class);
        }
        return super.getValidationGroups();
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.VDS.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return oldHost.getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getVdsStaticData().getName();
    }

    @Override
    public void setEntityId(AuditLogable logable) {
        logable.setVdsId(oldHost.getId());
    }

    private void checkKdumpIntegrationStatus() {
        VdsStatic vdsSt = getParameters().getVdsStaticData();
        if (vdsSt.isPmEnabled() && vdsSt.isPmKdumpDetection()) {
            VdsDynamic vdsDyn = vdsDynamicDao.get(vdsSt.getId());
            if (vdsDyn != null && vdsDyn.getKdumpStatus() != KdumpStatus.ENABLED) {
                AuditLogable logable = new AuditLogableImpl();
                logable.setVdsId(vdsSt.getId());
                logable.setVdsName(vdsSt.getName());
                auditLogDirector.log(logable, AuditLogType.KDUMP_DETECTION_NOT_CONFIGURED_ON_VDS);
            }
        }
    }

    @Override
    protected boolean isPowerManagementLegal(boolean pmEnabled,
                                             List<FenceAgent> fenceAgents,
                                             String clusterCompatibilityVersion,
                                             boolean validateAgents) {
        return super.isPowerManagementLegal(pmEnabled, fenceAgents, clusterCompatibilityVersion, validateAgents);
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
            labelDao.updateLabelsForHost(getVdsId(), labelIds);
        }
    }
}
