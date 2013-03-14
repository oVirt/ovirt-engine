package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FenceSpmStorageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@LockIdNameAttribute
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

    public FenceVdsManualyCommand(T parameters) {
        super(parameters);
        _problematicVds = DbFacade.getInstance().getVdsDao().get(parameters.getVdsId());
    }

    public Guid getProblematicVdsId() {
        return _problematicVds.getId();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MANUAL_FENCE);
        // check problematic vds status
        if (IsLegalStatus(_problematicVds.getStatus())) {
            if (_problematicVds.getSpmStatus() == VdsSpmStatus.SPM) {
                if(getStoragePool().getstorage_pool_type() != StorageType.LOCALFS) {
                    returnValue = returnValue && InitializeVds();
                }
                if (returnValue && getStoragePool().getstatus() != StoragePoolStatus.NotOperational
                        && getStoragePool().getstatus() != StoragePoolStatus.Problematic
                        && getStoragePool().getstatus() != StoragePoolStatus.Maintenance) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
                }
            }
        } else {
            if (_problematicVds.getStatus() == VDSStatus.Connecting) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VDS_INTERMITENT_CONNECTIVITY);

            } else {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VDS_NOT_MATCH_VALID_STATUS);
            }
        }
        return returnValue;
    }

    @Override
    protected void executeCommand() {
        boolean result = true;
        setVdsName(_problematicVds.getName());
        if (_problematicVds.getSpmStatus() == VdsSpmStatus.SPM) {
            result = ActivateDataCenter();
            if (getVdsDAO().getAllForStoragePool(getStoragePool().getId()).size() == 1) {
                //reset SPM flag fot this Host
                storage_pool sp = getStoragePool();
                sp.setspm_vds_id(null);
                getStoragePoolDAO().update(sp);
                result = true;
            }
        }
        if ((getParameters()).getClearVMs() && result) {
            VdsActionParameters tempVar = new VdsActionParameters(_problematicVds.getId());
            tempVar.setSessionId(getParameters().getSessionId());
            Backend.getInstance().runInternalAction(VdcActionType.ClearNonResponsiveVdsVms,
                    tempVar,
                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        }
        setSucceeded(result);
        if (getSucceeded()) {
            // Remove all alerts except NOT CONFIG alert
            AlertDirector.RemoveAllVdsAlerts(_problematicVds.getId(), false);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return _fenceSpmCalled != null && !_fenceSpmCalled ? AuditLogType.VDS_MANUAL_FENCE_FAILED_CALL_FENCE_SPM
                : getSucceeded() ? AuditLogType.VDS_MANUAL_FENCE_STATUS : AuditLogType.VDS_MANUAL_FENCE_STATUS_FAILED;
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
            result = true;
            break;
        default:
            result = false;
            break;
        }
        return result;
    }

    private Boolean _fenceSpmCalled;

    private boolean ActivateDataCenter() {
        boolean result = false;
        _fenceSpmCalled = false;

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
                        || masterDomain.getStatus() == StorageDomainStatus.Unknown || masterDomain.getStatus() == StorageDomainStatus.InActive)) {
            if (getStoragePool().getstorage_pool_type() != StorageType.LOCALFS) {
                for (VDS vds : getAllRunningVdssInPool()) {
                    try {
                        SpmStatusResult statusResult = (SpmStatusResult) Backend
                                .getInstance()
                                .getResourceManager()
                                .RunVdsCommand(VDSCommandType.SpmStatus,
                                        new SpmStatusVDSCommandParameters(vds.getId(), getStoragePool().getId()))
                                .getReturnValue();
                        log.infoFormat("Trying to fence spm {0} via vds {1}",
                                _problematicVds.getName(),
                                vds.getName());
                        if (Backend
                                .getInstance()
                                .getResourceManager()
                                .RunVdsCommand(
                                        VDSCommandType.FenceSpmStorage,
                                        new FenceSpmStorageVDSCommandParameters(vds.getId(),
                                                getStoragePool().getId(),
                                                statusResult.getSpmId(),
                                                statusResult.getSpmLVER()))
                                .getSucceeded()) {
                            resetIrs();
                            result = true;
                            _fenceSpmCalled = true;
                            break;
                        }
                    } catch (java.lang.Exception e) {
                        log.warnFormat("Could not fence spm on vds {0}", vds.getName());
                    }
                }
            } else {
                resetIrs();
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    private void resetIrs() {
        if (getStoragePool().getspm_vds_id() != null) {
            ResetIrsVDSCommandParameters tempVar =
                    new ResetIrsVDSCommandParameters(getStoragePool()
                            .getId(), getStoragePool().getspm_vds_id().getValue());
            tempVar.setIgnoreStopFailed(true);
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.ResetIrs, tempVar);
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getProblematicVdsId().toString(), LockMessagesMatchUtil.VDS_FENCE);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getVdsId(), VdcObjectType.VDS,
                getActionType().getActionGroup()));
    }
}
