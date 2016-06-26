package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;

/**
 * Represents the data retrieved from /api/v2/status call to foreman server
 */
public class ReportedForemanStatus implements Serializable {

    private static final long serialVersionUID = 4520053972647767073L;
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
