package org.ovirt.engine.core.notifier.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setUp() throws Exception {
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

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testEmptyFilter() throws Exception {
        filter.clearFilterEntries();
        filter.processEvent(new E("message0"));
        filter.processEvent(new E("message1"));
        Assert.assertTrue(snmp.getEvents().isEmpty());
    }

    @Test
    public void testConfigurationEntries() throws Exception {
        filter.clearFilterEntries();
        filter.addFilterEntries(
                Collections.singletonList(
                        new FirstMatchSimpleFilter.FilterEntry("message0", null, false, "smtp", "dbtest1@redhat.com"))
                );
        filter.processEvent(new E("message0"));
        filter.processEvent(new E("message1"));
        Assert.assertTrue(smtp.getEvents().contains("message0-->dbtest1@redhat.com"));
    }

    @Test
    public void testSimpleParse() throws Exception {
        filter.clearFilterEntries();
        filter.addFilterEntries(
                FirstMatchSimpleFilter.parse("include:VDC_STOP(snmp:) " +
                        "exclude:*")
                );
        filter.processEvent(new E("VDC_STOP"));
        Assert.assertTrue(snmp.getEvents().contains("VDC_STOP-->"));
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
        Assert.assertTrue(smtp.getEvents().contains("message1-->" + expected));
        Assert.assertTrue(smtp.getEvents().contains("message2-->" + expected));
        Assert.assertEquals(2, smtp.getEvents().size());
    }

    @Test
    public void testSeverity() throws Exception {
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
        Assert.assertTrue(smtp.getEvents().contains("message2-->" + expected1));
        Assert.assertTrue(smtp.getEvents().contains("message3-->" + expected1));
        Assert.assertTrue(smtp.getEvents().contains("message3-->" + expected2));
        Assert.assertTrue(smtp.getEvents().contains("message4-->" + expected1));
        Assert.assertTrue(smtp.getEvents().contains("message4-->" + expected2));
        Assert.assertEquals(5, smtp.getEvents().size());
    }

    @Test
    public void testSeverityAndEventCombo() throws Exception {
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
        Assert.assertTrue(smtp.getEvents().contains("error_message-->" + expected));
        Assert.assertEquals(1, smtp.getEvents().size());
    }

    @Test
    public void testAll() throws Exception {
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
        Assert.assertTrue(snmp.getEvents().contains("message1-->pupu"));
        Assert.assertTrue(smtp.getEvents().contains("message1-->pupu"));
    }

    @Test
    public void testFilter() throws Exception {
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

        Assert.assertTrue(snmp.getEvents().contains("message0-->profile1"));
        Assert.assertTrue(snmp.getEvents().contains("message0-->profile2"));
        Assert.assertTrue(snmp.getEvents().contains("message1-->profile2"));
        Assert.assertTrue(snmp.getEvents().contains("message2-->profile2"));
        Assert.assertEquals(4, snmp.getEvents().size());

        Assert.assertTrue(smtp.getEvents().contains("message1-->alonbl@gentoo.org"));
        Assert.assertTrue(smtp.getEvents().contains("message1-->alonbl@redhat.com"));
        Assert.assertTrue(smtp.getEvents().contains("message2-->alon.barlev@gmail.com"));
        Assert.assertTrue(smtp.getEvents().contains("message2-->alonbl@redhat.com"));
        Assert.assertEquals(4, smtp.getEvents().size());
    }

    @Test()
    public void testParsePositive() throws Exception {
        // Should parse
        FirstMatchSimpleFilter.parse("include:message(kuku:pupu) include:message(kuku:pupu)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNegative1() throws Exception {
        // No event
        FirstMatchSimpleFilter.parse("include:(kuku:pupu)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNegative2() throws Exception {
        // No Transport
        FirstMatchSimpleFilter.parse("include:message(:pupu)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNegative3() throws Exception {
        // Random text
        FirstMatchSimpleFilter.parse("lorem ipsum");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNegative4() throws Exception {
        // Invalid severity
        FirstMatchSimpleFilter.parse("include:message:_badSeverityTest_(kuku:pupu)");
    }
}
