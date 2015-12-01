package org.ovirt.engine.ui.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.frontend.utils.JsSingleValueStringObject;

public final class VisibleLocalesInfoData extends JsSingleValueStringObject {

    protected VisibleLocalesInfoData() {
    }

    public static List<String> getVisibleList() {
        return getLocaleValues(getValueFrom("visibleLocales")); //$NON-NLS-1$
    }

    private static List<String> getLocaleValues(String localeString) {
        List<String> result = new ArrayList<>();
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
     * @return An array of locales that has been filtered.
     */
    public static String[] getFilteredLocaleNames(List<String> allLocaleNames) {
        List<String> result = new ArrayList<>(allLocaleNames);
        List<String> hiddenList = new ArrayList<>(allLocaleNames);
        hiddenList.removeAll(getVisibleList());
        result.removeAll(hiddenList);
        Collections.sort(result);
        return result.toArray(new String[result.size()]);
    }

}
