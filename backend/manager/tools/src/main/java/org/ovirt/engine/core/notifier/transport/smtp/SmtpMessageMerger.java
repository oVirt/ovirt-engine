package org.ovirt.engine.core.notifier.transport.smtp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.notifier.dao.DispatchResult;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;
import org.ovirt.engine.core.notifier.transport.Observable;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

/**
 * Helper class to merge different events of the same type ({@link AuditLogEvent#getLogTypeName()}) into one e-mail to
 * reduce the number of similar e-mails. Also, it helps to work with the merged e-mails.
 * In order to configure this behavior, the following properties should be provided:
 * <ul>
 * <li><code>MAIL_MERGE_LOG_TYPES</code> comma-separated list of log type names {@link AuditLogEvent#getLogTypeName()}
 * that could be merged (e.g. ENGINE_BACKUP_STARTED, ENGINE_BACKUP_FAILED, ENGINE_BACKUP_COMPLETED).</li>
 * </ul>
 *
 * The following properties are optional:
 * <ul>
 * <li><code>MAIL_MERGE_MAX_TIME_DIFFERENCE</code> maximum allowed log time ({@link AuditLogEvent#getLogTime()})
 * difference (milliseconds) for two events to be merged. If two events have difference in log time greater than
 * specified by the parameter then the events will not be merged. Default value is 5000.</li>
 * </ul>
 */
class SmtpMessageMerger {
    private static final String MAIL_MERGE_LOG_TYPES_PROPERTY = "MAIL_MERGE_LOG_TYPES";
    private static final String MAIL_MERGE_MAX_TIME_DIFFERENCE_PROPERTY = "MAIL_MERGE_MAX_TIME_DIFFERENCE";
    private static final String MAIL_MERGE_LOG_TYPES_SEPARATOR = ",";
    private static final long MAIL_MERGE_MAX_TIME_DIFFERENCE_DEFAULT = 5000L;

    private final Set<String> mergeLogTypes = new HashSet<>();
    private final long maxTimeDifference;

    SmtpMessageMerger(NotificationProperties props) {
        String mergeLogTypesProperty = props.getProperty(MAIL_MERGE_LOG_TYPES_PROPERTY, true);
        if (!StringUtils.isEmpty(mergeLogTypesProperty)) {
            mergeLogTypes.addAll(Arrays.stream(mergeLogTypesProperty.split(MAIL_MERGE_LOG_TYPES_SEPARATOR))
                    .map(String::trim)
                    .filter(type -> !type.isEmpty())
                    .map(String::toUpperCase)
                    .collect(Collectors.toList()));
        }
        maxTimeDifference = props.getLong(
                MAIL_MERGE_MAX_TIME_DIFFERENCE_PROPERTY, MAIL_MERGE_MAX_TIME_DIFFERENCE_DEFAULT);
    }

    /**
     * Prepare e-mail (subject and body) content for the merged event.
     * @param hostName   the host name on which the subject will refer to
     * @param attempt    associated attempt which the message will be created by
     * @param isBodyHtml defines the format of message body
     * @param locale     locale for the message content
     * @return e-mail content built by the event.
     */
    EventMessageContent prepareEMailMessageContent(String hostName, Smtp.DispatchAttempt attempt, boolean isBodyHtml, Locale locale) {
        EventMessageContent message = prepareEMailMessageContent(hostName, attempt.event, isBodyHtml, locale);
        if (!attempt.merged.isEmpty()) {
            List<EventMessageContent> mergedMessages = attempt.merged.stream()
                    .map(event -> prepareEMailMessageContent(hostName, event, isBodyHtml, locale))
                    .collect(Collectors.toList());
            //If the message consists of multiple merged messages then we need to define a subject of the resulting
            // e-mail by the following algorithm:
            //1. We walk through all subjects and define common prefix and common suffix of the subject across all
            // messages.
            //2. Then the resulting subject would be a concatenation: common prefix + <comma-separated list of middle
            // parts of all messages> + common suffix.
            String subject = message.getMessageSubject();
            String commonPrefix = null;
            String commonSuffix = null;
            for (EventMessageContent mergedMessage : mergedMessages) {
                String anotherSubject = mergedMessage.getMessageSubject();
                String prefix = defineCommonPrefix(subject, anotherSubject);
                String suffix = defineCommonSuffix(subject, anotherSubject);
                commonPrefix = defineShorterString(commonPrefix, prefix);
                commonSuffix = defineShorterString(commonSuffix, suffix);
            }
            int pr = commonPrefix.length();
            int sx = commonSuffix.length();
            StringBuilder mergedSubject = new StringBuilder(commonPrefix)
                    .append(subject.substring(pr, subject.length() - sx));
            for (EventMessageContent mergedMessage : mergedMessages) {
                String anotherSubject = mergedMessage.getMessageSubject();
                mergedSubject.append(", ")
                        .append(anotherSubject.substring(pr, anotherSubject.length() - sx).trim());
            }
            mergedSubject.append(commonSuffix);

            //If the message consists of multiple merged messages then the body of the resulting message will be just a
            // concatenation of bodies of all messages divided by a separator.
            StringBuilder mergedBody = new StringBuilder(message.getMessageBody());
            for (EventMessageContent mergedMessage : mergedMessages) {
                if (isBodyHtml) {
                    mergedBody.append("<br><br>==========================<br><br>");
                } else {
                    mergedBody.append("\n==========================\n\n");
                }
                mergedBody.append(mergedMessage.getMessageBody());
            }

            message = new EventMessageContent(mergedSubject.toString(), mergedBody.toString());
        }
        return message;
    }

