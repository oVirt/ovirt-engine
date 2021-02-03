package org.ovirt.engine.core.bll.hostdeploy;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.host.HostConnectivityChecker;
import org.ovirt.engine.core.bll.host.Updateable;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.validator.UpgradeHostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SshHostRebootParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDynamicDao;

@NonTransactiveCommandAttribute
public class UpgradeHostInternalCommand<T extends UpgradeHostParameters> extends VdsCommand<T> {

    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private Updateable upgradeManager;

    /**
     * C'tor for compensation purposes
     *
     * @param commandId
     *            the command id
     */
    public UpgradeHostInternalCommand(Guid commandId) {
        super(commandId);
    }

    public UpgradeHostInternalCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        UpgradeHostValidator validator = new UpgradeHostValidator(getVds(), getCluster());

        return validate(validator.hostExists())
                && validate(validator.statusSupportedForHostUpgradeInternal())
                && validate(validator.clusterCpuSecureAndNotAffectedByTsxRemoval());
    }

    @Override
    protected void executeCommand() {
        getCompensationContext().snapshotEntityStatus(getVds().getDynamicData(), getParameters().getInitialStatus());
        getCompensationContext().stateChanged();
        VDSType vdsType = getVds().getVdsType();

        if (vdsType == VDSType.VDS || vdsType == VDSType.oVirtNode) {
            try {
                setVdsStatus(VDSStatus.Installing);
                upgradeManager.update(getVds(), getParameters().getTimeout());
                if (vdsType == VDSType.oVirtNode || getParameters().isReboot()) {
                    SshHostRebootParameters params = new SshHostRebootParameters(getVds().getId());
                    params.setPrevVdsStatus(getParameters().getInitialStatus());
                    ActionReturnValue returnValue = runInternalAction(ActionType.SshHostReboot,
                            params,
                            ExecutionHandler.createInternalJobContext());
                    if (!returnValue.getSucceeded()) {
                        setVdsStatus(VDSStatus.InstallFailed);
                        log.error("Engine failed to restart via ssh host '{}' ('{}') after upgrade",
                                getVds().getName(),
                                getVds().getId());
                        return;
                    }
                } else {
                    // letting the host a chance to recover from restarting the VDSM service after the upgrade
                    if (!new HostConnectivityChecker().check(getVds())) {
                        log.warn(
                                "Engine failed to communicate with VDSM agent on host '{}' with address '{}' ('{}') " +
                                        "after upgrade",
                                getVds().getName(),
                                getVds().getHostName(),
                                getVds().getId());
                    }
                }
            } catch (Exception e) {
                setVdsStatus(VDSStatus.InstallFailed);
                return;
            }
        }

        try {
            updateHostStatusAfterSuccessfulUpgrade();
            setSucceeded(true);
        } catch (Exception e) {
            log.error("Failed to set new status for host '{}' after upgrade has ended.", getVdsName());
            log.error("Exception", e);
            setVdsStatus(VDSStatus.InstallFailed);
        }
    }

    public void updateHostStatusAfterSuccessfulUpgrade() {
        VdsDynamic dynamicHostData = vdsDynamicDao.get(getVdsId());
        dynamicHostData.setUpdateAvailable(false);
        vdsDynamicDao.update(dynamicHostData);

        if (getVds().getVdsType() == VDSType.VDS && !getParameters().isReboot()) {
            if (getParameters().getInitialStatus() == VDSStatus.Maintenance) {
                setVdsStatus(VDSStatus.Maintenance);
            } else {
                setVdsStatus(VDSStatus.Initializing);
            }
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPGRADE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        AuditLogType value = AuditLogType.HOST_UPGRADE_FAILED;
        if (getSucceeded()) {
            if (getParameters().isReboot()) {
                value = AuditLogType.HOST_UPGRADE_FINISHED_AND_WILL_BE_REBOOTED;
            } else {
                value = AuditLogType.HOST_UPGRADE_FINISHED;
            }
        }

        return value;
    }
}
