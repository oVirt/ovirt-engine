package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@LockIdNameAttribute
public class ForceRemoveStorageDomainCommand<T extends StorageDomainParametersBase> extends StorageDomainCommandBase<T> {
    public ForceRemoveStorageDomainCommand(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void executeCommand() {
        if (getStoragePool() != null) {
            try {
                // if master try to reconstruct
                if (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
                    ReconstructMasterParameters tempVar = new ReconstructMasterParameters(getStoragePool().getId(),
                            getStorageDomain().getId(), false);
                    tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                    Backend.getInstance().runInternalAction(VdcActionType.ReconstructMasterDomain, tempVar);
                }
                // try to force detach first
                DetachStorageDomainVDSCommandParameters tempVar2 = new DetachStorageDomainVDSCommandParameters(
                        getStoragePool().getId(), getStorageDomain().getId(), Guid.Empty, -1);
                tempVar2.setForce(true);
                Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.DetachStorageDomain, tempVar2);
            } catch (RuntimeException ex) {
                log.errorFormat("Could not force detach storage domain {0}. error: {1}", getStorageDomain()
                        .getstorage_name(), ex.toString());
            }
        }

        StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                .storageDomainRemoved(getStorageDomain().getStorageStaticData());

        DbFacade.getInstance().getStorageDomainDao().remove(getStorageDomain().getId());

        if (getStoragePool() != null) {
            // if iso reset path for pool
            if (getStorageDomain().getstorage_domain_type() == StorageDomainType.ISO) {
                // todo: when iso in multiple pools will be implemented, we
                // should reset iso path for all related pools
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.ResetISOPath,
                                new IrsBaseVDSCommandParameters(getStoragePool().getId()));
            }
            if (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
                calcStoragePoolStatusByDomainsStatus();
            }
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_FORCE_REMOVE_STORAGE_DOMAIN
                : AuditLogType.USER_FORCE_REMOVE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DESTROY_DOMAIN);
        boolean returnValue =
                super.canDoAction()
                        && checkStorageDomain()
                        && (getStorageDomain().getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached || checkStorageDomainStatusNotEqual(StorageDomainStatus.Active));

        if (returnValue && getStorageDomain().getstorage_domain_type() == StorageDomainType.Master
                && getStoragePool() != null) {
            if (electNewMaster(false) == null) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DESTROY_LAST_STORAGE_DOMAIN);
            } else if (!InitializeVds()) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DESTROY_LAST_STORAGE_DOMAIN_HOST_NOT_ACTIVE);
            }
        }
        return returnValue;
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(), LockingGroup.STORAGE.name());
    }
}
