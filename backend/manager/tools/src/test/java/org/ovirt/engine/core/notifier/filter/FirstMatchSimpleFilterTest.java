package org.ovirt.engine.core.notifier.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.notifier.dao.DispatchResult;
import org.ovirt.engine.core.notifier.transport.Observer;
import org.ovirt.engine.core.notifier.transport.Transport;

public class FirstMatchSimpleFilterTest {

    FirstMatchSimpleFilter filter;

    T snmp;

    T smtp;

    /*
     * This replaces the application message representation; the only requests we have are getName() to return the
     * name based on which we filter, and getSeverity() used only in filters where the severity is specified.
     */
    private static class E extends AuditLogEvent {
        private String e;
        private AuditLogSeverity s;

        public E(String e) {
            this(e, null);
        }

        public E(String e, AuditLogSeverity s) {
            this.e = e;
            this.s = s;
        }

        @Override
        public String getName() {
            return e;
        }

        @Override
        public AuditLogSeverity getSeverity() {
            return s;
        }
    }

    private static class T extends Transport {

        private String t;

        private List<String> events = new ArrayList<>();

        public T(String t) {
            this.t = t;
        }

        @Override
        public void dispatchEvent(AuditLogEvent event, String address) {
            events.add(event.getName() + "-->" + address);
        }

        public List<String> getEvents() {
            return events;
        }

        @Override
        public String getName() {
            return t;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void notifyObservers(DispatchResult data) {

        }

        @Override
        public void registerObserver(Observer observer) {

        }

        @Override
        public void removeObserver(Observer observer) {

        }
    }

