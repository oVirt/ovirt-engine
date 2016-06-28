package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.dal.utils.CacheManager;

public abstract class TimeoutBase {
    private boolean useTimeout;
    private long endTime = 0L;

    public boolean getUseTimout() {
        return useTimeout;
    }

    public void setUseTimout(boolean value) {
        useTimeout = value;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long value) {
        useTimeout = true;
        endTime = value;
    }

    private String timeoutObjectId = "";

    protected abstract String getKey();

    private String getkeyForCheck() {
        return "".equals(getTimeoutObjectId()) ? getKey() :  getTimeoutObjectId();
    }

    public String getTimeoutObjectId() {
        return timeoutObjectId;
    }

    public void setTimeoutObjectId(String value) {
        timeoutObjectId = value;
    }

    /**
     * Checks if timeout is used and if it is, checks the timeout. If no timeout set, then it will set this object as
     * timeout.
     *
     * @return should the action be logged again
     */
    public boolean getLegal() {
        if (getUseTimout()) {
            String keyForCheck = getkeyForCheck();
            synchronized (keyForCheck.intern()) {
                if (!CacheManager.getTimeoutBaseCache().containsKey(keyForCheck)) {
                    CacheManager.getTimeoutBaseCache().put(keyForCheck,
                            keyForCheck,
                            getEndTime(),
                            TimeUnit.MILLISECONDS);
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    public void evict(String keyForCheck) {
        synchronized (keyForCheck.intern()) {
            if (CacheManager.getTimeoutBaseCache().containsKey(keyForCheck)) {
                CacheManager.getTimeoutBaseCache().evict(keyForCheck);
            }
        }
    }
}
