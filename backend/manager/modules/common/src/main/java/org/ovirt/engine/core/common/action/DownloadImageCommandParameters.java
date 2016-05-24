package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;

public class DownloadImageCommandParameters extends ImagesActionsParametersBase {
    private HttpLocationInfo httpLocationInfo;

    public DownloadImageCommandParameters() {
    }

    public HttpLocationInfo getHttpLocationInfo() {
        return httpLocationInfo;
    }

    public void setHttpLocationInfo(HttpLocationInfo httpLocationInfo) {
        this.httpLocationInfo = httpLocationInfo;
    }
}
