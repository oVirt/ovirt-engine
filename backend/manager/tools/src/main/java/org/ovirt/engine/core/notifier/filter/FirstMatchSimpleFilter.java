package org.ovirt.engine.core.notifier.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.notifier.transport.Transport;

public class FirstMatchSimpleFilter {

    private static final Pattern PATTERN_BLANK = Pattern.compile("\\s*");
    static final private Pattern PATTERN_PARSE = Pattern.compile(
            "\\s*" +
            "((?<include>include)|(?<exclude>exclude))" +
            ":" +
            "((?<anymsg>\\*)|(?<message>\\w+))" +
            "(?<recipient>" +
                "\\(" +
                    "(" +
                        "(?<anyrecipient>\\*)|" +
                        "(?<transport>\\w+):(?<name>[^)]*)" +
                    ")" +
                "\\)" +
            ")?" +
            "\\s*" +
            ""
        );
    private static final Logger log = Logger.getLogger(FirstMatchSimpleFilter.class);
    private Map<String, Transport> transports = new HashMap<>();
    private List<FilterEntry> notify = new LinkedList<>();
    private Set<Recipient> recipients = new HashSet<>();

    public void registerTransport(Transport transport) {
        transports.put(transport.getName(), transport);
    }

    public void unregisterTransport(Transport transport) {
        transports.remove(transport.getName());
    }

    private void addRecipient(Recipient recipient) {
        recipients.add(recipient);
    }

    public void addFilterEntries(List<FilterEntry> entries) {
        for (FilterEntry entry : entries) {
            log.debug(String.format("addFilterEntry: %s", entry));
            notify.add(entry);
            if (entry.getRecipient() != null) {
                addRecipient(entry.getRecipient());
            }
        }
    }

    public void clearFilterEntries() {
        notify.clear();
        recipients.clear();
    }

    public void processEvent(AuditLogEvent event) {
        log.debug(String.format("Event: %s", event.getName()));
        for (Recipient recipient : recipients) {
            log.debug(String.format("Recipient: %s", recipient));
            for (FilterEntry entry : notify) {
                if ((
                        entry.getEventName() == null ||
                        entry.getEventName().equals(event.getName())
                        ) &&
                        (
                        entry.getRecipient() == null ||
                        entry.getRecipient().equals(recipient)
                        )) {
                    log.debug(String.format("Entry match((%s)): %s", entry.isExclude() ? "exclude" : "include", entry));
                    if (!entry.isExclude()) {
                        Transport transport = transports.get(recipient.getTransport());
                        if (transport == null) {
                            log.debug(String.format("Ignoring recipient '%s' as transport not registered", recipient));
                        }
                        else {
                            transport.dispatchEvent(event, recipient.getName());
                        }
                    }
                    break;
                }
            }
        }
    }

    public static List<FilterEntry> parse(String filters) {
        List<FilterEntry> ret = new LinkedList<>();
        if (!PATTERN_BLANK.matcher(filters).matches()) {
            Matcher m = PATTERN_PARSE.matcher(filters);
            boolean ok = false;
            int expectedStart = 0;
            while (m.find()) {
                log.debug(String.format("parse: handling '%s'", m.group(0)));

                if (m.start() != expectedStart) {
                    throw new RuntimeException("Cannot parse filters");
                }
                expectedStart = m.end();
                ok = m.end() == m.regionEnd();

                ret.add(
                        new FilterEntry(
                                m.group("anymsg") != null ? null : m.group("message"),
                                m.group("exclude") != null,
                                m.group("recipient") == null || m.group("anyrecipient") != null ? null : new Recipient(
                                        m.group("transport"),
                                        m.group("name")
                                )
                        )
                        );
            }
            if (!ok) {
                throw new IllegalArgumentException("Cannot parse filters");
            }
        }
        return ret;
    }

    public static class Recipient {

        private final String transport;

        private final String name;

        public Recipient(String transport, String name) {
            this.transport = transport;
            this.name = name;
        }

        public String getTransport() {
            return transport;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null ||
                    !this.getClass().equals(obj.getClass())) {
                return false;
            }

            Recipient that = (Recipient) obj;
            return (StringUtils.equals(this.transport, that.transport) && StringUtils.equals(this.name, that.name));
        }

        @Override
        public int hashCode() {
            int result = transport != null ? transport.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Recipient{" +
                    "transport='" + transport + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static class FilterEntry {

        private final String eventName;

        private final boolean exclude;

        private final Recipient recipient;

        public FilterEntry(String eventName, boolean exclude, Recipient recipient) {
            this.eventName = eventName;
            this.exclude = exclude;
            this.recipient = recipient;
        }

        public FilterEntry(String eventName, boolean exclude, String transport, String name) {
            this(eventName, exclude, new Recipient(transport, name));
        }

        public String getEventName() {
            return eventName;
        }

        public boolean isExclude() {
            return exclude;
        }

        public Recipient getRecipient() {
            return recipient;
        }

        @Override
        public String toString() {
            return "FilterEntry{" +
                    "eventName='" + eventName + '\'' +
                    ", exclude=" + exclude +
                    ", recipient=" + recipient +
                    '}';
        }

    }
}
