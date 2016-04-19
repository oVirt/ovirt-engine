package org.ovirt.engine.ui.common.auth;

import org.ovirt.engine.ui.frontend.utils.JsSingleValueStringObject;

/**
 * Overlay type for {@code ssoToken} global JS object.
 */
public final class SsoTokenData extends JsSingleValueStringObject {

    protected SsoTokenData() {
    }

    public static String getToken() {
        return getValueFrom("ssoToken"); //$NON-NLS-1$
    }

}