    @BeforeEach
    public void setUp() {
        filter = new FirstMatchSimpleFilter();
        /*
         * Here we register two transports into the filter logic.
         *
         * All we need from a transport is to know its name and able to dispatch messages into it.
         *
         * transports have nothing to do with application, once initialized.
         */
        snmp = new T("snmp");
        smtp = new T("smtp");
        filter.registerTransport(snmp);
        filter.registerTransport(smtp);
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    public void testEmptyFilter() {
        filter.clearFilterEntries();
        filter.processEvent(new E("message0"));
        filter.processEvent(new E("message1"));
        assertTrue(snmp.getEvents().isEmpty());
    }

    @Test
    public void testConfigurationEntries() {
        filter.clearFilterEntries();
        filter.addFilterEntries(
                Collections.singletonList(
                        new FirstMatchSimpleFilter.FilterEntry("message0", null, false, "smtp", "dbtest1@redhat.com"))
                );
        filter.processEvent(new E("message0"));
        filter.processEvent(new E("message1"));
        assertTrue(smtp.getEvents().contains("message0-->dbtest1@redhat.com"));
    }

    @Test
    public void testSimpleParse() {
        filter.clearFilterEntries();
        filter.addFilterEntries(
                FirstMatchSimpleFilter.parse("include:VDC_STOP(snmp:) " +
                        "exclude:*")
                );
        filter.processEvent(new E("VDC_STOP"));
        assertTrue(snmp.getEvents().contains("VDC_STOP-->"));
    }

    @Test
    public void testIncludeExcludeOrder() {
        String expected = "alonbl@redhat.com";
        filter.clearFilterEntries();
        filter.addFilterEntries(
                FirstMatchSimpleFilter.parse(
                        "" +
                                "include:message1(smtp:" + expected + ") " +
                                "include:message2(smtp:" + expected + ") " +
                                "exclude:message3(smtp:" + expected + ") " +
                                "exclude:message1(smtp:" + expected + ") " +
                                "include:*"
                        ));
        filter.processEvent(new E("message1"));
        filter.processEvent(new E("message2"));
        filter.processEvent(new E("message3"));
        assertTrue(smtp.getEvents().contains("message1-->" + expected));
        assertTrue(smtp.getEvents().contains("message2-->" + expected));
        assertEquals(2, smtp.getEvents().size());
    }

    @Test
    public void testSeverity() {
        String expected1 = "test1@example.com";
        String expected2 = "test2@example.com";
        filter.clearFilterEntries();
        filter.addFilterEntries(
                FirstMatchSimpleFilter.parse(
                        "include:*:WARNING(smtp:" + expected1 + ") " +
                        "exclude:*(smtp:" + expected1 + ")" +
                        "exclude:*:WARNING(smtp:" + expected2 + ") " +
                        "include:*(smtp:" + expected2 + ")"
                ));
        filter.processEvent(new E("message1", AuditLogSeverity.NORMAL));
        filter.processEvent(new E("message2", AuditLogSeverity.WARNING));
        filter.processEvent(new E("message3", AuditLogSeverity.ERROR));
        filter.processEvent(new E("message4", AuditLogSeverity.ALERT));
        assertTrue(smtp.getEvents().contains("message2-->" + expected1));
        assertTrue(smtp.getEvents().contains("message3-->" + expected1));
        assertTrue(smtp.getEvents().contains("message3-->" + expected2));
        assertTrue(smtp.getEvents().contains("message4-->" + expected1));
        assertTrue(smtp.getEvents().contains("message4-->" + expected2));
        assertEquals(5, smtp.getEvents().size());
    }

    @Test
    public void testSeverityAndEventCombo() {
        // These combinations aren't useful in the real world, but we want them to work anyway
        String expected = "test@example.com";
        filter.clearFilterEntries();
        filter.addFilterEntries(
                FirstMatchSimpleFilter.parse(
                        "include:normal_message:ERROR(smtp:" + expected + ") " +
                        "include:error_message:NORMAL(smtp:" + expected + ") " +
                        "exclude:*"
                ));
        filter.processEvent(new E("normal_message", AuditLogSeverity.NORMAL));
        filter.processEvent(new E("error_message", AuditLogSeverity.ERROR));
        assertTrue(smtp.getEvents().contains("error_message-->" + expected));
        assertEquals(1, smtp.getEvents().size());
    }

    @Test
    public void testAll() {
        filter.clearFilterEntries();
        filter.addFilterEntries(Collections.singletonList(
                new FirstMatchSimpleFilter.FilterEntry("kuku", null, false, "snmp", "pupu"))
                );
        filter.addFilterEntries(Collections.singletonList(
                new FirstMatchSimpleFilter.FilterEntry("kuku", null, false, "smtp", "pupu"))
                );
        filter.addFilterEntries(
                FirstMatchSimpleFilter.parse(
                        "" +
                                "include:*"
                        ));
        filter.processEvent(new E("message1"));
        assertTrue(snmp.getEvents().contains("message1-->pupu"));
        assertTrue(smtp.getEvents().contains("message1-->pupu"));
    }

    @Test
    public void testFilter() {
        filter.clearFilterEntries();
        /*
         * Add configuration filter
         */
        filter.addFilterEntries(
                FirstMatchSimpleFilter.parse(
                        "include:message1(smtp:alonbl@redhat.com) " +
                                "include:message2(smtp:alonbl@redhat.com) " +
                                "exclude:message3(smtp:alonbl@redhat.com) " +
                                "exclude:message1(smtp:alonbl@redhat.com) " +
                                "include:message2(smtp:alon.barlev@gmail.com) " +
                                "include:message1(smtp:alonbl@gentoo.org) " +
                                "exclude:message1(snmp:profile1) " +
                                "exclude:message2(snmp:profile1) " +
                                "include:*(snmp:profile2) " +
                                "include:*(snmp:profile1) " +
                                "exclude:*" +
                                ""
                        ));

        filter.processEvent(new E("message0"));
        filter.processEvent(new E("message1"));
        filter.processEvent(new E("message2"));

        assertTrue(snmp.getEvents().contains("message0-->profile1"));
        assertTrue(snmp.getEvents().contains("message0-->profile2"));
        assertTrue(snmp.getEvents().contains("message1-->profile2"));
        assertTrue(snmp.getEvents().contains("message2-->profile2"));
        assertEquals(4, snmp.getEvents().size());

        assertTrue(smtp.getEvents().contains("message1-->alonbl@gentoo.org"));
        assertTrue(smtp.getEvents().contains("message1-->alonbl@redhat.com"));
        assertTrue(smtp.getEvents().contains("message2-->alon.barlev@gmail.com"));
        assertTrue(smtp.getEvents().contains("message2-->alonbl@redhat.com"));
        assertEquals(4, smtp.getEvents().size());
    }

    @Test
    public void testParsePositive() {
        // Should parse
        FirstMatchSimpleFilter.parse("include:message(kuku:pupu) include:message(kuku:pupu)");
    }

    @Test
    public void testParseNegative1() {
        // No event
        assertThrows(IllegalArgumentException.class, () -> FirstMatchSimpleFilter.parse("include:(kuku:pupu)"));
    }

    @Test
    public void testParseNegative2() {
        // No Transport
        assertThrows(IllegalArgumentException.class, () -> FirstMatchSimpleFilter.parse("include:message(:pupu)"));
    }

    @Test
    public void testParseNegative3() {
        // Random text
        assertThrows(IllegalArgumentException.class, () -> FirstMatchSimpleFilter.parse("lorem ipsum"));
    }

    @Test
    public void testParseNegative4() {
        // Invalid severity
        assertThrows(IllegalArgumentException.class,
                () -> FirstMatchSimpleFilter.parse("include:message:_badSeverityTest_(kuku:pupu)"));
    }
}
