package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.utils.CacheManager;

public class EventFloodRegulator {

    private final AuditLogableBase event;
    private final AuditLogType logType;
    private boolean useTimeout;
    private long endTime;
    private String timeoutObjectId;

    public EventFloodRegulator(AuditLogableBase event, AuditLogType logType) {
        this.event = event;
        this.logType = logType;
        timeoutObjectId = "";
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
    public void updateTimeoutLogableObject() {
        int eventFloodRate = (event.isExternal() && event.getEventFloodInSec() == 0)
                ? 30 // Minimal default duration for External Events is 30 seconds.
                : logType.getEventFloodRate();
        if (eventFloodRate > 0) {
            setEndTime(TimeUnit.SECONDS.toMillis(eventFloodRate));
            timeoutObjectId = composeObjectId();
        }
    }

    public void evict() {
        String keyForCheck = composeObjectId();

        synchronized (keyForCheck.intern()) {
            if (CacheManager.getTimeoutBaseCache().containsKey(keyForCheck)) {
                CacheManager.getTimeoutBaseCache().evict(keyForCheck);
            }
        }
    }

    /**
     * Composes an object id from all log id's to identify uniquely each instance.
     *
     * @return unique object id
     */
    private String composeObjectId() {
        final StringBuilder builder = new StringBuilder();

        compose(builder, "type", logType.toString());
        compose(builder, "sd", nullToEmptyString(event.getStorageDomainId()));
        compose(builder, "dc", nullToEmptyString(event.getStoragePoolId()));
        compose(builder, "user", nullToEmptyString(event.getUserId()));
        compose(builder, "cluster", event.getClusterId().toString());
        compose(builder, "vds", event.getVdsId().toString());
        compose(builder, "vm", emptyGuidToEmptyString(event.getVmId()));
        compose(builder, "template", emptyGuidToEmptyString(event.getVmTemplateId()));
        compose(builder, "customId", StringUtils.defaultString(event.getCustomId()));

        return builder.toString();
    }

    private void compose(StringBuilder builder, String key, String value) {
        final char DELIMITER = ',';
        final char NAME_VALUE_SEPARATOR = '=';
        if (builder.length() > 0) {
            builder.append(DELIMITER);
        }

        builder.append(key).append(NAME_VALUE_SEPARATOR).append(value);
    }

    private void setEndTime(long value) {
        useTimeout = true;
        endTime = value;
    }

    private String emptyGuidToEmptyString(Guid guid) {
        return Guid.Empty.equals(guid) ? "" : guid.toString();
    }

    private static String nullToEmptyString(Object obj) {
        return Objects.toString(obj, "");
    }
}
