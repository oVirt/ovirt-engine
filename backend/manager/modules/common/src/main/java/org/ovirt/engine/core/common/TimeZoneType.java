package org.ovirt.engine.core.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Match;
import org.ovirt.engine.core.compat.Regex;

public enum TimeZoneType {
    GENERAL_TIMEZONE(ConfigValues.DefaultGeneralTimeZone, ConfigurationValues.DefaultGeneralTimeZone, "Etc/GMT") {
        @Override
        protected Map<String, String> initializeTimeZoneList() {
            Map<String, String> generalTimeZones = new HashMap<>();
            generalTimeZones.put("Etc/GMT", "(GMT+00:00) GMT Standard Time");
            generalTimeZones.put("Asia/Kabul", "(GMT+04:30) Afghanistan Standard Time");
            generalTimeZones.put("America/Anchorage", "(GMT-09:00) Alaskan Standard Time");
            generalTimeZones.put("Asia/Riyadh", "(GMT+03:00) Arab Standard Time");
            generalTimeZones.put("Asia/Dubai", "(GMT+04:00) Arabian Standard Time");
            generalTimeZones.put("Asia/Baghdad", "(GMT+03:00) Arabic Standard Time");
            generalTimeZones.put("America/Halifax", "(GMT-04:00) Atlantic Standard Time");
            // generalTimeZones.put("Asia/Baku", "(GMT+04:00) Azerbaijan Standard Time"); // Not in Sysprep documentation
            generalTimeZones.put("Atlantic/Azores", "(GMT-10:00) Azores Standard Time");
            generalTimeZones.put("America/Regina", "(GMT-06:00) Canada Central Standard Time");
            generalTimeZones.put("Atlantic/Cape_Verde", "(GMT-01:00) Cape Verde Standard Time");
            generalTimeZones.put("Asia/Yerevan", "(GMT+04:00) Caucasus Standard Time");
            generalTimeZones.put("Australia/Adelaide", "(GMT+09:30) Cen. Australia Standard Time");
            generalTimeZones.put("Australia/Darwin", "(GMT+09:30) Cen. Australia Standard Time");
            generalTimeZones.put("America/Guatemala", "(GMT-06:00) Central America Standard Time");
            generalTimeZones.put("Asia/Almaty", "(GMT+06:00) Central Asia Standard Time");
            // generalTimeZones.put("America/Cuiaba", "(GMT-04:00) Central Brazilian Standard Time "); // Not in Sysprep documentation
            generalTimeZones.put("Europe/Budapest", "(GMT+01:00) Central Europe Standard Time");
            generalTimeZones.put("Europe/Warsaw", "(GMT+01:00) Central European Standard Time");
            generalTimeZones.put("Pacific/Guadalcanal", "(GMT+11:00) Central Pacific Standard Time");
            generalTimeZones.put("America/Chicago", "(GMT-06:00) Central Standard Time");
            generalTimeZones.put("America/Mexico_City", "(GMT-06:00) Central Standard Time (Mexico)");
            generalTimeZones.put("Asia/Shanghai", "(GMT+08:00) China Standard Time");
            generalTimeZones.put("Etc/GMT+12", "(GMT-12:00) Dateline Standard Time");
            generalTimeZones.put("Africa/Nairobi", "(GMT+03:00) E. Africa Standard Time");
            generalTimeZones.put("Australia/Brisbane", "(GMT+10:00) E. Australia Standard Time");
            generalTimeZones.put("Asia/Nicosia", "(GMT+02:00) E. Europe Standard Time");
            generalTimeZones.put("America/Sao_Paulo", "(GMT-03:00) E. South America Standard Time");
            generalTimeZones.put("America/New_York", "(GMT-05:00) Eastern Standard Time");
            generalTimeZones.put("Africa/Cairo", "(GMT+02:00) Egypt Standard Time");
            generalTimeZones.put("Africa/Algiers", "(GMT+01:00) Algeria Standard Time");
            generalTimeZones.put("Asia/Yekaterinburg", "(GMT+05:00) Ekaterinburg Standard Time");
            generalTimeZones.put("Pacific/Fiji", "(GMT+12:00) Fiji Standard Time");
            generalTimeZones.put("Europe/Kiev", "(GMT+02:00) FLE Standard Time");
            generalTimeZones.put("Asia/Tbilisi", "(GMT+04:00) Georgian Standard Time");
            generalTimeZones.put("Europe/London", "(GMT+00:00) London Standard Time"); // Updated display name
            generalTimeZones.put("America/Godthab", "(GMT-03:00) Greenland Standard Time");
            generalTimeZones.put("Atlantic/Reykjavik", "(GMT+00:00) Iceland Standard Time"); // Updated display name
            generalTimeZones.put("Europe/Bucharest", "(GMT+02:00) GTB Standard Time");
            generalTimeZones.put("Pacific/Honolulu", "(GMT-10:00) Hawaiian Standard Time");
            generalTimeZones.put("Asia/Calcutta", "(GMT+05:30) India Standard Time");
            generalTimeZones.put("Asia/Tehran", "(GMT+03:00) Iran Standard Time");
            generalTimeZones.put("Asia/Jerusalem", "(GMT+02:00) Israel Standard Time");
            generalTimeZones.put("Asia/Seoul", "(GMT+09:00) Korea Standard Time");
            // generalTimeZones.put("(GMT-02:00) Mid-Atlantic Standard Time", 75); // Not in Unicode CLDR list
            generalTimeZones.put("America/Denver", "(GMT-07:00) Mountain Standard Time");
            generalTimeZones.put("Asia/Rangoon", "(GMT+06:30) Myanmar Standard Time");
            generalTimeZones.put("Asia/Novosibirsk", "(GMT+06:00) N. Central Asia Standard Time");
            generalTimeZones.put("Asia/Katmandu", "(GMT+05:45) Nepal Standard Time");
            generalTimeZones.put("Pacific/Auckland", "(GMT+12:00) New Zealand Standard Time");
            generalTimeZones.put("America/St_Johns", "(GMT-03:30) Newfoundland Standard Time");
            generalTimeZones.put("Asia/Irkutsk", "(GMT+08:00) North Asia East Standard Time");
            generalTimeZones.put("Asia/Krasnoyarsk", "(GMT+07:00) North Asia Standard Time");
            generalTimeZones.put("America/Santiago", "(GMT+04:00) Pacific SA Standard Time");
            generalTimeZones.put("America/Los_Angeles", "(GMT-08:00) Pacific Standard Time");
            generalTimeZones.put("Europe/Paris", "(GMT+01:00) Romance Standard Time");
            generalTimeZones.put("Europe/Moscow", "(GMT+03:00) Russian Standard Time");
            generalTimeZones.put("America/Cayenne", "(GMT-03:00) SA Eastern Standard Time");
            generalTimeZones.put("America/Bogota", "(GMT-05:00) SA Pacific Standard Time");
            generalTimeZones.put("America/La_Paz", "(GMT-04:00) SA Western Standard Time");
            generalTimeZones.put("Pacific/Apia", "(GMT-11:00) Samoa Standard Time");
            generalTimeZones.put("Asia/Bangkok", "(GMT+07:00) SE Asia Standard Time");
            generalTimeZones.put("Asia/Singapore", "(GMT+08:00) Singapore Standard Time");
            generalTimeZones.put("Africa/Johannesburg", "(GMT+02:00) South Africa Standard Time");
            generalTimeZones.put("Asia/Colombo", "(GMT+05:30) Sri Lanka Standard Time");
            generalTimeZones.put("Asia/Taipei", "(GMT+08:00) Taipei Standard Time");
            generalTimeZones.put("Australia/Hobart", "(GMT+10:00) Tasmania Standard Time");
            generalTimeZones.put("Asia/Tokyo", "(GMT+09:00) Tokyo Standard Time");
            generalTimeZones.put("Pacific/Tongatapu", "(GMT+13:00) Tonga Standard Time");
            generalTimeZones.put("America/Indianapolis", "(GMT-05:00) US Eastern Standard Time (Indiana)"); // Updated display name
            generalTimeZones.put("America/Phoenix", "(GMT-07:00) US Mountain Standard Time (Arizona)"); // Updated display name
            generalTimeZones.put("Asia/Vladivostok", "(GMT+10:00) Vladivostok Standard Time");
            generalTimeZones.put("Australia/Perth", "(GMT+08:00) W. Australia Standard Time");
            generalTimeZones.put("Africa/Lagos", "(GMT+01:00) W. Central Africa Standard Time");
            generalTimeZones.put("Europe/Berlin", "(GMT+01:00) W. Europe Standard Time");
            generalTimeZones.put("Asia/Tashkent", "(GMT+05:00) West Asia Standard Time");
            generalTimeZones.put("Pacific/Port_Moresby", "(GMT+10:00) West Pacific Standard Time");
            generalTimeZones.put("Asia/Yakutsk", "(GMT+09:00) Yakutsk Standard Time");
            generalTimeZones.put("America/Caracas", "(GMT-04:30) Venezuelan Standard Time");

            return sortMapByValue(generalTimeZones);
        }
    },

