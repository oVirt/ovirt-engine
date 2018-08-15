package org.ovirt.engine.core.bll.network.macpool;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.utils.lock.AutoCloseableLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
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
    private AuditLogDirector auditLogDirector;

    private static final Logger log = LoggerFactory.getLogger(MacPoolPerCluster.class);

    static final String UNABLE_TO_CREATE_MAC_POOL_IT_ALREADY_EXIST = "This MAC Pool already exist";
    private final Map<Guid, MacPool> macPools = new HashMap<>();
    private final ReentrantReadWriteLock lockObj = new ReentrantReadWriteLock();

    //required by J2EE specification; session bean should have no-arg constructor.
    public MacPoolPerCluster() {
    }

    MacPoolPerCluster(MacPoolDao macPoolDao,
            ClusterDao clusterDao,
            MacPoolFactory macPoolFactory,
            DecoratedMacPoolFactory decoratedMacPoolFactory) {
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
                createPoolInternal(macPool, true);
            }
            try (AutoCloseableLock lock = readLockResource()) {
                Set<Pair<MacPool, MacPool>> overlappingPools = computeOverlappingPools();
                if (!overlappingPools.isEmpty()) {
                    reportOverlaps(overlappingPools);
                }
            }
            log.info("Successfully initialized");
        } catch (RuntimeException e) {
            log.error("Error initializing: {}", e.getMessage());
            throw e;
        }
    }

    private void reportOverlaps(Set<Pair<MacPool, MacPool>> overlappingPools) {
        Map<Guid, String> idsToNames = macPoolDao.getAll().stream().collect(Collectors.toMap(
            org.ovirt.engine.core.common.businessentities.MacPool::getId,
            org.ovirt.engine.core.common.businessentities.MacPool::getName)
        );
        StringJoiner overlapReport = new StringJoiner(", ");
        overlappingPools.forEach(pair ->
            overlapReport.add("[a range in '" + idsToNames.get(pair.getFirst().getId()) + "' overlaps a range in '" + idsToNames.get(pair.getSecond().getId()) + "']")
        );
        AuditLogableImpl auditLoggable = new AuditLogableImpl();
        auditLoggable.addCustomValue("overlapping", overlapReport.toString());
        auditLogDirector.log(auditLoggable, AuditLogType.MAC_POOL_VIOLATES_NO_OVERLAPPING_MAC_POOLS);
    }

    private Set<Pair<MacPool, MacPool>> computeOverlappingPools() {
        Set<Pair<MacPool, MacPool>> overlappingPools = new HashSet<>();
        macPools.forEach((guid1, pool1) ->
            macPools.forEach((guid2, pool2) -> {
                if (!guid1.equals(guid2) && !overlappingPools.contains(new Pair<>(pool2, pool1)) && pool1.overlaps(pool2)) {
                    overlappingPools.add(new Pair<>(pool1, pool2));
                }
            })
        );
        return overlappingPools;
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

    /**
     * Do not use this method from elsewhere, than from compensation mechanism.
     * @param macPoolId id of MacPool.
     * @return {@link MacPool instance} having given ID.
     */
    public MacPool getMacPoolById(Guid macPoolId) {
        return getMacPoolById(macPoolId, Collections.emptyList());
    }

    /**
     * @param macPoolId id of mac pool.
     * @return {@link MacPool} instance decorated by given decorators.
     */
    private MacPool getMacPoolById(Guid macPoolId, List<MacPoolDecorator> decorators) {
        try (AutoCloseableLock lock = readLockResource()) {
            MacPool result = getMacPoolWithoutLocking(macPoolId, decorators);
            log.debug("Returning {} for requested id={}", result, macPoolId);
            return result;
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
            throw new IllegalStateException(createExceptionMessageMacPoolHavingIdDoesNotExist(macPoolId));
        }

        return decoratedMacPoolFactory.createDecoratedPool(poolById, decorators);
    }

    /**
     * @param macPool pool definition
     */
    public void createPool(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        try (AutoCloseableLock lock = writeLockResource()) {
            createPoolInternal(macPool, false);
        }
    }

    private MacPool createPoolInternal(org.ovirt.engine.core.common.businessentities.MacPool macPool,
            boolean engineStartup) {
        if (macPools.containsKey(macPool.getId())) {
            throw new IllegalStateException(UNABLE_TO_CREATE_MAC_POOL_IT_ALREADY_EXIST);
        }

        log.debug("Creating new MacPool {}.", macPool);
        MacPool poolForScope = macPoolFactory.createMacPool(macPool, engineStartup);
        macPools.put(macPool.getId(), poolForScope);
        return poolForScope;
    }

    /**
     * @return true if the duplicate are not allowed in the pool but it contains duplicates
     */
    public boolean containsDuplicates(Guid macPoolId) {
        return macPools.get(macPoolId).containsDuplicates();
    }

    /**
     * @return true if the duplicate are not allowed in the pool but it contains duplicates
     */
    public boolean isDuplicateMacAddressesAllowed(Guid macPoolId) {
        return macPools.get(macPoolId).isDuplicateMacAddressesAllowed();
    }

    /**
     * @param macPool pool definition to re-init the pool
     */
    public void modifyPool(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        try (AutoCloseableLock lock = writeLockResource()) {
            Guid macPoolId = macPool.getId();
            if (!macPools.containsKey(macPoolId)) {
                throw new IllegalStateException(createExceptionMessageMacPoolHavingIdDoesNotExist(macPoolId));
            }

            log.debug("Updating pool {}. (old will be deleted and new initialized from db entity)", macPool);
            removeWithoutLocking(macPoolId);
            createPoolInternal(macPool, false);
        }
    }

    public void removePool(Guid macPoolId) {
        try (AutoCloseableLock lock = writeLockResource()) {
            removeWithoutLocking(macPoolId);
        }
    }

    private void removeWithoutLocking(Guid macPoolId) {
        log.debug("Removing pool id=", macPoolId);
        macPools.remove(macPoolId);
    }

    String createExceptionMessageMacPoolHavingIdDoesNotExist(Guid macPoolId) {
        return String.format("Pool for id=\"%1$s\" does not exist", macPoolId);
    }

    protected AutoCloseableLock writeLockResource() {
        return new AutoCloseableLock(lockObj.writeLock());
    }

    protected AutoCloseableLock readLockResource() {
        return new AutoCloseableLock(lockObj.readLock());
    }

    public void logFreeMacs() {
        macPools.values()
                .stream()
                .forEach(macPool -> log.info("Mac pool {} has {} available free macs",
                        macPool.getId(),
                        macPool.getAvailableMacsCount()));
    }
}
