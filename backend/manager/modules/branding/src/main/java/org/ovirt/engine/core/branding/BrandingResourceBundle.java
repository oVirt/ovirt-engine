package org.ovirt.engine.core.branding;

import java.io.IOException;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This is a resource bundle containing the messages for the 'welcome' page.
 * The messages come from the ovirt branding package.
 */
public class BrandingResourceBundle extends ListResourceBundle {

    /**
     * The section key prefix.
     */
    public static final String SECTION = "section";

    /**
     * The row key prefix.
     */
    public static final String ROW = "row";

    /**
     * The extra data key prefix.
     */
    public static final String EXTRA = "extra";

    private final String groupKey;

    /**
     * Private constructor for BrandingResourceBundle.
     * @param brandingGroupKey The group key to use to look up the messages.
     */
    private BrandingResourceBundle(final String brandingGroupKey) {
        this.groupKey = brandingGroupKey;
    }

    @Override
    protected Object[][] getContents() {
        BrandingManager brandingManager = BrandingManager.getInstance();
        Map<String, String> messageMap = brandingManager.getMessageMap(groupKey, getLocale());
        String[][] result = new String[messageMap.size()][2];
        int i = 0;
        for (Map.Entry<String, String> entry: messageMap.entrySet()) {
            result[i][0] = entry.getKey();
            result[i][1] = entry.getValue();
            i++;
        }
        return result;
    }

    /**
     * Get the {@code ResourceBundle.Control} needed to properly initialize the branding resource bundle.
     * @param brandingGroupKey The group key to use to look up the messages.
     * @return A {@code ResourceBundle.Control}
     */
    public static ResourceBundle.Control getBrandingControl(final String brandingGroupKey) {
        return new ResourceBundle.Control() {
            @Override
            public ResourceBundle newBundle(final String baseName, final Locale locale, final String format,
                    final ClassLoader loader, final boolean reload) throws IllegalAccessException,
                    InstantiationException, IOException {
                return new BrandingResourceBundle(brandingGroupKey);
            }
        };
    }
}
