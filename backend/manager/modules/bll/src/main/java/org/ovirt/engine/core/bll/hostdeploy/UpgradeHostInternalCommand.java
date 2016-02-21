package org.ovirt.engine.core.bll.hostdeploy;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.host.HostUpgradeManager;
import org.ovirt.engine.core.bll.host.Updateable;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.validator.UpgradeHostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
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
    private VdsDynamicDao hostDao;

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
        UpgradeHostValidator validator = new UpgradeHostValidator(getVds());

        return validate(validator.hostExists())
                && validate(validator.statusSupportedForHostUpgradeInternal());
    }

    @Override
    protected void executeCommand() {
        getCompensationContext().snapshotEntityStatus(getVds().getDynamicData(), getParameters().getInitialStatus());
        getCompensationContext().stateChanged();
        VDSType vdsType = getVds().getVdsType();

        if (vdsType == VDSType.VDS || vdsType == VDSType.oVirtNode) {
            try {
                setVdsStatus(VDSStatus.Installing);
                Updateable upgradeManager = new HostUpgradeManager();
                upgradeManager.update(getVds());
                if (vdsType == VDSType.oVirtNode) {
                    setVdsStatus(VDSStatus.Reboot);
                    runInternalAction(VdcActionType.SshHostReboot,
                            new VdsActionParameters(getVds().getId()),
                            ExecutionHandler.createInternalJobContext());
                }
            } catch (Exception e) {
                setVdsStatus(VDSStatus.InstallFailed);
                return;
            }
        } else if (getVds().isOvirtVintageNode()) {
            InstallVdsParameters parameters = new InstallVdsParameters(getVdsId());
            parameters.setIsReinstallOrUpgrade(true);
            parameters.setoVirtIsoFile(getParameters().getoVirtIsoFile());
            parameters.setActivateHost(getParameters().getInitialStatus() == VDSStatus.Up);

            VdcReturnValueBase result = runInternalAction(VdcActionType.UpgradeOvirtNodeInternal, parameters);
            if (!result.getSucceeded()) {
                setVdsStatus(VDSStatus.InstallFailed);
                propagateFailure(result);
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
        VdsDynamic dynamicHostData = hostDao.get(getVdsId());
        dynamicHostData.setUpdateAvailable(false);
        hostDao.update(dynamicHostData);

        if (getVds().getVdsType() == VDSType.VDS) {
            reestablishConnectionIfNeeded();
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
        return getSucceeded() ? AuditLogType.HOST_UPGRADE_FINISHED : AuditLogType.HOST_UPGRADE_FAILED;
    }
}
