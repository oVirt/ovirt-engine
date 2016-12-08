package org.ovirt.engine.core.bll.network.macpool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.utils.lock.AutoCloseableLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class MacPoolPerCluster {

    @Inject
    private MacPoolDao macPoolDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private DecoratedMacPoolFactory decoratedMacPoolFactory;

    @Inject
    MacPoolFactory macPoolFactory;

    @Inject
    private DbFacade dbFacade;

    private static final Logger log = LoggerFactory.getLogger(MacPoolPerCluster.class);

    static final String UNABLE_TO_CREATE_MAC_POOL_IT_ALREADY_EXIST = "This MAC Pool already exist";
    static final String INEXISTENT_POOL_EXCEPTION_MESSAGE = "Coding error, pool for requested GUID does not exist";
    static final String POOL_TO_BE_REMOVED_DOES_NOT_EXIST_EXCEPTION_MESSAGE =
            "Trying to removed pool which does not exist.";
    private final Map<Guid, MacPool> macPools = new HashMap<>();
    private final ReentrantReadWriteLock lockObj = new ReentrantReadWriteLock();

    //required by J2EE specification; session bean should have no-arg constructor.
    public MacPoolPerCluster() {}

    MacPoolPerCluster(MacPoolDao macPoolDao, ClusterDao clusterDao, MacPoolFactory macPoolFactory, DecoratedMacPoolFactory decoratedMacPoolFactory) {
        this.macPoolDao = macPoolDao;
        this.clusterDao = clusterDao;
        this.macPoolFactory = macPoolFactory;
        this.decoratedMacPoolFactory = decoratedMacPoolFactory;
    }

    @PostConstruct
    void initialize() {
        try {
            List<org.ovirt.engine.core.common.businessentities.MacPool> macPools = macPoolDao.getAll();
            for (org.ovirt.engine.core.common.businessentities.MacPool macPool : macPools) {
                initializeMacPool(macPool);
            }
            log.info("Successfully initialized");
        } catch (RuntimeException e) {
            log.error("Error initializing: {}", e.getMessage());
            throw e;
        }
    }

    private void initializeMacPool(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        List<String> macsForMacPool = macPoolDao.getAllMacsForMacPool(macPool.getId());

        final MacPool pool = createPoolInternal(macPool);
        for (String mac : macsForMacPool) {
            pool.forceAddMac(mac);
        }
    }

    /**
     * @param clusterId id of cluster.
     * @return {@link ReadMacPool} instance that is transaction & compensation agnostic.
     */
    public ReadMacPool getMacPoolForCluster(Guid clusterId) {
        return getMacPoolById(getMacPoolId(clusterId));
    }

    /**
     * @param clusterId id of cluster.
     * @param commandContext command context, is used by {@link TransactionalMacPoolDecorator}.
     * @return {@link MacPool} instance to be used within transaction, compensation or scope without either.
     * @throws NullPointerException if commandContext is null
     */
    public MacPool getMacPoolForCluster(Guid clusterId, CommandContext commandContext) {
        Objects.requireNonNull(commandContext);
        return getMacPoolById(getMacPoolId(clusterId), commandContext);
    }

    public MacPool getMacPoolById(Guid macPoolId) {
        return getMacPoolById(macPoolId, Collections.emptyList());
    }

    /**
     * @param macPoolId id of mac pool.
     * @return {@link MacPool} instance decorated by given decorators.
     */
    private MacPool getMacPoolById(Guid macPoolId, List<MacPoolDecorator> decorators) {
        try (AutoCloseableLock lock = readLockResource()) {
            return getMacPoolWithoutLocking(macPoolId, decorators);
        }
    }

    public MacPool getMacPoolById(Guid macPoolId, CommandContext commandContext) {
        return getMacPoolById(macPoolId, Collections.singletonList(new TransactionalMacPoolDecorator(commandContext)));
    }

    private Guid getMacPoolId(Guid clusterId) {
        final Cluster cluster = clusterDao.get(clusterId);
        return cluster == null ? null : cluster.getMacPoolId();
    }

    private MacPool getMacPoolWithoutLocking(Guid macPoolId, List<MacPoolDecorator> decorators) {
        final MacPool poolById = macPools.get(macPoolId);

        if (poolById == null) {
            throw new IllegalStateException(INEXISTENT_POOL_EXCEPTION_MESSAGE);
        }

        return decoratedMacPoolFactory.createDecoratedPool(macPoolId, poolById, decorators);
    }

    /**
     * @param macPool pool definition
     */
    public void createPool(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        try (AutoCloseableLock lock = writeLockResource()) {
            createPoolInternal(macPool);
        }
    }

    private MacPool createPoolInternal(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        if (macPools.containsKey(macPool.getId())) {
            throw new IllegalStateException(UNABLE_TO_CREATE_MAC_POOL_IT_ALREADY_EXIST);
        }

        MacPool poolForScope = macPoolFactory.createMacPool(macPool);
        macPools.put(macPool.getId(), poolForScope);
        return poolForScope;
    }

    /**
     * @param macPool pool definition to re-init the pool
     */
    public void modifyPool(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        try (AutoCloseableLock lock = writeLockResource()) {
            if (!macPools.containsKey(macPool.getId())) {
                throw new IllegalStateException(INEXISTENT_POOL_EXCEPTION_MESSAGE);
            }

            removeWithoutLocking(macPool.getId());
            initializeMacPool(macPool);
        }
    }

    public void removePool(Guid macPoolId) {
        try (AutoCloseableLock lock = writeLockResource()) {
            removeWithoutLocking(macPoolId);
        }
    }

    private void removeWithoutLocking(Guid macPoolId) {
        macPools.remove(macPoolId);
    }

    protected AutoCloseableLock writeLockResource() {
        return new AutoCloseableLock(lockObj.writeLock());
    }

    protected AutoCloseableLock readLockResource() {
        return new AutoCloseableLock(lockObj.readLock());
    }
}
