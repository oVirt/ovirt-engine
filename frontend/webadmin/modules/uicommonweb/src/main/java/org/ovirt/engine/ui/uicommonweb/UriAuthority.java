package org.ovirt.engine.ui.uicommonweb;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * This is a crude implementation of the authority component of a URI. It does NOT fully conform to the specification of
 * RFC 3986; in particular, it currently ignores the optional user component of the authority. <br>
 * <br>
 * This class adheres to a similar contract as {@link Uri}, please refer to that for details about proper usage.
 */
public class UriAuthority {

    private static final RegExp PATTERN_AUTHORITY = RegExp.compile("^([^:]*)(?::(\\d*))?$", "i"); //$NON-NLS-1$ $NON-NLS-2$

    private boolean valid;
    private String host;
    private String port;

    public UriAuthority(String authority) {
        MatchResult matcher = PATTERN_AUTHORITY.exec(authority == null ? new String() : authority);
        valid = matcher != null;
        if (valid) {
            setHost(matcher.getGroup(1));
            setPort(matcher.getGroup(2));
        }
    }

    public String getStringRepresentation() {
        if (!valid) {
            return null;
        }

        String authority = new String();
        authority += host;
        if (!port.isEmpty()) {
            authority += ':' + port;
        }
        return authority;
    }

    public boolean isValid() {
        return valid;
    }

    public String getHost() {
        return valid ? host : null;
    }

    public void setHost(String host) {
        this.host = (host == null) ? new String() : host;
    }

    public String getPort() {
        return valid ? port : null;
    }

    public void setPort(String port) {
        this.port = (port == null) ? new String() : port;
    }

}
