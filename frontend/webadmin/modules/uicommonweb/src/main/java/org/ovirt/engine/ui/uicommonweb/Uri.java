package org.ovirt.engine.ui.uicommonweb;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * This is a crude implementation of a URI for our current needs. It does NOT conform to the specification of RFC 3986;
 * generally speaking it is not as picky about the characters it accepts, and its partition into components is not as
 * fine. It should be extended as better parsing is needed.<br>
 * <br>
 * Usage: The constructor is to be passed the candidate URI as argument. Before any URI component is accessed, the URI
 * should be checked for validity. In case an optional capturing group wasn't matched, its getter will return an empty
 * String. The return value of the getters will be null iff the URI is invalid.
 */
public class Uri {

    public static final String SCHEME_HTTP = "http"; //$NON-NLS-1$
    public static final String SCHEME_HTTPS = "https"; //$NON-NLS-1$

    private static final RegExp PATTERN_URI =
            RegExp.compile("^(?:(.*)://)?([^/]*)(/.*)?$", "i"); //$NON-NLS-1$ $NON-NLS-2$

    private boolean valid;
    private String scheme;
    private UriAuthority authority;
    private String path;
    private String stringRepresentation;

    public Uri(String uri) {
        initialize(uri);
    }

    public Uri(String scheme, String authority, String path) {
        StringBuilder stringBuilder = new StringBuilder();
        if (scheme != null  && !scheme.isEmpty()) {
            stringBuilder.append(scheme);
            stringBuilder.append("://"); //$NON-NLS-1$
        }
        if (authority != null) {
            stringBuilder.append(authority);
        }
        if (path != null) {
            stringBuilder.append(path);
        }
        String candidate = stringBuilder.toString();

        initialize(candidate);
    }

    private void initialize(String uri) {
        MatchResult matcher = PATTERN_URI.exec(uri == null ? "" : uri); //$NON-NLS-1$
        valid = matcher != null;
        if (valid) {
            setScheme(matcher.getGroup(1));
            setAuthority(new UriAuthority(matcher.getGroup(2)));
            setPath(matcher.getGroup(3));
            stringRepresentation = uri;
        }
    }

    public String getStringRepresentation() {
        if (!valid) {
            return null;
        }
        return stringRepresentation;
    }

    public boolean isValid() {
        return valid;
    }

    public String getScheme() {
        return valid ? scheme : null;
    }

    private void setScheme(String scheme) {
        this.scheme = (scheme == null) ? "" : scheme; //$NON-NLS-1$
    }

    public UriAuthority getAuthority() {
        return valid ? authority : null;
    }

    private void setAuthority(UriAuthority authority) {
        this.authority = (authority == null) ? new UriAuthority(null) : authority;
        if (!this.authority.isValid()) {
            valid = false;
        }
    }

    public String getPath() {
        return valid ? path : null;
    }

    public String[] getPathSegments() {
        return getPath() != null ? getPath().split("/") : null; //$NON-NLS-1$
    }

    private void setPath(String path) {
        this.path = (path == null) ? "" : path; //$NON-NLS-1$
    }

}
