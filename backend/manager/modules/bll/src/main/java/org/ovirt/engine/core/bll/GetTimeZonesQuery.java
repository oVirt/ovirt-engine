package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.queries.TimeZoneQueryParams;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SysprepHandler;

public class GetTimeZonesQuery<P extends TimeZoneQueryParams> extends QueriesCommandBase<P> {
    private static Map<String, String> windowsTimezones = new HashMap<String, String>();
    private static Map<String, String> javaTimezones =
            new LinkedHashMap<String, String>(TimeZone.getAvailableIDs().length);

    static {
        initWindowsTimeZones();
        initJavaTimeZones();
    }

    private static void initWindowsTimeZones() {
        // get all time zones that is supported by sysprep

        // This is a bit of a hack since Java doesn't use the same timezone
        // standard as Windows
        // Or actually Windows doesn't use the standard that everybody else is
        // using (surprise...)
        // Since this is only used to present to user the list windows timezones
        // We can safely return the list of timezones that are supported by
        // sysprep handler and be done with it
        for (String value : SysprepHandler.timeZoneIndex.keySet()) {
            // we use:
            // key = "Afghanistan Standard Time"
            // value = "(GMT+04:30) Afghanistan Standard Time"
            String key = SysprepHandler.getTimezoneKey(value);
            windowsTimezones.put(key, value);
        }
        windowsTimezones = sortMapByValue(windowsTimezones);
    }

    private static void initJavaTimeZones() {
        for (String id : TimeZone.getAvailableIDs()) {
            TimeZone timeZone = TimeZone.getTimeZone(id);
            String displayName = beautifyTZDisplayName(timeZone);
            if (!javaTimezones.containsValue(displayName)) {
                javaTimezones.put(timeZone.getID(), displayName);
            }
        }
    }

    /**
     *
     * @param timeZone
     * @return the offset in <code> [hh:mm] DISPLAY_NAME </code> style. e.g. <blockquote> [02:00] Eastern European Time
     *         <blockquote>
     */
    private static String beautifyTZDisplayName(TimeZone timeZone) {
        StringBuilder sb = new StringBuilder();
        long offsetInMinutes = TimeUnit.MILLISECONDS.toMinutes(timeZone.getRawOffset());
        sb.append(" (GMT");
        sb.append(offsetInMinutes >= 0 ? "+" : "-");
        sb.append(String.format("%02d", Math.abs(offsetInMinutes) / 60));
        sb.append(":");
        sb.append(String.format("%02d", Math.abs(offsetInMinutes) % 60));
        sb.append(") ");
        sb.append(timeZone.getDisplayName());

        return sb.toString();
    }

    public GetTimeZonesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        switch (getParameters().getTimeZoneType()) {
        case GENERAL_TIMEZONE:
            getQueryReturnValue().setReturnValue(javaTimezones);
            break;
        case WINDOWS_TIMEZONE:
            getQueryReturnValue().setReturnValue(windowsTimezones);
            break;
        }
    }

    private static Map<String, String> sortMapByValue(Map<String, String> map) {
        List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>(map.entrySet());
        Collections.sort(list, TimeZoneTimeExtractComperator.instance);

        Map<String, String> result = new LinkedHashMap<String, String>();
        for (Iterator<Map.Entry<String, String>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static final class TimeZoneTimeExtractComperator implements Comparator<Map.Entry<String, String>> {
        public static final TimeZoneTimeExtractComperator instance = new TimeZoneTimeExtractComperator();

        private static final Pattern regex = Pattern.compile(SysprepHandler.TimzeZoneExtractTimePattern);

        // we get a string like "(GMT-04:30) Afghanistan Standard Time"
        // we use regex to extract the time only and replace it to number
        // in this sample we get -430
        @Override
        public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
            int a = 0, b = 0;
            Matcher match1 = regex.matcher(o1.toString());
            Matcher match2 = regex.matcher(o2.toString());
            if (match1.matches() && match1.groupCount() > 0) {
                a = Integer.parseInt(match1.group(1).substring(3).replace(":", "").replace("+", ""));
            }
            if (match2.matches() && match2.groupCount() > 0) {
                b = Integer.parseInt(match2.group(1).substring(3).replace(":", "").replace("+", ""));
            }

            if (a == b) {
                return 0;
            }

            return (a > b) ? 1 : -1;
        }
    }
}
