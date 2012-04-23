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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SysprepHandler;

public class GetTimeZonesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    private static Map<String, String> timezones;
    private static Object o = new Object();

    public GetTimeZonesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // get all time zones that is supported by sysprep

        // This is a bit of a hack since Java doesn't use the same timezone
        // standard as Windows
        // Or actually Windows doesn't use the standard that everybody else is
        // using (suprise...)
        // Since this is only used to present to user the list windows timezones
        // We can safely return the list of timezones that are supported by
        // sysprep handler and be done with it
        synchronized (o) {
            if (timezones == null) {
                timezones = new HashMap<String, String>();
                for (String value : SysprepHandler.timeZoneIndex.keySet()) {
                    // we use:
                    // key = "Afghanistan Standard Time"
                    // value = "(GMT+04:30) Afghanistan Standard Time"
                    String key = SysprepHandler.getTimezoneKey(value);
                    timezones.put(key, value);
                }
                timezones = sortMapByValue(timezones);
            }
        }
        getQueryReturnValue().setReturnValue(timezones);
    }

    private static boolean IsSupportedBySysprep(TimeZone timeZone) {
        return SysprepHandler.timeZoneIndex.containsKey(timeZone.getID());
    }

    private Map sortMapByValue(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            private Pattern regex = Pattern.compile(SysprepHandler.TimzeZoneExtractTimePattern);

            // we get a string like "(GMT-04:30) Afghanistan Standard Time"
            // we use regex to extract the time only and replace it to number
            // in this sample we get -430
            @Override
            public int compare(Object o1, Object o2) {
                int a = 0, b = 0;
                Matcher match1 = regex.matcher(o1.toString());
                Matcher match2 = regex.matcher(o2.toString());
                if (match1.matches() && match1.groupCount() > 0) {
                    a = Integer.parseInt(match1.group(1).substring(3).replace(":", "").replace("+", ""));
                }
                if (match2.matches() && match2.groupCount() > 0) {
                    b = Integer.parseInt(match2.group(1).substring(3).replace(":", "").replace("+", ""));
                }
                return (a > b) ? 1 : 0;
            }
        });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
