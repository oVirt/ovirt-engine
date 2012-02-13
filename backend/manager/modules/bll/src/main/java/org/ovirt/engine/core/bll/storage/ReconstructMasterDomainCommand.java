package org.ovirt.engine.core.bll.storage;

import java.text.MessageFormat;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MarkPoolInReconstructModeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ReconstructMarkAction;
import org.ovirt.engine.core.common.vdscommands.ReconstructMasterVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RefreshStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ReconstructMasterDomainCommand<T extends ReconstructMasterParameters> extends
        DeactivateStorageDomainCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ReconstructMasterDomainCommand(Guid commandId) {
        super(commandId);
    }

    public ReconstructMasterDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RECONSTRUCT_MASTER);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);

        List<storage_pool_iso_map> poolDomains = DbFacade.getInstance()
                .getStoragePoolIsoMapDAO().getAllForStoragePool(getStoragePool().getId());
        for (storage_pool_iso_map poolDomain : poolDomains) {
            if (poolDomain.getstatus() == StorageDomainStatus.Locked) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
                return false;
            }
        }

        return InitializeVds();
    }

    @Override
    protected void executeCommand() {
        try {
            boolean commandSucceeded = (Boolean) TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew,
                    new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            boolean commandSucceeded = false;

                            ProceedStorageDomainTreatmentByDomainType(true);
                            // set status to inactive in order to send it on
                            // reconstruct or if its last master
                            if (getParameters().getIsDeactivate()) {
                                SetStorageDomainStatus(StorageDomainStatus.InActive);
                                CalcStoragePoolStatusByDomainsStatus();
                            }

                            commandSucceeded = true;
                            if (!_isLastMaster) {
                                // pause the timers for the domain error handling.
                                Backend.getInstance()
                                        .getResourceManager()
                                        .RunVdsCommand(
                                                VDSCommandType.MarkPoolInReconstructMode,
                                                new MarkPoolInReconstructModeVDSCommandParameters(getStoragePoolId()
                                                        .getValue(), ReconstructMarkAction.ClearJobs));
                                // if we have spm, stop spm and reset cache (resetIrs)
                                commandSucceeded = stopSpm();
                                commandSucceeded =
                                        commandSucceeded
                                                && Backend
                                                        .getInstance()
                                                        .getResourceManager()
                                                        .RunVdsCommand(
                                                                VDSCommandType.DisconnectStoragePool,
                                                                new DisconnectStoragePoolVDSCommandParameters(getVds()
                                                                        .getId(),
                                                                        getStoragePool().getId(),
                                                                        getVds().getvds_spm_id()))
                                                        .getSucceeded();
                                if (commandSucceeded) {
                                    List<storage_pool_iso_map> domains = DbFacade.getInstance()
                                                    .getStoragePoolIsoMapDAO()
                                                    .getAllForStoragePool(getStoragePool().getId());
                                    for (storage_pool_iso_map domain : domains) {
                                        if (domain.getstatus() == null
                                                || domain.getstatus() == StorageDomainStatus.Unknown) {
                                            domain.setstatus(StorageDomainStatus.Active);
                                        } else if (domain.getstatus() == StorageDomainStatus.Locked) {
                                            throw new VdcBLLException(
                                                    VdcBllErrors.CANT_RECONSTRUCT_WHEN_A_DOMAIN_IN_POOL_IS_LOCKED,
                                                    "Cannot reconstruct master domain when a domain in the pool is " +
                                                            "locked.");
                                        }
                                    }
                                    commandSucceeded = Backend
                                            .getInstance()
                                            .getResourceManager()
                                            .RunVdsCommand(
                                                    VDSCommandType.ReconstructMaster,
                                                    new ReconstructMasterVDSCommandParameters(getVds().getId(),
                                                            getStoragePool().getId(), getStoragePool().getname(),
                                                            _newMasterStorageDomainId, domains, getStoragePool()
                                                                    .getmaster_domain_version())).getSucceeded();
                                }
                            } else {
                                stopSpm();
                            }
                            return commandSucceeded;
                        }
                    });

            connectAndRefreshAllUpHosts(commandSucceeded);

            if (!_isLastMaster && commandSucceeded) {
                SearchParameters p =
                        new SearchParameters(MessageFormat.format(DesktopsInStoragePoolQuery, getStoragePool()
                                .getname()), SearchType.VM);
                p.setMaxCount(Integer.MAX_VALUE);
                @SuppressWarnings("unchecked")
                List<VM> vmsInPool = (List<VM>) Backend.getInstance().runInternalQuery(VdcQueryType.Search, p)
                        .getReturnValue();
                VmCommand.UpdateVmInSpm(getStoragePool().getId(), vmsInPool);
            }
            setSucceeded(commandSucceeded);
        } finally {
            // reset cache and mark reconstruct for pool as finished
            Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.MarkPoolInReconstructMode,
                                new MarkPoolInReconstructModeVDSCommandParameters(getStoragePoolId()
                                        .getValue(), ReconstructMarkAction.ClearCache));
        }
    }

    protected boolean stopSpm() {
        boolean commandSucceeded = true;
        if (getStoragePool().getspm_vds_id() != null) {
            // if spm host id is different from selected host get the spm
            // in order to try and perform stop spm
            VDS spm = null;
            if (getStoragePool().getspm_vds_id().equals(getVds().getId())) {
                spm = getVds();
            } else {
                spm = DbFacade.getInstance()
                        .getVdsDAO()
                        .get(getStoragePool().getspm_vds_id());
            }
            if (spm != null) {
                ResetIrsVDSCommandParameters tempVar2 = new ResetIrsVDSCommandParameters(
                        getStoragePool().getId(), spm.gethost_name(), spm.getId());
                tempVar2.setIgnoreStopFailed(true);
                commandSucceeded = Backend.getInstance().getResourceManager()
                            .RunVdsCommand(VDSCommandType.ResetIrs, tempVar2).getSucceeded();

                // if spm host is up switch to use it in the following logic
                if (spm.getstatus() == VDSStatus.Up) {
                    setVdsId(spm.getId());
                    setVds(spm);
                }
            }
        }
        return commandSucceeded;
    }

    private void connectAndRefreshAllUpHosts(final boolean commandSucceeded) {
        TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew,
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        try {
                            for (VDS vds : getAllRunningVdssInPool()) {
                                try {
                                    if (!_isLastMaster && commandSucceeded) {
                                        VDSReturnValue returnValue = Backend.getInstance()
                                                .getResourceManager()
                                                .RunVdsCommand(
                                                        VDSCommandType.ConnectStoragePool,
                                                        new ConnectStoragePoolVDSCommandParameters(vds.getId(),
                                                                getStoragePool().getId(), vds.getvds_spm_id(),
                                                                getMasterDomainIdFromDb(), getStoragePool()
                                                                        .getmaster_domain_version()));
                                        if (returnValue.getSucceeded()) {
                                            Backend.getInstance()
                                                    .getResourceManager()
                                                    .RunVdsCommand(
                                                            VDSCommandType.RefreshStoragePool,
                                                            new RefreshStoragePoolVDSCommandParameters(vds.getId(),
                                                                    getStoragePool().getId(),
                                                                    _newMasterStorageDomainId,
                                                                    getStoragePool().getmaster_domain_version()));
                                        } else {
                                            log.errorFormat("Post reconstruct actions (connectPool) did not complete on host {0} in the pool. error {1}",
                                                    vds.getId(),
                                                    returnValue.getVdsError().getMessage());
                                        }
                                    }
                                    // only if we deactivate the storage domain we want to disconnect from it.
                                    if (getParameters().getIsDeactivate()) {
                                        StorageHelperDirector.getInstance()
                                                .getItem(getStorageDomain().getstorage_type())
                                                .DisconnectStorageFromDomainByVdsId(getStorageDomain(), vds.getId());
                                    }

                                } catch (Exception e) {
                                    log.errorFormat("Post reconstruct actions (connectPool,refreshPool,disconnect storage)"
                                            + " did not complete on host {0} in the pool. error {1}",
                                            vds.getId(),
                                            e.getMessage());
                                }
                            }
                        } catch (Exception ex) {
                            log.errorFormat("Post reconstruct actions (connectPool,refreshPool,disconnect storage)"
                                    + " did not complete on all up hosts in the pool. error {0}",
                                    ex.getMessage());
                        }
                        return null;
                    }
                });
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? _isLastMaster ? AuditLogType.RECONSTRUCT_MASTER_FAILED_NO_MASTER
                : AuditLogType.RECONSTRUCT_MASTER_DONE : AuditLogType.RECONSTRUCT_MASTER_FAILED;
    }

    private static Log log = LogFactory.getLog(ReconstructMasterDomainCommand.class);
}