    WINDOWS_TIMEZONE(ConfigValues.DefaultWindowsTimeZone, ConfigurationValues.DefaultWindowsTimeZone, "GMT Standard Time") {
        @Override
        protected Map<String, String> initializeTimeZoneList() {
            Map<String, String> windowsTimeZones = new HashMap<>();
            windowsTimeZones.put("Arabian Standard Time", "(GMT+04:00) Arabian Standard Time");
            windowsTimeZones.put("W. Australia Standard Time", "(GMT+08:00) W. Australia Standard Time");
            windowsTimeZones.put("Caucasus Standard Time", "(GMT+04:00) Caucasus Standard Time");
            windowsTimeZones.put("India Standard Time", "(GMT+05:30) India Standard Time");
            windowsTimeZones.put("Taipei Standard Time", "(GMT+08:00) Taipei Standard Time");
            windowsTimeZones.put("Eastern Standard Time", "(GMT-05:00) Eastern Standard Time");
            windowsTimeZones.put("Russian Standard Time", "(GMT+03:00) Russian Standard Time");
            windowsTimeZones.put("GMT Standard Time", "(GMT) GMT Standard Time");
            windowsTimeZones.put("Tasmania Standard Time", "(GMT+10:00) Tasmania Standard Time");
            windowsTimeZones.put("W. Europe Standard Time", "(GMT+01:00) W. Europe Standard Time");
            windowsTimeZones.put("E. Africa Standard Time", "(GMT+03:00) E. Africa Standard Time");
            windowsTimeZones.put("Alaskan Standard Time", "(GMT-09:00) Alaskan Standard Time");
            windowsTimeZones.put("US Mountain Standard Time", "(GMT-07:00) US Mountain Standard Time");
            windowsTimeZones.put("Iran Standard Time", "(GMT+03:00) Iran Standard Time");
            windowsTimeZones.put("Sri Lanka Standard Time", "(GMT+05:30) Sri Lanka Standard Time");
            windowsTimeZones.put("New Zealand Standard Time", "(GMT+12:00) New Zealand Standard Time");
            windowsTimeZones.put("Central Standard Time (Mexico)", "(GMT-06:00) Central Standard Time (Mexico)");
            windowsTimeZones.put("Arabic Standard Time", "(GMT+03:00) Arabic Standard Time");
            windowsTimeZones.put("Egypt Standard Time", "(GMT+02:00) Egypt Standard Time");
            windowsTimeZones.put("E. South America Standard Time", "(GMT-03:00) E. South America Standard Time");
            windowsTimeZones.put("Hawaiian Standard Time", "(GMT-10:00) Hawaiian Standard Time");
            windowsTimeZones.put("Myanmar Standard Time", "(GMT+06:30) Myanmar Standard Time");
            windowsTimeZones.put("Newfoundland Standard Time", "(GMT-03:30) Newfoundland Standard Time");
            windowsTimeZones.put("US Eastern Standard Time", "(GMT-05:00) US Eastern Standard Time");
            windowsTimeZones.put("Canada Central Standard Time", "(GMT-06:00) Canada Central Standard Time");
            windowsTimeZones.put("Yakutsk Standard Time", "(GMT+09:00) Yakutsk Standard Time");
            windowsTimeZones.put("Central Pacific Standard Time", "(GMT+11:00) Central Pacific Standard Time");
            windowsTimeZones.put("Greenwich Standard Time", "(GMT) Greenwich Standard Time");
            windowsTimeZones.put("North Asia Standard Time", "(GMT+07:00) North Asia Standard Time");
            windowsTimeZones.put("SA Eastern Standard Time", "(GMT-03:00) SA Eastern Standard Time");
            windowsTimeZones.put("Azores Standard Time", "(GMT-10:00) Azores Standard Time");
            windowsTimeZones.put("South Africa Standard Time", "(GMT+02:00) South Africa Standard Time");
            windowsTimeZones.put("SA Western Standard Time", "(GMT-04:00) SA Western Standard Time");
            windowsTimeZones.put("Vladivostok Standard Time", "(GMT+10:00) Vladivostok Standard Time");
            windowsTimeZones.put("SE Asia Standard Time", "(GMT+07:00) SE Asia Standard Time");
            windowsTimeZones.put("China Standard Time", "(GMT+08:00) China Standard Time");
            windowsTimeZones.put("Mid-Atlantic Standard Time", "(GMT-02:00) Mid-Atlantic Standard Time");
            windowsTimeZones.put("Central European Standard Time", "(GMT+01:00) Central European Standard Time");
            windowsTimeZones.put("Central Standard Time", "(GMT-06:00) Central Standard Time");
            windowsTimeZones.put("GTB Standard Time", "(GMT+02:00) GTB Standard Time");
            windowsTimeZones.put("Tokyo Standard Time", "(GMT+09:00) Tokyo Standard Time");
            windowsTimeZones.put("Nepal Standard Time", "(GMT+05:45) Nepal Standard Time");
            windowsTimeZones.put("Greenland Standard Time", "(GMT-03:00) Greenland Standard Time");
            windowsTimeZones.put("W. Central Africa Standard Time", "(GMT+01:00) W. Central Africa Standard Time");
            windowsTimeZones.put("Cape Verde Standard Time", "(GMT-01:00) Cape Verde Standard Time");
            windowsTimeZones.put("Pacific Standard Time", "(GMT-08:00) Pacific Standard Time");
            windowsTimeZones.put("Central Europe Standard Time", "(GMT+01:00) Central Europe Standard Time");
            windowsTimeZones.put("West Asia Standard Time", "(GMT+05:00) West Asia Standard Time");
            windowsTimeZones.put("Israel Standard Time", "(GMT+02:00) Israel Standard Time");
            windowsTimeZones.put("FLE Standard Time", "(GMT+02:00) FLE Standard Time");
            windowsTimeZones.put("Afghanistan Standard Time", "(GMT+04:30) Afghanistan Standard Time");
            windowsTimeZones.put("Romance Standard Time", "(GMT+01:00) Romance Standard Time");
            windowsTimeZones.put("E. Europe Standard Time", "(GMT+02:00) E. Europe Standard Time");
            windowsTimeZones.put("Arab Standard Time", "(GMT+03:00) Arab Standard Time");
            windowsTimeZones.put("Ekaterinburg Standard Time", "(GMT+05:00) Ekaterinburg Standard Time");
            windowsTimeZones.put("Korea Standard Time", "(GMT+09:00) Korea Standard Time");
            windowsTimeZones.put("Cen. Australia Standard Time", "(GMT+09:30) Cen. Australia Standard Time");
            windowsTimeZones.put("Georgian Standard Time", "(GMT+04:00) Georgian Standard Time");
            windowsTimeZones.put("E. Australia Standard Time", "(GMT+10:00) E. Australia Standard Time");
            windowsTimeZones.put("Central America Standard Time", "(GMT-06:00) Central America Standard Time");
            windowsTimeZones.put("North Asia East Standard Time", "(GMT+08:00) North Asia East Standard Time");
            windowsTimeZones.put("Central Asia Standard Time", "(GMT+06:00) Central Asia Standard Time");
            windowsTimeZones.put("Fiji Standard Time", "(GMT+12:00) Fiji Standard Time");
            windowsTimeZones.put("Pacific SA Standard Time", "(GMT+04:00) Pacific SA Standard Time");
            windowsTimeZones.put("Tonga Standard Time", "(GMT+13:00) Tonga Standard Time");
            windowsTimeZones.put("Singapore Standard Time", "(GMT+08:00) Singapore Standard Time");
            windowsTimeZones.put("Mountain Standard Time", "(GMT-07:00) Mountain Standard Time");
            windowsTimeZones.put("Atlantic Standard Time", "(GMT-04:00) Atlantic Standard Time");
            windowsTimeZones.put("Samoa Standard Time", "(GMT-11:00) Samoa Standard Time");
            windowsTimeZones.put("Dateline Standard Time", "(GMT-12:00) Dateline Standard Time");
            windowsTimeZones.put("SA Pacific Standard Time", "(GMT-05:00) SA Pacific Standard Time");
            windowsTimeZones.put("West Pacific Standard Time", "(GMT+10:00) West Pacific Standard Time");
            windowsTimeZones.put("N. Central Asia Standard Time", "(GMT+06:00) N. Central Asia Standard Time");
            windowsTimeZones.put("Venezuela Standard Time", "(GMT-04:30) Venezuela Standard Time");

            return sortMapByValue(windowsTimeZones);
        }
    };

