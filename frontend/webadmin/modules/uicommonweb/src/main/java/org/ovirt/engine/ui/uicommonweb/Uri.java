package org.ovirt.engine.ui.uicommonweb;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * This is a crude implementation of a URI for our current needs. It does NOT conform to the specification of RFC 3986;
 * generally speaking it is not as picky about the characters it accepts, and its partition into components is not as
 * fine. It should be extended as better parsing is needed.<br>
 * <br>
 * Usage: The constructor is to be passed the candidate URI as argument. Before any URI component is accessed (including
 * toString()), the URI should be checked for validity. In case an optional capturing group wasn't matched, its getter
 * will return an empty String. The return value of the getters and toString() will be null iff the URI is invalid.
 */
public class Uri {

    public static final String SCHEME_HTTP = "http"; //$NON-NLS-1$

    private static final RegExp PATTERN_URI =
            RegExp.compile("^(?:(.*)://)?([^/]*)(/.*)?$", "i"); //$NON-NLS-1$ $NON-NLS-2$

    private boolean valid;
    private String scheme;
    private UriAuthority authority;
    private String path;

    public Uri(String uri) {
        MatchResult matcher = PATTERN_URI.exec(uri == null ? new String() : uri);
        valid = matcher != null;
        if (valid) {
            setScheme(matcher.getGroup(1));
            setAuthority(new UriAuthority(matcher.getGroup(2)));
            setPath(matcher.getGroup(3));
        }
    }

    @Override
    public String toString() {
        if (!valid) {
            return null;
        }

        String uri = new String();
        if (!scheme.isEmpty()) {
            uri += scheme + "://"; //$NON-NLS-1$
        }
        uri += authority.toString();
        uri += path;
        return uri;
    }

    public boolean isValid() {
        return valid;
    }

    public String getScheme() {
        return valid ? scheme : null;
    }

    public void setScheme(String scheme) {
        this.scheme = (scheme == null) ? new String() : scheme;
    }

    public UriAuthority getAuthority() {
        return valid ? authority : null;
    }

    public void setAuthority(UriAuthority authority) {
        this.authority = (authority == null) ? new UriAuthority(null) : authority;
        if (!authority.isValid()) {
            valid = false;
        }
    }

    public String getPath() {
        return valid ? path : null;
    }

    public void setPath(String path) {
        this.path = (path == null) ? new String() : path;
    }

}
