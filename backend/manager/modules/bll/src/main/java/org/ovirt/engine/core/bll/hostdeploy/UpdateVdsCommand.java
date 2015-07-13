package org.ovirt.engine.core.bll.hostdeploy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.validator.UpdateHostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVdsCommand<T extends UpdateVdsActionParameters>  extends VdsCommand<T>  implements RenamedEntityInfoProvider{

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private HostedEngineHelper hostedEngineHelper;

    private VDS oldHost;
    private static final List<String> UPDATE_FIELDS_VDS_BROKER = Arrays.asList(
            "host_name",
            "ip",
            "vds_unique_id",
            "port",
            "cluster_id",
            "protocol");
    private VdcActionType actionType;

    public UpdateVdsCommand(T parameters, CommandContext cmdContext) {
        this(parameters, cmdContext, VdcActionType.InstallVdsInternal);
    }

    public UpdateVdsCommand(T parameters, CommandContext commandContext, VdcActionType actionType) {
        super(parameters, commandContext);
        this.actionType = actionType;
    }

    public UpdateVdsCommand(Guid commandId) {
        this(commandId, VdcActionType.InstallVdsInternal);
    }

    protected UpdateVdsCommand(Guid commandId, VdcActionType actionType) {
        super(commandId);
        this.actionType = actionType;
    }

    @Override
    protected boolean validate() {
        oldHost = getVdsDao().get(getVdsId());
        UpdateHostValidator validator =
                new UpdateHostValidator(getDbFacade(),
                        oldHost,
                        getParameters().getvds(),
                        getParameters().isInstallHost(),
                        hostedEngineHelper);

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
                && validate(validator.changeProtocolAllowed())
                && validate(validator.hostProviderExists())
                && validate(validator.hostProviderTypeMatches())
                && validateNetworkProviderConfiguration()
                && isPowerManagementLegal(getParameters().getVdsStaticData().isPmEnabled(),
                        getParameters().getFenceAgents(),
                        oldHost.getClusterCompatibilityVersion().toString())
                && validate(validator.protocolIsNotXmlrpc())
                && validate(validator.supportsDeployingHostedEngine(
                        getParameters().getHostedEngineDeployConfiguration()));
    }

    private boolean validateNetworkProviderConfiguration() {
        return !getParameters().isInstallHost()
                || getParameters().getVdsStaticData().getOpenstackNetworkProviderId() == null
                || validateNetworkProviderProperties(getParameters().getVdsStaticData().getOpenstackNetworkProviderId(),
                        getParameters().getNetworkMappings());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    @Override
    protected void executeCommand() {
        updateVdsData();
        if (needToUpdateVdsBroker()) {
            initializeVds();
        }

        if (getParameters().isInstallHost()) {
            InstallVdsParameters tempVar = new InstallVdsParameters(getVdsId(), getParameters().getPassword());
            tempVar.setIsReinstallOrUpgrade(getParameters().isReinstallOrUpgrade());
            tempVar.setoVirtIsoFile(getParameters().getoVirtIsoFile());
            if (getVdsDao().get(getVdsId()).getStatus() ==  VDSStatus.InstallingOS) {
                // TODO: remove hack when reinstall api will provider override-firewall parameter.
                // https://bugzilla.redhat.com/show_bug.cgi?id=1177126 - for now we override firewall
                // configurations on each deploy for provisioned host to avoid wrong deployment.
                tempVar.setOverrideFirewall(true);
            } else {
                tempVar.setOverrideFirewall(getParameters().getOverrideFirewall());
            }
            tempVar.setOverrideFirewall(getParameters().getOverrideFirewall());
            tempVar.setActivateHost(getParameters().getActivateHost());
            tempVar.setNetworkProviderId(getParameters().getVdsStaticData().getOpenstackNetworkProviderId());
            tempVar.setNetworkMappings(getParameters().getNetworkMappings());
            tempVar.setAuthMethod(getParameters().getAuthMethod());
            if (getParameters().getHostedEngineDeployConfiguration() != null) {
                tempVar.setHostedEngineConfiguration(
                        hostedEngineHelper.createVdsDeployParams(getVdsId(),
                                getParameters().getHostedEngineDeployConfiguration().getDeployAction()));
            }
            ArrayList<VdcReturnValueBase> resultList = runInternalMultipleActions(
                    actionType, new ArrayList<>(Arrays.asList(tempVar)));

            // Since Host status is set to "Installing", failure of InstallVdsCommand will hang the Host to in that
            // status, therefore needed to fail the command to revert the status.
            if (!resultList.isEmpty()) {
                VdcReturnValueBase vdcReturnValueBase = resultList.get(0);
                if (vdcReturnValueBase != null && !vdcReturnValueBase.isValid()) {
                    ArrayList<String> validationMessages = vdcReturnValueBase.getValidationMessages();
                    if (!validationMessages.isEmpty()) {
                        // add can do action messages to return value so error messages
                        // are returned back to the client
                        getReturnValue().getValidationMessages().addAll(validationMessages);
                        log.error("Installation/upgrade of Host '{}', '{}' failed: {}",
                                getVdsId(),
                                getVdsName(),
                                StringUtils.join(Backend.getInstance()
                                        .getErrorsTranslator()
                                        .translateErrorText(validationMessages),
                                        ","));
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
            List<NetworkCluster> networkClusters = DbFacade.getInstance()
            .getNetworkClusterDao().getAllForCluster(oldHost.getClusterId());
            List<Network> networks = DbFacade.getInstance().getNetworkDao()
            .getAllForCluster(oldHost.getClusterId());
            for (NetworkCluster item : networkClusters) {
                for (Network net : networks) {
                    if (net.getId().equals(item.getNetworkId())) {
                        NetworkClusterHelper.setStatus(oldHost.getClusterId(), net);
                    }
                }
            }
        }
        alertIfPowerManagementNotConfigured(getParameters().getVdsStaticData());
        testVdsPowerManagementStatus(getParameters().getVdsStaticData());
        checkKdumpIntegrationStatus();
        setSucceeded(true);
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
                DbFacade.getInstance().getVdsStaticDao().update(getParameters().getVdsStaticData());
                updateFenceAgents();// TODO: what compensation needed for fencing?
                getCompensationContext().stateChanged();
                return null;
            }

            private void updateFenceAgents() {
                if (getParameters().getFenceAgents() != null) { // if == null, means no update. Empty list means
                                                                  // delete agents.
                    DbFacade.getInstance().getFenceAgentDao().removeByVdsId(getVdsId());
                    for (FenceAgent agent : getParameters().getFenceAgents()) {
                        agent.setHostId(getVdsId());
                        DbFacade.getInstance().getFenceAgentDao().save(agent);
                    }
                }
            }
        });

    }

    private boolean needToUpdateVdsBroker() {
        return VdsHandler.isFieldsUpdated(getParameters().getVdsStaticData(), oldHost.getStaticData(),
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
    public void setEntityId(AuditLogableBase logable) {
        logable.setVdsId(oldHost.getId());
    }

    private void checkKdumpIntegrationStatus() {
        VdsStatic vdsSt = getParameters().getVdsStaticData();
        if (vdsSt.isPmEnabled() && vdsSt.isPmKdumpDetection()) {
            VdsDynamic vdsDyn = getDbFacade().getVdsDynamicDao().get(vdsSt.getId());
            if (vdsDyn != null && vdsDyn.getKdumpStatus() != KdumpStatus.ENABLED) {
                auditLogDirector.log(
                        new AuditLogableBase(vdsSt.getId()),
                        AuditLogType.KDUMP_DETECTION_NOT_CONFIGURED_ON_VDS
                );
            }
        }
    }

    @Override
    protected boolean isPowerManagementLegal(boolean pmEnabled,
                                             List<FenceAgent> fenceAgents,
                                             String clusterCompatibilityVersion) {
        return super.isPowerManagementLegal(pmEnabled, fenceAgents, clusterCompatibilityVersion);
    }
}