    private ConfigValues defaultTimeZoneConfigKey;
    private ConfigurationValues defaultTimeZoneConfigurationKey;
    private String ultimateFallback;
    private Map<String, String> timeZones;

    TimeZoneType(ConfigValues defaultTimeZoneConfigKey, ConfigurationValues defaultTimeZoneConfigurationKey, String ultimateFallback) {
        this.defaultTimeZoneConfigKey = defaultTimeZoneConfigKey;
        this.defaultTimeZoneConfigurationKey = defaultTimeZoneConfigurationKey;
        this.ultimateFallback = ultimateFallback;
    }

    public ConfigValues getDefaultTimeZoneConfigKey() {
        return defaultTimeZoneConfigKey;
    }

    public ConfigurationValues getDefaultTimeZoneConfigurationKey() {
        return defaultTimeZoneConfigurationKey;
    }

    public String getUltimateFallback() {
        return ultimateFallback;
    }

    public Map<String, String> getTimeZoneList() {
        if (timeZones == null) {
            timeZones = initializeTimeZoneList();
        }
        return timeZones;
    }

    // we get a string like "(GMT-04:30) Afghanistan Standard Time"
    // we use regex to extract the time only and replace it to number
    // in this sample we get -430
    private static final String TimeZoneExtractTimePattern = ".*(GMT[+,-]\\d{2}:\\d{2}).*";

