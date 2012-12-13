package org.ovirt.engine.core.notifier.utils.mail;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.EventAuditLogSubscriber;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.ovirt.engine.core.notifier.utils.sender.EventSenderResult;
import org.ovirt.engine.core.notifier.utils.sender.mail.EventSenderMailImpl;
import org.ovirt.engine.core.notifier.utils.sender.mail.JavaMailSender;

/**
 * A tester of the {@link JavaMailSender}. Tests both secured and non-secured methods of mail sending as SMTP and SMTP
 * over SSL.
 */
public class MailSenderTest {

    /**
     * The test covers sending an email using non-secured mail client (SMTP) configuration. <br>
     * The test covers the following: <br>
     * <li>Creation of a formatted message
     * <li>Creating a non secured mail client
     * <li>Send an email to the user which contains faked data with HTML body message
     */
    @Test
    public void testNonSecuredEventMailSender() {
        EventAuditLogSubscriber eventData = new EventAuditLogSubscriber();
        eventData.setlog_time(new Date());
        eventData.setevent_type(0);
        eventData.setuser_name("a test user name");
        eventData.setvm_name("a fine test VM");
        eventData.setvds_name("a fine test host");
        eventData.setmethod_address("mailtest.redhat@gmail.com");
        eventData.setvm_template_name("a test template");
        eventData.setstorage_pool_name("a test storage pool name");
        eventData.setstorage_domain_name("a test storage pool domain");
        eventData.setseverity(3);

        EventSenderMailImpl mailSender = new EventSenderMailImpl(getMailProperties());
        eventData.setmessage("a test message to be sent via non-secured mode");
        EventSenderResult sentResult = null;
        try {
            sentResult = mailSender.send(eventData, null);
        } catch (Exception e) {
            sentResult = new EventSenderResult();
            sentResult.setSent(false);
        }
        assertTrue(sentResult.isSent());
    }

    /**
     * The test covers sending an email using secured mail client (SMTP over SSL) configuration. <br>
     * The test covers the following: <br>
     * <li>Creation of a formatted message
     * <li>Creating a non secured mail client
     * <li>Send an email to the user which contains faked data as a plan text message
     */
    @Test
    public void testSecuredEventMailSender() {
        EventAuditLogSubscriber eventData = new EventAuditLogSubscriber();
        eventData.setlog_time(new Date());
        eventData.setevent_type(0);
        eventData.setuser_name("a test user name");
        eventData.setvm_name("a fine test VM");
        eventData.setvds_name("a fine test host");
        eventData.setmethod_address("mailtest.redhat@gmail.com");
        eventData.setvm_template_name("a test template");
        eventData.setstorage_pool_name("a test storage pool name");
        eventData.setstorage_domain_name("a test storage pool domain");
        eventData.setseverity(3);

        EventSenderMailImpl mailSender = new EventSenderMailImpl(getSecuredMailProperties());
        eventData.setmessage("a test message to be sent via secured mode");
        mailSender.send(eventData, null);

        EventSenderResult sentResult = null;
        try {
            sentResult = mailSender.send(eventData, null);
        } catch (Exception e) {
            sentResult = new EventSenderResult();
            sentResult.setSent(false);
        }

        assertTrue(sentResult.isSent());
    }

    private static Map<String, String> getMailProperties() {
        Map<String, String> prop = new HashMap<String, String>();
        prop.put(NotificationProperties.MAIL_SERVER, "smtp.redhat.com");
        prop.put(NotificationProperties.MAIL_USER, "dev-null@redhat.com");
        prop.put(NotificationProperties.HTML_MESSAGE_FORMAT, "true");

        return prop;
    }

    private static Map<String, String> getSecuredMailProperties() {
        Map<String, String> prop = new HashMap<String, String>();
        prop.put(NotificationProperties.MAIL_SERVER, "smtp.gmail.com");
        prop.put(NotificationProperties.MAIL_PORT, "465");
        prop.put(NotificationProperties.MAIL_USER, "mailtest.redhat@gmail.com");
        prop.put(NotificationProperties.MAIL_PASSWORD, "q1!w2@e3#!");
        prop.put(NotificationProperties.MAIL_FROM, "dev-null@redhat.com");
        prop.put(NotificationProperties.MAIL_ENABLE_SSL, "true");
        return prop;
    }

}
