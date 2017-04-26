package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.dal.utils.CacheManager;

public class EventFloodRegulator {

    private final AuditLogable event;
    private final AuditLogType logType;
    private boolean useTimeout;
    private long endTime;
    private String timeoutObjectId;

    public EventFloodRegulator(AuditLogable event, AuditLogType logType) {
        this.event = event;
        this.logType = logType;
        updateTimeoutLogableObject();
    }

    /**
     * Checks if timeout is used and if it is, checks the timeout. If no timeout set, then it will set this object as
     * timeout.
     *
     * @return should the action be logged again
     */
    public boolean isLegal() {
        if (useTimeout) {
            String keyForCheck = "".equals(timeoutObjectId) ? logType.toString() : timeoutObjectId;
            synchronized (keyForCheck.intern()) {
                if (!CacheManager.getTimeoutBaseCache().containsKey(keyForCheck)) {
                    CacheManager.getTimeoutBaseCache().put(keyForCheck,
                            keyForCheck,
                            endTime,
                            TimeUnit.MILLISECONDS);
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    /**
     * Update the logged object timeout attribute by log type definition
     */
    private void updateTimeoutLogableObject() {
        int eventFloodRate = (event.isExternal() && event.getEventFloodInSec() == 0)
                ? 30 // Minimal default duration for External Events is 30 seconds.
                : logType.getEventFloodRate();
        if (eventFloodRate > 0) {
            setEndTime(TimeUnit.SECONDS.toMillis(eventFloodRate));
            timeoutObjectId = composeObjectId();
        } else {
            timeoutObjectId = "";
        }
    }

    private String composeObjectId() {
        return EventKeyComposer.composeObjectId(event, logType);
    }

    public void evict() {
        String keyForCheck = composeObjectId();

        synchronized (keyForCheck.intern()) {
            if (CacheManager.getTimeoutBaseCache().containsKey(keyForCheck)) {
                CacheManager.getTimeoutBaseCache().evict(keyForCheck);
            }
        }
    }

    private void setEndTime(long value) {
        useTimeout = true;
        endTime = value;
    }
}
