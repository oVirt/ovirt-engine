package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
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
        storagePoolDao.getAll().forEach(dc -> irsProxyData.computeIfAbsent(dc.getId(), id -> createProxy(dc)));
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private static IrsProxy createProxy(StoragePool dc) {
        if (dc.isManaged()) {
            return Injector.injectMembers(new IrsProxyImpl(dc.getId()));
        }
        return new NullableIrsProxy();
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
        irsProxyData.computeIfAbsent(storagePoolId, id -> createProxy(storagePoolDao.get(id)));
        return irsProxyData.get(storagePoolId);
    }

    public void removeProxy(Guid storagePoolId) {
        irsProxyData.get(storagePoolId).dispose();
        irsProxyData.remove(storagePoolId);
    }

    public static class NullableIrsProxy implements IrsProxy {
        @Override
        public void dispose() {
        }

        @Override
        public List<Guid> obtainDomainsReportedAsProblematic(List<VDSDomainsData> vdsDomainsData) {
            return Collections.emptyList();
        }

        @Override
        public void clearVdsFromCache(Guid vdsId, String vdsName) {
        }

        @Override
        public void updateVdsDomainsData(VDS vds, ArrayList<VDSDomainsData> data) {
        }

        @Override
        public boolean getHasVdssForSpmSelection() {
            return false;
        }

        @Override
        public IIrsServer getIrsProxy() {
            return null;
        }

        @Override
        public void runInControlledConcurrency(Runnable codeblock) {
        }

        @Override
        public boolean failover() {
            return false;
        }

        @Override
        public Guid getCurrentVdsId() {
            return null;
        }

        @Override
        public void setCurrentVdsId(Guid value) {
        }

        @Override
        public Guid getPreferredHostId() {
            return null;
        }

        @Override
        public void setPreferredHostId(Guid preferredHostId) {
        }

        @Override
        public Set<Guid> getTriedVdssList() {
            return Collections.emptySet();
        }

        @Override
        public void clearPoolTimers() {
        }

        @Override
        public void clearCache() {
        }

        @Override
        public String getIsoDirectory() {
            return null;
        }

        @Override
        public void setFencedIrs(Guid fencedIrs) {
        }

        @Override
        public void resetIrs() {
        }
    }
}
