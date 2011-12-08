/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

import java.util.Map;
import java.util.HashMap;

/**
 * Mapping of "Windows Standard Format" timezone names to java.util.TimeZone format
 */
public class TimeZoneMapping {

    private static final Map<String, String> windowsToJava = new HashMap<String, String>();
    private static final Map<String, String> javaToWindows = new HashMap<String, String>();

    public static String getJava(String windows) {
        return windows != null ? windowsToJava.get(windows) : null;
    }

    public static String getWindows(String java) {
        return java != null ? javaToWindows.get(java) : null;
    }

    private static void add(String windows, String java) {
        windowsToJava.put(windows, java);
        javaToWindows.put(java, windows);
    }

    static {
        add("AUS Central Standard Time",       "Australia/Darwin");
        add("AUS Eastern Standard Time",       "Australia/Sydney");
        add("Afghanistan Standard Time",       "Asia/Kabul");
        add("Alaskan Standard Time",           "America/Anchorage");
        add("Arab Standard Time",              "Asia/Riyadh");
        add("Arabian Standard Time",           "Asia/Dubai");
        add("Arabic Standard Time",            "Asia/Baghdad");
        add("Argentina Standard Time",         "America/Buenos_Aires");
        add("Armenian Standard Time",          "Asia/Yerevan");
        add("Atlantic Standard Time",          "America/Halifax");
        add("Azerbaijan Standard Time",        "Asia/Baku");
        add("Azores Standard Time",            "Atlantic/Azores");
        add("Canada Central Standard Time",    "America/Regina");
        add("Cape Verde Standard Time",        "Atlantic/Cape_Verde");
        add("Caucasus Standard Time",          "Asia/Yerevan");
        add("Cen. Australia Standard Time",    "Australia/Adelaide");
        add("Central America Standard Time",   "America/Guatemala");
        add("Central Asia Standard Time",      "Asia/Dhaka");
        add("Central Brazilian Standard Time", "America/Manaus");
        add("Central Europe Standard Time",    "Europe/Budapest");
        add("Central European Standard Time",  "Europe/Warsaw");
        add("Central Pacific Standard Time",   "Pacific/Guadalcanal");
        add("Central Standard Time",           "America/Chicago");
        add("Central Standard Time",           "America/Mexico_City");
        add("China Standard Time",             "Asia/Shanghai");
        add("Dateline Standard Time",          "Etc/GMT+12");
        add("E. Africa Standard Time",         "Africa/Nairobi");
        add("E. Australia Standard Time",      "Australia/Brisbane");
        add("E. Europe Standard Time",         "Europe/Minsk");
        add("E. South America Standard Time",  "America/Sao_Paulo");
        add("Eastern Standard Time",           "America/New_York");
        add("Egypt Standard Time",             "Africa/Cairo");
        add("Ekaterinburg Standard Time",      "Asia/Yekaterinburg");
        add("FLE Standard Time",               "Europe/Kiev");
        add("Fiji Standard Time",              "Pacific/Fiji");
        add("GMT Standard Time",               "Europe/London");
        add("GTB Standard Time",               "Europe/Istanbul");
        add("Georgian Standard Time",          "Etc/GMT-3");
        add("Greenland Standard Time",         "America/Godthab");
        add("Greenwich Standard Time",         "Africa/Reykjavik");
        add("Hawaiian Standard Time",          "Pacific/Honolulu");
        add("India Standard Time",             "Asia/Calcutta");
        add("Iran Standard Time",              "Asia/Tehran");
        add("Israel Standard Time",            "Asia/Jerusalem");
        add("Jordan Standard Time",            "Asia/Amman");
        add("Korea Standard Time",             "Asia/Seoul");
        add("Mauritius Standard Time",         "Indian/Mauritius");
        add("Mexico Standard Time",            "America/Mexico_City");
        add("Mexico Standard Time",            "America/Chihuahua");
        add("Mid-Atlantic Standard Time",      "Atlantic/South_Georgia");
        add("Middle East Standard Time",       "Asia/Beirut");
        add("Montevideo Standard Time",        "America/Montevideo");
        add("Morocco Standard Time",           "Africa/Casablanca");
        add("Mountain Standard Time",          "America/Denver");
        add("Mountain Standard Time",          "America/Chihuahua");
        add("Myanmar Standard Time",           "Asia/Rangoon");
        add("N. Central Asia Standard Time",   "Asia/Novosibirsk");
        add("Namibia Standard Time",           "Africa/Windhoek");
        add("Nepal Standard Time",             "Asia/Katmandu");
        add("New Zealand Standard Time",       "Pacific/Auckland");
        add("Newfoundland Standard Time",      "America/St_Johns");
        add("North Asia East Standard Time",   "Asia/Irkutsk");
        add("North Asia Standard Time",        "Asia/Krasnoyarsk");
        add("Pacific SA Standard Time",        "America/Santiago");
        add("Pacific Standard Time",           "America/Los_Angeles");
        add("Pacific Standard Time",           "America/Tijuana");
        add("Pakistan Standard Time",          "Asia/Karachi");
        add("Romance Standard Time",           "Europe/Paris");
        add("Russian Standard Time",           "Europe/Moscow");
        add("SA Eastern Standard Time",        "Etc/GMT+3");
        add("SA Pacific Standard Time",        "America/Bogota");
        add("SA Western Standard Time",        "America/La_Paz");
        add("SE Asia Standard Time",           "Asia/Bangkok");
        add("Samoa Standard Time",             "Pacific/Apia");
        add("Singapore Standard Time",         "Asia/Singapore");
        add("South Africa Standard Time",      "Africa/Johannesburg");
        add("Sri Lanka Standard Time",         "Asia/Colombo");
        add("Taipei Standard Time",            "Asia/Taipei");
        add("Tasmania Standard Time",          "Australia/Hobart");
        add("Tokyo Standard Time",             "Asia/Tokyo");
        add("Tonga Standard Time",             "Pacific/Tongatapu");
        add("US Eastern Standard Time",        "Etc/GMT+5");
        add("US Mountain Standard Time",       "America/Phoenix");
        add("Venezuela Standard Time",         "America/Caracas");
        add("Vladivostok Standard Time",       "Asia/Vladivostok");
        add("W. Australia Standard Time",      "Australia/Perth");
        add("W. Central Africa Standard Time", "Africa/Lagos");
        add("W. Europe Standard Time",         "Europe/Berlin");
        add("West Asia Standard Time",         "Asia/Tashkent");
        add("West Pacific Standard Time",      "Pacific/Port_Moresby");
        add("Yakutsk Standard Time",           "Asia/Yakutsk");
    }
}
