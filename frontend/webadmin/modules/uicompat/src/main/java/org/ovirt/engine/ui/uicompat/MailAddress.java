package org.ovirt.engine.ui.uicompat;

import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;

/**
 * Email address verification.
 * (Imitate 'org.hibernate.validator.constraints.impl.EmailValidator')
 */
public class MailAddress {

    private static String ATOM = "[^\\x00-\\x1F^\\(^\\)^\\<^\\>^\\@^\\,^\\;^\\:^\\\\^\\\"^\\.^\\[^\\]^\\s]"; //$NON-NLS-1$
    private static String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]"; //$NON-NLS-1$

    private static String pattern = "^" + ATOM + "+(\\." + ATOM + "+)*@" + DOMAIN + "|" + IP_DOMAIN + ")$"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    public MailAddress(String value) {
        if (!Regex.isMatch(value, pattern, RegexOptions.IgnoreCase)) {
            throw new RuntimeException();
        }
    }
}
