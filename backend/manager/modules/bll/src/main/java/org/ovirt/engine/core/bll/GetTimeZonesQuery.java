package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.queries.TimeZoneQueryParams;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SysprepHandler;

public class GetTimeZonesQuery<P extends TimeZoneQueryParams> extends QueriesCommandBase<P> {
    private static Map<String, String> windowsTimezones = new HashMap<String, String>();
    private static Map<String, String> linuxTimezones = new HashMap<String, String>();

    static {
        initWindowsTimeZones();
        initLinuxTimeZones();
    }

    public GetTimeZonesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        switch (getParameters().getTimeZoneType()) {
        case GENERAL_TIMEZONE:
            getQueryReturnValue().setReturnValue(linuxTimezones);
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

    /* Instead of returning nearly 600 time zone names shared between Java's
     * TimeZone.getAvailableIDs() and /usr/share/zoneinfo contents, pare down
     * the list to show zones similar to those supported by Sysprep.
     *
     * Values were converted to Olson names using the Unicode CLDR tables at
     * http://unicode.org/cldr/charts/supplemental/zone_tzid.html
     *
     * For a good overview, see http://stackoverflow.com/tags/timezone/info
     *
     * Note: some items marked with "Updated display name" if their names
     * differ from the names given in SysprepHandler.
     */
    private static void initLinuxTimeZones() {
        linuxTimezones.put("Asia/Kabul", "(GMT+04:30) Afghanistan Standard Time");
        linuxTimezones.put("America/Anchorage", "(GMT-09:00) Alaskan Standard Time");
        linuxTimezones.put("Asia/Riyadh", "(GMT+03:00) Arab Standard Time");
        linuxTimezones.put("Asia/Dubai", "(GMT+04:00) Arabian Standard Time");
        linuxTimezones.put("Asia/Baghdad", "(GMT+03:00) Arabic Standard Time");
        linuxTimezones.put("America/Halifax", "(GMT-04:00) Atlantic Standard Time");
        // linuxTimezones.put("Asia/Baku", "(GMT+04:00) Azerbaijan Standard Time"); // Not in Sysprep documentation
        linuxTimezones.put("Atlantic/Azores", "(GMT-10:00) Azores Standard Time");
        linuxTimezones.put("America/Regina", "(GMT-06:00) Canada Central Standard Time");
        linuxTimezones.put("Atlantic/Cape_Verde", "(GMT-01:00) Cape Verde Standard Time");
        linuxTimezones.put("Asia/Yerevan", "(GMT+04:00) Caucasus Standard Time");
        linuxTimezones.put("Australia/Adelaide", "(GMT+09:30) Cen. Australia Standard Time");
        linuxTimezones.put("America/Guatemala", "(GMT-06:00) Central America Standard Time");
        linuxTimezones.put("Asia/Almaty", "(GMT+06:00) Central Asia Standard Time");
        // linuxTimezones.put("America/Cuiaba", "(GMT-04:00) Central Brazilian Standard Time "); // Not in Sysprep documentation
        linuxTimezones.put("Europe/Budapest", "(GMT+01:00) Central Europe Standard Time");
        linuxTimezones.put("Europe/Warsaw", "(GMT+01:00) Central European Standard Time");
        linuxTimezones.put("Pacific/Guadalcanal", "(GMT+11:00) Central Pacific Standard Time");
        linuxTimezones.put("America/Chicago", "(GMT-06:00) Central Standard Time");
        linuxTimezones.put("America/Mexico_City", "(GMT-06:00) Central Standard Time (Mexico)");
        linuxTimezones.put("Asia/Shanghai", "(GMT+08:00) China Standard Time");
        linuxTimezones.put("Etc/GMT+12", "(GMT-12:00) Dateline Standard Time");
        linuxTimezones.put("Africa/Nairobi", "(GMT+03:00) E. Africa Standard Time");
        linuxTimezones.put("Australia/Brisbane", "(GMT+10:00) E. Australia Standard Time");
        linuxTimezones.put("Asia/Nicosia", "(GMT+02:00) E. Europe Standard Time");
        linuxTimezones.put("America/Sao_Paulo", "(GMT-03:00) E. South America Standard Time");
        linuxTimezones.put("America/New_York", "(GMT-05:00) Eastern Standard Time");
        linuxTimezones.put("Africa/Cairo", "(GMT+02:00) Egypt Standard Time");
        linuxTimezones.put("Asia/Yekaterinburg", "(GMT+05:00) Ekaterinburg Standard Time");
        linuxTimezones.put("Pacific/Fiji", "(GMT+12:00) Fiji Standard Time");
        linuxTimezones.put("Europe/Kiev", "(GMT+02:00) FLE Standard Time");
        linuxTimezones.put("Asia/Tbilisi", "(GMT+04:00) Georgian Standard Time");
        linuxTimezones.put("Europe/London", "(GMT+00:00) GMT Standard Time"); // Updated display name
        linuxTimezones.put("America/Godthab", "(GMT-03:00) Greenland Standard Time");
        linuxTimezones.put("Atlantic/Reykjavik", "(GMT) Greenwich Mean Time"); // Updated display name
        linuxTimezones.put("Europe/Bucharest", "(GMT+02:00) GTB Standard Time");
        linuxTimezones.put("Pacific/Honolulu", "(GMT-10:00) Hawaiian Standard Time");
        linuxTimezones.put("Asia/Calcutta", "(GMT+05:30) India Standard Time");
        linuxTimezones.put("Asia/Tehran", "(GMT+03:00) Iran Standard Time");
        linuxTimezones.put("Asia/Jerusalem", "(GMT+02:00) Israel Standard Time");
        linuxTimezones.put("Asia/Seoul", "(GMT+09:00) Korea Standard Time");
        // linuxTimezones.put("(GMT-02:00) Mid-Atlantic Standard Time", 75); // Not in Unicode CLDR list
        linuxTimezones.put("America/Denver", "(GMT-07:00) Mountain Standard Time");
        linuxTimezones.put("Asia/Rangoon", "(GMT+06:30) Myanmar Standard Time");
        linuxTimezones.put("Asia/Novosibirsk", "(GMT+06:00) N. Central Asia Standard Time");
        linuxTimezones.put("Asia/Katmandu", "(GMT+05:45) Nepal Standard Time");
        linuxTimezones.put("Pacific/Auckland", "(GMT+12:00) New Zealand Standard Time");
        linuxTimezones.put("America/St_Johns", "(GMT-03:30) Newfoundland Standard Time");
        linuxTimezones.put("Asia/Irkutsk", "(GMT+08:00) North Asia East Standard Time");
        linuxTimezones.put("Asia/Krasnoyarsk", "(GMT+07:00) North Asia Standard Time");
        linuxTimezones.put("America/Santiago", "(GMT+04:00) Pacific SA Standard Time");
        linuxTimezones.put("America/Los_Angeles", "(GMT-08:00) Pacific Standard Time");
        linuxTimezones.put("Europe/Paris", "(GMT+01:00) Romance Standard Time");
        linuxTimezones.put("Europe/Moscow", "(GMT+03:00) Russian Standard Time");
        linuxTimezones.put("America/Cayenne", "(GMT-03:00) SA Eastern Standard Time");
        linuxTimezones.put("America/Bogota", "(GMT-05:00) SA Pacific Standard Time");
        linuxTimezones.put("America/La_Paz", "(GMT-04:00) SA Western Standard Time");
        linuxTimezones.put("Pacific/Apia", "(GMT-11:00) Samoa Standard Time");
        linuxTimezones.put("Asia/Bangkok", "(GMT+07:00) SE Asia Standard Time");
        linuxTimezones.put("Asia/Singapore", "(GMT+08:00) Singapore Standard Time");
        linuxTimezones.put("Africa/Johannesburg", "(GMT+02:00) South Africa Standard Time");
        linuxTimezones.put("Asia/Colombo", "(GMT+06:00) Sri Lanka Standard Time");
        linuxTimezones.put("Asia/Taipei", "(GMT+08:00) Taipei Standard Time");
        linuxTimezones.put("Australia/Hobart", "(GMT+10:00) Tasmania Standard Time");
        linuxTimezones.put("Asia/Tokyo", "(GMT+09:00) Tokyo Standard Time");
        linuxTimezones.put("Pacific/Tongatapu", "(GMT+13:00) Tonga Standard Time");
        linuxTimezones.put("America/Indianapolis", "(GMT-05:00) US Eastern Standard Time (Indiana)"); // Updated display name
        linuxTimezones.put("America/Phoenix", "(GMT-07:00) US Mountain Standard Time (Arizona)"); // Updated display name
        linuxTimezones.put("Asia/Vladivostok", "(GMT+10:00) Vladivostok Standard Time");
        linuxTimezones.put("Australia/Perth", "(GMT+08:00) W. Australia Standard Time");
        linuxTimezones.put("Africa/Lagos", "(GMT+01:00) W. Central Africa Standard Time");
        linuxTimezones.put("Europe/Berlin", "(GMT+01:00) W. Europe Standard Time");
        linuxTimezones.put("Asia/Tashkent", "(GMT+05:00) West Asia Standard Time");
        linuxTimezones.put("Pacific/Port_Moresby", "(GMT+10:00) West Pacific Standard Time");
        linuxTimezones.put("Asia/Yakutsk", "(GMT+09:00) Yakutsk Standard Time");

        linuxTimezones = sortMapByValue(linuxTimezones);
    }
}
