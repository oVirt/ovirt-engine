package org.ovirt.engine.core.common.businessentities.gluster;

/**
 * Enum of Gluster Hook Content Type
 *
 * @see GlusterHookEntity
 */
public enum GlusterHookContentType {
    /**
     * Hook Text Content Type
     */
    TEXT,

    /**
     * Hook Binary Content Type
     */
    BINARY;

    public static GlusterHookContentType fromMimeType(String contentType) {
        if (contentType != null && contentType.toLowerCase().startsWith("text/")) {
            return GlusterHookContentType.TEXT;
        } else {
            return GlusterHookContentType.BINARY;
        }
    }
}