    private static String defineCommonPrefix(String str1, String str2) {
        int size = Math.min(str1.length(), str2.length());
        for (int i = 0; i < size; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                return str1.substring(0, i);
            }
        }
        return str1.substring(0, size);
    }

    private static String defineCommonSuffix(String str1, String str2) {
        int l1 = str1.length();
        int l2 = str2.length();
        int size = Math.min(l1, l2);
        for (int i = 1; i <= size; i++) {
            if (str1.charAt(l1 - i) != str2.charAt(l2 - i)) {
                return str1.substring(l1 - i + 1);
            }
        }
        return str1.substring(0, size);
    }

    private static String defineShorterString(String initialStr, String newStr) {
        return initialStr == null ? newStr : (initialStr.length() < newStr.length() ? initialStr : newStr);
    }

    private EventMessageContent prepareEMailMessageContent(String hostName, AuditLogEvent event, boolean isBodyHtml, Locale locale) {
        EventMessageContent message = new EventMessageContent();
        message.prepareMessage(hostName, event, isBodyHtml, locale);
        return message;
    }

    /**
     * Notify listeners about successful e-mail sending.
     * @param observable observable object with the listeners to notify.
     * @param attempt    e-mail that was sent successfully.
     */
    void notifyAboutSuccess(Observable observable, Smtp.DispatchAttempt attempt) {
        observable.notifyObservers(DispatchResult.success(
                attempt.event, attempt.address, EventNotificationMethod.SMTP));
        attempt.merged.forEach(event -> observable.notifyObservers(DispatchResult.success(
                event, attempt.address, EventNotificationMethod.SMTP)));
    }

    /**
     * Notify listeners about failed e-mail sending.
     * @param observable observable object with the listeners to notify.
     * @param attempt    e-mail that was failed to be sent.
     * @param message    description of the failure.
     */
    void notifyAboutFailure(Observable observable, Smtp.DispatchAttempt attempt, String message) {
        observable.notifyObservers(DispatchResult.failure(
                attempt.event, attempt.address, EventNotificationMethod.SMTP, message));
        attempt.merged.forEach(event -> observable.notifyObservers(DispatchResult.failure(
                event, attempt.address, EventNotificationMethod.SMTP, message)));
    }

    /**
     * Walk through all events and merge similar events into one (to send fewer e-mails)
     * @param events list of events. Could be modified (all events merged into another events would be removed from the
     *               list).
     */
    void mergeSimilarEvents(Collection<Smtp.DispatchAttempt> events) {
        List<Smtp.DispatchAttempt> previous = new ArrayList<>();
        for (Iterator<Smtp.DispatchAttempt> iterator = events.iterator(); iterator.hasNext();) {
            Smtp.DispatchAttempt attempt = iterator.next();
            if (previous.isEmpty()) {
                previous.add(attempt);
            } else {
                boolean merged = false;
                for (Smtp.DispatchAttempt p : previous) {
                    if (mergeIfSimilar(p, attempt)) {
                        iterator.remove();
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    previous.add(attempt);
                }
            }
        }
    }

    /**
     * Merge two e-mails into one. The e-mails will be merged if and only if:
     * <ul>
     *     <li>they have exactly the same type ({@link AuditLogEvent#getLogTypeName()});</li>
     *     <li>the type was mentioned in the MAIL_MERGE_LOG_TYPES configuration property;</li>
     *     <li>time difference between the event log time ({@link AuditLogEvent#getLogTime()}) is less than specified
     *         in the MAIL_MERGE_MAX_TIME_DIFFERENCE configuration property.</li>
     * </ul>
     * @param main  e-mail that would contain result of the merge.
     * @param other e-mail that would be merged into the main e-mail.
     * @return <code>true</code> if the specified e-mails were merged.
     */
    private boolean mergeIfSimilar(Smtp.DispatchAttempt main, Smtp.DispatchAttempt other) {
        if (!Objects.equals(main.address, other.address)) {
            return false;
        }
        AuditLogEvent mainEvent = main.event;
        AuditLogEvent otherEvent = other.event;
        if (!Objects.equals(mainEvent.getLogTypeName(), otherEvent.getLogTypeName())) {
            return false;
        }

        if (!mergeLogTypes.contains(mainEvent.getLogTypeName())) {
            return false;
        }

        long logTime = Optional.ofNullable(main.event.getLogTime()).map(Date::getTime).orElse(0L);
        long otherLogTime = Optional.ofNullable(other.event.getLogTime()).map(Date::getTime).orElse(0L);
        if (Math.abs(logTime - otherLogTime) > maxTimeDifference) {
            return false;
        }

        main.merged.add(other.event);
        main.merged.addAll(other.merged);

        return true;
    }
}
