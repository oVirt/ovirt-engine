package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;



public class TimeZoneModel {
    private static final Map<TimeZoneType, Iterable<TimeZoneModel>> cachedTimeZoneModels = new HashMap<TimeZoneType, Iterable<TimeZoneModel>>();
    private static final Map<TimeZoneType, Map<String, String>> cachedTimeZones = new HashMap<TimeZoneType, Map<String,String>>();
    private static final Map<TimeZoneType, String> cachedDefaultTimeZoneKeys = new HashMap<TimeZoneType, String>();

    public static Iterable<TimeZoneModel> getTimeZones(TimeZoneType timeZoneType) {
        return cachedTimeZoneModels.get(timeZoneType);
    }

    public static String getDefaultTimeZoneKey(TimeZoneType timeZoneType) {
        return cachedDefaultTimeZoneKeys.get(timeZoneType);
    }

    /**
     * Invoke an action supplied in <code>postUpdateDefaultTimeZoneKey</code>
     * such that it is guaranteed that the given type of default time zone is already loaded and cached
     * before its invocation
     */
    public static void withLoadedDefaultTimeZoneKey(TimeZoneType timeZoneType, Runnable postUpdateDefaultTimeZoneKey) {
        if (getDefaultTimeZoneKey(timeZoneType) == null) {
            updateDefaultTimeZoneKey(timeZoneType, postUpdateDefaultTimeZoneKey);
        } else {
            postUpdateDefaultTimeZoneKey.run();
        }
    }

    /**
     * Invoke an action supplied in <code>postUpdateTimeZones</code>
     * such that it is guaranteed that list of time zones of given type is already loaded and cached
     * before its invocation
     */
    public static void withLoadedTimeZones(TimeZoneType timeZoneType, Runnable postUpdateTimeZones) {
        if (getTimeZones(timeZoneType) == null) {
            updateTimeZones(timeZoneType, postUpdateTimeZones);
        } else {
            postUpdateTimeZones.run();
        }

    }

    private static void updateTimeZones(final TimeZoneType timeZoneType, final Runnable postUpdateTimeZones) {

        AsyncDataProvider.getTimeZoneList(new AsyncQuery(null,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        Map<String, String> timeZones = (Map<String, String>) returnValue;
                        cachedTimeZones.put(timeZoneType, timeZones);
                        mapListModels(timeZoneType, timeZones);
                        postUpdateTimeZones.run();
                    }
                }), timeZoneType);
    }

    private static void mapListModels(TimeZoneType timeZoneType, Map<String, String> timeZones) {
        List<TimeZoneModel> models = new ArrayList<TimeZoneModel>();
        models.add(new TimeZoneModel(null, timeZoneType)); // add empty field representing default engine TZ
        for (Map.Entry<String, String> entry : timeZones.entrySet()) {
            models.add(new TimeZoneModel(entry.getKey(), timeZoneType));
        }
        cachedTimeZoneModels.put(timeZoneType, models);
    }


    private static void updateDefaultTimeZoneKey(final TimeZoneType timeZoneType, final Runnable postUpdateDefaultTimeZone) {

        AsyncDataProvider.getDefaultTimeZone(new AsyncQuery(null,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        cachedDefaultTimeZoneKeys.put(timeZoneType, (String) returnValue);
                        postUpdateDefaultTimeZone.run();

                    }
                }), timeZoneType);
    }

    private String timeZoneKey = null;
    private TimeZoneType timeZoneType = TimeZoneType.GENERAL_TIMEZONE;

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
            final String defaultTimeZoneKey = getDefaultTimeZoneKey(timeZoneType);
            return cachedTimeZones.get(timeZoneType).get(defaultTimeZoneKey);
        } else {
            return cachedTimeZones.get(timeZoneType).get(timeZoneKey);
        }
    }
}
