package org.ovirt.engine.core.bll.eventqueue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@Singleton(name = "EventQueue")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Local(EventQueue.class)
public class EventQueueMonitor implements EventQueue {

    private static Log log = LogFactory.getLog(EventQueueMonitor.class);

    private static final ConcurrentMap<Guid, ReentrantLock> poolsLockMap = new ConcurrentHashMap<Guid, ReentrantLock>();
    private static final Map<Guid, LinkedList<Pair<Event, FutureTask<EventResult>>>> poolsEventsMap =
            new HashMap<Guid, LinkedList<Pair<Event, FutureTask<EventResult>>>>();
    private static final Map<Guid, Event> poolCurrentEventMap = new HashMap<Guid, Event>();

    @Override
    public void submitEventAsync(Event event, Callable<EventResult> callable) {
        submitTaskInternal(event, callable);
    }

    @Override
    public EventResult submitEventSync(Event event, Callable<EventResult> callable) {
        FutureTask<EventResult> task = submitTaskInternal(event, callable);
        if (task != null) {
            try {
                return task.get();
            } catch (Exception e) {
                log.errorFormat("Failed at submitEventSync, for pool {0} with exception {1}",
                        event.getStoragePoolId(), e);
            }
        }
        return null;
    }

    private FutureTask<EventResult> submitTaskInternal(Event event,
            Callable<EventResult> callable) {
        FutureTask<EventResult> task = null;
        Guid storagePoolId = event.getStoragePoolId();
        ReentrantLock lock = getPoolLock(storagePoolId);
        lock.lock();
        try {
            Event currentEvent = poolCurrentEventMap.get(storagePoolId);
            if (currentEvent != null) {
                switch (currentEvent.getEventType()) {
                case RECONSTRUCT:
                    if (event.getEventType() == EventType.VDSCONNECTTOPOOL || event.getEventType() == EventType.RECOVERY) {
                        task = addTaskToQueue(event, callable, storagePoolId, isEventShouldBeFirst(event));
                    } else {
                        log.debugFormat("Current event was skiped because of reconstruct is running now for pool {0}, event {1}",
                                storagePoolId, event);
                    }
                    break;
                default:
                    task = addTaskToQueue(event, callable, storagePoolId, isEventShouldBeFirst(event));
                    break;
                }
            } else {
                task = addTaskToQueue(event, callable, storagePoolId, false);
                poolCurrentEventMap.put(storagePoolId, event);
                ThreadPoolUtil.execute(new InternalEventQueueThread(storagePoolId, lock,
                        poolsEventsMap, poolCurrentEventMap));
            }
        } finally {
            lock.unlock();
        }
        return task;
    }

    /**
     * The following method should decide if we want that the event will be first for executing, before all other events
     * already submitted to queue
     * @param event
     *            - submitted event
     * @return
     */
    private boolean isEventShouldBeFirst(Event event) {
        return event.getEventType() == EventType.RECOVERY;
    }

    private FutureTask<EventResult> addTaskToQueue(Event event, Callable<EventResult> callable, Guid storagePoolId, boolean addFirst) {
        FutureTask<EventResult> task = new FutureTask<EventResult>(callable);
        Pair<Event, FutureTask<EventResult>> queueEvent = new Pair<Event, FutureTask<EventResult>>(event, task);
        if (addFirst) {
            getEventQueue(storagePoolId).addFirst(queueEvent);
        } else {
            getEventQueue(storagePoolId).add(queueEvent);
        }
        return task;
    }

    private LinkedList<Pair<Event, FutureTask<EventResult>>> getEventQueue(Guid storagePoolId) {
        LinkedList<Pair<Event, FutureTask<EventResult>>> queue = poolsEventsMap.get(storagePoolId);
        if (queue == null) {
            queue = new LinkedList<Pair<Event, FutureTask<EventResult>>>();
            poolsEventsMap.put(storagePoolId, queue);
        }
        return queue;
    }

    private ReentrantLock getPoolLock(Guid poolId) {
        if (!poolsLockMap.containsKey(poolId)) {
            poolsLockMap.putIfAbsent(poolId, new ReentrantLock());
        }
        return poolsLockMap.get(poolId);
    }

    private static class InternalEventQueueThread implements Runnable {

        private Guid storagePoolId;
        private ReentrantLock lock;
        private Map<Guid, Event> poolCurrentEventMap;
        private Map<Guid, LinkedList<Pair<Event, FutureTask<EventResult>>>> poolsEventsMap;

        public InternalEventQueueThread(Guid storagePoolId,
                ReentrantLock lock,
                Map<Guid, LinkedList<Pair<Event, FutureTask<EventResult>>>> poolsEventsMap,
                Map<Guid, Event> poolCurrentEventMap) {
            this.storagePoolId = storagePoolId;
            this.lock = lock;
            this.poolsEventsMap = poolsEventsMap;
            this.poolCurrentEventMap = poolCurrentEventMap;
        }

        @Override
        public void run() {
            while (true) {
                Pair<Event, FutureTask<EventResult>> pair;
                lock.lock();
                try {
                    pair = poolsEventsMap.get(storagePoolId).poll();
                    if (pair != null) {
                        poolCurrentEventMap.put(storagePoolId, pair.getFirst());
                    } else {
                        poolCurrentEventMap.remove(storagePoolId);
                        poolsEventsMap.remove(storagePoolId);
                        log.debugFormat("All task for event query were executed pool {0}", storagePoolId);
                        break;
                    }
                } finally {
                    lock.unlock();
                }
                Future<EventResult> futureResult = ThreadPoolUtil.execute(pair.getSecond());
                try {
                    if (futureResult.get() == null) {
                        EventResult result = pair.getSecond().get();
                        if (result != null && result.getEventType() == EventType.RECONSTRUCT) {
                            log.infoFormat("Finished reconstruct for pool {0}. Clearing event queue", storagePoolId);
                            lock.lock();
                            try {
                                LinkedList<Pair<Event, FutureTask<EventResult>>> queue =
                                        new LinkedList<Pair<Event, FutureTask<EventResult>>>();
                                for (Pair<Event, FutureTask<EventResult>> task : poolsEventsMap.get(storagePoolId)) {
                                    EventType eventType = task.getFirst().getEventType();
                                    if (eventType == EventType.VDSCONNECTTOPOOL || (eventType == EventType.RECOVERY && !result.isSuccess())) {
                                        queue.add(task);
                                    } else {
                                        log.infoFormat("The following operation {0} was cancelled, because of recosntruct was run before",
                                                task.getFirst());
                                        task.getSecond().cancel(true);
                                    }
                                }
                                if (queue.isEmpty()) {
                                    poolCurrentEventMap.remove(storagePoolId);
                                    poolsEventsMap.remove(storagePoolId);
                                    break;
                                } else {
                                    poolsEventsMap.put(storagePoolId, queue);
                                }
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.errorFormat("Exception during process of events for pool {0}, error is {1}",
                            storagePoolId, e.getMessage());
                }
            }
        }
    }
}
