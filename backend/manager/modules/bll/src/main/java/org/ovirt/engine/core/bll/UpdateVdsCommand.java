package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.validator.UpdateHostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
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

    private VDS oldHost;
    private static final List<String> UPDATE_FIELDS_VDS_BROKER = Arrays.asList("host_name", "ip", "vds_unique_id", "port", "vds_group_id");
    private VdcActionType actionType;

    public UpdateVdsCommand(T parameters) {
        this(parameters, VdcActionType.InstallVdsInternal);
    }

    public UpdateVdsCommand(T parameters, VdcActionType actionType) {
        super(parameters);
        this.actionType = actionType;
    }

    protected UpdateVdsCommand(Guid commandId) {
        this(commandId, VdcActionType.InstallVdsInternal);
    }

    protected UpdateVdsCommand(Guid commandId, VdcActionType actionType) {
        super(commandId);
        this.actionType = actionType;
    }

    @Override
    protected boolean canDoAction() {
        oldHost = getVdsDAO().get(getVdsId());
        UpdateHostValidator validator =
                new UpdateHostValidator(getDbFacade(),
                        oldHost,
                        getParameters().getvds(),
                        getParameters().isInstallHost());

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
                        oldHost.getVdsGroupCompatibilityVersion().toString());
    }

    private boolean validateNetworkProviderConfiguration() {
        return !getParameters().isInstallHost()
                || getParameters().getNetworkProviderId() == null
                || validateNetworkProviderProperties(getParameters().getNetworkProviderId(),
                        getParameters().getNetworkMappings());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
    }

    @Override
    protected void executeCommand() {
        updateVdsData();
        if (NeedToUpdateVdsBroker()) {
            initializeVds();
        }

        if (getParameters().isInstallHost()) {
            InstallVdsParameters tempVar = new InstallVdsParameters(getVdsId(), getParameters().getPassword());
            tempVar.setIsReinstallOrUpgrade(getParameters().isReinstallOrUpgrade());
            tempVar.setoVirtIsoFile(getParameters().getoVirtIsoFile());
            if (getVdsDAO().get(getVdsId()).getStatus() ==  VDSStatus.InstallingOS) {
                // TODO: remove hack when reinstall api will provider override-firewall parameter.
                // https://bugzilla.redhat.com/show_bug.cgi?id=1177126 - for now we override firewall
                // configurations on each deploy for provisioned host to avoid wrong deployment.
                tempVar.setOverrideFirewall(true);
            } else {
                tempVar.setOverrideFirewall(getParameters().getOverrideFirewall());
            }
            tempVar.setOverrideFirewall(getParameters().getOverrideFirewall());
            tempVar.setActivateHost(getParameters().getActivateHost());
            tempVar.setRebootAfterInstallation(getParameters().isRebootAfterInstallation());
            tempVar.setNetworkProviderId(getParameters().getNetworkProviderId());
            tempVar.setNetworkMappings(getParameters().getNetworkMappings());
            tempVar.setAuthMethod(getParameters().getAuthMethod());
            ArrayList<VdcReturnValueBase> resultList = runInternalMultipleActions(
                    actionType,
                    new ArrayList<VdcActionParametersBase>(Arrays
                            .asList(tempVar)));

            // Since Host status is set to "Installing", failure of InstallVdsCommand will hang the Host to in that
            // status, therefore needed to fail the command to revert the status.
            if (!resultList.isEmpty()) {
                VdcReturnValueBase vdcReturnValueBase = resultList.get(0);
                if (vdcReturnValueBase != null && !vdcReturnValueBase.getCanDoAction()) {
                    ArrayList<String> canDoActionMessages = vdcReturnValueBase.getCanDoActionMessages();
                    if (!canDoActionMessages.isEmpty()) {
                        // add can do action messages to return value so error messages
                        // are returned back to the client
                        getReturnValue().getCanDoActionMessages().addAll(canDoActionMessages);
                        log.error("Installation/upgrade of Host '{}', '{}' failed: {}",
                                getVdsId(),
                                getVdsName(),
                                StringUtils.join(Backend.getInstance()
                                        .getErrorsTranslator()
                                        .TranslateErrorText(canDoActionMessages),
                                        ","));
                    }
                    // set can do action to false so can do action messages are
                    // returned back to client
                    getReturnValue().setCanDoAction(false);
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

        if (oldHost.getProtocol() != getParameters().getVdsStaticData().getProtocol()) {
            ResourceManager.getInstance().reestablishConnection(oldHost.getId());
        }

        // set clusters network to be operational (if needed)
        if (oldHost.getStatus() == VDSStatus.Up) {
            List<NetworkCluster> networkClusters = DbFacade.getInstance()
            .getNetworkClusterDao().getAllForCluster(oldHost.getVdsGroupId());
            List<Network> networks = DbFacade.getInstance().getNetworkDao()
            .getAllForCluster(oldHost.getVdsGroupId());
            for (NetworkCluster item : networkClusters) {
                for (Network net : networks) {
                    if (net.getId().equals(item.getNetworkId())) {
                        NetworkClusterHelper.setStatus(oldHost.getVdsGroupId(), net);
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

    private boolean NeedToUpdateVdsBroker() {
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
}
