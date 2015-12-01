package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.DateTime;

public class AuditLogConditionFieldAutoCompleterTest {

    @Before
    public void setUp() {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void testValidateFieldValueWithEnum() {
        IConditionFieldAutoCompleter comp = new AuditLogConditionFieldAutoCompleter();
        assertTrue("VDC_START", comp.validateFieldValue("TYPE", "1"));
        assertFalse("SOME_RANDOM_TEXT", comp.validateFieldValue("TYPE", "SOME_RANDOM_TEXT"));
    }

    @Test
    public void testValidateFieldValueWithTime() {
        IConditionFieldAutoCompleter comp = new AuditLogConditionFieldAutoCompleter();
        String dateString = "01/12/1972";
        assertTrue(dateString, comp.validateFieldValue("TIME", dateString));
        assertFalse("SOME_RANDOM_TEXT", comp.validateFieldValue("TIME", "SOME_RANDOM_TEXT"));
    }

    @Test
    public void testformatValueWithTime() {
        Pair<String, String> pair = new Pair<>();
        IConditionFieldAutoCompleter comp = new AuditLogConditionFieldAutoCompleter();
        Date date = new Date(72, 0, 12);
        String dateString = DateFormat.getDateInstance(DateFormat.SHORT).format(date);
        pair.setSecond(dateString);
        comp.formatValue("TIME", pair, false);
        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
        assertEquals(quote(fmt.format(date)), pair.getSecond());
        pair.setSecond("1");
        comp.formatValue("TIME", pair, false);
        // Try today
        // SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy" +
        // " 23:59:59 ");
        // Today begins at 00:00 - this is why we reset the DateTime object to midnight.
        DateTime dt = new DateTime(new Date());
        dt = dt.resetToMidnight();
        assertEquals(quote(fmt.format(dt)), pair.getSecond());
        // Try Yesterday
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        pair.setSecond("2");
        comp.formatValue("TIME", pair, false);
        // Yesterday (as any other day) begins at 00:00 - this is why we reset the DateTime object to midnight.

        dt = new DateTime(cal.getTime());
        dt = dt.resetToMidnight();
        assertEquals(quote(fmt.format(dt)), pair.getSecond());

        // Just going to test that this works
        pair.setSecond("Wednesday");
        comp.formatValue("TIME", pair, false);
        assertFalse("Day should be transformed to a date", pair.getSecond().equals("Wednesday"));
    }

    private String quote(String s) {
        return "'" + s + "'";
    }
}
