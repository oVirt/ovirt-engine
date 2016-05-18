package org.ovirt.engine.core.bll.network.macpool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.LockedObjectFactory;

@ApplicationScoped
public class DecoratedMacPoolFactory {
    @Inject
    private LockedObjectFactory lockedObjectFactory;

    private final Map<Guid, ReentrantReadWriteLock> poolLocks = new ConcurrentHashMap<>();

    public DecoratedMacPoolFactory() {
    }

    public DecoratedMacPoolFactory(LockedObjectFactory lockedObjectFactory) {
        this.lockedObjectFactory = lockedObjectFactory;
    }

    public MacPool createDecoratedPool(Guid macPoolId, MacPool poolById, List<MacPoolDecorator> decorators) {
        MacPool decoratedPool = decoratePool(macPoolId, poolById, decorators);
        return lockedObjectFactory.createLockingInstance(decoratedPool, MacPool.class, lockForMacPool(macPoolId));
    }

    private ReentrantReadWriteLock lockForMacPool(Guid macPoolId) {
        if (poolLocks.containsKey(macPoolId)) {
            return poolLocks.get(macPoolId);
        }

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        poolLocks.put(macPoolId, lock);
        return lock;
    }

    /**
     * Decorates actual pool with given decorators, applying first passed decorator on actual pool,
     * next on decorated object from previous step etc.
     *
     * @param macPoolId id of mac pool passed in second parameter
     * @param poolById actual pool instance
     * @param decorators collection of decorators  @return decorated pool.
     */
    private MacPool decoratePool(Guid macPoolId, MacPool poolById, List<MacPoolDecorator> decorators) {
        MacPool result = poolById;

        if (decorators != null) {
            for (MacPoolDecorator decorator : decorators) {
                decorator.setMacPool(result);
                decorator.setMacPoolId(macPoolId);
                result = decorator;
            }
        }

        return result;
    }
}
