package org.ovirt.engine.core.searchbackend;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;

import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.RefObject;

public class AuditLogConditionFieldAutoCompleterTest extends TestCase {

    public void testValidateFieldValueWithEnum() {
        IConditionFieldAutoCompleter comp = new AuditLogConditionFieldAutoCompleter();
        assertTrue("VDC_START", comp.validateFieldValue("TYPE", "1"));
        assertFalse("SOME_RANDOM_TEXT", comp.validateFieldValue("TYPE", "SOME_RANDOM_TEXT"));
    }

    public void testValidateFieldValueWithTime() {
        IConditionFieldAutoCompleter comp = new AuditLogConditionFieldAutoCompleter();
        String dateString = "01/12/1972";
        assertTrue(dateString, comp.validateFieldValue("TIME", dateString));
        assertFalse("SOME_RANDOM_TEXT", comp.validateFieldValue("TIME", "SOME_RANDOM_TEXT"));
    }

    public void testformatValueWithTime() {
        Locale.setDefault(Locale.US);
        RefObject<String> rels = new RefObject<String>();
        RefObject<String> value = new RefObject<String>();
        IConditionFieldAutoCompleter comp = new AuditLogConditionFieldAutoCompleter();
        Date date = new Date(72, 0, 12);
        String dateString = DateFormat.getDateInstance(DateFormat.SHORT).format(date);
        value.argvalue = dateString;
        comp.formatValue("TIME", rels, value, false);
        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
        assertEquals(quote(fmt.format(date)), value.argvalue);
        value.argvalue = "1";
        comp.formatValue("TIME", rels, value, false);
        // Try today
        // SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy" +
        // " 23:59:59 ");
        //Today begins at 00:00 - this is why we reset the DateTime object to midnight.
        DateTime dt = new DateTime(new Date());
        dt = dt.resetToMidnight();
        assertEquals(quote(fmt.format(dt)), value.argvalue);
        // Try Yesterday
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        value.argvalue = "2";
        comp.formatValue("TIME", rels, value, false);
        //Yesterday (as any other day) begins at 00:00 - this is why we reset the DateTime object to midnight.

        dt = new DateTime(cal.getTime());
        dt = dt.resetToMidnight();
        assertEquals(quote(fmt.format(dt)), value.argvalue);

        // Just going to test that this works
        value.argvalue = "Wednesday";
        comp.formatValue("TIME", rels, value, false);
        assertFalse("Day should be transformed to a date", value.argvalue.equals("Wednesday"));
    }

    private String quote(String s) {
        return "'" + s + "'";
    }
}
