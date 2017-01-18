package org.ovirt.engine.core.common.businessentities;

import java.util.Map;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class HttpLocationInfo extends ExternalLocationInfo {

    String url;
    Map<String, String> headers;

    public HttpLocationInfo() {
        super(ConnectionMethod.HTTP);
    }

    public HttpLocationInfo(String url, Map<String, String> headers) {
        this();
        this.url = url;
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("url", url)
                .build();
    }
}
