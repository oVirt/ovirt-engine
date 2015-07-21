package org.ovirt.engine.ui.common.system;

import org.ovirt.engine.ui.frontend.utils.JsSingleValueStringObject;

/**
 * Overlay type for {@code engineRpmVersion} global JS object.
 */
public final class EngineRpmVersionData extends JsSingleValueStringObject {

    protected EngineRpmVersionData() {
    }

    public static String getVersion() {
        return getValueFrom("engineRpmVersion"); //$NON-NLS-1$
    }

}
