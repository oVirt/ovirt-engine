package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.vdscommands.FenceSpmStorageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

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
        _problematicVds = DbFacade.getInstance().getVdsDAO().get(parameters.getVdsId());
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MANUAL_FENCE);
        // check problematic vds status
        if (IsLegalStatus(_problematicVds.getstatus())) {
            if (_problematicVds.getspm_status() == VdsSpmStatus.SPM) {
                if(getStoragePool().getstorage_pool_type() != StorageType.LOCALFS) {
                    returnValue = InitializeVds();
                }
                if (returnValue && getStoragePool().getstatus() != StoragePoolStatus.NotOperational
                        && getStoragePool().getstatus() != StoragePoolStatus.Problematic
                        && getStoragePool().getstatus() != StoragePoolStatus.Maintanance) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
                }
            }
        } else {
            if (_problematicVds.getstatus() == VDSStatus.Problematic) {
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
        setVdsName(_problematicVds.getvds_name());
        if (_problematicVds.getspm_status() == VdsSpmStatus.SPM) {
            result = ActivateDataCenter();
        }
        if ((getParameters()).getClearVMs() && result) {
            VdsActionParameters tempVar = new VdsActionParameters(_problematicVds.getvds_id());
            tempVar.setSessionId(getParameters().getSessionId());
            Backend.getInstance().runInternalAction(VdcActionType.ClearNonResponsiveVdsVms,
                    tempVar);
        }
        setSucceeded(result);
        if (getSucceeded()) {
            // Remove all alerts except NOT CONFIG alert
            AlertDirector.RemoveAllVdsAlerts(_problematicVds.getvds_id(), false);
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
        // storage_domains masterDomain = null; // LINQ 32934
        // DbFacade.Instance.GetStorageDomainsByStoragePoolId(StoragePool.id)
        // LINQ 32934 .Where(a => a.storage_domain_type ==
        // StorageDomainType.Master).FirstOrDefault();

        storage_domains masterDomain = LinqUtils.firstOrNull(
                DbFacade.getInstance().getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId()),
                new Predicate<storage_domains>() {
                    @Override
                    public boolean eval(storage_domains a) {
                        return a.getstorage_domain_type() == StorageDomainType.Master;
                    }
                });
        CalcStoragePoolStatusByDomainsStatus();

        // fence spm if moving from not operational and master domain is active
        if (masterDomain != null
                && masterDomain.getstatus() != null
                && (masterDomain.getstatus() == StorageDomainStatus.Active
                        || masterDomain.getstatus() == StorageDomainStatus.Unknown || masterDomain.getstatus() == StorageDomainStatus.InActive)) {
            if (getStoragePool().getstorage_pool_type() != StorageType.LOCALFS) {
                for (VDS vds : getAllRunningVdssInPool()) {
                    try {
                        SpmStatusResult statusResult = (SpmStatusResult) Backend
                                .getInstance()
                                .getResourceManager()
                                .RunVdsCommand(VDSCommandType.SpmStatus,
                                        new SpmStatusVDSCommandParameters(vds.getvds_id(), getStoragePool().getId()))
                                .getReturnValue();
                        log.infoFormat("Trying to fence spm {0} via vds {1}",
                                _problematicVds.getvds_name(),
                                vds.getvds_name());
                        if (Backend
                                .getInstance()
                                .getResourceManager()
                                .RunVdsCommand(
                                        VDSCommandType.FenceSpmStorage,
                                        new FenceSpmStorageVDSCommandParameters(vds.getvds_id(),
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
                        log.warnFormat("Could not fence spm on vds {0}", vds.getvds_name());
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
            VDS currentSPMVds =
                    DbFacade.getInstance().getVdsDAO().get(getStoragePool().getspm_vds_id());
            ResetIrsVDSCommandParameters tempVar =
                    new ResetIrsVDSCommandParameters(getStoragePool()
                            .getId(), currentSPMVds.gethost_name(), currentSPMVds.getvds_id());
            tempVar.setIgnoreStopFailed(true);
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.ResetIrs, tempVar);
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(FenceVdsManualyCommand.class);

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getParameters().getVdsId(), VdcObjectType.VDS);
    }
}
