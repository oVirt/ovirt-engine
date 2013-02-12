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
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVdsCommand<T extends UpdateVdsActionParameters>  extends VdsCommand<T>  implements RenamedEntityInfoProvider{

    private VDS _oldVds;
    private static final List<String> UPDATE_FIELDS_VDS_BROKER = Arrays.asList("host_name", "ip", "vds_unique_id", "port", "vds_group_id");

    public UpdateVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = false;
        _oldVds = DbFacade.getInstance().getVdsDao().get(getVdsId());

        if (_oldVds != null && getParameters().getVdsStaticData() != null) {
            String compatibilityVersion = _oldVds.getVdsGroupCompatibilityVersion().toString();

            if (VdsHandler.IsUpdateValid(getParameters().getVdsStaticData(), _oldVds.getStaticData(),
                    _oldVds.getStatus())) {
                if ("".equals(getParameters().getVdsStaticData().getName())) {
                    addCanDoActionMessage(VdcBllMessages.VDS_TRY_CREATE_WITH_EXISTING_PARAMS);
                }
                String vdsName = getParameters().getvds().getName();
                String hostName = getParameters().getvds().getHostName();
                int maxVdsNameLength = Config.<Integer> GetValue(ConfigValues.MaxVdsNameLength);
                // check that VDS name is not null or empty
                if (vdsName == null || vdsName.isEmpty()) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
                    returnValue = false;
                    // check that VDS name is not too long
                } else if (vdsName.length() > maxVdsNameLength) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
                    returnValue = false;
                } else if (_oldVds.getStatus() != VDSStatus.InstallFailed && !_oldVds.getHostName().equals(hostName)) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_HOSNAME_CANNOT_CHANGE);
                    returnValue = false;
                }
                // check if a name is updated to an existing vds name
                else if (!StringUtils.equals(_oldVds.getName().toLowerCase(), getParameters().getVdsStaticData()
                        .getName().toLowerCase())
                        && VdsHandler.isVdsWithSameNameExistStatic(getParameters().getVdsStaticData().getName())) {
                    addCanDoActionMessage(VdcBllMessages.VDS_TRY_CREATE_WITH_EXISTING_PARAMS);
                } else if (!StringUtils.equals(_oldVds.getHostName().toLowerCase(), getParameters().getVdsStaticData()
                        .getHostName().toLowerCase())
                        && VdsHandler.isVdsWithSameHostExistStatic(getParameters().getVdsStaticData().getHostName())) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST);
                } else if (getParameters().getInstallVds() && _oldVds.getStatus() != VDSStatus.Maintenance
                        && _oldVds.getStatus() != VDSStatus.NonOperational
                        && _oldVds.getStatus() != VDSStatus.InstallFailed) {
                    addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_INSTALL_STATUS_ILLEGAL);
                } else if (getParameters().getInstallVds()
                        && StringUtils.isEmpty(getParameters().getRootPassword())
                        && getParameters().getVdsStaticData().getVdsType() == VDSType.VDS) {
                    addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_INSTALL_EMPTY_PASSWORD);
                } else if (!getParameters().getInstallVds()
                        && _oldVds.getPort() != getParameters().getVdsStaticData().getPort()) {
                    addCanDoActionMessage(VdcBllMessages.VDS_PORT_CHANGE_REQUIRE_INSTALL);
                } else if (!_oldVds.getVdsGroupId().equals(getParameters().getVdsStaticData().getVdsGroupId())) {
                    // Forbid updating group id - this must be done through
                    // ChangeVDSClusterCommand
                    // This is due to permission check that must be done both on
                    // the VDS and on the VDSGroup
                    addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_UPDATE_CLUSTER);
                } else {
                    returnValue = true;
                }

                // if all ok check PM is legal
                returnValue =
                    returnValue && IsPowerManagementLegal(getParameters().getVdsStaticData(), compatibilityVersion);
            } else {
                addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE.toString());
            }
        } else {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
        }
        return returnValue;
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
            InitializeVds();
        }

        if (getParameters().getInstallVds()) {
            InstallVdsParameters tempVar = new InstallVdsParameters(getVdsId(), getParameters().getRootPassword());
            tempVar.setIsReinstallOrUpgrade(getParameters().getIsReinstallOrUpgrade());
            tempVar.setoVirtIsoFile(getParameters().getoVirtIsoFile());
            tempVar.setOverrideFirewall(getParameters().getOverrideFirewall());
            tempVar.setRebootAfterInstallation(getParameters().isRebootAfterInstallation());
            ArrayList<VdcReturnValueBase> resultList = Backend.getInstance().runInternalMultipleActions(
                    VdcActionType.InstallVds,
                    new ArrayList<VdcActionParametersBase>(Arrays
                            .asList(tempVar)));

            // Since Host status is set to "Installing", failure of InstallVdsCommand will hang the Host to in that
            // status, therefore needed to fail the command to revert the status.
            if (!resultList.isEmpty()) {
                VdcReturnValueBase vdcReturnValueBase = resultList.get(0);
                if (vdcReturnValueBase != null && !vdcReturnValueBase.getCanDoAction()) {
                    ArrayList<String> canDoActionMessages = vdcReturnValueBase.getCanDoActionMessages();
                    if (!canDoActionMessages.isEmpty()) {
                        log.errorFormat("Installation/upgrade of Host {0},{1} failed due to: {2} ",
                                getVdsId(),
                                getVdsName(),
                                StringUtils.join(Backend.getInstance()
                                        .getErrorsTranslator()
                                        .TranslateErrorText(canDoActionMessages),
                                        ","));
                    }
                    setSucceeded(false);
                    return;
                }
            }
        }

        // set clusters network to be operational (if needed)
        if (_oldVds.getStatus() == VDSStatus.Up) {
            List<NetworkCluster> networkClusters = DbFacade.getInstance()
            .getNetworkClusterDao().getAllForCluster(_oldVds.getVdsGroupId());
            List<Network> networks = DbFacade.getInstance().getNetworkDao()
            .getAllForCluster(_oldVds.getVdsGroupId());
            for (NetworkCluster item : networkClusters) {
                for (Network net : networks) {
                    if (net.getId().equals(item.getNetworkId())) {
                        NetworkClusterHelper.setStatus(_oldVds.getVdsGroupId(), net);
                    }
                }
            }
        }
        AlertIfPowerManagementNotConfigured(getParameters().getVdsStaticData());
        TestVdsPowerManagementStatus(getParameters().getVdsStaticData());
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
                getCompensationContext().stateChanged();
                return null;
            }
        });

        if (getParameters().getInstallVds()) {
            runVdsCommand(VDSCommandType.SetVdsStatus,
                    new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Installing));
        }
    }

    private boolean NeedToUpdateVdsBroker() {
        return VdsHandler.IsFieldsUpdated(getParameters().getVdsStaticData(), _oldVds.getStaticData(),
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
        return _oldVds.getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getVdsStaticData().getName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {
        logable.setVdsId(_oldVds.getId());
    }

}