    private static int extractOffsetFromMatch(Match match) {
        return Integer.parseInt(match.groups().get(0).getValue().substring(3).replace(":", "").replace("+", ""));
    }

    public int getStandardOffset(String timeZoneKey) {
        String s = getTimeZoneList().get(timeZoneKey);
        Match match = Regex.match(s, TimeZoneExtractTimePattern);
        int value = 0;
        if(match.success() && match.groups().size() > 0) {
            value = extractOffsetFromMatch(match);
            boolean neg = value < 0;
            value = Math.abs(value);
            value = (value / 100) * 60 + value % 100;
            if(neg) {
                value *= -1;
            }
        }
        return value;
    }

    protected abstract Map<String, String> initializeTimeZoneList();


    private static final class TimeZoneTimeExtractComparator implements Comparator<Map.Entry<String, String>>, Serializable {
        private static final long serialVersionUID = 5250199634939368530L;
        public static final TimeZoneTimeExtractComparator instance = new TimeZoneTimeExtractComparator();


        // we get a string like "(GMT-04:30) Afghanistan Standard Time"
        // we use regex to extract the time only and replace it to number
        // in this sample we get -430
        @Override
        public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
            int a = 0;
            int b = 0;
            Match match1 = Regex.match(o1.toString(), TimeZoneExtractTimePattern);
            Match match2 = Regex.match(o2.toString(), TimeZoneExtractTimePattern);
            if (match1.success() && match1.groups().size() > 0) {
                a = extractOffsetFromMatch(match1);
            }
            if (match2.success() && match2.groups().size() > 0) {
                b = extractOffsetFromMatch(match2);
            }

            return Integer.compare(a, b);
        }
    }

    private static Map<String, String> sortMapByValue(Map<String, String> map) {
        List<Map.Entry<String, String>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, TimeZoneTimeExtractComparator.instance);

        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
