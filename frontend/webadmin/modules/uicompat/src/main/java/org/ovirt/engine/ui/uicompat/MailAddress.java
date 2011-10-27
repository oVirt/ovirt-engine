package org.ovirt.engine.ui.uicompat;

import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;

/**
 * Email address verification.
 * (Imitate 'org.hibernate.validator.constraints.impl.EmailValidator')
 */
public class MailAddress {

    private static String ATOM = "[^\\x00-\\x1F^\\(^\\)^\\<^\\>^\\@^\\,^\\;^\\:^\\\\^\\\"^\\.^\\[^\\]^\\s]";
    private static String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*";
    private static String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";
    
    private static String pattern = "^" + ATOM + "+(\\." + ATOM + "+)*@" + DOMAIN + "|" + IP_DOMAIN + ")$";
    
    public MailAddress(String value) { 
        if (!Regex.IsMatch(value, pattern, RegexOptions.IgnoreCase)) {            
            throw new RuntimeException();
        }
    }
}
