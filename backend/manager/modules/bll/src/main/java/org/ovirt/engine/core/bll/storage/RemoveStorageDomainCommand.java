package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.Pair;
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
    }

    @Override
    protected void executeCommand() {
        final StorageDomain dom = getStorageDomain();
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
                getStorageHelper(dom).storageDomainRemoved(dom.getStorageStaticData());
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

        StorageDomain dom = getStorageDomain();
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
        return getStorageHelper(getStorageDomain()).connectStorageToDomainByVdsId(getStorageDomain(),
                getVds().getId());
    }

    private void DisconnectStorage() {
        getStorageHelper(getStorageDomain()).disconnectStorageFromDomainByVdsId(getStorageDomain(),
                getVds().getId());
    }

    protected VDSBrokerFrontend getVdsBroker() {
        return getBackend().getResourceManager();
    }

    protected boolean isFCP(StorageDomain dom) {
        return dom.getStorageType() == StorageType.FCP;
    }

    protected boolean isISCSI(StorageDomain dom) {
        return dom.getStorageType() == StorageType.ISCSI;
    }

    protected boolean isLocalFs(StorageDomain dom) {
        return dom.getStorageType() == StorageType.LOCALFS;
    }

    protected boolean isDataDomain(StorageDomain dom) {
        return dom.getStorageDomainType() == StorageDomainType.Data;
    }

    protected boolean isISO(StorageDomain dom) {
        return dom.getStorageDomainType() == StorageDomainType.ISO;
    }

    protected boolean isExport(StorageDomain dom) {
        return dom.getStorageDomainType() == StorageDomainType.ImportExport;
    }

    protected boolean isDomainAttached(StorageDomain storageDomain) {
        if (storageDomain.getStoragePoolId() == null) {
            return false;
        }

        Guid storageDomainId = storageDomain.getId();
        Guid storagePoolId = storageDomain.getStoragePoolId().getValue();

        return getDbFacade().getStoragePoolIsoMapDao()
                .get(new StoragePoolIsoMapId(storageDomainId, storagePoolId)) != null;
    }

    protected boolean detachStorage(StorageDomain dom) {
        Guid domId = dom.getId();
        Guid poolId = dom.getStoragePoolId().getValue();
        DetachStorageDomainFromPoolParameters params = new DetachStorageDomainFromPoolParameters(domId, poolId);
        params.setDestroyingPool(getParameters().getDestroyingPool());

        return getBackend()
                .runInternalAction(VdcActionType.DetachStorageDomainFromPool,
                        params).getSucceeded();
    }

    protected boolean formatStorage(StorageDomain dom, VDS vds) {
        return getVdsBroker()
                .RunVdsCommand(VDSCommandType.FormatStorageDomain,
                        new FormatStorageDomainVDSCommandParameters(vds.getId(), dom.getId())).getSucceeded();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(), LockMessagesMatchUtil.STORAGE);
    }
}
