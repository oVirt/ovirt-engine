package org.ovirt.engine.ui.webadmin.uimode;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.utils.JsSingleValueStringObject;

/**
 * Overlay type for {@code applicationMode} global JS object.
 */
public final class UiModeData extends JsSingleValueStringObject {

    protected UiModeData() {
    }

    public static ApplicationMode getUiMode() {
        String applicationModeValue = getValueFrom("applicationMode"); //$NON-NLS-1$
        try {
            return ApplicationMode.from(Integer.parseInt(applicationModeValue));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}
