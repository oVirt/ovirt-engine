package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
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
        VdsStatic updatedHost = getParameters().getVdsStaticData();

        if (oldHost == null || updatedHost == null) {
            return failCanDoAction(VdcBllMessages.VDS_INVALID_SERVER_ID);
        }

        String compatibilityVersion = oldHost.getVdsGroupCompatibilityVersion().toString();

        if (!VdsHandler.isUpdateValid(updatedHost, oldHost.getStaticData(), oldHost.getStatus())) {
            return failCanDoAction(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE);
        }

        String vdsName = updatedHost.getName();
        if (vdsName == null || vdsName.isEmpty()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
        }

        // check that VDS name is not too long
        int maxVdsNameLength = Config.<Integer> getValue(ConfigValues.MaxVdsNameLength);
        if (vdsName.length() > maxVdsNameLength) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
        }

        String hostName = updatedHost.getHostName();
        if (oldHost.getStatus() != VDSStatus.InstallFailed && !oldHost.getHostName().equals(hostName)) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_HOSTNAME_CANNOT_CHANGE);
        }

        // check if a name is updated to an existing vds name
        if (!StringUtils.equalsIgnoreCase(oldHost.getName(), updatedHost.getName())
                && getVdsDAO().getByName(vdsName) != null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
        }

        if (!StringUtils.equalsIgnoreCase(oldHost.getHostName(), updatedHost.getHostName())
                && getVdsDAO().getAllForHostname(hostName).size() != 0) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST);
        }

        if (getParameters().isInstallHost()
                && oldHost.getStatus() != VDSStatus.Maintenance
                && oldHost.getStatus() != VDSStatus.NonOperational
                && oldHost.getStatus() != VDSStatus.InstallFailed
                && oldHost.getStatus() != VDSStatus.InstallingOS) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_INSTALL_STATUS_ILLEGAL);
        }

        if (getParameters().isInstallHost()
                && getParameters().getAuthMethod() == AuthenticationMethod.Password
                && StringUtils.isEmpty(getParameters().getPassword())
                && updatedHost.getVdsType() == VDSType.VDS) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_INSTALL_EMPTY_PASSWORD);
        }

        if (!getParameters().isInstallHost() && oldHost.getPort() != updatedHost.getPort()) {
            return failCanDoAction(VdcBllMessages.VDS_PORT_CHANGE_REQUIRE_INSTALL);
        }

        // Forbid updating group id - this must be done through ChangeVDSClusterCommand
        // This is due to permission check that must be done both on the VDS and on the VDSGroup
        if (!oldHost.getVdsGroupId().equals(updatedHost.getVdsGroupId())) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_UPDATE_CLUSTER);
        }

        if (getParameters().isInstallHost()
                && getParameters().getNetworkProviderId() != null
                && !validateNetworkProviderProperties(getParameters().getNetworkProviderId(),
                        getParameters().getNetworkMappings())) {
            return false;
        }

        if (updatedHost.getProtocol() != oldHost.getProtocol()
                && oldHost.getStatus() != VDSStatus.Maintenance
                && oldHost.getStatus() != VDSStatus.InstallingOS) {
            return failCanDoAction(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE);
        }

        // if all ok check PM is legal
        return isPowerManagementLegal(updatedHost.isPmEnabled(), getParameters().getFenceAgents(), compatibilityVersion);
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
                AuditLogDirector.log(
                        new AuditLogableBase(vdsSt.getId()),
                        AuditLogType.KDUMP_DETECTION_NOT_CONFIGURED_ON_VDS
                );
            }
        }
    }
}
