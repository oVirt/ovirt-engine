package org.ovirt.engine.ui.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

public final class VisibleLocalesInfoData extends JavaScriptObject {

    protected VisibleLocalesInfoData() {
    }

    public static native VisibleLocalesInfoData instance() /*-{
        return $wnd.visibleLocales;
    }-*/;

    private native String getValueString() /*-{
        return this.value;
    }-*/;

    public List<String> getVisibleList() {
        return getLocaleValues(getValueString());
    }

    private List<String> getLocaleValues(String localeString) {
        List<String> result = new ArrayList<String>();
        if (localeString != null && !localeString.isEmpty()) {
            String[] locales = localeString.trim().split(" *, *"); //$NON-NLS-1$
            for (String localeKey: locales) {
                result.add(localeKey.replaceAll("-", "_")); //$NON-NLS-1$ $NON-NLS-2$
            }
        }
        return result;
    }

    /**
     * Determine the list of locales to display by comparing the list of visible locales with the list of all locales.
     * Any locale not in the visible list will be removed from the list of all locales.
     *
     * Elements in the lists should be of format xx_YY, which is the standard Java Locale format.
     * @param allLocaleNames List of all available locales. Cannot be null.
     * @param visibleLocales The list of visible locales. Cannot be null
     * @return An array of locales that has been filtered.
     */
    public static String[] getFilteredLocaleNames(List<String> allLocaleNames,
            List<String> visibleLocales) {
        List<String> result = new ArrayList<String>(allLocaleNames);
        List<String> hiddenList = new ArrayList<String>(allLocaleNames);
        hiddenList.removeAll(visibleLocales);
        result.removeAll(hiddenList);
        Collections.sort(result);
        return result.toArray(new String[result.size()]);
    }
}
