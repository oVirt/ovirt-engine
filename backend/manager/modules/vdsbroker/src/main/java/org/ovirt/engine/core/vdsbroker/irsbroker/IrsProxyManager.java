package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class IrsProxyManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(IrsProxyManager.class);

    @Inject
    private StoragePoolDao storagePoolDao;

    private Map<Guid, IrsProxy> irsProxyData = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        for (StoragePool dataCenter : storagePoolDao.getAll()) {
            if (!irsProxyData.containsKey(dataCenter.getId())) {
                irsProxyData.put(dataCenter.getId(), createProxy(dataCenter.getId()));
            }
        }
        log.info("Start initializing {}", getClass().getSimpleName());
    }

    private static IrsProxy createProxy(Guid storagePoolId) {
        return Injector.injectMembers(new IrsProxy(storagePoolId));
    }

    /**
     * Return the IRS Proxy object for the given pool id. If there's no proxy data available, since there's no SPM
     * for the pool, then returns <code>null</code>.
     * @param storagePoolId The ID of the storage pool to get the IRS proxy for.
     * @return The IRS Proxy object, on <code>null</code> if no proxy data is available.
     */
    public IrsProxy getProxy(Guid storagePoolId) {
        return irsProxyData.get(storagePoolId);
    }

    public IrsProxy getCurrentProxy(Guid storagePoolId) {
        if (!irsProxyData.containsKey(storagePoolId)) {
            irsProxyData.put(storagePoolId, createProxy(storagePoolId));
        }
        return irsProxyData.get(storagePoolId);
    }

    public void removeProxy(Guid storagePoolId) {
        irsProxyData.get(storagePoolId).dispose();
        irsProxyData.remove(storagePoolId);
    }
}
