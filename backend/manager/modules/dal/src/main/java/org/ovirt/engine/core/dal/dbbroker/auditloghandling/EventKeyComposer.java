package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;

public class EventKeyComposer {

    private static final char KEY_PARTS_DELIMITER = ',';
    private static final char NAME_VALUE_SEPARATOR = '=';

    private EventKeyComposer() {
    }

    /**
     * Composes an object id from all log id's to identify uniquely each instance.
     *
     * @param event
     *            the event entity that its attributes will be used to created the key
     * @param logType
     *            the log type associated with the event
     * @return unique object id
     */
    public static String composeObjectId(AuditLogable event, AuditLogType logType) {
        final StringBuilder builder = new StringBuilder();

        compose(builder, "type", logType.toString());
        compose(builder, "sd", emptyGuidToEmptyString(event.getStorageDomainId()));
        compose(builder, "dc", emptyGuidToEmptyString(event.getStoragePoolId()));
        compose(builder, "user", emptyGuidToEmptyString(event.getUserId()));
        compose(builder, "cluster", emptyGuidToEmptyString(event.getClusterId()));
        compose(builder, "vds", emptyGuidToEmptyString(event.getVdsId()));
        compose(builder, "vm", emptyGuidToEmptyString(event.getVmId()));
        compose(builder, "template", emptyGuidToEmptyString(event.getVmTemplateId()));
        compose(builder, "customId", StringUtils.defaultString(event.getCustomId()));

        return builder.toString();
    }

    private static void compose(StringBuilder builder, String key, String value) {
        if (builder.length() > 0) {
            builder.append(KEY_PARTS_DELIMITER);
        }

        builder.append(key).append(NAME_VALUE_SEPARATOR).append(value);
    }

    private static String emptyGuidToEmptyString(Guid guid) {
        return (guid == null || Guid.Empty.equals(guid)) ? "" : guid.toString();
    }
}
