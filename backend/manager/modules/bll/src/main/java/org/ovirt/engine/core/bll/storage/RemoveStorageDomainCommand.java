package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class RemoveStorageDomainCommand<T extends RemoveStorageDomainParameters> extends StorageDomainCommandBase<T> {
    public RemoveStorageDomainCommand(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void executeCommand() {
        final storage_domains dom = getStorageDomain();
        VDS vds = getVds();
        boolean format = getParameters().getDoFormat();

        setSucceeded(false);

        if (isLocalFs(dom) && isDomainAttached(dom) && !detachStorage(dom)) {
            return;
        }

        if (!isISO(dom) && !isExport(dom) || format) {
            if (!ConnectStorage()) {
                return;
            }

            boolean failed = !formatStorage(dom, vds);

            DisconnectStorage();

            if (failed) {
                return;
            }
        }

        executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                getStorageHelper(dom).StorageDomainRemoved(dom.getStorageStaticData());
                getDbFacade().getStorageDomainDynamicDao().remove(dom.getId());
                getDbFacade().getStorageDomainStaticDao().remove(dom.getId());
                return null;
            }
        });

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_STORAGE_DOMAIN
                : AuditLogType.USER_REMOVE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        storage_domains dom = getStorageDomain();
        if (dom == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            return false;
        }

        VDS vds = getVds();
        boolean format = getParameters().getDoFormat();
        boolean localFs = isLocalFs(dom);

        if (!checkStorageDomain() || !checkStorageDomainSharedStatusNotLocked(dom)) {
            return false;
        }

        if (!localFs && !checkStorageDomainNotInPool()) {
            return false;
        }

        if (localFs && isDomainAttached(dom) && !canDetachDomain(getParameters().getDestroyingPool(), false, true)) {
            return false;
        }

        if (vds == null) {
            if (localFs) {
                if (!InitializeVds()) {
                    return false;
                }
            } else {
                addCanDoActionMessage(VdcBllMessages.CANNOT_REMOVE_STORAGE_DOMAIN_INVALID_HOST_ID);
                return false;
            }
        }

        if (isDataDomain(dom) && !format) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_REMOVE_STORAGE_DOMAIN_DO_FORMAT);
            return false;
        }

        return true;
    }

    private boolean ConnectStorage() {
        return getStorageHelper(getStorageDomain()).ConnectStorageToDomainByVdsId(getStorageDomain(),
                getVds().getId());
    }

    private void DisconnectStorage() {
        getStorageHelper(getStorageDomain()).DisconnectStorageFromDomainByVdsId(getStorageDomain(),
                getVds().getId());
    }

    protected VDSBrokerFrontend getVdsBroker() {
        return getBackend().getResourceManager();
    }

    protected boolean isFCP(storage_domains dom) {
        return dom.getstorage_type() == StorageType.FCP;
    }

    protected boolean isISCSI(storage_domains dom) {
        return dom.getstorage_type() == StorageType.ISCSI;
    }

    protected boolean isLocalFs(storage_domains dom) {
        return dom.getstorage_type() == StorageType.LOCALFS;
    }

    protected boolean isDataDomain(storage_domains dom) {
        return dom.getstorage_domain_type() == StorageDomainType.Data;
    }

    protected boolean isISO(storage_domains dom) {
        return dom.getstorage_domain_type() == StorageDomainType.ISO;
    }

    protected boolean isExport(storage_domains dom) {
        return dom.getstorage_domain_type() == StorageDomainType.ImportExport;
    }

    protected boolean isDomainAttached(storage_domains storageDomain) {
        if (storageDomain.getstorage_pool_id() == null) {
            return false;
        }

        Guid storageDomainId = storageDomain.getId();
        Guid storagePoolId = storageDomain.getstorage_pool_id().getValue();

        return getDbFacade().getStoragePoolIsoMapDao()
                .get(new StoragePoolIsoMapId(storageDomainId, storagePoolId)) != null;
    }

    protected boolean detachStorage(storage_domains dom) {
        Guid domId = dom.getId();
        Guid poolId = dom.getstorage_pool_id().getValue();
        DetachStorageDomainFromPoolParameters params = new DetachStorageDomainFromPoolParameters(domId, poolId);
        params.setDestroyingPool(getParameters().getDestroyingPool());

        return getBackend()
                .runInternalAction(VdcActionType.DetachStorageDomainFromPool,
                        params).getSucceeded();
    }

    protected boolean formatStorage(storage_domains dom, VDS vds) {
        return getVdsBroker()
                .RunVdsCommand(VDSCommandType.FormatStorageDomain,
                        new FormatStorageDomainVDSCommandParameters(vds.getId(), dom.getId())).getSucceeded();
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(), LockingGroup.STORAGE.name());
    }
}
