package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class TimeoutBase implements Serializable {
    private static final long serialVersionUID = -4969034051659487755L;
    private static final Map<String, TimeoutBase> mHandler = new HashMap<String, TimeoutBase>();
    private static final Object mLock = new Object();
    private boolean mUseTimeout;
    private long mEndTime = 0L;

    public boolean getUseTimout() {
        return mUseTimeout;
    }

    public void setUseTimout(boolean value) {
        mUseTimeout = value;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long value) {
        mUseTimeout = true;
        mEndTime = value;
    }

    private String timeoutObjectId = "";

    protected abstract String getKey();

    private String getkeyForCheck() {
        return "".equals(getTimeoutObjectId()) ? getKey() : String.format("%1$s_%2$s", getKey(), getTimeoutObjectId());
    }

    public String getTimeoutObjectId() {
        return timeoutObjectId;
    }

    public void setTimeoutObjectId(String value) {
        timeoutObjectId = value;
    }

    /**
     * Checks if timeout is used and if it is, checks the timeout. If no timeout set, then it will set this object as timeout.
     * @return should the action be logged again
     */
    public boolean getLegal() {
        boolean returnValue = true;
        if (getUseTimout()) {
            synchronized (mLock) {
                String keyForCheck = getkeyForCheck();
                TimeoutBase timeoutBase = mHandler.get(keyForCheck);

                if (timeoutBase != null) {
                    // not first try. check if timeout passed
                    if (System.currentTimeMillis() < timeoutBase.getEndTime()) {
                        returnValue = false;
                    } else {
                        // timeout over. Clean data
                        mHandler.remove(keyForCheck);
                    }
                } else {
                    // first try, add value
                    mHandler.put(keyForCheck, this);
                }
            }
        }

        return returnValue;
    }
}
