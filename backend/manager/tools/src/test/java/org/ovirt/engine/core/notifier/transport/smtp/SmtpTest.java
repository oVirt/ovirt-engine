package org.ovirt.engine.core.notifier.transport.smtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.notifier.dao.DispatchResult;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;
import org.ovirt.engine.core.notifier.filter.AuditLogEventType;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

public class SmtpTest {
    @Test
    public void testCombineEngineBackupMessages() {
        NotificationProperties properties = mock(NotificationProperties.class);
        when(properties.getProperty("MAIL_SERVER", true)).thenReturn("smtp");
        when(properties.getProperty("MAIL_SERVER")).thenReturn("smtp");
        when(properties.getProperty("MAIL_SMTP_ENCRYPTION")).thenReturn("none");
        when(properties.getProperty("MAIL_MERGE_LOG_TYPES", true)).thenReturn(
                "ENGINE_BACKUP_FAILED, ENGINE_BACKUP_COMPLETED, ENGINE_BACKUP_STARTED");
        when(properties.getLong("MAIL_MERGE_MAX_TIME_DIFFERENCE", 5000L)).thenReturn(5000L);
        Smtp smtp = new Smtp(properties);
        Date dt = new Date();
        String address = "user@user.com";
        AuditLogEvent event = prepareEvent(dt, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=files, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log");
        smtp.dispatchEvent(event, address);
        event = prepareEvent(dt, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=db, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log");
        smtp.dispatchEvent(event, address);

        List<DispatchResult> results = new ArrayList<>();
        smtp.registerObserver((o, data) -> results.add(data));
        smtp.idle();
        assertEquals(2, results.size());
        DispatchResult dispatchResult = results.get(0);
        assertFalse(dispatchResult.isSuccess());
        assertEquals(address, dispatchResult.getAddress());
    }

    static AuditLogEvent prepareEvent(Date dt, AuditLogType type, String message) {
        AuditLogEvent event = new AuditLogEvent();
        event.setLogTime(dt);
        event.setType(AuditLogEventType.alertMessage);
        event.setLogTypeName(type.name());
        event.setMessage(message);
        event.setSeverity(AuditLogSeverity.ERROR);
        return event;
    }
}
