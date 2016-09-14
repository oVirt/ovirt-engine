package org.ovirt.engine.core.bll.network.macpool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.LockedObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DecoratedMacPoolFactory {
    private static final Logger log = LoggerFactory.getLogger(DecoratedMacPoolFactory.class);

    @Inject
    private LockedObjectFactory lockedObjectFactory;

    private final Map<Guid, ReentrantReadWriteLock> poolLocks = new HashMap<>();

    public DecoratedMacPoolFactory() {
    }

    public DecoratedMacPoolFactory(LockedObjectFactory lockedObjectFactory) {
        this.lockedObjectFactory = lockedObjectFactory;
    }

    public MacPool createDecoratedPool(MacPool macPool, List<MacPoolDecorator> decorators) {
        MacPool lockedPool =
                lockedObjectFactory.createLockingInstance(macPool, MacPool.class, lockForMacPool(macPool.getId()));

        MacPool decoratedPool = decoratePool(lockedPool, decorators);
        log.debug("MacPool {} decorated as {}.", macPool, decoratedPool);
        return decoratedPool;
    }

    private synchronized ReentrantReadWriteLock lockForMacPool(Guid macPoolId) {
        if (poolLocks.containsKey(macPoolId)) {
            log.debug("Returning cached read/write lock for macPoolId={}", macPoolId);
            return poolLocks.get(macPoolId);
        }

        log.debug("Creating read/write lock for macPoolId={}", macPoolId);
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        poolLocks.put(macPoolId, lock);
        return lock;
    }

    /**
     * Decorates actual pool with given decorators, applying first passed decorator on actual pool,
     * next on decorated object from previous step etc.
     *
     * @param macPool actual pool instance
     * @param decorators collection of decorators  @return decorated pool.
     */
    private MacPool decoratePool(MacPool macPool, List<MacPoolDecorator> decorators) {
        if (CollectionUtils.isEmpty(decorators)) {
            log.debug("No MacPoolDecorators were passed to decorate pool {}. ", macPool);
            return macPool;
        }

        MacPool result = macPool;
        log.debug("Decorating MacPool {} with decorators: {}.", Arrays.toString(decorators.toArray()));
        for (MacPoolDecorator decorator : decorators) {
            decorator.setMacPool(result);
            result = decorator;
        }

        return result;
    }
}
