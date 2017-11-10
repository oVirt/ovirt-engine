package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.HostLocking;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * Confirm a host has been rebooted, clear spm flag, its VMs(optional) and alerts.
 *
 * This command should be run mutually exclusive from other fence actions to prevent same action or other fence actions
 * to clear the VMs and start them.
 *
 * @see RestartVdsCommand
 */
public class FenceVdsManualyCommand<T extends FenceVdsManualyParameters> extends StorageHandlingCommandBase<T> {

    @Inject
    private VdsDao vdsDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private AlertDirector alertDirector;
    @Inject
    private HostLocking hostLocking;

    private VDS problematicVds;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public FenceVdsManualyCommand(Guid commandId) {
        super(commandId);
    }

    public FenceVdsManualyCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public void init() {
        super.init();
        problematicVds = vdsDao.get(getParameters().getVdsId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    public Guid getProblematicVdsId() {
        return problematicVds.getId();
    }

    @Override
    protected boolean validate() {
        // check problematic vds status
        if (isLegalStatus(problematicVds.getStatus())) {
            if (problematicVds.getSpmStatus() == VdsSpmStatus.SPM) {
                if(!getStoragePool().isLocal()) {
                    if (!initializeVds()) {
                        return false;
                    }
                }
                if (!validate(new StoragePoolValidator(getStoragePool()).isInStatus
                        (StoragePoolStatus.NotOperational, StoragePoolStatus.NonResponsive, StoragePoolStatus.Maintenance))) {
                    return false;
                }
            }
        } else {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VDS_NOT_MATCH_VALID_STATUS);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        setVdsName(problematicVds.getName());
        log.info("Start fence execution for Host '{}' (spm status: '{}')",
                problematicVds.getName(),
                problematicVds.getSpmStatus());
        if (problematicVds.getSpmStatus() == VdsSpmStatus.SPM) {
            activateDataCenter();
        }
        if (getParameters().getClearVMs()) {
            VdsActionParameters tempVar = new VdsActionParameters(problematicVds.getId());
            tempVar.setSessionId(getParameters().getSessionId());
            runInternalActionWithTasksContext(
                    ActionType.ClearNonResponsiveVdsVms,
                    tempVar);
        }
        setSucceeded(true);
        // Remove all alerts except NOT CONFIG alert
        alertDirector.removeAllVdsAlerts(problematicVds.getId(), false);
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
    private static boolean isLegalStatus(VDSStatus status) {
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
        StorageDomain masterDomain =
                storageDomainDao.getStorageDomains(getStoragePool().getId(), StorageDomainType.Master).stream().findFirst().orElse(null);
        calcStoragePoolStatusByDomainsStatus();

        // fence spm if moving from not operational and master domain is active
        if (masterDomain == null) {
            log.info("no master domain found");
        } else {
            log.info("Master domain id:'{}' has status:'{}'",
                    masterDomain.getId(), masterDomain.getStatus());
            if (masterDomain.getStatus() == StorageDomainStatus.Active ||
                    masterDomain.getStatus() == StorageDomainStatus.Unknown ||
                    masterDomain.getStatus() == StorageDomainStatus.Inactive) {
                resetSPM();
            }
        }
    }

    private void resetSPM() {
        log.info("Start reset of SPM. Spm vds id: '{}'", getStoragePool().getSpmVdsId());
        if (getStoragePool().getSpmVdsId() != null) {
            ResetIrsVDSCommandParameters resetIrsParams =
                    new ResetIrsVDSCommandParameters(getStoragePool().getId(), getStoragePool().getSpmVdsId());
            resetIrsParams.setVdsAlreadyRebooted(true);
            runVdsCommand(VDSCommandType.ResetIrs, resetIrsParams);
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return hostLocking.getPowerManagementLock(getProblematicVdsId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getVdsId(), VdcObjectType.VDS,
                getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__MANUAL_FENCE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
     }

    @Override
    protected void freeLock() {
        if (getParameters().getParentCommand() != ActionType.RestartVds) {
            super.freeLock();
        }
    }
}
