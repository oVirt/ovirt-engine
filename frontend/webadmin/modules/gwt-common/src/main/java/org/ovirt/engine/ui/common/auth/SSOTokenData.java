package org.ovirt.engine.ui.common.auth;

import org.ovirt.engine.ui.frontend.utils.JsSingleValueStringObject;

/**
 * Overlay type for {@code ssoToken} global JS object.
 */
public final class SSOTokenData extends JsSingleValueStringObject {

    protected SSOTokenData() {
    }

    public static String getToken() {
        return getValueFrom("ssoToken"); //$NON-NLS-1$
    }

}
