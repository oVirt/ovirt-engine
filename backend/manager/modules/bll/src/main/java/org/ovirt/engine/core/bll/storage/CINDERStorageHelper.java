package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class CINDERStorageHelper extends StorageHelperBase {

    private Logger log = LoggerFactory.getLogger(CINDERStorageHelper.class);

    private boolean runInNewTransaction;

    public CINDERStorageHelper() {
        this(true);
    }

    public CINDERStorageHelper(boolean runInNewTransaction) {
        this.runInNewTransaction = runInNewTransaction;
    }

    @Override
    protected Pair<Boolean, VdcFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        return new Pair<>(true, null);
    }

    private <T> void execute(final Callable<T> callable) {
        if (runInNewTransaction) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                @Override
                public Object runInTransaction() {
                    invokeCallable(callable);
                    return null;
                }
            });
        } else {
            invokeCallable(callable);
        }
    }

    private <T> void invokeCallable(Callable<T> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            log.error("Error in CinderStorageHelper.", e);
        }
    }

    public void attachCinderDomainToPool(final Guid storageDomainId, final Guid storagePoolId) {
        execute(new Callable<Object>() {
            @Override
            public Object call() {
                StoragePoolIsoMap storagePoolIsoMap =
                        new StoragePoolIsoMap(storageDomainId, storagePoolId, StorageDomainStatus.Maintenance);
                getStoragePoolIsoMapDAO().save(storagePoolIsoMap);
                return null;
            }
        });
    }

    public void activateCinderDomain(Guid storageDomainId, Guid storagePoolId) {
        OpenStackVolumeProviderProxy proxy = OpenStackVolumeProviderProxy.getFromStorageDomainId(storageDomainId);
        if (proxy == null) {
            log.error("Couldn't create an OpenStackVolumeProviderProxy for storage domain ID: {}", storageDomainId);
            return;
        }
        try {
            proxy.testConnection();
            updateCinderDomainStatus(storageDomainId, storagePoolId, StorageDomainStatus.Active);
        } catch (VdcBLLException e) {
            AuditLogableBase loggable = new AuditLogableBase();
            loggable.addCustomValue("CinderException", e.getCause().getCause() != null ?
                    e.getCause().getCause().getMessage() : e.getCause().getMessage());
            new AuditLogDirector().log(loggable, AuditLogType.CINDER_PROVIDER_ERROR);
            throw e;
        }
    }

    private void updateCinderDomainStatus(final Guid storageDomainId,
                                          final Guid storagePoolId,
                                          final StorageDomainStatus storageDomainStatus) {
        execute(new Callable<Object>() {
            @Override
            public Object call() {
                StoragePoolIsoMap map =
                        getStoragePoolIsoMapDAO().get(new StoragePoolIsoMapId(storageDomainId, storagePoolId));
                map.setStatus(storageDomainStatus);
                getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getStatus());
                return null;
            }
        });
    }

    public void deactivateCinderDomain(Guid storageDomainId, Guid storagePoolId) {
        updateCinderDomainStatus(storageDomainId, storagePoolId, StorageDomainStatus.Maintenance);
    }

    private StoragePoolIsoMapDAO getStoragePoolIsoMapDAO() {
        return getDbFacade().getStoragePoolIsoMapDao();
    }

    private static DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }
}
