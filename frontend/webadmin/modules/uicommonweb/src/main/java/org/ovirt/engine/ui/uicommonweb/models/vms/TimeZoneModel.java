package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;



public class TimeZoneModel {
    private static final Map<TimeZoneType, Collection<TimeZoneModel>> cachedTimeZoneModels = new HashMap<>();

    public static Collection<TimeZoneModel> getTimeZones(TimeZoneType timeZoneType) {
        return cachedTimeZoneModels.get(timeZoneType);
    }

    static {
        for (TimeZoneType timeZoneType : TimeZoneType.values()) {
            mapListModels(timeZoneType, AsyncDataProvider.getInstance().getTimezones(timeZoneType));
        }
    }

    private static void mapListModels(TimeZoneType timeZoneType, Map<String, String> timeZones) {
        List<TimeZoneModel> models = new ArrayList<>();
        models.add(new TimeZoneModel(null, timeZoneType)); // add empty field representing default engine TZ
        for (Map.Entry<String, String> entry : timeZones.entrySet()) {
            models.add(new TimeZoneModel(entry.getKey(), timeZoneType));
        }
        cachedTimeZoneModels.put(timeZoneType, models);
    }

    private final String timeZoneKey;
    private final TimeZoneType timeZoneType;

    public TimeZoneModel(String timeZoneKey, TimeZoneType timeZoneType) {
        this.timeZoneKey = timeZoneKey;
        this.timeZoneType = timeZoneType;

    }

    public String getTimeZoneKey() {
        return timeZoneKey;
    }

    public boolean isDefault() {
        return timeZoneKey == null;
    }

    public String getDisplayValue() {
        if (isDefault()) {
            String defaultTimeZoneKey = (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(timeZoneType.getDefaultTimeZoneConfigurationKey());
            // check if default timezone is correct
            if (!AsyncDataProvider.getInstance().getTimezones(timeZoneType).containsKey(defaultTimeZoneKey)) {
                // if not show GMT
                defaultTimeZoneKey = timeZoneType.getUltimateFallback();
            }
            return AsyncDataProvider.getInstance().getTimezones(timeZoneType).get(defaultTimeZoneKey);
        } else {
            return AsyncDataProvider.getInstance().getTimezones(timeZoneType).get(timeZoneKey);
        }
    }
}
