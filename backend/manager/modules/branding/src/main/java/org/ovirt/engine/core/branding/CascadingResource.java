package org.ovirt.engine.core.branding;

import java.io.File;

/**
 * Representation of a cascading resources configured in a branding resources.properties file.
 */
public class CascadingResource {

    private File file;
    private String contentType;

    public CascadingResource(File file, String contentType) {
        this.file = file;
        this.contentType = contentType;
    }

    public File getFile() {
        return file;
    }

    public String getContentType() {
        return contentType;
    }

}
