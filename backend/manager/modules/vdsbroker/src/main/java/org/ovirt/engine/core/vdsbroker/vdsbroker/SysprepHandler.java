package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigUtil;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.TimeZoneInfo;
import org.ovirt.engine.core.dal.dbbroker.generic.DomainsPasswordMap;
import org.ovirt.engine.core.utils.FileUtil;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public final class SysprepHandler {
    private static final Map<String, String> userPerDomain = new HashMap<String, String>();
    private static Map<String, String> passwordPerDomain = new HashMap<String, String>();
    public static final Map<String, Integer> timeZoneIndex = new HashMap<String, Integer>();

    // we get a string like "(GMT-04:30) Afghanistan Standard Time"
    // we use regex to extract the time only and replace it to number
    // in this sample we get -430
    public static String TimzeZoneExtractTimePattern = ".*(GMT[+,-]\\d{2}:\\d{2}).*";

    private static Log log = LogFactory.getLog(SysprepHandler.class);

    static {
        initTimeZones();
        fillUsersMap();
        fillPasswordsMap();
    }

    /**
     * TODO: This code is the exact code as in UsersDomainCacheManagerService, until we have a suitable location that
     * them both can use. Note that every change in one will probably require the same change in the other
     */
    private static void fillUsersMap() {
        String userPerDomainEntry = Config.<String> GetValue(ConfigValues.AdUserName);
        if (!userPerDomainEntry.isEmpty()) {
            String[] domainUserPairs = userPerDomainEntry.split(",");

            for (String domainUserPair : domainUserPairs) {
                String[] parts = domainUserPair.split(":");
                String domain = parts[0].trim().toLowerCase();
                String userName = parts[1].trim();

                userPerDomain.put(domain, userName);
            }
        }
    }

    /**
     * TODO: This code is the exact code as in UsersDomainCacheManagerService, until we have a suitable location that
     * them both can use. Note that every change in one will probably require the same change in the other
     */
    private static void fillPasswordsMap() {
        passwordPerDomain = Config.<DomainsPasswordMap> GetValue(ConfigValues.AdUserPassword);
    }

    public static String GetSysPrep(VM vm, String hostName, String domain, SysPrepParams sysPrepParams) {
        String sysPrepContent = "";
        switch (vm.getStaticData().getos()) {
        case WindowsXP:
            sysPrepContent = LoadFile(Config.<String> GetValue(ConfigValues.SysPrepXPPath));
            sysPrepContent = replace(sysPrepContent, "$ProductKey$", Config.<String> GetValue(ConfigValues.ProductKey));
            break;

        case Windows2003:
            sysPrepContent = LoadFile(Config.<String> GetValue(ConfigValues.SysPrep2K3Path));
            sysPrepContent = replace(sysPrepContent, "$ProductKey$", Config.<String> GetValue(ConfigValues.ProductKey2003));
            break;

        case Windows2003x64:
            sysPrepContent = LoadFile(Config.<String> GetValue(ConfigValues.SysPrep2K3Path));
            sysPrepContent = replace(sysPrepContent, "$ProductKey$", Config.<String> GetValue(ConfigValues.ProductKey2003x64));
            break;

        case Windows2008:
            sysPrepContent = LoadFile(Config.<String> GetValue(ConfigValues.SysPrep2K8Path));
            sysPrepContent = replace(sysPrepContent, "$ProductKey$", Config.<String> GetValue(ConfigValues.ProductKey2008));
            break;

        case Windows2008x64:
            sysPrepContent = LoadFile(Config.<String> GetValue(ConfigValues.SysPrep2K8x64Path));
            sysPrepContent = replace(sysPrepContent, "$ProductKey$", Config.<String> GetValue(ConfigValues.ProductKey2008x64));
            break;
        case Windows2008R2x64:
            sysPrepContent = LoadFile(Config.<String> GetValue(ConfigValues.SysPrep2K8R2Path));
            sysPrepContent = replace(sysPrepContent, "$ProductKey$", Config.<String> GetValue(ConfigValues.ProductKey2008R2));
            break;

        case Windows7:
            sysPrepContent = LoadFile(Config.<String> GetValue(ConfigValues.SysPrepWindows7Path));
            sysPrepContent = replace(sysPrepContent, "$ProductKey$", Config.<String> GetValue(ConfigValues.ProductKeyWindow7));
            break;

        case Windows7x64:
            sysPrepContent = LoadFile(Config.<String> GetValue(ConfigValues.SysPrepWindows7x64Path));
            sysPrepContent = replace(sysPrepContent, "$ProductKey$", Config.<String> GetValue(ConfigValues.ProductKeyWindow7x64));
            break;

        default:
            break;
        }

        if (sysPrepContent.length() > 0) {

            sysPrepContent = populateSysPrepDomainProperties(sysPrepContent, domain, sysPrepParams);
            sysPrepContent = replace(sysPrepContent, "$ComputerName$", hostName != null ? hostName : "");
            sysPrepContent = replace(sysPrepContent, "$AdminPassword$", Config.<String> GetValue(ConfigValues.LocalAdminPassword));

            String timeZone = getTimeZone(vm);

            sysPrepContent = replace(sysPrepContent, "$TimeZone$", timeZone);
            sysPrepContent = replace(sysPrepContent, "$OrgName$", Config.<String> GetValue(ConfigValues.OrganizationName));
        }

        return sysPrepContent;
    }

    private static String populateSysPrepDomainProperties(String sysPrepContent,
            String domain,
            SysPrepParams sysPrepParams) {

        String domainName;
        String adminUserName;
        String adminPassword;

        if (sysPrepParams == null || StringUtils.isEmpty(sysPrepParams.getSysPrepDomainName())) {
            domainName = useDefaultIfNull("domain", domain, "", true);
        } else {
            domainName = sysPrepParams.getSysPrepDomainName();
        }

        if (sysPrepParams == null || sysPrepParams.getSysPrepUserName() == null
                || sysPrepParams.getSysPrepPassword() == null) {
            adminUserName = useDefaultIfNull("user", userPerDomain.get(domainName.toLowerCase()),
                    Config.<String> GetValue(ConfigValues.SysPrepDefaultUser), true);

            adminPassword = useDefaultIfNull("password", passwordPerDomain.get(domainName.toLowerCase()),
                    Config.<String> GetValue(ConfigValues.SysPrepDefaultPassword), false);
        } else {
            adminUserName = sysPrepParams.getSysPrepUserName();
            adminPassword = sysPrepParams.getSysPrepPassword();
        }

        // Get values from SysPrepParams - alternative for username,password and domain.
        sysPrepContent = replace(sysPrepContent, "$JoinDomain$", domainName);
        sysPrepContent = replace(sysPrepContent, "$DomainAdmin$", adminUserName);
        sysPrepContent = replace(sysPrepContent, "$DomainAdminPassword$", adminPassword);

        return sysPrepContent;
    }

    static String replace(String sysPrepContent, String pattern, String value) {
        return sysPrepContent.replaceAll(Pattern.quote(pattern), Matcher.quoteReplacement(value));
    }

    private static String useDefaultIfNull(String key, String value, String defaultValue,
            boolean printDefaultValue) {
        if (value == null && printDefaultValue) {
            log.warnFormat("Could not find value for key '{0}'. Going to use default value of: '{1}'",
                    key, defaultValue);
        }
        return value != null ? value : defaultValue;
    }

    private static String getTimeZone(VM vm) {
        String timeZone;
        // Can be empty if the VM was imported.
        if (StringUtils.isEmpty(vm.getTimeZone())) {
            vm.setTimeZone(TimeZoneInfo.Local.getId());
        }

        switch (vm.getStaticData().getos()) {
        case WindowsXP:
        case Windows2003:
        case Windows2003x64:
            // send correct time zone as sysprep expect to get it (a wierd
            // number)
            timeZone = getTimezoneIndexByKey(vm.getTimeZone());
            break;

        case Windows2008:
        default:
            timeZone = vm.getTimeZone();
            break;
        }

        return timeZone;
    }

    private static String getSysprepDir() {
        return Config.<String> GetValue(ConfigValues.DataDir) + File.separator + "sysprep";
    }

    private static String LoadFile(String fileName) {
        String content = "";
        fileName = ConfigUtil.resolvePath(getSysprepDir(), fileName);
        if (FileUtil.fileExists(fileName)) {
            try {
                content = FileUtil.readAllText(fileName);
            } catch (RuntimeException e) {
                log.error("Failed to read sysprep template: " + fileName, e);
            }
        } else {
            log.error("Sysprep template: " + fileName + " not found");
        }
        return content;
    }

    // exclude 13 and 158 - not in the sysprep documentation!
    // {"Arabic Standard Time", 158},
    // {"Jerusalem Standard Time", 135},
    // {"Mexico Standard Time 2", 13},
    // {"Malay Peninsula Standard Time", 215},

    // TimeZone reference from Microsoft:
    // http://msdn.microsoft.com/en-us/library/ms912391(v=winembedded.11).aspx
    private static void initTimeZones() {
        timeZoneIndex.put("(GMT+04:30) Afghanistan Standard Time", 175);
        timeZoneIndex.put("(GMT-09:00) Alaskan Standard Time", 3);
        timeZoneIndex.put("(GMT+03:00) Arab Standard Time", 150);
        timeZoneIndex.put("(GMT+04:00) Arabian Standard Time", 165);
        timeZoneIndex.put("(GMT+03:00) Arabic Standard Time", 158);
        timeZoneIndex.put("(GMT-04:00) Atlantic Standard Time", 50);
        // timeZoneIndex.put("(GMT+04:00) Azerbaijan Standard Time", xxx);
        timeZoneIndex.put("(GMT-10:00) Azores Standard Time", 80);
        timeZoneIndex.put("(GMT-06:00) Canada Central Standard Time", 25);
        timeZoneIndex.put("(GMT-01:00) Cape Verde Standard Time", 83);
        timeZoneIndex.put("(GMT+04:00) Caucasus Standard Time", 170);
        timeZoneIndex.put("(GMT+09:30) Cen. Australia Standard Time", 250);
        timeZoneIndex.put("(GMT-06:00) Central America Standard Time", 33);
        timeZoneIndex.put("(GMT+06:00) Central Asia Standard Time", 195);
        // timeZoneIndex.put("(GMT-04:00) Central Brazilian Standard Time ", xxx);
        timeZoneIndex.put("(GMT+01:00) Central Europe Standard Time", 95);
        timeZoneIndex.put("(GMT+01:00) Central European Standard Time", 100);
        timeZoneIndex.put("(GMT+11:00) Central Pacific Standard Time", 280);
        timeZoneIndex.put("(GMT-06:00) Central Standard Time", 20);
        timeZoneIndex.put("(GMT-06:00) Central Standard Time (Mexico)", 30);
        timeZoneIndex.put("(GMT+08:00) China Standard Time", 210);
        timeZoneIndex.put("(GMT-12:00) Dateline Standard Time", 0);
        timeZoneIndex.put("(GMT+03:00) E. Africa Standard Time", 155);
        timeZoneIndex.put("(GMT+10:00) E. Australia Standard Time", 260);
        timeZoneIndex.put("(GMT+02:00) E. Europe Standard Time", 115);
        timeZoneIndex.put("(GMT-03:00) E. South America Standard Time", 65);
        timeZoneIndex.put("(GMT-05:00) Eastern Standard Time", 35);
        timeZoneIndex.put("(GMT+02:00) Egypt Standard Time", 120);
        timeZoneIndex.put("(GMT+05:00) Ekaterinburg Standard Time", 180);
        timeZoneIndex.put("(GMT+12:00) Fiji Standard Time", 285);
        timeZoneIndex.put("(GMT+02:00) FLE Standard Time", 125);
        timeZoneIndex.put("(GMT+04:00) Georgian Standard Time", 70);
        timeZoneIndex.put("(GMT) GMT Standard Time", 85);
        timeZoneIndex.put("(GMT-03:00) Greenland Standard Time", 73);
        timeZoneIndex.put("(GMT) Greenwich Standard Time", 90);
        timeZoneIndex.put("(GMT+02:00) GTB Standard Time", 130);
        timeZoneIndex.put("(GMT-10:00) Hawaiian Standard Time", 2);
        timeZoneIndex.put("(GMT+05:30) India Standard Time", 190);
        timeZoneIndex.put("(GMT+03:00) Iran Standard Time", 160);
        timeZoneIndex.put("(GMT+02:00) Israel Standard Time", 135);
        timeZoneIndex.put("(GMT+09:00) Korea Standard Time", 230);
        timeZoneIndex.put("(GMT-02:00) Mid-Atlantic Standard Time", 75);
        timeZoneIndex.put("(GMT-07:00) Mountain Standard Time", 10);
        timeZoneIndex.put("(GMT+06:30) Myanmar Standard Time", 203);
        timeZoneIndex.put("(GMT+06:00) N. Central Asia Standard Time", 201);
        timeZoneIndex.put("(GMT+05:45) Nepal Standard Time", 193);
        timeZoneIndex.put("(GMT+12:00) New Zealand Standard Time", 290);
        timeZoneIndex.put("(GMT-03:30) Newfoundland Standard Time", 60);
        timeZoneIndex.put("(GMT+08:00) North Asia East Standard Time", 227);
        timeZoneIndex.put("(GMT+07:00) North Asia Standard Time", 207);
        timeZoneIndex.put("(GMT+04:00) Pacific SA Standard Time", 56);
        timeZoneIndex.put("(GMT-08:00) Pacific Standard Time", 4);
        timeZoneIndex.put("(GMT+01:00) Romance Standard Time", 105);
        timeZoneIndex.put("(GMT+03:00) Russian Standard Time", 145);
        timeZoneIndex.put("(GMT-03:00) SA Eastern Standard Time", 70);
        timeZoneIndex.put("(GMT-05:00) SA Pacific Standard Time", 45);
        timeZoneIndex.put("(GMT-04:00) SA Western Standard Time", 55);
        timeZoneIndex.put("(GMT-11:00) Samoa Standard Time", 1);
        timeZoneIndex.put("(GMT+07:00) SE Asia Standard Time", 205);
        timeZoneIndex.put("(GMT+08:00) Singapore Standard Time", 215);
        timeZoneIndex.put("(GMT+02:00) South Africa Standard Time", 140);
        timeZoneIndex.put("(GMT+06:00) Sri Lanka Standard Time", 200);
        timeZoneIndex.put("(GMT+08:00) Taipei Standard Time", 220);
        timeZoneIndex.put("(GMT+10:00) Tasmania Standard Time", 265);
        timeZoneIndex.put("(GMT+09:00) Tokyo Standard Time", 235);
        timeZoneIndex.put("(GMT+13:00) Tonga Standard Time", 300);
        timeZoneIndex.put("(GMT-05:00) US Eastern Standard Time", 40);
        timeZoneIndex.put("(GMT-07:00) US Mountain Standard Time", 15);
        timeZoneIndex.put("(GMT+10:00) Vladivostok Standard Time", 270);
        timeZoneIndex.put("(GMT+08:00) W. Australia Standard Time", 225);
        timeZoneIndex.put("(GMT+01:00) W. Central Africa Standard Time", 113);
        timeZoneIndex.put("(GMT+01:00) W. Europe Standard Time", 110);
        timeZoneIndex.put("(GMT+05:00) West Asia Standard Time", 185);
        timeZoneIndex.put("(GMT+10:00) West Pacific Standard Time", 275);
        timeZoneIndex.put("(GMT+09:00) Yakutsk Standard Time", 240);
    }

    // we use:
    // key = "Afghanistan Standard Time"
    // value = "(GMT+04:30) Afghanistan Standard Time"
    public static String getTimezoneKey(String value) {
        return value.substring(value.indexOf(' ') + 1);
    }

    // we get "Afghanistan Standard Time" we return "175"
    // the "Afghanistan Standard Time" is the vm Key that we get from the method getTimezoneKey()
    // "175" is the timezone keys that xp/2003 excpect to get, vista/7/2008 gets "Afghanistan Standard Time"
    public static String getTimezoneIndexByKey(String key) {
        for (String s : timeZoneIndex.keySet()) {
            if (getTimezoneKey(s).equals(key)) {
                return timeZoneIndex.get(s).toString();
            }
        }
        log.errorFormat("getTimezoneIndexByKey: cannot find timezone key '{0}'", key);
        return key;
    }

}

