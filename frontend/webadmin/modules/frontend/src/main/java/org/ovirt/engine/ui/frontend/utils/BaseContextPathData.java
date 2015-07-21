package org.ovirt.engine.ui.frontend.utils;

/**
 * Overlay type for {@code baseContextPath} global JS object.
 */
public final class BaseContextPathData extends JsSingleValueStringObject {

    protected BaseContextPathData() {
    }

    public static String getPath() {
        String value = getValueFrom("baseContextPath"); //$NON-NLS-1$
        assert value != null : "Missing baseContextPath JS object in host page"; //$NON-NLS-1$
        assert value.startsWith("/") : "Value of baseContextPath must start with '/' character"; //$NON-NLS-1$ //$NON-NLS-2$
        return value;
    }

    public static String getRelativePath() {
        String path = getPath();
        return path.startsWith("/") ? path.substring(1) : path; //$NON-NLS-1$
    }

}
