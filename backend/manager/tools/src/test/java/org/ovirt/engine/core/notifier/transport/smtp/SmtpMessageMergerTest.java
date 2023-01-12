package org.ovirt.engine.core.notifier.transport.smtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.notifier.dao.DispatchResult;
import org.ovirt.engine.core.notifier.transport.Observable;
import org.ovirt.engine.core.notifier.transport.Observer;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

public class SmtpMessageMergerTest {

    private List<Smtp.DispatchAttempt> testMessages = new ArrayList<>();

    @BeforeEach
    public void initTest() {
        String address = "user@user.com";
        Date date = new Date();
        Date shiftedDate = new Date(date.getTime() - 1000);
        Date significantlyShiftedDate = new Date(date.getTime() - 10000);
        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(date, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=files, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                address));
        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(date, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=db, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                address));
        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(significantlyShiftedDate, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=grafanadb, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                address));

        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(date, AuditLogType.ENGINE_BACKUP_STARTED, "engine-backup: Backup Started, scope=files, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                address));
        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(date, AuditLogType.ENGINE_BACKUP_STARTED, "engine-backup: Backup Started, scope=db, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                address));

        String anotherAddress = "yetanotheruser@user.com";
        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(date, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=files, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                anotherAddress));
        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(date, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=db, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                anotherAddress));
        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(shiftedDate, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=grafanadb, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                anotherAddress));

        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(shiftedDate, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=dwhdb, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                address));
        testMessages.add(new Smtp.DispatchAttempt(
                SmtpTest.prepareEvent(shiftedDate, AuditLogType.ENGINE_BACKUP_COMPLETED, "engine-backup: Backup Finished, scope=dwhdb, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log"),
                anotherAddress));
    }

    @Test
    public void testMergeNotConfigured() {
        NotificationProperties properties = mock(NotificationProperties.class);
        SmtpMessageMerger messageMerger = new SmtpMessageMerger(properties);
        int numberOfEventsBeforeMerge = testMessages.size();

        messageMerger.mergeSimilarEvents(testMessages);

        assertEquals(numberOfEventsBeforeMerge, testMessages.size(), "Events were merged, but should not.");
    }

    @Test
    public void testMerge() {
        NotificationProperties properties = mock(NotificationProperties.class);
        when(properties.getProperty("MAIL_MERGE_LOG_TYPES", true)).thenReturn(
                "ENGINE_BACKUP_COMPLETED");
        when(properties.getLong("MAIL_MERGE_MAX_TIME_DIFFERENCE", 5000L)).thenReturn(5000L);
        SmtpMessageMerger messageMerger = new SmtpMessageMerger(properties);

        messageMerger.mergeSimilarEvents(testMessages);

        assertEquals(5, testMessages.size(), "Events were merged incorrectly");

        //First 2 ENGINE_BACKUP_COMPLETED and second from the end events should be merged, because they have the same
        //LogType, address and similar log time (within 5 seconds)
        Smtp.DispatchAttempt event = testMessages.get(0);
        assertEquals(AuditLogType.ENGINE_BACKUP_COMPLETED.name(), event.event.getLogTypeName());
        assertEquals(2, event.merged.size());

        //Third ENGINE_BACKUP_COMPLETED was not merged with any other events because it has significantly different
        //log time (10 seconds in the past)
        event = testMessages.get(1);
        assertEquals(AuditLogType.ENGINE_BACKUP_COMPLETED.name(), event.event.getLogTypeName());
        assertEquals(0, event.merged.size());

        //Next 2 ENGINE_BACKUP_STARTED events were not merged because their type was not configured via
        //MAIL_MERGE_LOG_TYPES
        event = testMessages.get(2);
        assertEquals(AuditLogType.ENGINE_BACKUP_STARTED.name(), event.event.getLogTypeName());
        assertEquals(0, event.merged.size());

        event = testMessages.get(3);
        assertEquals(AuditLogType.ENGINE_BACKUP_STARTED.name(), event.event.getLogTypeName());
        assertEquals(0, event.merged.size());

        //Last 4 ENGINE_BACKUP_COMPLETED were merged into one, because they have the same LogType, address and similar
        //log time (within 5 seconds)
        event = testMessages.get(4);
        assertEquals(AuditLogType.ENGINE_BACKUP_COMPLETED.name(), event.event.getLogTypeName());
        assertEquals(3, event.merged.size());
    }

    @Test
    public void testPrepareMessageContent() {
        NotificationProperties properties = mock(NotificationProperties.class);
        when(properties.getProperty("MAIL_MERGE_LOG_TYPES", true)).thenReturn(
                "ENGINE_BACKUP_COMPLETED,ENGINE_BACKUP_STARTED");
        when(properties.getLong("MAIL_MERGE_MAX_TIME_DIFFERENCE", 5000L)).thenReturn(5000L);
        SmtpMessageMerger messageMerger = new SmtpMessageMerger(properties);

        messageMerger.mergeSimilarEvents(testMessages);

        assertEquals(4, testMessages.size(), "Events were merged incorrectly");

        //Test Plain Text
        Smtp.DispatchAttempt event = testMessages.get(0);
        EventMessageContent content = messageMerger.prepareEMailMessageContent("localhost", event, false, null);
        String expectedSubject = "alertMessage (localhost), [engine-backup: Backup Finished, scope=files, db, dwhdb, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log]";
        String expectedBody = "Time:" + event.event.getLogTime() + "\n" +
                "Message:engine-backup: Backup Finished, scope=files, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log\n" +
                "Severity:ERROR\n" +
                "\n==========================\n" +
                "\n" +
                "Time:" + event.merged.get(0).getLogTime() + "\n" +
                "Message:engine-backup: Backup Finished, scope=db, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log\n" +
                "Severity:ERROR\n" +
                "\n==========================\n" +
                "\n" +
                "Time:" + event.merged.get(1).getLogTime() + "\n" +
                "Message:engine-backup: Backup Finished, scope=dwhdb, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log\n" +
                "Severity:ERROR\n";
        assertEquals(expectedSubject, content.getMessageSubject());
        assertEquals(expectedBody, content.getMessageBody());

        event = testMessages.get(1);
        content = messageMerger.prepareEMailMessageContent("localhost", event, false, null);
        expectedSubject = "alertMessage (localhost), [engine-backup: Backup Finished, scope=grafanadb, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log]";
        expectedBody = "Time:" + event.event.getLogTime() + "\n" +
                "Message:engine-backup: Backup Finished, scope=grafanadb, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log\n" +
                "Severity:ERROR\n";
        assertEquals(expectedSubject, content.getMessageSubject());
        assertEquals(expectedBody, content.getMessageBody());

        //Test HTML
        event = testMessages.get(2);
        content = messageMerger.prepareEMailMessageContent("localhost", event, true, null);
        expectedSubject = "alertMessage (localhost), [engine-backup: Backup Started, scope=files, db, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log]";
        expectedBody = "<b>Time:</b> " + event.event.getLogTime() + "<br>" +
                "<b>Message:</b> engine-backup: Backup Started, scope=files, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log<br>" +
                "<b>Severity:</b> ERROR<p><br>" +
                "<br>==========================<br>" +
                "<br>" +
                "<b>Time:</b> " + event.merged.get(0).getLogTime() + "<br>" +
                "<b>Message:</b> engine-backup: Backup Started, scope=db, log=/var/log/ovirt-engine-backup/ovirt-engine-backup-20221215053034.log<br>" +
                "<b>Severity:</b> ERROR<p>";
        assertEquals(expectedSubject, content.getMessageSubject());
        assertEquals(expectedBody, content.getMessageBody());
    }

    @Test
    public void testNotifyAboutSuccessOrFailure() {
        NotificationProperties properties = mock(NotificationProperties.class);
        when(properties.getProperty("MAIL_MERGE_LOG_TYPES", true)).thenReturn(
                "ENGINE_BACKUP_COMPLETED,ENGINE_BACKUP_STARTED");
        when(properties.getLong("MAIL_MERGE_MAX_TIME_DIFFERENCE", 5000L)).thenReturn(5000L);
        SmtpMessageMerger messageMerger = new SmtpMessageMerger(properties);

        assertEquals(10, testMessages.size(), "Test data was changed, the test should be adjusted accordingly");
        List<DispatchResult> notifications = new ArrayList<>();
        Observable observable = new Observable() {
            @Override
            public void notifyObservers(DispatchResult data) {
                notifications.add(data);
            }

            @Override
            public void registerObserver(Observer observer) {
            }

            @Override
            public void removeObserver(Observer observer) {
            }
        };

        //Verify notifyAboutSuccess before messages merged
        for (Smtp.DispatchAttempt event : testMessages) {
            messageMerger.notifyAboutSuccess(observable, event);
        }

        assertEquals(10, notifications.size());
        long successes = notifications.stream().filter(DispatchResult::isSuccess).count();
        assertEquals(10L, successes);

        //Verify notifyAboutFailure before messages merged
        notifications.clear();
        for (Smtp.DispatchAttempt event : testMessages) {
            messageMerger.notifyAboutFailure(observable, event, "an error");
        }
        assertEquals(10, notifications.size());
        successes = notifications.stream().filter(DispatchResult::isSuccess).count();
        assertEquals(0L, successes);

        messageMerger.mergeSimilarEvents(testMessages);
        assertEquals(4, testMessages.size(), "Events were merged incorrectly");

        //Verify notifyAboutSuccess when messages were merged
        notifications.clear();
        for (Smtp.DispatchAttempt event : testMessages) {
            messageMerger.notifyAboutSuccess(observable, event);
        }

        assertEquals(10, notifications.size()); // the number of notifications should not be changed even if messages were merged
        successes = notifications.stream().filter(DispatchResult::isSuccess).count();
        assertEquals(10L, successes);

        //Verify notifyAboutFailure when messages were merged
        notifications.clear();
        for (Smtp.DispatchAttempt event : testMessages) {
            messageMerger.notifyAboutFailure(observable, event, "yet another error");
        }

        assertEquals(10, notifications.size()); // the number of notifications should not be changed even if messages were merged
        successes = notifications.stream().filter(DispatchResult::isSuccess).count();
        assertEquals(0L, successes);
    }
}
