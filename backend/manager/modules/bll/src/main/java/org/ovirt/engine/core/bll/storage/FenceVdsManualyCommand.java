package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.FenceVdsBaseCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

/**
 * Confirm a host has been rebooted, clear spm flag, its VMs(optional) and alerts.
 *
 * This command should be run mutually exclusive from other fence actions to prevent same action or other fence actions
 * to clear the VMs and start them.
 *
 * @see org.ovirt.engine.core.bll.RestartVdsCommand
 */
public class FenceVdsManualyCommand<T extends FenceVdsManualyParameters> extends StorageHandlingCommandBase<T> {
    private final VDS _problematicVds;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected FenceVdsManualyCommand(Guid commandId) {
        super(commandId);
        _problematicVds = null;
    }

    public FenceVdsManualyCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        _problematicVds = DbFacade.getInstance().getVdsDao().get(parameters.getVdsId());
    }

    public FenceVdsManualyCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    public Guid getProblematicVdsId() {
        return _problematicVds.getId();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        // check problematic vds status
        if (IsLegalStatus(_problematicVds.getStatus())) {
            if (_problematicVds.getSpmStatus() == VdsSpmStatus.SPM) {
                if(!getStoragePool().isLocal()) {
                    returnValue = returnValue && initializeVds();
                }
                if (returnValue && getStoragePool().getStatus() != StoragePoolStatus.NotOperational
                        && getStoragePool().getStatus() != StoragePoolStatus.NonResponsive
                        && getStoragePool().getStatus() != StoragePoolStatus.Maintenance) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
                }
            }
        } else {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VDS_NOT_MATCH_VALID_STATUS);
        }
        return returnValue;
    }

    @Override
    protected void executeCommand() {
        setVdsName(_problematicVds.getName());
        if (_problematicVds.getSpmStatus() == VdsSpmStatus.SPM) {
            activateDataCenter();
        }
        if ((getParameters()).getClearVMs()) {
            VdsActionParameters tempVar = new VdsActionParameters(_problematicVds.getId());
            tempVar.setSessionId(getParameters().getSessionId());
            runInternalActionWithTasksContext(
                    VdcActionType.ClearNonResponsiveVdsVms,
                    tempVar);
        }
        setSucceeded(true);
        // Remove all alerts except NOT CONFIG alert
        AlertDirector.RemoveAllVdsAlerts(_problematicVds.getId(), false);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (isInternalExecution()) {
            return getSucceeded()
                            ? AuditLogType.VDS_AUTO_FENCE_STATUS
                            : AuditLogType.VDS_AUTO_FENCE_STATUS_FAILED;
        } else {
            return getSucceeded()
                            ? AuditLogType.VDS_MANUAL_FENCE_STATUS
                            : AuditLogType.VDS_MANUAL_FENCE_STATUS_FAILED;
        }
    }

    /**
     * Determines whether VDS [is legal status] [the specified status].
     *
     * @param status
     *            The status.
     * @return <c>true</c> if [is legal status] [the specified status];
     *         otherwise, <c>false</c>.
     */
    private static boolean IsLegalStatus(VDSStatus status) {
        boolean result;
        switch (status) {
        case Down:
        case InstallFailed:
        case Maintenance:
        case NonOperational:
        case NonResponsive:
        case Reboot:
        case Installing:
        case Connecting:
        case Kdumping:
            result = true;
            break;
        default:
            result = false;
            break;
        }
        return result;
    }

    private void activateDataCenter() {
        StorageDomain masterDomain = LinqUtils.firstOrNull(
                DbFacade.getInstance().getStorageDomainDao().getAllForStoragePool(getStoragePool().getId()),
                new Predicate<StorageDomain>() {
                    @Override
                    public boolean eval(StorageDomain a) {
                        return a.getStorageDomainType() == StorageDomainType.Master;
                    }
                });
        calcStoragePoolStatusByDomainsStatus();

        // fence spm if moving from not operational and master domain is active
        if (masterDomain != null
                && masterDomain.getStatus() != null
                && (masterDomain.getStatus() == StorageDomainStatus.Active
                        || masterDomain.getStatus() == StorageDomainStatus.Unknown || masterDomain.getStatus() == StorageDomainStatus.Inactive)) {
            resetIrs();
        }
    }

    private void resetIrs() {
        if (getStoragePool().getspm_vds_id() != null) {
            ResetIrsVDSCommandParameters tempVar =
                    new ResetIrsVDSCommandParameters(getStoragePool()
                            .getId(), getStoragePool().getspm_vds_id());
            tempVar.setVdsAlreadyRebooted(true);
            runVdsCommand(VDSCommandType.ResetIrs, tempVar);
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return FenceVdsBaseCommand.createFenceExclusiveLocksMap(getProblematicVdsId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getVdsId(), VdcObjectType.VDS,
                getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MANUAL_FENCE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
     }

    @Override
    protected void freeLock() {
        if (getParameters().getParentCommand() != VdcActionType.RestartVds) {
            super.freeLock();
        }
    }
}
