package org.ovirt.engine.core.common;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Match;
import org.ovirt.engine.core.compat.Regex;

public enum TimeZoneType {
    GENERAL_TIMEZONE(ConfigValues.DefaultGeneralTimeZone, "Etc/GMT") {
        Map<String, String> generalTimeZones;

        @Override
        public Map<String, String> getTimeZoneList() {
            return generalTimeZones;
        }

        @Override
        public void init(Map<String, String> timezones) {
            generalTimeZones = sortMapByValue(timezones);
        }
    },

    WINDOWS_TIMEZONE(ConfigValues.DefaultWindowsTimeZone, "GMT Standard Time") {
        Map<String, String> windowsTimeZones;

        @Override
        public Map<String, String> getTimeZoneList() {
            return windowsTimeZones;
        }

        @Override
        public void init(Map<String, String> timezones) {
            windowsTimeZones = sortMapByValue(timezones);
        }
    };

    private ConfigValues defaultTimeZoneConfigurationKey;
    private String ultimateFallback;

    TimeZoneType(ConfigValues defaultTimeZoneConfigurationKey, String ultimateFallback) {
        this.defaultTimeZoneConfigurationKey = defaultTimeZoneConfigurationKey;
        this.ultimateFallback = ultimateFallback;
    }

    public ConfigValues getDefaultTimeZoneConfigurationKey() {
        return defaultTimeZoneConfigurationKey;
    }

    public String getUltimateFallback() {
        return ultimateFallback;
    }


    // we get a string like "(GMT-04:30) Afghanistan Standard Time"
    // we use regex to extract the time only and replace it to number
    // in this sample we get -430
    private static final String TimeZoneExtractTimePattern = ".*(GMT[+,-]\\d{2}:\\d{2}).*";

    private static int extractOffsetFromMatch(Match match) {
        return Integer.parseInt(match.groups().get(0).getValue().substring(3).replace(":", "").replace("+", ""));
    }

    public abstract Map<String, String> getTimeZoneList();

    public abstract void init(Map<String, String> timezones);


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
        return map.entrySet().stream()
                .sorted(TimeZoneTimeExtractComparator.instance)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }
}
