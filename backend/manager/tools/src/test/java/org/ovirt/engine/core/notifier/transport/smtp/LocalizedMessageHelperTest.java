package org.ovirt.engine.core.notifier.transport.smtp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.notifier.filter.AuditLogEventType;

public class LocalizedMessageHelperTest {

    private MessageBody message;

    @BeforeEach
    public void init() {
        message = new MessageBody();
        message.setUserInfo("user@user.com");
        message.setVmInfo("vm01");
        message.setHostInfo("host");
        message.setTemplateInfo("templ");
        message.setDatacenterInfo("dc");
        message.setStorageDomainInfo("storage");
        Calendar cal = Calendar.getInstance();
        cal.set(2022 , Calendar.DECEMBER, 31, 23, 59, 59);
        message.setLogTime(cal.getTime());
        message.setSeverity(AuditLogSeverity.WARNING);
        message.setMessage("message");

    }

    @Test
    public void testForEnglish() {
        Locale locale = Locale.ENGLISH;
        String subject = LocalizedMessageHelper.prepareMessageSubject(AuditLogEventType.alertMessage, "localhost", message.getMessage(), locale);
        assertEquals("alertMessage (localhost), [message]", subject);

        String plainBody = LocalizedMessageHelper.prepareMessageBody(message, locale);
        assertEquals("Time: Dec 31, 2022, 11:59:59\u202fPM\n" +
                "Message: message\n" +
                "Severity: WARNING\n" +
                "User Name: user@user.com\n" +
                "VM Name: vm01\n" +
                "Host Name: host\n" +
                "Template Name: templ\n" +
                "Data Center Name: dc\n" +
                "Storage Domain Name: storage\n", plainBody);

        String htmlBody = LocalizedMessageHelper.prepareHTMLMessageBody(message, locale);
        assertEquals("<b>Time:</b> Dec 31, 2022, 11:59:59\u202fPM<br>" +
                "<b>Message:</b> message<br>" +
                "<b>Severity:</b> WARNING<br>" +
                "<b>User Name:</b> user@user.com<br>" +
                "<b>VM Name:</b> vm01<br>" +
                "<b>Host Name:</b> host<br>" +
                "<b>Template Name:</b> templ<br>" +
                "<b>Data Center Name:</b> dc<br>" +
                "<b>Storage Domain Name:</b> storage<br>", htmlBody);
    }

    @Test
    public void testForNonTranslatedLanguage() {
        Locale locale = Locale.forLanguageTag("fr-FR");
        String subject = LocalizedMessageHelper.prepareMessageSubject(AuditLogEventType.alertMessage, "localhost", message.getMessage(), locale);
        assertEquals("alertMessage (localhost), [message]", subject);

        String plainBody = LocalizedMessageHelper.prepareMessageBody(message, locale);
        assertEquals("Time: 31 déc. 2022, 23:59:59\n" +
                "Message: message\n" +
                "Severity: WARNING\n" +
                "User Name: user@user.com\n" +
                "VM Name: vm01\n" +
                "Host Name: host\n" +
                "Template Name: templ\n" +
                "Data Center Name: dc\n" +
                "Storage Domain Name: storage\n", plainBody);

        String htmlBody = LocalizedMessageHelper.prepareHTMLMessageBody(message, locale);
        assertEquals("<b>Time:</b> 31 déc. 2022, 23:59:59<br>" +
                "<b>Message:</b> message<br>" +
                "<b>Severity:</b> WARNING<br>" +
                "<b>User Name:</b> user@user.com<br>" +
                "<b>VM Name:</b> vm01<br>" +
                "<b>Host Name:</b> host<br>" +
                "<b>Template Name:</b> templ<br>" +
                "<b>Data Center Name:</b> dc<br>" +
                "<b>Storage Domain Name:</b> storage<br>", htmlBody);
    }

    @Test
    public void testForNotDefaultLanguage() {
        Locale locale = Locale.forLanguageTag("ru-RU");
        String subject = LocalizedMessageHelper.prepareMessageSubject(AuditLogEventType.alertMessage, "localhost", message.getMessage(), locale);
        assertEquals("Оповещение (localhost), [message]", subject);

        String plainBody = LocalizedMessageHelper.prepareMessageBody(message, locale);
        assertEquals("Время: 31 дек. 2022\u202fг., 23:59:59\n" +
                "Сообщение: message\n" +
                "Уровень оповещения: ПРЕДУПРЕЖДЕНИЕ\n" +
                "Пользователь: user@user.com\n" +
                "ВМ: vm01\n" +
                "Хост: host\n" +
                "Шаблон: templ\n" +
                "Датацентр: dc\n" +
                "Устройство хранения: storage\n", plainBody);

        String htmlBody = LocalizedMessageHelper.prepareHTMLMessageBody(message, locale);
        assertEquals("<b>Время:</b> 31 дек. 2022\u202fг., 23:59:59<br>" +
                "<b>Сообщение:</b> message<br>" +
                "<b>Уровень оповещения:</b> ПРЕДУПРЕЖДЕНИЕ<br>" +
                "<b>Пользователь:</b> user@user.com<br>" +
                "<b>ВМ:</b> vm01<br>" +
                "<b>Хост:</b> host<br>" +
                "<b>Шаблон:</b> templ<br>" +
                "<b>Датацентр:</b> dc<br>" +
                "<b>Устройство хранения:</b> storage<br>", htmlBody);
    }
}
